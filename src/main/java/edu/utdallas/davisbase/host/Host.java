package edu.utdallas.davisbase.host;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import edu.utdallas.davisbase.DavisBaseException;
import edu.utdallas.davisbase.NotImplementedException;
import edu.utdallas.davisbase.command.ExecuteException;
import edu.utdallas.davisbase.compiler.CompileException;
import edu.utdallas.davisbase.parser.ParseException;
import edu.utdallas.davisbase.parser.Parser;
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

  static String prompt = "davisql> ";
	static String copyright = "Team Blue";
  static String version = "v1.0";
  static boolean exit_prompt = false;

	static Scanner scanner = new Scanner(System.in).useDelimiter(";");

  public String readStatement(String input) throws IOException {

    PromptScreen();

		String userInput = "";

		System.out.print(prompt);
    userInput = scanner.next().replace("\n", " ").replace("\r", "").trim().toLowerCase();
    return userInput;
  }


  public static void PromptScreen() {
		System.out.println(line_chars("-",80));
    System.out.println("Welcome to DavisBase");
		System.out.println("DavisBase Version " + version);
		System.out.println(copyright);
		System.out.println("\nType \"help;\" to display supported commands.");
		System.out.println(line_chars("-",80));
	}


	public static String line_chars(String s,int num) {
		String a = "";
		for(int i=0;i<num;i++) {
			a += s;
		}
		return a;
  }

  public String display_fix(int len, String s) {
		return String.format("%-"+(len+3)+"s", s);
	}


	public static void help() {
		System.out.println(line_chars("*",80));
		System.out.println("SUPPORTED COMMANDS");
		System.out.println("All commands below are case insensitive");
		System.out.println();
		System.out.println("\tSHOW TABLES;                                                 Display all the tables in the database.");
		System.out.println("\tCREATE TABLE table_name (<column_name datatype>);            Create a new table in the database.");
		System.out.println("\tINSERT INTO table_name VALUES (value1,value2,..);            Insert a new record into the table.");
		System.out.println("\tDELETE FROM TABLE table_name WHERE row_id = key_value;       Delete a record from the table whose rowid is <key_value>.");
		System.out.println("\tUPDATE table_name SET column_name = value WHERE row_id = ..; Modifies the records in the table.");
		System.out.println("\tCREATE INDEX ON table_name (column_name);                    Create index for the specified column in the table");
		System.out.println("\tSELECT * FROM table_name;                                    Display all records in the table.");
		System.out.println("\tSELECT * FROM table_name WHERE column_name operator value;   Display records in the table where the given condition is satisfied.");
		System.out.println("\tDROP TABLE table_name;                                       Remove table data and its schema.");
		System.out.println("\tVERSION;                                                     Show the program version.");
		System.out.println("\tHELP;                                                        Show this help information.");
		System.out.println("\tEXIT;                                                        Exit DavisBase.");
		System.out.println();
		System.out.println();
		System.out.println(line_chars("*",80));
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

    try {
      System.out.println("Index was successfully created on column '"+result.getColumnName()
      +"' in table '"+result.getTableName()+ "'.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void write(CreateTableResult result) throws IOException {

    try {
      System.out.println("'"+result.getTableName()+"' table was successfully created.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void write(DeleteResult result) throws IOException {

    try {
      System.out.println(result.getRowsDeleted() +" rows were deleted in the table '"+result.getTableName()+"'.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void write(DropTableResult result) throws IOException {

    try {
      System.out.println("'"+result.getTableName() +"' table is deleted from database.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void write(ExitResult result) throws IOException {

    try {
      System.out.println("Exiting database....");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void write(InsertResult result) throws IOException {

    try {
      System.out.println(result.getRowsInserted() +" rows were inserted in the table '"+result.getTableName()+"'.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void write(SelectResult result) throws IOException {

    if(result.getData().size() == 0)
      System.out.println("Empty result set.");

    else {

      System.out.print(line_chars("-", ((result.getSchema().size())*8)+ 3));
      System.out.println();

      for(int i = 0; i < result.getSchema().size(); i++){

        System.out.print(display_fix(result.getSchema().getColumnName(i).length(), result.getSchema().getColumnName(i))+"|");
      }
      System.out.println();

      System.out.print(line_chars("-", ((result.getSchema().size())*8)+ 3));
      System.out.println();

      for(SelectResultDataRow row : result.getData()){

        while(row.iterator().hasNext()){

          System.out.print(display_fix(10,row.iterator().next().toString())+"|");
        }
        System.out.println();
      }

    }

  }

  public void write(ShowTablesResult result) throws IOException {

    if(result.getTableNames().size() == 0)
      System.out.println("Empty result set.");

    else{
      System.out.print(line_chars("-", 10));
      System.out.println();

      System.out.println("table_name   |");

      System.out.print(line_chars("-", 10));
      System.out.println();

      for(String tableName : result.getTableNames())
        System.out.println(display_fix(10, tableName) + "|");
    }
  }

  public void write(UpdateResult result) throws IOException {

    try {
      System.out.println(result.getRowsUpdated() +" rows were updated in the table '"+result.getTableName()+"'.");
    } catch (Exception e) {
      e.printStackTrace();
    }
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
