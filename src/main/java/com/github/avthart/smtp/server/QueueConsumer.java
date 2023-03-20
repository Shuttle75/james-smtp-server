package com.github.avthart.smtp.server;

import org.apache.activemq.broker.BrokerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;


@Service
public class QueueConsumer {

    @Autowired
    BrokerService producerBroker;

    @JmsListener(destination = "smtp.queue", concurrency = "8-16")
    public void listener(ObjectMessage objectMessage) throws JMSException {
        objectMessage.getObject();
        producerBroker.checkQueueSize("smtp.queue");
    }
}
