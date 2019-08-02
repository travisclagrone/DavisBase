package edu.utdallas.davisbase.storage;

import java.io.RandomAccessFile;
import java.time.LocalDate;
import java.time.Year;

//import org.omg.PortableInterceptor.SUCCESSFUL;

public class testBTree {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			TableRowBuilder tableRow = new TableRowBuilder();
			RandomAccessFile file;
			file = new RandomAccessFile("C:\\Users\\Supriya\\eclipse-workspace\\JavaPractice\\data\\Index.tbl", "rw");
			TableFile table = new TableFile(file);
			tableRow.appendText("davisbase_colegmefmkewngiuweniejoifjpowejgijgiorwkgnrwkgrjgnmewnvmveumns");
			tableRow.appendText("davisbase_colegmefmkewngiuweniejoifjpowejgijgiorwkgnrwkgrjgnmewnvmveumns");
			table.appendRow(tableRow);
			System.out.print("happy");
		} catch (Exception e) {
		}
	}
}
