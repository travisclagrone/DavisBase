package edu.utdallas.davisbase.command;

import edu.utdallas.davisbase.DavisBaseException;

public class ExecuteException extends DavisBaseException {

  private static final long serialVersionUID = 3237082395181809434L;

  public ExecuteException() {
    super();
  }

  public ExecuteException(String message) {
    super(message);
  }

  public ExecuteException(String message, Throwable cause) {
    super(message, cause);
  }

  public ExecuteException(Throwable cause) {
    super(cause);
  }

}
