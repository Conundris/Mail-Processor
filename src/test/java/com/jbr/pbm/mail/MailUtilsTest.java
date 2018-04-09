package com.jbr.pbm.mail;


import com.googlecode.zohhak.api.TestWith;
import com.googlecode.zohhak.api.runners.ZohhakRunner;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailParseException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

@RunWith(ZohhakRunner.class)
public class MailUtilsTest {

  private byte[] pdfAttachment;

  @BeforeClass
  public static void BeforeClass() {
    System.out.println("@BeforeClass - runOnceBeforeClass");
  }

  @AfterClass
  public static void AfterClass() {
    System.out.println("@AfterClass - runOnceAfterClass");
  }

  @Before
  public void setUp() {
    ClassLoader classLoader = getClass().getClassLoader();

    try {
      pdfAttachment = FileUtils.readFileToByteArray(new File(
          (Objects.requireNonNull(classLoader.getResource("attachments/pdf.pdf")).getFile())));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @After
  public void tearDown() {
  }

  @TestWith({"Fw: [#12345678] Test Subject",
      "[#12345678]Test Subject",
      "[#12345678]Test Subject [#876564321]",
      "[#12345678][#876564321]",
      "[#12345678][#876564321]Test Subject",
      "Test Subject [#12345678][#876564321]",
      "Fw:[#12345678] Test Subject",
      "Fw:[#12345678]Test Subject",
      "Test Subject[#12345678]Test Subject",
      "[Subject: Fw: [#12345678] Test Subject",
      "[Subject: Fw:[#12345678]Test Subject",
      "Fw: [#12345678] [#12345678] [#12345678] Test Subject"})
  public void checkMessageSubject_valid(String subject) {
    boolean result = MailUtils.isValidSubjectLine(subject);
    Assert.assertTrue(result);
  }

  @TestWith({"Fw: [#1234568] Test 7 numbers",
      "Fw: [#123456789] Test 9 numbers",
      "Fw:[#12345678 Missing right bracket",
      "Fw:#12345678] Missing left bracket",
      "[Subject: Fw: {#12345678} wrong brackets",
      "Fw: [#123456F8] Letter within brackets"})
  public void checkMessageSubject_invalid(String subject) {
    boolean result = MailUtils.isValidSubjectLine(subject);
    Assert.assertFalse(result);
  }

//  @Test
//  public void checkFormatISO8601_valid() {
//    String result = MailUtils.formatIso8601(new Date(1, 0, 1));
//    Assert.assertEquals("1901-01-01T00:25:021Z", result);
//  }

  @Test
  public void checkExtractCustMatches_singleAccount() {
    String subjectLine = "[#12345678]Test Subject";
    List<String> matches = MailUtils.extractCustMatches(subjectLine);
    // Expecting only one match
    Assert.assertEquals("There should be one customer match", 1, matches.size());
    Assert.assertEquals("[#12345678]", matches.get(0));
  }

  @Test
  public void checkExtractCustMatches_invalidSingleAccount() {
    String subjectLine = "[#12345F78]Test Subject";
    List<String> matches = MailUtils.extractCustMatches(subjectLine);
    // Expecting no match
    Assert.assertEquals("There should be no customer match", 0, matches.size());
  }

  @Test
  public void checkExtractCustMatches_jointAccount() {
    String subjectLine = "[#12345678] Test Subject [#86564321]";
    ArrayList<String> matches = new ArrayList<>(Arrays.asList("[#12345678]", ("[#86564321]")));

    List<String> custMatches = MailUtils.extractCustMatches(subjectLine);
    // Expecting two matches
    Assert.assertEquals("Customer matches should be 2.", custMatches.size(), matches.size());
    for (int i = 0; i < custMatches.size(); i++) {
      Assert.assertEquals(matches.get(i), custMatches.get(i));
    }

  }

  @Test
  public void checkExtractCustMatches_mulitple() {
    String subjectLine = "[#12345678]Test Subject [#76564321] [#85564321]";
    ArrayList<String> matches = new ArrayList<>(
        Arrays.asList("[#12345678]", "[#76564321]", "[#85564321]"));

    List<String> custMatches = MailUtils.extractCustMatches(subjectLine);
    // Expecting three matches
    Assert.assertEquals("Customer matches should be 3.", custMatches.size(), matches.size());
    for (int i = 0; i < custMatches.size(); i++) {
      Assert.assertEquals(matches.get(i), custMatches.get(i));
    }
  }

  @Test
  public void checkExtractCustomerNumbers_validSingle() {
    String subjectLine = "[#12345678]Test Subject";

    List<String> matches = MailUtils.extractCustMatches(subjectLine);
    List<String> customerNumbers = MailUtils.extractCustomerNumbers(matches);

    Assert.assertEquals("One customer number expected", 1, customerNumbers.size());
    Assert.assertEquals("12345678", customerNumbers.get(0));
  }

  @Test
  public void checkExtractCustomerNumbers_validJoint() {
    String subjectLine = "[#12345678]Test Subject [#87654321]";

    List<String> matches = MailUtils.extractCustMatches(subjectLine);
    List<String> customerNumbers = MailUtils.extractCustomerNumbers(matches);

    Assert.assertEquals("Two Customer Numbers expected", 2, customerNumbers.size());
    Assert.assertEquals("12345678", customerNumbers.get(0));
    Assert.assertEquals("87654321", customerNumbers.get(1));
  }

  @Test
  public void checkExtractCustomerNumbers_moreThenTwo() {
    String subjectLine = "[#12345678]Test Subject [#88654321] [#87654321]";

    List<String> matches = MailUtils.extractCustMatches(subjectLine);
    List<String> customerNumbers = MailUtils.extractCustomerNumbers(matches);

    Assert.assertEquals("Two Customer Numbers expected", 2, customerNumbers.size());
    Assert.assertEquals("12345678", customerNumbers.get(0));
    Assert.assertEquals("88654321", customerNumbers.get(1));
  }

  @Test
  public void checkExtractOriginalDateTime() {
    String content = "...Sent: 2018-03-06T01:01:01:000Z To...";
    String result = MailUtils.extractOriginalDateTime(content);

    Assert.assertEquals("2018-03-06T01:01:01:000Z", result);
  }

  @Test
  public void checkIsAttachment() {
    final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    final MimeMessage message = mailSender.createMimeMessage();

    try {

      final MimeMessageHelper helper = new MimeMessageHelper(message, true);

      helper.setFrom("from@localhost.com");
      helper.setTo("to@localhost.com");
      helper.setSubject("[#12345678] Parsing of Attachments");
      helper.setText("Spring Integration Rocks!");
      helper.setSentDate(new Date());

      helper.addAttachment("pdf", new ByteArrayResource(pdfAttachment), "application/pdf");

      MimeMessage test = helper.getMimeMessage();

      Multipart parts = (Multipart) test.getContent();
      Part pdfPart = parts.getBodyPart(1);
      Assert.assertTrue(Part.ATTACHMENT.equalsIgnoreCase(pdfPart.getDisposition()));
      Assert.assertEquals("pdf", pdfPart.getFileName());
      Assert.assertEquals("application/pdf", pdfPart.getDataHandler().getContentType());
      Assert.assertTrue(MailUtils.isAttachmentOrInline(pdfPart));

    } catch (MessagingException e) {
      throw new MailParseException(e);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void checkIsInline() {
    final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    final MimeMessage message = mailSender.createMimeMessage();

    try {

      final MimeMessageHelper helper = new MimeMessageHelper(message, true);

      helper.setFrom("from@localhost.com");
      helper.setTo("to@localhost.com");
      helper.setSubject("[#12345678] Parsing of Attachments");
      helper.setText("Spring Integration Rocks!");
      helper.setSentDate(new Date());

      helper.addInline("pdf", new ByteArrayResource(pdfAttachment), "application/pdf");

      MimeMessage test = helper.getMimeMessage();

      Multipart parts = (Multipart) test.getContent();
      Multipart inlineParts = (Multipart) parts.getBodyPart(0).getContent();
      Part pdfInlinePart = inlineParts.getBodyPart(1);
      Assert.assertTrue(Part.INLINE.equalsIgnoreCase(pdfInlinePart.getDisposition()));
      Assert.assertEquals("application/pdf", pdfInlinePart.getDataHandler().getContentType());
      Assert.assertTrue(MailUtils.isAttachmentOrInline(pdfInlinePart));

    } catch (MessagingException e) {
      throw new MailParseException(e);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void checkGetHTMLPart() {
    final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    final MimeMessage message = mailSender.createMimeMessage();

    try {

      final MimeMessageHelper helper = new MimeMessageHelper(message, false);

      helper.setFrom("from@localhost.com");
      helper.setTo("to@localhost.com");
      helper.setSubject("[#12345678] Parsing of Attachments");

      helper.setText("<div>Spring Integration Rocks!</div>");
      helper.setSentDate(new Date());

      MimeMessage test = helper.getMimeMessage();

      Assert.assertFalse(MailUtils.isAttachmentOrInline(test));

      Assert.assertEquals("<div>Spring Integration Rocks!</div>", test.getContent());

    } catch (MessagingException e) {
      throw new MailParseException(e);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}