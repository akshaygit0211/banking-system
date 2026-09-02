package com.banking.frauddectectionservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class FraudCheckResults {
	

	private boolean isFraud;
	private String reason;

	public boolean isFraud() {
		return isFraud;
	}
	public void setFraudulent(boolean isFraud) {
		this.isFraud = isFraud;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public FraudCheckResults(boolean isFraud, String reason) {
		super();
		this.isFraud = isFraud;
		this.reason = reason;
	}
	public FraudCheckResults() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
	
	
	
	

}
