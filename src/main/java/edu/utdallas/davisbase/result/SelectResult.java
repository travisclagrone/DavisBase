package edu.utdallas.davisbase.result;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.hash;

public class SelectResult implements Result {

  private final SelectResultSchema schema;
  private final SelectResultData data;

  /**
   * @param schema the column-wise schema of this result (not null)
   * @param data   the row-wise data of this result (not null)
   */
  public SelectResult(SelectResultSchema schema, SelectResultData data) {
    checkNotNull(schema);
    checkNotNull(data);

    this.schema = schema;
    this.data = data;
  }

  /**
   * @return the {@link SelectResultSchema} of this {@code SelectResult}
   */
  public SelectResultSchema getSchema() {
    return schema;
  }

  /**
   * @return the {@link SelectResultData} of this {@code SelectResult}
   */
  public SelectResultData getData() {
    return data;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj != null && obj instanceof SelectResult)) {
      return false;
    }

    SelectResult other = (SelectResult) obj;
    return
        schema.equals(other.schema) &&
        data.equals(other.data);
  }

  @Override
  public int hashCode() {
    return hash(schema, data);
  }

  @Override
  public String toString() {
    return toStringHelper(SelectResult.class)
        .add("schema", getSchema())
        .add("data", getData())
        .toString();
  }

}
