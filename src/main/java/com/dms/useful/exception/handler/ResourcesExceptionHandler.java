package com.dms.useful.exception.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.validation.ConstraintViolationException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
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
		status = HttpStatus.NOT_ACCEPTABLE;
		List<ErrorDetails> erros = criarListaErros(ex.getBindingResult(), status);
		return handleExceptionInternal(ex, erros, headers, status, request);
	}

	@ExceptionHandler({ EmptyResultDataAccessException.class })
	public ResponseEntity<Object> handleEmptyResultDataAccessException(EmptyResultDataAccessException ex,
			WebRequest request) {
		String userMessage = getMessageProperties("resource.not-found");
		List<ErrorDetails> erros = Arrays.asList(ErrorDetailsBuilder.newBuilder()
				.title("Empty Result Data Access Exception").status(HttpStatus.NOT_FOUND.value())
				.timestamp(new Date().getTime()).userMessage(userMessage).developerMessage(ex.toString()).build());

		return handleExceptionInternal(ex, erros, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
	}

	@ExceptionHandler({ DataIntegrityViolationException.class })
	public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex,
			WebRequest request) {
		String userMessage = getMessageProperties("resource.not-acceptable");
		List<ErrorDetails> erros = Arrays
				.asList(ErrorDetailsBuilder.newBuilder().title("Data Integrity Violation Exception")
						.status(HttpStatus.NOT_ACCEPTABLE.value()).timestamp(new Date().getTime())
						.userMessage(userMessage).developerMessage(ExceptionUtils.getRootCauseMessage(ex)).build());

		return handleExceptionInternal(ex, erros, new HttpHeaders(), HttpStatus.NOT_ACCEPTABLE, request);
	}

	@ExceptionHandler({ ConstraintViolationException.class })
	public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request){
		String userMessage = getMessageProperties("resource.not-acceptable");
		List<ErrorDetails> erros = Arrays
				.asList(ErrorDetailsBuilder.newBuilder().title("Must not be null")
						.status(HttpStatus.NOT_ACCEPTABLE.value()).timestamp(new Date().getTime())
						.userMessage(userMessage).developerMessage(ExceptionUtils.getRootCauseMessage(ex)).build());

		return handleExceptionInternal(ex, erros, new HttpHeaders(), HttpStatus.NOT_ACCEPTABLE, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		StringBuilder builder = new StringBuilder();
		builder.append("Media type is not supported. ");
		ex.getSupportedMediaTypes().forEach(t -> builder.append(t + ", "));
		String messageUser = builder.substring(0, builder.length() - 1);
		String messageDeveloper = ex.getCause() != null ? ex.getCause().toString() : ex.toString();
		ErrorDetails erros = ErrorDetailsBuilder.newBuilder()
				.title("Not acceptable Media Type").status(status.value()).timestamp(new Date().getTime())
				.userMessage(messageUser).developerMessage(messageDeveloper).build();

		return handleExceptionInternal(ex, erros, headers, HttpStatus.NOT_ACCEPTABLE, request);
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
		ErrorDetails erros = ErrorDetailsBuilder.newBuilder()
				.title("Unsupported Media Type").status(status.value()).timestamp(new Date().getTime())
				.userMessage(messageUser).developerMessage(messageDeveloper).build();

		return handleExceptionInternal(ex, erros, headers, HttpStatus.UNSUPPORTED_MEDIA_TYPE, request);
	}
}
