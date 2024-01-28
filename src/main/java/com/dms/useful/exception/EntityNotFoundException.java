package com.dms.useful.exception;

public class EntityNotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = -6384510609956521498L;

	public EntityNotFoundException(String message) {
		super(message);
	}
}
