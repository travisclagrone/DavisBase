package edu.utdallas.davisbase.compiler;

import edu.utdallas.davisbase.DavisBaseException;

public class CompileException extends DavisBaseException {

  private static final long serialVersionUID = 8412887049117246638L;

  public CompileException() {
    super();
  }

  public CompileException(String message) {
    super(message);
  }

  public CompileException(String message, Throwable cause) {
    super(message, cause);
  }

  public CompileException(Throwable cause) {
    super(cause);
  }

}
