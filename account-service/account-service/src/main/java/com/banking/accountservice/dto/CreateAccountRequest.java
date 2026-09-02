package com.banking.accountservice.dto;

import java.math.BigDecimal;

import com.banking.accountservice.entity.AccountType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CreateAccountRequest {

	@NotBlank(message = "Account holder name is required")
	private String accountHolderName;

	@NotBlank(message = "Account holder email is required")
	@Email(message = "Invalid email format")
	private String accountHolderEmail;

	@NotBlank(message = "Account holder phone is required")
	private String accountHolderPhone;

	@NotNull(message = "Account type is required")
	private AccountType accountType;

	@NotNull(message = "Initial deposit is required")
	@Positive(message = "Initial deposit must be a positive value")
	private BigDecimal initialDeposit;

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
	
	
}
