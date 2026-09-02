package com.banking.frauddectectionservice.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.KafkaListeners;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;


@Service
public class FraudDetectionEventConsumer {

	private final Logger log = LoggerFactory.getLogger(FraudDetectionEventConsumer.class); 
	
	@Autowired
	private FraudDetectionService fraudDetectionService;

	
	
	 /**
	  * Listen to transaction.initiated topic
	  * Every transaction goes through fraud check before completing
	  * @param payload
	  */
	  
	
	@KafkaListener(topics = "transaction.initiated")
	public void consumeTransactionInitiated(@Payload Map<String, Object> payload) {
		
	    System.out.println("******** LISTENER INVOKED ********");
	    log.info("Payload = {}", payload);
	    System.out.println(payload);

	    log.info("Received transaction for fraud check {}", payload.get("id"));		
		
		try {
			fraudDetectionService.checkTransaction(payload);
			
		} catch (Exception e) {
		    log.error("Error while processing transaction", e);
		}
		
		
	}
	
	
}

