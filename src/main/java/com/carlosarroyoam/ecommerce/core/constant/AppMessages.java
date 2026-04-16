package com.carlosarroyoam.ecommerce.core.constant;

public class AppMessages {
  public static final String ILLEGAL_ACCESS_EXCEPTION = "Illegal access to utility class";

  public static final String USER_NOT_FOUND_EXCEPTION = "User not found";

  public static final String CUSTOMER_NOT_FOUND_EXCEPTION = "Customer not found";

  public static final String CUSTOMER_ADDRESS_NOT_FOUND_EXCEPTION = "Customer address not found";

  public static final String PRODUCT_NOT_FOUND_EXCEPTION = "Product not found";

  public static final String CATEGORY_NOT_FOUND_EXCEPTION = "Category not found";

  public static final String PAYMENT_NOT_FOUND_EXCEPTION = "Payment not found";

  private AppMessages() {
    throw new IllegalAccessError(ILLEGAL_ACCESS_EXCEPTION);
  }
}
