package edu.utdallas.davisbase.storage;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
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

	public byte[] toBytes() {
		throw new NotImplementedException();
	}
}
