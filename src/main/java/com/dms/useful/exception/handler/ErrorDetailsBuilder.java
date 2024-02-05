package com.dms.useful.exception.handler;

import java.time.LocalDateTime;

/**
 * This {@code ErrorDetailsBuilder} class is a builder of {@code ErrorDetails}
 * class
 * 
 * @author Diorgenes Morais
 * @version 2.0.0
 * @since 2.0.0
 */
public class ErrorDetailsBuilder {

	private int status;
	private String type;
	private String title;
	private String detail;
	private String instance;
	private LocalDateTime timestamp;

	private ErrorDetailsBuilder() {}

	/**
	 * Builder of this class
	 * 
	 * @return an {@code ErrorDetailsBuilder}
	 */
	public static ErrorDetailsBuilder builder() {
		return new ErrorDetailsBuilder();
	}

	public ErrorDetailsBuilder type(String type) {
		this.type = type;
		return this;
	}

	public ErrorDetailsBuilder title(String title) {
		this.title = title;
		return this;
	}

	public ErrorDetailsBuilder status(int status) {
		this.status = status;
		return this;
	}

	public ErrorDetailsBuilder detail(String detail) {
		this.detail = detail;
		return this;
	}

	public ErrorDetailsBuilder instance(String instance) {
		this.instance = instance;
		return this;
	}

	public ErrorDetailsBuilder timestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
		return this;
	}

	/**
	 * Builder of {@code ErrorDetails}
	 * 
	 * @return instance of {@code ErrorDetails}
	 */
	public ErrorDetails build() {
		return new ErrorDetails(this.status, this.type, this.title, this.detail, this.instance, this.timestamp);
	}
}
