package com.solace.se.samples.gcppubsub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gcp.pubsub.integration.outbound.PubSubMessageHandler;
import org.springframework.stereotype.Service;

import com.solace.se.samples.gcppubsub.SolaceGcpPubSubApplication.PubsubOutboundGateway;
import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Queue;
import com.solacesystems.jcsmp.SpringJCSMPFactory;

@Service
public class SolaceMsgConsumer implements InitializingBean, DisposableBean {

	private static final Logger logger = LoggerFactory.getLogger(SolaceMsgConsumer.class);

	private JCSMPSession session;
	private FlowReceiver queueFlow;
	
	//Name of the Solace Queue to consume messages that will get sent to GCP PubSub
	@Value("${solace.connector.solace.sourcequeuename}")
	String queueName;

	//Solace Base Topic to be removed from the messages that will get sent to GCP PubSub
	@Value("${solace.connector.solace.basetopic}")
	String solaceBaseTopic;
    
	@Autowired
	private PubsubOutboundGateway messagingGateway;

	@Autowired
	private PubSubMessageHandler adapter;
	
	@Autowired 
    private SpringJCSMPFactory solaceFactory;

	//Initialize the Solace consumer by Establishing the connection and binding to the queue after the Bean has been instantiated by Spring
	@Override
	public void afterPropertiesSet() throws Exception {
		
		SolaceMsgListener solMsgListener = new SolaceMsgListener(messagingGateway, adapter, solaceBaseTopic);
		
		session = solaceFactory.createSession();
		
		final Queue queue = JCSMPFactory.onlyInstance().createQueue(queueName);
		
		final ConsumerFlowProperties flow_prop = new ConsumerFlowProperties();
		flow_prop.setEndpoint(queue);
		flow_prop.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
		
		queueFlow = session.createFlow(solMsgListener,flow_prop);
		
		logger.info("##### Solace Consumer Connected.. Awaiting message #####");
		queueFlow.start();
	
	}

	@Override
	public void destroy() throws Exception {
		try {
			logger.info("PreDestroy - Close Solace consumer");
	
			// Close consumer
			queueFlow.close();
	        logger.info("Exiting.");
	        session.closeSession();
	        
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}
}