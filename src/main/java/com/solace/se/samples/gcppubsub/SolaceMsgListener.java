package com.solace.se.samples.gcppubsub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gcp.pubsub.integration.outbound.PubSubMessageHandler;

import com.solace.se.samples.gcppubsub.SolaceGcpPubSubApplication.PubsubOutboundGateway;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.XMLMessageListener;


public class SolaceMsgListener implements XMLMessageListener {

	private static final Logger logger = LoggerFactory.getLogger(SolaceMsgListener.class);
	
	private PubsubOutboundGateway messagingGateway;
	private PubSubMessageHandler adapter;
	private String solaceBaseTopic;
	
	public SolaceMsgListener(PubsubOutboundGateway messagingGateway, PubSubMessageHandler adapter, String solaceBaseTopic) {
		this.messagingGateway = messagingGateway;
		this.adapter = adapter;
		this.solaceBaseTopic = solaceBaseTopic;
	}
	
    public void onReceive(BytesXMLMessage msg) {

    	//Trim Solace topic space
    	String solaceTopic = msg.getDestination().getName();
		String gcpTopic = solaceTopic.replaceFirst( solaceBaseTopic , "");
    	
    	//Set Google PunSub Topic dynamically
    	adapter.setTopic(gcpTopic);
    	
    	try {
    		
    		logger.info("============= Received a message from SolaceTopic ["+ solaceTopic +"]");
    		String PayloadAsText = new String(msg.getBytes());
    		logger.info("============= Sending message from Solace to GCP PubSub Topic [" + gcpTopic + "] - Payload ["+PayloadAsText+"]");

	    	messagingGateway.sendToPubsub(PayloadAsText);
	    	msg.ackMessage();
    	}
    	catch(Exception e) {
    		logger.error("Error while sending to Google PubSub", e);
    	}
    }

    public void onException(JCSMPException e) {
        logger.info("Consumer received exception:", e);
    }

}

