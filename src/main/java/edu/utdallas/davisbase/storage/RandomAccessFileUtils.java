package edu.utdallas.davisbase.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Static method utilities for working with {@link RandomAccessFile}s.
 */
class RandomAccessFileUtils {

  private RandomAccessFileUtils() {
    throw new IllegalStateException(
        format("%s may not be instantiated.",
            RandomAccessFileUtils.class.getName()));
  }

  /**
   * @see RandomAccessFile#skipBytes(int)
   * @see com.google.common.io.ByteStreams#skipFully(java.io.InputStream, long)
   */
  public static void skipFully(RandomAccessFile file, int n) throws IOException {
    checkArgument(n >= 0, "Number of bytes to skipFully must be nonnegative.");

    long available;
    while (n > 0) {
      available = file.length() - file.getFilePointer();
      if (available <= 0) {
        throw new EOFException();
      }
      n -= file.skipBytes(n);
    }
  }

}
