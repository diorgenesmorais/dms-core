package com.dms.useful.exception.handler;

/**
 * 
 * @author Diorgenes Morais
 * @since 1.1.6
 */
public class ErroDTO {

	private String messageUser;
	private String messageDeveloper;

	public ErroDTO(String messageUser, String messageDeveloper) {
		this.messageUser = messageUser;
		this.messageDeveloper = messageDeveloper;
	}

	public String getMessageUser() {
		return messageUser;
	}

	public String getMessageDeveloper() {
		return messageDeveloper;
	}

}
