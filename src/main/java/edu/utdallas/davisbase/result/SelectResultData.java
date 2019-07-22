package edu.utdallas.davisbase.result;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.util.Objects.hash;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * Row-oriented data for a {@link SelectResult}.
 */
public class SelectResultData implements Iterable<SelectResultDataRow> {

  private final Path path;
  private final int size;

  private SelectResultData(Path path, int size) {
    this.path = path;
    this.size = size;
  }

  /**
   * @return the number of rows in this data
   */
  public int size() {
    return size;
  }

  public Iterator<SelectResultDataRow> iterator() {
    try {
      return new Iterator<SelectResultDataRow>() {

        private final ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(newInputStream(path)));

        @Override
        public boolean hasNext() {
          try {
            boolean hasNext = input.available() > 0;
            if (!hasNext) {
              input.close();
            }
            return hasNext;
          }
          catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        }

        @Override
        public SelectResultDataRow next() {
          try {
            return (SelectResultDataRow) input.readObject();
          }
          catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
          }
          catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        }

      };
    }
    catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj != null && obj instanceof SelectResultData)) {
      return false;
    }

    SelectResultData other = (SelectResultData) obj;
    return
        path.equals(other.path) &&
        size == other.size;
  }

  @Override
  public int hashCode() {
    return hash(path, size);
  }

  @Override
  public String toString() {
    return toStringHelper(SelectResultData.class)
        .add("path", path)
        .add("size", size)
        .toString();
  }

  /**
   * A mutable builder of exactly one {@link SelectResultData} instance.
   *
   * Intended to enable <i>incremental</i> building of a {@code SelectResultData} instance so that:
   * <ol>
   *   <li>The set of constituent {@link SelectResultDataRow}s--which may be large--need not</i> be
   *       wholly present in memory at the moment a {@code SelectResultData} is constructed.</li>
   *   <li>The execution of a {@link edu.utdallas.davisbase.command.SelectCommand SelectCommand}
   *       may proceed in a simple online fashion without the extraneous responsibility of managing
   *       the result set size.</li>
   * </ol>
   * Taken together, these features not only overcome runtime environment memory size limitations,
   * but also simplify the command execution logic by correctly separating the responsibility of
   * generating a result set through a {@code SelectCommand} from the responsibility of scalably
   * managing the internal data structure of the result set.
   */
  public static class Builder {

    private final Path path;
    private final ObjectOutputStream output;
    private int rowCount;

    @SuppressWarnings("nullness")  // Necessary since createTempFile(String, String) is annotated @NotNull String but explicitly _does_ accept null.
    public Builder() throws IOException {
      this.path = createTempFile(null, null);
      this.output = new ObjectOutputStream(new BufferedOutputStream(newOutputStream(this.path)));
      this.rowCount = 0;
    }

    public void writeRow(SelectResultDataRow row) throws IOException {
      checkNotNull(row);
      checkArgument(rowCount < Integer.MAX_VALUE, "Cannot write more than %d rows", Integer.MAX_VALUE);

      output.writeObject(row);
      rowCount += 1;
    }

    public SelectResultData build() throws IOException {
      output.close();
      return new SelectResultData(path, rowCount);
    }

  }

}
