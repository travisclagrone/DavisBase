package edu.utdallas.davisbase.storage;

import java.awt.font.NumericShaper;
import java.io.RandomAccessFile;

public class CreateTable {
	static int pagesize=512;
	static int noOfPages=1000;
	static String path = "C:\\\\Users\\\\Supriya\\\\Documents\\\\GitHub\\\\DavisBase\\\\src\\\\main\\\\java\\\\edu\\\\utdallas\\\\davisbase\\\\";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		insertNewTable("davisbase_tables");
		try {
			String tableName = "davisbase_columns";
			RandomAccessFile tableFile = new RandomAccessFile(path+"data/"+tableName+".tbl", "rw");
			System.out.print(tableFile.length());
		} catch (Exception c) {
			
		}
	}

	public void insertSchema(String tableName, String[] columnNames, String[] nullable, String[] dataType ) {
		
		String[] davisbase_Columns = {"rowid", "table_name", "column_name", "data_type", "ordinal_position", "is_nullable"};
		
	}
	public void CreatCommandExecution(String CreateQuery) {
		String tableName = getTableName(CreateQuery);
		insertNewTable(tableName);

	}

	// getting table name from query
	public String getTableName(String Query) {
		String tabelName = "TableName";
		return tabelName;
	}

	// update davisbase_tables with new table info
	public static void insertNewTable(String tableName) {
		try {
			RandomAccessFile tableFile = new RandomAccessFile(path+"data/" + tableName + ".tbl", "rw");
			int numofPages=(int) (tableFile.length()/((long) pagesize));
			
			//number of records
			tableFile.seek((numofPages-1)*pagesize+2);
			short numofrecords=tableFile.readShort();
			tableFile.seek((numofPages-1)*pagesize+8);
			
			//reading all the starting points of record
			byte[] recordsPoints=new byte[numofrecords];
			tableFile.read(recordsPoints);
			
			for(int i=0;i<recordsPoints.length;i++) {
				
			}
			
//			tableFile.seek(0x00);
//			tableFile.writeByte(0x0D);
			tableFile.close();
		} catch (Exception e) {
		}
	}

	// deduce the columns info from the Query
	public String[] getColumnInfo(String Query) {
		return null;
	}

	// Update davisbase_columns with the new table column info
	public void insertColumnInfo(String[] columnInfoArr) {

	}

}
