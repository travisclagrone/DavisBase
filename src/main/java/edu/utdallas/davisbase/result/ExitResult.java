package edu.utdallas.davisbase.result;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.Objects;

public class ExitResult implements Result {

  @Override
  public boolean equals(Object obj) {
    return obj != null && obj instanceof ExitResult;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getClass());
  }

  @Override
  public String toString() {
    return toStringHelper(getClass()).toString();
  }

}
