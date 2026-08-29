package com.carlosarroyoam.ecommerce.core.constant;

/**
 * Constantes centralizadas con los mensajes de error usados en las excepciones de la API,
 * reutilizadas entre los distintos servicios para mantener consistencia en las respuestas de error.
 */
public class AppMessages {
  public static final String ILLEGAL_ACCESS_EXCEPTION = "Illegal access to utility class";

  public static final String FEATURE_NOT_IMPLEMENTED_EXCEPTION =
      "This feature is not yet implemented";

  public static final String JWT_REFRESH_TOKEN_IS_REQUIRED = "The refresh token is required";
  public static final String JWT_REFRESH_TOKEN_IS_NOT_VALID =
      "The provided refresh token is not valid or has expired";

  public static final String USER_NOT_FOUND_EXCEPTION = "User not found";
  public static final String USER_CANNOT_DELETE_ITSELF_EXCEPTION = "User cannot delete itself";
  public static final String USER_CANNOT_BE_DELETED_EXCEPTION = "User cannot be deleted";

  public static final String CUSTOMER_NOT_FOUND_EXCEPTION = "Customer not found";

  public static final String CUSTOMER_ADDRESS_NOT_FOUND_EXCEPTION = "Customer address not found";

  public static final String CATEGORY_NOT_FOUND_EXCEPTION = "Category not found";

  public static final String PRODUCT_NOT_FOUND_EXCEPTION = "Product not found";

  public static final String PRODUCT_VARIANT_NOT_FOUND_EXCEPTION = "Product variant not found";

  public static final String ATTRIBUTE_NOT_FOUND_EXCEPTION = "Attribute not found";

  public static final String PROPERTY_NOT_FOUND_EXCEPTION = "Property not found";

  public static final String ORDER_NOT_FOUND_EXCEPTION = "Order not found";
  public static final String ORDER_CANNOT_BE_CANCELLED_EXCEPTION = "Order cannot be cancelled";

  public static final String PAYMENT_NOT_FOUND_EXCEPTION = "Payment not found";

  public static final String SHIPMENT_NOT_FOUND_EXCEPTION = "Shipment not found";

  public static final String REFUND_NOT_FOUND_EXCEPTION = "Refund not found";

  /** Constructor privado que impide instanciar la clase. */
  private AppMessages() {
    throw new IllegalAccessError(ILLEGAL_ACCESS_EXCEPTION);
  }
}
