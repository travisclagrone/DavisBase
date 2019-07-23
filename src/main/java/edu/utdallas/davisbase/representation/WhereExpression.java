package edu.utdallas.davisbase.representation;

import net.sf.jsqlparser.expression.*;

public class WhereExpression {
  public enum Operator
  {
    EQUALSTO, NOTEQUALTO, GREATERTHAN, GREATERTHANEQUALS, LESSTHAN, LESSTHANEQUALS;
  }

  private final String expression;
  private final boolean not;
  private final String column;
  private final Operator operator;
  private final Expression value;

  public WhereExpression(String expression, boolean not, Expression column, Operator operator, Expression value) {
    this.expression= expression;
    this.not = not;
    this.column = column.toString();
    this.operator = operator;
    this.value=value;
  }

  public String getExpression() {
    return expression;
  }

  public boolean isNot() {
    return not;
  }

  public String getColumn() {
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
