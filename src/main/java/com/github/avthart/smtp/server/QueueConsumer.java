package com.github.avthart.smtp.server;

import org.apache.activemq.broker.BrokerService;
import org.apache.commons.mail.util.MimeMessageParser;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Properties;


@Service
public class QueueConsumer {

    private final BrokerService producerBroker;

    public QueueConsumer(BrokerService producerBroker) {
        this.producerBroker = producerBroker;
    }

    @JmsListener(destination = "smtp.queue")
    public void listener(ObjectMessage objectMessage) throws JMSException, MessagingException {
        InputStream inputStream = new ByteArrayInputStream((byte[]) objectMessage.getObject());

        Properties props = System.getProperties();
        Session mailSession = Session.getInstance(props);
        MimeMessage mimeMessage = new MimeMessage(mailSession, inputStream);

        try {
            final MimeMessageParser mimeParser = new MimeMessageParser(mimeMessage).parse();
            System.out.println("Attachments " + mimeParser.getAttachmentList().size());
        }
        catch (Exception e) {
            // Error parsing
            throw new RuntimeException(e);
        }

        producerBroker.checkQueueSize("smtp.queue");
    }
}
