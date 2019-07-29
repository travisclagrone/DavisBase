package edu.utdallas.davisbase.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.List;

import edu.utdallas.davisbase.BooleanUtils;
import edu.utdallas.davisbase.catalog.CatalogTable;
import edu.utdallas.davisbase.catalog.CatalogTableColumn;

public class Storage {

  private final StorageConfiguration configuration;
  private final StorageState state;

  @SuppressWarnings("initialization")
  public Storage(StorageConfiguration configuration, StorageState state) {
    checkNotNull(configuration, "configuration");
    checkNotNull(state, "state");

    this.configuration = configuration;
    this.state = state;

    initDavisBase();
  }

  public void createTableFile(String tableName) throws IOException {

    String path = state.getDataDirectory().getPath() + "/" + tableName + "."
        + this.configuration.getTableFileExtension();

    File file = new File(path);

    if (!file.exists()) {
      RandomAccessFile table = new RandomAccessFile(state.getDataDirectory().getPath() + "/"
          + tableName + "." + this.configuration.getTableFileExtension(), "rw");
      // Page.addTableMetaDataPage(table);
      table.close();
    }
  }

  public TableFile openTableFile(String tableName) throws IOException {
    checkNotNull(tableName);

    final File tableFileHandle = getTableFileHandle(tableName);
    checkArgument(tableFileHandle.exists(),
        format("File '%s' for table '%s' does not exist.",
            tableFileHandle.toString(),
            tableName));
    checkArgument(!tableFileHandle.isDirectory(),
        format("File '%s' for table '%s' is a directory, but should be a file.",
            tableFileHandle.toString(),
            tableName));

    final RandomAccessFile randomAccessFile = new RandomAccessFile(tableFileHandle, "rw");
    final long length = randomAccessFile.length();
    checkState(length % configuration.getPageSize() == 0,
        format("File length %d is not a multiple of page size %d.",
            length,
            configuration.getPageSize()));

    return new TableFile(randomAccessFile);
  }

  public void deleteTableFile(String tableName) throws IOException {
    checkNotNull(tableName, "tableName");

    final File tableFileHandle = getTableFileHandle(tableName);
    checkArgument(tableFileHandle.exists(),
        format("File '%s' for table '%s' does not exist.",
            tableFileHandle.toString(),
            tableName));
    checkArgument(!tableFileHandle.isDirectory(),
        format("File '%s' for table '%s' is a directory, but should be a file.",
            tableFileHandle.toString(),
            tableName));

    // Use java.nio.file.Files#delete(Path) instead of java.io.File#delete() because the former
    // throws a descriptive exception on failure whereas the latter doesn't.
    Files.delete(tableFileHandle.toPath());
  }

  private File getTableFileHandle(String tableName) throws IOException {
    assert tableName != null : "tableName should not be null";

    final String tableFileName = tableName + "." + configuration.getTableFileExtension();
    final File tableFileHandle = new File(state.getDataDirectory(), tableFileName);

    return tableFileHandle;
  }

  public void initDavisBase() {
    try {
      File dataDir = state.getDataDirectory();
      if (!dataDir.exists()) {
        dataDir.mkdir();
      }

      String[] currentTableList = dataDir.list();
      boolean existSysTable = false;
      boolean existSysColumn = false;
      for (int i = 0; i < currentTableList.length; i++) {
        if (currentTableList[i].equals("davisbase_tables.tbl"))
          existSysTable = true;
        if (currentTableList[i].equals("davisbase_columns.tbl"))
          existSysColumn = true;
      }

      if (!existSysTable) {
        initSysTable();
      }

      if (!existSysColumn) {
        initSysColumn();
      }

    } catch (SecurityException e) {
      System.out.println(e);
    }

  }

  private void initRootPage(String tablePath) {

    try {

      RandomAccessFile sysTable = new RandomAccessFile(tablePath, "rw");
      sysTable.setLength(this.configuration.getPageSize());
      sysTable.seek(0);
      sysTable.writeByte(-1);
      sysTable.writeInt(1);
      sysTable.writeInt(2);
      sysTable.writeInt(-1);
      sysTable.close();
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  private void initSysTable() {

    try {
      String davisTableName = this.configuration.getCatalogTablesTableName();
      String davisColumnName = this.configuration.getCatalogColumnsTableName();
      String sysTablePath =
          state.getDataDirectory().getPath() + "/" + this.configuration.getCatalogTablesTableName()
              + "." + this.configuration.getTableFileExtension();

      RandomAccessFile sysTable = new RandomAccessFile(sysTablePath, "rw");

      TableRowBuilder sysTableRow = new TableRowBuilder();
      sysTableRow.appendText(davisTableName);
      TableRowBuilder sysColumnRow = new TableRowBuilder();
      sysColumnRow.appendText(davisColumnName);

      TableFile sysTableFile = new TableFile(sysTable);

      sysTableFile.appendRow(sysTableRow);
      sysTableFile.appendRow(sysColumnRow);

      sysTable.close();
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  private void initSysColumn() {

    try {

      String davisTableName = this.configuration.getCatalogTablesTableName();
      String davisColumnName = this.configuration.getCatalogColumnsTableName();
      String sysColumnPath =
          state.getDataDirectory().getPath() + "/" + this.configuration.getCatalogColumnsTableName()
              + "." + this.configuration.getTableFileExtension();

      RandomAccessFile sysTable = new RandomAccessFile(sysColumnPath, "rw");

      CatalogTable davisTable = CatalogTable.DAVISBASE_TABLES;
      CatalogTable davisColumn = CatalogTable.DAVISBASE_COLUMNS;

      TableFile sysColumnFile = new TableFile(sysTable);

      List<CatalogTableColumn> davisTableColumnList = davisTable.getColumns();



      for (CatalogTableColumn col : davisTableColumnList) {
        TableRowBuilder row = new TableRowBuilder();
        row.appendText(davisTableName);
        row.appendText(col.getName());
        row.appendText(col.getDataType().name());
        row.appendTinyInt(col.getOrdinalPosition());
        row.appendText(BooleanUtils.toText(col.isNullable()));
        sysColumnFile.appendRow(row);
      }

      List<CatalogTableColumn> davisColumnColumnList = davisColumn.getColumns();
      for (CatalogTableColumn col : davisColumnColumnList) {
        TableRowBuilder row = new TableRowBuilder();
        row.appendText(davisColumnName);
        row.appendText(col.getName());
        row.appendText(col.getDataType().name());
        row.appendTinyInt(col.getOrdinalPosition());
        row.appendText(BooleanUtils.toText(col.isNullable()));
        sysColumnFile.appendRow(row);
      }

      sysTable.close();

    } catch (Exception e) {
      System.out.println(e);
    }
  }
}
