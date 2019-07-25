package edu.utdallas.davisbase.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import org.checkerframework.checker.nullness.qual.Nullable;

public class StorageState {

	public static class Builder {

		public static File getDefaultDataDirectory() {
//			return new File(System.getProperty("user.dir"));
      return new File("data");
		}

		private @Nullable File dataDirectory = null;

		public Builder() {
		}

		public void setDataDirectory(String dataDirectory) {
			checkNotNull(dataDirectory);
			File file = new File(dataDirectory);

			checkArgument(file.exists(), "Data directory path does not exist");
			checkArgument(file.isDirectory(), "Data directory path is not a directory");

			this.dataDirectory = file;
		}

		public StorageState build() {
			File dataDirectory = getDefaultDataDirectory();
			if (this.dataDirectory != null) {
				dataDirectory = this.dataDirectory;
			}

			return new StorageState(dataDirectory);
		}
	}

	private File dataDirectory;

	private StorageState(File dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	/**
	 * @return the dataDirectory
	 */
	public File getDataDirectory() {
		return dataDirectory;
	}
	
	
}
