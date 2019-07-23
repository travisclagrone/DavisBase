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

/**
 * An executor of {@link Command}s against a {@link Storage} context, and thereby a producer of
 * {@link Result}s.
 */
public class Executor {

  private final ExecutorConfiguration configuration;

  public Executor(ExecutorConfiguration configuration) {
    checkNotNull(configuration, "configuration");

    this.configuration = configuration;
  }

  public Result execute(Command command) throws ExecuteException {
    checkNotNull(command, "command");

    Result result;
    if (command instanceof CreateIndexCommand) {
      result = execute((CreateIndexCommand) command);
    }
    else if (command instanceof CreateTableCommand) {
      result = execute((CreateTableCommand) command);
    }
    else if (command instanceof DeleteCommand) {
      result = execute((DeleteCommand) command);
    }
    else if (command instanceof DropTableCommand) {
      result = execute((DropTableCommand) command);
    }
    else if (command instanceof ExitCommand) {
      result = execute((ExitCommand) command);
    }
    else if (command instanceof InsertCommand) {
      result = execute((InsertCommand) command);
    }
    else if (command instanceof SelectCommand) {
      result = execute((SelectCommand) command);
    }
    else if (command instanceof ShowTablesCommand) {
      result = execute((ShowTablesCommand) command);
    }
    else if (command instanceof UpdateCommand) {
      result = execute((UpdateCommand) command);
    }
    else {
      throw new ExecuteException(format("Unimplemented command type: %s", command.getClass().getName()));
    }
    return result;
  }

  public CreateIndexResult execute(CreateIndexCommand command) throws ExecuteException {
    // COMBAK Implement Executor.execute(CreateIndexCommand)
    throw new NotImplementedException();
  }

  public CreateTableResult execute(CreateTableCommand command) throws ExecuteException {
    // TODO Implement Executor.execute(CreateTableCommand)
    throw new NotImplementedException();
  }

  public DeleteResult execute(DeleteCommand command) throws ExecuteException {
    // COMBAK Implement Executor.execute(DeleteCommand)
    throw new NotImplementedException();
  }

  public DropTableResult execute(DropTableCommand command) throws ExecuteException {
    // COMBAK Implement Executor.execute(DropTableCommand)
    throw new NotImplementedException();
  }

  public ExitResult execute(ExitCommand command) throws ExecuteException {
    // TODO Implement Executor.execute(ExitCommand)
    throw new NotImplementedException();
  }

  public InsertResult execute(InsertCommand command) throws ExecuteException {
    // TODO Implement Executor.execute(InsertCommand)
    throw new NotImplementedException();
  }

  public SelectResult execute(SelectCommand command) throws ExecuteException {
    // TODO Implement Executor.execute(SelectCommand)
    throw new NotImplementedException();
  }

  public ShowTablesResult execute(ShowTablesCommand command) throws ExecuteException {
    // TODO Implement Executor.execute(ShowTablesCommand)
    throw new NotImplementedException();
  }

  public UpdateResult execute(UpdateCommand command) throws ExecuteException {
    // COMBAK Implement Executor.execute(UpdateCommand)
    throw new NotImplementedException();
  }

}
