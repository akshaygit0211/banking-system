package com.banking.transactionservice.event;

import java.math.BigDecimal;

public class TransactionCompletedEvent {

	
private String transactionId;
	
	private String senderAccountNumber;
	
	private String receiverAccountNumber;
	
	private String description;
	
	private BigDecimal amount;

	public String gettransactionId() {
		return transactionId;
	}

	public void setId(String transactionId) {
		this.transactionId = transactionId;
	}

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public TransactionCompletedEvent() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TransactionCompletedEvent(String transactionId, String senderAccountNumber, String receiverAccountNumber,
			String description, BigDecimal amount) {
		super();
		this.transactionId = transactionId;
		this.senderAccountNumber = senderAccountNumber;
		this.receiverAccountNumber = receiverAccountNumber;
		this.description = description;
		this.amount = amount;
	}
	
	
	
	
}

