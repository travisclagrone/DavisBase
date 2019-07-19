package edu.utdallas.davisbase.result;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;

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

  private SelectResultData(Path path) {
    this.path = path;
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

    public Builder() throws IOException {
      this.path = createTempFile(null, null);
      this.output = new ObjectOutputStream(new BufferedOutputStream(newOutputStream(this.path)));
    }

    public void writeRow(SelectResultDataRow row) throws IOException {
      checkNotNull(row);

      output.writeObject(row);
    }

    public SelectResultData build() throws IOException {
      output.close();
      return new SelectResultData(path);
    }

  }

}
