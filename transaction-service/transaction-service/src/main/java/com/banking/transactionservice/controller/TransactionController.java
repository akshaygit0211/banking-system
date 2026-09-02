package com.banking.transactionservice.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banking.transactionservice.dto.TransactionResponse;
import com.banking.transactionservice.dto.TransferRequest;
import com.banking.transactionservice.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/transactions")

public class TransactionController {

	private final Logger log = LoggerFactory.getLogger(TransactionController.class); 
	
	@Autowired
	private TransactionService transactionService;
	
	
	@PostMapping("/transfer")
	 public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request){
		 
		 return  ResponseEntity.status(HttpStatus.CREATED)
				 .body(transactionService.tranfer(request));
	 }
	 
	
	@GetMapping("/{transactionId}")
	public ResponseEntity<TransactionResponse> getTransaction(@PathVariable String transactionId){
		return ResponseEntity.ok(transactionService.getTransaction(transactionId));
		
	}
	
	
	@GetMapping("/account/{accountNumber}")
	public ResponseEntity<List<TransactionResponse>> getTransactionHistory(@PathVariable String accountNumber){
		return ResponseEntity.ok(transactionService.getTransactionHistory(accountNumber));
		
	}
	
	
	@PostMapping("/{transactionId}/verify")
	public ResponseEntity<TransactionResponse> verifyOtp(
			@PathVariable String transactionId,
			@RequestParam String otp){
		
		log.info("otp verification request  - transaction ",transactionId);
		
		return ResponseEntity.ok(transactionService.veifyOtp(transactionId,otp));
	}
		
		
		
	 
}
