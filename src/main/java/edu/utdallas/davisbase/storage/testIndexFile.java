package edu.utdallas.davisbase.storage;

import java.io.RandomAccessFile;

public class testIndexFile {

  public static void main(String[] args) {
    // TODO Auto-generated method stub
    try {

      RandomAccessFile file;
      file = new RandomAccessFile(
          "C:\\Users\\Hanumantha Rao\\Documents\\Semesters\\Summer 2019\\CS 6360 - DB Design\\Projects\\Part 1\\DavisBase\\data\\test1.tbl", "rw");
      IndexFile iFile = new IndexFile(file);
      System.out.println("happy");
      iFile.addText("hanum", 11);
      iFile.addText("hanu", 11);
      System.out.print("happy");
    } catch (Exception e) {

    }

  }

}
