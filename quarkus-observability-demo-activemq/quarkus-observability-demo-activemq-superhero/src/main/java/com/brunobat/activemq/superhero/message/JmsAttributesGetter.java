package com.brunobat.activemq.superhero.message;

import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesGetter;

import jakarta.annotation.Nullable;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.Topic;

enum JmsAttributesGetter implements MessagingAttributesGetter<Message, Message> {
    INSTANCE;

    @Nullable
    @Override
    public String getSystem(Message message) {
        return "jms";
    }

    @Nullable
    @Override
    public String getDestinationKind(Message message) {
        Destination jmsDestination = null;
        try {
            jmsDestination = message.getJMSDestination();
        } catch (Exception ignored) {
            // Ignore
        }

        if (jmsDestination == null) {
            return null;
        }

        if (jmsDestination instanceof Queue) {
            return "queue";
        }
        if (jmsDestination instanceof Topic) {
            return "topic";
        }
        return null;
    }

    @Nullable
    @Override
    public String getDestination(Message message) {
        Destination jmsDestination = null;
        try {
            jmsDestination = message.getJMSDestination();
        } catch (Exception ignored) {
            // Ignore
        }

        if (jmsDestination == null) {
            return null;
        }

        if (jmsDestination instanceof Queue) {
            try {
                return ((Queue) jmsDestination).getQueueName();
            } catch (JMSException e) {
                return "unknown";
            }
        }
        if (jmsDestination instanceof Topic) {
            try {
                return ((Topic) jmsDestination).getTopicName();
            } catch (JMSException e) {
                return "unknown";
            }
        }
        return null;
    }

    @Override
    public boolean isTemporaryDestination(Message message) {
        return false;
    }

    @Nullable
    @Override
    public String getConversationId(Message message) {
        try {
            return message.getJMSCorrelationID();
        } catch (JMSException e) {
            return null;
        }
    }

    @Nullable
    @Override
    public Long getMessagePayloadSize(Message message) {
        return null;
    }

    @Nullable
    @Override
    public Long getMessagePayloadCompressedSize(Message message) {
        return null;
    }

    @Nullable
    @Override
    public String getMessageId(Message message, @Nullable Message message2) {
        return null;
    }
}
