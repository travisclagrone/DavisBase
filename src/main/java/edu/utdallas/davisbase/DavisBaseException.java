package edu.utdallas.davisbase;

public class DavisBaseException extends Exception {

  private static final long serialVersionUID = 2608452553501271111L;

  public DavisBaseException() {
    super();
  }

  public DavisBaseException(String message) {
    super(message);
  }

  public DavisBaseException(String message, Throwable cause) {
    super(message, cause);
  }

  public DavisBaseException(Throwable cause) {
    super(cause);
  }

}
