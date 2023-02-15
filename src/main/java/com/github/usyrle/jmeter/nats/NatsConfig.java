package com.github.usyrle.jmeter.nats;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.Subscription;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Objects;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

/**
 * Builds NATS configuration out of properties provided to a JMeter Config Element.
 */
@Slf4j
@Getter
@Setter
public class NatsConfig extends ConfigTestElement implements ConfigElement, TestBean, TestStateListener {

    private static final String NATS_CONNECTION = "natsConnection";
    private static final String NATS_SUBSCRIPTION = "natsSubscription";
    private static final String PUBLISH_SUBJECT = "publishSubject";

    private static final String KEYSTORE_TYPE = "jks";

    // NATS connection config props
    private String natsServers;

    // channel config props
    private String publishSubject;
    private String subscribeSubject;

    // SSL connection config props
    private String keystore;
    private String keystorePw;
    private String truststore;

    public static Connection getNatsConnection() {
        final JMeterVariables vars = JMeterContextService.getContext().getVariables();

        return (Connection) vars.getObject(NATS_CONNECTION);
    }

    public static Subscription getNatsSubscription() {
        final JMeterVariables vars = JMeterContextService.getContext().getVariables();

        return (Subscription) vars.getObject(NATS_SUBSCRIPTION);
    }

    public static String getPublish() {
        final JMeterVariables vars = JMeterContextService.getContext().getVariables();

        return (String) vars.getObject(PUBLISH_SUBJECT);
    }

    @SneakyThrows
    public Options getNatsOptionsForSsl() {
        final SSLContext context = SSLContext.getInstance(Options.DEFAULT_SSL_PROTOCOL);

        // create KeyManagerFactory
        final KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
        try (BufferedInputStream in = new BufferedInputStream(
                new FileInputStream(keystore))) {
            ks.load(in, keystorePw.toCharArray());
        }

        final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, keystorePw.toCharArray());

        // create TrustManagerFactory
        final KeyStore ts = KeyStore.getInstance(KEYSTORE_TYPE);
        try (BufferedInputStream in = new BufferedInputStream(
                new FileInputStream(truststore))) {
            ts.load(in, null);
        }

        final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);

        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        return new Options.Builder()
                .sslContext(context)
                .server(natsServers)
                .build();
    }

    @Override
    public void testStarted() {
        this.setRunningVersion(true);
        TestBeanHelper.prepare(this);
        final JMeterVariables vars = getThreadContext().getVariables();

        if (!Objects.isNull(vars.getObject(NATS_CONNECTION))) {
            log.warn("NATS connection is already present.");
            return;
        }

        try {
            final Connection nc;

            if (!getKeystore().trim().isEmpty()) {
                nc = Nats.connect(getNatsOptionsForSsl());
            } else {
                nc = Nats.connect(natsServers);
            }

            final Subscription sub = nc.subscribe(subscribeSubject);

            vars.putObject(NATS_CONNECTION, nc);
            vars.putObject(NATS_SUBSCRIPTION, sub);
            vars.putObject(PUBLISH_SUBJECT, publishSubject);

            log.info("NATS connection made successfully.");
        } catch (Exception e) {
            log.error("Exception encountered while creating connection: {}", e.getMessage());
        }
    }

    @Override
    public void testEnded() {
        final JMeterVariables vars = getThreadContext().getVariables();

        if (Objects.isNull(vars.getObject(NATS_CONNECTION))) {
            return;
        }

        try {
            ((Subscription) vars.getObject(NATS_SUBSCRIPTION)).unsubscribe();
            ((Connection) vars.getObject(NATS_CONNECTION)).close();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void testStarted(String s) {
        testStarted();
    }

    @Override
    public void testEnded(String s) {
        testEnded();
    }
}
