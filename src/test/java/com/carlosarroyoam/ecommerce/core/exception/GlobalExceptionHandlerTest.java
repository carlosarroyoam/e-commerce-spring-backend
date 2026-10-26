package com.carlosarroyoam.ecommerce.core.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.support.testutils.TestMessageSources;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;

/** Pruebas unitarias de la resolución de mensajes de {@link GlobalExceptionHandler}. */
class GlobalExceptionHandlerTest {
  private MessageSource messageSource;
  private GlobalExceptionHandler handler;
  private MockHttpServletRequest request;

  @BeforeEach
  void setUp() {
    messageSource = TestMessageSources.create();
    handler =
        new GlobalExceptionHandler(
            new ProblemDetailFactory(), new ExceptionLogger(), messageSource);
    request = new MockHttpServletRequest("GET", "/users/999");
  }

  @Test
  @DisplayName("Given a message key, when handle application exception, then detail is the text")
  void givenMessageKey_whenHandleApplicationException_thenDetailIsResolvedText() {
    ResourceNotFoundException ex =
        new ResourceNotFoundException(AppMessages.USER_NOT_FOUND_EXCEPTION);

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
  @DisplayName("Given every AppMessages constant, when resolved, then each has a bundle entry")
  void givenEveryAppMessagesConstant_whenResolved_thenEachHasBundleEntry() {
    List<String> missingKeys =
        Arrays.stream(AppMessages.class.getDeclaredFields())
            .filter(GlobalExceptionHandlerTest::isMessageKeyConstant)
            .map(GlobalExceptionHandlerTest::readConstant)
            .filter(
                key ->
                    messageSource.getMessage(key, null, null, LocaleContextHolder.getLocale())
                        == null)
            .toList();

    assertThat(missingKeys).isEmpty();
  }

  /**
   * Indica si el campo es una constante de clave de mensaje, es decir, una {@code public static
   * final String} distinta de {@code ILLEGAL_ACCESS_EXCEPTION}, que no es un mensaje de la API.
   *
   * @param field el campo a evaluar
   * @return {@code true} si el campo es una clave de mensaje
   */
  private static boolean isMessageKeyConstant(Field field) {
    int modifiers = field.getModifiers();
    return field.getType() == String.class
        && Modifier.isPublic(modifiers)
        && Modifier.isStatic(modifiers)
        && Modifier.isFinal(modifiers)
        && !field.getName().equals("ILLEGAL_ACCESS_EXCEPTION");
  }

  /**
   * Lee el valor de una constante estática de {@link AppMessages}.
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
