package edu.utdallas.davisbase.result;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.google.common.base.MoreObjects.ToStringHelper;

import edu.utdallas.davisbase.DataType;

/**
 * A column-wise schema for a {@link SelectResult}.
 */
public class SelectResultSchema implements Iterable<SelectResultSchemaColumn> {

  private final List<SelectResultSchemaColumn> columns;

  /**
   * @param columns the order-significant collection of (nonnull) columns that comprise this
   *                {@link SelectResultSchema} (not null)
   */
  public SelectResultSchema(Collection<SelectResultSchemaColumn> columns) {
    checkNotNull(columns);
    for (SelectResultSchemaColumn column : columns) {
      checkNotNull(column);
    }

    // Copy the collection and then wrap it in an unmodifiable view to ensure immutability.
    this.columns = unmodifiableList(new ArrayList<>(columns));
  }

  /**
   * @return the count of {@link SelectResultSchemaColumn}s in this {@link SelectResultSchema} (not
   *         negative)
   * @see java.util.List#size()
   */
  public int size() {
    return columns.size();
  }

  /**
   * @return {@code true} if this {@link SelectResultSchema} has zero
   *         {@link SelectResultSchemaColumn}s
   * @see java.util.List#isEmpty()
   */
  public boolean isEmpty() {
    return columns.isEmpty();
  }

  /**
   * @param index the zero-based index of the {@link SelectResultSchemaColumn} to return (not
   *              negative)
   * @return the {@link SelectResultSchemaColumn} at the specified position in this
   *         {@link SelectResultSchema} (not null)
   * @see java.util.List#get(int)
   */
  public SelectResultSchemaColumn getColumn(int index) {
    return columns.get(index);
  }

  /**
   * Returns the name of the column at the specified position in this schema. Convenience method
   * equivalent to {@code schema.getColumn(index).getName()}.
   *
   * @param index the zero-based index of the {@link SelectResultSchemaColumn} whose name to return
   *              (not negative)
   * @return the name of the {@link SelectResultSchemaColumn} at the specified position in this
   *         {@link SelectResultSchema} (not null)
   */
  public String getColumnName(int index) {
    return columns.get(index).getName();
  }

  /**
   * Returns the data type of the column at the specified position in this schema. Convenience
   * method equivalent to {@code schema.getColumn(index).getDataType()}.
   *
   * @param index the zero-based index of the {@link SelectResultSchemaColumn} whose
   *              {@link DataType} to return (not negative)
   * @return the {@link DataType} of the {@link SelectResultSchemaColumn} at the specified position
   *         in this {@link SelectResultSchema} (not null)
   */
  public DataType getColumnDataType(int index) {
    return columns.get(index).getDataType();
  }

  /**
   * @return an iterator over the (nonnull) {@link SelectResultSchemaColumn}s in this
   *         {@link SelectResultSchema} in proper sequence (not null)
   * @see java.util.List#iterator()
   */
  public Iterator<SelectResultSchemaColumn> iterator() {
    return columns.iterator();
  }

  /**
   * @return a sequential {@link Stream} over the (nonnull) {@link SelectResultSchemaColumn}s in
   *         this {@link SelectResultSchema} (not null)
   * @see java.util.Collection#stream()
   */
  public Stream<SelectResultSchemaColumn> stream() {
    return columns.stream();
  }

  /**
   * @param action the action to perform on each {@link SelectResultSchemaColumn} (not null)
   * @see java.lang.Iterable#forEach(java.util.function.Consumer)
   */
  public void forEach(Consumer<? super SelectResultSchemaColumn> action) {
    columns.forEach(action);
  }

  /**
   * @param obj the object to be compared for equality with this {@link SelectResultSchema}
   * @return {@code true} if the specified object is value-equal to this {@link SelectResultSchema}
   * @see java.util.List#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    return columns.equals(obj);
  }

  /**
   * @return the hash code value for this {@link SelectResultSchema}
   * @see java.util.List#hashCode()
   */
  public int hashCode() {
    return columns.hashCode();
  }

  /**
   * @return a deep string representation of this {@link SelectResultSchema} (not null)
   */
  @Override
  public String toString() {
    ToStringHelper toStringHelper = toStringHelper(SelectResultSchema.class);
    for (SelectResultSchemaColumn column : columns) {
      toStringHelper.addValue(column);
    }
    return toStringHelper.toString();
  }

}
