package edu.utdallas.davisbase.host;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import edu.utdallas.davisbase.DavisBaseException;
import edu.utdallas.davisbase.NotImplementedException;
import edu.utdallas.davisbase.command.ExecuteException;
import edu.utdallas.davisbase.compiler.CompileException;
import edu.utdallas.davisbase.parser.ParseException;
import edu.utdallas.davisbase.result.Result;
import edu.utdallas.davisbase.result.SelectResult;
import edu.utdallas.davisbase.result.ShowTablesResult;
import edu.utdallas.davisbase.result.UpdateResult;
import edu.utdallas.davisbase.storage.StorageException;
import edu.utdallas.davisbase.result.CreateIndexResult;
import edu.utdallas.davisbase.result.CreateTableResult;
import edu.utdallas.davisbase.result.DeleteResult;
import edu.utdallas.davisbase.result.DropTableResult;
import edu.utdallas.davisbase.result.ExitResult;
import edu.utdallas.davisbase.result.InsertResult;

public class Host {

  protected final HostConfiguration configuration;
  protected final BufferedReader reader;
  protected final PrintWriter writer;

  public Host(HostConfiguration configuration, BufferedReader reader, PrintWriter writer) {
    checkNotNull(configuration);
    checkNotNull(reader);
    checkNotNull(writer);

    this.configuration = configuration;
    this.reader = reader;
    this.writer = writer;
  }

  public String readStatement() throws IOException {
    // TODO Implement Host.readStatement()
    throw new NotImplementedException();
  }

  //region write(Result)

  public void write(Result result) throws IOException {
    checkNotNull(result);

    if (result instanceof CreateIndexResult) {
      write((CreateIndexResult) result);
    }
    else if (result instanceof CreateTableResult) {
      write((CreateTableResult) result);
    }
    else if (result instanceof DeleteResult) {
      write((DeleteResult) result);
    }
    else if (result instanceof DropTableResult) {
      write((DropTableResult) result);
    }
    else if (result instanceof ExitResult) {
      write((ExitResult) result);
    }
    else if (result instanceof InsertResult) {
      write((InsertResult) result);
    }
    else if (result instanceof SelectResult) {
      write((SelectResult) result);
    }
    else if (result instanceof ShowTablesResult) {
      write((ShowTablesResult) result);
    }
    else if (result instanceof UpdateResult) {
      write((UpdateResult) result);
    }
    else {
      throw newWriteNotImplementedException(result.getClass());
    }
  }

  public void write(CreateIndexResult result) throws IOException {
    // TODO Implement Host.write(CreateIndexResult)
    throw new NotImplementedException();
  }

  public void write(CreateTableResult result) throws IOException {
    // TODO Implement Host.write(CreateTableResult)
    throw new NotImplementedException();
  }

  public void write(DeleteResult result) throws IOException {
    // TODO Implement Host.write(DeleteResult)
    throw new NotImplementedException();
  }

  public void write(DropTableResult result) throws IOException {
    // TODO Implement Host.write(DropTableResult)
    throw new NotImplementedException();
  }

  public void write(ExitResult result) throws IOException {
    // TODO Implement Host.write(ExitResult)
    throw new NotImplementedException();
  }

  public void write(InsertResult result) throws IOException {
    // TODO Implement Host.write(InsertResult)
    throw new NotImplementedException();
  }

  public void write(SelectResult result) throws IOException {
    // TODO Implement Host.write(SelectResult)
    throw new NotImplementedException();
  }

  public void write(ShowTablesResult result) throws IOException {
    // TODO Implement Host.write(ShowTablesResult)
    throw new NotImplementedException();
  }

  public void write(UpdateResult result) throws IOException {
    // TODO Implement Host.write(UpdateResult)
    throw new NotImplementedException();
  }

  //endregion

  //region write(DavisBaseException)

  public void write(DavisBaseException exception) throws IOException {
    checkNotNull(exception);

    if (exception instanceof HostException) {
      write((HostException) exception);
    }
    else if (exception instanceof ParseException) {
      write((ParseException) exception);
    }
    else if (exception instanceof CompileException) {
      write((CompileException) exception);
    }
    else if (exception instanceof ExecuteException) {
      write((ExecuteException) exception);
    }
    else if (exception instanceof StorageException) {
      write((StorageException) exception);
    }
    else {
      throw newWriteNotImplementedException(exception.getClass());
    }
  }

  public void write(HostException exception) throws IOException {
    // TODO Implement Host.write(HostException)
    throw new NotImplementedException();
  }

  public void write(ParseException exception) throws IOException {
    // TODO Implement Host.write(ParseException)
    throw new NotImplementedException();
  }

  public void write(CompileException exception) throws IOException {
    // TODO Implement Host.write(CompileException)
    throw new NotImplementedException();
  }

  public void write(ExecuteException exception) throws IOException {
    // TODO Implement Host.write(ExecuteException)
    throw new NotImplementedException();
  }

  public void write(StorageException exception) throws IOException {
    // TODO Implement Host.write(StorageException)
    throw new NotImplementedException();
  }

  //endregion

  public void write(IOException exception) throws IOException {
    // TODO Implement Host.write(IOException)
    throw new NotImplementedException();
  }

  public void write(RuntimeException exception) throws IOException {
    // TODO Implement Host.write(RuntimeException)
    throw new NotImplementedException();
  }

  protected static NotImplementedException newWriteNotImplementedException(Class argumentClass) {
    checkNotNull(argumentClass);
    String message = String.format("%s.write(%s)", Host.class.getName(), argumentClass.getName());
    return new NotImplementedException(message);
  }

}
