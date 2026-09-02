package com.banking.paymentservice.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentOrderResponse {

	private String paymentId;
	private String eazorPayOrderId;
	private BigDecimal amount;
	private String currency;
	private String status;
	private String razorPayKeyId;
	public String getPaymentId() {
		return paymentId;
	}
	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}
	public String getEazorPayOrderId() {
		return eazorPayOrderId;
	}
	public void setEazorPayOrderId(String eazorPayOrderId) {
		this.eazorPayOrderId = eazorPayOrderId;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getRazorPayKeyId() {
		return razorPayKeyId;
	}
	public void setRazorPayKeyId(String razorPayKeyId) {
		this.razorPayKeyId = razorPayKeyId;
	}
	public PaymentOrderResponse(String paymentId, String eazorPayOrderId, BigDecimal amount, String currency,
			String status, String razorPayKeyId) {
		super();
		this.paymentId = paymentId;
		this.eazorPayOrderId = eazorPayOrderId;
		this.amount = amount;
		this.currency = currency;
		this.status = status;
		this.razorPayKeyId = razorPayKeyId;
	}
	public PaymentOrderResponse() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
	
}
