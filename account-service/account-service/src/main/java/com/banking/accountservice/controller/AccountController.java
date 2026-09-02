package com.banking.accountservice.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/v1/accounts")
@Slf4j

public class AccountController {

	@Autowired
	private  AccountService accountService;
	
	@PostMapping("/create")
	public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
		
		return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccountService(request));
	}
	
	@GetMapping("/{accountNumber}")
	public ResponseEntity<AccountResponse> getAccountByAccountNummber(@PathVariable String accountNumber) {
		
		return ResponseEntity.ok(accountService.getAccount(accountNumber));
	}
	
	@GetMapping("/{accountNumber}/balance")
	public ResponseEntity<BigDecimal> getBalanceByAccountNummber(@PathVariable String accountNumber) {
		
		return ResponseEntity.ok(accountService.getBalance(accountNumber));
	}
	
	@PutMapping("/{accountNumber}/balance")
	public ResponseEntity<String> blockAccount(@PathVariable String accountNumber) {
		accountService.blockAccount(accountNumber);
		
		return ResponseEntity.ok("Account blocked successfully");
	}
	
	
	
	
	/**
	 * SAGA  STEP 1 - Deduct Balance
	 * called by Transaction Service when transaction is initiated
	 */

	@PutMapping("/{accountNumber}/deduct")
	public ResponseEntity<String> deductBalance(@PathVariable String accountNumber,
			@RequestParam BigDecimal amount) {
	
		accountService.deductBalance(accountNumber, amount);
		return ResponseEntity.ok("Balance deducted successfully");
	}
	
	/**
	 * SAGA  STEP 4 - Deduct Balance
	 * called by Transaction Service in two SCNERIO
	 * 1. Fraud deducted -> refund sender (undo step 1)
	 * 2. Transaction completed -> credit receiver
	 */
	@PutMapping("/{accountNumber}/credit")
		public ResponseEntity<String> creditBalance(@PathVariable String accountNumber,
				@RequestParam BigDecimal amount) {
		
			accountService.creditBalance(accountNumber, amount);
			return ResponseEntity.ok("Balance credited successfully");
		}


	
	
}
