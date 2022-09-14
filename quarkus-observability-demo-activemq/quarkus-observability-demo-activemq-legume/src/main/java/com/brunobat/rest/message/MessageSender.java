package com.brunobat.rest.message;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.InstrumenterBuilder;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingSpanNameExtractor;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;

import static io.opentelemetry.instrumentation.api.instrumenter.messaging.MessageOperation.SEND;
import static io.quarkus.opentelemetry.runtime.OpenTelemetryConfig.INSTRUMENTATION_NAME;

@ApplicationScoped
@Slf4j
public class MessageSender {
    @Inject
    ConnectionFactory connectionFactory;

    @Inject
    OpenTelemetry telemetry;

    private static Instrumenter<Message, Message> getProducerInstrumenter(final OpenTelemetry openTelemetry) {
        InstrumenterBuilder<Message, Message> serverBuilder = Instrumenter.builder(
                openTelemetry,
                INSTRUMENTATION_NAME, MessagingSpanNameExtractor.create(JmsAttributesGetter.INSTANCE, SEND));

        Instrumenter<Message, Message> messageMessageInstrumenter = serverBuilder
                .addAttributesExtractor(MessagingAttributesExtractor.create(JmsAttributesGetter.INSTANCE, SEND))
                .newProducerInstrumenter((message, key, value) -> {
                    if (message != null) {
                        try {
                            message.setObjectProperty(key, value);
                        } catch (JMSException e) {
                            // nothing
                        }
                    }
                });
        return messageMessageInstrumenter;
    }

    public void send(String str) {
        Context parentOtelContext = Context.current();
        Instrumenter<Message, Message> producerInstrumenter = getProducerInstrumenter(telemetry);

        try (JMSContext jmsContext = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
            Message msg = jmsContext.createTextMessage(str);
            Context spanContext = producerInstrumenter
                    .start(parentOtelContext, msg);
            jmsContext.createProducer()
                    .send(jmsContext.createQueue("heroes"), msg);
            log.info("sent message: " + str);
        }
    }
}
