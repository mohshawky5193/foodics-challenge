package com.foodics.challenge.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {


  private final JavaMailSender mailSender;


  private static final String MAIL_TEMPLATE = "We want to buy %s";

  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  @Async
  public void sendEmail(String subject,String to,String messageToBeSent) {
    SimpleMailMessage message = new SimpleMailMessage();

      message.setTo(to);
      message.setSubject(subject);
      message.setText(messageToBeSent);
      mailSender.send(message);

  }
}
