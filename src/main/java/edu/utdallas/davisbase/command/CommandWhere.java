package edu.utdallas.davisbase.command;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Objects.hash;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

import org.checkerframework.checker.nullness.qual.NonNull;

import edu.utdallas.davisbase.DataType;

public class CommandWhere {

  public static enum Operator {
    EQUAL,
    NOT_EQUAL,
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL,
    LESS_THAN,
    LESS_THAN_OR_EQUAL;
  }

  private final byte leftColumnIndex;
  private final Operator operator;
  private final @NonNull Object rightLiteralValue;

  public CommandWhere(byte leftColumnIndex, Operator operator, @NonNull Object rightLiteralValue) {
    checkElementIndex(leftColumnIndex, Byte.MAX_VALUE,
        format("leftColumnIndex %d must be in range [0, %d)", leftColumnIndex, Byte.MAX_VALUE));
    checkNotNull(operator, "operator");
    checkNotNull(rightLiteralValue, "rightLiteralValue");
    checkArgument(stream(DataType.values()).map(DataType::getJavaClass).anyMatch(cls -> rightLiteralValue.getClass().equals(cls)),
        format("rightLiteralValue is an instance of %s, which is not one of the types defined by edu.utdallas.davisbase.DataType#getJavaClass()", rightLiteralValue.getClass().getName()));

    this.leftColumnIndex = leftColumnIndex;
    this.operator = operator;
    this.rightLiteralValue = rightLiteralValue;
  }

  /**
   * @return the zero-based index of the column in the source table, where the column is the
   *         referenced column on the left side of this simple {@code WHERE} clause expression (not
   *         negative, and less than {@link java.lang.Byte#MAX_VALUE Byte.MAX_VALUE})
   */
  public byte getLeftColumnIndex() {
    return leftColumnIndex;
  }

  /**
   * @return the single binary relational {@link CommandWhere.Operator Operator} of this compiled
   *         {@code WHERE} clause (not null)
   */
  public Operator getOperator() {
    return operator;
  }

  /**
   * @return the literal value on the right side of this simple {@code WHERE} clause expression (not
   *         null, and an instance of one of the types defined by
   *         {@link edu.utdallas.davisbase.DataType#getJavaClass() DataType.getJavaClass()})
   */
  public @NonNull Object getRightLiteralValue() {
    return rightLiteralValue;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj != null && obj instanceof CommandWhere)) {
      return false;
    }

    CommandWhere other = (CommandWhere) obj;
    return
        getLeftColumnIndex() == other.getLeftColumnIndex() &&
        getOperator().equals(other.getOperator()) &&
        getRightLiteralValue().equals(other.getRightLiteralValue());
  }

  @Override
  public int hashCode() {
    return hash(getLeftColumnIndex(), getOperator(), getRightLiteralValue());
  }

  @Override
  public String toString() {
    return toStringHelper(CommandWhere.class)
        .add("leftColumnIndex", getLeftColumnIndex())
        .add("operator", getOperator())
        .add("rightLiteralValue", getRightLiteralValue())
        .toString();
  }

}
