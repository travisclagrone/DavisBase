package edu.utdallas.davisbase.storage;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class IndexPage {

	static final int BYTES_OF_PAGE_OFFSET = Short.BYTES;
	static final int PAGE_OFFSET_OF_CELL_COUNT = 0x01;
	static final int PAGE_OFFSET_OF_RIGHTMOST_PAGE_NO = 0x06;
	static final int PAGE_OFFSET_OF_CELL_PAGE_OFFSET_ARRAY = 0x10;
	static final int PAGE_OFFSET_OF_CELL_CONTENT_START_POINT = 0x03;
	static final int PAGE_OFFSET_OF_CELL_PARENT_PAGE_NO = 0x0A;
	static final int pageSize = StorageConfiguration.Builder.getDefaultPageSize();
	static final int maximumNoOFKeys = 2;
	static final long metaDataRootPageNoOffsetInFile = 0x05;

	private static int AddInteriorPage(RandomAccessFile file) {
		int numofPages = 0;
		try {
			numofPages = (int) (file.length() / pageSize);
			numofPages = numofPages + 1;
			file.setLength(pageSize * numofPages);
			file.seek((numofPages - 1) * pageSize);
			file.writeByte(0x02);// writing page type
			file.seek(((numofPages - 1) * pageSize) + PAGE_OFFSET_OF_RIGHTMOST_PAGE_NO);
			file.writeInt(-1); // setting right most child to -1
		} catch (Exception e) {
			System.out.println(e);
		}
		return numofPages;
	}

	private static int AddLeafPage(RandomAccessFile file) {
		int numofPages = 0;
		try {
			numofPages = (int) (file.length() / pageSize);
			numofPages = numofPages + 1;
			file.setLength(pageSize * numofPages);
			file.seek((numofPages - 1) * pageSize);
			file.writeByte(0x0A);// writing page type

			file.seek(((numofPages - 1) * pageSize) + 6);
			file.writeInt(-1);// setting right most child to -1

		} catch (Exception e) {
			System.out.println(e);
		}
		return numofPages;
	}

	public static void addTableMetaDataPage(RandomAccessFile file) throws IOException {
		try {
			file.setLength(pageSize);
			file.seek(0x00);
			file.writeByte(0);
			int firstPageNo = AddLeafPage(file);
			file.seek(0x05);
			file.writeInt(firstPageNo); // writing root page no.
			setPageasRoot(file, firstPageNo);
		} catch (Exception e) {

		}
	}

	private static void setPageasRoot(RandomAccessFile file, int pageNo) {
		// TODO Auto-generated method stub
		int seekParentByte = (pageNo - 1) * pageSize + 10;
		try {
			file.seek(seekParentByte);
			file.writeInt(-1);// making root
			updateMetaDataRoot(file, pageNo);
		} catch (Exception e) {
		}
	}

	private static void updateMetaDataRoot(RandomAccessFile file, int pageNo) throws IOException {
		// TODO Auto-generated method stub
		file.seek(metaDataRootPageNoOffsetInFile);
		file.writeInt(pageNo);
	}

	// splitting leaf page
	public static int splitLeafPage(RandomAccessFile file, int pageNo) {
		sortKeys(file, pageNo);
		int newSiblingPageNo = AddLeafPage(file);
		boolean rootflag = CheckifRootNode(file, pageNo);
		int parentPageNo;
		try {
			if (rootflag) {
				parentPageNo = AddInteriorPage(file);
				setPageasRoot(file, parentPageNo);
				setParent(file, pageNo, parentPageNo);
			} else {
				parentPageNo = getParent(file, pageNo);
			}
			setParent(file, newSiblingPageNo, parentPageNo);
			splitLeafNodeData(file, pageNo, parentPageNo, newSiblingPageNo);
			setParentRightChildPointer(file, parentPageNo, newSiblingPageNo);
			setRightSiblingPointer(file, pageNo, newSiblingPageNo);
		} catch (Exception e) {
		}
		return -1;
	}

	public static void setParent(RandomAccessFile file, int pageNo, int parentPageNo) {
		long seekParentByte = convertPageNoToFileOffset(pageNo) + PAGE_OFFSET_OF_CELL_PARENT_PAGE_NO;
		try {
			file.seek(seekParentByte);
			file.writeInt(parentPageNo);
		} catch (Exception e) {

		}
	}

	public static void splitInteriorPage(RandomAccessFile file, int pageNo) {
		sortKeys(file, pageNo);
		int newSiblingPageNo = AddInteriorPage(file);
		boolean rootflag = CheckifRootNode(file, pageNo);
		int parentPageNo;
		try {
			if (rootflag) {
				parentPageNo = AddInteriorPage(file);
			} else {
				parentPageNo = getParent(file, pageNo);
			}
			splitInteriorNodeData(file, pageNo, parentPageNo, newSiblingPageNo);
			setParentRightChildPointer(file, parentPageNo, newSiblingPageNo);
		} catch (Exception e) {

		}
	}

	private static void splitInteriorNodeData(RandomAccessFile file, int pageNo, int parentPageNo,
			int newSiblingPageNo) {
		// TODO make sure to update left child pointers
		// TODO Auto-generated method stub

	}

	private static void setRightSiblingPointer(RandomAccessFile file, int pageNo, int siblingPageNo) {
		try {
			long currentPageOffset = convertPageNoToFileOffset(pageNo);
			file.seek(currentPageOffset + PAGE_OFFSET_OF_RIGHTMOST_PAGE_NO);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void setParentRightChildPointer(RandomAccessFile file, int parentPageNo, int newleafPageNo) {
		// TODO Auto-generated method stub
		setRightSiblingPointer(file, parentPageNo, newleafPageNo);
	}

	private static void splitLeafNodeData(RandomAccessFile file, int pageNo, int parentPageNo, int siblingPageNo) {
		try {
			long bigPageOffset = convertPageNoToFileOffset(pageNo);
			file.seek(bigPageOffset + PAGE_OFFSET_OF_CELL_COUNT);
			int noOfrecordsInBigPage = file.readShort();
			int splitIndex = (int) Math.ceil(noOfrecordsInBigPage / 2); // this record goes into the parent.
			// add split index + 1 th element in the array to parent.
			addMiddleElementFromChildtoParent(file, splitIndex, pageNo, parentPageNo);

			// add element above split index+1 from big page to new page.
			addElementsAboveIToSibling(file, splitIndex, pageNo, siblingPageNo);

			// remove elements from split index +1 th element.
			removeElementsAfterIthPosition(file, splitIndex, pageNo);

		} catch (IOException e) {
		}

	}

	private static void removeElementsAfterIthPosition(RandomAccessFile file, int splitIndex, int pageNo) {
		// TODO Auto-generated method stub
		assert splitIndex > 1;
		try {
			long pageOffset = convertPageNoToFileOffset(pageNo);
			file.seek(pageOffset + PAGE_OFFSET_OF_CELL_COUNT);
			short noOfRecords = file.readShort();
			// get the last index
			file.seek(pageOffset + PAGE_OFFSET_OF_CELL_PAGE_OFFSET_ARRAY + 2 * (splitIndex - 2));
			short cellStartPoint2B = file.readShort();

			file.seek(pageOffset + PAGE_OFFSET_OF_CELL_CONTENT_START_POINT);
			file.writeShort(cellStartPoint2B);

			file.seek(pageOffset + PAGE_OFFSET_OF_CELL_COUNT);
			file.writeShort(splitIndex - 1);
			optimizePageSpace(file, pageNo);
			// UpdatePagewithProperData.

		} catch (Exception e) {

		}

	}

	private static void optimizePageSpace(RandomAccessFile file, int pageNo) {
		try {
			long pageOffset = convertPageNoToFileOffset(pageNo);
			file.seek(pageOffset + PAGE_OFFSET_OF_CELL_COUNT);
			int noOfRecords = file.readShort();
			List<byte[]> recordsInPage = new ArrayList();
			short recordOffset;
			byte[] recordBytes;
			int recordRowIdsCount;
			int recordIndexSpace;
			int totalSpaceIn1Rec;
			for (short i = 0; i < noOfRecords; i++) {
				file.seek(pageOffset + PAGE_OFFSET_OF_CELL_PAGE_OFFSET_ARRAY + 2 * i);
				recordOffset = file.readShort();
				file.seek(pageOffset + recordOffset);
				recordRowIdsCount = file.readByte();
				file.seek(pageOffset + recordOffset + 1);
				recordIndexSpace = file.readByte();
				totalSpaceIn1Rec = 1 + 1 + recordIndexSpace + recordRowIdsCount * 4;

				file.seek(pageOffset);
				if (file.readByte() == 0x02) {
					totalSpaceIn1Rec = totalSpaceIn1Rec + 4;// left child space
				}
				recordBytes = new byte[totalSpaceIn1Rec];

				file.seek(pageOffset + recordOffset);
				file.read(recordBytes);
				recordsInPage.add(recordBytes);
			}

			int lastdataEntryPointOffset = pageSize;
			for (int i = 0; i < recordsInPage.size(); i++) {
				totalSpaceIn1Rec = recordsInPage.get(i).length;
				lastdataEntryPointOffset = lastdataEntryPointOffset - totalSpaceIn1Rec;
				file.seek(pageOffset + lastdataEntryPointOffset);
				file.write(recordsInPage.get(i));

				file.seek(pageOffset + PAGE_OFFSET_OF_CELL_CONTENT_START_POINT);
				file.writeShort(lastdataEntryPointOffset);

				file.seek(pageOffset + PAGE_OFFSET_OF_CELL_PAGE_OFFSET_ARRAY + (2 * i));
				file.writeShort(lastdataEntryPointOffset);
			}

		} catch (Exception e) {

		}

	}

	private static void addElementsAboveIToSibling(RandomAccessFile file, int splitIndex, int pageNo,
			int siblingPageNo) {
		// TODO Auto-generated method stub
		try {
			long pageOffset = convertPageNoToFileOffset(pageNo);
			file.seek(pageOffset + PAGE_OFFSET_OF_CELL_COUNT);
			short noOfRecords = file.readShort();

			List<byte[]> records2Add = new ArrayList<byte[]>();
			byte[] recordBytes;
			short seekPointOffset = 0;
			int noOfRowIds;
			int indexSpace;
			int totalRecordSpace;
//			int seekAddBytes = 0;
			int leftChildPageNoSpace = 0;
			file.seek(pageOffset);
			if (file.readByte() == 0x02) {
				leftChildPageNoSpace = 4; // excluding left childPageNo filed
			}

			for (int i = splitIndex; i < noOfRecords - 1; i++) {
				file.seek(pageOffset + PAGE_OFFSET_OF_CELL_PAGE_OFFSET_ARRAY + (2 * splitIndex));
				seekPointOffset = (short) (file.readShort());

				file.seek(pageOffset + seekPointOffset + leftChildPageNoSpace);
				noOfRowIds = file.readByte();
				file.seek(pageOffset + seekPointOffset + 1 + leftChildPageNoSpace);
				indexSpace = file.readByte();
				totalRecordSpace = leftChildPageNoSpace + 1 + 1 + indexSpace + (4 * noOfRowIds);
				recordBytes = new byte[totalRecordSpace];

				file.seek(pageOffset + seekPointOffset);
				file.read(recordBytes);
				records2Add.add(recordBytes);
			}

			long siblingPageOffset = convertPageNoToFileOffset(siblingPageNo);
			file.seek(siblingPageOffset + PAGE_OFFSET_OF_CELL_COUNT);
			short noOfcellsInSibling = file.readShort();
			file.seek(siblingPageOffset + PAGE_OFFSET_OF_CELL_CONTENT_START_POINT);
			short lastDataEntryPointOffsetSibling = file.readShort();
			if (lastDataEntryPointOffsetSibling == 0) {
				lastDataEntryPointOffsetSibling = (short) pageSize;
			}
			int totalDatatoWrite;
			
			for (int i = 0; i < records2Add.size(); i++) {
				totalDatatoWrite = records2Add.get(i).length;
				file.seek(siblingPageOffset + lastDataEntryPointOffsetSibling - totalDatatoWrite);
				file.write(records2Add.get(i));
				
				lastDataEntryPointOffsetSibling=(short) (lastDataEntryPointOffsetSibling-totalDatatoWrite);
				file.seek(siblingPageOffset+PAGE_OFFSET_OF_CELL_COUNT);
				noOfcellsInSibling=(short) (noOfcellsInSibling+1);
				file.writeShort(noOfcellsInSibling);
				file.seek(siblingPageOffset+PAGE_OFFSET_OF_CELL_CONTENT_START_POINT);
				file.writeShort(lastDataEntryPointOffsetSibling);
				file.seek(siblingPageOffset+PAGE_OFFSET_OF_CELL_PAGE_OFFSET_ARRAY+(2*(noOfcellsInSibling-1)));
				file.writeShort(lastDataEntryPointOffsetSibling);
			}

		} catch (Exception e) {

		}

	}

	private static void addMiddleElementFromChildtoParent(RandomAccessFile file, int splitIndex, int pageNo,
			int parentPageNo) {
		// TODO Auto-generated method stub
		try {
			long pageOffset = convertPageNoToFileOffset(pageNo);
			file.seek(pageOffset + PAGE_OFFSET_OF_CELL_PAGE_OFFSET_ARRAY + (2 * (splitIndex - 1)));
			short middleElementOffset = file.readShort();
			long dataSeekPoint = pageOffset + middleElementOffset;

			file.seek(pageOffset);
			if (file.readByte() == 0x02) {
				dataSeekPoint = dataSeekPoint + 4; // because interior page has left child info
			}

			file.seek(dataSeekPoint);
			int noOfRowIds = file.readByte();

			file.seek(pageOffset + middleElementOffset + 1);
			int indexDataSpace = file.readByte();

			int totalRowSpace = 1 + 1 + indexDataSpace + (4 * noOfRowIds);
			byte[] data2BCopied = new byte[totalRowSpace];
			file.seek(dataSeekPoint);
			file.read(data2BCopied);

			// writing into parent page
			long parentPageOffset = convertPageNoToFileOffset(parentPageNo);
			file.seek(parentPageOffset + PAGE_OFFSET_OF_CELL_CONTENT_START_POINT);
			short currentDataStartOffset = file.readShort();
			long dataEntrySeekPoint = parentPageOffset + currentDataStartOffset - data2BCopied.length - 4;
			file.seek(dataEntrySeekPoint);
			file.writeInt(pageNo);// writing left child page no
			file.write(data2BCopied);
			sortKeys(file, parentPageNo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void sortKeys(RandomAccessFile file, int currentPageNo) {
		// TODO Auto-generated method stub
	}

	private static int getParent(RandomAccessFile file, int pageNo) {
		// TODO Auto-generated method stub
		long seekParentByte = convertPageNoToFileOffset(pageNo) + PAGE_OFFSET_OF_CELL_PARENT_PAGE_NO;
		try {
			file.seek(seekParentByte);
			return file.readInt();
		} catch (Exception e) {

		}
		return -1;
	}

	private static boolean CheckifRootNode(RandomAccessFile file, int pageNo) {
		// TODO Auto-generated method stub
		long seekParentByte = convertPageNoToFileOffset(pageNo) + PAGE_OFFSET_OF_CELL_PARENT_PAGE_NO;
		try {
			file.seek(seekParentByte);
			if (file.readInt() == -1) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	public static long convertPageNoToFileOffset(int pageNo) {
		assert 1 <= pageNo && pageNo <= Integer.MAX_VALUE;

		final long fileOffset = (pageNo - 1) * (long) pageSize;
		return fileOffset;
	}

}
