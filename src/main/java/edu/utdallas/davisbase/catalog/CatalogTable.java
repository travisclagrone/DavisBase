package edu.utdallas.davisbase.catalog;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableList;
import java.util.List;

/**
 * An enumerated type of {@code CatalogTable}.
 */
public enum CatalogTable {
  DAVISBASE_TABLES  (DavisBaseTablesTableColumn.values()),
  DAVISBASE_COLUMNS (DavisBaseColumnsTableColumn.values());

  private final CatalogTableColumn[] columns;

  private CatalogTable(CatalogTableColumn[] columns) {
    assert columns != null : "columns should not be null";
    assert stream(columns).allMatch(col -> col != null) : "Not all columns are nonnull";

    this.columns = columns;
  }

  /**
   * @return the canonical name of this table (not null)
   */
  public String getName() {
    return name().toLowerCase();
  }

  /**
   * @return an unmodifiable list of the (nonnull) {@link CatalogTableColumn}s that make up this
   *         table's schema, ordered by {@link CatalogTableColumn#getOrdinalPosition()} (not null)
   */
  public List<CatalogTableColumn> getColumns() {
    return unmodifiableList(asList(columns));
  }

}
