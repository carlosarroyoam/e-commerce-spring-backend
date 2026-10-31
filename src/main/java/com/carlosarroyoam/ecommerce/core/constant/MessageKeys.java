package com.carlosarroyoam.ecommerce.core.constant;

/**
 * Constantes centralizadas con las claves de los mensajes de error usados en las excepciones de la
 * API, reutilizadas entre los distintos servicios para mantener consistencia en las respuestas de
 * error. El texto de cada clave vive en {@code messages.properties} y lo resuelve {@link
 * com.carlosarroyoam.ecommerce.core.exception.GlobalExceptionHandler}.
 */
public class MessageKeys {
  public static final String FEATURE_NOT_IMPLEMENTED_EXCEPTION = "feature.not-implemented";

  public static final String REFRESH_TOKEN_IS_REQUIRED_EXCEPTION = "auth.refresh-token.required";
  public static final String REFRESH_TOKEN_IS_NOT_VALID_EXCEPTION = "auth.refresh-token.invalid";

  public static final String USER_NOT_FOUND_EXCEPTION = "user.not-found";
  public static final String USER_CANNOT_DELETE_ITSELF_EXCEPTION = "user.cannot-delete-itself";
  public static final String USER_CANNOT_BE_DELETED_EXCEPTION = "user.cannot-be-deleted";

  public static final String CUSTOMER_NOT_FOUND_EXCEPTION = "customer.not-found";
  public static final String CUSTOMER_ADDRESS_NOT_FOUND_EXCEPTION = "customer.address.not-found";

  public static final String CATEGORY_NOT_FOUND_EXCEPTION = "category.not-found";

  public static final String PRODUCT_NOT_FOUND_EXCEPTION = "product.not-found";
  public static final String PRODUCT_VARIANT_NOT_FOUND_EXCEPTION = "product.variant.not-found";
  public static final String ATTRIBUTE_NOT_FOUND_EXCEPTION = "attribute.not-found";
  public static final String PROPERTY_NOT_FOUND_EXCEPTION = "property.not-found";

  public static final String ORDER_NOT_FOUND_EXCEPTION = "order.not-found";
  public static final String ORDER_CANNOT_BE_CANCELED_EXCEPTION = "order.cannot-be-canceled";

  public static final String PAYMENT_NOT_FOUND_EXCEPTION = "payment.not-found";

  public static final String SHIPMENT_NOT_FOUND_EXCEPTION = "shipment.not-found";

  public static final String REFUND_NOT_FOUND_EXCEPTION = "refund.not-found";

  /** Constructor privado que impide instanciar la clase. */
  private MessageKeys() {
    throw new IllegalAccessError(CommonConstants.ILLEGAL_ACCESS_ERROR);
  }
}
