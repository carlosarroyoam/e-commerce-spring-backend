package com.carlosarroyoam.demojpa;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ControllerAdvisor extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ControllerAdvisor.class);

	@Override
	protected ResponseEntity<Object> createResponseEntity(Object body, HttpHeaders headers, HttpStatusCode statusCode,
			WebRequest request) {
		if (body instanceof ProblemDetail originalBody) {
			Map<String, Object> modifiedBody = createResponseBody(new Exception(originalBody.getDetail()), request,
					HttpStatus.valueOf(statusCode.value()));

			body = modifiedBody;
		}

		return super.createResponseEntity(body, headers, statusCode, request);
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<Object> handleSQLException(WebRequest request, ResponseStatusException ex) {
		Map<String, Object> body = createResponseBody(ex, request, HttpStatus.valueOf(ex.getStatusCode().value()));
		body.put("message", ex.getReason());

		log.error("Error: {}", ex.getReason());

		return new ResponseEntity<>(body, ex.getStatusCode());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> exception(Exception ex, WebRequest request) {
		Map<String, Object> body = createResponseBody(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);

		log.error("Exception: {}", ex.getMessage());

		return ResponseEntity.internalServerError().body(body);
	}

	private Map<String, Object> createResponseBody(Exception ex, WebRequest request, HttpStatus status) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("message", ex.getMessage());
		body.put("error", status.getReasonPhrase());
		body.put("status", status.value());
		body.put("path", request.getDescription(false).replace("uri=", ""));
		body.put("timestamp", ZonedDateTime.now(ZoneId.of("UTC")));

		return body;
	}

}
