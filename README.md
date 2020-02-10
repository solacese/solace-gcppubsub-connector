# Solace - Google PubSub Connector

## Overview
This repository contains a Gradle - Spring Boot project for bridging TextMessages from Google PubSub to a Solace Broker and Vice versa.

This connector is capable of publishing messages to multiple GCP Topics, in order to do so the "SolaceToGCPQueue" should attract a predefined base topic pattern, the base topic will get trimmed before sending to GCP, and the last Solace topic level will get used as the GCP topic to send the message to.  

```
Example: 

The solace queue "SolaceToGCPQueue" attracts the topic pattern "solace/to/gcppubsub/*"

On the application properties, "solace.connector.solace.basetopic" is defined as "solace/to/gcppubsub/"

Messages published on Solace to the topic "solace/to/gcppubsub/GCPTopic1" will get published to the GCP PubSb Topic "GCPTopic1" 
```

> :information_source: For messages to be successfully published to GCP PubSub, the topic "GCPTopic1" has to be previously created. If the topic doesn't exist on GCP an expection will be returned to the connector and the message will not be ACKed to the solace queue.

### Warning

> :warning: This project is intended to serve as a POC for demonstrating the integration between a Solace Broker and the Google PubSub Only. Therefore, there are several opportunities for improvement.    
> :warning: Keep in mind that this code has not been tested or coded to be PRODUCTION ready.

## Prerequisites

Solace Broker
+ Broker URL
+ VPN on the broker
+ Topic to send the messages consumed from GCP PubSub 
+ Exclusive Queue that attracts the previous topic (for validating that messages are being received in Solace) - ex: GCPToSolaceQueue
+ Exclusive Queue to store messages to be sent to GCP PubSub - ex: SolaceToGCPQueue
+ Client-Username with permission to publish and send guaranteed messages, that owns the SolaceToGCPQueue queue or has permission to consume it

GCP PubSub
+ GCP Project 
+ At least one GCP Pub/Sub Topic to receive messages from Solace - ex: projects/gcpsolace/topics/GCPTopic1
+ At least one GCP Pub/Sub Subscription to the previous Topic (for validating that messages are being received in GCP PubSub) - ex: projects/gcpsolace/subscriptions/GCPTopic1_Sub1 
+ At least one GCP Pub/Sub Topic to send messages to Solace - ex: projects/gcpsolace/topics/GCPToSolaceTopic 
+ At least one GCP Pub/Sub Subscription to the previous Topic, for sending messages to Solace - ex: projects/gcpsolace/subscriptions/GCPToSolaceTopic_Sub1 
+ Service account with the "Pub/Sub Publisher" & "Pub/Sub Subscriber" roles
+ JSON Key for the previous service account
 
### 

The previous values need to be set as properties on the application.properties file:

```
solace.java.host=tcp://solace01:55555
solace.java.msg-vpn=<vpn>
solace.java.client-username=<username>
solace.java.client-password=<password>

spring.cloud.gcp.project-id=<GCP_ProjectID>
spring.cloud.gcp.credentials.location=file:<ServiceAccount_JSON_Key_Path>

solace.connector.solace.sourcequeuename=<QueueName>
solace.connector.solace.basetopic=<BaseTopicName>

solace.connector.solace.desttopicname=<TopicName>
solace.connector.pubsub.sourcesubscriptionname=<GCPSub_Name>
```

## Checking out

To check out the project, clone this GitHub repository:

```
git clone https://github.com/solacese/solace-gcppubsub-connector.git
cd solace-gcppubsub-connector
```

## Building and running directly with gradlew

To have gradle build and run the application just after downloading this GIT Repository, you can simply run the following command on your console: 
 
```
$ ./gradlew bootRun
```
> :information_source: Remember to set appropriate values on the application.properties file before running the command

## Building a bootJar

To have gradle create a JAR containing all the compiled classes plus all the required libs, you can run the following command on your console: 
 
```
$ ./gradlew build
```

A JAR file named solace-gcppubsub-connector will be created on the ./build/libs folder. 
Once the JAR has been created simply run it using the following command:

```
$ java -jar solace-gcppubsub-connector.jar
```

> :information_source: In order to provide redundancy, multiple instances can be run at the same. Since the Sol.SolaceToAzure queue is an exclusive queue, only the first instance will actively consume messages from it.

> :warning: Since the application.properties is contained within the generated JAR, changes on the configuration values will require to rerun the  ./gradlew build command

## Authors

See the list of [contributors](https://github.com/solacese/solace-gcppubsub-connector/graphs/contributors) who participated in this project.


## Resources

For more information try these resources:

- Spring Boot Integration Messaging with Google Cloud Pub/Sub at: https://spring.io/guides/gs/messaging-gcp-pubsub/
- GCP Cloud Pub/Sub Setup & Sring Boot Integration at: https://dzone.com/articles/spring-boot-and-gcp-cloud-pubsub
- GCP Pub/Sub access control at: https://cloud.google.com/pubsub/docs/access-control
- GCP Creating and managing service account at: https://cloud.google.com/iam/docs/creating-managing-service-accounts
- GCP Creating and managing service account keys at: https://cloud.google.com/iam/docs/creating-managing-service-account-keys
- GCP Publishing messages via REST: 
  -  https://cloud.google.com/pubsub/docs/publisher
  -  https://cloud.google.com/pubsub/docs/reference/rest/v1/projects.topics/publish
  -  https://stackoverflow.com/questions/48210592/gcp-pubsub-publish-message-via-curl-type-of-request

- The Solace Developer Portal website at: http://dev.solace.com
- Get a better understanding of [Solace technology](http://dev.solace.com/tech/).
- Check out the [Solace blog](http://dev.solace.com/blog/) for other interesting discussions around Solace technology
- Ask the [Solace community.](http://dev.solace.com/community/)