package com.jbr.pbm.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonComponent
public class MailMessage implements Serializable {

  private List<String> customerNumbers;
  private String subjectLine;
  private String originalDateTimeSent;
  private String forwardDateTimeSent;
  private String forwardEmailAddress;
  private String originalEmailAddress;
  private String content;
  private String html;
  private ArrayList<MailAttachment> attachments;

  @Autowired
  public MailMessage() {
    this.attachments = new ArrayList<>();
    // TODO write unit test for this
    customerNumbers = Arrays.asList(new String[2]);
  }

  @Bean
  public String getSubjectLine() {
    return subjectLine;
  }

  public void setSubjectLine(String subjectLine) {
    this.subjectLine = subjectLine;
  }

  @Bean
  public String getOriginalDateTimeSent() {
    return originalDateTimeSent;
  }

  public void setOriginalDateTimeSent(String originalDateTimeSent) {
    this.originalDateTimeSent = originalDateTimeSent;
  }

  @Bean
  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public void addAttachment(MailAttachment attachment) {
    attachments.add(attachment);
  }

  @Bean
  public String getForwardDateTimeSent() {
    return forwardDateTimeSent;
  }

  public void setForwardDateTimeSent(String forwardDateTimeSent) {
    this.forwardDateTimeSent = forwardDateTimeSent;
  }

  @Bean
  public List<MailAttachment> getAttachments() {
    return attachments;
  }

  public List<String> getCustomerNumbers() {
    return customerNumbers;
  }

  public void setCustomerNumbers(List<String> customerNumbers) {
    this.customerNumbers = customerNumbers;
  }

  public String getForwardEmailAddress() {
    return forwardEmailAddress;
  }

  public void setForwardEmailAddress(String forwardEmailAddress) {
    this.forwardEmailAddress = forwardEmailAddress;
  }

  public String getOriginalEmailAddress() {
    return originalEmailAddress;
  }

  public void setOriginalEmailAddress(String originalEmailAddress) {
    this.originalEmailAddress = originalEmailAddress;
  }

  public void setAttachments(ArrayList<MailAttachment> attachments) {
    this.attachments = attachments;
  }


  public void clearAttachments() { attachments.clear(); }

  public String getHtml() {
    return html;
  }

  public void setHtml(String html) {
    this.html = html;
  }

  @Override
  public String toString() {
    return "MailMessage{" +
        "customerNumbers=" + customerNumbers +
        ", subjectLine='" + subjectLine + '\'' +
        ", originalDateTimeSent='" + originalDateTimeSent + '\'' +
        ", forwardDateTimeSent='" + forwardDateTimeSent + '\'' +
        ", forwardEmailAddress='" + forwardEmailAddress + '\'' +
        ", content='" + html + '\'' +
        ", attachments=" + attachments +
        '}';
  }
}
