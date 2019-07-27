package edu.utdallas.davisbase.storage;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import edu.utdallas.davisbase.NotImplementedException;

public class TableRowBuilder {

	private final List<Object> values = new ArrayList<>();

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


	private byte[] intToByteArray(final int i) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeInt(i);
		dos.flush();
		return bos.toByteArray();
	}

	public byte[] toBytes() {
		Object data;
		int finalByteArraySize = 0;
		byte[] tempByteArray;
		List<byte[]> bytesArraysList = new ArrayList<>();
		for (int i = 0; i < this.getNoOfValues(); i++) {
			data = this.getValueAt(i);
//			byte[] ByteBuffer.allocate(4).putInt((int) data).array();
			switch (data.getClass().getSimpleName()) {
			case "Integer":
				tempByteArray = new byte[4];
				tempByteArray = ByteBuffer.allocate(4).putInt((int) data).array();
				break;
			case "String":
				tempByteArray = data.toString().getBytes();
				break;
			case "Byte":
				tempByteArray = new byte[1];
				tempByteArray = ByteBuffer.allocate(tempByteArray.length).put((byte) data).array();
				break;
			case "Short":
				tempByteArray = new byte[2];
				tempByteArray = ByteBuffer.allocate(tempByteArray.length).putShort((short) data).array();
				break;
			case "Long":
				tempByteArray = new byte[8];
				tempByteArray = ByteBuffer.allocate(tempByteArray.length).putLong((long) data).array();
				break;
			case "Float":
				tempByteArray = new byte[4];
				tempByteArray = ByteBuffer.allocate(tempByteArray.length).putFloat((float) data).array();
				break;
			case "Double":
				tempByteArray = new byte[8];
				tempByteArray = ByteBuffer.allocate(tempByteArray.length).putDouble((double) data).array();
				break;
			case "Year":
				int yearInt = ((Year) data).getValue();
				tempByteArray = new byte[1];
				tempByteArray = ByteBuffer.allocate(tempByteArray.length).put((byte) yearInt).array();
				break;
			case "LocalTime":
				int timeInt = ((LocalTime) data).toSecondOfDay();
				tempByteArray = new byte[4];
				tempByteArray = ByteBuffer.allocate(4).putInt((int) timeInt).array();
				break;
			case "LocalDateTime":
				long dateTimeInt = ((LocalDateTime) data).toEpochSecond(ZoneOffset.UTC);
				tempByteArray = new byte[8];
				tempByteArray = ByteBuffer.allocate(tempByteArray.length).putLong((long) dateTimeInt).array();
				break;
			case "LocalDate":
				long dateInt = ((LocalDate) data).toEpochDay();
				tempByteArray = new byte[8];
				tempByteArray = ByteBuffer.allocate(tempByteArray.length).putLong((long) dateInt).array();
				break;
			case "":
				tempByteArray = new byte[0];
				break;
			default:
				tempByteArray = new byte[0];
				break;
			}
			if (!(tempByteArray.length == 0))
				bytesArraysList.add(tempByteArray);
			finalByteArraySize = finalByteArraySize + tempByteArray.length;
		}

		byte[] finalByteArray = new byte[finalByteArraySize];
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
