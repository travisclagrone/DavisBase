package edu.utdallas.davisbase.catalog;

import edu.utdallas.davisbase.DataType;

import static java.lang.String.format;

/**
 * An enumerated column schema for the {@link CatalogTable#DAVISBASE_COLUMNS davisbase_columns}
 * catalog table.
 */
public enum DavisBaseColumnsTableColumn implements CatalogTableColumn {
  ROWID            (DataType.INT,     false, false),
  TABLE_NAME       (DataType.TEXT,    false, true),
  COLUMN_NAME      (DataType.TEXT,    false, false),
  DATA_TYPE        (DataType.TEXT,    false, false),
  ORDINAL_POSITION (DataType.TINYINT, false, false),
  IS_NULLABLE      (DataType.TEXT,    false, false),
  IS_UNIQUE      (DataType.TEXT,false, false);

  private final DataType dataType;
  private final boolean isNullable;
  private final boolean isUnique;

  private DavisBaseColumnsTableColumn(DataType dataType, boolean isNullable, boolean isUnique) {
    assert dataType != null : "dataType should not be null";

    this.dataType = dataType;
    this.isNullable = isNullable;
    this.isUnique = isUnique;
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
    assert ordinal() < Byte.MAX_VALUE : format("ordinal() should be less than %d", Byte.MAX_VALUE);

    return (byte) ordinal();
  }

  @Override
  public boolean isNullable() {
    return isNullable;
  }

  @Override
  public boolean isUnique() {
    return isUnique;
  }


}
