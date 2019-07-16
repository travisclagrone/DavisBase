package edu.utdallas.davisbase.parser;

import edu.utdallas.davisbase.DavisBaseException;

public class ParseException extends DavisBaseException {

  private static final long serialVersionUID = -518391964966191323L;

  public ParseException() {
    super();
  }

  public ParseException(String message) {
    super(message);
  }

  public ParseException(String message, Throwable cause) {
    super(message, cause);
  }

  public ParseException(Throwable cause) {
    super(cause);
  }

}
