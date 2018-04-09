package com.jbr.pbm.mail;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailParseException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
public class EmailParserIT {

  @Rule
  public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP);
  @Autowired
  private EmailParser emailParser;
  private File[] files;

  @BeforeClass
  public static void beforeClass() {
    System.out.println("@BeforeClass - runOnceBeforeClass");
  }

  @AfterClass
  public static void afterClass() {
    System.out.println("@AfterClass - runOnceAfterClass");
  }

  @Before
  public void setup() {
    greenMail.withConfiguration(new GreenMailConfiguration().withDisabledAuthentication());
    greenMail.start();

    ClassLoader classLoader = getClass().getClassLoader();

    files = new File(
        Objects.requireNonNull(classLoader.getResource("attachments/")).getFile()).listFiles();

    assert files != null;
  }

  @After
  public void tearDown() {
    greenMail.stop();
  }

  @Test
  public void checkProcessMail_MultipleAttachments()
      throws MessagingException {

    final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setPort(greenMail.getSmtp().getPort());
    final MimeMessage message = mailSender.createMimeMessage();

    final MimeMessageHelper helper = new MimeMessageHelper(message, true);

    helper.setFrom("from@localhost.com");
    helper.setTo("to@localhost.com");
    helper.setSubject("[#12345678] Parsing of Attachments");
    helper.setText("Mail Content", false);
    helper.setSentDate(new Date());

    for (File attachment : files) {
      try {
        helper.addAttachment(attachment.getName(),
            new ByteArrayResource(FileUtils.readFileToByteArray(attachment)),
            helper.getFileTypeMap().getContentType(attachment));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    mailSender.send(message);

    try {
      MimeMessage[] messages = greenMail.getReceivedMessages();

      if (messages.length == 1) {
        MailMessage processedMail = emailParser.processMail(messages[0]);
        Assert.assertEquals(files.length, processedMail.getAttachments().size());

        for (int i = 0; i < processedMail.getAttachments().size(); i++) {
          Assert.assertArrayEquals(FileUtils.readFileToByteArray(files[i]),
              processedMail.getAttachments().get(i).getFileData());
        }
      } else {
        Assert.fail("More then one Message found");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void checkProcessMail_HTMLContent()
      throws MessagingException, IOException {

    final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setPort(greenMail.getSmtp().getPort());
    final MimeMessage message = mailSender.createMimeMessage();

    final MimeMessageHelper helper = new MimeMessageHelper(message, false);

    helper.setFrom("from@localhost.com");
    helper.setTo("to@localhost.com");
    helper.setSubject("[#12345678] Parsing of Attachments");
    helper.setText("<body><div>Mail Content</body></div>", true);
    helper.setSentDate(new Date());

    mailSender.send(message);

    MimeMessage[] messages = greenMail.getReceivedMessages();

    if (messages.length == 1) {
      MailMessage processedMail = emailParser.processMail(messages[0]);
      Assert.assertEquals(message.getContent(), processedMail.getContent().replaceAll("\r\n", ""));
    } else {
      Assert.fail("More then one Message found");
    }
  }

  @Test
  public void checkProcessMail_Text() {

    final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    final MimeMessage message = mailSender.createMimeMessage();

    try {

      final MimeMessageHelper helper = new MimeMessageHelper(message, false);

      helper.setFrom("from@localhost.com");
      helper.setTo("to@localhost.com");
      helper.setSubject("[#12345678] Parsing of Attachments");
      helper.setText("Spring Integration Rocks!");
      helper.setSentDate(new Date());

      MailMessage processedMail = emailParser.processMail(helper.getMimeMessage());

      Assert.assertEquals(processedMail.getContent(), helper.getMimeMessage().getContent());

    } catch (MessagingException e) {
      throw new MailParseException(e);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void checkProcessMail_InvalidSubjectLine() {
    final JavaMailSender mailSender = new JavaMailSenderImpl();
    final MimeMessage message = mailSender.createMimeMessage();

    try {
      final MimeMessageHelper helper = new MimeMessageHelper(message, false);

      helper.setFrom("from@localhost.com");
      helper.setTo("to@localhost.com");
      helper.setSubject("Invalid Subject Line");
      helper.setText("Spring Integration is Proper Content");
      helper.setSentDate(new Date());

      MailMessage processedMail = emailParser.processMail(helper.getMimeMessage());

      Assert.assertNull(processedMail);

    } catch (MessagingException e) {
      e.printStackTrace();
      Assert.fail();
    }
  }
}
