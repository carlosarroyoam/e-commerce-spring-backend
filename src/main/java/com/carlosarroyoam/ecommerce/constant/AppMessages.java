package com.carlosarroyoam.ecommerce.constant;

public class AppMessages {
  public static final String ILLEGAL_ACCESS_EXCEPTION = "Illegal access to utility class";

  public static final String PRODUCT_NOT_FOUND_EXCEPTION = "Product not found";

  private AppMessages() {
    throw new IllegalAccessError(ILLEGAL_ACCESS_EXCEPTION);
  }
}