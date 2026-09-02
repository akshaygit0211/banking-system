package com.banking.paymentservice.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banking.paymentservice.dto.CreatePaymentRequest;
import com.banking.paymentservice.dto.PaymentOrderResponse;
import com.banking.paymentservice.service.PaymentService;
import com.razorpay.RazorpayException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private  PaymentService paymentService;

@PostMapping("/createOrder")
public ResponseEntity<PaymentOrderResponse> createPaymentOrder(@Valid @RequestBody CreatePaymentRequest request) throws RazorpayException{ 
	return ResponseEntity.status(HttpStatus.CREATED)
			.body(paymentService.createPaymentOrder(request));
		
}
		


		@PostMapping("/webhook")
		public ResponseEntity<String> handleWebHook(
			
				 @RequestBody Map<String, Object> payload){ {
			paymentService.handleWebHooks(payload);
			return ResponseEntity.ok("Web Hook Proceesed");	 
				}
}
}
