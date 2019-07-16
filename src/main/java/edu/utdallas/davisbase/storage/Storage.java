package edu.utdallas.davisbase.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import edu.utdallas.davisbase.NotImplementedException;

public class Storage {

  private final StorageConfiguration configuration;
  private final StorageState state;

  public Storage(StorageConfiguration configuration, StorageState state) {
    this.configuration = configuration;
    this.state = state;
  }

  public void createTableFile(String tableName) throws IOException {
    // TODO Implement Storage.createTableFile(String)
    throw new NotImplementedException();
  }

  public TableFile openTableFile(String tableName, short rootPageId) throws IOException {
    checkNotNull(tableName);
    checkElementIndex(rootPageId, Short.MAX_VALUE);

    final String tableFileName = tableName + configuration.getTableFileExtension();
    final File tableFileHandle = new File(state.getDataDirectory(), tableFileName);

    checkArgument(tableFileHandle.exists(),
        String.format("File \"%s\" for table \"%s\" does not exist.",
            tableFileHandle.toString(),
            tableName));
    checkArgument(tableFileHandle.isDirectory(),
        String.format("File \"%s\" for table \"%s\" is actually a directory.",
            tableFileHandle.toString(),
            tableName));

    final RandomAccessFile randomAccessFile = new RandomAccessFile(tableFileHandle, "rw");
    final long length = randomAccessFile.length();
    checkState(length % configuration.getPageSize() == 0,
        String.format("File length %d is not a multiple of page size %d.",
            length,
            configuration.getPageSize()));
    checkState(rootPageId < (length / configuration.getPageSize()),
        String.format("Root page id %d is out of file length %d given page size %d.",
            rootPageId,
            length,
            configuration.getPageSize()));

    return new TableFile(randomAccessFile, rootPageId);
  }

}
