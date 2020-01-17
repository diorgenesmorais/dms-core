package com.dms.useful.event;

import static org.junit.Assert.*;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockHttpServletResponse;

public class ResourceCreatedEventTest {

	private HttpServletResponse servletResponse;
	private ResourceCreatedEvent<Integer> resource;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setup() {
		this.servletResponse = new MockHttpServletResponse();
	}

	@Test
	public void shouldGetTheResponse() throws Exception {
		this.servletResponse.addHeader("location", "localhost");
		this.resource = new ResourceCreatedEvent<Integer>(this, this.servletResponse, 1);

		assertTrue(resource.getResponse().containsHeader("location"));
	}

	@Test
	public void shouldThrowsAnException() throws Exception {
		exception.expect(IllegalArgumentException.class);
		this.resource = new ResourceCreatedEvent<Integer>(this, this.servletResponse, null);
	}

	@Test
	public void shouldGetMessageOfException() throws Exception {
		exception.expectMessage("Id should not be null, error in com.dms.useful.event.ResourceCreatedEvent");
		this.resource = new ResourceCreatedEvent<Integer>(this, this.servletResponse, null);
	}
}
