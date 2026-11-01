package com.carlosarroyoam.ecommerce.core.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.carlosarroyoam.ecommerce.core.constant.MessageKeys;
import com.carlosarroyoam.ecommerce.support.testutils.TestMessageSources;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.MethodParameter;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.TypeInformation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

/** Pruebas unitarias de la resolución de mensajes de {@link GlobalExceptionHandler}. */
class GlobalExceptionHandlerTest {
  private MessageSource messageSource;
  private GlobalExceptionHandler handler;
  private MockHttpServletRequest request;

  @BeforeEach
  void setUp() {
    messageSource = TestMessageSources.messages();
    handler =
        new GlobalExceptionHandler(
            new ProblemDetailFactory(), new ExceptionLogger(), messageSource);
    request = new MockHttpServletRequest("GET", "/users/999");
  }

  @Test
  @DisplayName("Given a message key, when handle application exception, then detail is the text")
  void givenMessageKey_whenHandleApplicationException_thenDetailIsResolvedText() {
    ResourceNotFoundException ex =
        new ResourceNotFoundException(MessageKeys.USER_NOT_FOUND_EXCEPTION);

    ProblemDetail problem = handler.handleApplicationException(ex, request);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(problem.getDetail()).isEqualTo("User not found");
  }

  @Test
  @DisplayName("Given a literal message, when handle application exception, then detail is kept")
  void givenLiteralMessage_whenHandleApplicationException_thenDetailIsTheLiteral() {
    BusinessException ex = new BusinessException("Some literal message");

    ProblemDetail problem = handler.handleApplicationException(ex, request);

    assertThat(problem.getDetail()).isEqualTo("Some literal message");
  }

  @Test
  @DisplayName("Given a message with args, when handle application exception, then args are set")
  void givenMessageWithArgs_whenHandleApplicationException_thenDetailHasArgsInterpolated() {
    ResourceNotFoundException ex = new ResourceNotFoundException("Item {0} not found", 42);

    ProblemDetail problem = handler.handleApplicationException(ex, request);

    assertThat(problem.getDetail()).isEqualTo("Item 42 not found");
  }

  @Test
  @DisplayName("Given authentication exception, when handle, then returns unauthorized problem")
  void givenAuthenticationException_whenHandleAuthenticationException_thenReturnsUnauthorized() {
    ProblemDetail problem =
        handler.handleAuthenticationException(
            new BadCredentialsException("Bad credentials"), request);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(problem.getDetail()).isEqualTo("Bad credentials");
    assertThat(problem.getInstance()).hasToString("/users/999");
  }

  @Test
  @DisplayName("Given access denied exception, when handle, then returns forbidden problem")
  void givenAccessDeniedException_whenHandleAccessDeniedException_thenReturnsForbidden() {
    ProblemDetail problem =
        handler.handleAccessDeniedException(new AccessDeniedException("Access Denied"), request);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    assertThat(problem.getInstance()).hasToString("/users/999");
  }

  @Test
  @DisplayName(
      "Given unexpected exception, when handle, then returns generic internal server error")
  void givenUnexpectedException_whenHandleException_thenReturnsGenericInternalServerError() {
    ProblemDetail problem = handler.handleException(new IllegalStateException("secret"), request);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(problem.getDetail()).isEqualTo("Whoops! Something went wrong");
  }

  @Test
  @DisplayName(
      "Given validation errors, when handle, then returns unprocessable entity with errors")
  void givenValidationErrors_whenHandleMethodArgumentNotValid_thenReturnsErrorsByField()
      throws NoSuchMethodException {
    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(new Object(), "request");
    bindingResult.addError(new FieldError("request", "firstName", "must not be blank"));
    MethodArgumentNotValidException ex =
        new MethodArgumentNotValidException(
            new MethodParameter(Object.class.getDeclaredMethod("toString"), -1), bindingResult);

    ProblemDetail problem = handler.handleMethodArgumentNotValid(ex, request);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
    assertThat(problem.getDetail()).isEqualTo("Invalid request data");
    assertThat(problem.getProperties())
        .containsEntry("errors", Map.of("firstName", "must not be blank"));
  }

  @Test
  @DisplayName("Given response status exception, when handle, then returns its status and message")
  void givenResponseStatusException_whenHandleResponseStatus_thenReturnsItsStatusAndMessage() {
    ResponseStatusException ex =
        new ResponseStatusException(
            HttpStatus.NOT_IMPLEMENTED, MessageKeys.FEATURE_NOT_IMPLEMENTED_EXCEPTION);

    ProblemDetail problem = handler.handleResponseStatus(ex, request);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_IMPLEMENTED.value());
    assertThat(problem.getDetail()).isEqualTo("This feature is not yet implemented");
    assertThat(problem.getInstance()).hasToString("/users/999");
  }

  @Test
  @DisplayName("Given unknown sort property, when handle, then returns bad request")
  void givenUnknownSortProperty_whenHandleInvalidSortProperty_thenReturnsBadRequest() {
    PropertyReferenceException ex =
        new PropertyReferenceException("nonexistent", TypeInformation.of(String.class), List.of());

    ProblemDetail problem = handler.handleInvalidSortProperty(ex, request);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Sort property is not valid");
  }

  @Test
  @DisplayName("Given wrapped unknown sort property, when handle, then returns bad request")
  void givenWrappedUnknownSortProperty_whenHandleInvalidSortProperty_thenReturnsBadRequest() {
    InvalidDataAccessApiUsageException ex =
        new InvalidDataAccessApiUsageException(
            "wrapped",
            new PropertyReferenceException(
                "nonexistent", TypeInformation.of(String.class), List.of()));

    ProblemDetail problem = handler.handleInvalidSortProperty(ex, request);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("Sort property is not valid");
  }

  @Test
  @DisplayName("Given unrelated data access usage error, when handle, then returns internal error")
  void givenUnrelatedDataAccessError_whenHandleInvalidSortProperty_thenReturnsInternalError() {
    InvalidDataAccessApiUsageException ex = new InvalidDataAccessApiUsageException("bad query");

    ProblemDetail problem = handler.handleInvalidSortProperty(ex, request);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(problem.getDetail()).isEqualTo("Whoops! Something went wrong");
  }

  @Test
  @DisplayName("Given every MessageKeys constant, when resolved, then each has a bundle entry")
  void givenEveryMessageKeysConstant_whenResolved_thenEachHasBundleEntry() {
    List<String> missingKeys =
        Arrays.stream(MessageKeys.class.getDeclaredFields())
            .filter(GlobalExceptionHandlerTest::isMessageKeyConstant)
            .map(GlobalExceptionHandlerTest::readConstant)
            .filter(key ->
                messageSource.getMessage(key, null, null, LocaleContextHolder.getLocale()) == null)
            .toList();

    assertThat(missingKeys).isEmpty();
  }

  /**
   * Indica si el campo es una constante de clave de mensaje, es decir, una {@code public static
   * final String}.
   *
   * @param field el campo a evaluar
   * @return {@code true} si el campo es una clave de mensaje
   */
  private static boolean isMessageKeyConstant(Field field) {
    int modifiers = field.getModifiers();
    return field.getType() == String.class
        && Modifier.isPublic(modifiers)
        && Modifier.isStatic(modifiers)
        && Modifier.isFinal(modifiers);
  }

  /**
   * Lee el valor de una constante estática de {@link MessageKeys}.
   *
   * @param field el campo a leer
   * @return el valor de la constante
   */
  private static String readConstant(Field field) {
    try {
      return (String) field.get(null);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }
}
