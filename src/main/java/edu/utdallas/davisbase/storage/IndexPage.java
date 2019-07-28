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

	public static int splitLeafPage(RandomAccessFile file, int pageNo) {
		SortKeys(file, pageNo);
		int newleafPageNo = AddLeafPage(file);
		boolean rootflag = CheckifRootNode(file, pageNo);
		try {
			if (rootflag) {
				int parentPageNo = AddInteriorPage(file);
				setPageasRoot(file, parentPageNo);
//				setParent(file, pageNo, parentPageNo);
//				insertChild(file, pageNo, parentPageNo);
				return newleafPageNo;
			} else {
				int parentPageNo = getParent(file, pageNo);
//				if (!checkParentspace(file, parentPageNo)) {
//					int newPageNo = splitInteriorPage(file, parentPageNo);
//				}
				return newleafPageNo;
			}

//			setParent(file, newleafPageNo, parentPageNo);
//			insertChild(file, newleafPageNo, parentPageNo, rowId);
////			setRightSibling(file, pageNo, newleafPageNo);
//			setRightMostChild(file, parentPageNo);
		} catch (Exception e) {
		}
		return -1;
	}

	public static void SortKeys(RandomAccessFile file, int currentPageNo) {
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
