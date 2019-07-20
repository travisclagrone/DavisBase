package edu.utdallas.davisbase.representation;

import net.sf.jsqlparser.expression.Expression;

public class WhereExpression {
  String expression;
  private boolean not;
  private String column;
  private String operator;
  private String value;

  public WhereExpression(String expression, boolean not, Expression column, String operator, Expression value) {
    this.expression= expression;
    this.not = not;
    this.column = column.toString();
    this.operator = operator;
    this.value = value.toString();
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
