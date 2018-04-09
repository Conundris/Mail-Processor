package com.jbr.pbm.mail;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.interceptor.WireTap;
import org.springframework.integration.test.mail.TestMailServer;
import org.springframework.integration.test.mail.TestMailServer.ImapServer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class MailIT {

  private final static ImapServer imapIdleServer = TestMailServer.imap(35468);

  private static final Logger logger = LoggerFactory.getLogger(Mail.class);

  @Autowired
  private Mail mail;

  private DirectChannel tapChannel;

  @BeforeClass
  public static void beforeClass() {
    System.out.println("@BeforeClass - runOnceBeforeClass");
  }

  @AfterClass
  public static void afterClass() {
    System.out.println("@AfterClass - runOnceAfterClass");
    imapIdleServer.stop();
  }

  @Before
  public void setup() {
    //User Flag used by Mock ImapServer from Spring
    mail.mailReceiver().setUserFlag("testSIUserFlag");

    tapChannel = new DirectChannel();

    mail.directChannel().addInterceptor(new WireTap(tapChannel));
  }

  @After
  public void tearDown() {
  }

  @Test
  public void checkTest() throws InterruptedException {

    tapChannel.subscribe(message -> {
      try {
        MimeMessage mimeMessage = (MimeMessage) message.getPayload();

        Folder folder = mimeMessage.getFolder();

        folder.open(Folder.READ_WRITE);

        Message reopenedMessage = mail.getMessageWorkaround(folder, mimeMessage);

        // Spring Integration Mail implmentation currently has the msgnum field within the MimeMessage as 0
        // This isn't using the workaround
        Assert.assertEquals(mimeMessage.getMessageNumber(), 0);
        // Value should be 1 since it's the only email from the ImapServer
        // This is after calling the Workaround
        Assert.assertEquals(reopenedMessage.getMessageNumber(), 1);

        folder.close();

      } catch (MessagingException e) {
        logger.warn(e.getMessage(), e);
      }
    });

    Thread.sleep(1000);
  }
}
