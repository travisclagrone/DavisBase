package edu.utdallas.davisbase.catalog;

import edu.utdallas.davisbase.DataType;

/**
 * A column schema for a {@link CatalogTable}.
 */
public interface CatalogTableColumn {

  /**
   * @return the canonical name of this column (not null)
   */
  public String getName();

  /**
   * @return the {@link DataType} of this column (not null)
   */
  public DataType getDataType();

  /**
   * @return the zero-based ordinal position of this column in the table to which it belongs (not
   *         negative, less than {@link java.lang.Byte#MAX_VALUE Byte.MAX_VALUE}, and unique within
   *         a table)
   */
  public byte getOrdinalPosition();

  /**
   * @return {@code true} if-and-only-if this column accepts DavisBase {@code null} values
   */
  public boolean isNullable();

  /**
   * @return {@code true} if-and-only-if this column accepts only unique values
   */
  public boolean isUnique();

  /**
   * @return {@code true} if-and-only-if this column is the primary key
   */
  public boolean isPrimaryKey();
}
