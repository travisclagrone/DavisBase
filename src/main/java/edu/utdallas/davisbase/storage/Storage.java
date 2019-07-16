package edu.utdallas.davisbase.storage;

public class Storage {

  private final StorageConfiguration configuration;
  private final StorageState state;

  public Storage(StorageConfiguration configuration, StorageState state) {
    this.configuration = configuration;
    this.state = state;
  }
}
