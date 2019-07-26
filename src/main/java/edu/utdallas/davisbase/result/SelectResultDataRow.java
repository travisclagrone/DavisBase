package edu.utdallas.davisbase.result;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Arrays.asList;
import static java.util.Arrays.copyOf;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.Arrays;
import java.util.Iterator;

import com.google.common.base.MoreObjects.ToStringHelper;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A row of primitive DavisBase values for a {@link SelectResultData} instance.
 */
public class SelectResultDataRow implements Serializable, Iterable<@Nullable Object> {

  private static final long serialVersionUID = -4160693623154109607L;

  private final @Nullable Object @NonNull [] values;

  /**
   * @apiNote This constructor assumes that {@code values} contains only instances of valid
   *          DavisBase data types (or {@code null}). {@link SelectResultDataRow.Builder} does all
   *          the work of actually enforcing this assumption.
   * @implSpec If the runtime type of every element of {@code values} is immutable, then this
   *           {@code SelectResultDataRow} instance MUST be immutable.
   * @implNote The passed array is copied for defensive-programming purposes so as to fulfill the
   *           implementation specification of immutability.
   * @param values the (not null) array whose (nullable) values are to constitute this
   *               {@code SelectResultDataRow}
   * @see SelectResultDataRow.Builder#build()
   */
  private SelectResultDataRow(@Nullable Object @NonNull [] values) {
    this.values = copyOf(values, values.length);
  }

  public int size() {
    return values.length;
  }

  public @Nullable Object get(int index) {
    return values[index];
  }

  public @Nullable Byte getTinyInt(int index) {
    return (Byte) values[index];
  }

  public @Nullable Short getSmallInt(int index) {
    return (Short) values[index];
  }

  public @Nullable Integer getInt(int index) {
    return (Integer) values[index];
  }

  public @Nullable Long getBigInt(int index) {
    return (Long) values[index];
  }

  public @Nullable Float getFloat(int index) {
    return (Float) values[index];
  }

  public @Nullable Double getDouble(int index) {
    return (Double) values[index];
  }

  public @Nullable Year getYear(int index) {
    return (Year) values[index];
  }

  public @Nullable LocalTime getTime(int index) {
    return (LocalTime) values[index];
  }

  public @Nullable LocalDateTime getDateTime(int index) {
    return (LocalDateTime) values[index];
  }

  public @Nullable LocalDate getDate(int index) {
    return (LocalDate) values[index];
  }

  public @Nullable String getText(int index) {
    return (String) values[index];
  }

  public Iterator<@Nullable Object> iterator() {
    return asList(values).iterator();
  }

  @Override
  @SuppressWarnings("nullness")  // Necessary since Arrays.equals(Object[], Object[]) is annotated @NonNull Object @NonNull [] when it can actually take @Nullable Object @NonNull [].
  public boolean equals(Object obj) {
    if (!(obj != null && obj instanceof SelectResultDataRow)) {
      return false;
    }

    SelectResultDataRow other = (SelectResultDataRow) obj;
    return Arrays.equals(values, other.values);
  }

  @Override
  @SuppressWarnings("nullness")  // Necessary since Arrays.hashCode(Object[]) is annotated @NonNull Object @NonNull [] when it can actually take @Nullable Object @NonNull [].
  public int hashCode() {
    return Arrays.hashCode(values);
  }

  @Override
  public String toString() {
    ToStringHelper toStringHelper = toStringHelper(SelectResultDataRow.class);
    for (@Nullable Object value : values) {
      toStringHelper.addValue(value);
    }
    return toStringHelper.toString();
  }

  /**
   * A single-use mutable builder of {@link SelectResultDataRow}.
   * <p>
   * Intended to enforce type safety of {@code SelectResultDataRow} member values. This is necessary
   * because the most derived common base class of the set of Java classes corresponding to the
   * DavisBase {@link edu.utdallas.davisbase.DataType DataType}s is {@code Object}, and so any build
   * process that relies on the invocation of a single method signature (such as a public
   * constructor) is unavoidably relegated to dynamic run-time type checking rather than
   * compile-time type safety. {@code SelectResultDataRow.Builder} achieves compile-time type safety
   * by presenting a complete set of strongly-typed incremental build methods, and <i>no</i> method
   * that accepts arbitrary-typed {@code Object}s.
   *
   * @apiNote A single {@code SelectResultDataRow.Builder} instance can technically build multiple
   *          {@link SelectResultDataRow} instances. However, you really shouldn't. Just create a
   *          new builder for each row that you want to build. Think stateless functional
   *          programming, not stateful procedural programming.
   */
  public static class Builder {

    private final @Nullable Object @NonNull [] values;
    private int index = 0;

    /**
     * @param columns the exact size (in columns) of the {@link SelectResultDataRow}, and so the
     *                exact number of values that must be added to this
     *                {@code SelectResultDataRow.Builder} before invoking {@link #build()} (not
     *                negative)
     */
    public Builder(int columns) {
      this.values = new Object[columns];
    }

    /**
     * Does the actual {@code add} operation.
     * <p>
     * MUST NOT be invoked <i>except</i> from the public strongly-typed {@code add*} methods of
     * {@link Builder}. Adherence to this delegation pattern ensures that every added value is an
     * instance of the Java class correctly corresponding to one of the valid DavisBase
     * {@link edu.utdallas.davisbase.DataType DataType}s.
     *
     * @apiNote If {@code value} is not null, then it is assumed to be a valid DavisBase data type.
     * @implSpec If an invocation would result in an {@link ArrayIndexOutOfBoundsException}, then
     *           all subsequent invocations must be idempotent. In particular, an infinite sequence
     *           of invocations will not cause {@link #index} to overflow repeatedly, but rather
     *           {@code index} must converge to an invalid index (a sink state).
     * @implNote {@link #index} is incremented in a <i>separate</i> statement <i>after</i>
     *           {@code value} is assigned to the next element of {@link #values} in order to ensure
     *           that if {@code index} has reached an invalid index of {@code values}--and thus the
     *           statement {@code values[index] = value;} throws an
     *           {@link ArrayIndexOutOfBoundsException} and so interrupts execution before reaching
     *           the {@code index += 1;} statement--that {@code index} is never again incremented.
     * @param value the value to append to the row next (nullable)
     */
    private void add(@Nullable Object value) {
      values[index] = value;
      index += 1;
    }

    /**
     * @apiNote If {@code value} is not null, then it is assumed to be a valid DavisBase
     *          {@code TinyInt}.
     * @param value a {@code TinyInt} value to append to the row next (nullable)
     */
    public void addTinyInt(@Nullable Byte value) {
      add(value);
    }

    /**
     * @apiNote If {@code value} is not null, then it is assumed to be a valid DavisBase
     *          {@code SmallInt}.
     * @param value a {@code SmallInt} value to append to the row next (nullable)
     */
    public void addSmallInt(@Nullable Short value) {
      add(value);
    }

    /**
     * @apiNote If {@code value} is not null, then it is assumed to be a valid DavisBase {@code Int}.
     * @param value a {@code Int} value to append to the row next (nullable)
     */
    public void addInt(@Nullable Integer value) {
      add(value);
    }

    /**
     * @apiNote If {@code value} is not null, then it is assumed to be a valid DavisBase
     *          {@code BigInt}.
     * @param value a {@code BigInt} value to append to the row next (nullable)
     */
    public void addBigInt(@Nullable Long value) {
      add(value);
    }

    /**
     * @apiNote If {@code value} is not null, then it is assumed to be a valid DavisBase
     *          {@code Float}.
     * @param value a {@code Float} value to append to the row next (nullable)
     */
    public void addFloat(@Nullable Float value) {
      add(value);
    }

    /**
     * @apiNote If {@code value} is not null, then it is assumed to be a valid DavisBase
     *          {@code Double}.
     * @param value a {@code Double} value to append to the row next (nullable)
     */
    public void addDouble(@Nullable Double value) {
      add(value);
    }

    /**
     * @apiNote If {@code value} is not null, then it is assumed to be a valid DavisBase {@code Year}.
     * @param value a {@code Year} value to append to the row next (nullable)
     */
    public void addYear(@Nullable Year value) {
      add(value);
    }

    /**
     * @apiNote If {@code value} is not null, then it is assumed to be a valid DavisBase {@code Time}.
     * @param value a {@code Time} value to append to the row next (nullable)
     */
    public void addTime(@Nullable LocalTime value) {
      add(value);
    }

    /**
     * @apiNote If {@code value} is not null, then it is assumed to be a valid DavisBase
     *          {@code DateTime}.
     * @param value a {@code DateTime} value to append to the row next (nullable)
     */
    public void addDateTime(@Nullable LocalDateTime value) {
      add(value);
    }

    /**
     * @apiNote If {@code value} is not null, then it is assumed to be a valid DavisBase {@code Date}.
     * @param value a {@code Date} value to append to the row next (nullable)
     */
    public void addDate(@Nullable LocalDate value) {
      add(value);
    }

    /**
     * @apiNote If {@code value} is not null, then it is assumed to be a valid DavisBase {@code Text}.
     * @param value a {@code Text} value to append to the row next (nullable)
     */
    public void addText(@Nullable String value) {
      add(value);
    }

    /**
     * @return a {@link SelectResultDataRow} of the specified number of columns containing the added
     *         values in order (not null)
     */
    public SelectResultDataRow build() {
      checkState(index <= values.length,
          "Only %d values have been added, but %d columns were specified",
              index, values.length);

      return new SelectResultDataRow(values);
    }

  }

}
