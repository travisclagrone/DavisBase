package edu.utdallas.davisbase.catalog;

import static java.lang.String.format;

import edu.utdallas.davisbase.DataType;

/**
 * An enumerated column schema for the {@link CatalogTable#DAVISBASE_COLUMNS davisbase_columns}
 * catalog table.
 */
public enum DavisBaseColumnsTableColumn implements CatalogTableColumn {
  ROWID            (DataType.INT,     false),
  TABLE_NAME       (DataType.TEXT,    false),
  COLUMN_NAME      (DataType.TEXT,    false),
  DATA_TYPE        (DataType.TEXT,    false),
  ORDINAL_POSITION (DataType.TINYINT, false),
  IS_NULLABLE      (DataType.TEXT,    false);

  private final DataType dataType;
  private final boolean isNullable;

  private DavisBaseColumnsTableColumn(DataType dataType, boolean isNullable) {
    assert dataType != null : "dataType should not be null";
    assert ordinal() < Byte.MAX_VALUE : format("ordinal() should be less than %d", Byte.MAX_VALUE);

    this.dataType = dataType;
    this.isNullable = isNullable;
  }

  @Override
  public String getName() {
    return name().toLowerCase();
  }

  @Override
  public DataType getDataType() {
    return dataType;
  }

  @Override
  public byte getOrdinalPosition() {
    return (byte) ordinal();
  }

  @Override
  public boolean isNullable() {
    return isNullable;
  }

}
