package edu.utdallas.davisbase.catalog;

import edu.utdallas.davisbase.DataType;

import static java.lang.String.format;

/**
 * An enumerated column schema for the {@link CatalogTable#DAVISBASE_COLUMNS davisbase_columns}
 * catalog table.
 */
public enum DavisBaseColumnsTableColumn implements CatalogTableColumn {
  ROWID            (DataType.INT,     false, false, false),
  TABLE_NAME       (DataType.TEXT,    false, true, false),
  COLUMN_NAME      (DataType.TEXT,    false, false, false),
  DATA_TYPE        (DataType.TEXT,    false, false, false),
  ORDINAL_POSITION (DataType.TINYINT, false, false, false),
  IS_NULLABLE      (DataType.TEXT,    false, false, false),
  IS_UNIQUE      (DataType.TEXT,false, false, false);

  private final DataType dataType;
  private final boolean isNullable;
  private final boolean isUnique;
  private final boolean isPrimaryKey;

  private DavisBaseColumnsTableColumn(DataType dataType, boolean isNullable, boolean isUnique, boolean isPrimaryKey) {
    assert dataType != null : "dataType should not be null";

    this.dataType = dataType;
    this.isNullable = isNullable;
    this.isUnique = isUnique;
    this.isPrimaryKey = isPrimaryKey;
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

  @Override
  public boolean isPrimaryKey() {
    return isPrimaryKey;
  }

}
