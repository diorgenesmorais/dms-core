package com.dms.useful.exception.handler;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * {@code ProblemDetail} class
 * 
 * @author Diorgenes Morais
 * @since 2.0.3
 */
@JsonInclude(Include.NON_NULL)
public class ProblemDetail {

	private String type;
	private int status;
	@Nullable
	private String title;
	@Nullable
	private String detail;
	@Nullable
	private URI instance;
	@Nullable
	private OffsetDateTime timestamp;
	@Nullable
	private Map<String, Object> properties;

	protected ProblemDetail() {}

	protected ProblemDetail(String type, int status) {
		super();
		this.type = type;
		this.status = status;
	}

	public String getType() {
		return this.type;
	}

	public ProblemDetail type(@NonNull String type) {
		this.type = type;
		return this;
	}

	public int getStatus() {
		return this.status;
	}

	public ProblemDetail status(int status) {
		this.status = status;
		return this;
	}

	@Nullable
	public String getTitle() {
		if (this.title == null) {
			HttpStatus httpStatus = HttpStatus.resolve(this.status);
			if (httpStatus != null) {
				return httpStatus.getReasonPhrase();
			}
		}
		return this.title;
	}

	public ProblemDetail title(String title) {
		this.title = title;
		return this;
	}

	@Nullable
	public String getDetail() {
		return this.detail;
	}

	public ProblemDetail detail(@Nullable String detail) {
		this.detail = detail;
		return this;
	}

	@Nullable
	public URI getInstance() {
		return this.instance;
	}

	public ProblemDetail instance(@Nullable URI instance) {
		this.instance = instance;
		return this;
	}

	@Nullable
	public OffsetDateTime getTimestamp() {
		return this.timestamp;
	}

	public ProblemDetail timestamp(@Nullable OffsetDateTime timestamp) {
		this.timestamp = timestamp;
		return this;
	}

	@Nullable
	public Map<String, Object> getProperties() {
		return this.properties;
	}

	public ProblemDetail properties(@Nullable Map<String, Object> properties) {
		this.properties = properties;
		return this;
	}

	public static ProblemDetail builder(String type, HttpStatus status) {
		Assert.notNull(type, "Type is required");
		Assert.notNull(status, "HttpStatusCode is required");
		return new ProblemDetail(type, status.value());
	}

	@Override
	public int hashCode() {
		return Objects.hash(detail, instance, properties, status, timestamp, title, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProblemDetail other = (ProblemDetail) obj;
		return Objects.equals(detail, other.detail) && Objects.equals(instance, other.instance)
				&& Objects.equals(properties, other.properties) && status == other.status
				&& Objects.equals(timestamp, other.timestamp) && Objects.equals(title, other.title)
				&& Objects.equals(type, other.type);
	}

	@Override
	public String toString() {
		return "ProblemDetail [type=" + type + ", status=" + status + ", title=" + title + ", detail=" + detail
				+ ", instance=" + instance + ", timestamp=" + timestamp + ", properties=" + properties + "]";
	}

}
