package com.dms.useful.exception.handler;

public enum ProblemType {
	EMPTY_RESULT_DATA_ACCESS("/empty-result-data-access", "Empty Result Data Access"),
	DATA_INTEGRITY_VIOLATION("/data-integrity-violation", "Data Integrity Violation"),
	HTTP_MESSAGE_NOT_READABLE("/http-message-not-readable", "Http Message Not Readable"),
	CONSTRAINT_VIOLATION("/constraint-violation", "Constraint Violation"),
	NOT_ACCEPTABLE_MEDIA_TYPE("/not-acceptable-media-type", "Not acceptable Media Type"),
	NO_SUCH_ELEMENT("/no-such-element", "No Such Element"),
	METHOD_ARGUMENT_NOT_VALID("/method-argument-not-valid", "Method Argument Not Valid"),
	HTTP_REQUEST_METHOD_NOT_SUPPORTED("/http-request-method-not-supported", "Http Request Method Not Supported"),
	HTTP_MEDIA_TYPE_NOT_ACCEPTABLE("/http-media-type-not-acceptable", "Http Media Type Not Acceptable"),
	NOT_FOUND("/not-found", "Not Found"),
	INTERNAL_SERVER_ERROR("/internal-server-error", "Internal Server Error");

	private String uri;
	private String title;

	ProblemType(String path, String title) {
		this.uri = "https://api.dms.com.br" + path;
		this.title = title;
	}

	public String getUri() {
		return uri;
	}

	public String getTitle() {
		return title;
	}
}
