package edu.utdallas.davisbase;

/**
 * Exception thrown when a feature is not yet implemented.
 *
 * This exception is intended to be used only during development for scaffolding
 * and test-driven design purposes; it should never appear in production.
 */
public class NotImplementedException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public NotImplementedException() {
    super();
  }

  public NotImplementedException(String message) {
    super(message);
  }

  public NotImplementedException(String message, Throwable cause) {
    super(message, cause);
  }

  public NotImplementedException(Throwable cause) {
    super(cause);
  }
}
