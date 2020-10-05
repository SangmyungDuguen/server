package smu.capstone.heartsignal.service.mailService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import smu.capstone.heartsignal.dto.SuccessDTO;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;

    public SuccessDTO sendMail(String mail) throws FileNotFoundException, URISyntaxException, Exception {
        final SuccessDTO success = new SuccessDTO(true);
        final SuccessDTO fail = new SuccessDTO(false);
        final Date date = new Date();
        final SimpleDateFormat form = new SimpleDateFormat("yyyy년 MM월 dd일 E요일 HH시 mm분 ss초");

        final String subject = "HEART SIGNAL 알림";
        final String text = "피보호자의 심박수에 이상이 발생하였습니다.\n\n" + form.format(date) + "\n[HEART SIGNAL Administrator]";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(mail);
            message.setSubject(subject);
            message.setText(text);
            javaMailSender.send(message);
            return success;
        } catch (Exception e) {
            return fail;
        }
    }
}
