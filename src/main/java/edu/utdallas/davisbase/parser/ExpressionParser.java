package edu.utdallas.davisbase.parser;

import net.sf.jsqlparser.expression.Expression;

import java.util.Arrays;
import java.util.List;

public class ExpressionParser {
  private String column;
  private String operator; //=, in, not
  private List<String> values;


  private static final String[] OPERATOR_VALUES= {"=", "in", "not"};

  public ExpressionParser(){

  }

  public void parseExpression(Expression exp){
    String expressionString = exp.toString();
    String[] words=expressionString.split("=|in|not");//splits the string based on different operator values
    this.column= words[0];
    String[] values=words[1].split(",");
    this.values = Arrays.asList(values);
  }


  public String getColumn() {
    return column;
  }

  public void setColumn(String column) {
    this.column = column;
  }

  public String getOperator() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }


}
