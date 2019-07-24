package edu.utdallas.davisbase.host;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

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
  protected final Scanner reader;
  protected final PrintWriter writer;

  public Host(HostConfiguration configuration, BufferedReader reader, PrintWriter writer) {
    checkNotNull(configuration);
    checkNotNull(reader);
    checkNotNull(writer);

    this.configuration = configuration;
    this.reader = new Scanner(reader);
    this.writer = writer;
  }

  public String readStatement() throws IOException {

    configuration.PromptScreen();

    String userInput = "";

    writer.write(configuration.prompt);
    userInput = reader.useDelimiter(";").next().replace("\n", " ").replace("\r", "").trim().toLowerCase();
    return userInput;
  }

  public static String line_chars(String s, int num) {
    String a = "";
    for (int i = 0; i < num; i++) {
      a += s;
    }
    return a;
  }

  public String display_fix(int len, String s) {
    return String.format("%-" + (len + 3) + "s", s);
  }

  public void help() {
    writer.println(line_chars("*", 80));
    writer.println("SUPPORTED COMMANDS");
    writer.println("All commands below are case insensitive");
    writer.println();
    writer.println(
        "\tSHOW TABLES;                                                 Display all the tables in the database.");
    writer
        .println("\tCREATE TABLE table_name (<column_name datatype>);            Create a new table in the database.");
    writer
        .println("\tINSERT INTO table_name VALUES (value1,value2,..);            Insert a new record into the table.");
    // writer.println("\tDELETE FROM TABLE table_name WHERE row_id = key_value;
    // Delete a record from the table whose rowid is <key_value>.");
    // writer.println("\tUPDATE table_name SET column_name = value WHERE row_id =
    // ..; Modifies the records in the table.");
    // writer.println("\tCREATE INDEX ON table_name (column_name); Create index for
    // the specified column in the table");
    writer.println("\tSELECT * FROM table_name;                                    Display all records in the table.");
    // writer.println("\tSELECT * FROM table_name WHERE column_name operator value;
    // Display records in the table where the given condition is satisfied.");
    // writer.println("\tDROP TABLE table_name; Remove table data and its schema.");
    writer.println("\tVERSION;                                                     Show the program version.");
    writer.println("\tHELP;                                                        Show this help information.");
    writer.println("\tEXIT;                                                        Exit DavisBase.");
    writer.println();
    writer.println();
    writer.println(line_chars("*", 80));
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
      writer.println("Index was successfully created on column '" + result.getColumnName() + "' in table '"
          + result.getTableName() + "'.");
    } catch (Exception e) {
      writer.println("A write exception occurred while writing CreateIndexResult."+
      "The exception message : "+e.getMessage());
    }
  }

  public void write(CreateTableResult result) throws IOException {

    try {
      writer.println("'" + result.getTableName() + "' table was successfully created.");
    } catch (Exception e) {
      writer.println("A write exception occurred while writing CreateTableResult."+
      "The exception message : "+e.getMessage());
    }
  }

  public void write(DeleteResult result) throws IOException {

    try {
      writer.println(result.getRowsDeleted() + " rows were deleted in the table '" + result.getTableName() + "'.");
    } catch (Exception e) {
      writer.println("A write exception occurred while writing DeleteResult."+
      "The exception message : "+e.getMessage());
    }
  }

  public void write(DropTableResult result) throws IOException {

    try {
      writer.println("'" + result.getTableName() + "' table is deleted from database.");
    } catch (Exception e) {
      writer.println("A write exception occurred while writing DropTableResult."+
      "The exception message : "+e.getMessage());
    }
  }

  public void write(ExitResult result) throws IOException {

    try {
      writer.println("Exiting database....");
    } catch (Exception e) {
      writer.println("A write exception occurred while writing ExitResult."+
      "The exception message : "+e.getMessage());
    }
  }

  public void write(InsertResult result) throws IOException {

    try {
      writer.println(result.getRowsInserted() + " rows were inserted in the table '" + result.getTableName() + "'.");
    } catch (Exception e) {
      writer.println("A write exception occurred while writing InsertResult."+
      "The exception message : "+e.getMessage());
    }
  }

  public void write(SelectResult result) throws IOException {

    try {
      writer.print(line_chars("-", ((result.getSchema().size()) * 8) + 3));
      writer.println();

      for (int i = 0; i < result.getSchema().size(); i++) {

        writer
            .print(display_fix(result.getSchema().getColumnName(i).length(), result.getSchema().getColumnName(i)) + "|");
      }
      writer.println();

      writer.print(line_chars("-", ((result.getSchema().size()) * 8) + 3));
      writer.println();

      if (result.getData().size() == 0)
        writer.println("Empty result set.");

      else {

        for (SelectResultDataRow row : result.getData()) {

          for (@Nullable Object value : row) {
            writer.print(display_fix(10, value.toString()) + "|");
          }

        /*  Iterator<Object> iterator = row.iterator();
          while (iterator.hasNext()) {

            writer.print(display_fix(10, iterator.next().toString()) + "|");
          } */
          writer.println();
        }
      }
    } catch (Exception e) {
        writer.println("A write exception occurred while writing SelectResult."+
        "The exception message : "+e.getMessage());
    }

  }

  public void write(ShowTablesResult result) throws IOException {

    try {
      writer.print(line_chars("-", 10));
      writer.println();

      writer.println("table_name   |");

      writer.print(line_chars("-", 10));
      writer.println();

      if (result.getTableNames().size() == 0)
        writer.println("Empty result set.");

      else {
        for (String tableName : result.getTableNames())
          writer.println(display_fix(10, tableName) + "|");
      }
    } catch (Exception e) {
        writer.println("A write exception occurred while writing ShowTablesResult."+
          "The exception message : "+e.getMessage());
    }
  }

  public void write(UpdateResult result) throws IOException {

    try {
      writer.println(result.getRowsUpdated() + " rows were updated in the table '" + result.getTableName() + "'.");
    } catch (Exception e) {
      writer.println("A write exception occurred while writing UpdateResult."+
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

    writer.println("There is an exception in Host with the following message : " + exception.getMessage());
  }

  public void write(ParseException exception) throws IOException {

    writer.println("There is an exception in Parser with the following message : " + exception.getMessage());
  }

  public void write(CompileException exception) throws IOException {

    writer.println("There is an exception in Compiler with the following message : " + exception.getMessage());
  }

  public void write(ExecuteException exception) throws IOException {

    writer.println("There is an exception in Executer with the following message : " + exception.getMessage());
  }

  public void write(StorageException exception) throws IOException {

    writer.println("There is an exception in Storage with the following message : " + exception.getMessage());
  }

  // endregion

  public void write(IOException exception) throws IOException {

    writer.println("There is an IOexception with the following message : " + exception.getMessage());
  }

  public void write(RuntimeException exception) throws IOException {

    writer.println("There is an Runtime exception with the following message : " + exception.getMessage());
  }

  protected static NotImplementedException newWriteNotImplementedException(Class argumentClass) {
    checkNotNull(argumentClass);
    String message = String.format("%s.write(%s)", Host.class.getName(), argumentClass.getName());
    return new NotImplementedException(message);
  }

}
