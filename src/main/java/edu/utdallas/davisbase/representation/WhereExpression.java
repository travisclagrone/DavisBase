package edu.utdallas.davisbase.representation;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.schema.Column;

public class WhereExpression {

  public static enum Operator {
    EQUALSTO, NOTEQUALTO, GREATERTHAN, GREATERTHANEQUALS, LESSTHAN, LESSTHANEQUALS;
  }

  private final String expression;
  private final boolean not;
  private final Column column;
  private final Operator operator;
  private final Expression value;

  public WhereExpression(String expression, boolean not, Column column, Operator operator, Expression value) {
    this.expression= expression;
    this.not = not;
    this.column = column;
    this.operator = operator;
    this.value=value;
  }

  public String getExpression() {
    return expression;
  }

  public boolean isNot() {
    return not;
  }

  public Column getColumn() {
    return column;
  }

  public Operator getOperator() {
    return operator;
  }

  public Expression getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "WhereExpression{" +
      "expression='" + expression + '\'' +
      ", not=" + not +
      ", column='" + column + '\'' +
      ", operator='" + operator + '\'' +
      ", value='" + value + '\'' +
      '}';
  }
}
