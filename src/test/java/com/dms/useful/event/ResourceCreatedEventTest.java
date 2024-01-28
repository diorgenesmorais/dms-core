package com.dms.useful.event;

import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockHttpServletResponse;

public class ResourceCreatedEventTest {

	private HttpServletResponse servletResponse;
	private ResourceCreatedEvent<Integer> resource;
	private ExpectedException exception;

	@Before
	public void setup() {
		this.servletResponse = new MockHttpServletResponse();
	}

	@Test
	public void shouldGetTheResponse() throws Exception {
		this.servletResponse.addHeader("location", "localhost");
		this.resource = new ResourceCreatedEventImpl(this, this.servletResponse, 1);

		assertTrue(resource.getResponse().containsHeader("location"));
	}

	@SuppressWarnings("null")
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowsAnException() throws Exception {
		this.resource = new ResourceCreatedEventImpl(this, this.servletResponse, null);
	}

	@SuppressWarnings("null")
	@Test(expected = Exception.class)
	public void shouldGetMessageOfException() throws Exception {
		exception.reportMissingExceptionWithMessage("Id should not be null, error in com.dms.useful.event.ResourceCreatedEvent");
		this.resource = new ResourceCreatedEventImpl(this, this.servletResponse, null);
	}
}
