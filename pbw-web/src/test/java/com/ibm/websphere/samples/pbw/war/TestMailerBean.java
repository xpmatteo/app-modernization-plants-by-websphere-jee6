// ABOUTME: Test stub for MailerBean used in unit tests
// ABOUTME: Provides a simple implementation that can throw exceptions on demand
package com.ibm.websphere.samples.pbw.war;

import com.ibm.websphere.samples.pbw.ejb.MailerAppException;
import com.ibm.websphere.samples.pbw.ejb.MailerBean;
import com.ibm.websphere.samples.pbw.jpa.Customer;

/**
 * Test stub for MailerBean that allows control of behavior in tests.
 */
public class TestMailerBean extends MailerBean {

	private boolean shouldThrowMailerException = false;
	private boolean shouldThrowGenericException = false;
	private int callCount = 0;
	private Customer lastCustomer = null;
	private String lastOrderId = null;

	@Override
	public void createAndSendMail(Customer customer, String orderId) throws MailerAppException {
		callCount++;
		lastCustomer = customer;
		lastOrderId = orderId;

		if (shouldThrowMailerException) {
			throw new MailerAppException("Test MailerAppException");
		}
		if (shouldThrowGenericException) {
			throw new RuntimeException("Test generic exception");
		}
		// Otherwise do nothing (successful send)
	}

	public void setShouldThrowMailerException(boolean should) {
		this.shouldThrowMailerException = should;
	}

	public void setShouldThrowGenericException(boolean should) {
		this.shouldThrowGenericException = should;
	}

	public int getCallCount() {
		return callCount;
	}

	public Customer getLastCustomer() {
		return lastCustomer;
	}

	public String getLastOrderId() {
		return lastOrderId;
	}

	public void reset() {
		callCount = 0;
		lastCustomer = null;
		lastOrderId = null;
		shouldThrowMailerException = false;
		shouldThrowGenericException = false;
	}
}
