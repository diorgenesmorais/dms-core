package com.dms.useful.exception.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Classe abstrata que manipula as excessões da API.
 * 
 * <pre>
 * Para implementar no projeto, basta extender e anotar com
 * &#64;ControllerAdvice
 * 
 * Para implementar mensagens personalizadas para o usuário
 * criar no path src/main/resources o arquivo
 * messages.properties
 * Ex. de menssagem:
 * resource.not-found=Recurso n\u00E3o encontrado
 * </pre>
 * 
 * @author Diorgenes Morais
 * @since 1.0.1
 */
public abstract class ResourcesExceptionHandler extends ResponseEntityExceptionHandler {

	@Autowired
	private MessageSource messageSource;

	@ExceptionHandler({ 
		ConstraintViolationException.class,
		DataIntegrityViolationException.class,
		EmptyResultDataAccessException.class
	})
	public final ResponseEntity<Object> handlerResourcesException(Exception ex, WebRequest request) throws Exception {
		HttpHeaders headers = new HttpHeaders();

		if (ex instanceof ConstraintViolationException) {
			return handleConstraintViolationException((ConstraintViolationException) ex, headers, HttpStatus.NOT_ACCEPTABLE, request);
		} else if (ex instanceof DataIntegrityViolationException) {
			return handleDataIntegrityViolationException((DataIntegrityViolationException) ex, headers, HttpStatus.NOT_ACCEPTABLE, request);
		} else if (ex instanceof EmptyResultDataAccessException) {
			return handleEmptyResultDataAccessException((EmptyResultDataAccessException) ex, headers, HttpStatus.NOT_FOUND, request);
		}
		return super.handleException(ex, request);
	}

	/*
	 * Avoid NoSuchMessageException
	 */
	private String getMessageProperties(String code) {
		String userMessage;
		try {
			userMessage = messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
		} catch (NoSuchMessageException e) {
			userMessage = "Mensagem interna (NoSuchMessageException)";
		}
		return userMessage;
	}

	private List<ErrorDetails> criarListaErros(BindingResult bindingResult, HttpStatus status) {
		List<ErrorDetails> erros = new ArrayList<>();

		for (FieldError fieldError : bindingResult.getFieldErrors()) {
			String messageUser = messageSource.getMessage(fieldError, LocaleContextHolder.getLocale());
			erros.add(ErrorDetailsBuilder.newBuilder().title("Not Valid").status(status.value())
					.timestamp(new Date().getTime()).userMessage(messageUser).developerMessage(fieldError.toString())
					.build());
		}
		return erros;
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		String messageUser = getMessageProperties("mensagem.invalida");
		String messageDeveloper = ex.getCause() != null ? ex.getCause().toString() : ex.toString();
		List<ErrorDetails> erros = Arrays.asList(ErrorDetailsBuilder.newBuilder()
				.title("Http Message Not Readable Exception").status(status.value()).timestamp(new Date().getTime())
				.userMessage(messageUser).developerMessage(messageDeveloper).build());

		return handleExceptionInternal(ex, erros, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		List<ErrorDetails> erros = criarListaErros(ex.getBindingResult(), status);
		return handleExceptionInternal(ex, erros, headers, status, request);
	}

	public ResponseEntity<Object> handleEmptyResultDataAccessException(EmptyResultDataAccessException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {

		String userMessage = ex.getMessage();
		List<ErrorDetails> erros = Arrays.asList(ErrorDetailsBuilder.newBuilder()
				.title("Empty Result Data Access Exception").status(status.value())
				.timestamp(new Date().getTime()).userMessage(userMessage).developerMessage(ex.toString()).build());

		return handleExceptionInternal(ex, erros, headers, status, request);
	}

	public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {

		String userMessage = ex.getMessage();
		List<ErrorDetails> erros = Arrays
				.asList(ErrorDetailsBuilder.newBuilder().title("Data Integrity Violation Exception")
						.status(status.value()).timestamp(new Date().getTime())
						.userMessage(userMessage).developerMessage(ExceptionUtils.getRootCauseMessage(ex)).build());

		return handleExceptionInternal(ex, erros, headers, status, request);
	}

	public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {

		String userMessage = ex.getConstraintViolations().stream()
									.map(cv -> cv == null ? "null" : cv.getPropertyPath() + ": " + cv.getMessage())
									.collect(Collectors.joining( ", " ));
		
		List<ErrorDetails> erros = Arrays.asList(ErrorDetailsBuilder.newBuilder().title("Constraint Violation Exception")
				.status(status.value()).timestamp(new Date().getTime()).userMessage(userMessage)
				.developerMessage(ExceptionUtils.getRootCauseMessage(ex)).build());

		return handleExceptionInternal(ex, erros, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		StringBuilder builder = new StringBuilder();
		builder.append("Media type is not supported. ");
		ex.getSupportedMediaTypes().forEach(t -> builder.append(t + ", "));
		String messageUser = builder.substring(0, builder.length() - 1);
		String messageDeveloper = ex.getCause() != null ? ex.getCause().toString() : ex.toString();
		List<ErrorDetails> erros = Arrays.asList(ErrorDetailsBuilder.newBuilder().title("Not acceptable Media Type")
				.status(status.value()).timestamp(new Date().getTime()).userMessage(messageUser)
				.developerMessage(messageDeveloper).build());

		return handleExceptionInternal(ex, erros, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		List<MediaType> mediaTypes = ex.getSupportedMediaTypes();
		if (!CollectionUtils.isEmpty(mediaTypes)) {
			headers.setAccept(mediaTypes);
		}

		StringBuilder builder = new StringBuilder();
		builder.append(ex.getContentType());
		builder.append(" media type is not supported. Tipos de mídia suportados são: ");
		ex.getSupportedMediaTypes().forEach(t -> builder.append(t + ", "));
		String messageUser = builder.substring(0, builder.length() - 1);
		String messageDeveloper = ex.getCause() != null ? ex.getCause().toString() : ex.toString();
		List<ErrorDetails> erros = Arrays.asList(ErrorDetailsBuilder.newBuilder().title("Unsupported Media Type")
				.status(status.value()).timestamp(new Date().getTime()).userMessage(messageUser)
				.developerMessage(messageDeveloper).build());

		return handleExceptionInternal(ex, erros, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		Set<HttpMethod> supportedMethods = ex.getSupportedHttpMethods();
		if (!CollectionUtils.isEmpty(supportedMethods)) {
			headers.setAllow(supportedMethods);
		}

		StringBuilder builder = new StringBuilder();
		builder.append("Método ");
		builder.append(ex.getMethod());
		builder.append(" não aceito. Método(s) aceito(s): ");
		ex.getSupportedHttpMethods().forEach(m -> builder.append(m + ", "));

		String userMessage = builder.toString();
		List<ErrorDetails> erros = Arrays.asList(ErrorDetailsBuilder.newBuilder().title("Request Method Not Supported")
				.status(status.value()).timestamp(new Date().getTime()).userMessage(userMessage)
				.developerMessage(ExceptionUtils.getRootCauseMessage(ex)).build());

		return handleExceptionInternal(ex, erros, headers, status, request);
	}
}
