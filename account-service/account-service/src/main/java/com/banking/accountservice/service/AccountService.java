package com.banking.accountservice.service;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.entity.AccountStatus;
import com.banking.accountservice.entity.AccountType;
import com.banking.accountservice.exception.AccountAlreadyExistsException;
import com.banking.accountservice.exception.AccountNotFoundException;
import com.banking.accountservice.repository.AccountRepository;

@Service

public class AccountService {

	private static final Logger log = LoggerFactory.getLogger(AccountService.class);
	private static final SecureRandom secureRandom = new SecureRandom();

	@Autowired
	private AccountRepository accountRepository;

	public AccountResponse createAccountService(CreateAccountRequest request) {
		log.info("Creating account for: {}", request.getAccountHolderEmail());

		boolean existsByAccountHolderEmail = accountRepository
				.existsByAccountHolderEmail(request.getAccountHolderEmail());
		if (existsByAccountHolderEmail) {
			throw new AccountAlreadyExistsException(
					"Account is already exists with this email :" + request.getAccountHolderEmail());
		}

		Account account = new Account();
		account.setAccountHolderName(request.getAccountHolderName());
		account.setAccountHolderEmail(request.getAccountHolderEmail());
		account.setAccountHolderPhone(request.getAccountHolderPhone());
		account.setAccountType(request.getAccountType());
		account.setBalance(request.getInitialDeposit());
		account.setAccountStatus(AccountStatus.ACTIVE);
		account.setAccountNumber(generateAccountNumber());
		
		AccountType accountType = request.getAccountType();

		account.setDailyTransactionLimit(
				request.getAccountType() == AccountType.SAVINGS ? new BigDecimal(10000) : new BigDecimal(50000));

		Account savedAccount = accountRepository.save(account);
		log.info("Account created successfully with account number: {}", savedAccount.getAccountNumber());

		return mapToAccountResponse(savedAccount);
	}

	// Generate a unique 12 digit account number
	private String generateAccountNumber() {

		String accountNumber;

		do {
			long number = secureRandom.nextLong(1_000_000_000_000L);
			accountNumber = String.format("%012d", number);

		} while (accountRepository.existsByAccountNumber(accountNumber));

		log.info("accountNumber::::: {}", accountNumber);	
		return accountNumber;
	}

	private AccountResponse mapToAccountResponse(Account savedAccount) {

		AccountResponse response = new AccountResponse();
		response.setId(savedAccount.getId());
		response.setAccountHolderName(savedAccount.getAccountHolderName());
		response.setAccountHolderEmail(savedAccount.getAccountHolderEmail());
		response.setAccountHolderPhone(savedAccount.getAccountHolderPhone());
		response.setAccountType(savedAccount.getAccountType());
		response.setInitialDeposit(savedAccount.getBalance());
		response.setAccountStatus(savedAccount.getAccountStatus());
		 response.setAccountNumber(savedAccount.getAccountNumber());
		response.setDailyTransactionLimit(savedAccount.getDailyTransactionLimit());
		response.setCreatedAt(LocalDateTime.now());
		response.setBalance(savedAccount.getBalance());
	
		return response;
	}

	/**
	 * Block account - get Account details by account number -
	 */

	public AccountResponse getAccount(String accountNumber) {

		Account byAccountNumber = accountRepository.findByAccountNumber(accountNumber).orElseThrow(
				() -> new AccountNotFoundException("Account not found with account number :" + accountNumber));

		return mapToAccountResponse(byAccountNumber);

	}

	/**
	 * Block account - get balance by account number -
	 */

	public BigDecimal getBalance(String accountNumber) {

		Account byAccountNumber = accountRepository.findByAccountNumber(accountNumber).orElseThrow(
				() -> new AccountNotFoundException("Account not found with account number :" + accountNumber));

		return byAccountNumber.getBalance();
	}

	/**
	 * Block account - called by fraud detection service via kafka
	 */

	public void blockAccount(String accountNumber) {

		Account byAccountNumber = accountRepository.findByAccountNumber(accountNumber).orElseThrow(
				() -> new AccountNotFoundException("Account not found with account number :" + accountNumber));

		byAccountNumber.setAccountStatus(AccountStatus.BLOCKED);
		accountRepository.save(byAccountNumber);

		log.info("Account blocked successfully with account number: {}", accountNumber);

	}
	
	/**
	 * Deduct balance from sender account
	 * Called by Transaction Service when transaction is initiated
	 */

	public void deductBalance(String accountNumber, BigDecimal amount) {
		
		log.info("Deducting balance from account number: {} for amount: {}", accountNumber, amount);
		System.out.println("accountNumber:::"+accountNumber +":::amount:::"+ amount);
		Account byAccountNumber = accountRepository.findByAccountNumber(accountNumber).orElseThrow(
				() -> new AccountNotFoundException("Account not found with account number :" + accountNumber));

			
		if(byAccountNumber.getAccountStatus() != AccountStatus.ACTIVE) {
			throw new RuntimeException("Account is  not active: " + accountNumber);
		}
		
		BigDecimal currentBalance = byAccountNumber.getBalance();
		
		
		if (currentBalance.compareTo(amount) < 0) {
			throw new IllegalArgumentException("Insufficient balance in account: " + accountNumber);
		}

		byAccountNumber.setBalance(currentBalance.subtract(amount));
		accountRepository.save(byAccountNumber);

		log.info("Balance deducted successfully from account number: {}", accountNumber);
		log.info("New balance for account number {}: {}", accountNumber, byAccountNumber.getBalance());
		
	}

	
	/**
	 * Credit balance
	 * Called by Transaction Service via kafka
	 */

	public void creditBalance(String accountNumber, BigDecimal amount) {
		
		log.info("Crediting balance to account number: {} for amount: {}", accountNumber, amount);
		
		Account byAccountNumber = accountRepository.findByAccountNumber(accountNumber).orElseThrow(
				() -> new AccountNotFoundException("Account not found with account number :" + accountNumber));
		
		byAccountNumber.setBalance(byAccountNumber.getBalance().add(amount));
		accountRepository.save(byAccountNumber);
		
		log.info("Balance credited successfully to account number: {}", accountNumber);
	
		
	
		
		
	}
}