package com.banking.transactionservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.banking.transactionservice.client.AccountServiceClient;
import com.banking.transactionservice.dto.TransactionResponse;
import com.banking.transactionservice.dto.TransferRequest;
import com.banking.transactionservice.entity.Transaction;
import com.banking.transactionservice.entity.TransactionStatus;
import com.banking.transactionservice.entity.TransactionType;
import com.banking.transactionservice.event.TransactionCompletedEvent;
import com.banking.transactionservice.event.TransactionInitiatedEvent;
import com.banking.transactionservice.repository.TransactionRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
public class TransactionService {
	
	private final Logger log = LoggerFactory.getLogger(TransactionService.class);
	
	@Autowired
	private TransactionRepository transactionRepository;
	
	@Autowired
	private AccountServiceClient accountServiceClient;
	
	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;
	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	
	private static final String TRANSACTION_INITIATED_TOPIC = "transaction.initiated";
	private static final String TRANSACTION_COMPLETED_TOPIC = "transaction.completed";
	private static final String TRANSACTION_REFUNDED_TOPIC = "transaction.refunded";
	private static final String TRANSACTION_FRAUD_DETECTED_TOPIC = "fraud.detected";


	
	/**
	 * SAGA Step -1 :initiate Transfer
	 * deducts From sender via Feign Client
	 * Saves transaction as PROCESSING
	 * publish event to kafka for fraud check   
	 */

	public TransactionResponse  tranfer( TransferRequest request) {
		// TODO Auto-generated method stub
		
		log.info("SAGA starts transfer",request.getSenderAccountNumber() ,"->",request.getReceiverAccountNumber() ,"amount" ,request.getAmount());
		//SAGA step -1 -> deduct from sender
		String deductBalance = accountServiceClient.deductBalance(request.getSenderAccountNumber(), 
				request.getAmount());
		
		// save transaction as PROCESSING
		
		Transaction transaction = new Transaction();
		
		transaction.setSenderAccountNumber(request.getSenderAccountNumber());
		transaction.setReceiverAccountNumber(request.getReceiverAccountNumber());
		transaction.setAmount(request.getAmount());
		transaction.setCompletedAt(LocalDateTime.now());
		transaction.setDescription(request.getDescription());
		transaction.setReferenceNumber(UUID.randomUUID().toString());
		transaction.setTransactionType(TransactionType.TRANSFER);
		transaction.setTransactionStatus(TransactionStatus.PROCESSING);
		
		Transaction savedTransaction = transactionRepository.save(transaction);
		log.info("transaction saved as PROCESSING");
		//System.out.println("transaction saved as PROCESSING");
		
		//SAGA STEP 2 -> publish for fraud check
		
		TransactionInitiatedEvent event = new TransactionInitiatedEvent();
		event.setAmount(savedTransaction.getAmount());
		event.setDescription(savedTransaction.getDescription());
		event.setId(savedTransaction.getId());
		event.setSenderAccountNumber(savedTransaction.getSenderAccountNumber());
		event.setReceiverAccountNumber(savedTransaction.getReceiverAccountNumber());
		
		//kafkaTemplate.send(TRANSACTION_INITIATED_TOPIC,savedTransaction.getId(),event);
		kafkaTemplate.send(TRANSACTION_INITIATED_TOPIC, savedTransaction.getId(), event)
	    .whenComplete((result, ex) -> {
	        if (ex != null) {
	            log.error("Failed to publish transaction.initiated event", ex);
	        } else {
	            log.info(
	                "Published to topic={} partition={} offset={}",
	                result.getRecordMetadata().topic(),
	                result.getRecordMetadata().partition(),
	                result.getRecordMetadata().offset()
	            );
	        }
	    });
		log.info(" SAGA STEP 2 TRANSACTION_INITIATED EVENT published ",savedTransaction.getId());
		
		
		
		return mapToResponse(savedTransaction);
	}

	private TransactionResponse mapToResponse(Transaction savedTransaction) {
	
		TransactionResponse transactionResponse = new TransactionResponse();
		transactionResponse.setId(savedTransaction.getId());
		transactionResponse.setAmount(savedTransaction.getAmount());
		transactionResponse.setCompletedAt(savedTransaction.getCompletedAt());
		transactionResponse.setCreatedAt(savedTransaction.getCreatedAt());
		transactionResponse.setDescription(savedTransaction.getDescription());
		transactionResponse.setFailureReason(savedTransaction.getFailureReason());
		transactionResponse.setReceiverAccountNumber(savedTransaction.getReceiverAccountNumber());
		transactionResponse.setSenderAccountNumber(savedTransaction.getSenderAccountNumber());
		transactionResponse.setTransactionStatus(savedTransaction.getTransactionStatus());
		transactionResponse.setTransactionType(savedTransaction.getTransactionType());
		
		
		
		return transactionResponse;
	}

	public TransactionResponse getTransaction(String transactionId) {
		
		/*
		 * Transaction byId = transactionRepository.findById(transactionId).
		 * orElseThrow(() -> new RuntimeException("Transaction not Found" +
		 * transactionId));
		 */
		
		
		return mapToResponse(transactionRepository.
									findById(transactionId)
									.orElseThrow( () ->  new RuntimeException("Transaction not Found "+transactionId)));
		
			
	}

	public List<TransactionResponse> getTransactionHistory(String accountNumber) {
		
		//esa likhte aane ko hona mann se 
	 transactionRepository.findBySenderAccountNumberOrderByCreatedAtDesc(accountNumber)
	 .stream().map(this  :: mapToResponse)
	 .collect(Collectors.toList());
		
		
		return null;
	}

	public TransactionResponse  veifyOtp(String transactionId, String otp) {
		// TODO Auto-generated method stub
		log.info("Verifying OTP for transaction {} otp {}",transactionId,otp);
		
		Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new RuntimeException("Transaction not Found" + transactionId));
		
		String otpkey = "verification:otp:"+transactionId;
		
		
		String 	storedotp= redisTemplate.opsForValue().get(otpkey);
		
		if(storedotp == null) {
			log.warn("OTP for transaction {} is expired or not found",transactionId);
			compensateTransaction(transaction, "OTP expired - transaction cancelled and refunded");
			return mapToResponse(transaction);
		}
		
		if(!storedotp.equals(otp)) {
			log.warn("OTP for transaction {} is invalid",transactionId);
			redisTemplate.delete(otpkey);
			blockAccountAndCompensate(transaction, "OTP invalid - transaction cancelled and account blocked for security reasons");
			return mapToResponse(transaction);
			
		} 
		
		log.info("OTP verifired for transaction {} is valid",transactionId); 
		redisTemplate.delete(otpkey);
		completeTransaction(transaction); 
		return mapToResponse(transaction);
		
		
		
	}

	
	

	private void compensateTransaction(Transaction transaction, String reason) {
		log.info("SAGA compensation - refnding transaction {} reason {}",transaction.getId(),reason);
		transaction.getTransactionStatus();
		transaction.getAmount();
		
		//CREDIT money back to sender	synchronously by feign client using account service -> credit balance
		
		accountServiceClient.creditBalance(transaction.getSenderAccountNumber(), transaction.getAmount()); 
		transaction.setTransactionStatus(TransactionStatus.FLAGGED);
		transaction.setFailureReason(reason+"SAGA compensation ,amount refunded at:"+LocalDateTime.now());
		transactionRepository.save(transaction);
		
		//PUBLISH refund event -> Notification service will alert user about refund
		
		Map<String,Object> refundEvent = new java.util.HashMap<>();
		refundEvent.put("transactionId", transaction.getId());
		refundEvent.put("senderAccountNumber", transaction.getSenderAccountNumber());
		refundEvent.put("amount", transaction.getAmount());
		refundEvent.put("reason", reason);
		
		
		kafkaTemplate.send(TRANSACTION_REFUNDED_TOPIC,transaction.getId(),refundEvent);
		log.info("SAGA compensation complete- refnding transaction {} reason {} refund event published",transaction.getAmount() + transaction.getSenderAccountNumber());
	}
	
	private void blockAccountAndCompensate(Transaction transaction, String reason) {
		
		//publish fraud.detected  ->Account Service will block the account.
		Map<String ,Object> fraudDetectedEvent = new java.util.HashMap<>();
		fraudDetectedEvent.put("transactionId",transaction.getId());
		fraudDetectedEvent.put("accountNumber",transaction.getSenderAccountNumber());
		fraudDetectedEvent.put("reason",reason);
		
		kafkaTemplate.send("TRANSACTION_FRAUD_DETECTED_TOPIC",transaction.getSenderAccountNumber(),fraudDetectedEvent);
		log.warn(" fraud detected event published -> account{} will be blocked , kindly contacted to your bank",transaction.getSenderAccountNumber());
		
		//SAGA compensation -> refund the transaction
		compensateTransaction(transaction,reason);
		
	}
	
	private void completeTransaction(Transaction transaction) {
	
		transaction.setTransactionStatus(TransactionStatus.COMPLETED);
		transaction.setCompletedAt(LocalDateTime.now());
		transactionRepository.save(transaction);
		
		TransactionCompletedEvent completedEvent = new TransactionCompletedEvent(
				
				transaction.getId(),
					transaction.getSenderAccountNumber(),
					transaction.getReceiverAccountNumber(),
					transaction.getDescription(),
					transaction.getAmount()
					
				
				);
		
		kafkaTemplate.send(TRANSACTION_COMPLETED_TOPIC,transaction.getId(),completedEvent);
		
		log.info("SAGA complete - transaction {} completed and event published",transaction.getId());
		
	}

	public void processCleanResult(String transactionId) {
		
		
		Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new RuntimeException("Transaction not Found" + transactionId));

		if(transaction.getTransactionStatus() != TransactionStatus.PROCESSING) {
			 log.warn("Transaction {} is not in processing state, it is in {} state, ignoring verification-required event",transactionId,transaction.getTransactionStatus());
			 return;
		 }
		completeTransaction(transaction);
		
	}	
}
