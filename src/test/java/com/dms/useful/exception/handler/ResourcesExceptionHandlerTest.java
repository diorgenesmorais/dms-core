package com.dms.useful.exception.handler;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

/**
 * Referente aos testes unitários da classe {@code ResourcesExceptionHandler}
 * 
 * @author Diorgenes Morais
 *
 */
public class ResourcesExceptionHandlerTest {

	private ResourcesExceptionHandler exceptionHandlerSupport;

	private DefaultHandlerExceptionResolver defaultExceptionResolver;

	private WebRequest request;

	private HttpServletRequest servletRequest;

	private MockHttpServletResponse servletResponse;
	
	private ObjectMapper objectMapper = new ObjectMapper()
				.registerModule(new ParameterNamesModule())
				.registerModule(new Jdk8Module())
				.registerModule(new JavaTimeModule());

	@Before
	public void setup() {
		this.servletRequest = new MockHttpServletRequest("GET", "/products");
		this.servletResponse = new MockHttpServletResponse();
		this.request = new ServletWebRequest(this.servletRequest, this.servletResponse);

		this.exceptionHandlerSupport = new ApplicationExceptionHandler();

		this.defaultExceptionResolver = new DefaultHandlerExceptionResolver();
	}

	private ResponseEntity<Object> testException(Exception ex) throws Exception {
		ResponseEntity<Object> responseEntity = this.exceptionHandlerSupport.handlerResourcesException(ex,
				this.request);

		this.defaultExceptionResolver.resolveException(this.servletRequest, this.servletResponse, null, ex);

		assertEquals(this.servletResponse.getStatus(), responseEntity.getStatusCodeValue());

		String json = objectMapper.writeValueAsString(responseEntity.getBody());
		System.out.println("Body: " + json);

		return responseEntity;
	}

	@Test
	public void whenHttpMediaTypeNotAcceptableException() throws Exception {
		List<MediaType> acceptable = Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML);
		Exception ex = new HttpMediaTypeNotAcceptableException(acceptable);
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

	@Test
	public void whenConstraintException() throws Exception {
		// expected response, because the exception is not in DefaultHandlerExceptionResolver
		this.servletResponse.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);

		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

		Set<ConstraintViolation<Model>> violations = validator.validate(new Model());

		Exception ex = new ConstraintViolationException(violations);

		ResponseEntity<Object> responseEntity = testException(ex);
		assertEquals(HttpStatus.NOT_ACCEPTABLE, responseEntity.getStatusCode());
	}
	
	@Test
	public void shouldGetTheMessageConstraintViolationException() throws Exception {
		// expected response, because the exception is not in DefaultHandlerExceptionResolver
		this.servletResponse.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);

		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

		Model model = new Model();
		model.setNome("Romeu");
		Set<ConstraintViolation<Model>> violations = validator.validate(model);

		Exception ex = new ConstraintViolationException(violations);

		ResponseEntity<Object> responseEntity = testException(ex);
		assertEquals(HttpStatus.NOT_ACCEPTABLE, responseEntity.getStatusCode());
	}
	
	@Test
	public void whenDataIntegrityViolationException() throws Exception {
		// expected response, because the exception is not in DefaultHandlerExceptionResolver
		this.servletResponse.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);

		Exception ex = new DataIntegrityViolationException("Não pode excluir");

		ResponseEntity<Object> responseEntity = testException(ex);
		assertEquals(HttpStatus.NOT_ACCEPTABLE, responseEntity.getStatusCode());
	}

	@Test
	public void whenDuplicateEntry() throws Exception {
		// expected response, because the exception is not in DefaultHandlerExceptionResolver
		this.servletResponse.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);

		Exception ex = new DataIntegrityViolationException("could not execute statement; "
				+ "SQL [n/a]; constraint [nome]; nested exception is "
				+ "org.hibernate.exception.ConstraintViolationException: could not execute statement", 
				new SQLIntegrityConstraintViolationException("Duplicate entry 'MASTER' for key 'nome'"));

		ResponseEntity<Object> responseEntity = testException(ex);
		assertEquals(HttpStatus.NOT_ACCEPTABLE, responseEntity.getStatusCode());
	}

	@Test
	public void whenEmptyResultDataAccessException() throws Exception {
		// expected response, because the exception is not in DefaultHandlerExceptionResolver
		this.servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
		
		Exception ex = new EmptyResultDataAccessException(1);

		ResponseEntity<Object> responseEntity = testException(ex);
		assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	public void whenHttpMessageNotReadableException() throws Exception {
		@SuppressWarnings("deprecation")
		Exception ex = new HttpMessageNotReadableException("message");

		ResponseEntity<Object> responseEntity = testException(ex);
		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
	}

	@Test
	public void whenMethodArgumentNotValidException() throws Exception {
		Model model = new Model();
		BindingResult bindingResult = new BeanPropertyBindingResult(model, "model");
		bindingResult.addError(new FieldError("model", "nome", "Não pode ser nulo"));
		bindingResult.addError(new FieldError("model", "sobrenome", "não pode ser nulo"));

		Method method = Model.class.getDeclaredMethod("setNome", String.class);
		MethodParameter parameter = new MethodParameter(method, 0);

		Exception ex = new MethodArgumentNotValidException(parameter, bindingResult);

		ResponseEntity<Object> responseEntity = testException(ex);
		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
	}

	@Test
	public void whenNoSuchElementException() throws Exception {
		// expected response, because the exception is not in DefaultHandlerExceptionResolver
		this.servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);

		Exception ex = new NoSuchElementException("a busca retornou vázio");

		ResponseEntity<Object> responseEntity = testException(ex);
		assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
	}
	
	@Test
	public void whenNoHandlerFoundException() throws Exception {
		// expected response, because the exception is not in DefaultHandlerExceptionResolver
		this.servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
		HttpHeaders hearders = new HttpHeaders();

		Exception ex = new NoHandlerFoundException("GET", "/hostcheck", hearders);

		ResponseEntity<Object> responseEntity = testException(ex);
		assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
	}
	
	@Test
	public void whenUnhandledException() throws Exception {
		// expected response, because the exception is not in DefaultHandlerExceptionResolver
		this.servletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

		ResponseEntity<Object> responseEntity = testException(new Exception("Erro interno no servidor"));
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
	}

	@Test
	public void whenHttpMediaTypeNotSupportedNotInformed() throws Exception {
		List<MediaType> acceptable = new ArrayList<>();
		Exception ex = new HttpMediaTypeNotSupportedException(MediaType.APPLICATION_XML, acceptable);

		ResponseEntity<Object> responseEntity = testException(ex);
		assertEquals(acceptable, responseEntity.getHeaders().getAccept());
		assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, responseEntity.getStatusCode());
	}
}
