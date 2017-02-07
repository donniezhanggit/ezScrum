package ntut.csie.ezScrum.issue.mail.service.core;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import ntut.csie.ezScrum.dao.AccountDAO;
import ntut.csie.ezScrum.web.dataObject.AccountObject;

public class GmailSender {
	private final String host = "smtp.gmail.com";
	private final int port = 465;
	private String senderEmailAddress;
	private String senderEmailPassword;
	private Properties properties = new Properties();
	private Session session;

	public GmailSender(String senderAddress, String password) {
		senderEmailAddress = senderAddress;
		senderEmailPassword = password;
		System.setProperty("mail.mime.charset", "utf-8");
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.socketFactory.port", port);
		properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.port", port);
		properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");

		session = Session.getInstance(properties, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(senderEmailAddress, senderEmailPassword);
			}
		});
	}

	public String send(String address, String subject, String sprintGoal, String storyInfo, String schedule) {
		
		try {
			Message message = new MimeMessage(session);
			MimeMultipart multipart = new MimeMultipart();
			MimeBodyPart messageBody = new MimeBodyPart();
			message.setFrom(new InternetAddress(senderEmailAddress));
			String[] recivers = address.split(";");
			for(int reciverNum = 0; reciverNum < recivers.length; reciverNum++){
				String recive = recivers[reciverNum];
				AccountObject reciver = AccountDAO.getInstance().get(recive);
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(reciver.getEmail()));
				message.setSubject(subject);
				String content = getMailContent(subject, sprintGoal, storyInfo, schedule);

				messageBody.setContent(content, "text/html; charset=utf-8");
				multipart.addBodyPart(messageBody);
				message.setContent(multipart);
				
				Transport transport = session.getTransport("smtp");
				transport.connect(host, port, senderEmailAddress, senderEmailPassword);
				Transport.send(message);
			}
			
			return "寄送email結束.";
		} catch (MessagingException e) {
			String errorMessage = e.getMessage().toString();
			if (errorMessage.contains("https://accounts.google.com/signin/continue?")) {
				return "請開啟    安全性較低的應用程式存取權限  或使用設定google兩段式登入";
			} else if (errorMessage.contains("Username and Password not accepted")) {
				return "帳號密碼不正確";
			} else if (errorMessage.contains("Invalid Addresses")) {
				return "送信位址格式不正確";
			} else if (errorMessage.contains("Could not connect to SMTP host: smtp.gmail.com")) {
				return "無法連線到SMTP host，請檢察防火牆或Proxy設定";
			} else if (errorMessage.contains("Unknown SMTP host: smtp.gmail.com")) {
				return "Unknown SMTP host: smtp.gmail.com，請檢察網路連線";
			} else {
				return e.getMessage().toString();
			}
		}
	}
	
	public String getMailContent(String subject, String sprintGoal, String storyInfo, String schedule){
		String content = "                             " + "<font size=+4><b>"+subject+"</b></font><br><br>";
		content = content + "<font size=+3><b>Sprint Goal</b></font><br>";
		content = content + "<font size=4><li>"+sprintGoal+"</li></font><br>";
		content = content + "<font size=+3><b>Sprint Backlog(Estimates in Parenthesis)</b></font><br>";
		content = content + "<font size=4>"+storyInfo+"</font><br><br>";
		content = content + "<font size=+3><b>Schedule</b></font><br>";
		content = content + "<font size=4>" + schedule+ "</font>";
		return content;
	}
}