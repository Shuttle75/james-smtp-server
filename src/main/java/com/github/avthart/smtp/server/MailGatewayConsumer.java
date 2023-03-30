package com.github.avthart.smtp.server;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.broker.BrokerService;
import org.apache.commons.mail.util.MimeMessageParser;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
@Service
public class MailGatewayConsumer {

    private final BrokerService producerBroker;
    private final JmsTemplate jmsTemplate;

    public MailGatewayConsumer(BrokerService producerBroker, JmsTemplate jmsTemplate) {
        this.producerBroker = producerBroker;
        this.jmsTemplate = jmsTemplate;
    }

    @JmsListener(destination = "smtp.mail.in")
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

        /*
        *
        *                         MailGateway
        *
        * */


        producerBroker.checkQueueSize("smtp.queue");

        jmsTemplate.send("smtp.mail.out", session -> objectMessage);
    }
}
