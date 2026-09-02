package com.banking.paymentservice.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.banking.paymentservice.dto.CreatePaymentRequest;
import com.banking.paymentservice.dto.PaymentOrderResponse;
import com.banking.paymentservice.entity.Payment;
import com.banking.paymentservice.entity.PaymentStatus;
import com.banking.paymentservice.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import jakarta.validation.Valid;

@Service
public class PaymentService {
	
	private final Logger log = LoggerFactory.getLogger(PaymentService.class);
	
	private  PaymentRepository paymentRepository; 
	
	private KafkaTemplate<String,Object> kafkaTemplate;
	
	private static final String PAYMENT_COMPLETED_TOPIC = "payment.completed";
	private static final String PAYMENT_FAILED_TOPIC = "payment.failed";
	
	
	@Value("{razorpay.key-id}")
	private String keyId;
	
	@Value("{razorpay.key-secret}")
	private String keySecret;
	
	
	/**
	 * Create RazorPay payment Order
	 * 
	 * 
	 * Flow:
	 * 1.Create Order in razorpay
	 * 2. Save Payment record in DB
	 * 3.Return order details to frontend
	 * 4.FrontEnd show RazorPay Checkout page
	 * 5. User pays
	 * 6. RazorPay calls webhooks
	 * 
	 * @param request
	 * @return
	 * @throws RazorpayException 
	 */
	public  PaymentOrderResponse createPaymentOrder( CreatePaymentRequest request) throws RazorpayException {

		log.info("Creating payment order for account :{} amount {} ",
				request.getAccountNumber(),request.getAmount());
		
		RazorpayClient razorpayClient = new RazorpayClient(keyId,keySecret);
		//converting amount into its smallest units dollar to cents , rupee to paisa
		 int convertedAmount = request.getAmount().multiply(BigDecimal.valueOf(100)).intValue();
		 
		 JSONObject orderRequest = new JSONObject();
		 orderRequest.put("amount", convertedAmount);
		 orderRequest.put("currency", "USD/INR");
		 orderRequest.put("receiptId",System.currentTimeMillis()+ UUID.randomUUID().toString()
				 .replace("-","").substring(0,10));


		 Order razorPayOrder =  razorpayClient.orders.create(orderRequest);
		 log.info("RazorPay Order Created ",razorPayOrder.get("id").toString());
		 
		 //Save Payment Record
		 
		 Payment payment = new Payment();
		 payment.setRazorpayOrderId(razorPayOrder.get("id").toString());
		 payment.setAccountNumber(request.getAccountNumber());
		 payment.setAmount(request.getAmount());
		 payment.setCurrency("USD/INR");
		 payment.setStatus(PaymentStatus.CREATED);
		 payment.setDescription(request.getDescription());
		 
		 Payment savedPayment = paymentRepository.save(payment);
		 
		 
		return new PaymentOrderResponse(
				savedPayment.getPaymentId(), razorPayOrder.get("id").toString(), 
				request.getAmount(), 
				"USD/INR",
				"CREATED",
				keyId
				);
	}
	
	//Webhook captures events
	
	public void handleWebHooks(Map<String, Object> payload) {
		
		log.info("Received RazorPay  webhook:{} ",payload.get("event"));
		
		String event = (String)payload.get("event");
		
		if("payment.captured".equals(event)) {
			handlePaymentSuccess(payload);
		}
		
		else if ("payment.failed".equals(event)) {
			handlePaymentFailure(payload);
		}
		
	}

	

	
	private void handlePaymentSuccess(Map<String, Object> payload) {
		// TODO Auto-generated method stub
		
		try {
			
		Map<String, Object> paymentData =	extractPaymentData(payload);
		String orderId = (String) paymentData.get("order_id");
		String paymentId = (String) paymentData.get("paymentId");
		
		Payment payment = paymentRepository.findByRazorpayOrderId(orderId).orElseThrow(() -> new RuntimeException("Payment not Found for order ::"+orderId));
		payment.setRazorpayPaymentId(paymentId);
		payment.setStatus(PaymentStatus.COMPLETED);
		paymentRepository.save(payment);
		
		//publish Payment Completed Eventk
		
		Map<String, Object> event =  new HashMap<>();
		event.put("paymentId", payment.getPaymentId());
		event.put("accountNumber", payment.getAccountNumber());
		event.put("amount", payment.getAmount());
		event.put("paymentId",paymentId);
		
		kafkaTemplate.send(PAYMENT_COMPLETED_TOPIC,payment.getPaymentId(),event);
		log.info("payment completed ::",payment.getPaymentId());
		
		} catch (Exception e) {
			log.error("Error handling payment success :",e.getMessage());
		}
	}

	private void handlePaymentFailure(Map<String, Object> payload) {

		try {
			
			Map<String, Object> paymentData =	extractPaymentData(payload);
			String orderId = (String) paymentData.get("order_id");
			String paymentId = (String) paymentData.get("paymentId");
			
			Payment payment = paymentRepository.findByRazorpayOrderId(orderId)
					.orElseThrow(() -> new RuntimeException("Payment not Found for order ::"+orderId));
			
			payment.setFailureReason("Payment Failed via RazorPay");
			payment.setStatus(PaymentStatus.FAILED);
			paymentRepository.save(payment);

			
			Map<String, Object> event =  new HashMap<>();
			event.put("paymentId", payment.getPaymentId());
			event.put("accountNumber", payment.getAccountNumber());
			event.put("amount", payment.getAmount());
			event.put("paymentId",paymentId);
			event.put("reason", "Payment Failed via RazorPay");
			
			kafkaTemplate.send(PAYMENT_FAILED_TOPIC,payment.getPaymentId(),event);
			log.warn("payment failed ::",payment.getPaymentId());

		} catch (Exception e) {
			log.error("Error handling payment success :",e.getMessage());
		}
		
	}

	private Map<String, Object> extractPaymentData(Map<String, Object> payload) {
		// TODO Auto-generated method stub
		
		Map<String, Object> entity = (Map<String, Object>) payload.get("payload");
		
		Map<String, Object> paymentWrapper = (Map<String, Object>) entity.get("payment");
		
		return (Map<String, Object>) paymentWrapper.get("entity");
	
	}

	

}
