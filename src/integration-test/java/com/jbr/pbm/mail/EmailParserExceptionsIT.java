package com.jbr.pbm.mail;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;

import java.util.Date;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

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
import org.springframework.mail.MailParseException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("testfails")
@DirtiesContext
public class EmailParserExceptionsIT {

  @Rule
  public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP);
  @Autowired
  private EmailParser emailParser;

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
  }

  @After
  public void tearDown() {
    greenMail.stop();
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

      MimeMessage test = helper.getMimeMessage();

      MailMessage processedMail = emailParser.processMail(helper.getMimeMessage());

      emailParser.sendMessage(helper.getMimeMessage());

      Assert.assertTrue(true);

    } catch (MessagingException e) {
      throw new MailParseException(e);
    }
  }
}
