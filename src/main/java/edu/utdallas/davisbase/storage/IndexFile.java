package edu.utdallas.davisbase.storage;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.Arrays;

import org.checkerframework.checker.nullness.qual.Nullable;

import edu.utdallas.davisbase.DavisBase;
import edu.utdallas.davisbase.NotImplementedException;
import edu.utdallas.davisbase.common.DavisBaseConstant;

import static java.lang.String.format;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class IndexFile implements Closeable {

  protected final RandomAccessFile file;
  
  public IndexFile(RandomAccessFile file) {
    checkNotNull(file);
    checkArgument(file.getChannel().isOpen());
    this.file = file;

    try {

      if (file.length() < 512) {
        IndexPage.addTableMetaDataPage(file);
      }
    } catch (Exception e) {

    }
  }

  @Override
  public void close() throws IOException {
    file.close();
  }
  
  //returns root page offset
  public long rootPageOffset() throws IOException {
    int rootPgaeNo;
    long rootPageNoOffset = 5;
    file.seek(rootPageNoOffset);
    rootPgaeNo = file.readInt();
    long rootPageOffset = (rootPgaeNo - 1) * Page.pageSize;
    return rootPageOffset;
  }
  
  //returns root page type
  public byte rootPageType() throws IOException {
    long rootPageOffset = rootPageOffset();
    file.seek(rootPageOffset);
    return file.readByte();    
  }
  
  //returns no of records in a page
  public short noOfRecordsInPage(long pageOffset) throws IOException {
    file.seek(pageOffset+1);    
    return file.readShort();
  }
  
  //writing No.of RowIds and indexValue length in root page for first time
  public short writeFirstRecordInRootPage(long rootPageOffset, byte indexValueLength) throws IOException {
    short indexRecordOffseet;
    indexRecordOffseet = (short) (Page.pageSize-indexValueLength-6);
    file.seek(rootPageOffset+indexRecordOffseet);
    file.writeByte(1);//No of rowIds
    file.seek(rootPageOffset+indexRecordOffseet+1);
    file.writeByte(indexValueLength);//index value length
    return indexRecordOffseet;
  }
  
  //updating root page header data for first record
  public void updateRootPageHeaderData(long rootPageOffset, short indexRecordOffseet) throws IOException {
  //updating header values
    file.seek(rootPageOffset+1);
    file.writeShort(1);
    file.seek(rootPageOffset+3);
    file.writeShort(indexRecordOffseet);//writing on 0x03 - data record offset w.r.t page
    file.seek(rootPageOffset+16);
    file.writeShort(indexRecordOffseet);//writing on 2nd row from 0x10 - each record offset w.r.t page
  }
  
  //adding a rowId for already existing record in leaf page
  public void addRowIdInLeafPage(long pageOffset, short recordOffset, short minRecordOffset, int rowId) throws IOException {
    file.seek(pageOffset+recordOffset);
    byte noOfRowIds = file.readByte();
    file.seek(pageOffset+recordOffset+1);
    byte recordValueLength = file.readByte();
    int totalSpaceRequired = recordOffset-minRecordOffset+2+recordValueLength+(noOfRowIds*4);
    byte[] copyBytesToTemp = new byte[totalSpaceRequired];
    file.seek(pageOffset+minRecordOffset);
    file.read(copyBytesToTemp, 0, totalSpaceRequired);
    file.seek(pageOffset+minRecordOffset-4);
    file.write(copyBytesToTemp);
    file.seek(pageOffset+minRecordOffset+totalSpaceRequired-4);
    file.writeInt(rowId);
    recordOffset = (short) (recordOffset - 4);
    file.seek(pageOffset+recordOffset);
    file.writeByte(noOfRowIds+1);
    file.seek(pageOffset+3);
    file.writeShort(minRecordOffset-4);
    file.seek(pageOffset+1);
    short noOfRecords = file.readShort();
    short addOffset = 0; short tempRecordOffset;
    while(noOfRecords > 0) {
      file.seek(pageOffset+16+addOffset);
      tempRecordOffset = file.readShort();
      file.seek(pageOffset+16+addOffset);
      if(tempRecordOffset <= recordOffset+4) 
        file.writeShort(tempRecordOffset-4);
      noOfRecords--;
      addOffset += 2;
    }
  }
  
  //adding a rowId for already existing record in interior page
  private void addRowIdInInteriorPage(long pageOffset, short recordOffset, int rowId) throws IOException {
    // TODO Auto-generated method stub
    file.seek(pageOffset+1);
    short recordCount = file.readShort();
    short minRecordOffset = (short) Page.pageSize;
    short addOffset = 0;
    while(recordCount > 0) {
      file.seek(pageOffset+16+addOffset);
      recordOffset = file.readShort();
      minRecordOffset = recordOffset < minRecordOffset ? recordOffset : minRecordOffset;
      recordCount--;
      addOffset += 2;
    }
    file.seek(pageOffset+recordOffset+4);
    byte noOfRowIds = file.readByte();
    file.seek(pageOffset+recordOffset+5);
    byte recordValueLength = file.readByte();
    int totalSpaceRequired = recordOffset-minRecordOffset+2+recordValueLength+(noOfRowIds*4);
    byte[] copyBytesToTemp = new byte[totalSpaceRequired];
    file.seek(pageOffset+minRecordOffset);
    file.read(copyBytesToTemp, 0, totalSpaceRequired);
    file.seek(pageOffset+minRecordOffset-4);
    file.write(copyBytesToTemp);
    file.seek(pageOffset+minRecordOffset+totalSpaceRequired-4);
    file.writeInt(rowId);
    file.seek(pageOffset+3);
    file.writeShort(minRecordOffset-4);
    file.seek(pageOffset+1);
    short noOfRecords = file.readShort();
    addOffset = 0; short tempRecordOffset;
    while(noOfRecords > 0) {
      file.seek(pageOffset+16+addOffset);
      tempRecordOffset = file.readShort();
      file.seek(pageOffset+16+addOffset);
      if(tempRecordOffset <= recordOffset) 
        file.writeShort(tempRecordOffset-4);
      noOfRecords--;
      addOffset += 2;
    }
  }
  
  //adding a record in Index page for text type column
  public void addText(@Nullable String indexValue, int rowId) throws IOException {
    //locating root page    
    long rootPageOffset = rootPageOffset();
    file.seek(rootPageOffset);
    byte rootPageType = file.readByte();
    
    //reading no of records in root page
    short noOfRecordsInRootPage = noOfRecordsInPage(rootPageOffset);
    
    /*if noOfRecordsInRootPage is 0 then add record in rootPage
      else if it matches with root page record value, insert just the rowId
      else determine to which child it should check with
    */
    if(noOfRecordsInRootPage == 0) {
      //writing data to page
      byte indexValueLength;
      indexValueLength = (byte) indexValue.length();
      short indexRecordOffseet = writeFirstRecordInRootPage(rootPageOffset, indexValueLength);
      file.seek(rootPageOffset+indexRecordOffseet+2);
      byte[] indexValueToByteArray = indexValue.getBytes();
      file.write(indexValueToByteArray);//write index value to cell array
      file.seek(rootPageOffset+indexRecordOffseet+indexValueLength+2);
      file.writeInt(rowId);//write rowId value
      
      //updating header values
      updateRootPageHeaderData(rootPageOffset, indexRecordOffseet);
    }
    
    /*if root page type is still 0x0A that  means there are no children yet
      two cases here i) inserting rowId for already existing record
                    ii) Or adding a new record
     */
    else if(noOfRecordsInRootPage > 0 && rootPageType == 0x0A) {
      short recordCount = noOfRecordsInRootPage;
      short matchedRecordOffset = 0;
      short addOffset = 0;
      short recordOffset = 0; short minRecordOffset = (short) Page.pageSize;
      boolean valueMatchFlag = false;
      while(recordCount > 0) {
        file.seek(rootPageOffset+16+addOffset);
        recordOffset = file.readShort();
        minRecordOffset = recordOffset < minRecordOffset ? recordOffset : minRecordOffset;
        file.seek(rootPageOffset+recordOffset+1);
        Byte recordLength = file.readByte();
        byte[] recordBytes = new byte[recordLength];
        file.seek(rootPageOffset+recordOffset+2);
        file.read(recordBytes);
        String recordValue = new String(recordBytes);
        if(indexValue.compareToIgnoreCase(recordValue) == 0) {
          valueMatchFlag = true;
          matchedRecordOffset = recordOffset;
        }
        recordCount--;
        addOffset += 2;
      }
      if(valueMatchFlag)//inserting rowId for already existing record 
        addRowIdInLeafPage(rootPageOffset, matchedRecordOffset, minRecordOffset, rowId);
      else {//adding a new record into Index page
        recordOffset = (short) (minRecordOffset-indexValue.length()-6);
        file.seek(rootPageOffset+recordOffset);
        file.write(1);
        file.seek(rootPageOffset+recordOffset+1);
        file.write(indexValue.length());
        file.seek(rootPageOffset+recordOffset+2);
        byte[] indexValueToByteArray = indexValue.getBytes();
        file.write(indexValueToByteArray);
        file.seek(rootPageOffset+recordOffset+indexValue.length()+2);
        file.writeInt(rowId);
        file.seek(rootPageOffset+3);
        file.writeShort(recordOffset);
        file.seek(rootPageOffset+16+(noOfRecordsInRootPage*2));
        file.writeShort(recordOffset);
        file.seek(rootPageOffset+1);
        file.writeShort(noOfRecordsInRootPage+1);
        file.seek(5);
        int pageNo = file.readInt();
        IndexPage.sortKeys(file, pageNo);
        if(noOfRecordsInRootPage+1 > 3)
          IndexPage.splitLeafPage(file, pageNo);
      }
    }
    
    /*
     if root page type is not 0x0A then there are children in the tree
     if the index value is not already in the tree then we have to traverse the tree to find the leaf page to add the new record
     if the index value is already present then we have to check every page in the traverse path o insert the rowId
     */
    else {
      byte pageType = rootPageType;
      long pageOffset = rootPageOffset;
      while(pageType == 0x02) {//checking all interior pages in path
        short noOfRecordsInPage;
        file.seek(pageOffset+1);
        noOfRecordsInPage = file.readShort();
        
        short addOffset = 0;
        short recordOffset = 0; 
        boolean valueMatchFlag = false;
        while(noOfRecordsInPage > 0) {
          file.seek(pageOffset+16+addOffset);
          recordOffset = file.readShort();
          file.seek(pageOffset+recordOffset+5);
          Byte recordLength = file.readByte();
          byte[] recordBytes = new byte[recordLength];
          file.seek(pageOffset+recordOffset+6);
          file.read(recordBytes);
          String recordValue = new String(recordBytes);
          if(indexValue.compareToIgnoreCase(recordValue) == 0) {//if index value matches with already existing value then add rowId
            addRowIdInInteriorPage(pageOffset, recordOffset, rowId);
            valueMatchFlag = true;
            break;
          }
          else if(indexValue.compareToIgnoreCase(recordValue) > 0) {//if index value greater than record value move to next record on right
            if(noOfRecordsInPage-1 == 0) {//if there are no more records to compare with then move to right child page
              file.seek(pageOffset+6);
              int nextPageNo = file.readInt();
              pageOffset = (nextPageNo - 1) * Page.pageSize;
              file.seek(pageOffset);
              pageType = file.readByte();
              break;
            }
          }
          
          else {//if index value less than record value move to left record
            file.seek(pageOffset+recordOffset);
            int nextPageNo = file.readInt();
            pageOffset = (nextPageNo - 1) * Page.pageSize;
            file.seek(pageOffset);
            pageType = file.readByte();
            break;
          }
          
        }
        if(valueMatchFlag)//if we found index value already in one of interior pages then break out of loop
          break;
        noOfRecordsInPage--;
        addOffset += 2;
      }
      if(pageType == 0x0A) {//after breaking out of loop if the page type is 0x0A then we have reached a leaf page where
                            //we either add new record or just add rowId to existing record
        file.seek(pageOffset+1);
        short recordCount = file.readShort();
        short matchedRecordOffset = 0;
        short addOffset = 0;
        short recordOffset = 0; short minRecordOffset = (short) Page.pageSize;
        boolean valueMatchFlag = false;
        while(recordCount > 0) {
          file.seek(pageOffset+16+addOffset);
          recordOffset = file.readShort();
          minRecordOffset = recordOffset < minRecordOffset ? recordOffset : minRecordOffset;
          file.seek(pageOffset+recordOffset+1);
          Byte recordLength = file.readByte();
          byte[] recordBytes = new byte[recordLength];
          file.seek(pageOffset+recordOffset+2);
          file.read(recordBytes);
          String recordValue = new String(recordBytes);
          if(indexValue.compareToIgnoreCase(recordValue) == 0) {
            matchedRecordOffset = recordOffset;
            valueMatchFlag = true;
          }
          recordCount--;
          addOffset += 2;
        }
        if(valueMatchFlag)//add rowId to existing record
          addRowIdInLeafPage(pageOffset, matchedRecordOffset, minRecordOffset, rowId);
        else {//add new record in index leaf page
          recordOffset = (short) (minRecordOffset-indexValue.length()-6);
          file.seek(pageOffset+recordOffset);
          file.write(1);
          file.seek(pageOffset+recordOffset+1);
          file.write(indexValue.length());
          file.seek(pageOffset+recordOffset+2);
          byte[] indexValueToByteArray = indexValue.getBytes();
          file.write(indexValueToByteArray);
          file.seek(pageOffset+recordOffset+indexValue.length()+2);
          file.writeInt(rowId);
          file.seek(pageOffset+3);
          file.writeShort(recordOffset);
          file.seek(pageOffset+16+(noOfRecordsInRootPage*2));
          file.writeShort(recordOffset);
          file.seek(pageOffset+1);
          file.writeShort(noOfRecordsInRootPage+1);
          file.seek(5);
          int pageNo = file.readInt();
          IndexPage.sortKeys(file, pageNo);
          if(noOfRecordsInRootPage+1 > 3)
            IndexPage.splitLeafPage(file, pageNo);
        }
      }
    }
  }

  //adding a record in Index page for Integer type column
  public void addInt(@Nullable Integer indexValue, int rowId) throws IOException {
    //locating root page    
    long rootPageOffset = rootPageOffset();
    file.seek(rootPageOffset);
    byte rootPageType = file.readByte();
    
    //reading no of records in root page
    short noOfRecordsInRootPage = noOfRecordsInPage(rootPageOffset);
    
    /*if noOfRecordsInRootPage is 0 then add record in rootPage
      else if it matches with root page record value, insert just the rowId
      else determine to which child it should check with
    */
    byte indexValueLength;
    indexValueLength = 4;
    if(noOfRecordsInRootPage == 0) {
      //writing data to page      
      short indexRecordOffseet = writeFirstRecordInRootPage(rootPageOffset, indexValueLength);
      file.seek(rootPageOffset+indexRecordOffseet+2);     
      file.writeInt(indexValue);//write index value to cell array
      file.seek(rootPageOffset+indexRecordOffseet+indexValueLength+2);
      file.writeInt(rowId);//write rowId value
      
      //updating header values
      updateRootPageHeaderData(rootPageOffset, indexRecordOffseet);
    }
    
    /*if root page type is still 0x0A that  means there are no children yet
      two cases here i) inserting rowId for already existing record
                    ii) Or adding a new record
     */
    else if(noOfRecordsInRootPage > 0 && rootPageType == 0x0A) {
      short recordCount = noOfRecordsInRootPage;
      short matchedRecordOffset = 0;
      short addOffset = 0;
      short recordOffset = 0; short minRecordOffset = (short) Page.pageSize;
      boolean valueMatchFlag = false;
      while(recordCount > 0) {
        file.seek(rootPageOffset+16+addOffset);
        recordOffset = file.readShort();
        minRecordOffset = recordOffset < minRecordOffset ? recordOffset : minRecordOffset;
        file.seek(rootPageOffset+recordOffset+2);
        int recordValue = file.readInt();        
        if(indexValue == recordValue) {
          valueMatchFlag = true;
          matchedRecordOffset = recordOffset;
        }
        recordCount--;
        addOffset += 2;
      }
      if(valueMatchFlag)//inserting rowId for already existing record 
        addRowIdInLeafPage(rootPageOffset, matchedRecordOffset, minRecordOffset, rowId);
      else {//adding a new record into Index page
        recordOffset = (short) (minRecordOffset-indexValueLength-6);
        file.seek(rootPageOffset+recordOffset);
        file.write(1);
        file.seek(rootPageOffset+recordOffset+1);
        file.write(indexValueLength);
        file.seek(rootPageOffset+recordOffset+2);        
        file.writeInt(indexValue);
        file.seek(rootPageOffset+recordOffset+indexValueLength+2);
        file.writeInt(rowId);
        file.seek(rootPageOffset+3);
        file.writeShort(recordOffset);
        file.seek(rootPageOffset+16+(noOfRecordsInRootPage*2));
        file.writeShort(recordOffset);
        file.seek(rootPageOffset+1);
        file.writeShort(noOfRecordsInRootPage+1);
        file.seek(5);
        int pageNo = file.readInt();
        IndexPage.sortKeys(file, pageNo);
        if(noOfRecordsInRootPage+1 > 3)
          IndexPage.splitLeafPage(file, pageNo);
      }
    }
    
    /*
     if root page type is not 0x0A then there are children in the tree
     if the index value is not already in the tree then we have to traverse the tree to find the leaf page to add the new record
     if the index value is already present then we have to check every page in the traverse path o insert the rowId
     */
    else {
      byte pageType = rootPageType;
      long pageOffset = rootPageOffset;
      while(pageType == 0x02) {//checking all interior pages in path
        short noOfRecordsInPage;
        file.seek(pageOffset+1);
        noOfRecordsInPage = file.readShort();
        
        short addOffset = 0;
        short recordOffset = 0; 
        boolean valueMatchFlag = false;
        while(noOfRecordsInPage > 0) {
          file.seek(pageOffset+16+addOffset);
          recordOffset = file.readShort();
          file.seek(pageOffset+recordOffset+6);
          int recordValue = file.readInt();          
          if(indexValue == recordValue) {//if index value matches with already existing value then add rowId
            addRowIdInInteriorPage(pageOffset, recordOffset, rowId);
            valueMatchFlag = true;
            break;
          }
          else if(indexValue > recordValue) {//if index value greater than record value move to next record on right
            if(noOfRecordsInPage-1 == 0) {//if there are no more records to compare with then move to right child page
              file.seek(pageOffset+6);
              int nextPageNo = file.readInt();
              pageOffset = (nextPageNo - 1) * Page.pageSize;
              file.seek(pageOffset);
              pageType = file.readByte();
              break;
            }
          }
          
          else {//if index value less than record value move to left record
            file.seek(pageOffset+recordOffset);
            int nextPageNo = file.readInt();
            pageOffset = (nextPageNo - 1) * Page.pageSize;
            file.seek(pageOffset);
            pageType = file.readByte();
            break;
          }
          
        }
        if(valueMatchFlag)//if we found index value already in one of interior pages then break out of loop
          break;
        noOfRecordsInPage--;
        addOffset += 2;
      }
      if(pageType == 0x0A) {//after breaking out of loop if the page type is 0x0A then we have reached a leaf page where
                            //we either add new record or just add rowId to existing record
        file.seek(pageOffset+1);
        short recordCount = file.readShort();
        short matchedRecordOffset = 0;
        short addOffset = 0;
        short recordOffset = 0; short minRecordOffset = (short) Page.pageSize;
        boolean valueMatchFlag = false;
        while(recordCount > 0) {
          file.seek(pageOffset+16+addOffset);
          recordOffset = file.readShort();
          minRecordOffset = recordOffset < minRecordOffset ? recordOffset : minRecordOffset;          
          file.seek(pageOffset+recordOffset+2);
          int recordValue = file.readInt();
          if(indexValue == recordValue) {
            matchedRecordOffset = recordOffset;
            valueMatchFlag = true;
          }
          recordCount--;
          addOffset += 2;
        }
        if(valueMatchFlag)//add rowId to existing record
          addRowIdInLeafPage(pageOffset, matchedRecordOffset, minRecordOffset, rowId);
        else {//add new record in index leaf page
          recordOffset = (short) (minRecordOffset-indexValueLength-6);
          file.seek(pageOffset+recordOffset);
          file.write(1);
          file.seek(pageOffset+recordOffset+1);
          file.write(indexValueLength);
          file.seek(pageOffset+recordOffset+2);          
          file.writeInt(indexValue);
          file.seek(pageOffset+recordOffset+indexValueLength+2);
          file.writeInt(rowId);
          file.seek(pageOffset+3);
          file.writeShort(recordOffset);
          file.seek(pageOffset+16+(noOfRecordsInRootPage*2));
          file.writeShort(recordOffset);
          file.seek(pageOffset+1);
          file.writeShort(noOfRecordsInRootPage+1);
          file.seek(5);
          int pageNo = file.readInt();
          IndexPage.sortKeys(file, pageNo);
          if(noOfRecordsInRootPage+1 > 3)
            IndexPage.splitLeafPage(file, pageNo);
        }
      }
    }
  }
  
  //adding a record in Index page for BigInt type column
  public void addBigInt(@Nullable Long indexValue, int rowId) throws IOException {
    //locating root page    
    long rootPageOffset = rootPageOffset();
    file.seek(rootPageOffset);
    byte rootPageType = file.readByte();
    
    //reading no of records in root page
    short noOfRecordsInRootPage = noOfRecordsInPage(rootPageOffset);
    
    /*if noOfRecordsInRootPage is 0 then add record in rootPage
      else if it matches with root page record value, insert just the rowId
      else determine to which child it should check with
    */
    byte indexValueLength;
    indexValueLength = 8;
    if(noOfRecordsInRootPage == 0) {
      //writing data to page      
      short indexRecordOffseet = writeFirstRecordInRootPage(rootPageOffset, indexValueLength);
      file.seek(rootPageOffset+indexRecordOffseet+2);     
      file.writeLong(indexValue);//write index value to cell array
      file.seek(rootPageOffset+indexRecordOffseet+indexValueLength+2);
      file.writeInt(rowId);//write rowId value
      
      //updating header values
      updateRootPageHeaderData(rootPageOffset, indexRecordOffseet);
    }
    
    /*if root page type is still 0x0A that  means there are no children yet
      two cases here i) inserting rowId for already existing record
                    ii) Or adding a new record
     */
    else if(noOfRecordsInRootPage > 0 && rootPageType == 0x0A) {
      short recordCount = noOfRecordsInRootPage;
      short matchedRecordOffset = 0;
      short addOffset = 0;
      short recordOffset = 0; short minRecordOffset = (short) Page.pageSize;
      boolean valueMatchFlag = false;
      while(recordCount > 0) {
        file.seek(rootPageOffset+16+addOffset);
        recordOffset = file.readShort();
        minRecordOffset = recordOffset < minRecordOffset ? recordOffset : minRecordOffset;
        file.seek(rootPageOffset+recordOffset+2);
        long recordValue = file.readLong();        
        if(indexValue == recordValue) {
          valueMatchFlag = true;
          matchedRecordOffset = recordOffset;
        }
        recordCount--;
        addOffset += 2;
      }
      if(valueMatchFlag)//inserting rowId for already existing record 
        addRowIdInLeafPage(rootPageOffset, matchedRecordOffset, minRecordOffset, rowId);
      else {//adding a new record into Index page
        recordOffset = (short) (minRecordOffset-indexValueLength-6);
        file.seek(rootPageOffset+recordOffset);
        file.write(1);
        file.seek(rootPageOffset+recordOffset+1);
        file.write(indexValueLength);
        file.seek(rootPageOffset+recordOffset+2);        
        file.writeLong(indexValue);
        file.seek(rootPageOffset+recordOffset+indexValueLength+2);
        file.writeInt(rowId);
        file.seek(rootPageOffset+3);
        file.writeShort(recordOffset);
        file.seek(rootPageOffset+16+(noOfRecordsInRootPage*2));
        file.writeShort(recordOffset);
        file.seek(rootPageOffset+1);
        file.writeShort(noOfRecordsInRootPage+1);
        file.seek(5);
        int pageNo = file.readInt();
        IndexPage.sortKeys(file, pageNo);
        if(noOfRecordsInRootPage+1 > 3)
          IndexPage.splitLeafPage(file, pageNo);
      }
    }
    
    /*
     if root page type is not 0x0A then there are children in the tree
     if the index value is not already in the tree then we have to traverse the tree to find the leaf page to add the new record
     if the index value is already present then we have to check every page in the traverse path o insert the rowId
     */
    else {
      byte pageType = rootPageType;
      long pageOffset = rootPageOffset;
      while(pageType == 0x02) {//checking all interior pages in path
        short noOfRecordsInPage;
        file.seek(pageOffset+1);
        noOfRecordsInPage = file.readShort();
        
        short addOffset = 0;
        short recordOffset = 0; 
        boolean valueMatchFlag = false;
        while(noOfRecordsInPage > 0) {
          file.seek(pageOffset+16+addOffset);
          recordOffset = file.readShort();
          file.seek(pageOffset+recordOffset+6);
          long recordValue = file.readLong();          
          if(indexValue == recordValue) {//if index value matches with already existing value then add rowId
            addRowIdInInteriorPage(pageOffset, recordOffset, rowId);
            valueMatchFlag = true;
            break;
          }
          else if(indexValue > recordValue) {//if index value greater than record value move to next record on right
            if(noOfRecordsInPage-1 == 0) {//if there are no more records to compare with then move to right child page
              file.seek(pageOffset+6);
              int nextPageNo = file.readInt();
              pageOffset = (nextPageNo - 1) * Page.pageSize;
              file.seek(pageOffset);
              pageType = file.readByte();
              break;
            }
          }
          
          else {//if index value less than record value move to left record
            file.seek(pageOffset+recordOffset);
            int nextPageNo = file.readInt();
            pageOffset = (nextPageNo - 1) * Page.pageSize;
            file.seek(pageOffset);
            pageType = file.readByte();
            break;
          }
          
        }
        if(valueMatchFlag)//if we found index value already in one of interior pages then break out of loop
          break;
        noOfRecordsInPage--;
        addOffset += 2;
      }
      if(pageType == 0x0A) {//after breaking out of loop if the page type is 0x0A then we have reached a leaf page where
                            //we either add new record or just add rowId to existing record
        file.seek(pageOffset+1);
        short recordCount = file.readShort();
        short matchedRecordOffset = 0;
        short addOffset = 0;
        short recordOffset = 0; short minRecordOffset = (short) Page.pageSize;
        boolean valueMatchFlag = false;
        while(recordCount > 0) {
          file.seek(pageOffset+16+addOffset);
          recordOffset = file.readShort();
          minRecordOffset = recordOffset < minRecordOffset ? recordOffset : minRecordOffset;          
          file.seek(pageOffset+recordOffset+2);
          long recordValue = file.readLong();
          if(indexValue == recordValue) {
            matchedRecordOffset = recordOffset;
            valueMatchFlag = true;
          }
          recordCount--;
          addOffset += 2;
        }
        if(valueMatchFlag)//add rowId to existing record
          addRowIdInLeafPage(pageOffset, matchedRecordOffset, minRecordOffset, rowId);
        else {//add new record in index leaf page
          recordOffset = (short) (minRecordOffset-indexValueLength-6);
          file.seek(pageOffset+recordOffset);
          file.write(1);
          file.seek(pageOffset+recordOffset+1);
          file.write(indexValueLength);
          file.seek(pageOffset+recordOffset+2);          
          file.writeLong(indexValue);
          file.seek(pageOffset+recordOffset+indexValueLength+2);
          file.writeInt(rowId);
          file.seek(pageOffset+3);
          file.writeShort(recordOffset);
          file.seek(pageOffset+16+(noOfRecordsInRootPage*2));
          file.writeShort(recordOffset);
          file.seek(pageOffset+1);
          file.writeShort(noOfRecordsInRootPage+1);
          file.seek(5);
          int pageNo = file.readInt();
          IndexPage.sortKeys(file, pageNo);
          if(noOfRecordsInRootPage+1 > 3)
            IndexPage.splitLeafPage(file, pageNo);
        }
      }
    }
  }
  
  //adding a record in Index page for Double type column
  public void addDouble(@Nullable Double indexValue, int rowId) throws IOException {
    //locating root page    
    long rootPageOffset = rootPageOffset();
    file.seek(rootPageOffset);
    byte rootPageType = file.readByte();
    
    //reading no of records in root page
    short noOfRecordsInRootPage = noOfRecordsInPage(rootPageOffset);
    
    /*if noOfRecordsInRootPage is 0 then add record in rootPage
      else if it matches with root page record value, insert just the rowId
      else determine to which child it should check with
    */
    byte indexValueLength;
    indexValueLength = 8;
    if(noOfRecordsInRootPage == 0) {
      //writing data to page      
      short indexRecordOffseet = writeFirstRecordInRootPage(rootPageOffset, indexValueLength);
      file.seek(rootPageOffset+indexRecordOffseet+2);     
      file.writeDouble(indexValue);//write index value to cell array
      file.seek(rootPageOffset+indexRecordOffseet+indexValueLength+2);
      file.writeInt(rowId);//write rowId value
      
      //updating header values
      updateRootPageHeaderData(rootPageOffset, indexRecordOffseet);
    }
    
    /*if root page type is still 0x0A that  means there are no children yet
      two cases here i) inserting rowId for already existing record
                    ii) Or adding a new record
     */
    else if(noOfRecordsInRootPage > 0 && rootPageType == 0x0A) {
      short recordCount = noOfRecordsInRootPage;
      short matchedRecordOffset = 0;
      short addOffset = 0;
      short recordOffset = 0; short minRecordOffset = (short) Page.pageSize;
      boolean valueMatchFlag = false;
      while(recordCount > 0) {
        file.seek(rootPageOffset+16+addOffset);
        recordOffset = file.readShort();
        minRecordOffset = recordOffset < minRecordOffset ? recordOffset : minRecordOffset;
        file.seek(rootPageOffset+recordOffset+2);
        double recordValue = file.readDouble();        
        if(indexValue == recordValue) {
          valueMatchFlag = true;
          matchedRecordOffset = recordOffset;
        }
        recordCount--;
        addOffset += 2;
      }
      if(valueMatchFlag)//inserting rowId for already existing record 
        addRowIdInLeafPage(rootPageOffset, matchedRecordOffset, minRecordOffset, rowId);
      else {//adding a new record into Index page
        recordOffset = (short) (minRecordOffset-indexValueLength-6);
        file.seek(rootPageOffset+recordOffset);
        file.write(1);
        file.seek(rootPageOffset+recordOffset+1);
        file.write(indexValueLength);
        file.seek(rootPageOffset+recordOffset+2);        
        file.writeDouble(indexValue);
        file.seek(rootPageOffset+recordOffset+indexValueLength+2);
        file.writeInt(rowId);
        file.seek(rootPageOffset+3);
        file.writeShort(recordOffset);
        file.seek(rootPageOffset+16+(noOfRecordsInRootPage*2));
        file.writeShort(recordOffset);
        file.seek(rootPageOffset+1);
        file.writeShort(noOfRecordsInRootPage+1);
        file.seek(5);
        int pageNo = file.readInt();
        IndexPage.sortKeys(file, pageNo);
        if(noOfRecordsInRootPage+1 > 3)
          IndexPage.splitLeafPage(file, pageNo);
      }
    }
    
    /*
     if root page type is not 0x0A then there are children in the tree
     if the index value is not already in the tree then we have to traverse the tree to find the leaf page to add the new record
     if the index value is already present then we have to check every page in the traverse path o insert the rowId
     */
    else {
      byte pageType = rootPageType;
      long pageOffset = rootPageOffset;
      while(pageType == 0x02) {//checking all interior pages in path
        short noOfRecordsInPage;
        file.seek(pageOffset+1);
        noOfRecordsInPage = file.readShort();
        
        short addOffset = 0;
        short recordOffset = 0; 
        boolean valueMatchFlag = false;
        while(noOfRecordsInPage > 0) {
          file.seek(pageOffset+16+addOffset);
          recordOffset = file.readShort();
          file.seek(pageOffset+recordOffset+6);
          double recordValue = file.readDouble();          
          if(indexValue == recordValue) {//if index value matches with already existing value then add rowId
            addRowIdInInteriorPage(pageOffset, recordOffset, rowId);
            valueMatchFlag = true;
            break;
          }
          else if(indexValue > recordValue) {//if index value greater than record value move to next record on right
            if(noOfRecordsInPage-1 == 0) {//if there are no more records to compare with then move to right child page
              file.seek(pageOffset+6);
              int nextPageNo = file.readInt();
              pageOffset = (nextPageNo - 1) * Page.pageSize;
              file.seek(pageOffset);
              pageType = file.readByte();
              break;
            }
          }
          
          else {//if index value less than record value move to left record
            file.seek(pageOffset+recordOffset);
            int nextPageNo = file.readInt();
            pageOffset = (nextPageNo - 1) * Page.pageSize;
            file.seek(pageOffset);
            pageType = file.readByte();
            break;
          }
          
        }
        if(valueMatchFlag)//if we found index value already in one of interior pages then break out of loop
          break;
        noOfRecordsInPage--;
        addOffset += 2;
      }
      if(pageType == 0x0A) {//after breaking out of loop if the page type is 0x0A then we have reached a leaf page where
                            //we either add new record or just add rowId to existing record
        file.seek(pageOffset+1);
        short recordCount = file.readShort();
        short matchedRecordOffset = 0;
        short addOffset = 0;
        short recordOffset = 0; short minRecordOffset = (short) Page.pageSize;
        boolean valueMatchFlag = false;
        while(recordCount > 0) {
          file.seek(pageOffset+16+addOffset);
          recordOffset = file.readShort();
          minRecordOffset = recordOffset < minRecordOffset ? recordOffset : minRecordOffset;          
          file.seek(pageOffset+recordOffset+2);
          double recordValue = file.readDouble();
          if(indexValue == recordValue) {
            matchedRecordOffset = recordOffset;
            valueMatchFlag = true;
          }
          recordCount--;
          addOffset += 2;
        }
        if(valueMatchFlag)//add rowId to existing record
          addRowIdInLeafPage(pageOffset, matchedRecordOffset, minRecordOffset, rowId);
        else {//add new record in index leaf page
          recordOffset = (short) (minRecordOffset-indexValueLength-6);
          file.seek(pageOffset+recordOffset);
          file.write(1);
          file.seek(pageOffset+recordOffset+1);
          file.write(indexValueLength);
          file.seek(pageOffset+recordOffset+2);          
          file.writeDouble(indexValue);
          file.seek(pageOffset+recordOffset+indexValueLength+2);
          file.writeInt(rowId);
          file.seek(pageOffset+3);
          file.writeShort(recordOffset);
          file.seek(pageOffset+16+(noOfRecordsInRootPage*2));
          file.writeShort(recordOffset);
          file.seek(pageOffset+1);
          file.writeShort(noOfRecordsInRootPage+1);
          file.seek(5);
          int pageNo = file.readInt();
          IndexPage.sortKeys(file, pageNo);
          if(noOfRecordsInRootPage+1 > 3)
            IndexPage.splitLeafPage(file, pageNo);
        }
      }
    }
  }
  
  //adding a record in Index page for Float type column
  public void addFloat(@Nullable Float indexValue, int rowId) throws IOException {
    //locating root page    
    long rootPageOffset = rootPageOffset();
    file.seek(rootPageOffset);
    byte rootPageType = file.readByte();
    
    //reading no of records in root page
    short noOfRecordsInRootPage = noOfRecordsInPage(rootPageOffset);
    
    /*if noOfRecordsInRootPage is 0 then add record in rootPage
      else if it matches with root page record value, insert just the rowId
      else determine to which child it should check with
    */
    byte indexValueLength;
    indexValueLength = 4;
    if(noOfRecordsInRootPage == 0) {
      //writing data to page      
      short indexRecordOffseet = writeFirstRecordInRootPage(rootPageOffset, indexValueLength);
      file.seek(rootPageOffset+indexRecordOffseet+2);     
      file.writeFloat(indexValue);//write index value to cell array
      file.seek(rootPageOffset+indexRecordOffseet+indexValueLength+2);
      file.writeInt(rowId);//write rowId value
      
      //updating header values
      updateRootPageHeaderData(rootPageOffset, indexRecordOffseet);
    }
    
    /*if root page type is still 0x0A that  means there are no children yet
      two cases here i) inserting rowId for already existing record
                    ii) Or adding a new record
     */
    else if(noOfRecordsInRootPage > 0 && rootPageType == 0x0A) {
      short recordCount = noOfRecordsInRootPage;
      short matchedRecordOffset = 0;
      short addOffset = 0;
      short recordOffset = 0; short minRecordOffset = (short) Page.pageSize;
      boolean valueMatchFlag = false;
      while(recordCount > 0) {
        file.seek(rootPageOffset+16+addOffset);
        recordOffset = file.readShort();
        minRecordOffset = recordOffset < minRecordOffset ? recordOffset : minRecordOffset;
        file.seek(rootPageOffset+recordOffset+2);
        float recordValue = file.readFloat();        
        if(indexValue == recordValue) {
          valueMatchFlag = true;
          matchedRecordOffset = recordOffset;
        }
        recordCount--;
        addOffset += 2;
      }
      if(valueMatchFlag)//inserting rowId for already existing record 
        addRowIdInLeafPage(rootPageOffset, matchedRecordOffset, minRecordOffset, rowId);
      else {//adding a new record into Index page
        recordOffset = (short) (minRecordOffset-indexValueLength-6);
        file.seek(rootPageOffset+recordOffset);
        file.write(1);
        file.seek(rootPageOffset+recordOffset+1);
        file.write(indexValueLength);
        file.seek(rootPageOffset+recordOffset+2);        
        file.writeFloat(indexValue);
        file.seek(rootPageOffset+recordOffset+indexValueLength+2);
        file.writeInt(rowId);
        file.seek(rootPageOffset+3);
        file.writeShort(recordOffset);
        file.seek(rootPageOffset+16+(noOfRecordsInRootPage*2));
        file.writeShort(recordOffset);
        file.seek(rootPageOffset+1);
        file.writeShort(noOfRecordsInRootPage+1);
        file.seek(5);
        int pageNo = file.readInt();
        IndexPage.sortKeys(file, pageNo);
        if(noOfRecordsInRootPage+1 > 3)
          IndexPage.splitLeafPage(file, pageNo);
      }
    }
    
    /*
     if root page type is not 0x0A then there are children in the tree
     if the index value is not already in the tree then we have to traverse the tree to find the leaf page to add the new record
     if the index value is already present then we have to check every page in the traverse path o insert the rowId
     */
    else {
      byte pageType = rootPageType;
      long pageOffset = rootPageOffset;
      while(pageType == 0x02) {//checking all interior pages in path
        short noOfRecordsInPage;
        file.seek(pageOffset+1);
        noOfRecordsInPage = file.readShort();
        
        short addOffset = 0;
        short recordOffset = 0; 
        boolean valueMatchFlag = false;
        while(noOfRecordsInPage > 0) {
          file.seek(pageOffset+16+addOffset);
          recordOffset = file.readShort();
          file.seek(pageOffset+recordOffset+6);
          float recordValue = file.readFloat();          
          if(indexValue == recordValue) {//if index value matches with already existing value then add rowId
            addRowIdInInteriorPage(pageOffset, recordOffset, rowId);
            valueMatchFlag = true;
            break;
          }
          else if(indexValue > recordValue) {//if index value greater than record value move to next record on right
            if(noOfRecordsInPage-1 == 0) {//if there are no more records to compare with then move to right child page
              file.seek(pageOffset+6);
              int nextPageNo = file.readInt();
              pageOffset = (nextPageNo - 1) * Page.pageSize;
              file.seek(pageOffset);
              pageType = file.readByte();
              break;
            }
          }
          
          else {//if index value less than record value move to left record
            file.seek(pageOffset+recordOffset);
            int nextPageNo = file.readInt();
            pageOffset = (nextPageNo - 1) * Page.pageSize;
            file.seek(pageOffset);
            pageType = file.readByte();
            break;
          }
          
        }
        if(valueMatchFlag)//if we found index value already in one of interior pages then break out of loop
          break;
        noOfRecordsInPage--;
        addOffset += 2;
      }
      if(pageType == 0x0A) {//after breaking out of loop if the page type is 0x0A then we have reached a leaf page where
                            //we either add new record or just add rowId to existing record
        file.seek(pageOffset+1);
        short recordCount = file.readShort();
        short matchedRecordOffset = 0;
        short addOffset = 0;
        short recordOffset = 0; short minRecordOffset = (short) Page.pageSize;
        boolean valueMatchFlag = false;
        while(recordCount > 0) {
          file.seek(pageOffset+16+addOffset);
          recordOffset = file.readShort();
          minRecordOffset = recordOffset < minRecordOffset ? recordOffset : minRecordOffset;          
          file.seek(pageOffset+recordOffset+2);
          float recordValue = file.readFloat();
          if(indexValue == recordValue) {
            matchedRecordOffset = recordOffset;
            valueMatchFlag = true;
          }
          recordCount--;
          addOffset += 2;
        }
        if(valueMatchFlag)//add rowId to existing record
          addRowIdInLeafPage(pageOffset, matchedRecordOffset, minRecordOffset, rowId);
        else {//add new record in index leaf page
          recordOffset = (short) (minRecordOffset-indexValueLength-6);
          file.seek(pageOffset+recordOffset);
          file.write(1);
          file.seek(pageOffset+recordOffset+1);
          file.write(indexValueLength);
          file.seek(pageOffset+recordOffset+2);          
          file.writeFloat(indexValue);
          file.seek(pageOffset+recordOffset+indexValueLength+2);
          file.writeInt(rowId);
          file.seek(pageOffset+3);
          file.writeShort(recordOffset);
          file.seek(pageOffset+16+(noOfRecordsInRootPage*2));
          file.writeShort(recordOffset);
          file.seek(pageOffset+1);
          file.writeShort(noOfRecordsInRootPage+1);
          file.seek(5);
          int pageNo = file.readInt();
          IndexPage.sortKeys(file, pageNo);
          if(noOfRecordsInRootPage+1 > 3)
            IndexPage.splitLeafPage(file, pageNo);
        }
      }
    }
  }
  
  //adding a record in Index page for SmallInt type column
  public void addSmallInt(@Nullable Short indexValue, int rowId) throws IOException {
    //locating root page    
    long rootPageOffset = rootPageOffset();
    file.seek(rootPageOffset);
    byte rootPageType = file.readByte();
    
    //reading no of records in root page
    short noOfRecordsInRootPage = noOfRecordsInPage(rootPageOffset);
    
    /*if noOfRecordsInRootPage is 0 then add record in rootPage
      else if it matches with root page record value, insert just the rowId
      else determine to which child it should check with
    */
    byte indexValueLength;
    indexValueLength = 2;
    if(noOfRecordsInRootPage == 0) {
      //writing data to page      
      short indexRecordOffseet = writeFirstRecordInRootPage(rootPageOffset, indexValueLength);
      file.seek(rootPageOffset+indexRecordOffseet+2);     
      file.writeShort(indexValue);//write index value to cell array
      file.seek(rootPageOffset+indexRecordOffseet+indexValueLength+2);
      file.writeInt(rowId);//write rowId value
      
      //updating header values
      updateRootPageHeaderData(rootPageOffset, indexRecordOffseet);
    }
    
    /*if root page type is still 0x0A that  means there are no children yet
      two cases here i) inserting rowId for already existing record
                    ii) Or adding a new record
     */
    else if(noOfRecordsInRootPage > 0 && rootPageType == 0x0A) {
      short recordCount = noOfRecordsInRootPage;
      short matchedRecordOffset = 0;
      short addOffset = 0;
      short recordOffset = 0; short minRecordOffset = (short) Page.pageSize;
      boolean valueMatchFlag = false;
      while(recordCount > 0) {
        file.seek(rootPageOffset+16+addOffset);
        recordOffset = file.readShort();
        minRecordOffset = recordOffset < minRecordOffset ? recordOffset : minRecordOffset;
        file.seek(rootPageOffset+recordOffset+2);
        short recordValue = file.readShort();        
        if(indexValue == recordValue) {
          valueMatchFlag = true;
          matchedRecordOffset = recordOffset;
        }
        recordCount--;
        addOffset += 2;
      }
      if(valueMatchFlag)//inserting rowId for already existing record 
        addRowIdInLeafPage(rootPageOffset, matchedRecordOffset, minRecordOffset, rowId);
      else {//adding a new record into Index page
        recordOffset = (short) (minRecordOffset-indexValueLength-6);
        file.seek(rootPageOffset+recordOffset);
        file.write(1);
        file.seek(rootPageOffset+recordOffset+1);
        file.write(indexValueLength);
        file.seek(rootPageOffset+recordOffset+2);        
        file.writeShort(indexValue);
        file.seek(rootPageOffset+recordOffset+indexValueLength+2);
        file.writeInt(rowId);
        file.seek(rootPageOffset+3);
        file.writeShort(recordOffset);
        file.seek(rootPageOffset+16+(noOfRecordsInRootPage*2));
        file.writeShort(recordOffset);
        file.seek(rootPageOffset+1);
        file.writeShort(noOfRecordsInRootPage+1);
        file.seek(5);
        int pageNo = file.readInt();
        IndexPage.sortKeys(file, pageNo);
        if(noOfRecordsInRootPage+1 > 3)
          IndexPage.splitLeafPage(file, pageNo);
      }
    }
    
    /*
     if root page type is not 0x0A then there are children in the tree
     if the index value is not already in the tree then we have to traverse the tree to find the leaf page to add the new record
     if the index value is already present then we have to check every page in the traverse path o insert the rowId
     */
    else {
      byte pageType = rootPageType;
      long pageOffset = rootPageOffset;
      while(pageType == 0x02) {//checking all interior pages in path
        short noOfRecordsInPage;
        file.seek(pageOffset+1);
        noOfRecordsInPage = file.readShort();
        
        short addOffset = 0;
        short recordOffset = 0; 
        boolean valueMatchFlag = false;
        while(noOfRecordsInPage > 0) {
          file.seek(pageOffset+16+addOffset);
          recordOffset = file.readShort();
          file.seek(pageOffset+recordOffset+6);
          short recordValue = file.readShort();          
          if(indexValue == recordValue) {//if index value matches with already existing value then add rowId
            addRowIdInInteriorPage(pageOffset, recordOffset, rowId);
            valueMatchFlag = true;
            break;
          }
          else if(indexValue > recordValue) {//if index value greater than record value move to next record on right
            if(noOfRecordsInPage-1 == 0) {//if there are no more records to compare with then move to right child page
              file.seek(pageOffset+6);
              int nextPageNo = file.readInt();
              pageOffset = (nextPageNo - 1) * Page.pageSize;
              file.seek(pageOffset);
              pageType = file.readByte();
              break;
            }
          }
          
          else {//if index value less than record value move to left record
            file.seek(pageOffset+recordOffset);
            int nextPageNo = file.readInt();
            pageOffset = (nextPageNo - 1) * Page.pageSize;
            file.seek(pageOffset);
            pageType = file.readByte();
            break;
          }
          
        }
        if(valueMatchFlag)//if we found index value already in one of interior pages then break out of loop
          break;
        noOfRecordsInPage--;
        addOffset += 2;
      }
      if(pageType == 0x0A) {//after breaking out of loop if the page type is 0x0A then we have reached a leaf page where
                            //we either add new record or just add rowId to existing record
        file.seek(pageOffset+1);
        short recordCount = file.readShort();
        short matchedRecordOffset = 0;
        short addOffset = 0;
        short recordOffset = 0; short minRecordOffset = (short) Page.pageSize;
        boolean valueMatchFlag = false;
        while(recordCount > 0) {
          file.seek(pageOffset+16+addOffset);
          recordOffset = file.readShort();
          minRecordOffset = recordOffset < minRecordOffset ? recordOffset : minRecordOffset;          
          file.seek(pageOffset+recordOffset+2);
          short recordValue = file.readShort();
          if(indexValue == recordValue) {
            matchedRecordOffset = recordOffset;
            valueMatchFlag = true;
          }
          recordCount--;
          addOffset += 2;
        }
        if(valueMatchFlag)//add rowId to existing record
          addRowIdInLeafPage(pageOffset, matchedRecordOffset, minRecordOffset, rowId);
        else {//add new record in index leaf page
          recordOffset = (short) (minRecordOffset-indexValueLength-6);
          file.seek(pageOffset+recordOffset);
          file.write(1);
          file.seek(pageOffset+recordOffset+1);
          file.write(indexValueLength);
          file.seek(pageOffset+recordOffset+2);          
          file.writeShort(indexValue);
          file.seek(pageOffset+recordOffset+indexValueLength+2);
          file.writeInt(rowId);
          file.seek(pageOffset+3);
          file.writeShort(recordOffset);
          file.seek(pageOffset+16+(noOfRecordsInRootPage*2));
          file.writeShort(recordOffset);
          file.seek(pageOffset+1);
          file.writeShort(noOfRecordsInRootPage+1);
          file.seek(5);
          int pageNo = file.readInt();
          IndexPage.sortKeys(file, pageNo);
          if(noOfRecordsInRootPage+1 > 3)
            IndexPage.splitLeafPage(file, pageNo);
        }
      }
    }
  }
  
  //adding a record in Index page for TinyInt type column
  public void addTinyInt(@Nullable Byte indexValue, int rowId) throws IOException {
    //locating root page    
    long rootPageOffset = rootPageOffset();
    file.seek(rootPageOffset);
    byte rootPageType = file.readByte();
    
    //reading no of records in root page
    short noOfRecordsInRootPage = noOfRecordsInPage(rootPageOffset);
    
    /*if noOfRecordsInRootPage is 0 then add record in rootPage
      else if it matches with root page record value, insert just the rowId
      else determine to which child it should check with
    */
    byte indexValueLength;
    indexValueLength = 1;
    if(noOfRecordsInRootPage == 0) {
      //writing data to page      
      short indexRecordOffseet = writeFirstRecordInRootPage(rootPageOffset, indexValueLength);
      file.seek(rootPageOffset+indexRecordOffseet+2);     
      file.writeByte(indexValue);//write index value to cell array
      file.seek(rootPageOffset+indexRecordOffseet+indexValueLength+2);
      file.writeInt(rowId);//write rowId value
      
      //updating header values
      updateRootPageHeaderData(rootPageOffset, indexRecordOffseet);
    }
    
    /*if root page type is still 0x0A that  means there are no children yet
      two cases here i) inserting rowId for already existing record
                    ii) Or adding a new record
     */
    else if(noOfRecordsInRootPage > 0 && rootPageType == 0x0A) {
      short recordCount = noOfRecordsInRootPage;
      short matchedRecordOffset = 0;
      short addOffset = 0;
      short recordOffset = 0; short minRecordOffset = (short) Page.pageSize;
      boolean valueMatchFlag = false;
      while(recordCount > 0) {
        file.seek(rootPageOffset+16+addOffset);
        recordOffset = file.readShort();
        minRecordOffset = recordOffset < minRecordOffset ? recordOffset : minRecordOffset;
        file.seek(rootPageOffset+recordOffset+2);
        byte recordValue = file.readByte();        
        if(indexValue == recordValue) {
          valueMatchFlag = true;
          matchedRecordOffset = recordOffset;
        }
        recordCount--;
        addOffset += 2;
      }
      if(valueMatchFlag)//inserting rowId for already existing record 
        addRowIdInLeafPage(rootPageOffset, matchedRecordOffset, minRecordOffset, rowId);
      else {//adding a new record into Index page
        recordOffset = (short) (minRecordOffset-indexValueLength-6);
        file.seek(rootPageOffset+recordOffset);
        file.write(1);
        file.seek(rootPageOffset+recordOffset+1);
        file.write(indexValueLength);
        file.seek(rootPageOffset+recordOffset+2);        
        file.writeByte(indexValue);
        file.seek(rootPageOffset+recordOffset+indexValueLength+2);
        file.writeInt(rowId);
        file.seek(rootPageOffset+3);
        file.writeShort(recordOffset);
        file.seek(rootPageOffset+16+(noOfRecordsInRootPage*2));
        file.writeShort(recordOffset);
        file.seek(rootPageOffset+1);
        file.writeShort(noOfRecordsInRootPage+1);
        file.seek(5);
        int pageNo = file.readInt();
        IndexPage.sortKeys(file, pageNo);
        if(noOfRecordsInRootPage+1 > 3)
          IndexPage.splitLeafPage(file, pageNo);
      }
    }
    
    /*
     if root page type is not 0x0A then there are children in the tree
     if the index value is not already in the tree then we have to traverse the tree to find the leaf page to add the new record
     if the index value is already present then we have to check every page in the traverse path o insert the rowId
     */
    else {
      byte pageType = rootPageType;
      long pageOffset = rootPageOffset;
      while(pageType == 0x02) {//checking all interior pages in path
        short noOfRecordsInPage;
        file.seek(pageOffset+1);
        noOfRecordsInPage = file.readShort();
        
        short addOffset = 0;
        short recordOffset = 0; 
        boolean valueMatchFlag = false;
        while(noOfRecordsInPage > 0) {
          file.seek(pageOffset+16+addOffset);
          recordOffset = file.readShort();
          file.seek(pageOffset+recordOffset+6);
          byte recordValue = file.readByte();          
          if(indexValue == recordValue) {//if index value matches with already existing value then add rowId
            addRowIdInInteriorPage(pageOffset, recordOffset, rowId);
            valueMatchFlag = true;
            break;
          }
          else if(indexValue > recordValue) {//if index value greater than record value move to next record on right
            if(noOfRecordsInPage-1 == 0) {//if there are no more records to compare with then move to right child page
              file.seek(pageOffset+6);
              int nextPageNo = file.readInt();
              pageOffset = (nextPageNo - 1) * Page.pageSize;
              file.seek(pageOffset);
              pageType = file.readByte();
              break;
            }
          }
          
          else {//if index value less than record value move to left record
            file.seek(pageOffset+recordOffset);
            int nextPageNo = file.readInt();
            pageOffset = (nextPageNo - 1) * Page.pageSize;
            file.seek(pageOffset);
            pageType = file.readByte();
            break;
          }
          
        }
        if(valueMatchFlag)//if we found index value already in one of interior pages then break out of loop
          break;
        noOfRecordsInPage--;
        addOffset += 2;
      }
      if(pageType == 0x0A) {//after breaking out of loop if the page type is 0x0A then we have reached a leaf page where
                            //we either add new record or just add rowId to existing record
        file.seek(pageOffset+1);
        short recordCount = file.readShort();
        short matchedRecordOffset = 0;
        short addOffset = 0;
        short recordOffset = 0; short minRecordOffset = (short) Page.pageSize;
        boolean valueMatchFlag = false;
        while(recordCount > 0) {
          file.seek(pageOffset+16+addOffset);
          recordOffset = file.readShort();
          minRecordOffset = recordOffset < minRecordOffset ? recordOffset : minRecordOffset;          
          file.seek(pageOffset+recordOffset+2);
          byte recordValue = file.readByte();
          if(indexValue == recordValue) {
            matchedRecordOffset = recordOffset;
            valueMatchFlag = true;
          }
          recordCount--;
          addOffset += 2;
        }
        if(valueMatchFlag)//add rowId to existing record
          addRowIdInLeafPage(pageOffset, matchedRecordOffset, minRecordOffset, rowId);
        else {//add new record in index leaf page
          recordOffset = (short) (minRecordOffset-indexValueLength-6);
          file.seek(pageOffset+recordOffset);
          file.write(1);
          file.seek(pageOffset+recordOffset+1);
          file.write(indexValueLength);
          file.seek(pageOffset+recordOffset+2);          
          file.writeByte(indexValue);
          file.seek(pageOffset+recordOffset+indexValueLength+2);
          file.writeInt(rowId);
          file.seek(pageOffset+3);
          file.writeShort(recordOffset);
          file.seek(pageOffset+16+(noOfRecordsInRootPage*2));
          file.writeShort(recordOffset);
          file.seek(pageOffset+1);
          file.writeShort(noOfRecordsInRootPage+1);
          file.seek(5);
          int pageNo = file.readInt();
          IndexPage.sortKeys(file, pageNo);
          if(noOfRecordsInRootPage+1 > 3)
            IndexPage.splitLeafPage(file, pageNo);
        }
      }
    }
  }
  
  //adding a record in Index page for DateTime type column
  public void addDateTime(@Nullable LocalDateTime indexValue, int rowId) throws IOException {
    //locating root page    
    long rootPageOffset = rootPageOffset();
    file.seek(rootPageOffset);
    byte rootPageType = file.readByte();
    
    //reading no of records in root page
    short noOfRecordsInRootPage = noOfRecordsInPage(rootPageOffset);
    
    /*if noOfRecordsInRootPage is 0 then add record in rootPage
      else if it matches with root page record value, insert just the rowId
      else determine to which child it should check with
    */
    byte indexValueLength;
    indexValueLength = 8;
    if(noOfRecordsInRootPage == 0) {
      //writing data to page      
      short indexRecordOffseet = writeFirstRecordInRootPage(rootPageOffset, indexValueLength);
      file.seek(rootPageOffset+indexRecordOffseet+2);
      byte[] indexValueToByteArray = ByteBuffer.allocate(8).putLong((long) indexValue.toEpochSecond(ZoneOffset.UTC)).array();
      file.write(indexValueToByteArray);//write index value to cell array
      file.seek(rootPageOffset+indexRecordOffseet+indexValueLength+2);
      file.writeInt(rowId);//write rowId value
      
      //updating header values
      updateRootPageHeaderData(rootPageOffset, indexRecordOffseet);
    }
    
    /*if root page type is still 0x0A that  means there are no children yet
      two cases here i) inserting rowId for already existing record
                    ii) Or adding a new record
     */
    else if(noOfRecordsInRootPage > 0 && rootPageType == 0x0A) {
      short recordCount = noOfRecordsInRootPage;
      short matchedRecordOffset = 0;
      short addOffset = 0;
      short recordOffset = 0; short minRecordOffset = (short) Page.pageSize;
      boolean valueMatchFlag = false;
      while(recordCount > 0) {
        file.seek(rootPageOffset+16+addOffset);
        recordOffset = file.readShort();
        minRecordOffset = recordOffset < minRecordOffset ? recordOffset : minRecordOffset;
        file.seek(rootPageOffset+recordOffset+2);
        LocalDateTime recordValue = LocalDateTime.ofEpochSecond(file.readLong(), 0, ZoneOffset.UTC);       
        if(indexValue.compareTo(recordValue) == 0) {
          valueMatchFlag = true;
          matchedRecordOffset = recordOffset;
        }
        recordCount--;
        addOffset += 2;
      }
      if(valueMatchFlag)//inserting rowId for already existing record 
        addRowIdInLeafPage(rootPageOffset, matchedRecordOffset, minRecordOffset, rowId);
      else {//adding a new record into Index page
        recordOffset = (short) (minRecordOffset-indexValueLength-6);
        file.seek(rootPageOffset+recordOffset);
        file.write(1);
        file.seek(rootPageOffset+recordOffset+1);
        file.write(indexValueLength);
        file.seek(rootPageOffset+recordOffset+2);        
        byte[] indexValueToByteArray = ByteBuffer.allocate(8).putLong((long) indexValue.toEpochSecond(ZoneOffset.UTC)).array();
        file.write(indexValueToByteArray);
        file.seek(rootPageOffset+recordOffset+indexValueLength+2);
        file.writeInt(rowId);
        file.seek(rootPageOffset+3);
        file.writeShort(recordOffset);
        file.seek(rootPageOffset+16+(noOfRecordsInRootPage*2));
        file.writeShort(recordOffset);
        file.seek(rootPageOffset+1);
        file.writeShort(noOfRecordsInRootPage+1);
        file.seek(5);
        int pageNo = file.readInt();
        IndexPage.sortKeys(file, pageNo);
        if(noOfRecordsInRootPage+1 > 3)
          IndexPage.splitLeafPage(file, pageNo);
      }
    }
    
    /*
     if root page type is not 0x0A then there are children in the tree
     if the index value is not already in the tree then we have to traverse the tree to find the leaf page to add the new record
     if the index value is already present then we have to check every page in the traverse path o insert the rowId
     */
    else {
      byte pageType = rootPageType;
      long pageOffset = rootPageOffset;
      while(pageType == 0x02) {//checking all interior pages in path
        short noOfRecordsInPage;
        file.seek(pageOffset+1);
        noOfRecordsInPage = file.readShort();
        
        short addOffset = 0;
        short recordOffset = 0; 
        boolean valueMatchFlag = false;
        while(noOfRecordsInPage > 0) {
          file.seek(pageOffset+16+addOffset);
          recordOffset = file.readShort();
          file.seek(pageOffset+recordOffset+6);
          LocalDateTime recordValue = LocalDateTime.ofEpochSecond(file.readLong(), 0, ZoneOffset.UTC);          
          if(indexValue.compareTo(recordValue) == 0) {//if index value matches with already existing value then add rowId
            addRowIdInInteriorPage(pageOffset, recordOffset, rowId);
            valueMatchFlag = true;
            break;
          }
          else if(indexValue.compareTo(recordValue) > 0) {//if index value greater than record value move to next record on right
            if(noOfRecordsInPage-1 == 0) {//if there are no more records to compare with then move to right child page
              file.seek(pageOffset+6);
              int nextPageNo = file.readInt();
              pageOffset = (nextPageNo - 1) * Page.pageSize;
              file.seek(pageOffset);
              pageType = file.readByte();
              break;
            }
          }
          
          else {//if index value less than record value move to left record
            file.seek(pageOffset+recordOffset);
            int nextPageNo = file.readInt();
            pageOffset = (nextPageNo - 1) * Page.pageSize;
            file.seek(pageOffset);
            pageType = file.readByte();
            break;
          }
          
        }
        if(valueMatchFlag)//if we found index value already in one of interior pages then break out of loop
          break;
        noOfRecordsInPage--;
        addOffset += 2;
      }
      if(pageType == 0x0A) {//after breaking out of loop if the page type is 0x0A then we have reached a leaf page where
                            //we either add new record or just add rowId to existing record
        file.seek(pageOffset+1);
        short recordCount = file.readShort();
        short matchedRecordOffset = 0;
        short addOffset = 0;
        short recordOffset = 0; short minRecordOffset = (short) Page.pageSize;
        boolean valueMatchFlag = false;
        while(recordCount > 0) {
          file.seek(pageOffset+16+addOffset);
          recordOffset = file.readShort();
          minRecordOffset = recordOffset < minRecordOffset ? recordOffset : minRecordOffset;          
          file.seek(pageOffset+recordOffset+2);
          LocalDateTime recordValue = LocalDateTime.ofEpochSecond(file.readLong(), 0, ZoneOffset.UTC);
          if(indexValue.compareTo(recordValue) == 0) {
            matchedRecordOffset = recordOffset;
            valueMatchFlag = true;
          }
          recordCount--;
          addOffset += 2;
        }
        if(valueMatchFlag)//add rowId to existing record
          addRowIdInLeafPage(pageOffset, matchedRecordOffset, minRecordOffset, rowId);
        else {//add new record in index leaf page
          recordOffset = (short) (minRecordOffset-indexValueLength-6);
          file.seek(pageOffset+recordOffset);
          file.write(1);
          file.seek(pageOffset+recordOffset+1);
          file.write(indexValueLength);
          file.seek(pageOffset+recordOffset+2);          
          byte[] indexValueToByteArray = ByteBuffer.allocate(8).putLong((long) indexValue.toEpochSecond(ZoneOffset.UTC)).array();
          file.write(indexValueToByteArray);
          file.seek(pageOffset+recordOffset+indexValueLength+2);
          file.writeInt(rowId);
          file.seek(pageOffset+3);
          file.writeShort(recordOffset);
          file.seek(pageOffset+16+(noOfRecordsInRootPage*2));
          file.writeShort(recordOffset);
          file.seek(pageOffset+1);
          file.writeShort(noOfRecordsInRootPage+1);
          file.seek(5);
          int pageNo = file.readInt();
          IndexPage.sortKeys(file, pageNo);
          if(noOfRecordsInRootPage+1 > 3)
            IndexPage.splitLeafPage(file, pageNo);
        }
      }
    }
  }
  
  //adding a record in Index page for Date type column
  public void addDate(@Nullable LocalDate indexValue, int rowId) throws IOException {
    //locating root page    
    long rootPageOffset = rootPageOffset();
    file.seek(rootPageOffset);
    byte rootPageType = file.readByte();
    
    //reading no of records in root page
    short noOfRecordsInRootPage = noOfRecordsInPage(rootPageOffset);
    
    /*if noOfRecordsInRootPage is 0 then add record in rootPage
      else if it matches with root page record value, insert just the rowId
      else determine to which child it should check with
    */
    byte indexValueLength;
    indexValueLength = 8;
    if(noOfRecordsInRootPage == 0) {
      //writing data to page      
      short indexRecordOffseet = writeFirstRecordInRootPage(rootPageOffset, indexValueLength);
      file.seek(rootPageOffset+indexRecordOffseet+2);
      byte[] indexValueToByteArray = ByteBuffer.allocate(8).putLong((long) indexValue.toEpochDay()).array();
      file.write(indexValueToByteArray);//write index value to cell array
      file.seek(rootPageOffset+indexRecordOffseet+indexValueLength+2);
      file.writeInt(rowId);//write rowId value
      
      //updating header values
      updateRootPageHeaderData(rootPageOffset, indexRecordOffseet);
    }
    
    /*if root page type is still 0x0A that  means there are no children yet
      two cases here i) inserting rowId for already existing record
                    ii) Or adding a new record
     */
    else if(noOfRecordsInRootPage > 0 && rootPageType == 0x0A) {
      short recordCount = noOfRecordsInRootPage;
      short matchedRecordOffset = 0;
      short addOffset = 0;
      short recordOffset = 0; short minRecordOffset = (short) Page.pageSize;
      boolean valueMatchFlag = false;
      while(recordCount > 0) {
        file.seek(rootPageOffset+16+addOffset);
        recordOffset = file.readShort();
        minRecordOffset = recordOffset < minRecordOffset ? recordOffset : minRecordOffset;
        file.seek(rootPageOffset+recordOffset+2);
        LocalDate recordValue = LocalDate.ofEpochDay(file.readLong());       
        if(indexValue.compareTo(recordValue) == 0) {
          valueMatchFlag = true;
          matchedRecordOffset = recordOffset;
        }
        recordCount--;
        addOffset += 2;
      }
      if(valueMatchFlag)//inserting rowId for already existing record 
        addRowIdInLeafPage(rootPageOffset, matchedRecordOffset, minRecordOffset, rowId);
      else {//adding a new record into Index page
        recordOffset = (short) (minRecordOffset-indexValueLength-6);
        file.seek(rootPageOffset+recordOffset);
        file.write(1);
        file.seek(rootPageOffset+recordOffset+1);
        file.write(indexValueLength);
        file.seek(rootPageOffset+recordOffset+2);        
        byte[] indexValueToByteArray = ByteBuffer.allocate(8).putLong((long) indexValue.toEpochDay()).array();
        file.write(indexValueToByteArray);
        file.seek(rootPageOffset+recordOffset+indexValueLength+2);
        file.writeInt(rowId);
        file.seek(rootPageOffset+3);
        file.writeShort(recordOffset);
        file.seek(rootPageOffset+16+(noOfRecordsInRootPage*2));
        file.writeShort(recordOffset);
        file.seek(rootPageOffset+1);
        file.writeShort(noOfRecordsInRootPage+1);
        file.seek(5);
        int pageNo = file.readInt();
        IndexPage.sortKeys(file, pageNo);
        if(noOfRecordsInRootPage+1 > 3)
          IndexPage.splitLeafPage(file, pageNo);
      }
    }
    
    /*
     if root page type is not 0x0A then there are children in the tree
     if the index value is not already in the tree then we have to traverse the tree to find the leaf page to add the new record
     if the index value is already present then we have to check every page in the traverse path o insert the rowId
     */
    else {
      byte pageType = rootPageType;
      long pageOffset = rootPageOffset;
      while(pageType == 0x02) {//checking all interior pages in path
        short noOfRecordsInPage;
        file.seek(pageOffset+1);
        noOfRecordsInPage = file.readShort();
        
        short addOffset = 0;
        short recordOffset = 0; 
        boolean valueMatchFlag = false;
        while(noOfRecordsInPage > 0) {
          file.seek(pageOffset+16+addOffset);
          recordOffset = file.readShort();
          file.seek(pageOffset+recordOffset+6);
          LocalDate recordValue = LocalDate.ofEpochDay(file.readLong());          
          if(indexValue.compareTo(recordValue) == 0) {//if index value matches with already existing value then add rowId
            addRowIdInInteriorPage(pageOffset, recordOffset, rowId);
            valueMatchFlag = true;
            break;
          }
          else if(indexValue.compareTo(recordValue) > 0) {//if index value greater than record value move to next record on right
            if(noOfRecordsInPage-1 == 0) {//if there are no more records to compare with then move to right child page
              file.seek(pageOffset+6);
              int nextPageNo = file.readInt();
              pageOffset = (nextPageNo - 1) * Page.pageSize;
              file.seek(pageOffset);
              pageType = file.readByte();
              break;
            }
          }
          
          else {//if index value less than record value move to left record
            file.seek(pageOffset+recordOffset);
            int nextPageNo = file.readInt();
            pageOffset = (nextPageNo - 1) * Page.pageSize;
            file.seek(pageOffset);
            pageType = file.readByte();
            break;
          }
          
        }
        if(valueMatchFlag)//if we found index value already in one of interior pages then break out of loop
          break;
        noOfRecordsInPage--;
        addOffset += 2;
      }
      if(pageType == 0x0A) {//after breaking out of loop if the page type is 0x0A then we have reached a leaf page where
                            //we either add new record or just add rowId to existing record
        file.seek(pageOffset+1);
        short recordCount = file.readShort();
        short matchedRecordOffset = 0;
        short addOffset = 0;
        short recordOffset = 0; short minRecordOffset = (short) Page.pageSize;
        boolean valueMatchFlag = false;
        while(recordCount > 0) {
          file.seek(pageOffset+16+addOffset);
          recordOffset = file.readShort();
          minRecordOffset = recordOffset < minRecordOffset ? recordOffset : minRecordOffset;          
          file.seek(pageOffset+recordOffset+2);
          LocalDate recordValue = LocalDate.ofEpochDay(file.readLong());
          if(indexValue.compareTo(recordValue) == 0) {
            matchedRecordOffset = recordOffset;
            valueMatchFlag = true;
          }
          recordCount--;
          addOffset += 2;
        }
        if(valueMatchFlag)//add rowId to existing record
          addRowIdInLeafPage(pageOffset, matchedRecordOffset, minRecordOffset, rowId);
        else {//add new record in index leaf page
          recordOffset = (short) (minRecordOffset-indexValueLength-6);
          file.seek(pageOffset+recordOffset);
          file.write(1);
          file.seek(pageOffset+recordOffset+1);
          file.write(indexValueLength);
          file.seek(pageOffset+recordOffset+2);          
          byte[] indexValueToByteArray = ByteBuffer.allocate(8).putLong((long) indexValue.toEpochDay()).array();
          file.write(indexValueToByteArray);
          file.seek(pageOffset+recordOffset+indexValueLength+2);
          file.writeInt(rowId);
          file.seek(pageOffset+3);
          file.writeShort(recordOffset);
          file.seek(pageOffset+16+(noOfRecordsInRootPage*2));
          file.writeShort(recordOffset);
          file.seek(pageOffset+1);
          file.writeShort(noOfRecordsInRootPage+1);
          file.seek(5);
          int pageNo = file.readInt();
          IndexPage.sortKeys(file, pageNo);
          if(noOfRecordsInRootPage+1 > 3)
            IndexPage.splitLeafPage(file, pageNo);
        }
      }
    }
  }
  
  //adding a record in Index page for Time type column
  public void addTime(@Nullable LocalTime indexValue, int rowId) throws IOException {
    //locating root page    
    long rootPageOffset = rootPageOffset();
    file.seek(rootPageOffset);
    byte rootPageType = file.readByte();
    
    //reading no of records in root page
    short noOfRecordsInRootPage = noOfRecordsInPage(rootPageOffset);
    
    /*if noOfRecordsInRootPage is 0 then add record in rootPage
      else if it matches with root page record value, insert just the rowId
      else determine to which child it should check with
    */
    byte indexValueLength;
    indexValueLength = 4;
    if(noOfRecordsInRootPage == 0) {
      //writing data to page      
      short indexRecordOffseet = writeFirstRecordInRootPage(rootPageOffset, indexValueLength);
      file.seek(rootPageOffset+indexRecordOffseet+2);
      byte[] indexValueToByteArray = ByteBuffer.allocate(4).putInt((int) indexValue.toSecondOfDay()).array();
      file.write(indexValueToByteArray);//write index value to cell array
      file.seek(rootPageOffset+indexRecordOffseet+indexValueLength+2);
      file.writeInt(rowId);//write rowId value
      
      //updating header values
      updateRootPageHeaderData(rootPageOffset, indexRecordOffseet);
    }
    
    /*if root page type is still 0x0A that  means there are no children yet
      two cases here i) inserting rowId for already existing record
                    ii) Or adding a new record
     */
    else if(noOfRecordsInRootPage > 0 && rootPageType == 0x0A) {
      short recordCount = noOfRecordsInRootPage;
      short matchedRecordOffset = 0;
      short addOffset = 0;
      short recordOffset = 0; short minRecordOffset = (short) Page.pageSize;
      boolean valueMatchFlag = false;
      while(recordCount > 0) {
        file.seek(rootPageOffset+16+addOffset);
        recordOffset = file.readShort();
        minRecordOffset = recordOffset < minRecordOffset ? recordOffset : minRecordOffset;
        file.seek(rootPageOffset+recordOffset+2);
        LocalTime recordValue = LocalTime.ofSecondOfDay(file.readInt());       
        if(indexValue.compareTo(recordValue) == 0) {
          valueMatchFlag = true;
          matchedRecordOffset = recordOffset;
        }
        recordCount--;
        addOffset += 2;
      }
      if(valueMatchFlag)//inserting rowId for already existing record 
        addRowIdInLeafPage(rootPageOffset, matchedRecordOffset, minRecordOffset, rowId);
      else {//adding a new record into Index page
        recordOffset = (short) (minRecordOffset-indexValueLength-6);
        file.seek(rootPageOffset+recordOffset);
        file.write(1);
        file.seek(rootPageOffset+recordOffset+1);
        file.write(indexValueLength);
        file.seek(rootPageOffset+recordOffset+2);        
        byte[] indexValueToByteArray = ByteBuffer.allocate(4).putLong((int) indexValue.toSecondOfDay()).array();
        file.write(indexValueToByteArray);
        file.seek(rootPageOffset+recordOffset+indexValueLength+2);
        file.writeInt(rowId);
        file.seek(rootPageOffset+3);
        file.writeShort(recordOffset);
        file.seek(rootPageOffset+16+(noOfRecordsInRootPage*2));
        file.writeShort(recordOffset);
        file.seek(rootPageOffset+1);
        file.writeShort(noOfRecordsInRootPage+1);
        file.seek(5);
        int pageNo = file.readInt();
        IndexPage.sortKeys(file, pageNo);
        if(noOfRecordsInRootPage+1 > 3)
          IndexPage.splitLeafPage(file, pageNo);
      }
    }
    
    /*
     if root page type is not 0x0A then there are children in the tree
     if the index value is not already in the tree then we have to traverse the tree to find the leaf page to add the new record
     if the index value is already present then we have to check every page in the traverse path o insert the rowId
     */
    else {
      byte pageType = rootPageType;
      long pageOffset = rootPageOffset;
      while(pageType == 0x02) {//checking all interior pages in path
        short noOfRecordsInPage;
        file.seek(pageOffset+1);
        noOfRecordsInPage = file.readShort();
        
        short addOffset = 0;
        short recordOffset = 0; 
        boolean valueMatchFlag = false;
        while(noOfRecordsInPage > 0) {
          file.seek(pageOffset+16+addOffset);
          recordOffset = file.readShort();
          file.seek(pageOffset+recordOffset+6);
          LocalTime recordValue = LocalTime.ofSecondOfDay(file.readInt());          
          if(indexValue.compareTo(recordValue) == 0) {//if index value matches with already existing value then add rowId
            addRowIdInInteriorPage(pageOffset, recordOffset, rowId);
            valueMatchFlag = true;
            break;
          }
          else if(indexValue.compareTo(recordValue) > 0) {//if index value greater than record value move to next record on right
            if(noOfRecordsInPage-1 == 0) {//if there are no more records to compare with then move to right child page
              file.seek(pageOffset+6);
              int nextPageNo = file.readInt();
              pageOffset = (nextPageNo - 1) * Page.pageSize;
              file.seek(pageOffset);
              pageType = file.readByte();
              break;
            }
          }
          
          else {//if index value less than record value move to left record
            file.seek(pageOffset+recordOffset);
            int nextPageNo = file.readInt();
            pageOffset = (nextPageNo - 1) * Page.pageSize;
            file.seek(pageOffset);
            pageType = file.readByte();
            break;
          }
          
        }
        if(valueMatchFlag)//if we found index value already in one of interior pages then break out of loop
          break;
        noOfRecordsInPage--;
        addOffset += 2;
      }
      if(pageType == 0x0A) {//after breaking out of loop if the page type is 0x0A then we have reached a leaf page where
                            //we either add new record or just add rowId to existing record
        file.seek(pageOffset+1);
        short recordCount = file.readShort();
        short matchedRecordOffset = 0;
        short addOffset = 0;
        short recordOffset = 0; short minRecordOffset = (short) Page.pageSize;
        boolean valueMatchFlag = false;
        while(recordCount > 0) {
          file.seek(pageOffset+16+addOffset);
          recordOffset = file.readShort();
          minRecordOffset = recordOffset < minRecordOffset ? recordOffset : minRecordOffset;          
          file.seek(pageOffset+recordOffset+2);
          LocalTime recordValue = LocalTime.ofSecondOfDay(file.readInt());
          if(indexValue.compareTo(recordValue) == 0) {
            matchedRecordOffset = recordOffset;
            valueMatchFlag = true;
          }
          recordCount--;
          addOffset += 2;
        }
        if(valueMatchFlag)//add rowId to existing record
          addRowIdInLeafPage(pageOffset, matchedRecordOffset, minRecordOffset, rowId);
        else {//add new record in index leaf page
          recordOffset = (short) (minRecordOffset-indexValueLength-6);
          file.seek(pageOffset+recordOffset);
          file.write(1);
          file.seek(pageOffset+recordOffset+1);
          file.write(indexValueLength);
          file.seek(pageOffset+recordOffset+2);          
          byte[] indexValueToByteArray = ByteBuffer.allocate(4).putInt((int) indexValue.toSecondOfDay()).array();
          file.write(indexValueToByteArray);
          file.seek(pageOffset+recordOffset+indexValueLength+2);
          file.writeInt(rowId);
          file.seek(pageOffset+3);
          file.writeShort(recordOffset);
          file.seek(pageOffset+16+(noOfRecordsInRootPage*2));
          file.writeShort(recordOffset);
          file.seek(pageOffset+1);
          file.writeShort(noOfRecordsInRootPage+1);
          file.seek(5);
          int pageNo = file.readInt();
          IndexPage.sortKeys(file, pageNo);
          if(noOfRecordsInRootPage+1 > 3)
            IndexPage.splitLeafPage(file, pageNo);
        }
      }
    }
  }
}
