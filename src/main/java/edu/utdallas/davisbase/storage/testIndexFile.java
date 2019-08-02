package edu.utdallas.davisbase.storage;

import java.io.RandomAccessFile;

public class testIndexFile {

  public static void main(String[] args) {
    // TODO Auto-generated method stub
    try {

      RandomAccessFile file;
      file = new RandomAccessFile(
          "C:\\Users\\Supriya\\Documents\\GitHub\\DavisBase\\data\\Indextest1.tbl", "rw");
      IndexFile iFile = new IndexFile(file);
      System.out.println("happy");
//      iFile.addText("ppppppp", 10);
      iFile.addText("ooooo", 11);
      iFile.addText("iiii", 12);
//      iFile.addText("aaaaaaaaaaa", 13);
//      iFile.addText("wwwwwww", 10);
      System.out.print("happy");
    } catch (Exception e) {

    }

  }

}
