package edu.utdallas.davisbase.compiler;

import org.checkerframework.checker.nullness.qual.Nullable;

class InsertObject implements Comparable<InsertObject> {

  private int index;
  private @Nullable Object object;

  public InsertObject(int index, @Nullable Object object) {
    this.index = index;
    this.object = object;
  }

  public int getIndex() {
    return index;
  }

  public @Nullable Object getObject() {
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
    return Integer.compare(this.getIndex(), o.getIndex());
  }
}
