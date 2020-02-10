package com.solace.se.samples.gcppubsub;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;

public class SolacePublishEventHandler implements JCSMPStreamingPublishCorrelatingEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(SolacePublishEventHandler.class);

    public void responseReceived(String messageID) {
        logger.info("Producer received response for msg: " + messageID);
        
    }

    public void handleError(String messageID, JCSMPException e, long timestamp) {
        logger.info("Producer received error for msg: %s@%s - %s%n", messageID, timestamp, e);
        
    }

	@Override
	public void responseReceivedEx(Object key) {
        logger.info("Ex - Producer received response for msg: "+ (String)key);
        
        BasicAcknowledgeablePubsubMessage originalMessage = SolaceMsgPublisher.getGCPOriginalMessage((String) key);
        originalMessage.ack();
		
	}
    
	@Override
	public void handleErrorEx(Object key, JCSMPException e, long timestamp) {
		
		logger.info("Ex - Producer received error for msg ID: [" +(String)key +"] :" , e);
	}

}