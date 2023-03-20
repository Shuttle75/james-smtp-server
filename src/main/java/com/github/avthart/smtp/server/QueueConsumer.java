package com.github.avthart.smtp.server;

import org.apache.activemq.broker.BrokerService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;


@Service
public class QueueConsumer {

    private final BrokerService producerBroker;

    public QueueConsumer(BrokerService producerBroker) {
        this.producerBroker = producerBroker;
    }

    @JmsListener(destination = "smtp.queue")
    public void listener(ObjectMessage objectMessage) throws JMSException {
        objectMessage.getObject();
        producerBroker.checkQueueSize("smtp.queue");
    }
}
