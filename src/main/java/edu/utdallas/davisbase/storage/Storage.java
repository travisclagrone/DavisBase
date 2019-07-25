package edu.utdallas.davisbase.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import edu.utdallas.davisbase.NotImplementedException;
import edu.utdallas.davisbase.common.DavisBaseConstant;

public class Storage {

	private final StorageConfiguration configuration;
	private final StorageState state;

	@SuppressWarnings("initialization")
	public Storage(StorageConfiguration configuration, StorageState state) {
		this.configuration = configuration;
		this.state = state;
		initDavisBase();
	}

	public void createTableFile(String tableName) throws IOException {

		String path = state.getDataDirectory().getPath() + "/" + tableName + "."
				+ this.configuration.getTableFileExtension();

		File file = new File(path);

		if (!file.exists()) {
			RandomAccessFile table = new RandomAccessFile(state.getDataDirectory().getPath() + "/" + tableName + "."
					+ this.configuration.getTableFileExtension(), "rw");
			table.close();
		}
	}

	public TableFile openTableFile(String tableName) throws IOException {
		checkNotNull(tableName);

		final String tableFileName = tableName + configuration.getTableFileExtension();
		final File tableFileHandle = new File(state.getDataDirectory(), tableFileName);

		checkArgument(tableFileHandle.exists(),
				String.format("File \"%s\" for table \"%s\" does not exist.", tableFileHandle.toString(), tableName));
		checkArgument(tableFileHandle.isDirectory(), String.format(
				"File \"%s\" for table \"%s\" is actually a directory.", tableFileHandle.toString(), tableName));

		final RandomAccessFile randomAccessFile = new RandomAccessFile(tableFileHandle, "rw");
		final long length = randomAccessFile.length();
		checkState(length % configuration.getPageSize() == 0, String
				.format("File length %d is not a multiple of page size %d.", length, configuration.getPageSize()));

		return new TableFile(randomAccessFile);
	}

	public void initDavisBase() {
		try {
			File dataDir = state.getDataDirectory();
			if (!dataDir.exists()) {
				dataDir.mkdir();
			}

			String[] currentTableList = dataDir.list();
			boolean existSysTable = false;
			boolean existSysColumn = false;
			for (int i = 0; i < currentTableList.length; i++) {
				if (currentTableList[i].equals("davisbase_tables.tbl"))
					existSysTable = true;
				if (currentTableList[i].equals("davisbase_columns.tbl"))
					existSysColumn = true;
			}

			if (!existSysTable) {
				initSysTable();
			}

			if (!existSysColumn) {
				initSysColumn();
			}

		} catch (SecurityException e) {
			System.out.println(e);
		}

	}

	private void initSysTable() {

		try {

			RandomAccessFile sysTable = new RandomAccessFile(state.getDataDirectory().getPath() + "/"
					+ this.configuration.getCatalogTablesTableName() + "." + this.configuration.getTableFileExtension(),
					"rw");
			sysTable.setLength(this.configuration.getPageSize());
			sysTable.seek(0);
			sysTable.write(0x0D);
			sysTable.writeByte(0x02);

			int size1 = 24;
			int size2 = 25;

			int offsetSysTable = this.configuration.getPageSize() - size1;
			int offsetSysColumn = offsetSysTable - size2;

			sysTable.writeShort(offsetSysColumn);
			sysTable.writeInt(0);
			sysTable.writeInt(0);
			sysTable.writeShort(offsetSysTable);
			sysTable.writeShort(offsetSysColumn);

			sysTable.seek(offsetSysTable);
			sysTable.writeShort(20);
			sysTable.writeInt(1);
			sysTable.writeByte(1);
			sysTable.writeByte(28);
			sysTable.writeBytes("davisbase_tables");

			sysTable.seek(offsetSysColumn);
			sysTable.writeShort(21);
			sysTable.writeInt(2);
			sysTable.writeByte(1);
			sysTable.writeByte(29);
			sysTable.writeBytes("davisbase_columns");

			sysTable.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void initSysColumn() {

		try {
			RandomAccessFile sysColumn = new RandomAccessFile(
					state.getDataDirectory().getPath() + "/" + this.configuration.getCatalogColumnsTableName() + "."
							+ this.configuration.getTableFileExtension(),
					"rw");

			sysColumn.setLength(this.configuration.getPageSize());
			sysColumn.seek(0);
			sysColumn.writeByte(0x0D);
			sysColumn.writeByte(0x08);

			int[] offset = new int[10];
			offset[0] = this.configuration.getPageSize() - 43;
			offset[1] = offset[0] - 47;
			offset[2] = offset[1] - 44;
			offset[3] = offset[2] - 48;
			offset[4] = offset[3] - 49;
			offset[5] = offset[4] - 47;
			offset[6] = offset[5] - 57;
			offset[7] = offset[6] - 49;

			sysColumn.writeShort(offset[7]);
			sysColumn.writeInt(0);
			sysColumn.writeInt(0);

			for (int i = 0; i < 8; i++)
				sysColumn.writeShort(offset[i]);

			sysColumn.seek(offset[0]);
			sysColumn.writeShort(33);
			sysColumn.writeInt(1);
			sysColumn.writeByte(5);
			sysColumn.writeByte(28);
			sysColumn.writeByte(17);
			sysColumn.writeByte(15);
			sysColumn.writeByte(4);
			sysColumn.writeByte(14);
			sysColumn.writeBytes("davisbase_tables");
			sysColumn.writeBytes("rowid");
			sysColumn.writeBytes("INT");
			sysColumn.writeByte(1);
			sysColumn.writeBytes("NO");

			sysColumn.seek(offset[1]);
			sysColumn.writeShort(39);
			sysColumn.writeInt(2);
			sysColumn.writeByte(5);
			sysColumn.writeByte(28);
			sysColumn.writeByte(22);
			sysColumn.writeByte(16);
			sysColumn.writeByte(4);
			sysColumn.writeByte(14);
			sysColumn.writeBytes("davisbase_tables");
			sysColumn.writeBytes("table_name");
			sysColumn.writeBytes("TEXT");
			sysColumn.writeByte(2);
			sysColumn.writeBytes("NO");

			sysColumn.seek(offset[2]);
			sysColumn.writeShort(34);
			sysColumn.writeInt(3);
			sysColumn.writeByte(5);
			sysColumn.writeByte(29);
			sysColumn.writeByte(17);
			sysColumn.writeByte(15);
			sysColumn.writeByte(4);
			sysColumn.writeByte(14);
			sysColumn.writeBytes("davisbase_columns");
			sysColumn.writeBytes("rowid");
			sysColumn.writeBytes("INT");
			sysColumn.writeByte(1);
			sysColumn.writeBytes("NO");

			sysColumn.seek(offset[3]);
			sysColumn.writeShort(40);
			sysColumn.writeInt(4);
			sysColumn.writeByte(5);
			sysColumn.writeByte(29);
			sysColumn.writeByte(22);
			sysColumn.writeByte(16);
			sysColumn.writeByte(4);
			sysColumn.writeByte(14);
			sysColumn.writeBytes("davisbase_columns");
			sysColumn.writeBytes("table_name");
			sysColumn.writeBytes("TEXT");
			sysColumn.writeByte(2);
			sysColumn.writeBytes("NO");

			sysColumn.seek(offset[4]);
			sysColumn.writeShort(41);
			sysColumn.writeInt(5);
			sysColumn.writeByte(5);
			sysColumn.writeByte(29);
			sysColumn.writeByte(23);
			sysColumn.writeByte(16);
			sysColumn.writeByte(4);
			sysColumn.writeByte(14);
			sysColumn.writeBytes("davisbase_columns");
			sysColumn.writeBytes("column_name");
			sysColumn.writeBytes("TEXT");
			sysColumn.writeByte(3);
			sysColumn.writeBytes("NO");

			sysColumn.seek(offset[5]);
			sysColumn.writeShort(39);
			sysColumn.writeInt(6);
			sysColumn.writeByte(5);
			sysColumn.writeByte(29);
			sysColumn.writeByte(21);
			sysColumn.writeByte(16);
			sysColumn.writeByte(4);
			sysColumn.writeByte(14);
			sysColumn.writeBytes("davisbase_columns");
			sysColumn.writeBytes("data_type");
			sysColumn.writeBytes("TEXT");
			sysColumn.writeByte(4);
			sysColumn.writeBytes("NO");

			sysColumn.seek(offset[6]);
			sysColumn.writeShort(49);
			sysColumn.writeInt(7);
			sysColumn.writeByte(5);
			sysColumn.writeByte(29);
			sysColumn.writeByte(28);
			sysColumn.writeByte(19);
			sysColumn.writeByte(4);
			sysColumn.writeByte(14);
			sysColumn.writeBytes("davisbase_columns");
			sysColumn.writeBytes("ordinal_position");
			sysColumn.writeBytes("TINYINT");
			sysColumn.writeByte(5);
			sysColumn.writeBytes("NO");

			sysColumn.seek(offset[7]);
			sysColumn.writeShort(41);
			sysColumn.writeInt(8);
			sysColumn.writeByte(5);
			sysColumn.writeByte(29);
			sysColumn.writeByte(23);
			sysColumn.writeByte(16);
			sysColumn.writeByte(4);
			sysColumn.writeByte(14);
			sysColumn.writeBytes("davisbase_columns");
			sysColumn.writeBytes("is_nullable");
			sysColumn.writeBytes("TEXT");
			sysColumn.writeByte(6);
			sysColumn.writeBytes("NO");

			sysColumn.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
