package edu.utdallas.davisbase.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.checkerframework.checker.nullness.qual.Nullable;

public class StorageConfiguration {

  public static class Builder {

    public static String getDefaultDataDirectoryName() {
      return "data";
    }

    public static String getDefaultTableFileExtension() {
      return "tbl";
    }

    public static String getDefaultIndexFileExtension() {
      return "ndx";
    }

    public static String getDefaultCatalogTablesTableName() {
      return "davisbase_tables";
    }

    public static String getDefaultCatalogColumnsTableName() {
      return "davisbase_columns";
    }

    public static int getDefaultPageSize() {
      return 512;
    }

    public static int getMinimumPageSize() {
      return 512;
    }

    private @Nullable String dataDirectoryName = null;
    private @Nullable String tableFileExtension = null;
    private @Nullable String indexFileExtension = null;
    private @Nullable String catalogTablesTableName = null;
    private @Nullable String catalogColumnsTableName = null;
    private @Nullable Integer pageSize = null;

    public Builder() {}

    public void setDataDirectoryName(String dataDirectoryName) {
      checkNotNull(dataDirectoryName);
      this.dataDirectoryName = dataDirectoryName;
    }

    public void setTableFileExtension(String tableFileExtension) {
      checkNotNull(tableFileExtension);
      this.tableFileExtension = tableFileExtension;
    }

    public void setIndexFileExtension(String indexFileExtension) {
      checkNotNull(indexFileExtension);
      this.indexFileExtension = indexFileExtension;
    }

    public void setCatalogTablesTableName(String catalogTablesTableName) {
      checkNotNull(catalogTablesTableName);
      this.catalogTablesTableName = catalogTablesTableName;
    }

    public void setCatalogColumnsTableName(String catalogColumnsTableName) {
      checkNotNull(catalogColumnsTableName);
      this.catalogColumnsTableName = catalogColumnsTableName;
    }

    public void setPageSize(int pageSize) {
      checkArgument(getMinimumPageSize() <= pageSize,
          String.format("Page size must be at least %d (bytes)",
              getMinimumPageSize()));

      this.pageSize = pageSize;
    }

    public StorageConfiguration build() {
      String dataDirectoryName = getDefaultDataDirectoryName();
      if (this.dataDirectoryName != null) {
        dataDirectoryName = this.dataDirectoryName;
      }

      String tableFileExtension = getDefaultTableFileExtension();
      if (this.tableFileExtension != null) {
        tableFileExtension = this.tableFileExtension;
      }

      String indexFileExtension = getDefaultIndexFileExtension();
      if (this.indexFileExtension != null) {
        indexFileExtension = this.indexFileExtension;
      }

      String catalogTablesTableName = getDefaultCatalogTablesTableName();
      if (this.catalogTablesTableName != null) {
        catalogTablesTableName = this.catalogTablesTableName;
      }

      String catalogColumnsTableName = getDefaultCatalogColumnsTableName();
      if (this.catalogColumnsTableName != null) {
        catalogColumnsTableName = this.catalogColumnsTableName;
      }

      return new StorageConfiguration(
          dataDirectoryName,
          tableFileExtension,
          indexFileExtension,
          catalogTablesTableName,
          catalogColumnsTableName,
          pageSize);
    }
  }

  private final String dataDirectoryName;
  private final String tableFileExtension;
  private final String indexFileExtension;
  private final String catalogTablesTableName;
  private final String catalogColumnsTableName;
  private final int pageSize;

  private StorageConfiguration(
      String dataDirectoryName,
      String tableFileExtension,
      String indexFileExtension,
      String catalogTablesTableName,
      String catalogColumnsTableName,
      int pageSize
  ) {
    this.dataDirectoryName = dataDirectoryName;
    this.tableFileExtension = tableFileExtension;
    this.indexFileExtension = indexFileExtension;
    this.catalogTablesTableName = catalogTablesTableName;
    this.catalogColumnsTableName = catalogColumnsTableName;
    this.pageSize = pageSize;
  }

  /**
   * @return the dataDirectoryName
   */
  public String getDataDirectoryName() {
    return dataDirectoryName;
  }

  /**
   * @return the catalogColumnsTableName
   */
  public String getCatalogColumnsTableName() {
    return catalogColumnsTableName;
  }

  /**
   * @return the catalogTablesTableName
   */
  public String getCatalogTablesTableName() {
    return catalogTablesTableName;
  }

  /**
   * @return the indexFileExtension
   */
  public String getIndexFileExtension() {
    return indexFileExtension;
  }

  /**
   * @return the tableFileExtension
   */
  public String getTableFileExtension() {
    return tableFileExtension;
  }

  /**
   * @return the pageSize
   */
  public int getPageSize() {
    return pageSize;
  }
}
