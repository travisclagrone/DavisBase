package edu.utdallas.davisbase.storage;

import java.io.RandomAccessFile;

public class Page {

	static final int pageSize = StorageConfiguration.Builder.getDefaultPageSize();
	static final int thresholdInteriorCellSize = 10;

	// called when the interior node is overflowed
	public static int AddInteriorPage(RandomAccessFile file) {
		int numofPages = 0;
		try {
			numofPages = (int) (file.length() / pageSize);
			numofPages = numofPages + 1;
			file.setLength(pageSize * numofPages);
			file.seek((numofPages - 1) * pageSize);
			file.writeByte(0x05);
		} catch (Exception e) {
			System.out.println(e);
		}
		return numofPages;
	}

	// called when the leaf id overflowed.
	public static int AddLeafPage(RandomAccessFile file) {
		int numofPages = 0;
		try {
			numofPages = (int) (file.length() / pageSize);
			numofPages = numofPages + 1;
			file.setLength(pageSize * numofPages);
			file.seek((numofPages - 1) * pageSize);
			file.writeByte(0x0D);
		} catch (Exception e) {
			System.out.println(e);
		}
		return numofPages;
	}

	// if no space in leaf node
	public static int splitLeafPage(RandomAccessFile file, int pageNo) {

		boolean rootflag = CheckifRootNode(file, pageNo);
		try {
			if (rootflag) {
				int parentPageNo = AddInteriorPage(file);
				setPageasRoot(file, parentPageNo);
				setParent(file, pageNo, parentPageNo);
				insertChild(file, pageNo, parentPageNo);
				int newleafPageNo = AddLeafPage(file);
				setParent(file, newleafPageNo, parentPageNo);
				insertChild(file, newleafPageNo, parentPageNo);
				setRightSibling(file, pageNo, newleafPageNo);
				setRightMostChild(file, parentPageNo); // Asuming thre is no leaf node after this.
				return newleafPageNo;
			} else {
				int parentPageNo = getParent(file, pageNo);
				int newleafPageNo = AddLeafPage(file);
				setParent(file, newleafPageNo, parentPageNo);
				insertChild(file, newleafPageNo, parentPageNo);
				setRightSibling(file, pageNo, newleafPageNo);
				setRightMostChild(file, parentPageNo);
				if (!checkParentspace(file, parentPageNo)) {
					splitInteriorPage(file, parentPageNo);
				}
			}
		} catch (Exception e) {
		}
		return -1;
	}

	public static void splitInteriorPage(RandomAccessFile file, int pageNo) {
		boolean rootflag = CheckifRootNode(file, pageNo);
		int siblingInteriorPageNo = AddInteriorPage(file);
		try {
			splitInteriorData(file, pageNo, siblingInteriorPageNo);
			if (rootflag) {
				int parentPageNo = AddInteriorPage(file);
				setPageasRoot(file, parentPageNo);
				insertChild(file, pageNo, parentPageNo);
				insertChild(file, siblingInteriorPageNo, parentPageNo);
				setParent(file, pageNo, parentPageNo);
				setParent(file, pageNo, siblingInteriorPageNo);
				setRightMostChild(file, parentPageNo);
			} else {
				int parentPageNo = getParent(file, pageNo);
				setParent(file, siblingInteriorPageNo, parentPageNo);
				insertChild(file, siblingInteriorPageNo, parentPageNo);
				setRightMostChild(file, parentPageNo);
				if (!checkParentspace(file, parentPageNo)) {
					splitInteriorPage(file, parentPageNo);
				}
			}
		} catch (Exception e) {
		}
		return;
	}

	public static void splitInteriorData(RandomAccessFile file, int currentPageNo, int siblingInteriorPageNo) {
		// will split the childrens between the current page and new sibling
		// child parents to be updated.
		// rightmost child for current and sibling to be updated.
		return;
	}

	public static Boolean checkParentspace(RandomAccessFile file, int currentPageNo) {
		int seekNoofCells = currentPageNo * pageSize + 1;
		try {
			file.seek(seekNoofCells);
			if (thresholdInteriorCellSize > file.readShort()) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	public static int getParent(RandomAccessFile file, int currentPageNo) {
		int seekParentByte = currentPageNo * pageSize + 0x0A;
		try {
			file.seek(seekParentByte);
			return file.readInt();
		} catch (Exception e) {

		}
		return -1;
	}

	public static void setRightMostChild(RandomAccessFile file, int currentPageNo) {
		int rightMostChildPageNo = getRightMostChildPageNo(file, currentPageNo);
		int seekRightMostChildByte = currentPageNo * pageSize + 0x06;
		try {
			file.seek(seekRightMostChildByte);
			file.writeInt(rightMostChildPageNo);
		} catch (Exception e) {

		}

	}

	public static int getRightMostChildPageNo(RandomAccessFile file, int currentPageNo) {
		// get the rightmost child - after clarification
		return 0;
	}

	public static void setRightSibling(RandomAccessFile file, int currentPageNo, int newPageNo) {
		int seekSiblingByte = currentPageNo * pageSize + 0x06;
		try {
			file.seek(seekSiblingByte);
			file.writeInt(newPageNo);
		} catch (Exception e) {

		}
	}

	public static void insertChild(RandomAccessFile file, int childpageNo, int currentPageNo) {
		
		SortRowIds(file, currentPageNo);
		setRightMostChild(file, currentPageNo);
	}

	public static void SortRowIds(RandomAccessFile file, int currentPageNo) {

	}

	public static void setParent(RandomAccessFile file, int childpageNo, int parentPageNo) {
		int seekParentByte = childpageNo * pageSize + 0x0A;
		try {
			file.seek(seekParentByte);
			file.writeInt(parentPageNo);
		} catch (Exception e) {

		}
	}

	public static void setPageasRoot(RandomAccessFile file, int pageNo) {
		int seekParentByte = pageNo * pageSize + 6;
		try {
			file.seek(seekParentByte);
			file.writeByte(-1);// making root
		} catch (Exception e) {
		}
	}

	public static boolean CheckifRootNode(RandomAccessFile file, int pageNo) {
		int seekParentByte = (pageNo - 1) * pageSize + 6;
		try {
			file.seek(seekParentByte);
			if (file.readInt() == -1) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}
}
