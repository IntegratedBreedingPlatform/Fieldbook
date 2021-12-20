package com.efficio.fieldbook.web.exception;

public class FieldbookRequestValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String errorCode;
	private final Object[] params;

	public FieldbookRequestValidationException(String errorCode, final Object[] params) {
		this.errorCode = errorCode;
		this.params = params;
	}

	public String getErrorCode() {
		return this.errorCode;
	}

	public Object[] getParams() {
		return params;
	}
}
