package com.dms.useful.event.listener;

import java.net.URI;

import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationListener;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.dms.useful.event.ResourceCreatedEvent;

/**
 * This class adds a Location attribute to Headers in the request response,
 * telling how to access the newly created resource.
 * 
 * This class must be part of the application context as a component
 * 
 * @author Diorgenes Morais
 * @since 1.1.2
 * 
 */
public abstract class ResourceCreatedListener<ID> implements ApplicationListener<ResourceCreatedEvent<ID>> {

	@Override
	public void onApplicationEvent(ResourceCreatedEvent<ID> event) {
		HttpServletResponse response = event.getResponse();
		ID id = event.getId();

		addHeaderLocation(response, id);
	}

	private void addHeaderLocation(HttpServletResponse response, ID id) {
		URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}").build(id);
		response.setHeader("Location", uri.toASCIIString());
	}
}
