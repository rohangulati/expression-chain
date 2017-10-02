package com.rohangulati.expressionchain.util;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import static java.lang.String.format;

public final class Preconditions {

  public static <T> void checkNotEmpty(T[] elements) {
    if (elements == null || elements.length == 0) {
      throw new IllegalArgumentException();
    }
  }

  public static <T> void checkNotEmpty(T[] elements, @Nullable Object errorMessage) {
    if (elements == null || elements.length == 0) {
      throw new IllegalArgumentException(String.valueOf(errorMessage));
    }
  }

  public static <T> void checkNotEmpty(Collection<T> collection) {
    if (collection == null || collection.size() == 0) {
      throw new IllegalArgumentException();
    }
  }

  public static <T> void checkNotEmpty(Collection<T> collection, @Nullable Object errorMessage) {
    if (collection == null || collection.size() == 0) {
      throw new IllegalArgumentException(String.valueOf(errorMessage));
    }
  }

  public static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }

  public static <T> T checkNotNull(T reference, @Nullable Object errorMessage) {
    if (reference == null) {
      throw new NullPointerException(String.valueOf(errorMessage));
    }
    return reference;
  }

  public static <T> T checkNotNull(T reference,
                                   @Nullable String errorMessageTemplate,
                                   @Nullable Object... errorMessageArgs) {
    if (reference == null) {
      // If either of these parameters is null, the right thing happens anyway
      throw new NullPointerException(format(errorMessageTemplate, errorMessageArgs));
    }
    return reference;
  }

  public static void checkArgument(boolean expression) {
    if (!expression) {
      throw new IllegalArgumentException();
    }
  }

  private Preconditions() {
    // no instance creation
  }
}
