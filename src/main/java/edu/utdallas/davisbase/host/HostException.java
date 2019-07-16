package edu.utdallas.davisbase.host;

import edu.utdallas.davisbase.DavisBaseException;

public class HostException extends DavisBaseException {

  private static final long serialVersionUID = 5239327211212046718L;

  public HostException() {
    super();
  }

  public HostException(String message) {
    super(message);
  }

  public HostException(String message, Throwable cause) {
    super(message, cause);
  }

  public HostException(Throwable cause) {
    super(cause);
  }

}
