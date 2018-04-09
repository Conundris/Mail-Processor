package com.jbr.pbm.mail;

import com.jbr.pbm.pbmApi.PbmApi;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonReader;
import javax.mail.MessagingException;
import javax.mail.Part;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class MailUtils {

  @Autowired
  private PbmApi pbmApi;

  protected MailUtils() {
    throw new IllegalStateException("Utility class");
  }

  private static final Logger logger = LoggerFactory.getLogger(MailUtils.class);

  public static String formatIso8601(Date date) {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    // Quoted "Z" to indicate UTC, no timezone offset
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss'Z'");
    df.setTimeZone(tz);
    return df.format(date);
  }

  public static boolean isAttachmentOrInline(Part p) throws MessagingException {
    return Part.ATTACHMENT.equalsIgnoreCase(p.getDisposition()) ||
        Part.INLINE.equalsIgnoreCase(p.getDisposition());
  }

  public static boolean isValidSubjectLine(String subject) {
    String expression = "\\[#\\d{8}]";
    Pattern pattern = Pattern.compile(expression);
    Matcher matcher = pattern.matcher(subject);
    return matcher.find();
  }

  public static String extractOriginalDateTime(String content) {
    String regex = "Sent: (?<date>.*?) To";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(content);
    if (matcher.find()) {
      return matcher.group("date");
    }
    return "No Date found";
  }

  public static List<String> extractCustomerNumbers(List<String> customerMatches) {
    List<String> custNos = new ArrayList<>();

    for (String custNo : customerMatches) {
      if (custNos.size() < 2) {
        custNo = custNo.replace("[#", "");
        custNo = custNo.replace("]", "");
        custNos.add(custNo);
      }
    }
    return custNos;
  }

  public static List<String> extractCustMatches(String subjectLine) {
    List<String> custNos = new ArrayList<>();
    String expression = "\\[#\\d{8}]";
    Pattern pattern = Pattern.compile(expression);
    Matcher matcher = pattern.matcher(subjectLine);
    while (matcher.find()) {
      custNos.add(matcher.group());
    }
    return custNos;
  }

  public static String getHtmlPart(Part p) throws IOException, MessagingException {
    String html = p.getContent().toString();
    return Jsoup.parse(html).text();
  }

  public static String validateCustomer(URL url) {
    StringBuffer response = null;
    try {

      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      con.setRequestProperty("User-Agent", "Mozilla/5.0");
      int responseCode = con.getResponseCode();
      System.out.println("GET Response Code :: " + responseCode);
      if (responseCode == HttpURLConnection.HTTP_OK) { // success
        BufferedReader in = new BufferedReader(new InputStreamReader(
                con.getInputStream()));
        String inputLine;
        response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        in.close();
        return response.toString();
      } else {
        System.out.println("GET request not worked");
      }
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
    return response.toString();
  }

  public static boolean isValidEmailAddress(String originalEmail) {
    return !originalEmail.equals("No Email Found");
  }
  public static String getCustomerNumbers(String jsonResponse) {
    JsonReader reader = null;
    String link = null;
    try {
      reader = Json.createReader(new StringReader(jsonResponse));
      link = reader.readObject().getString("custno");
    }
    catch (Exception e) {
      logger.error("Exception occured.", e);
    } finally {
      assert reader != null;
      reader.close();
    }
    return link;
  }

  public static String extractoriginalEmailAddress(String content) {
    String regex = "From: .+(?=<)\\<(?<originalEmail>.*?)\\> Sent";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(content);
    if (matcher.find()) {
      return matcher.group("originalEmail");
    }
    return "No Email Found";
  }
}
