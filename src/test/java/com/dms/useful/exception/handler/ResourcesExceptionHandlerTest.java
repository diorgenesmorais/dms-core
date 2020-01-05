package com.dms.useful.exception.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import com.google.gson.Gson;

/**
 * Referente aos testes unitários da classe {@code ResourcesExceptionHandler}
 * 
 * @author Diorgenes Morais
 *
 */
public class ResourcesExceptionHandlerTest {

	private ResponseEntityExceptionHandler exceptionHandlerSupport;

	private DefaultHandlerExceptionResolver defaultExceptionResolver;

	private WebRequest request;

	private HttpServletRequest servletRequest;

	private MockHttpServletResponse servletResponse;

	private Gson gson = new Gson();

	@Before
	public void setup() {
		this.servletRequest = new MockHttpServletRequest();
		this.servletResponse = new MockHttpServletResponse();
		this.request = new ServletWebRequest(this.servletRequest, this.servletResponse);

		this.exceptionHandlerSupport = new ApplicationExceptionHandler();

		this.defaultExceptionResolver = new DefaultHandlerExceptionResolver();
	}

	@RestControllerAdvice
	private static class ApplicationExceptionHandler extends ResourcesExceptionHandler {

	}

	private ResponseEntity<Object> testException(Exception ex) throws Exception {
		try {
			ResponseEntity<Object> responseEntity = this.exceptionHandlerSupport.handleException(ex, this.request);

			// SPR-9653
			if (HttpStatus.INTERNAL_SERVER_ERROR.equals(responseEntity.getStatusCode())) {
				assertSame(ex, this.servletRequest.getAttribute("javax.servlet.error.exception"));
			}

			this.defaultExceptionResolver.resolveException(this.servletRequest, this.servletResponse, null, ex);

			assertEquals(this.servletResponse.getStatus(), responseEntity.getStatusCodeValue());

			System.out.println("Body: " + gson.toJson(responseEntity.getBody()));

			return responseEntity;
		} catch (Exception ex2) {
			throw new IllegalStateException("handleException threw exception", ex2);
		}
	}

	@Test
	public void whenHttpMediaTypeNotAcceptableException() throws Exception {
		Exception ex = new HttpMediaTypeNotAcceptableException("");
		ResponseEntity<Object> responseEntity = testException(ex);
		assertEquals(HttpStatus.NOT_ACCEPTABLE, responseEntity.getStatusCode());
	}

	@Test
	public void whenHttpMediaTypeNotSupported() throws Exception {
		List<MediaType> acceptable = Arrays.asList(MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_XML);
		Exception ex = new HttpMediaTypeNotSupportedException(MediaType.APPLICATION_JSON, acceptable);

		ResponseEntity<Object> responseEntity = testException(ex);
		assertEquals(acceptable, responseEntity.getHeaders().getAccept());
		assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, responseEntity.getStatusCode());
	}

	@Test
	public void whenHttpRequestMethodNotSupported() throws Exception {
		List<String> supported = Arrays.asList("POST", "DELETE");
		Exception ex = new HttpRequestMethodNotSupportedException("GET", supported);

		ResponseEntity<Object> responseEntity = testException(ex);
		assertEquals(EnumSet.of(HttpMethod.POST, HttpMethod.DELETE), responseEntity.getHeaders().getAllow());
		assertEquals(HttpStatus.METHOD_NOT_ALLOWED, responseEntity.getStatusCode());
	}
}
