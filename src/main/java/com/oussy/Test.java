package com.oussy;

import edu.utdallas.davisbase.storage.Storage;
import edu.utdallas.davisbase.storage.StorageConfiguration;
import edu.utdallas.davisbase.storage.StorageConfiguration.Builder;
import edu.utdallas.davisbase.storage.StorageState;

public class Test {

	public Test() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {

		// storageConfiguration
		Builder builder = new StorageConfiguration.Builder();
		StorageConfiguration storageConfiguration = builder.build();

		
		edu.utdallas.davisbase.storage.StorageState.Builder storageStateBuilder = new  StorageState.Builder();
		StorageState storageState = storageStateBuilder.build();
		//storageState.initDB();
		
		Storage store = new Storage(storageConfiguration, storageState);
		
		/*
		 * System.out.println("CatalogColumnsTableName ::: " +
		 * storageConfiguration.getCatalogColumnsTableName());
		 * System.out.println("CatalogTablesTableName ::: " +
		 * storageConfiguration.getCatalogTablesTableName());
		 * System.out.println("DataDirectoryName::: " +
		 * storageConfiguration.getDataDirectoryName());
		 * System.out.println("IndexFileExtension ::: " +
		 * storageConfiguration.getIndexFileExtension());
		 * System.out.println("PageSize ::: " + storageConfiguration.getPageSize());
		 * System.out.println("TableFileExtension ::: " +
		 * storageConfiguration.getTableFileExtension());
		 * 
		 * System.out.println(System.getenv("user.dir"));
		 */
	}

}
