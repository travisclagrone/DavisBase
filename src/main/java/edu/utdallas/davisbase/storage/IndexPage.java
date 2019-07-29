package edu.utdallas.davisbase.storage;

import java.io.IOException;
import java.io.RandomAccessFile;

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
		long seekParentByte = convertPageNoToFileOffset(pageNo)+ PAGE_OFFSET_OF_CELL_PARENT_PAGE_NO;
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
			if(rootflag) {
				parentPageNo=AddInteriorPage(file);
			}else {
				parentPageNo=getParent(file, pageNo);
			}
			splitInteriorNodeData(file, pageNo, parentPageNo, newSiblingPageNo);
			setParentRightChildPointer(file, parentPageNo, newSiblingPageNo);
		} catch (Exception e) {

		}
	}

	private static void splitInteriorNodeData(RandomAccessFile file, int pageNo, int parentPageNo,
			int newSiblingPageNo) {
		//TODO make sure to update left child pointers
		// TODO Auto-generated method stub
		
		
	}

	private static void setRightSiblingPointer(RandomAccessFile file, int pageNo, int newleafPageNo) {
		// TODO Auto-generated method stub

	}

	private static void setParentRightChildPointer(RandomAccessFile file, int parentPageNo, int newleafPageNo) {
		// TODO Auto-generated method stub
	}

	private static void splitLeafNodeData(RandomAccessFile file, int pageNo, int parentPageNo, int siblingPageNo) {
		try {
			long bigPageOffset = convertPageNoToFileOffset(pageNo);
			file.seek(bigPageOffset + PAGE_OFFSET_OF_CELL_COUNT);
			int noOfrecordsInBigPage = file.readShort();
			int splitIndex = (int) Math.floor(noOfrecordsInBigPage / 2); // this record goes into the parent.
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

	}

	private static void addElementsAboveIToSibling(RandomAccessFile file, int splitIndex, int pageNo,
			int siblingPageNo) {
		// TODO Auto-generated method stub

	}

	private static void addMiddleElementFromChildtoParent(RandomAccessFile file, int splitIndex, int pageNo,
			int parentPageNo) {
		// TODO Auto-generated method stub
		sortKeys(file, parentPageNo);
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
