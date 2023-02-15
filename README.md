# NATS JMeter Plugin

A plugin for JMeter to send and receive messages to NATS-connected applications.

## Usage

1. Clone this repository.
2. Run `mvn package` to assemble a fat jar with required dependencies.
3. Copy `target/jmeter-plugin-0.0.1-SNAPSHOT.jar` to directory `lib/ext` in your JMeter install.
4. Run JMeter.
5. In your Test Plan, add a Thread Group, a "NATS Config" Config Element, and a "Generic NATS Sampler" Sampler.
6. In the "NATS Config" Config Element:
   1. Fill in your NATS server URI. If you have multiple URIs, separate them with commas.
   2. Fill in your NATS subjects for subscription and publish.
     * The Publish Subject should be the subject your application-under-test will receive messages on.
     * The Subscribe Subject should be the subject your application-under-test will send messages to.
   3. If you need SSL for your NATS connection, add paths to the correct JKS files for Keystore and Truststore, and add the Keystore password.
     * If the "Java Keystore path" field is left blank, the NATS connection will be created without SSL.
7. In the "Generic NATS Sampler" Sampler:
   1. Choose a Payload Type of base64 or hex, and add the appropriately-encoded payload to the Payload text area.
   2. Set a response timeout in milliseconds.
7. Save and execute the Test Plan.

## Structure

* [NatsConfig](src/main/java/com/github/usyrle/jmeter/nats/NatsConfig.java) sets up a NATS connection (optionally with an SSLContext) and a subject subscription, and shares these in the JMeter context.
* [GenericNatsPayloadSampler](src/main/java/com/github/usyrle/jmeter/nats/GenericNatsPayloadSampler.java) sends a basic payload of bytes to a NATS subject (configured by NatsConfig), and waits up to a timeout for a response on a NATS subject.
* [PayloadType](src/main/java/com/github/usyrle/jmeter/nats/PayloadType.java) is a simple enum which generates the correct array for GenericNatsPayloadSampler to display and understand whether a payload is base64 or hex.
  * Additional payload types should be as simple as adding to this enum, then handling the new case in `GenericNatsPayloadSampler.getPayloadBytes()`.

## JMeter How-To

There's a real dearth of up-to-date JMeter plugin how-tos out there, so I'm sharing useful tidbits if you'd like to contribute, customize, or create your own.

* JMeter components using the "TestBean" framework consist of three different files, which must same package / directory:
    * `${Component}.java` should implement the `TestBean` interface, and should extend one of the various component type superclasses.
        * This file is the main class driving the component, any actual logic will live in here.
        * Of the component superclasses, this plugin uses a Config Element (`ConfigTestElement`) and a Sampler (`AbstractSampler`).
          * "Config Elements" generally set up shared resources for use across a Thread Group.
          * "Samplers" generally build, send, and receive payloads on a thread, and measure statistics.
    * `${Component}BeanInfo.java` should extend the `BeanInfoSupport` class.
        * This file sets up the properties available to a user in the GUI, collected inside one or more property groups.
        * The order the property groups are declared in, and the order in which the properties are given in the arguments, are the order in which they will display in the JMeter GUI.
        * **Make sure that every property declared** has a matching variable, getter, and setter in `${Component}.java`. If these properties do not match exactly, you will get weird behavior in the GUI (or fields won't render at all).
    * `${Component}Resources.properties` should be a basic properties file.
        * This file should be within `src/main/resources`, under a path exactly matching the package of `${Component}.java`.
        * This file can optionally be accompanied by additional localization files, e.g. `${Component}Resources_en_EN.properties`, as part of a resource bundle.
        * A number of tutorials populate this file with additional values (e.g. name, description) for each property you declare, but it's not strictly necessary. The TestBean framework allows most GUI display elements to be declared and tweaked in `${Component}BeanInfo.java`.
          * That said, the `displayName` property is set for each component within this plugin in order to display correctly in JMeter dropdowns.
* JMeter is quite picky about what interfaces can go together. For example, at one point I implemented `TestStateListener` in the Sampler to create a subscription on init and close on end, but it simply didn't work at all. As far as I can tell a `TestStateListener` needs to be a separate component entirely from the Sampler.
  * If something isn't working as you expect, there's a decent chance it's just something JMeter doesn't allow. And unfortunately, I couldn't find any documentation on what you can/can't mix and match.
* JMeter requires plugin jars to include their dependencies, so the Maven Shade plugin is used to generate an "uber jar."
  * The easiest way to minimize the jar size is to configure Shade with `<minimizeJar>true</minimizeJar>`, then declare an include-set of dependencies. Shade will then exclude everything else by default.

### Future TODO
* Generic fire-and-forget Sampler

## References and Resources

* [Creating a JMeter Plugin (especially the Test Bean section)](https://jmeter.apache.org/usermanual/jmeter_tutorial.html)
* [A Blazemeter guide for developing a plugin](https://www.blazemeter.com/blog/jmeter-plugin-development)
* [Another guide to developing a plugin, with the Test Bean framework](https://codyaray.com/2014/07/custom-jmeter-samplers-and-config-elements)
* [An extension of the apparently-defunct Kafkameter](https://github.com/rollno748/di-kafkameter)
* [NATS Java reference](https://github.com/nats-io/nats.java)
* Various JMeter bundled components:
  * [The CSVDataSet ConfigElement](https://github.com/apache/jmeter/blob/master/src/components/src/main/java/org/apache/jmeter/config/CSVDataSet.java)
    * ...[and associated BeanInfo class](https://github.com/apache/jmeter/blob/master/src/components/src/main/java/org/apache/jmeter/config/CSVDataSetBeanInfo.java)
    * ...[and associated Resources file](https://github.com/apache/jmeter/blob/master/src/components/src/main/resources/org/apache/jmeter/config/CSVDataSetResources.properties)
  * [The HTTPSamplerBase class shared across HTTP samplers](https://github.com/apache/jmeter/blob/master/src/protocol/http/src/main/java/org/apache/jmeter/protocol/http/sampler/HTTPSamplerBase.java)

## Licensing
For licensing information, please see [LICENSE](LICENSE) and [NOTICE](NOTICE).
