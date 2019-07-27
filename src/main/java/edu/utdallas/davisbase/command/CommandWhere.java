package edu.utdallas.davisbase.command;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Objects.hash;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.checkerframework.checker.nullness.qual.Nullable;
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

  private final CommandWhereColumn leftColumnReference;
  private final Operator operator;
  private final @Nullable Object rightLiteralValue;

  /**
   * @param leftColumnReference the specification of the column reference on the left side of the
   *                            simple {@code WHERE} clause expression (not null)
   * @param operator            the single binary relational operator
   *                            {@link CommandWhere.Operator Operator} of this compiled
   *                            {@code WHERE} clause (not null)
   * @param rightLiteralValue   the literal value on the right side of this simple {@code WHERE}
   *                            clause expression (either null or an instance of one of the class
   *                            returned by {@link DataType#getJavaClass() DataType.getJavaClass()})
   *                            for one of the {@link DataType}s)
   */
  public CommandWhere(CommandWhereColumn leftColumnReference, Operator operator, @Nullable Object rightLiteralValue) {
    checkNotNull(leftColumnReference, "leftColumnReference");
    checkNotNull(operator, "operator");
    checkArgument(rightLiteralValue == null || stream(DataType.values()).map(DataType::getJavaClass).anyMatch(cls -> rightLiteralValue.getClass().equals(cls)),
        format("rightLiteralValue is an instance of %s, but must be either null or an instance of one of types defined by edu.utdallas.davisbase.DataType#getJavaClass()",
            rightLiteralValue.getClass().getName()));

    this.leftColumnReference = leftColumnReference;
    this.operator = operator;
    this.rightLiteralValue = rightLiteralValue;
  }

  /**
   * @return the specification of the column reference on the left side of the simple {@code WHERE}
   *         clause expression (not null)
   */
  public CommandWhereColumn getLeftColumnReference() {
    return leftColumnReference;
  }

  /**
   * @return the single binary relational {@link CommandWhere.Operator Operator} of this compiled
   *         {@code WHERE} clause (not null)
   */
  public Operator getOperator() {
    return operator;
  }

  /**
   * @return the literal value on the right side of this simple {@code WHERE} clause expression
   *         (either null or an instance of one of the class returned by
   *         {@link DataType#getJavaClass() DataType.getJavaClass()}) for one of the
   *         {@link DataType}s)
   */
  public @Nullable Object getRightLiteralValue() {
    return rightLiteralValue;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj != null && obj instanceof CommandWhere)) {
      return false;
    }

    CommandWhere other = (CommandWhere) obj;
    return
        getLeftColumnReference() == other.getLeftColumnReference() &&
        getOperator().equals(other.getOperator()) &&
        getRightLiteralValue().equals(other.getRightLiteralValue());
  }

  @Override
  public int hashCode() {
    return hash(getLeftColumnReference(), getOperator(), getRightLiteralValue());
  }

  @Override
  public String toString() {
    return toStringHelper(CommandWhere.class)
        .add("leftColumnReference", getLeftColumnReference())
        .add("operator", getOperator())
        .add("rightLiteralValue", getRightLiteralValue())
        .toString();
  }

}
