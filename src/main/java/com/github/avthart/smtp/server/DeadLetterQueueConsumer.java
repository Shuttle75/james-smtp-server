package com.github.avthart.smtp.server;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.broker.BrokerService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.smtp.SMTPClient;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.mail.MessagingException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

@Slf4j
@Service
public class DeadLetterQueueConsumer {
  private static final String UTF_8_ENCODING = "UTF-8";
  protected static final String ACTIVEMQ_DLQ = "ActiveMQ.DLQ";
  private final BrokerService producerBroker;
  private final SmtpServerProperties properties;

  public DeadLetterQueueConsumer(BrokerService producerBroker, SmtpServerProperties properties) {
    this.producerBroker = producerBroker;
    this.properties = properties;
  }


  @JmsListener(destination = ACTIVEMQ_DLQ, concurrency = "2")
  public void listener(ObjectMessage objectMessage) throws JMSException, MessagingException, IOException {
    InputStream inputStream = new ByteArrayInputStream((byte[]) objectMessage.getObject());

    String sender = objectMessage.getStringProperty("sender");
    String[] recipients = objectMessage.getStringProperty("recipients").split(",");



    if(properties.isMailOutEnabled()) {
      SMTPClient smtpClient = new SMTPClient(UTF_8_ENCODING);
      smtpClient.connect("localhost", 7525);
      smtpClient.setSender(sender);
      for (String recipient : recipients) {
        smtpClient.addRecipient(recipient);
      }

      Writer wr = smtpClient.sendMessageData();
      for (String line : IOUtils.readLines(inputStream, UTF_8_ENCODING)) {
        wr.write(line);
      }
      wr.close();
    }
  }
}
