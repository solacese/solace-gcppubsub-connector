package com.solace.se.samples.gcppubsub;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.springframework.stereotype.Service;

import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.SpringJCSMPFactory;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageProducer;

@Service
public class SolaceMsgPublisher implements InitializingBean, DisposableBean {

	private static final Logger logger = LoggerFactory.getLogger(SolaceMsgPublisher.class);

	private static ConcurrentHashMap<String, BasicAcknowledgeablePubsubMessage> inFlightMessages  = new ConcurrentHashMap<>();
	
	private JCSMPSession session;
	private XMLMessageProducer producer;
	private Topic topic;
	
	//Name of the Solace Topic where the Consumed messages from Azure Service Bus will get published
	@Value("${solace.connector.solace.desttopicname}")
	String solTopicName;

	@Autowired 
    private SpringJCSMPFactory solaceFactory;

	//Initialize the Solace Publisher by Establishing the connection and creating an XMLProducer after the Bean has been instantiated by Spring
	@Override
	public void afterPropertiesSet() throws Exception {
		
		SolacePublishEventHandler pubEventHandler = new SolacePublishEventHandler();
		
		session = solaceFactory.createSession();
		
		producer = session.getMessageProducer(pubEventHandler);
	
		topic = JCSMPFactory.onlyInstance().createTopic(solTopicName);
		
		logger.info("##### Solace Publisher Connected. Ready to publish #####");
	}

	public void sendSolaceMsg(String textPayload, BasicAcknowledgeablePubsubMessage originalMessage) throws JCSMPException {
		
		String pubsubMsgId = originalMessage.getPubsubMessage().getMessageId();
		
		TextMessage jcsmpMsg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
	    jcsmpMsg.setText(textPayload);
	    jcsmpMsg.setDeliveryMode(DeliveryMode.PERSISTENT);
	    
	    jcsmpMsg.setCorrelationKey(pubsubMsgId);
	
	    inFlightMessages.put(pubsubMsgId, originalMessage);
	    logger.info("inFlightMessage ID stored :" + pubsubMsgId);
	    
	    logger.info("============= Sending message from GCP PubSub to Solace Topic ["+solTopicName+"] - ID [" + pubsubMsgId +"] - Payload [" + textPayload+ "]");
	    producer.send(jcsmpMsg, topic);
	    
	}
	
	public static BasicAcknowledgeablePubsubMessage getGCPOriginalMessage(String solMessageID) {
		return inFlightMessages.get(solMessageID);
	}
	
	
	@Override
	public void destroy() throws Exception {
		try {
			logger.info("destroy - Close Solace Publisher");
	
			// Close consumer
	        logger.info("Exiting.");
	        session.closeSession();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}
}