package com.github.avthart.smtp.server;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.broker.BrokerService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.mail.util.MimeMessageParser;
import org.apache.commons.net.smtp.SMTPClient;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Properties;

@Slf4j
@Service
public class MailOutConsumer {
    private static final String UTF_8_ENCODING = "UTF-8";
    private final BrokerService producerBroker;

    public MailOutConsumer(BrokerService producerBroker) {
        this.producerBroker = producerBroker;
    }

    @JmsListener(destination = "smtp.mail.out")
    public void listener(ObjectMessage objectMessage) throws JMSException, MessagingException {
        InputStream inputStream = new ByteArrayInputStream((byte[]) objectMessage.getObject());

        String sender = objectMessage.getStringProperty("sender");
        String[] recipients = objectMessage.getStringProperty("recipients").split(",");

        Properties props = System.getProperties();
        Session mailSession = Session.getInstance(props);
        MimeMessage mimeMessage = new MimeMessage(mailSession, inputStream);

        try {
            final MimeMessageParser mimeParser = new MimeMessageParser(mimeMessage).parse();
//            System.out.println("Attachments " + mimeParser.getAttachmentList().size());
        }
        catch (Exception e) {
            // Error parsing
            throw new RuntimeException(e);
        }

        SMTPClient smtpClient = new SMTPClient(UTF_8_ENCODING);
        try {
            smtpClient.connect("localhost", 2525);
            smtpClient.setSender(sender);
            for (String recipient: recipients) {
                smtpClient.addRecipient(recipient);
            }

            Writer wr = smtpClient.sendMessageData();
            for (String line : IOUtils.readLines(inputStream, UTF_8_ENCODING)) {
                wr.write(line);
            }
            wr.close();
//            log.info("Mail OUT sent, recipients " + objectMessage.getStringProperty("recipients"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        producerBroker.checkQueueSize("smtp.mail.out");
    }
}
