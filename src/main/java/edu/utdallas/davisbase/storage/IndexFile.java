package edu.utdallas.davisbase.storage;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
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
  
  public short writeFirstRecordInRootPage(long rootPageOffset, byte indexValueLength) throws IOException {
    short indexRecordOffseet;
    indexRecordOffseet = (short) (Page.pageSize-indexValueLength-6);
    file.seek(rootPageOffset+indexRecordOffseet);
    file.writeByte(1);//No of rowIds
    file.seek(rootPageOffset+indexRecordOffseet+1);
    file.writeByte(indexValueLength);//index value length
    return indexRecordOffseet;
  }
  
  public void updateRootPageHeaderData(long rootPageOffset, short indexRecordOffseet) throws IOException {
  //updating header values
    file.seek(rootPageOffset+1);
    file.writeShort(1);
    file.seek(rootPageOffset+3);
    file.writeShort(indexRecordOffseet);//writing on 0x03 - data record offset w.r.t page
    file.seek(rootPageOffset+16);
    file.writeShort(indexRecordOffseet);//writing on 2nd row from 0x10 - each record offset w.r.t page
  }
  
  public void addRowIdInRecord(long pageOffset, short recordOffset, short minRecordOffset, int rowId) throws IOException {
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
      if(tempRecordOffset <= recordOffset) 
        file.writeShort(tempRecordOffset-4);
      noOfRecords--;
      addOffset += 2;
    }
  }
  
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
    
    else if(noOfRecordsInRootPage > 0 && rootPageType == 0x0A) {
      short recordCount = noOfRecordsInRootPage;
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
        if(indexValue.compareToIgnoreCase(recordValue) == 0)
          valueMatchFlag = true;          
        recordCount--;
        addOffset += 2;
      }
      if(valueMatchFlag)
        addRowIdInRecord(rootPageOffset, recordOffset, minRecordOffset, rowId);
      else {
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
    
    else {
      byte pageType = rootPageType;
      long pageOffset = rootPageOffset;
      while(pageType == 0x02) {
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
          if(indexValue.compareToIgnoreCase(recordValue) == 0) {
            addRowIdInInteriorPage(pageOffset, recordOffset, rowId);
            valueMatchFlag = true;
            break;
          }
          else if(indexValue.compareToIgnoreCase(recordValue) > 0) {
            if(noOfRecordsInPage-1 == 0) {
              file.seek(pageOffset+6);
              int nextPageNo = file.readInt();
              pageOffset = (nextPageNo - 1) * Page.pageSize;
              file.seek(pageOffset);
              pageType = file.readByte();
              break;
            }
          }
          
          else {
            file.seek(pageOffset+recordOffset);
            int nextPageNo = file.readInt();
            pageOffset = (nextPageNo - 1) * Page.pageSize;
            file.seek(pageOffset);
            pageType = file.readByte();
            break;
          }
          
        }
        if(valueMatchFlag)
          break;
        noOfRecordsInPage--;
        addOffset += 2;
      }
      if(pageType == 0x0A) {
        file.seek(pageOffset+1);
        short recordCount = file.readShort();
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
          if(recordValue == indexValue)
            valueMatchFlag = true;          
          recordCount--;
          addOffset += 2;
        }
        if(valueMatchFlag)
          addRowIdInRecord(pageOffset, recordOffset, minRecordOffset, rowId);
        else {
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

  public void test() throws IOException {
    long pos = 998;
    file.seek(pos);
    byte[] b = new byte[9];
    file.read(b);
    String s = new String(b);
    System.out.println(s);
    
    /*
     * String n = "hanumantha rao"; byte[] t = n.getBytes(); file.seek(16);
     * file.write(t);
     */
    
    /*
     * file.seek(16); byte[] r = new byte[14]; file.read(r, 0, 14); file.seek(32);
     * file.write(r);
     */
    
    /*
     * file.seek(48); file.writeByte(1); file.seek(49); file.writeByte(14);
     * file.seek(50); String n = "hanumantha rao"; byte[] t = n.getBytes();
     * file.write(t); file.seek(64); file.writeInt(10);
     */
    
    String s1 = "IAnU";
    String s2 = "hanu";
    int c = s1.compareToIgnoreCase(s2);
    
  }
}
