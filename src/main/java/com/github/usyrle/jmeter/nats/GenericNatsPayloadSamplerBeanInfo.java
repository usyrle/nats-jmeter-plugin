package com.github.usyrle.jmeter.nats;

import java.beans.PropertyDescriptor;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TypeEditor;

public class GenericNatsPayloadSamplerBeanInfo extends BeanInfoSupport {

    private static final String PAYLOAD_TYPE = "payloadType";
    private static final String PAYLOAD_TIMEOUT = "payloadTimeout";
    private static final String PAYLOAD = "payload";

    public GenericNatsPayloadSamplerBeanInfo() {
        super(GenericNatsPayloadSampler.class);

        PropertyDescriptor p;

        createPropertyGroup("Payload Config", new String[]{PAYLOAD_TYPE, PAYLOAD_TIMEOUT, PAYLOAD});

        p = this.property(PAYLOAD_TYPE, TypeEditor.ComboStringEditor);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(TAGS, PayloadType.getPayloadTypeArray());
        p.setValue(DEFAULT, PayloadType.getPayloadTypeArray()[PayloadType.BASE64.getIndex()]);
        p.setDisplayName("Payload Type");
        p.setShortDescription("Encoding type of payload. Accepts either 'base64' or 'hex' as arguments.");

        p = this.property(PAYLOAD_TIMEOUT);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, 1000L);
        p.setDisplayName("Payload Timeout");
        p.setShortDescription("Timeout for receiving a response, in milliseconds.");

        p = this.property(PAYLOAD, TypeEditor.TextAreaEditor);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p.setDisplayName("Payload");
        p.setShortDescription("Payload to be supplied for load test. Can be base64-encoded or hex-encoded string");
    }
}
