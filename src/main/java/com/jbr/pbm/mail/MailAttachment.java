package com.jbr.pbm.mail;

import java.util.Arrays;

public class MailAttachment {

  private String fileName;
  private byte[] fileData;

  public MailAttachment() {
  }

  public MailAttachment(String fileName, byte[] fileData) {
    this.fileName = fileName;
    this.fileData = fileData;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public void setFileData(byte[] byteArray) {
    this.fileData = byteArray;
  }

  public byte[] getFileData() {
    return fileData;
  }

  @Override
  public String toString() {
    return "MailAttachment{" +
            "fileName='" + fileName + '\'' +
            ", fileData=" + Arrays.toString(fileData) +
            '}';
  }
}
