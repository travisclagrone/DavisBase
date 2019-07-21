package edu.utdallas.davisbase.parser;

import edu.utdallas.davisbase.DavisBaseException;

public class ParseWhereException extends DavisBaseException {

  private static final long serialVersionUID = -518391964966191323L;

  public ParseWhereException() {
    super();
  }

  public ParseWhereException(String message) {
    super(message);
  }

  public ParseWhereException(String message, Throwable cause) {
    super(message, cause);
  }

  public ParseWhereException(Throwable cause) {
    super(cause);
  }

}
