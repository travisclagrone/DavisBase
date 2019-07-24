package edu.utdallas.davisbase.representation;

public class InsertObject implements Comparable<InsertObject>{

  private Integer index;
  private Object object;

  public InsertObject(Integer index, Object object) {
    this.index = index;
    this.object = object;
  }

  public Integer getIndex() {
    return index;
  }

  public Object getObject() {
    return object;
  }

  @Override
  public String toString() {
    return "InsertObject{" +
      "index=" + index +
      ", object=" + object +
      '}';
  }

  @Override
  public int compareTo(InsertObject o) {
    return this.getIndex().compareTo(o.getIndex());
  }
}
