package edu.utdallas.davisbase.storage;

import static java.lang.String.format;

enum TablePageType {
  INTERIOR (0x05),
  LEAF     (0x0D);

  private final byte code;

  private TablePageType(int code) {
    this.code = (byte) code;
  }

  public byte toCode() {
    return code;
  }

  public static TablePageType fromCode(byte code) {
    for (TablePageType tablePageType : TablePageType.values()) {
      if (code == tablePageType.code) {
        return tablePageType;
      }
    }
    throw new IllegalArgumentException(format("Unrecognized TablePageType code: %x", code));
  }

}
