package edu.utdallas.davisbase.storage;

import static edu.utdallas.davisbase.RowIdUtils.ROWID_NULL_VALUE;
import static edu.utdallas.davisbase.storage.RandomAccessFileUtils.skipFully;
import static edu.utdallas.davisbase.storage.TablePageType.INTERIOR;
import static edu.utdallas.davisbase.storage.TablePageType.LEAF;

import java.io.IOException;
import java.io.RandomAccessFile;

class Page {
  static final byte METADATA_PAGE_TYPE_CODE = -1;

  static final long FILE_OFFSET_OF_METADATA_PAGE_TYPE_CODE = 0x00;
  static final long FILE_OFFSET_OF_METADATA_CURRENT_ROWID  = 0x01;
  static final long FILE_OFFSET_OF_METADATA_ROOT_PAGENO    = 0x05;  // spell-checker:ignore pageno

  static final int PAGE_OFFSET_OF_PAGE_TYPE_CODE         = 0X00;
  static final int PAGE_OFFSET_OF_CELL_COUNT             = 0x01;
  static final int PAGE_OFFSET_OF_CELL_CONTENT_START_POINT = 0x03;
  static final int PAGE_OFFSET_OF_RIGHTMOST_PAGENO       = 0x06;
  static final int PAGE_OFFSET_OF_CELL_PAGE_OFFSET_ARRAY = 0x10;

  static final int PAGE_CHILDREN_MAX_COUNT = 2;  // inclusive
  static final int PAGE_OFFSET_SIZE = Short.BYTES;
  static final int PAGE_SIZE = StorageConfiguration.Builder.getDefaultPageSize();

  // called when the interior node is overflowed
  static int AddInteriorPage(RandomAccessFile file) {
    int numofPages = 0;
    try {
      numofPages = (int) (file.length() / PAGE_SIZE);
      numofPages = numofPages + 1;
      file.setLength(PAGE_SIZE * numofPages);
      file.seek((numofPages - 1) * PAGE_SIZE);
      file.writeByte(0x05);// writing page type

      file.seek(((numofPages - 1) * PAGE_SIZE) + 6);
      file.writeInt(-1); // setting right most child to -1
    } catch (Exception e) {
      System.out.println(e);
    }
    return numofPages;
  }

  // called when the leaf id overflowed.
  static int AddLeafPage(RandomAccessFile file) {
    int numofPages = 0;
    try {
      numofPages = (int) (file.length() / PAGE_SIZE);
      numofPages = numofPages + 1;
      file.setLength(PAGE_SIZE * numofPages);
      file.seek((numofPages - 1) * PAGE_SIZE);
      file.writeByte(0x0D);// writing page type

      file.seek(((numofPages - 1) * PAGE_SIZE) + 6);
      file.writeInt(-1);// setting right most child to -1

    } catch (Exception e) {
      System.out.println(e);
    }
    return numofPages;
  }

  static void addTableMetaDataPage(RandomAccessFile file) throws IOException {
    file.setLength(PAGE_SIZE);

    file.seek(FILE_OFFSET_OF_METADATA_PAGE_TYPE_CODE);
    file.writeByte(METADATA_PAGE_TYPE_CODE);

    file.seek(FILE_OFFSET_OF_METADATA_CURRENT_ROWID);
    file.writeInt(ROWID_NULL_VALUE);

    final int rootPageNo = AddLeafPage(file);

    file.seek(FILE_OFFSET_OF_METADATA_ROOT_PAGENO);
    file.writeInt(rootPageNo);

    file.seek(0x09);  // QUESTION What is the field at 0x09 in the metadata page?
    file.writeInt(ROWID_NULL_VALUE);

    setPageasRoot(file, rootPageNo);
  }

  // if no space in leaf node
  static int splitLeafPage(RandomAccessFile file, int pageNo, int rowId) {

    boolean rootflag = CheckifRootNode(file, pageNo);
    try {
      if (rootflag) {
        int parentPageNo = AddInteriorPage(file);
        setPageasRoot(file, parentPageNo);
        setParent(file, pageNo, parentPageNo);
        insertChild(file, pageNo, parentPageNo, rowId);
        int newleafPageNo = AddLeafPage(file);
        setParent(file, newleafPageNo, parentPageNo);
        insertChild(file, newleafPageNo, parentPageNo, rowId);
        setRightSibling(file, pageNo, newleafPageNo);
        setRightMostChild(file, parentPageNo); // Asuming thre is no leaf node after this.
        return newleafPageNo;
      } else {
        int parentPageNo = getParent(file, pageNo);
        int newleafPageNo = AddLeafPage(file);
        setParent(file, newleafPageNo, parentPageNo);
        insertChild(file, newleafPageNo, parentPageNo, rowId);
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

  static void appendChildInINteriorPage(RandomAccessFile file, int childpageNo, int currentPageNo, int rowId) {
    try {
      long currentPageOffset = convertPageNoToFileOffset(currentPageNo);
      int payLoad = 8;// childPageNo & rowID

      // get the data Entry point.
      file.seek(currentPageOffset + 3);
      short startOFCellContentArea = file.readShort();
      short dataEntryPointOffset;
      if (startOFCellContentArea == 0) {
        startOFCellContentArea = (short) PAGE_SIZE;
      }
      dataEntryPointOffset = (short) (startOFCellContentArea - payLoad);
      file.seek(dataEntryPointOffset + currentPageOffset);

      // writing data
      file.writeInt(childpageNo);
      file.writeInt(rowId);

      file.seek(currentPageOffset + 1);
      int noOfRecordsInPage = file.readShort();
      noOfRecordsInPage = noOfRecordsInPage + 1;
      file.seek(currentPageOffset + 1);
      file.writeShort(noOfRecordsInPage);

      // updateing 0x03
      file.seek(currentPageOffset + PAGE_OFFSET_OF_CELL_CONTENT_START_POINT);
      file.writeShort(dataEntryPointOffset);

      // updatign Array
      file.seek(currentPageOffset + PAGE_OFFSET_OF_CELL_PAGE_OFFSET_ARRAY + 2 * (noOfRecordsInPage - 1));
      file.writeShort(dataEntryPointOffset);

      setRightMostChild(file, currentPageNo);

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    // get page offset
    //
  }

  static int getMaxRowIdFromChildren(RandomAccessFile file, int interiorPageNo) throws IOException {
    assert getTablePageType(file, interiorPageNo) == INTERIOR;
    long currentPageOffset = convertPageNoToFileOffset(interiorPageNo);
    short noofCellsInPage = getNumberOfCells(file, interiorPageNo);
    file.seek(currentPageOffset + 16 + (noofCellsInPage - 1) * 2);
    short lastChildOffset = file.readShort();
    file.seek(currentPageOffset + lastChildOffset + 4);
    int maxRowId = file.readInt();
    return maxRowId;
  }

  // FIXME This doesn't account for case when childPageNo is actually the special rightPageNo in header and doesn't have an associated cell.
  static void updateParentwithLeafPageMaxRowID(RandomAccessFile file, int childPageNo, int parentPageNo, int rowId) {
    long currentPageOffset = convertPageNoToFileOffset(parentPageNo);
    try {
      file.seek(currentPageOffset + PAGE_OFFSET_OF_CELL_CONTENT_START_POINT);
      long seekPoint = file.readShort() + currentPageOffset;
      file.seek(seekPoint);
      while (childPageNo != file.readInt()) {
        seekPoint = seekPoint + 8;
        file.seek(seekPoint);
      }
      file.seek(seekPoint + 4); // child page no is 4 byte integer.
      file.writeInt(rowId);

      if (!CheckifRootNode(file, parentPageNo)) {
        updateParentwithLeafPageMaxRowID(file, parentPageNo, getParent(file, parentPageNo), rowId);
      }

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return;

  }

  static int getLeftChild(RandomAccessFile file, int currentPageNo) {
    try {
      int currentPageOffset = (currentPageNo-1)*Page.PAGE_SIZE;
      file.seek(currentPageOffset+16);
      int leftChildDataOffsetPageNo = file.readShort();
            int leftChildPageNo = 0;
            if (leftChildDataOffsetPageNo == 0) {
                return -1;
            }

            file.seek(currentPageOffset + leftChildDataOffsetPageNo);
            leftChildPageNo = file.readInt();
      return leftChildPageNo;
    }catch(Exception e) {

    }
    return -1;

  }

  static int getnextRowIdInterior(RandomAccessFile file) {
    try {
      file.seek(0x09);
      int rowId = file.readInt();
      return (rowId + 1);

    } catch (Exception e) {
    }
    return -1;

  }

  static int splitInteriorPage(RandomAccessFile file, int pageNo) {
    boolean rootflag = CheckifRootNode(file, pageNo);
    int siblingInteriorPageNo = AddInteriorPage(file);
    try {
      splitInteriorData(file, pageNo, siblingInteriorPageNo);
      if (rootflag) {
        int parentPageNo = AddInteriorPage(file);
        setPageasRoot(file, parentPageNo);

        // get maximum rowId from the children pages
        int maxRowIdfromChildrenofCurrentPage = getMaxRowIdFromChildren(file, pageNo);
        insertChild(file, pageNo, parentPageNo, maxRowIdfromChildrenofCurrentPage);

        // get maximum rowId from the children pages
        int maxRowIdfromChildrenofSiblingPage = getMaxRowIdFromChildren(file, pageNo);
        insertChild(file, siblingInteriorPageNo, parentPageNo, maxRowIdfromChildrenofSiblingPage);
        setParent(file, pageNo, parentPageNo);
        setParent(file, siblingInteriorPageNo, parentPageNo);
        setRightMostChild(file, parentPageNo);

      } else {
        int parentPageNo = getParent(file, pageNo);
        setParent(file, siblingInteriorPageNo, parentPageNo);
        // get maximum rowId from the children pages

        int maxRowIdfromChildrenofSiblingPage = getMaxRowIdFromChildren(file, pageNo);
        insertChild(file, siblingInteriorPageNo, parentPageNo, maxRowIdfromChildrenofSiblingPage);
        setRightMostChild(file, parentPageNo);
        if (!checkParentspace(file, parentPageNo)) {
          return splitInteriorPage(file, parentPageNo);
        }
      }
    } catch (Exception e) {
    }
    return siblingInteriorPageNo;

  }

  static void splitInteriorData(RandomAccessFile file, int currentPageNo, int siblingInteriorPageNo) {
    long currentPageOffset = (currentPageNo - 1) * PAGE_SIZE;
    long siblingPageOffset = (siblingInteriorPageNo - 1) * PAGE_SIZE;
    int currentPageCellContentReference = 3;
    try {
      file.seek(currentPageOffset + currentPageCellContentReference);
      short currentPageCellContentOffset = file.readShort();
      file.seek(currentPageOffset + 1);
      int noofRecords = file.readShort();

      long lastBeforeElementInArray = currentPageOffset + 16 + 2 * (noofRecords - 2);
      file.seek(lastBeforeElementInArray);
      short lastBeforeElementOffset = file.readShort();

      short noOfBytes = (short) (lastBeforeElementOffset - currentPageCellContentOffset);
      byte[] lastChildData = new byte[noOfBytes];
      file.seek(currentPageOffset + currentPageCellContentOffset);
      file.readFully(lastChildData);
      noofRecords = noofRecords - 1;
      file.seek(currentPageOffset + 1);
      file.writeShort(noofRecords);
      file.seek(currentPageOffset + currentPageCellContentReference);
      file.writeShort(lastBeforeElementOffset);
      setRightMostChild(file, currentPageNo);

      file.seek(siblingPageOffset + 3);
      short siblingContentStartOffset = file.readShort();

      if (siblingContentStartOffset == 0) {
        siblingContentStartOffset = (short) PAGE_SIZE;
      }

      short newStartPoint = (short) (siblingContentStartOffset - noOfBytes);

      file.seek(siblingPageOffset + newStartPoint);
      file.write(lastChildData);

      file.seek(siblingPageOffset + 1);
      short noOfSiblingRecords = file.readShort();
      noOfSiblingRecords = (short) (noOfSiblingRecords + 1);

      file.seek(siblingPageOffset+1);
      file.writeShort(noOfSiblingRecords);

      file.seek(siblingPageOffset + 16 + 2 * (noOfSiblingRecords - 1));
      file.writeShort(newStartPoint);

      file.seek(siblingPageOffset + 3);
      file.writeShort(newStartPoint);

      setRightMostChild(file, siblingInteriorPageNo);

      setParent(file, getRightMostChildPageNo(file, siblingInteriorPageNo), siblingInteriorPageNo);

      updateLeftChildInfo(file, siblingInteriorPageNo);

    } catch (Exception e) {

    }
    // will split the childrens between the current page and new sibling
    // child parents to be updated.
    // rightmost child for current and sibling to be updated.
    return;
  }

  static void updateLeftChildInfo(RandomAccessFile file, int currentPageNo) {
    try {
        int pageOffset = ((currentPageNo - 1) * Page.PAGE_SIZE);
        int cellContentOffset = pageOffset + 3;
        file.seek(cellContentOffset);
        int firstChilddataOffset = file.readShort();
        file.seek(pageOffset + firstChilddataOffset + 8);
        int leftChildPageNo = file.readInt();
        file.seek(pageOffset+firstChilddataOffset);
        file.writeInt(leftChildPageNo);
    } catch (Exception e) {

    }

  }

  static boolean checkParentspace(RandomAccessFile file, int currentPageNo) {
    long seekNoofRecords = (currentPageNo - 1) * PAGE_SIZE + 1;
    try {
      file.seek(seekNoofRecords);
      int noOfRecords = file.readShort();
      if (PAGE_CHILDREN_MAX_COUNT > noOfRecords) {
        return true;
      }
    } catch (Exception e) {
    }
    return false;
  }

  static int getParent(RandomAccessFile file, int currentPageNo) {
    long seekParentByte = (currentPageNo - 1) * PAGE_SIZE + 10;
    try {
      file.seek(seekParentByte);
      return file.readInt();
    } catch (Exception e) {

    }
    return -1;
  }

  static int getRightMostChildPageNo(RandomAccessFile file, int currentPageNo) {
    // get the rightmost child - after clarification
    try {
      long currentPageCell = (currentPageNo - 1) * Page.PAGE_SIZE;

      file.seek(currentPageCell + 3);
      short rightChildDataStart = file.readShort();

      file.seek(currentPageCell + rightChildDataStart);
      int rightMostChildPageNo = file.readInt();

      return rightMostChildPageNo;

    } catch (Exception e) {

    }

    return 0;
  }

  static void setRightMostChild(RandomAccessFile file, int currentPageNo) {
    int rightMostChildPageNo = getRightMostChildPageNo(file, currentPageNo);
    long seekRightMostChildByte = (currentPageNo - 1) * PAGE_SIZE + PAGE_OFFSET_OF_RIGHTMOST_PAGENO;
    try {
      file.seek(seekRightMostChildByte);
      file.writeInt(rightMostChildPageNo);
    } catch (Exception e) {

    }
  }

  static void setRightSibling(RandomAccessFile file, int currentPageNo, int newPageNo) {
    long seekSiblingByte = (currentPageNo - 1) * PAGE_SIZE + 6;
    try {
      file.seek(seekSiblingByte);
      file.writeInt(newPageNo);
    } catch (Exception e) {

    }
  }

  /**
   * @return the page no of the right sibling of the given leaf page; `-1`
   *         indicates
   */
  static int getRightSiblingOfLeafPage(RandomAccessFile file, int pageNo) throws IOException {
    assert Page.getTablePageType(file, pageNo) == LEAF;

    final long fileOffsetOfPage = convertPageNoToFileOffset(pageNo);
    final long fileOffsetOfPageRightSiblingPageNo = fileOffsetOfPage + PAGE_OFFSET_OF_RIGHTMOST_PAGENO;
    file.seek(fileOffsetOfPageRightSiblingPageNo);

    final int rightSiblingPageNo = file.readInt();
    return rightSiblingPageNo;
  }

  static void insertChild(RandomAccessFile file, int childpageNo, int currentPageNo, int rowId) {
    try {
      appendChildInINteriorPage(file, childpageNo, currentPageNo, rowId);
      SortRowIds(file, currentPageNo);
      setRightMostChild(file, currentPageNo);
    } catch (Exception e) {

    }

  }

  static void updateInteriorRowID(RandomAccessFile file, int rowId) {
    try {
      file.seek(0x09);
      file.writeInt(rowId);
    }catch(Exception e) {

    }
    return;
  }

  static void SortRowIds(RandomAccessFile file, int currentPageNo) {{}

  }

  static void setParent(RandomAccessFile file, int childpageNo, int parentPageNo) {
    long seekParentByte = (childpageNo - 1) * PAGE_SIZE + 10;
    try {
      file.seek(seekParentByte);
      file.writeInt(parentPageNo);
    } catch (Exception e) {

    }
  }

  static void setPageasRoot(RandomAccessFile file, int pageNo) {

    int seekParentByte = (pageNo - 1) * PAGE_SIZE + 10;
    try {
      file.seek(seekParentByte);
      file.writeInt(-1);// making root
      setMetaDataRootPageNo(file, pageNo);
    } catch (Exception e) {
    }
  }

  static void setMetaDataRootPageNo(RandomAccessFile file, int newRootPageNo) throws IOException {
    file.seek(FILE_OFFSET_OF_METADATA_ROOT_PAGENO);
    file.writeInt(newRootPageNo);
  }

  static int getMetaDataRootPageNo(RandomAccessFile file) throws IOException {
    file.seek(FILE_OFFSET_OF_METADATA_ROOT_PAGENO);
    return file.readInt();
  }

  static boolean CheckifRootNode(RandomAccessFile file, int pageNo) {

    int seekParentByte = (pageNo - 1) * PAGE_SIZE + 10;
    try {
      file.seek(seekParentByte);
      if (file.readInt() == -1) {
        return true;
      }
    } catch (Exception e) {
    }
    return false;
  }

  static long convertPageNoToFileOffset(int pageNo) {
    assert 1 <= pageNo && pageNo <= Integer.MAX_VALUE;

    final long fileOffset = (pageNo - 1) * (long) Page.PAGE_SIZE;
    return fileOffset;
  }

  static void seekPageNo(RandomAccessFile file, int pageNo) throws IOException {
    final long pageFileOffset = convertPageNoToFileOffset(pageNo);
    file.seek(pageFileOffset);
  }

  static TablePageType getTablePageType(RandomAccessFile file, int pageNo) throws IOException {
    final long fileOffset = convertPageNoToFileOffset(pageNo);
    file.seek(fileOffset);

    final byte code = file.readByte();
    final TablePageType type = TablePageType.fromCode(code);
    return type;
  }

  private static void setPageTypeCode(RandomAccessFile file, int pageNo, byte pageTypeCode) throws IOException {
    seekPageNo(file, pageNo);
    skipFully(file, PAGE_OFFSET_OF_PAGE_TYPE_CODE);
    file.write(pageTypeCode);
  }

  static void setTablePageType(RandomAccessFile file, int pageNo, TablePageType tablePageType) throws IOException {
    setPageTypeCode(file, pageNo, tablePageType.toCode());
  }

  static short getNumberOfCells(RandomAccessFile file, int pageNo) throws IOException {
    final long fileOffsetOfPage = convertPageNoToFileOffset(pageNo);
    final long fileOffsetOfPageCellCount = fileOffsetOfPage + PAGE_OFFSET_OF_CELL_COUNT;
    file.seek(fileOffsetOfPageCellCount);

    final short cellCount = file.readShort();
    return cellCount;
  }

  /**
   * @param file      the file from which to get the page offset of the cell
   * @param pageNo    the one-based number of the page in the file
   * @param cellIndex the zero-based index of the cell in the page
   * @return zero-based offset of the start of the cell relative to the beginning
   *         of the page
   * @throws IOException
   */
  static short getPageOffsetOfCell(RandomAccessFile file, int pageNo, short cellIndex) throws IOException {
    final long fileOffsetOfPage = convertPageNoToFileOffset(pageNo);
    final long fileOffsetOfCellPageOffsetArray = fileOffsetOfPage + PAGE_OFFSET_OF_CELL_PAGE_OFFSET_ARRAY;
    final long fileOffsetOfEntryInCellPageOffsetArray = fileOffsetOfCellPageOffsetArray + (cellIndex * PAGE_OFFSET_SIZE);
    file.seek(fileOffsetOfEntryInCellPageOffsetArray);

    final short pageOffsetOfCell = file.readShort();
    return pageOffsetOfCell;
  }

  /**
   * @param file      the file from which to get the table interior cell's left
   *                  child page no
   * @param pageNo    the one-based number of the page in the file
   * @param cellIndex the zero-based index of the cell in the page
   * @return the left child page no of the table interior cell
   * @throws IOException
   */
  static int getTableInteriorCellLeftChildPageNo(RandomAccessFile file, int pageNo, short cellIndex) throws IOException {
    final long fileOffsetOfPage = convertPageNoToFileOffset(pageNo);
    final short pageOffsetOfCell = getPageOffsetOfCell(file, pageNo, cellIndex);
    final long fileOffsetOfPageCell = fileOffsetOfPage + pageOffsetOfCell;
    file.seek(fileOffsetOfPageCell);

    final int leftChildPageNo = file.readInt();
    return leftChildPageNo;
  }

  static int getLeftmostChildPageNoOfInteriorPage(RandomAccessFile file, int pageNo) throws IOException {
    assert getTablePageType(file, pageNo) == INTERIOR;

    final short cellCount = getNumberOfCells(file, pageNo);
    final int leftmostChildPageNo = (cellCount <= 0) ? getRightMostChildPageNo(file, pageNo)
        : getTableInteriorCellLeftChildPageNo(file, pageNo, (short) 0);
    return leftmostChildPageNo;
  }

  /**
   * @param file   the file in which to check if the specified page exists
   * @param pageNo the page no whose existence to check in the given file
   * @return whether the specified page exists in the given file
   * @throws IOException
   */
  static boolean exists(RandomAccessFile file, int pageNo) throws IOException {
    return pageNo > 0 && convertPageNoToFileOffset(pageNo) < file.length();
  }

  static byte getNumberOfColumnsOfTableLeafCell(RandomAccessFile file, long fileOffsetOfTableLeafCell) throws IOException {
    file.seek(fileOffsetOfTableLeafCell);
    return file.readByte();
  }

  static int getSizeOfTableLeafCellColumn(RandomAccessFile file, long fileOffsetOfTableLeafCell, int columnIndex) throws IOException {
    file.seek(fileOffsetOfTableLeafCell + 1 + columnIndex);  // COMBAK Clean this up.
    return file.readByte();
  }

}
