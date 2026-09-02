package com.banking.transactionservice.service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.banking.transactionservice.entity.Transaction;
import com.banking.transactionservice.entity.TransactionStatus;
import com.banking.transactionservice.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service

	public class  TransactionEventConsumer {
	
	@Autowired
	private  TransactionRepository transactionRepository;
	
	private final Logger log = LoggerFactory.getLogger(TransactionEventConsumer.class);

	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	
	@Autowired
	private  KafkaTemplate<String, Map<String, Object>> kafkaTemplate;
	
	@Autowired
	private  TransactionService transactionService;
	
	
	private static final String TRANSACTION_OTP_GENEREATED_TOPIC = "transaction.otp.generated";
	private static long  OTP_EXPIRES_MINUTES = 5;
	/**
	 * 
	 * 
	 * Consum verification-required
	 * Generate OTP and send to user via otp
	 * @param payload
	 */

	@KafkaListener(topics = "verification-required")
	public void consumeVerificationRequired(@Payload Map<String, Object> payload) {
		// TODO Auto-generated method stub
		try {
			
		 
	String transactionId =	(String) payload.get("transactionId");
	String accountNumber =	(String) payload.get("accountNunber");

	String reason =	(String) payload.get("reason");
	

	log.info("Received verification-required event for transaction {} account {} reason {}",transactionId,accountNumber,reason);
	
	Transaction transaction = transactionRepository.findById(transactionId)
	 .orElseThrow(() -> new RuntimeException("Transaction not found"+transactionId));
	 
	 //we are verifying the user only if the transaction.status is processing if it is not processing it means it is completed or it is flagged
	// in this  we need to add idempotency guard to prevent two time credit to my receiver or my sender
	 
	 
	 if(transaction.getTransactionStatus() != TransactionStatus.PROCESSING) {
		 log.warn("Transaction {} is not in processing state, it is in {} state, ignoring verification-required event",transactionId,transaction.getTransactionStatus());
		 return;
	 }
	 //Generate six digit OTP and send to user via otp service
	 String otp = String.format("%06d", (int)Math.random() * 900000 + 100000);
	 
		//store this otp in redis with transactionId as key and expiry of 5 minutes
	 	String otpkey = "verification:otp:"+transactionId;
	 	redisTemplate.opsForValue().set(otpkey, otpkey,OTP_EXPIRES_MINUTES,TimeUnit.MINUTES);
	 	
	 	//Update transaction status to PENDING_VERIFICATION
	 	
	 	transaction.setTransactionStatus(TransactionStatus.PENDING_VERIFICATION);
	 	transactionRepository.save(transaction);
	 	
	 	log.info("OTP generated for  transaction {} expires in{}  min",transactionId,OTP_EXPIRES_MINUTES);
	 	
	 	//notify User via otp service
	 	//otp should be sent to user via sms or email,  	by publishing event and that evene will consumees by notification service 
		
	 	
	 	Map<String, Object> otpEvent = new java.util.HashMap<>();
	 	otpEvent.put("transactionId", transactionId);
	 	otpEvent.put("accountNumber", accountNumber);
	 	otpEvent.put("otp", otp);
	 	otpEvent.put("reason", reason);
	 	otpEvent.put("amount", payload.get("amount"));
	 	
	 	kafkaTemplate.send(TRANSACTION_OTP_GENEREATED_TOPIC, transactionId, otpEvent);
		}
		
	 catch (Exception e) {
			log.error("Error processing verification-required event", e);
		}
	}
	
	@KafkaListener(topics = "fraud.check.clean")
	public void consumeFraudCheckCleanResult(@Payload Map<String, Object> payload) {
		
		System.out.println("fraud.check.clean\":::::"+payload);
		try {
			
			String transactionId =	(String) payload.get("transactionId");
			String accountNumber =	(String) payload.get("accountNunber");
			String reason =	(String) payload.get("reason");
			
			transactionService.processCleanResult(transactionId);
			
			
							
		} catch (Exception e) {
		log.error("Error processing fraud.check.clean event", e);	
		}
		
	
	}

}
