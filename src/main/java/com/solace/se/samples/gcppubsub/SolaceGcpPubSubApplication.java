package com.solace.se.samples.gcppubsub;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.integration.AckMode;
import org.springframework.cloud.gcp.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.cloud.gcp.pubsub.integration.outbound.PubSubMessageHandler;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.springframework.cloud.gcp.pubsub.support.GcpPubSubHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.solacesystems.jcsmp.JCSMPException;


@SpringBootApplication
public class SolaceGcpPubSubApplication {

	private static final Logger logger = LoggerFactory.getLogger(SolaceGcpPubSubApplication.class);

	public static void main(String[] args) throws IOException {
		SpringApplication.run(SolaceGcpPubSubApplication.class, args);
	}

	//Google PubSub Publish Logic
	@Autowired
	private PubSubMessageHandler adapter;
	
	@Autowired
	private SolaceMsgPublisher solPublisher;
	
	@Value("${solace.connector.pubsub.sourcesubscriptionname}")
	String pubsubSubName;
	
	
	@Bean
	@ServiceActivator(inputChannel = "pubsubOutputChannel")
	public MessageHandler messageSender(PubSubTemplate pubsubTemplate) {
		return adapter;
	}
	  
	  
	@Bean
	public PubSubMessageHandler gcpMessageHandler(PubSubTemplate pubsubTemplate) {
  
		PubSubMessageHandler adapter = new PubSubMessageHandler(pubsubTemplate, "defaultTopic");
		adapter.setPublishTimeout(3000);
		adapter.setSync(true);
		adapter.setPublishCallback(new ListenableFutureCallback<String>() {
			@Override
			public void onFailure(Throwable ex) {
				logger.error("There was an error sending the message to Google PubSub");
			}
			@Override
				public void onSuccess(String result) {
			    logger.info("Message was sent successfully to Google PubSub");
			}
		});
		  
		return adapter;
	}
	  
	
	@MessagingGateway(defaultRequestChannel = "pubsubOutputChannel")
	public interface PubsubOutboundGateway {
		void sendToPubsub(String text);
	}
	

	//Google PubSub Subscribe Logic
	
	@Bean
	public PubSubInboundChannelAdapter messageChannelAdapter( @Qualifier("pubsubInputChannel") MessageChannel inputChannel, PubSubTemplate pubSubTemplate) {
		PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, pubsubSubName);
		adapter.setOutputChannel(inputChannel);
		adapter.setAckMode(AckMode.MANUAL);
		return adapter;
	}
	
	@Bean
	public MessageChannel pubsubInputChannel() {
		return new DirectChannel();
	}
	
	@Bean
	@ServiceActivator(inputChannel = "pubsubInputChannel")
	public MessageHandler messageReceiver() {
		return message -> {
			BasicAcknowledgeablePubsubMessage originalMessage = message.getHeaders().get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
			logger.info("============= Received a message from Google Subscription ["+ pubsubSubName +"]");
			
			try {
				
				solPublisher.sendSolaceMsg( new String((byte[]) message.getPayload()) , originalMessage );
				
			} catch (JCSMPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		    
		};
	}
}
