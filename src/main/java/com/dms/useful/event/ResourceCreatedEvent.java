package com.dms.useful.event;

import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationEvent;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

public class ResourceCreatedEvent<ID> extends ApplicationEvent {

	private static final long serialVersionUID = 7067219761465481821L;

	private HttpServletResponse response;
	private ID id;

	/**
	 * Constructor override
	 * 
	 * @param source
	 *            where you generated the event
	 * @param response
	 * @param id
	 *            of model (resource)
	 */
	public ResourceCreatedEvent(Object source, HttpServletResponse response, @NonNull ID id) {
		super(source);
		Assert.notNull(id, String.format("Id should not be null, error in %s", this.getClass().getName()));
		this.response = response;
		this.id = id;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public ID getId() {
		return id;
	}

}
