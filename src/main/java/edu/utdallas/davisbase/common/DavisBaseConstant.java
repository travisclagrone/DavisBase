package edu.utdallas.davisbase.common;

public interface DavisBaseConstant {

  /*
   * String FILE_EXT = ".tbl"; String DATA_DIR = "data"; String CATALOG_DIR =
   * "catalog"; String SYS_TABLE_NAME = "davisbase_tables"; String SYS_COL_NAME =
   * "davisbase_columns"; int PAGE_SIZE= 512;
   */

  int NULL_SIZE = 0;
  int TINY_INT_SIZE = 1;
  int SMALL_INT_SIZE = 2;
  int INT_SIZE = 4;
  int BIG_INT_SIZE = 8;
  int LONG_SIZE = 8;
  int FLOAT_SIZE = 4;
  int DOUBLE_SIZE = 8;
  int YEAR_SIZE = 1;
  int TIME_SIZE = 4;
  int DATE_TIME_SIZE = 8;
  int DATE_SIZE = 8;
  // int TEXT_SIZE = 0x0C;

  byte NULL_TYPE_CODE = 0x00;
  byte TINY_INT_TYPE_CODE = 0x01;
  byte SMALL_INT_TYPE_CODE = 0x02;
  byte INT_TYPE_CODE = 0x03;
  byte BIG_INT_TYPE_CODE = 0x04;
  byte LONG_TYPE_CODE = 0x04;
  byte FLOAT_TYPE_CODE = 0x05;
  byte DOUBLE_TYPE_CODE = 0x06;
  byte YEAR_TYPE_CODE = 0x08;
  byte TIME_TYPE_CODE = 0x09;
  byte DATE_TIME_TYPE_CODE = 0x0A;
  byte DATE_TYPE_CODE = 0x0B;
  byte TEXT_TYPE_CODE = 0x0C;

}
