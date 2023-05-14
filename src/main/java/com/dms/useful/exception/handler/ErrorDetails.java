package com.dms.useful.exception.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * {@code ErrorDetails} class
 * 
 * @author Diorgenes Morais
 * @version 2.0.0
 * @since 2.0.0
 */
@JsonInclude(Include.NON_NULL)
public class ErrorDetails {

	private int status;
	private String type;
	private String title;
	private String detail;
	private String instance;

	public ErrorDetails(int status, String type, String title, String detail, String instance) {
		this.status = status;
		this.type = type;
		this.title = title;
		this.detail = detail;
		this.instance = instance;
	}

	public int getStatus() {
		return this.status;
	}
	
	public String getType() {
		return this.type;
	}

	public String getTitle() {
		return this.title;
	}

	public String getDetail() {
		return this.detail;
	}

	public String getInstance() {
		return this.instance;
	}
}
