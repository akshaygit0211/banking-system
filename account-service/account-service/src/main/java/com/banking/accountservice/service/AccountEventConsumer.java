package com.banking.accountservice.service;

import java.math.BigDecimal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
public class AccountEventConsumer {
	
	private static final Logger log = LoggerFactory.getLogger(AccountEventConsumer.class);

	@Autowired
	private  AccountService accountService;

	/**
	 * Consume transaction.completed evenet from kafka
	 * Credits receriver account
	 */
	
	
	@KafkaListener(topics = "transaction.completed")
	public void consumeTransactionCompleted(
			@Payload Map<String, Object> payload) {
	
		try {
			
			String receiverAccount = (String) payload.get("receiverAccountNumber");
		//	BigDecimal amount = (BigDecimal)payload.get("amount");
			BigDecimal amount = new BigDecimal(payload.get("amount").toString());		

			
			accountService.creditBalance(receiverAccount, amount);
			
			
		} catch (Exception e) {
			log.error("Error while crediting amount::",e.getMessage());
		}
	}
	
	
	/**
	 * Consume fraud detection evenet from kafka
	 * Block the account if fraud detected
	 */
	
	@KafkaListener(topics = "fraud.detected")
	public void consumeFraudDetected(
			@Payload Map<String, Object> payload) {
	
		try {
			
			String accountNumber = (String) payload.get("accountNumber");
			log.info("Fraud detected for account number: {}", accountNumber);
			
			accountService.blockAccount(accountNumber);
			
			
		} catch (Exception e) {
			log.error("Error while crediting amount::",e.getMessage());
		}
	}
	
	
	
	
	
}
