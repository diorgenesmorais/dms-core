package com.dms.useful.exception.handler;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.dms.useful.exception.EntityNotFoundException;

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
 * @version 2.0.3
 */
public abstract class ResourcesExceptionHandler extends ResponseEntityExceptionHandler {

	private Map<String, Object> properties;

	public ResourcesExceptionHandler() {
		this.properties = new LinkedHashMap<>();
	}

	private Map<String, Object> criarListaErros(BindingResult bindingResult) {
		this.properties.clear();

		for (FieldError fieldError : bindingResult.getFieldErrors()) {
			this.properties.put(fieldError.getField(), fieldError.getDefaultMessage());
		}
		return this.properties;
	}

	private URI getURI(WebRequest request) {
		HttpServletRequest req = ((ServletWebRequest) request).getRequest();
		return URI.create(req.getRequestURI());
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		if (body == null) {
			body = ProblemDetail.builder("https://api.dms.com.br/errors", status)
					.title(status.getReasonPhrase())
					.instance(getURI(request))
					.timestamp(OffsetDateTime.now());
		} else if (body instanceof String) {
			body = ProblemDetail.builder("https://api.dms.com.br/errors", status)
					.title(status.getReasonPhrase())
					.title((String) body)
					.instance(getURI(request))
					.timestamp(OffsetDateTime.now());
		}

		return new ResponseEntity<>(body, headers, status);
	};

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		ProblemDetail error = createProblemDetail(status, ProblemType.HTTP_MESSAGE_NOT_READABLE,
				ExceptionUtils.getRootCauseMessage(ex), request);

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

		Map<String, Object> errorsList = criarListaErros(ex.getBindingResult());
		ProblemDetail error = createProblemDetail(status, ProblemType.METHOD_ARGUMENT_NOT_VALID, "Por favor, verificar as propriedades do objeto properties", request)
				.properties(errorsList);
		return handleExceptionInternal(ex, error, headers, status, request);
	}

	@ExceptionHandler(EmptyResultDataAccessException.class)
	public ResponseEntity<Object> handleEmptyResultDataAccessException(EmptyResultDataAccessException ex, WebRequest request) {
		
		var status = HttpStatus.NOT_FOUND;

		ProblemDetail error = createProblemDetail(status, ProblemType.EMPTY_RESULT_DATA_ACCESS,
				ExceptionUtils.getRootCauseMessage(ex), request);

		return handleExceptionInternal(ex, error, new HttpHeaders(), status, request);
	}

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<Object> handleNoSuchElementException(NoSuchElementException ex, WebRequest request) {
		
		var status = HttpStatus.NOT_FOUND;

		ProblemDetail error = createProblemDetail(status, ProblemType.NO_SUCH_ELEMENT,
				ex.getMessage(), request);

		return handleExceptionInternal(ex, error, new HttpHeaders(), status, request);
	}

	public final ResponseEntity<Object> handlerResourcesException(Exception ex, WebRequest request) throws Exception {

		try {
			if (ex instanceof ConstraintViolationException) {
				return handleConstraintViolationException((ConstraintViolationException) ex, request);
			} else if (ex instanceof DataIntegrityViolationException) {
				return handleDataIntegrityViolationException((DataIntegrityViolationException) ex, request);
			} else if (ex instanceof EmptyResultDataAccessException) {
				return handleEmptyResultDataAccessException((EmptyResultDataAccessException) ex, request);
			} else if (ex instanceof NoSuchElementException) {
				return handleNoSuchElementException((NoSuchElementException) ex, request);
			}
			return super.handleException(ex, request);			
		} catch (Exception e) {
			return handleUncaught((Exception) ex, request);
		}
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
		
		var status = HttpStatus.NOT_ACCEPTABLE;

		ProblemDetail error = createProblemDetail(status, ProblemType.DATA_INTEGRITY_VIOLATION,
				ExceptionUtils.getRootCauseMessage(ex), request);

		return handleExceptionInternal(ex, error, new HttpHeaders(), status, request);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
		
		var status = HttpStatus.NOT_ACCEPTABLE;

		ProblemDetail error = createProblemDetail(status, ProblemType.CONSTRAINT_VIOLATION,
				ExceptionUtils.getRootCauseMessage(ex), request);

		return handleExceptionInternal(ex, error, new HttpHeaders(), status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		String userMessage = String.format("%s. Supports: %s",
				ex.getMessage().isEmpty() ? "Not acceptable Media Type" : ex.getMessage(),
				ex.getSupportedMediaTypes().stream().map(mt -> mt.toString()).collect(Collectors.joining(", ")));

		ProblemDetail error = createProblemDetail(status, ProblemType.HTTP_MEDIA_TYPE_NOT_ACCEPTABLE, userMessage, request);

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

		ProblemDetail error = createProblemDetail(status, ProblemType.HTTP_REQUEST_METHOD_NOT_SUPPORTED,
				userMessage, request);

		return handleExceptionInternal(ex, error, headers, status, request);
	}

	/**
	 * Crie um {@code ProblemDetail} básico (com as propriedades comuns) O
	 * proposito é permitir após retornar o ErrorDetailsBuilder adicionar se
	 * necessário mais alguma propriedade antes de chamar o build.
	 * 
	 * @param status      um objeto {@code HttpStatus}
	 * @param problemType Enum que contém a URI e title
	 * @param detail      detalhamento do problema (error)
	 * @return {@code ProblemDetail}
	 */
	public ProblemDetail createProblemDetail(HttpStatus status, ProblemType problemType, String detail, WebRequest request) {
		return ProblemDetail.builder(problemType.getUri(), status)
				.title(status.getReasonPhrase())
				.title(problemType.getTitle())
				.detail(detail)
				.instance(getURI(request))
				.timestamp(OffsetDateTime.now());
	}

	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {

		String message = String.format("Resource %s not found", ex.getRequestURL());
		ProblemDetail error = createProblemDetail(status, ProblemType.NOT_FOUND, message, request);

		return handleExceptionInternal(ex, error, headers, status, request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleUncaught(Exception ex, WebRequest request) {
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

		String message = "Ocorreu um erro interno inesperado no sistema";
		ProblemDetail error = createProblemDetail(status, ProblemType.INTERNAL_SERVER_ERROR, message, request);

		return handleExceptionInternal(ex, error, new HttpHeaders(), status, request);
	}

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {

		var status = HttpStatus.BAD_REQUEST;
		ProblemDetail error = createProblemDetail(status, ProblemType.NO_SUCH_ELEMENT,
				ex.getMessage(), request);

		return handleExceptionInternal(ex, error, new HttpHeaders(), status, request);
	}
	
	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		List<MediaType> mediaTypes = ex.getSupportedMediaTypes();
		if (!CollectionUtils.isEmpty(mediaTypes)) {
			headers.setAccept(mediaTypes);
			if (request instanceof ServletWebRequest) {
				ServletWebRequest servletWebRequest = (ServletWebRequest) request;
				if (HttpMethod.PATCH.equals(servletWebRequest.getHttpMethod())) {
					headers.setAcceptPatch(mediaTypes);
				}
			}
		}

		ProblemDetail error = createProblemDetail(status, ProblemType.NOT_ACCEPTABLE_MEDIA_TYPE,
				ex.getMessage(), request);
	
		return handleExceptionInternal(ex, error, headers, status, request);
	}
}
