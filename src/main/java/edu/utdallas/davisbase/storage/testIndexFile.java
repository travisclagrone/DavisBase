package edu.utdallas.davisbase.storage;

import java.io.RandomAccessFile;

public class testIndexFile {

  public static void main(String[] args) {
    // TODO Auto-generated method stub
    try {

      RandomAccessFile file;
      file = new RandomAccessFile(
          "C:\\Users\\Supriya\\Documents\\GitHub\\DavisBase\\data\\IndextestInt.tbl", "rw");
      IndexFile iFile = new IndexFile(file);
      System.out.println("happy");

      iFile.addInt(5906, 14);
      iFile.addInt(4596, 14);
      iFile.addInt(4686, 13);
      iFile.addInt(1256, 12);
      iFile.addInt(1234, 11);
      iFile.addInt(1230, 10);
//      iFile.addInt(1234, 11);
//      iFile.addInt(1234, 11);
//      iFile.addInt(1234, 11);
//      iFile.addInt(1234, 11);
//
//      iFile.addText("ppppppp", 10);
//      iFile.addText("iiii", 12);
//      iFile.addText("ooooo", 11);
//      iFile.addText("aaaaaaaaaaa", 13);
//      iFile.addText("wwwwwww", 10);

      System.out.print("happy");
    } catch (Exception e) {

    }

  }

}
