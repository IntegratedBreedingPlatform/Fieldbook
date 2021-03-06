
package com.efficio.fieldbook.web.trial.bean;

import java.io.Serializable;

public class ExpDesignValidationOutput implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -2261802820353959484L;
	private boolean isValid;
	private String message;
	private boolean userConfirmationRequired;

	public ExpDesignValidationOutput() {
		super();
	}

	public ExpDesignValidationOutput(boolean isValid, String message) {
		super();
		this.isValid = isValid;
		this.message = message;
	}

	public boolean isValid() {
		return this.isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	
	public boolean isUserConfirmationRequired() {
		return userConfirmationRequired;
	}

	
	public void setUserConfirmationRequired(boolean userConfirmationRequired) {
		this.userConfirmationRequired = userConfirmationRequired;
	}

}
