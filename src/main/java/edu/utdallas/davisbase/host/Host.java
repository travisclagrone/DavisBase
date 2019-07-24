package edu.utdallas.davisbase.host;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.repeat;

import java.io.IOException;
import java.io.PrintWriter;
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
    StringBuilder userInput;
    while (true) {
      userInput = new StringBuilder();

      printer.println(configuration.getPrompt());
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

  public String display_fix(int len, String s) {
    return String.format("%-" + (len + 3) + "s", s);
  }

  public void displayWelcome() {
    printer.println(configuration.getWelcome());
  }

  public void displayHelp() {
    printer.println(repeat("*", 80));
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
    printer.println("\tVERSION;                                                     Show the program version.");
    printer.println("\tHELP;                                                        Show this help information.");
    printer.println("\tEXIT;                                                        Exit DavisBase.");
    printer.println();
    printer.println();
    printer.println(repeat("*", 80));
  }

  // region write(Result)

  public void write(Result result) throws IOException {
    checkNotNull(result);

    if (result instanceof CreateIndexResult) {
      write((CreateIndexResult) result);
    } else if (result instanceof CreateTableResult) {
      write((CreateTableResult) result);
    } else if (result instanceof DeleteResult) {
      write((DeleteResult) result);
    } else if (result instanceof DropTableResult) {
      write((DropTableResult) result);
    } else if (result instanceof ExitResult) {
      write((ExitResult) result);
    } else if (result instanceof InsertResult) {
      write((InsertResult) result);
    } else if (result instanceof SelectResult) {
      write((SelectResult) result);
    } else if (result instanceof ShowTablesResult) {
      write((ShowTablesResult) result);
    } else if (result instanceof UpdateResult) {
      write((UpdateResult) result);
    } else {
      throw newWriteNotImplementedException(result.getClass());
    }
  }

  public void write(CreateIndexResult result) throws IOException {

    try {
      printer.println("Index was successfully created on column '" + result.getColumnName() + "' in table '"
          + result.getTableName() + "'.");
    } catch (Exception e) {
      printer.println("A write exception occurred while writing CreateIndexResult."+
      "The exception message : "+e.getMessage());
    }
  }

  public void write(CreateTableResult result) throws IOException {

    try {
      printer.println("'" + result.getTableName() + "' table was successfully created.");
    } catch (Exception e) {
      printer.println("A write exception occurred while writing CreateTableResult."+
      "The exception message : "+e.getMessage());
    }
  }

  public void write(DeleteResult result) throws IOException {

    try {
      printer.println(result.getRowsDeleted() + " rows were deleted in the table '" + result.getTableName() + "'.");
    } catch (Exception e) {
      printer.println("A write exception occurred while writing DeleteResult."+
      "The exception message : "+e.getMessage());
    }
  }

  public void write(DropTableResult result) throws IOException {

    try {
      printer.println("'" + result.getTableName() + "' table is deleted from database.");
    } catch (Exception e) {
      printer.println("A write exception occurred while writing DropTableResult."+
      "The exception message : "+e.getMessage());
    }
  }

  public void write(ExitResult result) throws IOException {

    try {
      printer.println("Exiting database....");
    } catch (Exception e) {
      printer.println("A write exception occurred while writing ExitResult."+
      "The exception message : "+e.getMessage());
    }
  }

  public void write(InsertResult result) throws IOException {

    try {
      printer.println(result.getRowsInserted() + " rows were inserted in the table '" + result.getTableName() + "'.");
    } catch (Exception e) {
      printer.println("A write exception occurred while writing InsertResult."+
      "The exception message : "+e.getMessage());
    }
  }

  public void write(SelectResult result) throws IOException {

    try {
      printer.print(repeat("-", ((result.getSchema().size()) * 8) + 3));
      printer.println();

      for (int i = 0; i < result.getSchema().size(); i++) {

        printer
            .print(display_fix(result.getSchema().getColumnName(i).length(), result.getSchema().getColumnName(i)) + "|");
      }
      printer.println();

      printer.print(repeat("-", ((result.getSchema().size()) * 8) + 3));
      printer.println();

      if (result.getData().size() == 0)
        printer.println("Empty result set.");

      else {

        for (SelectResultDataRow row : result.getData()) {

          for (@Nullable Object value : row) {
            printer.print(display_fix(10, value.toString()) + "|");
          }

        /*  Iterator<Object> iterator = row.iterator();
          while (iterator.hasNext()) {

            writer.print(display_fix(10, iterator.next().toString()) + "|");
          } */
          printer.println();
        }
      }
    } catch (Exception e) {
        printer.println("A write exception occurred while writing SelectResult."+
        "The exception message : "+e.getMessage());
    }

  }

  public void write(ShowTablesResult result) throws IOException {

    try {
      printer.print(repeat("-", 10));
      printer.println();

      printer.println("table_name   |");

      printer.print(repeat("-", 10));
      printer.println();

      if (result.getTableNames().size() == 0)
        printer.println("Empty result set.");

      else {
        for (String tableName : result.getTableNames())
          printer.println(display_fix(10, tableName) + "|");
      }
    } catch (Exception e) {
        printer.println("A write exception occurred while writing ShowTablesResult."+
          "The exception message : "+e.getMessage());
    }
  }

  public void write(UpdateResult result) throws IOException {

    try {
      printer.println(result.getRowsUpdated() + " rows were updated in the table '" + result.getTableName() + "'.");
    } catch (Exception e) {
      printer.println("A write exception occurred while writing UpdateResult."+
        "The exception message : "+e.getMessage());
    }
  }

  // endregion

  // region write(DavisBaseException)

  public void write(DavisBaseException exception) throws IOException {
    checkNotNull(exception);

    if (exception instanceof HostException) {
      write((HostException) exception);
    } else if (exception instanceof ParseException) {
      write((ParseException) exception);
    } else if (exception instanceof CompileException) {
      write((CompileException) exception);
    } else if (exception instanceof ExecuteException) {
      write((ExecuteException) exception);
    } else if (exception instanceof StorageException) {
      write((StorageException) exception);
    } else {
      throw newWriteNotImplementedException(exception.getClass());
    }
  }

  public void write(HostException exception) throws IOException {

    printer.println("There is an exception in Host with the following message : " + exception.getMessage());
  }

  public void write(ParseException exception) throws IOException {

    printer.println("There is an exception in Parser with the following message : " + exception.getMessage());
  }

  public void write(CompileException exception) throws IOException {

    printer.println("There is an exception in Compiler with the following message : " + exception.getMessage());
  }

  public void write(ExecuteException exception) throws IOException {

    printer.println("There is an exception in Executer with the following message : " + exception.getMessage());
  }

  public void write(StorageException exception) throws IOException {

    printer.println("There is an exception in Storage with the following message : " + exception.getMessage());
  }

  // endregion

  public void write(IOException exception) throws IOException {

    printer.println("There is an IOexception with the following message : " + exception.getMessage());
  }

  public void write(RuntimeException exception) throws IOException {

    printer.println("There is an Runtime exception with the following message : " + exception.getMessage());
  }

  protected static NotImplementedException newWriteNotImplementedException(Class argumentClass) {
    checkNotNull(argumentClass);
    String message = String.format("%s.write(%s)", Host.class.getName(), argumentClass.getName());
    return new NotImplementedException(message);
  }

}
