package com.banking.accountservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;


import com.banking.accountservice.entity.AccountStatus;
import com.banking.accountservice.entity.AccountType;


public class AccountResponse {

	private String id;
	private String accountHolderName;
	private String accountNumber;
	private String accountHolderEmail;
	private String accountHolderPhone; 
	private AccountType accountType;
	private BigDecimal initialDeposit;
	private AccountStatus accountStatus;
	private BigDecimal balance;
	private BigDecimal dailyTransactionLimit;
	private LocalDateTime createdAt;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAccountHolderName() {
		return accountHolderName;
	}
	public void setAccountHolderName(String accountHolderName) {
		this.accountHolderName = accountHolderName;
	}
	public String getAccountHolderEmail() {
		return accountHolderEmail;
	}
	public void setAccountHolderEmail(String accountHolderEmail) {
		this.accountHolderEmail = accountHolderEmail;
	}
	public String getAccountHolderPhone() {
		return accountHolderPhone;
	}
	public void setAccountHolderPhone(String accountHolderPhone) {
		this.accountHolderPhone = accountHolderPhone;
	}
	public AccountType getAccountType() {
		return accountType;
	}
	public void setAccountType(AccountType accountType) {
		this.accountType = accountType;
	}
	public BigDecimal getInitialDeposit() {
		return initialDeposit;
	}
	public void setInitialDeposit(BigDecimal initialDeposit) {
		this.initialDeposit = initialDeposit;
	}
	public AccountStatus getAccountStatus() {
		return accountStatus;
	}
	public void setAccountStatus(AccountStatus accountStatus) {
		this.accountStatus = accountStatus;
	}
	public BigDecimal getBalance() {
		return balance;
	}
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	public BigDecimal getDailyTransactionLimit() {
		return dailyTransactionLimit;
	}
	public void setDailyTransactionLimit(BigDecimal dailyTransactionLimit) {
		this.dailyTransactionLimit = dailyTransactionLimit;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	
	
	
}
