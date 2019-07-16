package edu.utdallas.davisbase.host;

import static com.google.common.base.Preconditions.checkNotNull;

import edu.utdallas.davisbase.NotImplementedException;
import edu.utdallas.davisbase.result.Result;

public class Host {

  protected final HostConfiguration configuration;

  public Host(HostConfiguration configuration) {
    checkNotNull(configuration);
    this.configuration = configuration;
  }

  public String readStatement() throws HostException {
    // TODO Implement Host.readStatement()
    throw new NotImplementedException();
  }

  public boolean write(Result result) throws HostException {
    // TODO Implement Host.write(Result)
    throw new NotImplementedException();
  }

}
