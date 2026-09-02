package com.banking.transactionservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;



@Data
public class TransferRequest {

	@NotBlank(message = "Sender account number is required")
	private String senderAccountNumber;
	
	@NotBlank(message = "Receiver account number is required")
	private String receiverAccountNumber;
	
	
	@Positive(message = "amount must be positive")
	private BigDecimal amount;
	
	
	private String description;


	public String getSenderAccountNumber() {
		return senderAccountNumber;
	}


	public void setSenderAccountNumber(String senderAccountNumber) {
		this.senderAccountNumber = senderAccountNumber;
	}


	public String getReceiverAccountNumber() {
		return receiverAccountNumber;
	}


	public void setReceiverAccountNumber(String receiverAccountNumber) {
		this.receiverAccountNumber = receiverAccountNumber;
	}


	public BigDecimal getAmount() {
		return amount;
	}


	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}
	
	
	
	
	
}