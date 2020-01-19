package com.dms.useful.event;

import javax.servlet.http.HttpServletResponse;

public class ResourceCreatedEventImpl extends ResourceCreatedEvent<Integer> {

	private static final long serialVersionUID = 3657465436059277960L;

	public ResourceCreatedEventImpl(Object source, HttpServletResponse response, Integer id) {
		super(source, response, id);
	}

}
