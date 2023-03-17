package com.github.avthart.smtp.server;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;


@Service
public class QueueConsumer {

    @JmsListener(destination = "smtp.queue")
    public void listener(ObjectMessage objectMessage) throws JMSException {
        objectMessage.getObject();
        System.out.println(objectMessage.getStringProperty("sender"));
    }
}
