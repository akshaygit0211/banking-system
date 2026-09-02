package com.banking.frauddectectionservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.banking.frauddectectionservice.client.AccountServiceClient;
import com.banking.frauddectectionservice.model.FraudCheckResults;

import lombok.RequiredArgsConstructor;

@Service
public class FraudDetectionService {

	@Autowired
	private  AccountServiceClient accountServiceClient;

	
	private static final String VERIFICATION_REQUIRED_TOPIC = "verification-required";
	private static final String FRAUD_CHECK_CLEAN_RESULT_TOPIC = "fraud.check.clean";
	
	
	@Autowired
	private  RedisTemplate<String,String> redisTemplate;
	
	@Autowired
	private  KafkaTemplate<String, Map<String, Object>> kafkaTemplate;
	
	
	@Value("${fraud.max-transactions-perminute}")
	private int maxTransactionsPerMinute;
	
	
	@Value("${fraud.suspicious-amount-multiplier}")
	private double suspicipousAmountMultiplier; //if transaction amount exceeds 3x average amount, it is suspicious
	
	@Value("${fraud.max-balance-percentage}")
	private double maxBalancePercentage; //if transaction amount exceeds 90% of account balance, it is suspicious
	
	private final Logger log = LoggerFactory.getLogger(FraudDetectionService.class);
	public void checkTransaction(Map<String, Object> payload) {

		log.info("Received transaction for fraud check {}", payload.get("id"));
		String transactionId = payload.get("id").toString();
		String senderAccountNumber = payload.get("senderAccountNumber").toString();
		//String receiverAccountNumber = payload.get("receiverAccountNumber").toString();
		BigDecimal amount = new BigDecimal(payload.get("amount").toString());		
		log.info("Checking transaction for fraud {}", transactionId);
		log.info("Sender account number {}", senderAccountNumber);
		//.info("Receiver account number",receiverAccountNumber);
		//log.info("Amount",amount);
		
		BigDecimal senderBalance = accountServiceClient.getBalance(senderAccountNumber);
		
		log.info("checking transaction {} account {} amount {} balance ", transactionId , senderAccountNumber, amount, senderBalance); 
		
	FraudCheckResults result =	performFraudCheck(senderAccountNumber, amount, senderBalance);
	
	if(result.isFraud()) {
		log.warn("Suspicious Activity detected: account {}  reason {}  - requesting OTP verification,",senderAccountNumber, result.getReason());
		//send notification to account service to block the account
		
		
		Map<String, Object> verificationEvent = new java.util.HashMap<>();
		verificationEvent.put("transactionId", transactionId);
		verificationEvent.put("senderAccountNumber", senderAccountNumber);
		verificationEvent.put("reason", result.getReason());
		verificationEvent.put("amount", amount);
		
		kafkaTemplate.send(VERIFICATION_REQUIRED_TOPIC, transactionId, verificationEvent);
		 
	}
	else {
		//transaction is clean
		log.info("Transaction is clean {}", transactionId);
		
		Map<String, Object> transactionCleanEvent = new java.util.HashMap<>();
		transactionCleanEvent.put("transactionId", transactionId);
		transactionCleanEvent.put("isFraud", false);
		transactionCleanEvent.put("reason", null);

		kafkaTemplate.send(FRAUD_CHECK_CLEAN_RESULT_TOPIC, transactionId, transactionCleanEvent); 

		
	}


}
	private FraudCheckResults performFraudCheck(String senderAccountNumber, BigDecimal amount,
			BigDecimal senderBalance) {

		//pattern 1 : Velocity check 
		if(isVelocityExceeded(senderAccountNumber)) {
			return new FraudCheckResults(true, " too many transaction in 60 sec and Velocity limit exceeded");
		}
		
		//Pattern 2 : Amount check //if result true
		if(isAmountSuspicious(senderAccountNumber,amount)) {
			return new FraudCheckResults(true, " Unusual transsaction amount  exceeds 3x your average amount");
			
		}
		
		//Pattern 3 : Balance check 90% of account balance
		if(senderBalance.compareTo(BigDecimal.ZERO) >0 && isBalanceCheckFailed(senderBalance,amount))  {
			return new FraudCheckResults(true, "Transaction exceeded 90% of account balance");
		}
		
		return new FraudCheckResults(false, null);
	}

	
	//to keep the count we have redis 
	private boolean isVelocityExceeded(String senderAccountNumber) {
		
		String key = "fraud.velocity:" + senderAccountNumber;
		Long count = redisTemplate.opsForValue().increment(key);
		
		if(count!=null && count == 1) {
			redisTemplate.expire(key, java.time.Duration.ofSeconds(60));
		}
		
		//on which count we have to block the transaction, lets say 5 transactions in 60 sec
		log.info("Velocity check for account {} count {}", senderAccountNumber, count,maxTransactionsPerMinute);

		return count != null && count > maxTransactionsPerMinute;
	}
	 
	
	private boolean isAmountSuspicious(String senderAccountNumber, BigDecimal amount) {

	    String avgKey = "fraud.avg_amount:" + senderAccountNumber;

	    String avgStr = redisTemplate.opsForValue().get(avgKey);

	    if (avgStr == null) {
	        redisTemplate.opsForValue().set(avgKey, amount.toString());
	        return false;
	    }

	    BigDecimal avgAmount = new BigDecimal(avgStr);

	    BigDecimal threshold =
	            avgAmount.multiply(BigDecimal.valueOf(suspicipousAmountMultiplier));

	    BigDecimal newAvg =
	            avgAmount.add(amount)
	                     .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

	    redisTemplate.opsForValue().set(avgKey, newAvg.toString());

	    log.info(
	            "Amount check for account {} amount {} avgAmount {} threshold {}",
	            senderAccountNumber,
	            amount,
	            avgAmount,
	            threshold
	    );

	    return amount.compareTo(threshold) > 0;
	}
	private boolean isBalanceCheckFailed(BigDecimal senderBalance, BigDecimal amount) {
		
		BigDecimal maxAllowed = senderBalance.multiply(BigDecimal.valueOf(maxBalancePercentage));
		
		
		log.info("Balance check for account balance {} amount {} maxBalancePercentage {}", senderBalance, amount,maxAllowed, amount.compareTo(maxAllowed)>0);
		return amount.compareTo(maxAllowed)>0;
	}
}
