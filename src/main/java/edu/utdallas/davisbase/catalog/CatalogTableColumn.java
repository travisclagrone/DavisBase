package edu.utdallas.davisbase.catalog;

import edu.utdallas.davisbase.DataType;

/**
 * A column schema for a catalog table.
 */
public interface CatalogTableColumn {

  public String getName();

  public DataType getDataType();

  public byte getOrdinalPosition();

  public boolean isNullable();

}
