package edu.utdallas.davisbase.parser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExpressionParser {
  private String column="";
  private String operator=""; //=, in, not
  private List<String> values=new ArrayList<>();
  private boolean not = false;

  private static final String[] OPERATOR_VALUES= {"=", "in", "not"};

  public void parseWhereExpression(Expression where){
    if (where instanceof InExpression){
      InExpression in = (InExpression) where;
      System.out.println(in.getLeftExpression());
      System.out.println(in.getLeftItemsList());
      System.out.println(in.getRightItemsList());
      System.out.print(in.isNot());
      this.not= in.isNot();
      this.column=in.getLeftExpression().toString();
//      this.values=buildValuesList(in.getRightItemsList()); TODO: translate itemslist

    }
    else if(where instanceof Between) {
      Between between = (Between) where;
      System.out.println(between.getBetweenExpressionStart());
      System.out.println(between.getBetweenExpressionStart());
      System.out.println(between.getLeftExpression());
      System.out.println(between.isNot());
      this.not=between.isNot();
      this.column=between.getLeftExpression().toString();
   //TODO: build between

    }
    else if(where instanceof NotEqualsTo){ //TODO: Represent not in operator
      NotEqualsTo not = (NotEqualsTo)where;
      System.out.println(not.getLeftExpression());
      System.out.println(not.getRightExpression());
      System.out.println(not.getStringExpression());
      System.out.println(not.isNot());
      this.not=not.isNot();
      this.operator=not.getStringExpression();
      this.column=not.getLeftExpression().toString();
      this.values=buildValuesList(not.getRightExpression());
    }
    else if(where instanceof EqualsTo){
      EqualsTo equals = (EqualsTo) where;
      System.out.println(equals.getStringExpression());
      System.out.println(equals.getLeftExpression());
      System.out.println(equals.getRightExpression());
      this.operator=equals.getStringExpression();
      this.column=equals.getLeftExpression().toString();
      this.values=buildValuesList(equals.getRightExpression());
    }
    else{
      System.out.println("Did not find match");
    }

  }

  private List<String> buildValuesList(Expression exp){
    String[] values=exp.toString().split(",");
    return Arrays.asList(values);
  }

}
