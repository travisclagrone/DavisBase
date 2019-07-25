package edu.utdallas.davisbase.storage;

import java.io.IOException;
import java.io.RandomAccessFile;
import static edu.utdallas.davisbase.storage.TablePageType.INTERIOR;
import static edu.utdallas.davisbase.storage.TablePageType.LEAF;

public class Page {

  static final int BYTES_OF_PAGE_OFFSET = Short.BYTES;

  static final int PAGE_OFFSET_OF_CELL_COUNT = 0x02;
  static final int PAGE_OFFSET_OF_CELL_PAGE_OFFSET_ARRAY = 0x10;

	static final int pageSize = StorageConfiguration.Builder.getDefaultPageSize();
  static final int maximumnoOFChildren = 2;
  static final long metaDataRootPageNoOffsetInFile = 0x05;

	TableRowBuilder tableRowBuilder;

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

	public static void addTableMetaDataPage(RandomAccessFile file) throws IOException {
		try {
			file.setLength(pageSize);
			file.seek(0x00);
			file.writeByte(-1);

			int firstPageNo = AddLeafPage(file);

			int rowId = 0;
			file.seek(0x01);
			file.writeInt(rowId);
			file.seek(0x05);
			file.writeInt(firstPageNo);

			setPageasRoot(file, firstPageNo);
		} catch (Exception e) {

		}
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
					int newPageNo = splitInteriorPage(file, parentPageNo);
				}
				return newleafPageNo;
			}
		} catch (Exception e) {
		}
		return -1;
	}

	public static int splitInteriorPage(RandomAccessFile file, int pageNo) {
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
					return splitInteriorPage(file, parentPageNo);
				}
			}
		} catch (Exception e) {
		}
		return siblingInteriorPageNo;

	}

	public static void splitInteriorData(RandomAccessFile file, int currentPageNo, int siblingInteriorPageNo) {
		long currentPageOffset = (currentPageNo - 1) * pageSize;
		long siblingPageOffset = (siblingInteriorPageNo - 1) * pageSize;
		int currentPageCellContentReference = 3;
		try {
			file.seek(currentPageOffset + currentPageCellContentReference);
			short currentPageCellContentOffset = file.readShort();
			file.seek(currentPageOffset + 1);
			int noofRecords = file.readShort();

			long lastBeforeElementInArray = currentPageOffset + 16 + 2 * (noofRecords - 1);
			file.seek(lastBeforeElementInArray);
			short lastBeforeElementOffset = file.readShort();

			short noOfBytes = (short) (lastBeforeElementOffset - currentPageCellContentOffset);
			byte[] lastChildData = new byte[noOfBytes];
			file.seek(currentPageCellContentOffset);
			file.readFully(lastChildData);

			file.seek(currentPageOffset + 1);
			file.writeShort(noofRecords - 1);
			file.seek(currentPageOffset + currentPageCellContentReference);
			file.writeShort(lastBeforeElementOffset);
			setRightMostChild(file, currentPageNo);

			file.seek(siblingPageOffset + 3);
			short siblingContentStartOffset = file.readShort();

			if (siblingContentStartOffset == 0) {
				siblingContentStartOffset = (short) pageSize;
			}

			short newStartPoint = (short) (siblingContentStartOffset - noOfBytes);

			file.seek(siblingPageOffset + newStartPoint);
			file.write(lastChildData);

			file.seek(siblingPageOffset + 1);
			short noOfSiblingRecords = file.readShort();

			file.seek(siblingPageOffset + 1);
			file.writeShort(noOfSiblingRecords + 1);

			file.seek(siblingPageOffset + 16 + 2 * (noOfSiblingRecords - 1));
			file.writeShort(newStartPoint);

			file.seek(siblingPageOffset + 3);
			file.writeByte(newStartPoint);

			setRightMostChild(file, siblingInteriorPageNo);

			setParent(file, getRightMostChildPageNo(file, siblingInteriorPageNo), siblingInteriorPageNo);

		} catch (Exception e) {

		}
		// will split the childrens between the current page and new sibling
		// child parents to be updated.
		// rightmost child for current and sibling to be updated.
		return;
	}

	public static Boolean checkParentspace(RandomAccessFile file, int currentPageNo) {
		long seekNoofRecords = (currentPageNo - 1) * pageSize + 1;
		try {
			file.seek(seekNoofRecords);
			if (maximumnoOFChildren > file.readShort()) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	public static int getParent(RandomAccessFile file, int currentPageNo) {
		long seekParentByte = (currentPageNo - 1) * pageSize + 10;
		try {
			file.seek(seekParentByte);
			return file.readInt();
		} catch (Exception e) {

		}
		return -1;
	}

	public static void setRightMostChild(RandomAccessFile file, int currentPageNo) {
		int rightMostChildPageNo = getRightMostChildPageNo(file, currentPageNo);
		long seekRightMostChildByte = currentPageNo * pageSize + 6;
		try {
			file.seek(seekRightMostChildByte);
			file.writeInt(rightMostChildPageNo);
		} catch (Exception e) {

		}
	}

	public static int getRightMostChildPageNo(RandomAccessFile file, int currentPageNo) {
		// get the rightmost child - after clarification
		try {
			long currentPageCell = (currentPageNo - 1) * Page.pageSize;
//			int seekNoofCells = currentPageCell + 1;
//			file.seek(seekNoofCells);
//			short noOfCells = file.readShort();
//			short arraySize = (short) (2 * noOfCells);
//			int lastElementInArray = currentPageCell + (16 + 2 * (arraySize - 1));
//			file.seek(lastElementInArray);
//			int lastChildAddress = (currentPageCell + file.readShort());
//			file.seek(currentPageCell + lastChildAddress + 4); //4 bytes for rowId
//			return file.readInt();

			file.seek(currentPageCell + 3);
			short rightChildDataStart = file.readShort();

			file.seek(currentPageCell + rightChildDataStart);
			int noofColumns = file.readByte();
			file.seek(currentPageCell + rightChildDataStart + 1 + noofColumns + 4);

//			file.seek(currentPageCell + rightChildDataStart + 4);//change this.
			return file.readInt();

		} catch (Exception e) {

		}

		return 0;
	}

	public static void setRightSibling(RandomAccessFile file, int currentPageNo, int newPageNo) {
		long seekSiblingByte = (currentPageNo - 1) * pageSize + 6;
		try {
			file.seek(seekSiblingByte);
			file.writeInt(newPageNo);
		} catch (Exception e) {

		}
	}

	public static void insertChild(RandomAccessFile file, int childpageNo, int currentPageNo) {
		try {
			TableFile tableFile = new TableFile(file);

//			int rowId = tableFile.getnextRowId(file);
			int rowId = getnextRowIdInterior(file);

			TableRowBuilder tableRowBuilder = new TableRowBuilder();
			tableRowBuilder.appendInt(rowId);
			tableRowBuilder.appendInt(childpageNo);
			tableFile.appendRow(tableRowBuilder);

			updateInteriorRowID(file,rowId);

			SortRowIds(file, currentPageNo);
			setRightMostChild(file, currentPageNo);
		} catch (Exception e) {

		}

	}


	public static void updateInteriorRowID(RandomAccessFile file, int rowId) {
		try {
			file.seek(0x09);
			file.writeInt(rowId);
		}catch(Exception e) {

		}
		return;
	}

	public static int getnextRowIdInterior(RandomAccessFile file) {
		try {
			file.seek(0x09);
			int rowId=file.readInt();
			return (rowId + 1);

		} catch (Exception e) {
		}
		return -1;
	}


	public static void SortRowIds(RandomAccessFile file, int currentPageNo) {

	}

	public static void setParent(RandomAccessFile file, int childpageNo, int parentPageNo) {
		long seekParentByte = (childpageNo - 1) * pageSize + 10;
		try {
			file.seek(seekParentByte);
			file.writeInt(parentPageNo);
		} catch (Exception e) {

		}
	}

	public static void setPageasRoot(RandomAccessFile file, int pageNo) {

		int seekParentByte = (pageNo-1) * pageSize + 10;
		try {
			file.seek(seekParentByte);
			file.writeInt(-1);// making root
			updateMetaDataRoot(file, pageNo);
		} catch (Exception e) {
		}
	}

	public static void updateMetaDataRoot(RandomAccessFile file, int newRootPageNo) throws IOException {
    file.seek(metaDataRootPageNoOffsetInFile);
    file.writeInt(newRootPageNo);
  }

  public static int getMetaDataRootPageNo(RandomAccessFile file) throws IOException {
    file.seek(metaDataRootPageNoOffsetInFile);
    return file.readInt();
  }

	public static boolean CheckifRootNode(RandomAccessFile file, int pageNo) {

		int seekParentByte = (pageNo - 1) * pageSize + 10;
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

    final long fileOffset = (pageNo - 1) * (long) Page.pageSize;
    return fileOffset;
  }

  public static TablePageType getTablePageType(RandomAccessFile file, int pageNo) throws IOException {
    final long fileOffset = convertPageNoToFileOffset(pageNo);
    file.seek(fileOffset);

    final byte code = file.readByte();
    final TablePageType type = TablePageType.fromCode(code);
    return type;
  }

  public static short getNumberOfCells(RandomAccessFile file, int pageNo) throws IOException {
    final long fileOffsetOfPage = convertPageNoToFileOffset(pageNo);
    final long fileOffsetOfPageCellCount = fileOffsetOfPage + PAGE_OFFSET_OF_CELL_COUNT;
    file.seek(fileOffsetOfPageCellCount);

    final short cellCount = file.readShort();
    return cellCount;
  }

  /**
   * @param file the file from which to get the page offset of the cell
   * @param pageNo the one-based number of the page in the file
   * @param cellIndex the zero-based index of the cell in the page
   * @return zero-based offset of the start of the cell relative to the beginning of the page
   * @throws IOException
   */
  public static short getPageOffsetOfCell(RandomAccessFile file, int pageNo, short cellIndex) throws IOException {
    final long fileOffsetOfPage = convertPageNoToFileOffset(pageNo);
    final long fileOffsetOfCellPageOffsetArray = fileOffsetOfPage + PAGE_OFFSET_OF_CELL_PAGE_OFFSET_ARRAY;
    final long fileOffsetOfEntryInCellPageOffsetArray = fileOffsetOfCellPageOffsetArray + (cellIndex * BYTES_OF_PAGE_OFFSET);
    file.seek(fileOffsetOfEntryInCellPageOffsetArray);

    final short pageOffsetOfCell = file.readShort();
    return pageOffsetOfCell;
  }

  /**
   * @param file the file from which to get the table interior cell's left child page no
   * @param pageNo the one-based number of the page in the file
   * @param cellIndex the zero-based index of the cell in the page
   * @return the left child page no of the table interior cell
   * @throws IOException
   */
  public static int getTableInteriorCellLeftChildPageNo(RandomAccessFile file, int pageNo, short cellIndex) throws IOException {
    final long fileOffsetOfPage = convertPageNoToFileOffset(pageNo);
    final short pageOffsetOfCell = getPageOffsetOfCell(file, pageNo, cellIndex);
    final long fileOffsetOfPageCell = fileOffsetOfPage + pageOffsetOfCell;
    file.seek(fileOffsetOfPageCell);

    final int leftChildPageNo = file.readInt();
    return leftChildPageNo;
  }

  public static int getLeftmostChildPageNoOfInteriorPage(RandomAccessFile file, int pageNo) throws IOException {
    assert getTablePageType(file, pageNo) == INTERIOR;

    final short cellCount = getNumberOfCells(file, pageNo);
    final int leftmostChildPageNo = (cellCount <= 0)
                                    ? getRightMostChildPageNo(file, pageNo)
                                    : getTableInteriorCellLeftChildPageNo(file, pageNo, (short) 0);
    return leftmostChildPageNo;
  }

}
