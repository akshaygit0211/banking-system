package com.banking.notificationservice.service;

import java.math.BigDecimal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service

public class NotificationService {

	private final Logger log = LoggerFactory.getLogger(NotificationService.class);


	@KafkaListener(topics = "transaction.otp.generated") 
		public void consumeotpGenerated(@Payload Map<String, Object> payload) {
			
			try {
			 
			
			String transactionId = (String) payload.get("transactionId");
			String accountNumber = (String) payload.get("senderAccountNumber");
			String otp = (String) payload.get("otp");
			String reason = (String) payload.get("reason");
			String amount = (String) payload.get("amount");
			
			sendAlert("Transaction VERIFICATION REQUIRED",
					String.format("Suspicious activity detected on your account"+
					"Reason : %s A Transaction of %s  is pending verification Your OTP is: %s  Valid for 5 minutes: If this wasn't you  ignore this message"
					));
			
			sendAlert(accountNumber,
					" SUSPICIOUS ACTIVITY DETECTED",
					String.format("Your account %s has been blocked"+"Reason %s" + "plase contact your bank immediately",
							accountNumber,reason
							));
			
			log.info("Received transaction.otp.generated event for transaction {} account {} otp {}", transactionId, accountNumber, otp);
			}
			// Here you can implement the logic to send the OTP to the user via email or SMS
			catch (Exception e) {
				log.error("Error while sending notification", e);
			}
		}


	private void sendAlert(String string, String format) {
		// TODO Auto-generated method stub
		
	}


	@KafkaListener(topics = "transaction.completed")
	public void consumeTransactionCompleted(@Payload Map<String, Object> payload) {
	    log.info("Received transaction.completed event: {}", payload);

		try {
		 
		
		String transactionId = (String) payload.get("transactionId");
		String senderAccount = (String) payload.get("senderAccountNumber");
		String receiverAccount = (String) payload.get("receiverAccountNumber");
		BigDecimal amount = new BigDecimal(payload.get("amount").toString());		

		//String amount = (String) payload.get("amount");
		
		//Debit Alert
		sendAlert(senderAccount,
				"DEBIT ALERT",
				String.format("%s debited from account %s",
						amount,senderAccount
						));
		
		sendAlert(receiverAccount,
				"Credit ALERT",
				String.format("%s credited from account %s",
						amount,receiverAccount
						));
		}
		// Here you can implement the logic to send the OTP to the user via email or SMS
		catch (Exception e) {
			log.error("Error while sending transaction notification", e.getMessage());
		}
	}
	
	
	@KafkaListener(topics = "fraud.detected")
	public void consumeFraudDetected(@Payload Map<String, Object> payload) {
		
		try {
		 
		
		String transactionId = (String) payload.get("transactionId");
		String accountNumber = (String) payload.get("accountNumber");
		String reason = (String) payload.get("reason");

		String receiverAccount = (String) payload.get("receriverAccountNumber");

		String amount = (String) payload.get("amount");
		
		//Debit Alert
		sendAlert(accountNumber,
				" SUSPICIOUS ACTIVITY DETECTED",
				String.format("Your account %s has been blocked"+"Reason %s" + "plase contact your bank immediately",
						accountNumber,reason
						));
		
		}
		// Here you can implement the logic to send the OTP to the user via email or SMS
		catch (Exception e) {
			log.error("Error while sending fraud alert", e.getMessage());
		}
	}
	
	@KafkaListener(topics = "transaction.refunded")
	public void consumeTransactionRefunded(@Payload Map<String, Object> payload) {
		
		try {
		 
			String transactionId = (String) payload.get("transactionId");
			String senderAccount = (String) payload.get("senderAccountNumber");
			String receiverAccount = (String) payload.get("receriverAccountNumber");
			String reason = (String) payload.get("reason");
			String amount = (String) payload.get("amount");

		
		//Debit Alert
		sendAlert(senderAccount,
				" REFUND PROCESSED",
				String.format("Your transaction of %s has been cancelled"+ "Reason %s"+"%s has been refunded to account  %s" ,
						amount,reason,amount,senderAccount
						));
		
		}
		// Here you can implement the logic to send the OTP to the user via email or SMS
		catch (Exception e) {
			log.error("Error while sending refund notification", e.getMessage());
		}
	}
	
	@KafkaListener(topics = "payment.completed")
	public void consumePaymentCompleted(@Payload Map<String, Object> payload) {
		
		try {
		 
			String amount = (String) payload.get("amount");
			String accountNumber = (String) payload.get("accountNumber");

		
		//Debit Alert
		sendAlert(accountNumber,
				" PAYMENT  SUCCESSFUL",
				String.format("Your Payment of %s completed "+
				"RazorPay Id %s",
						amount,payload.get("razorpayPaymentId")
						));
		
		}
		// Here you can implement the logic to send the OTP to the user via email or SMS
		catch (Exception e) {
			log.error("Error while sending payment complete notification", e.getMessage());
		}
	}
	
	@KafkaListener(topics = "payment.failed")
	public void consumePaymentFailed(@Payload Map<String, Object> payload) {
		
		try {
		 
			String amount = (String) payload.get("amount");
			String accountNumber = (String) payload.get("accountNumber");

		
		//Debit Alert
		sendAlert(accountNumber,
				" Your  payment of %s could not be processed",
				String.format("Payment of %s failed "+
				"RazorPay Id %s",
						amount,payload.get("razorpayPaymentId")
						));
		
		}
		// Here you can implement the logic to send the OTP to the user via email or SMS
		catch (Exception e) {
			log.error("Error while sending payment failed  notification", e.getMessage());
		}
	}
	
	
		private void sendAlert(String accountNumber, String subject, String message) {
		// TODO Auto-generated method stub
		
			log.info("------------------------------------");
			log.info("Account : {} ",accountNumber);
			log.info("Subject : {}",subject);
			log.info("Message :{}" ,message);
			log.info("-------------------------------------------");
			
			
			
	}


	
}

