package edu.utdallas.davisbase.executor;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import edu.utdallas.davisbase.NotImplementedException;
import edu.utdallas.davisbase.command.Command;
import edu.utdallas.davisbase.command.CreateIndexCommand;
import edu.utdallas.davisbase.command.CreateTableCommand;
import edu.utdallas.davisbase.command.DeleteCommand;
import edu.utdallas.davisbase.command.DropTableCommand;
import edu.utdallas.davisbase.command.ExitCommand;
import edu.utdallas.davisbase.command.InsertCommand;
import edu.utdallas.davisbase.command.SelectCommand;
import edu.utdallas.davisbase.command.ShowTablesCommand;
import edu.utdallas.davisbase.command.UpdateCommand;
import edu.utdallas.davisbase.result.CreateIndexResult;
import edu.utdallas.davisbase.result.CreateTableResult;
import edu.utdallas.davisbase.result.DeleteResult;
import edu.utdallas.davisbase.result.DropTableResult;
import edu.utdallas.davisbase.result.ExitResult;
import edu.utdallas.davisbase.result.InsertResult;
import edu.utdallas.davisbase.result.Result;
import edu.utdallas.davisbase.result.SelectResult;
import edu.utdallas.davisbase.result.ShowTablesResult;
import edu.utdallas.davisbase.result.UpdateResult;
import edu.utdallas.davisbase.storage.Storage;
import edu.utdallas.davisbase.storage.StorageException;

/**
 * An executor of {@link Command}s against a {@link Storage} context, and thereby a producer of
 * {@link Result}s.
 */
public class Executor {

  protected final ExecutorConfiguration configuration;

  public Executor(ExecutorConfiguration configuration) {
    checkNotNull(configuration, "configuration");

    this.configuration = configuration;
  }

  public Result execute(Command command, Storage context) throws ExecuteException, StorageException {
    checkNotNull(command, "command");
    checkNotNull(context, "context");

    Result result;
    if (command instanceof CreateIndexCommand) {
      result = execute((CreateIndexCommand) command, context);
    }
    else if (command instanceof CreateTableCommand) {
      result = execute((CreateTableCommand) command, context);
    }
    else if (command instanceof DeleteCommand) {
      result = execute((DeleteCommand) command, context);
    }
    else if (command instanceof DropTableCommand) {
      result = execute((DropTableCommand) command, context);
    }
    else if (command instanceof ExitCommand) {
      result = execute((ExitCommand) command, context);
    }
    else if (command instanceof InsertCommand) {
      result = execute((InsertCommand) command, context);
    }
    else if (command instanceof SelectCommand) {
      result = execute((SelectCommand) command, context);
    }
    else if (command instanceof ShowTablesCommand) {
      result = execute((ShowTablesCommand) command, context);
    }
    else if (command instanceof UpdateCommand) {
      result = execute((UpdateCommand) command, context);
    }
    else {
      throw new ExecuteException(format("Unimplemented command type: %s", command.getClass().getName()));
    }
    return result;
  }

  protected CreateIndexResult execute(CreateIndexCommand command, Storage context) throws ExecuteException, StorageException {
    assert command != null : "command should not be null";
    assert context != null : "context should not be null";

    // COMBAK Implement Executor.execute(CreateIndexCommand, Storage)
    throw new NotImplementedException();
  }

  protected CreateTableResult execute(CreateTableCommand command, Storage context) throws ExecuteException, StorageException {
    assert command != null : "command should not be null";
    assert context != null : "context should not be null";

    // TODO Implement Executor.execute(CreateTableCommand, Storage)
    throw new NotImplementedException();
  }

  protected DeleteResult execute(DeleteCommand command, Storage context) throws ExecuteException, StorageException {
    assert command != null : "command should not be null";
    assert context != null : "context should not be null";

    // COMBAK Implement Executor.execute(DeleteCommand, Storage)
    throw new NotImplementedException();
  }

  protected DropTableResult execute(DropTableCommand command, Storage context) throws ExecuteException, StorageException {
    assert command != null : "command should not be null";
    assert context != null : "context should not be null";

    // COMBAK Implement Executor.execute(DropTableCommand, Storage)
    throw new NotImplementedException();
  }

  protected ExitResult execute(ExitCommand command, Storage context) throws ExecuteException, StorageException {
    assert command != null : "command should not be null";
    assert context != null : "context should not be null";

    return new ExitResult();
  }

  protected InsertResult execute(InsertCommand command, Storage context) throws ExecuteException, StorageException {
    assert command != null : "command should not be null";
    assert context != null : "context should not be null";

    // TODO Implement Executor.execute(InsertCommand, Storage)
    throw new NotImplementedException();
  }

  protected SelectResult execute(SelectCommand command, Storage context) throws ExecuteException, StorageException {
    assert command != null : "command should not be null";
    assert context != null : "context should not be null";

    // TODO Implement Executor.execute(SelectCommand, Storage)
    throw new NotImplementedException();
  }

  protected ShowTablesResult execute(ShowTablesCommand command, Storage context) throws ExecuteException, StorageException {
    assert command != null : "command should not be null";
    assert context != null : "context should not be null";

    // TODO Implement Executor.execute(ShowTablesCommand, Storage)
    throw new NotImplementedException();
  }

  protected UpdateResult execute(UpdateCommand command, Storage context) throws ExecuteException, StorageException {
    assert command != null : "command should not be null";
    assert context != null : "context should not be null";

    // COMBAK Implement Executor.execute(UpdateCommand, Storage)
    throw new NotImplementedException();
  }

}
