package ticketing;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailSendManager {
	
	public boolean Send(String _passenger, String _code) {
		String user = "b4e109@gmail.com"; //발신자 이메일 아이디
		String password = "llww oeao ldrs vkfe"; //발신자 이메일의 패스워드를 입력

		// SMTP 서버 정보를 설정한다.
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com"); 
        prop.put("mail.smtp.port", 465); 
        prop.put("mail.smtp.auth", "true"); 
        prop.put("mail.smtp.ssl.enable", "true"); 
        prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        prop.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
        
        Session session = Session.getDefaultInstance(prop, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));//발신자 주소

            //수신자 주소
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(_passenger));

            // Subject
            message.setSubject("인증번호 보내드립니다."); //메일 제목을 입력

            // Text
            message.setText("인증번호: " + _code);    //메일 내용을 입력

            // send the message
            Transport.send(message); ////전송
            System.out.println("메일 발송 완료");
            return true;
        } catch (AddressException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
	}
}
