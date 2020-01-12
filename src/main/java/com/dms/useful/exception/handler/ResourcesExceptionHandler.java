package com.dms.useful.exception.handler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
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

	private MessageSource messageSource;
	private List<ErrorDetails> erros;

	public ResourcesExceptionHandler() {
		this.messageSource = new ReloadableResourceBundleMessageSource();
		this.erros = new ArrayList<>();
	}

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

	private ErrorDetails addErrorDatails(String title, int status,
			String userMessage, String developerMessage) {
		return ErrorDetailsBuilder.newBuilder().title(title)
				.status(status).timestamp(new Date().getTime()).userMessage(userMessage)
				.developerMessage(developerMessage).build();
	}

	private List<ErrorDetails> criarListaErros(BindingResult bindingResult, HttpStatus status) {

		for (FieldError fieldError : bindingResult.getFieldErrors()) {
			String userMessage = messageSource.getMessage(fieldError, LocaleContextHolder.getLocale());
			this.erros.add(addErrorDatails("Not Valid", status.value(), userMessage, fieldError.toString()));
		}
		return this.erros;
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		this.erros.add(addErrorDatails("Http Message Not Readable Exception", status.value(), ex.getMessage(), ExceptionUtils.getRootCauseMessage(ex)));

		return handleExceptionInternal(ex, erros, headers, status, request);
	}

	/**
	 * Exception to be thrown when validation on an argument annotated with {@code @Valid} fails.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		List<ErrorDetails> errorsList = criarListaErros(ex.getBindingResult(), status);
		return handleExceptionInternal(ex, errorsList, headers, status, request);
	}

	public ResponseEntity<Object> handleEmptyResultDataAccessException(EmptyResultDataAccessException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {

		this.erros.add(addErrorDatails("Empty Result Data Access Exception", status.value(), ex.getMessage(), ExceptionUtils.getRootCauseMessage(ex)));

		return handleExceptionInternal(ex, erros, headers, status, request);
	}

	public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {

		this.erros.add(addErrorDatails("Data Integrity Violation Exception", status.value(), ex.getMessage(), ExceptionUtils.getRootCauseMessage(ex)));

		return handleExceptionInternal(ex, this.erros, headers, status, request);
	}

	public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {

		String userMessage = ex.getConstraintViolations().stream()
									.map(cv -> cv == null ? "null" : cv.getPropertyPath() + ": " + cv.getMessage())
									.collect(Collectors.joining( ", " ));

		this.erros.add(addErrorDatails("Constraint Violation Exception", status.value(), userMessage, ExceptionUtils.getRootCauseMessage(ex)));

		return handleExceptionInternal(ex, this.erros, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		String userMessage = String.format("%s. Supports: %s", 
												ex.getMessage().isEmpty() ? "Not acceptable Media Type" : ex.getMessage(),
												ex.getSupportedMediaTypes().stream()
															.map(mt -> mt.toString())
															.collect(Collectors.joining(", ")));

		this.erros.add(addErrorDatails("Not acceptable Media Type", status.value(), userMessage, ExceptionUtils.getRootCauseMessage(ex)));

		return handleExceptionInternal(ex, this.erros, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		List<MediaType> mediaTypes = ex.getSupportedMediaTypes();
		if (!CollectionUtils.isEmpty(mediaTypes)) {
			headers.setAccept(mediaTypes);
		}

		String userMessage = String.format("%s. Supports: ", 
										ex.getMessage(),
										ex.getSupportedMediaTypes().stream()
													.map(mt -> mt.toString())
													.collect(Collectors.joining(", ")));

		this.erros.add(addErrorDatails("Unsupported Media Type", status.value(), userMessage, ExceptionUtils.getRootCauseMessage(ex)));

		return handleExceptionInternal(ex, erros, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		Set<HttpMethod> supportedMethods = ex.getSupportedHttpMethods();
		if (!CollectionUtils.isEmpty(supportedMethods)) {
			headers.setAllow(supportedMethods);
		}

		String userMessage = String.format("%s. Supports: %s", 
													ex.getMessage(), 
													headers.getAllow().stream()
															.map(hm -> hm.name())
															.collect(Collectors.joining(", ")));

		this.erros.add(addErrorDatails("Request Method Not Supported", status.value(), userMessage, ExceptionUtils.getRootCauseMessage(ex)));

		return handleExceptionInternal(ex, this.erros, headers, status, request);
	}
}
