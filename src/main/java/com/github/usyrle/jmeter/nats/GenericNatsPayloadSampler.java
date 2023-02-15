package com.github.usyrle.jmeter.nats;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Subscription;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;

/**
 * Takes a generic bytes payload encoded as either base64 or hex string, sends it to a given NATS subject, and waits for a reply on another NATS subject.
 * Requires NatsConfig to be set up as part of the Thread Group.
 */
@Slf4j
@Getter
@Setter
public class GenericNatsPayloadSampler extends AbstractSampler implements TestBean {

    private String payloadType;
    private Long payloadTimeout;
    private String payload;

    @Override
    public SampleResult sample(Entry entry) {

        final SampleResult sampleResult = new SampleResult();

        if (Objects.isNull(NatsConfig.getNatsConnection())) {
            log.error("No NATS connection was found");
            return sampleResult;
        }

        byte[] payloadBytes = getPayloadBytes(payload);
        sampleResult.setSentBytes(payloadBytes.length);

        final Connection nc = NatsConfig.getNatsConnection();
        final Subscription sub = NatsConfig.getNatsSubscription();

        try {
            sampleResult.sampleStart();

            nc.publish(NatsConfig.getPublish(), payloadBytes);

            final Message msg = sub.nextMessage(Duration.ofMillis(payloadTimeout));

            sampleResult.setSuccessful(Boolean.TRUE);
            sampleResult.setResponseMessage(new String(msg.getData()));
        } catch (Exception e) {
            log.error("Error getting next message - exception type {}, exception message {}",
                    e.getClass().getCanonicalName(),
                    e.getMessage());

            sampleResult.setSuccessful(Boolean.FALSE);
        }

        sampleResult.sampleEnd();
        return sampleResult;
    }

    @SneakyThrows
    byte[] getPayloadBytes(String payload) {
        if (PayloadType.BASE64.getName().equals(payloadType)) {
            return Base64.getDecoder().decode(payload);
        } else if (PayloadType.HEX.getName().equals(payloadType)) {
            return Hex.decodeHex(payload);
        } else {
            return payload.getBytes();
        }
    }
}
