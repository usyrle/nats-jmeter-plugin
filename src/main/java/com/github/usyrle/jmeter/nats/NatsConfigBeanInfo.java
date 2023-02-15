package com.github.usyrle.jmeter.nats;

import java.beans.PropertyDescriptor;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TypeEditor;

public class NatsConfigBeanInfo extends BeanInfoSupport {

    private static final String NATS_SERVERS = "natsServers";

    private static final String PUBLISH_SUBJECT = "publishSubject";
    private static final String SUBSCRIBE_SUBJECT = "subscribeSubject";

    private static final String KEYSTORE = "keystore";
    private static final String KEYSTORE_PW = "keystorePw";
    private static final String TRUSTSTORE = "truststore";

    public NatsConfigBeanInfo() {
        super(NatsConfig.class);

        PropertyDescriptor p;

        this.createPropertyGroup("NATS Connection Config", new String[]{NATS_SERVERS});

        p = this.property(NATS_SERVERS);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "nats://localhost:4222");
        p.setDisplayName("NATS Servers");
        p.setShortDescription("One or more NATS servers to connect to. Separate multiples with commas.");

        this.createPropertyGroup("Subscription Config", new String[]{PUBLISH_SUBJECT, SUBSCRIBE_SUBJECT});

        p = this.property(PUBLISH_SUBJECT);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p.setDisplayName("Publish Subject");
        p.setShortDescription("The subject subscribed to by application under test.");

        p = this.property(SUBSCRIBE_SUBJECT);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p.setDisplayName("Subscribe Subject");
        p.setShortDescription("The subject published to by application under test.");

        this.createPropertyGroup("SSL Config", new String[]{KEYSTORE, KEYSTORE_PW, TRUSTSTORE});

        p = this.property(KEYSTORE);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p.setDisplayName("Java Keystore path");
        p.setShortDescription("Path to JKS keystore file. If left blank, SSL will not be enabled.");

        p = this.property(KEYSTORE_PW, TypeEditor.PasswordEditor);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p.setDisplayName("Keystore PW");
        p.setShortDescription("Password for JKS keystore file.");

        p = this.property(TRUSTSTORE);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p.setDisplayName("Java Truststore file");
        p.setShortDescription("Path to JKS truststore file.");
    }
}
