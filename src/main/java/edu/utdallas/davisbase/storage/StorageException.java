package edu.utdallas.davisbase.storage;

import edu.utdallas.davisbase.DavisBaseException;

public class StorageException extends DavisBaseException {

  private static final long serialVersionUID = 8464284371638656479L;

  public StorageException() {
    super();
  }

  public StorageException(String message) {
    super(message);
  }

  public StorageException(String message, Throwable cause) {
    super(message, cause);
  }

  public StorageException(Throwable cause) {
    super(cause);
  }

}
