package edu.utdallas.davisbase.storage;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

import edu.utdallas.davisbase.NotImplementedException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * A DavisBase "Table" file.
 *
 * A {@link TableFile} object is the lowest-level <i>structured</i> interface to
 * a DavisBase "Table" file. It is intended to wrap a live (albeit closeable)
 * file connection, and thereby abstract access and manipulation of the
 * underlying binary file as if it were <b>a collection of records</b>.
 *
 * A {@link TableFile} object functions similarly to a mutable forward-only
 * cursor.
 *
 * @apiNote Unlike a conceptual "table", a {@link TableFile} object does not
 *          have a schema. This is because the {@link TableFile} class is
 *          intended to abstract only the binary structures and algorithms (e.g.
 *          paging, record de-/serialization, b-tree balancing, etc.), and be
 *          used by other higher-level classes to effect a schematic table and
 *          complex SQL operations. As such, <b>schematically correct reading
 *          and writing of records is the responsibility of the code using a
 *          {@link TableFile} object</b>.
 */
public class TableFile implements Closeable {

	protected final RandomAccessFile file;

	public TableFile(RandomAccessFile file) {
		checkNotNull(file);
		checkArgument(file.getChannel().isOpen());

		this.file = file;
	}

	@Override
	public void close() throws IOException {
		file.close();
	}

	public void appendRow(TableRowBuilder tableRowBuilder) throws IOException {
		int rowId = getnextRowId(file);
		int noOfColumns = tableRowBuilder.getNoOfValues();
		int[] columnSizeArray = new int[noOfColumns + 1];
		int payLoad = 4;// since rowId is not a part of table row builder
		columnSizeArray[0] = payLoad;
		Object data;
		for (int i = 1; i < columnSizeArray.length; i++) {
			data = tableRowBuilder.getValueAt(i - 1);
			switch (data.getClass().getSimpleName()) {
			case "Integer":
				columnSizeArray[i] = 4;
				break;
			case "String": {
			}
				columnSizeArray[i] = data.toString().length();
				break;
			case "Byte":
				columnSizeArray[i] = 1;
				break;
			case "Short":
				columnSizeArray[i] = 2;
				break;
			case "Long":
				columnSizeArray[i] = 8;
				break;
			case "Float":
				columnSizeArray[i] = 4;
				break;
			case "Double":
				columnSizeArray[i] = 8;
			case "Year":
				columnSizeArray[i] = 1;
				break;
			case "LocalTime":
				columnSizeArray[i] = 4;
				break;
			case "LocalDateTime":
				columnSizeArray[i] = 8;
				break;
			case "LocalDate":
				columnSizeArray[i] = 8;
				break;
			case "":
				columnSizeArray[i] = 0;
				break;

			default:
				columnSizeArray[i] = 0;
				break;

			}
			payLoad = (short) (payLoad + columnSizeArray[i]);
		}

		int totalSpaceRequired;
		int currentPageNo;

		currentPageNo = (int) (this.file.length() / Page.pageSize);
		long pageOffset = (currentPageNo - 1) * Page.pageSize;
		file.seek(pageOffset);
		byte pageType = file.readByte();

		totalSpaceRequired = (1 + columnSizeArray.length + payLoad);
		boolean overflowFlag = checkPagesize(totalSpaceRequired, currentPageNo);
		if (overflowFlag) {
			if (pageType == 0x05) {
				currentPageNo = Page.splitInteriorPage(file, currentPageNo);
			} else if (pageType == 0x0D) {
				currentPageNo = Page.splitLeafPage(file, currentPageNo);
			}

		}

		pageOffset = (currentPageNo - 1) * Page.pageSize;
		file.seek(pageOffset);
		pageType = file.readByte();
		long seekOffset = pageOffset + 3;
		file.seek(seekOffset);
		short cellOffset = file.readShort();
		if (cellOffset == 0) {
			cellOffset = (short) (Page.pageSize);
		}

		long dataEntryPoint = cellOffset - totalSpaceRequired;

		file.seek(dataEntryPoint);
		file.writeByte(noOfColumns);
		for (int i = 0; i < columnSizeArray.length; i++) {
			file.writeByte(columnSizeArray[i]);
		}
		file.writeInt(rowId);
		file.write(tableRowBuilder.toBytes());

	}

	private boolean checkPagesize(int sizeRequired, int currentPageNo) {
		try {
			this.file.seek((currentPageNo - 1) * Page.pageSize + 1);
			short noOfRecords = this.file.readShort();

			this.file.seek((currentPageNo - 1) * Page.pageSize + 3);
			short startofCellConcent = file.readShort();
			short arryLastEntry = (short) (16 + (noOfRecords * 2));
			if ((startofCellConcent - arryLastEntry - 1) > sizeRequired) {
				return true;
			}
		} catch (Exception e) {

		}
		return false;
	}

	public boolean goToNextRow() throws IOException {
		// TODO Implement TableFile.goToNextRow()
		throw new NotImplementedException();
	}

	public boolean goToRow(int rowId) throws IOException {
		// TODO Implement TableFile.goToRow(int)
		throw new NotImplementedException();
	}

	public @Nullable Byte readTinyInt(int columnIndex) throws IOException {
		// TODO Implement TableFile.readTinyInt(int)
		throw new NotImplementedException();
	}

	public @Nullable Short readSmallInt(int columnIndex) throws IOException {
		// TODO Implement TableFile.readSmallInt(int)
		throw new NotImplementedException();
	}

	public @Nullable Integer readInt(int columnIndex) throws IOException {
		// TODO Implement TableFile.readInt(int)
		throw new NotImplementedException();
	}

	public @Nullable Long readBigInt(int columnIndex) throws IOException {
		// TODO Implement TableFile.readBigInt(int)
		throw new NotImplementedException();
	}

	public @Nullable Float readFloat(int columnIndex) throws IOException {
		// TODO Implement TableFile.readFloat(int)
		throw new NotImplementedException();
	}

	public @Nullable Double readDouble(int columnIndex) throws IOException {
		// TODO Implement TableFile.readDouble(int)
		throw new NotImplementedException();
	}

	public @Nullable Year readYear(int columnIndex) throws IOException {
		// TODO Implement TableFile.readYear(int)
		throw new NotImplementedException();
	}

	public @Nullable LocalTime readTime(int columnIndex) throws IOException {
		// TODO Implement TableFile.readTime(int)
		throw new NotImplementedException();
	}

	public @Nullable LocalDateTime readDateTime(int columnIndex) throws IOException {
		// TODO Implement TableFile.readDateTime(int)
		throw new NotImplementedException();
	}

	public @Nullable LocalDate readDate(int columnIndex) throws IOException {
		// TODO Implement TableFile.readDate(int)
		throw new NotImplementedException();
	}

	public @Nullable String readText(int columnIndex) throws IOException {
		// TODO Implement TableFile.readText(int)
		throw new NotImplementedException();
	}

	public void removeRow() throws IOException {
		// TODO Implement TableFile.removeRow()
		throw new NotImplementedException();
	}

	public void writeNull(int columnIndex) throws IOException {
		// TODO Implement TableFile.writeNull(int)
		throw new NotImplementedException();
	}

	public void writeTinyInt(int columnIndex, byte value) throws IOException {
		// TODO Implement TableFile.writeTinyInt(int, byte)
		throw new NotImplementedException();
	}

	public void writeSmallInt(int columnIndex, short value) throws IOException {
		// TODO Implement TableFile.writeSmallInt(int, short)
		throw new NotImplementedException();
	}

	public void writeInt(int columnIndex, int value) throws IOException {
		// TODO Implement TableFile.writeInt(int, int)
		throw new NotImplementedException();
	}

	public void writeBigInt(int columnIndex, long value) throws IOException {
		// TODO Implement TableFile.writeBigInt(int, long)
		throw new NotImplementedException();
	}

	public void writeFloat(int columnIndex, float value) throws IOException {
		// TODO Implement TableFile.writeFloat(int, float)
		throw new NotImplementedException();
	}

	public void writeDouble(int columnIndex, double value) throws IOException {
		// TODO Implement TableFile.writeDouble(int, double)
		throw new NotImplementedException();
	}

	public void writeYear(int columnIndex, Year value) throws IOException {
		checkNotNull(value);

		// TODO Implement TableFile.writeYear(int, Year)
		throw new NotImplementedException();
	}

	public void writeTime(int columnIndex, LocalTime value) throws IOException {
		checkNotNull(value);

		// TODO Implement TableFile.writeTime(int, LocalTime)
		throw new NotImplementedException();
	}

	public void writeDateTime(int columnIndex, LocalDateTime value) throws IOException {
		checkNotNull(value);

		// TODO Implement TableFile.writeDateTime(int, value)
		throw new NotImplementedException();
	}

	public void writeDate(int columnIndex, LocalDate value) throws IOException {
		checkNotNull(value);

		// TODO Implement TableFile.writeDate(int, LocalDate)
		throw new NotImplementedException();
	}

	public void writeText(int columnIndex, String value) throws IOException {
		checkNotNull(value);

		// TODO Implement TableFile.writeText(int, String)
		throw new NotImplementedException();
	}

	public int getnextRowId(RandomAccessFile file) throws IOException {
//		checkNotNull(pageNo);
		try {
			file.seek(0x01);
			return file.readInt() + 1;

		} catch (Exception e) {
		}
		// TODO Implement TableFile.writeText(int, String)
		return -1;
	}

}
