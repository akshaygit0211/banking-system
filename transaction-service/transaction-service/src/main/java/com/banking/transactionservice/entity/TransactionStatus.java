package com.banking.transactionservice.entity;

/**
 * Transaction Lifecycle FLow;
 * 
 * Pending -> Processing -> Completed (clean transaction)
 * 				    			-> Pending_Verification (suspicious detected)
 * 								 -> Flagged (SAGA refund)
 * 							-> Failed
 * 							-> Flagged
 * 		
 */

public enum TransactionStatus {

	COMPLETED,FAILED,PENDING,PROCESSING,PENDING_VERIFICATION,FLAGGED
}
