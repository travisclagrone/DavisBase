package edu.utdallas.davisbase.host;

import static java.lang.String.format;

import static org.checkerframework.checker.nullness.NullnessUtil.castNonNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.repeat;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.Nullable;

import edu.utdallas.davisbase.DavisBaseException;
import edu.utdallas.davisbase.NotImplementedException;
import edu.utdallas.davisbase.compiler.CompileException;
import edu.utdallas.davisbase.executor.ExecuteException;
import edu.utdallas.davisbase.parser.ParseException;
import edu.utdallas.davisbase.result.Result;
import edu.utdallas.davisbase.result.SelectResult;
import edu.utdallas.davisbase.result.SelectResultDataRow;
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
  protected final Scanner scanner;
  protected final PrintWriter printer;

  public Host(HostConfiguration configuration, Scanner scanner, PrintWriter printer) {
    checkNotNull(configuration, "configuration");
    checkNotNull(scanner, "scanner");
    checkNotNull(printer, "printer");

    this.configuration = configuration;
    this.scanner = scanner;
    this.printer = printer;
  }

  // TODO Handle case when multiple statements in one input sequence.
  public String readStatement() throws IOException {
    StringBuilder userInput = new StringBuilder();  // NOTE Initializing here to placate the nullness and initialization checkers, since they can't figure out the loop logic below for some reason...
    while (true) {
      userInput = new StringBuilder();

      printer.println(configuration.getPrompt());
      printer.flush();
      while (!Pattern.matches("^([^']|('(\\\\\\\\|\\\\'|[^'])*'))*;\\s*$", userInput)) {
        userInput.append(scanner.nextLine());
        userInput.append(configuration.getLineSeparator());
      }

      if (Pattern.matches("(?i)\\s*HELP\\*;\\s*", userInput)) {
        displayHelp();
        continue;
      }

      break;
    }
    return userInput.toString().trim();
  }

  public void displayWelcome() {
    printer.println(configuration.getWelcome());
    printer.flush();
  }

  // TODO Refactor help message content to configuration, or at least a local constant.
  public void displayHelp() {
    final String altHorizontalLine = repeat("*", 80);

    printer.println(altHorizontalLine);
    printer.println("SUPPORTED COMMANDS");
    printer.println("All commands below are case insensitive");
    printer.println();
    printer.println("\tSHOW TABLES;                                                 Display all the tables in the database.");
    printer.println("\tCREATE TABLE table_name (<column_name datatype>);            Create a new table in the database.");
    printer.println("\tINSERT INTO table_name VALUES (value1,value2,..);            Insert a new record into the table.");
    // writer.println("\tDELETE FROM TABLE table_name WHERE row_id = key_value;
    // Delete a record from the table whose rowid is <key_value>.");
    // writer.println("\tUPDATE table_name SET column_name = value WHERE row_id =
    // ..; Modifies the records in the table.");
    // writer.println("\tCREATE INDEX ON table_name (column_name); Create index for
    // the specified column in the table");
    printer.println("\tSELECT * FROM table_name;                                    Display all records in the table.");
    // writer.println("\tSELECT * FROM table_name WHERE column_name operator value;
    // Display records in the table where the given condition is satisfied.");
    // writer.println("\tDROP TABLE table_name; Remove table data and its schema.");
    printer.println("\tHELP;                                                        Show this help information.");
    printer.println("\tEXIT;                                                        Exit DavisBase.");
    printer.println();
    printer.println();
    printer.println(altHorizontalLine);
    printer.flush();
  }

  // region write(Result)

  public void write(Result result) throws IOException {
    checkNotNull(result);

    if (result instanceof CreateIndexResult) {
      writeCreateIndexResult((CreateIndexResult) result);
    }
    else if (result instanceof CreateTableResult) {
      writeCreateTableResult((CreateTableResult) result);
    }
    else if (result instanceof DeleteResult) {
      writeDeleteResult((DeleteResult) result);
    }
    else if (result instanceof DropTableResult) {
      writeDropTableResult((DropTableResult) result);
    }
    else if (result instanceof ExitResult) {
      writeExitResult((ExitResult) result);
    }
    else if (result instanceof InsertResult) {
      writeInsertResult((InsertResult) result);
    }
    else if (result instanceof SelectResult) {
      writeSelectResult((SelectResult) result);
    }
    else if (result instanceof ShowTablesResult) {
      writeShowTablesResult((ShowTablesResult) result);
    }
    else if (result instanceof UpdateResult) {
      writeUpdateResult((UpdateResult) result);
    }
    else {
      throw newWriteNotImplementedException(result.getClass());
    }
    printer.flush();
  }

  protected void writeCreateIndexResult(CreateIndexResult result) throws IOException {
    printer.println(
        format("Index was successfully created on column '%s' in table '%s'.",
            result.getColumnName(),
            result.getTableName()));
  }

  protected void writeCreateTableResult(CreateTableResult result) throws IOException {
    printer.println(
        format("'%s' table was successfully created.",
            result.getTableName()));
  }

  protected void writeDeleteResult(DeleteResult result) throws IOException {
    printer.println(
        format("%d rows were deleted in the table '%s'.",
            result.getRowsDeleted(),
            result.getTableName()));
  }

  protected void writeDropTableResult(DropTableResult result) throws IOException {
    printer.println(
        format("'%s' table is deleted from database.",
            result.getTableName()));
  }

  protected void writeExitResult(ExitResult result) throws IOException {
    printer.println("Exiting database...");
  }

  protected void writeInsertResult(InsertResult result) throws IOException {
    printer.println(
        format("%d rows were inserted in the table '%s'.",
            result.getRowsInserted(),
            result.getTableName()));
  }

  protected void writeSelectResult(SelectResult result) throws IOException {
    printer.println(repeat("-", ((result.getSchema().size()) * 8) + 3));
    for (int i = 0; i < result.getSchema().size(); i++) {
      printer.print(
          formatCellValue(
              result.getSchema().getColumnName(i).length(),
              result.getSchema().getColumnName(i))
          + "|");
    }
    printer.println();
    printer.println(repeat("-", ((result.getSchema().size()) * 8) + 3)); 

    if (result.getData().size() <= 0) {
      printer.println("Empty result set.");
      return;
    }    
    
    for (SelectResultDataRow row : result.getData()) {
      int iColumn = 0;
      for (@Nullable Object value : row) {
        printer.print(formatCellValue(result.getSchema().getColumnName(iColumn).length(), Objects.toString(castNonNull(value))) + "|");  // NOTE `value` actually can be null, and `Objects#toString(Object)` can actually accept a null argument, but the annotated JDK is over-strict here.
        iColumn++;
      }
      printer.println();
    }
    
  }

  protected void writeShowTablesResult(ShowTablesResult result) throws IOException {
    final String smallHorizontalLine = repeat("-", 10);
    final String columnName = "table_name";

    printer.println(smallHorizontalLine);
    printer.println(formatCellValue(columnName.length(), columnName) + "|");
    printer.println(smallHorizontalLine);

    if (result.getTableNames().size() <= 0) {
      printer.println("Empty result set.");
      return;
    }

    for (String tableName : result.getTableNames()) {
      printer.println(formatCellValue(columnName.length(), tableName) + "|");
    }
  }

  protected void writeUpdateResult(UpdateResult result) throws IOException {
    printer.println(
        format("%d rows were updated in the table '%s'.",
            result.getRowsUpdated(),
            result.getTableName()));
  }

  // endregion

  // region write(DavisBaseException)

  public void write(DavisBaseException exception) throws IOException {
    checkNotNull(exception);

    if (exception instanceof HostException) {
      writeHostException((HostException) exception);
    }
    else if (exception instanceof ParseException) {
      writeParseException((ParseException) exception);
    }
    else if (exception instanceof CompileException) {
      writeCompileException((CompileException) exception);
    }
    else if (exception instanceof ExecuteException) {
      writeExecuteException((ExecuteException) exception);
    }
    else if (exception instanceof StorageException) {
      writeStorageException((StorageException) exception);
    }
    else {
      throw newWriteNotImplementedException(exception.getClass());
    }
    printer.flush();
  }

  protected void writeHostException(HostException exception) throws IOException {
    printer.println("A DavisBaseException occurred in the Host component with the following message : " + exception.getMessage());
  }

  protected void writeParseException(ParseException exception) throws IOException {
    printer.println("A DavisBaseException occurred in the Parser component with the following message : " + exception.getMessage());
  }

  protected void writeCompileException(CompileException exception) throws IOException {
    printer.println("A DavisBaseException occurred in the Compiler component with the following message : " + exception.getMessage());
  }

  protected void writeExecuteException(ExecuteException exception) throws IOException {
    printer.println("A DavisBaseException occurred in the Executor component with the following message : " + exception.getMessage());
  }

  protected void writeStorageException(StorageException exception) throws IOException {
    printer.println("A DavisBaseException occurred in the Storage component with the following message : " + exception.getMessage());
  }

  // endregion

  public void write(IOException exception) throws IOException {
    checkNotNull(exception, "exception");

    printer.println("An IOException occurred with the following message : " + exception.getMessage());
    printer.flush();
  }

  public void write(RuntimeException exception) throws IOException {
    checkNotNull(exception, "exception");

    printer.println("A RuntimeException occurred with the following message : " + exception.getMessage());
    printer.flush();
  }

  protected static NotImplementedException newWriteNotImplementedException(Class<?> argumentClass) {
    assert argumentClass != null : "argumentClass should not be null";

    final String message = String.format("%s.write(%s)", Host.class.getName(), argumentClass.getName());
    return new NotImplementedException(message);
  }

  protected static String formatCellValue(int len, String str) {
    assert 0 <= len && (len + 3) <= Integer.MAX_VALUE : format("len = %d", len);
    assert str != null : "str should not be null";

    return String.format("%-" + (len + 3) + "s", str);
  }

}
