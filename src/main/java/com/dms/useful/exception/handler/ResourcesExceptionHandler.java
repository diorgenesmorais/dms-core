package com.dms.useful.exception.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
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
 *
 * @since 1.1.6
 * @version 2.0.0
 */
public abstract class ResourcesExceptionHandler extends ResponseEntityExceptionHandler {

	private List<String> erros;

	public ResourcesExceptionHandler() {
		this.erros = new ArrayList<>();
	}

	private List<String> criarListaErros(BindingResult bindingResult) {
		this.erros.clear();

		for (FieldError fieldError : bindingResult.getFieldErrors()) {
			this.erros.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
		}
		return this.erros;
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		if (body == null) {
			body = ErrorDetailsBuilder.builder().status(status.value()).title(status.getReasonPhrase()).build();
		} else if (body instanceof String) {
			body = ErrorDetailsBuilder.builder().status(status.value()).title((String) body).build();
		}

		return super.handleExceptionInternal(ex, body, headers, status, request);
	};

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		ErrorDetails error = createErrorDetailBuilder(status, ProblemType.HTTP_MESSAGE_NOT_READABLE,
				ExceptionUtils.getRootCauseMessage(ex)).build();

		return handleExceptionInternal(ex, error, headers, status, request);
	}

	/**
	 * Exception to be thrown when validation on an argument annotated with
	 * {@code @Valid} fails.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		List<String> errorsList = criarListaErros(ex.getBindingResult());
		ErrorDetails error = createErrorDetailBuilder(status, ProblemType.METHOD_ARGUMENT_NOT_VALID,
				errorsList.toString()).build();
		return handleExceptionInternal(ex, error, headers, status, request);
	}

	@ExceptionHandler(EmptyResultDataAccessException.class)
	public ResponseEntity<Object> handleEmptyResultDataAccessException(EmptyResultDataAccessException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		ErrorDetails error = createErrorDetailBuilder(status, ProblemType.EMPTY_RESULT_DATA_ACCESS,
				ExceptionUtils.getRootCauseMessage(ex)).build();

		return handleExceptionInternal(ex, error, headers, status, request);
	}

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<Object> handleNoSuchElementException(NoSuchElementException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {

		ErrorDetails error = createErrorDetailBuilder(status, ProblemType.NO_SUCH_ELEMENT,
				ExceptionUtils.getRootCauseMessage(ex)).build();

		return handleExceptionInternal(ex, error, headers, status, request);
	}

	public final ResponseEntity<Object> handlerResourcesException(Exception ex, WebRequest request) throws Exception {
		HttpHeaders headers = new HttpHeaders();

		if (ex instanceof ConstraintViolationException) {
			return handleConstraintViolationException((ConstraintViolationException) ex, headers,
					HttpStatus.NOT_ACCEPTABLE, request);
		} else if (ex instanceof DataIntegrityViolationException) {
			return handleDataIntegrityViolationException((DataIntegrityViolationException) ex, headers,
					HttpStatus.NOT_ACCEPTABLE, request);
		} else if (ex instanceof EmptyResultDataAccessException) {
			return handleEmptyResultDataAccessException((EmptyResultDataAccessException) ex, headers,
					HttpStatus.NOT_FOUND, request);
		} else if (ex instanceof NoSuchElementException) {
			return handleNoSuchElementException((NoSuchElementException) ex, headers, HttpStatus.NOT_FOUND, request);
		}
		return super.handleException(ex, request);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		ErrorDetails error = createErrorDetailBuilder(status, ProblemType.DATA_INTEGRITY_VIOLATION,
				ExceptionUtils.getRootCauseMessage(ex)).build();

		return handleExceptionInternal(ex, error, headers, status, request);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		ErrorDetails error = createErrorDetailBuilder(status, ProblemType.CONSTRAINT_VIOLATION,
				ExceptionUtils.getRootCauseMessage(ex)).build();

		return handleExceptionInternal(ex, error, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		String userMessage = String.format("%s. Supports: %s",
				ex.getMessage().isEmpty() ? "Not acceptable Media Type" : ex.getMessage(),
				ex.getSupportedMediaTypes().stream().map(mt -> mt.toString()).collect(Collectors.joining(", ")));

		ErrorDetails error = createErrorDetailBuilder(status, ProblemType.HTTP_MEDIA_TYPE_NOT_ACCEPTABLE, userMessage)
				.build();

		return handleExceptionInternal(ex, error, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		Set<HttpMethod> supportedMethods = ex.getSupportedHttpMethods();
		if (!CollectionUtils.isEmpty(supportedMethods)) {
			headers.setAllow(supportedMethods);
		}

		String userMessage = String.format("%s. Supports: %s", ExceptionUtils.getRootCauseMessage(ex),
				headers.getAllow().stream().map(hm -> hm.name()).collect(Collectors.joining(", ")));

		ErrorDetails error = createErrorDetailBuilder(status, ProblemType.HTTP_REQUEST_METHOD_NOT_SUPPORTED,
				userMessage).build();

		return handleExceptionInternal(ex, error, headers, status, request);
	}

	/**
	 * Crie um {@code ErrorDetailBuilder} básico (com as propriedades comuns) O proposito é
	 * permitir após retornar o ErrorDetailsBuilder adicionar se necessário mais
	 * alguma propriedade antes de chamar o build.
	 * 
	 * @param status      um objeto {@code HttpStatus}
	 * @param problemType Enum que contém a URI e title
	 * @param detail      detalhamento do problema (error)
	 * @return {@code ErrorDetailsBuilder}
	 */
	private ErrorDetailsBuilder createErrorDetailBuilder(HttpStatus status, ProblemType problemType, String detail) {
		return ErrorDetailsBuilder.builder().status(status.value()).type(problemType.getUri())
				.title(problemType.getTitle()).detail(detail);
	}
}
