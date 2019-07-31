package edu.utdallas.davisbase.catalog;

import static java.lang.String.format;

import edu.utdallas.davisbase.DataType;

/**
 * An enumerated column schema for the {@link CatalogTable#DAVISBASE_TABLES davisbase_tables}
 * catalog table.
 */
public enum DavisBaseTablesTableColumn implements CatalogTableColumn {
  ROWID      (DataType.INT,  false, true,  false),
  TABLE_NAME (DataType.TEXT, false, false, false);

  private final DataType dataType;
  private final boolean isNullable;
  private final boolean isUnique;
  private final boolean isPrimaryKey;

  private DavisBaseTablesTableColumn(DataType dataType, boolean isNullable, boolean isUnique, boolean isPrimaryKey) {
    assert dataType != null : "dataType should not be null";
    assert !isPrimaryKey || (isNullable && isUnique);

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
