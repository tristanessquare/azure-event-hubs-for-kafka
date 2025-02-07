# Send and Receive Messages in Java using Azure Event Hubs for Apache Kafka Ecosystems with Managed Identity OAuth

This tutorial will show how to create and connect to an Event Hubs Kafka endpoint using an example producer and consumer written in Java. Azure Event Hubs for Apache Kafka Ecosystems supports [Apache Kafka version 1.0](https://kafka.apache.org/10/documentation.html) and later.

## Prerequisites

If you don't have an Azure subscription, create a [free account](https://azure.microsoft.com/free/?ref=microsoft.com&utm_source=microsoft.com&utm_medium=docs&utm_campaign=visualstudio) before you begin.

In addition:

* [Java Development Kit (JDK) 17+](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
    * On Ubuntu, run `apt-get install default-jdk` to install the JDK.
    * Be sure to set the JAVA_HOME environment variable to point to the folder where the JDK is installed.
* [Download](http://maven.apache.org/download.cgi) and [install](http://maven.apache.org/install.html) a Maven binary archive
    * On Ubuntu, you can run `apt-get install maven` to install Maven.
* [Git](https://www.git-scm.com/downloads)
    * On Ubuntu, you can run `sudo apt-get install git` to install Git.
    
## Create (or configure existing) Virtual Machine
There is currently no Managed Identity emulator so in order to use Managed Identity, you must create a (or configure an existing) virtual machine using a system-assigned managed identity. 
See [Quickstart: Create a Linux virtual machine in the Azure portal](https://docs.microsoft.com/en-us/azure/virtual-machines/linux/quick-create-portal#create-virtual-machine) and [Configure managed identities for Azure resources on a VM using the Azure portal](https://docs.microsoft.com/en-us/azure/active-directory/managed-identities-azure-resources/qs-configure-portal-windows-vm)

You will have install or confirm installation of the [required tools](#prerequisites) on your virtual machine.  

Note: it is strongly recommended to not open ssh ports to the open internet. We recommend [enabling Bastion](https://docs.microsoft.com/en-us/azure/bastion/quickstart-host-portal) and remoting into your VM that way.

## Create an Event Hubs namespace

An Event Hubs namespace is required to send or receive from any Event Hubs service. See [Create Kafka-enabled Event Hubs](https://docs.microsoft.com/azure/event-hubs/event-hubs-create-kafka-enabled) for instructions on getting an Event Hubs Kafka endpoint. Make sure to copy the Event Hubs connection string for later use.

## Add managed identity to Event Hubs namespace access control

In the Azure Portal, navigate to your Event Hubs namespace. Go to "Access Control (IAM)" in the left navigation. 

Click + Add and select "Add role assignment."

In the Role tab, select "Azure Event Hubs Data Owner" and click the Next button.

In the Members tab, select the Managed Identity radio button for type to assign access to.

Click the + Select members link.

In the Managed Identity dropdown, select Virtual Machine and select your virtual machine's managed identity.

Click "Review + Assign."

### FQDN

For these samples, you will need the Fully Qualified Domain Name of your Event Hubs namespace which can be found in Azure Portal. To do so, in Azure Portal, go to your Event Hubs namespace overview page and copy host name which should look like `**`mynamespace.servicebus.windows.net`**`.

If your Event Hubs namespace is deployed on a non-Public cloud, your domain name may differ (e.g. \*.servicebus.chinacloudapi.cn, \*.servicebus.usgovcloudapi.net, or \*.servicebus.cloudapi.de).

## Clone the example project

Now that you have a Kafka-enabled Event Hubs connection string, clone the Azure Event Hubs for Kafka repository in [your Azure VM](#create-or-configure-existing-virtual-machine) and navigate to the `quickstart/java` subfolder:

```bash
git clone https://github.com/Azure/azure-event-hubs-for-kafka.git
cd azure-event-hubs-for-kafka/tutorials/oauth/java/managedidentity
```

## Client Configuration For OAuth
Kafka clients need to be configured in a way that they can authenticate with Azure Active Directory and fetch OAuth access tokens. These tokens then can be used to get authorized while accessing certain Event Hubs resources.

#### Here is a list of authentication parameters for Kafka clients that needs to be configured for all clients

* Set SASL mecnahism to OAUTHBEARER

   `sasl.mechanism=OAUTHBEARER`
* Set Java Authentication and Authorization Service (JAAS) configuration to OAuthBearerLoginModule

   `sasl.jaas.config=org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;`
* Set login callback handler. This is the authentication handler which is responsible to complete oauth flow and return an access token.

   `sasl.login.callback.handler.class=de.microsoft.examples.AzureAuthenticateCallbackHandler;`

## Producer

Using the provided producer example, send messages to the Event Hubs service. To change the Kafka version, change the dependency in the pom file to the desired version.

### Provide an Event Hubs Kafka endpoint

#### producer.config

Update the `bootstrap.servers` in `producer/src/main/resources/producer.config` to direct the producer to the Event Hubs Kafka endpoint.

```config
bootstrap.servers=mynamespace.servicebus.windows.net:9093 # REPLACE
security.protocol=SASL_SSL
sasl.mechanism=OAUTHBEARER
sasl.jaas.config=org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;
sasl.login.callback.handler.class=de.microsoft.examples.AzureAuthenticateCallbackHandler;
```

### Run producer from command line

This sample is configured to send messages to topic `test`, if you would like to change the topic, change the TOPIC constant in `producer/src/main/java/de/microsoft/examples/TestProducer.java`.

To run the producer from the command line, generate the JAR and then run from within Maven (alternatively, generate the JAR using Maven, then run in Java by adding the necessary Kafka JAR(s) to the classpath):

```bash
mvn clean package
mvn exec:java -Dexec.mainClass="de.microsoft.examples.TestProducer"
```

The producer will now begin sending events to the Kafka-enabled Event Hub at topic `test` (or whatever topic you chose) and printing the events to stdout. 

## Consumer

Using the provided consumer example, receive messages from the Kafka-enabled Event Hubs. To change the Kafka version, change the dependency in the pom file to the desired version.

### Provide an Event Hubs Kafka endpoint

#### consumer.config

Change the `bootstrap.servers` in `consumer/src/main/resources/consumer.config` to direct the consumer to the Event Hubs endpoint.

```config
bootstrap.servers=mynamespace.servicebus.windows.net:9093 # REPLACE
group.id=$Default
request.timeout.ms=60000
security.protocol=SASL_SSL
sasl.mechanism=OAUTHBEARER
sasl.jaas.config=org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;
sasl.login.callback.handler.class=de.microsoft.examples.AzureAuthenticateCallbackHandler;
```

### Run consumer from command line

This sample is configured to receive messages from topic `test`, if you would like to change the topic, change the TOPIC constant in `consumer/src/main/java/de/microsoft/examples/TestConsumer.java`.

To run the producer from the command line, generate the JAR and then run from within Maven (alternatively, generate the JAR using Maven, then run in Java by adding the necessary Kafka JAR(s) to the classpath):

```bash
mvn clean package
mvn exec:java -Dexec.mainClass="de.microsoft.examples.TestConsumer"
```

If the Kafka-enabled Event Hub has incoming events (for instance, if your example producer is also running), then the consumer should now begin receiving events from topic `test` (or whatever topic you chose).

By default, Kafka consumers will read from the end of the stream rather than the beginning. This means any events queued before you begin running your consumer will not be read. If you started your consumer but it isn't receiving any events, try running your producer again while your consumer is polling. Alternatively, you can use Kafka's [`auto.offset.reset` consumer config](https://kafka.apache.org/documentation/#newconsumerconfigs) to make your consumer read from the beginning of the stream!
