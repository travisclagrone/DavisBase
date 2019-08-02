package edu.utdallas.davisbase.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static edu.utdallas.davisbase.RowIdUtils.ROWID_MAX_VALUE;
import static edu.utdallas.davisbase.RowIdUtils.ROWID_MIN_VALUE;
import static java.lang.String.format;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TableRowBuilder {

	private final LinkedList<Object> values = new LinkedList<>();

	public int getNoOfValues() {
		return this.values.size();
	}

	public Object getValueAt(int index) {
		return this.values.get(index);
	}

	public TableRowBuilder() {

	}

	public void appendNull() {
		this.values.add("");
	}

	public void appendTinyInt(byte value) {
		this.values.add(value);
	}

	public void appendSmallInt(short value) {
		this.values.add(value);
	}

	public void appendInt(int value) {
		this.values.add(value);
	}

	public void appendBigInt(long value) {
		this.values.add(value);
	}

	public void appendFloat(float value) {
		this.values.add(value);
	}

	public void appendDouble(double value) {
		this.values.add(value);
	}

	public void appendYear(Year value) {
		checkNotNull(value);
		this.values.add(value);
	}

	public void appendTime(LocalTime value) {
		checkNotNull(value);
		this.values.add(value);
	}

	public void appendDateTime(LocalDateTime value) {
		checkNotNull(value);
		this.values.add(value);
	}

	public void appendDate(LocalDate value) {
		checkNotNull(value);
		this.values.add(value);
	}

	public void appendText(String value) {
		checkNotNull(value);
		this.values.add(value);
	}

  void prependRowId(int rowId) {
    checkArgument(ROWID_MIN_VALUE <= rowId && rowId <= ROWID_MAX_VALUE);
    this.values.addFirst(rowId);
  }

	public byte[] toBytes() {
    List<byte[]> bytesArraysList = new ArrayList<>();
    int finalByteArraySize = 0;
		for (int i = 0; i < this.getNoOfValues(); i++) {
      final Object value = this.getValueAt(i);

      final byte[] tempByteArray;
      if (value.equals("")) {  // An empty string denotes null.
        tempByteArray = new byte[0];
      }
      else if (value instanceof Byte) {
        tempByteArray = ByteBuffer.allocate(1).put((byte) value).array();
      }
      else if (value instanceof Short) {
        tempByteArray = ByteBuffer.allocate(2).putShort((short) value).array();
      }
      else if (value instanceof Integer) {
        tempByteArray = ByteBuffer.allocate(4).putInt((int) value).array();
      }
      else if (value instanceof Long) {
        tempByteArray = ByteBuffer.allocate(8).putLong((long) value).array();
      }
      else if (value instanceof Float) {
        tempByteArray = ByteBuffer.allocate(4).putFloat((float) value).array();
      }
      else if (value instanceof Double) {
        tempByteArray = ByteBuffer.allocate(8).putDouble((double) value).array();
      }
      else if (value instanceof Year) {
        final int yearInt = ((Year) value).getValue() - 2000;
        tempByteArray = ByteBuffer.allocate(1).put((byte) yearInt).array();
      }
      else if (value instanceof LocalTime) {
        final int timeInt = ((LocalTime) value).toSecondOfDay();
        tempByteArray = ByteBuffer.allocate(4).putInt((int) timeInt).array();
      }
      else if (value instanceof LocalDateTime) {
        final long dateTimeInt = ((LocalDateTime) value).toEpochSecond(ZoneOffset.UTC);
        tempByteArray = ByteBuffer.allocate(8).putLong((long) dateTimeInt).array();
      }
      else if (value instanceof LocalDate) {
        final long dateInt = ((LocalDate) value).toEpochDay();
        tempByteArray = ByteBuffer.allocate(8).putLong((long) dateInt).array();
      }
      else if (value instanceof String) {
        tempByteArray = value.toString().getBytes();
      }
      else {
        throw new IllegalStateException(format("Value index %d of TableRowBuilder instance is of class %s.", i, value.getClass().getName()));
      }

			if (tempByteArray.length > 0) {
        bytesArraysList.add(tempByteArray);
      }
			finalByteArraySize = finalByteArraySize + tempByteArray.length;
		}

		final byte[] finalByteArray = new byte[finalByteArraySize];
		int k = 0;
		for (int j = 0; j < bytesArraysList.size(); j++) {
			for (int i = 0; i < bytesArraysList.get(j).length; i++) {
				finalByteArray[k] = bytesArraysList.get(j)[i];
				k++;
			}
		}

		return finalByteArray;
  }

}
