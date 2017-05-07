package org.mail;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Mail Sender class to Send Email Messages in Error and Success Mode
 * 
 * @author Ashish Ranjan
 */
public class MailSender
{
	
	private static final String EMAIL_CONTENT_FILE_NAME = "email.content.file";
	
	private static final String TO_RECIPIENTS = "recipients.to";
	private static final String BCC_RECIPIENTS = "recipients.bcc";
	private static final String CC_RECIPIENTS = "recipients.cc";
	private static final String MAIL_SENDER_NAME = "mail.sender.name";
	private static final String MAIL_SUBJECT = "mail.subject";
	
	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	
   public static void main(String [] args)
   {  
		if(args.length == 0 || args[0] == null) {
			System.out.println("Arguments Missing. Proper usage : java -jar MailSender.jar ");
			System.exit(1);
	    }
		
		try {
			sendMails(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
   }
   
   private static void sendMails(String dbName) throws Exception{
	   Properties mailProperties = new Properties();
	   Properties logsProperties = new Properties();
	   Properties recipientProperties = new Properties();
	   InputStream fileStream = null;
		try {
			fileStream = new FileInputStream("mail.properties");
			mailProperties.load(fileStream);
			fileStream = new FileInputStream("logs.properties");
			logsProperties.load(fileStream);
			fileStream = new FileInputStream("recipients.properties");
			recipientProperties.load(fileStream);
		} catch (FileNotFoundException e) {
			System.out.println("Please make sure mail.properties,logs.properties and recipients.properties are available.");
			throw new RuntimeException("Unable to find properties file. ",e);
		} catch (IOException e) {
			throw new RuntimeException("Exception while loading the properties file.",e);
		}
		List<String> toEmailIds = getEmailIds(recipientProperties.getProperty(TO_RECIPIENTS));
		List<String> ccEmailIds = getEmailIds(recipientProperties.getProperty(CC_RECIPIENTS));
		List<String> bccEmailIds = getEmailIds(recipientProperties.getProperty(BCC_RECIPIENTS));
		
		if(toEmailIds.size() == 0) {
			throw new RuntimeException(" No Recipieints to Sent emails. ");
		}
		
		String subject = null;
		StringBuilder contentBuilder = new StringBuilder();
		File emailContentFile = new File(logsProperties.getProperty(EMAIL_CONTENT_FILE_NAME));
		
		
		if(emailContentFile != null && emailContentFile.exists() && emailContentFile.length() != 0) { 
			subject = recipientProperties.getProperty(MAIL_SUBJECT);
		} else {
			System.out.println("Can't find any Content Files .. Make Sure Path is correct in logs.properties File.");
		}
		contentBuilder.append("\n\n");
		//Reading Log File
		BufferedReader br=null;
		try {
			br = new BufferedReader(new FileReader(emailContentFile));
	        String line = br.readLine();
	
	        while (line != null) {
	        	contentBuilder.append(line);
	        	contentBuilder.append("\n");
	            line = br.readLine();
	        }
		} catch (Exception e) {
			System.out.println("Exception while Reading the file :"+e.getMessage());
			throw new RuntimeException(e);
		} finally {
			br.close();
		}
		
    	contentBuilder.append("\n");
    	contentBuilder.append("Note : This is an auto generated email. Please don't Reply.");
		System.out.println("Sending mail. Subject :"+subject+ "\n Content : "+contentBuilder.toString());

	      // Get the default Session object.
	      Session session = Session.getDefaultInstance(mailProperties);

         // Create a default MimeMessage object.
         MimeMessage message = new MimeMessage(session);
         
         for(String recipientEmail : toEmailIds) {

             // Set To: header field of the header.
             message.addRecipient(Message.RecipientType.TO,
                                      new InternetAddress(recipientEmail));
         }
         
         for(String recipientEmail : ccEmailIds) {

             // Set To: header field of the header.
             message.addRecipient(Message.RecipientType.CC,
                                      new InternetAddress(recipientEmail));
         }
         
         for(String recipientEmail : bccEmailIds) {

             // Set To: header field of the header.
             message.addRecipient(Message.RecipientType.BCC,
                                      new InternetAddress(recipientEmail));
         }
             
         // Set From: header field of the header.
		message.setFrom(new InternetAddress(mailProperties.getProperty("mail.smtp.from", ""), 
				recipientProperties.getProperty(MAIL_SENDER_NAME, "Email Automation")));
         // Set Subject: header field
         message.setSubject(subject);

         // Now set the actual message
         message.setText(contentBuilder.toString());

         // Send message
         Transport.send(message);
         System.out.println("Email Sent successfully.... To : "+toEmailIds.toString()+" CC : "+ccEmailIds.toString()+" BCC :"+bccEmailIds.toString());
   }
   
   public static boolean isValidEmail(String emailAddress) {
       Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
       Matcher matcher = emailPattern.matcher(emailAddress);
       return matcher.matches();
   }
   
   public static List<String> getEmailIds(String emailStr) {
	   if(emailStr == null || emailStr == "") {
		   return new ArrayList<String>(0);
	   }
       String[] emailArr = emailStr.split(",");
       List<String> emailsList = new ArrayList<String>(emailArr.length);
       for (String email : emailArr) {
           if (!(email == "") && isValidEmail(email)) {
               emailsList.add(email);
           }
       }
       return emailsList;
   }
}