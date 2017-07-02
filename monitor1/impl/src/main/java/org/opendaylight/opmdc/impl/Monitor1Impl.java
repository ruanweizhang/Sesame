/*
 * Copyright © 2016 opmdc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.opmdc.impl;

//import static org.mockito.Mockito.mock; 
//import static org.mockito.Mockito.when; 
 
import java.lang.*;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;  
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.CheckedFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opendaylight.controller.md.sal.binding.api.DataBroker; 
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;

//import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32; 
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64; 


import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.BytesBuilder; 
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.PacketsBuilder;

import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData; 
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.FlowCapableNodeConnectorQueueStatisticsData; 
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.FlowCapableNodeConnectorQueueStatisticsDataBuilder; 
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.flow.capable.node.connector.queue.statistics.FlowCapableNodeConnectorQueueStatistics; 
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.flow.capable.node.connector.queue.statistics.FlowCapableNodeConnectorQueueStatisticsBuilder;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder; 

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodesBuilder; 
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdatedBuilder; 

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId; 
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef; 
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector; 
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorBuilder; 
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector; 
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorBuilder; 

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.Queue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.QueueBuilder;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.StateBuilder;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.Queue; 
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.QueueBuilder; 
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.QueueKey;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.QueueId; 
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;


import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.OpendaylightFlowStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.OpendaylightFlowTableStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;


import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTosActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4;

public class Monitor1Impl { //implements DataChangeListener{
	private static final Logger LOG = LoggerFactory.getLogger(OpmdcProvider.class);
	private DataBroker dataBroker = null;
	//private ReadOnlyTransaction readTransaction = null; 
    //private Nodes nodes = null; 
    //private CheckedFuture<Optional<Nodes>, ReadFailedException> checkedFuture;// = mock(CheckedFuture.class); 
    //private Optional<Nodes> optional;// = mock(Optional.class);
    private String switchName = "openflow:1";
	private MacAddress sw1mac = new MacAddress("08:9e:01:9a:8a:23");
	
	private double avgTableElapsedTime;
	private double avgNodeConnectorElapsedTime;
	private int tableCount;
	private int nodeConnectorCount;
	private int loopCount;

	public Monitor1Impl(DataBroker dataBroker){//, DataProviderService dataProviderService) {
		LOG.info("Monitor1Impl Session Initiated!!!");
		if (dataBroker == null){
			System.out.println("Monitor1Impl dataBroker is null!!!");
			return;
		}

		this.dataBroker = dataBroker; //this.dataProviderService = dataProviderService;
		//this.readTransaction = dataBroker.newReadOnlyTransaction();
		this.loopCount = 0;
		this.avgTableElapsedTime = 0;
		this.avgNodeConnectorElapsedTime = 0;
		this.tableCount = 0;
		this.nodeConnectorCount = 0;

		//nodes = buildNodeList();
		try {
			Thread.sleep(10000); //1000 milliseconds is one second.
		} catch(InterruptedException ex) { //Add Interrupt to exit
			Thread.currentThread().interrupt();
		}

		while (this.loopCount < 3){ //while (this.dataBroker != null){ //  
			try {
				Thread.sleep(100); //1000 milliseconds is one second.
			} catch(InterruptedException ex) { //Add Interrupt to exit
				Thread.currentThread().interrupt();
			}
			if (Thread.interrupted()){
				break; // Break the while dataBroker loop
			}

			switchTraffic(this.switchName, this.sw1mac, this.dataBroker); //For reading SDN SW table[0]

			this.loopCount++;
		} // End of the while dastaBroker loop

		if (this.tableCount != 0){
			//System.out.println("Monitor1Impl readData Elapsed Time(nano secs): " + (endTime - startTime));// / 1000000);
			LOG.info("Monitor1Impl Table AVG Elapsed Time(nano secs): {}", this.avgTableElapsedTime/(double)this.tableCount);
		}
		if (this.nodeConnectorCount != 0){
			//System.out.println("Monitor1Impl readData Elapsed Time(nano secs): " + (endTime - startTime));// / 1000000);
			LOG.info("Monitor1Impl NodeConnector AVG Elapsed Time(nano secs): {}", this.avgNodeConnectorElapsedTime/(double)this.nodeConnectorCount);
		}
	} // End of public Monitor1Impl(DataBroker dataBroker)

	private void switchTraffic(String switchName, MacAddress mac, DataBroker dataBroker){
		NodeId nodeId = new NodeId(switchName);
		//readTablefromSwitch(switchName); // Getting SDN SW flow table info. //SUCCESS
		
		long portNumber = 12;
		System.out.println("readNC port 12");
		readNodeConnectorFromNodeIdandPort(nodeId, portNumber);
		portNumber = 16;
		System.out.println("readNC port 16");
		readNodeConnectorFromNodeIdandPort(nodeId, portNumber);
	}

	private void readNodeConnectorFromNodeIdandPort(NodeId nodeId, long portNumber){
		NodeConnectorId ncId = InventoryUtils.getNodeConnectorId(nodeId, portNumber);
		
		InstanceIdentifier<Node> nodeIId = createNodeIId(nodeId);
		//Want it SUCCESS
		//InstanceIdentifier<NodeConnector> nodeConnectorIId = nodeIId.child(NodeConnector.class, new NodeConnectorKey(nodeConnector.getId()));
		InstanceIdentifier<NodeConnector> nodeConnectorIId = nodeIId.child(NodeConnector.class, new NodeConnectorKey(ncId));
		NodeConnector nodeConnector = GenericTransactionUtils.readData(this.dataBroker, LogicalDatastoreType.OPERATIONAL, nodeConnectorIId);
		if (nodeConnector != null) {
			//System.out.println("NodeId and Port Monitor1Impl NC info.: " + nodeConnector);
			LOG.info("NodeId and Port Monitor1Impl NodeID {} NC SUCCESS!!!", nodeId);
		} //End of if (table != null) statement
		else{
			//LOG.info("NodeId and Port Monitor1Impl NodeID {} NC is null!!!", nodeId);
		}
		//Want it SUCCESS
		//parsePortInfo(String.valueOf(nodeConnector));
//The newest try
		//Node node = ;
		NodeConnector nc = nodeConnector;
		FlowCapableNodeConnectorStatisticsData fnc = nc.getAugmentation(FlowCapableNodeConnectorStatisticsData.class);
        if (fnc != null){
        	System.out.println("fnc SUCCESS!!!");
	        InstanceIdentifier<FlowCapableNodeConnectorStatisticsData> tIID = InstanceIdentifier
	                .create(Nodes.class)
	                .child(Node.class, new NodeKey(new NodeId(nodeId))) // origin:node.getKey()
	                .child(NodeConnector.class, nc.getKey())
	                .augmentation(
	                        FlowCapableNodeConnectorStatisticsData.class);
	        FlowCapableNodeConnectorStatisticsData fcncsd = GenericTransactionUtils.readData(this.dataBroker, LogicalDatastoreType.OPERATIONAL, tIID);
	        if (fcncsd!=null){
	        	System.out.println("fcncsd SUCCESS!!!");
	        	System.out.println(fcncsd);
	        }
		}
		else{
			System.out.println("fnc is NULL!!!QQQQQQQQQQQQQ");
		}

		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!QueueStatistics Trial!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		FlowCapableNodeConnector fcnc = nc.getAugmentation(FlowCapableNodeConnector.class);
	    if (fcnc != null) {
			System.out.println("fcnc SUCCESS!!!");
			System.out.println(fcnc);
			List<Queue> queues = fcnc.getQueue();
            if (queues != null) {
                for (Queue q : queues) {
                    InstanceIdentifier<FlowCapableNodeConnectorQueueStatisticsData> tIID2 = InstanceIdentifier
                            .create(Nodes.class)
                            .child(Node.class, new NodeKey(new NodeId(nodeId)))
                            .child(NodeConnector.class,
                                    nc.getKey())
                            .augmentation(
                                    FlowCapableNodeConnector.class)
                            .child(Queue.class, q.getKey())
                            .augmentation(
                                    FlowCapableNodeConnectorQueueStatisticsData.class);
                }
            }
            else{
            	System.out.println("Queue is NULL!!!QQQQQQQQQQQQQ");
            }
    	}
    	else{
			System.out.println("fcnc is NULL!!!QQQQQQQQQQQQQ");
		}
//Try one
		//DataObject dataObject = (DataObject)nodeConnector;
		//System.out.println(dataObject);
		/*FlowCapableNodeConnectorQueueStatisticsData stData = (FlowCapableNodeConnectorQueueStatisticsData) dataObject; // Failed!
		 FlowCapableNodeConnectorQueueStatistics gs = stData.getFlowCapableNodeConnectorQueueStatistics();
        if(gs==null){
            //no data yet, ignore
            return;
        }
        System.out.println("gs.getTransmittedBytes() is");
        System.out.println(gs.getTransmittedBytes());
        */
//Try two
        /*QueueKey qKey = new QueueKey(queueStat.getQueueId());

        InstanceIdentifier<Queue> queueIdent = nodeIId 
                            .child(NodeConnector.class, new NodeConnectorKey(ncId)) 
                            .augmentation(FlowCapableNodeConnector.class) 
                            .child(Queue.class, qKey);
        InstanceIdentifier<FlowCapableNodeConnectorQueueStatisticsData> queueStatIdent = queueIdent
        					.augmentation(FlowCapableNodeConnectorQueueStatisticsData.class);
		*/
//This is fcnc try       
		/*InstanceIdentifier<FlowCapableNodeConnector> fcncIId = constructFlowCapableNodeConnectorIId(nodeConnectorIId);
		FlowCapableNodeConnector fcnc = GenericTransactionUtils.readData(this.dataBroker, LogicalDatastoreType.OPERATIONAL, fcncIId);

		if (fcnc != null) {
			System.out.println("NodeId and Port Monitor1Impl fcnc info.: " + fcnc);
			LOG.info("NodeId and Port Monitor1Impl NodeID {} fcnc SUCCESS!!!", nodeId);
			
		} //End of if (table != null) statement
		else{
			LOG.info("NodeId and Port Monitor1Impl NodeID {} fcnc is null!!!", nodeId);
		}*/
	}
	public InstanceIdentifier<FlowCapableNodeConnector> constructFlowCapableNodeConnectorIId (InstanceIdentifier<NodeConnector> ncIId) {
		InstanceIdentifier<FlowCapableNodeConnector> fcncIId = ncIId.augmentation(FlowCapableNodeConnector.class);
		return fcncIId;
	}
	public InstanceIdentifier<Node> createNodeIId(NodeId nodeId) {
		return InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(new NodeId(nodeId))).build();
	}

	/*private void parsePortInfo(String portInfo){
		String s_tx="getTransmitted=", s_rx=", getReceived=", s_nanoSec="getNanosecond=Counter32 [_value=", s_sec="getSecond=Counter32 [_value=";
		int byteIndex = portInfo.indexOf(s_tx)+s_tx.length();
		String sub1 = portInfo.substring(byteIndex);
		System.out.println("sub1 getTransmitted:");f
		System.out.println(sub1);
	}*/

	private void getNodeConnectorfromSwPort(String switchName, long portNumber){ //

		InstanceIdentifier<NodeConnector> nodeConnectorIId = InstanceIdentifier.builder( Nodes.class )
			.child( Node.class, new NodeKey( new NodeId( switchName ) ))
				.child( NodeConnector.class, new NodeConnectorKey( new NodeConnectorId( switchName+":"+portNumber ) ))
					.build();
		System.out.println("nodeConnectorIId");
		System.out.println(nodeConnectorIId);
		/*InstanceIdentifier<NodeConnector> nodeConnectorIId = InstanceIdentifier.builder( Nodes.class )
			.child( Node.class, new NodeKey( new NodeId( “openflow:1” ) )
				.child( NodeConnector.class, new NodeConnectorKey( new NodeConnectorId( “openflow:1:3” ) ).
					build();*/
	
		long startNodeConnectorTime = System.nanoTime();
		NodeConnector nodeConnector = GenericTransactionUtils.readData(this.dataBroker, LogicalDatastoreType.CONFIGURATION, nodeConnectorIId);
		long endNodeConnectorTime = System.nanoTime();
		// LOG.info("Monitor1Impl NodeConnector readData Elapsed Time(nano secs): {}", (endNodeConnectorTime - startNodeConnectorTime));
		
		if (nodeConnector != null) {
			this.avgNodeConnectorElapsedTime += (endNodeConnectorTime - startNodeConnectorTime);
			this.nodeConnectorCount++;
			System.out.println("Monitor1Impl nodeConnector info.: ");
			System.out.println(nodeConnector);
			LOG.info("Monitor1Impl nodeConnectorIID {} SUCCESS!!!", nodeConnectorIId);
			
		} //End of if (nodeConnector != null) statement
		else{
			LOG.info("Monitor1Impl nodeConnectorIID {} is null!!!", nodeConnectorIId);
		}
	} // End of getTrafficInfo function

	private void readTablefromSwitch(String switchName){

		NodeId nodeId = new NodeId(switchName);// + input.getSwitchId().toString());
		TableKey tableKey = new TableKey((short)0); // SDN SW using table[0] by default
		InstanceIdentifier<Table> tableIID = InstanceIdentifier.builder(Nodes.class)
				.child(Node.class, new NodeKey(nodeId))
				.augmentation(FlowCapableNode.class)
				.child(Table.class, tableKey).build();
		
		long startTableTime = System.nanoTime();
		Table table = GenericTransactionUtils.readData(this.dataBroker, LogicalDatastoreType.OPERATIONAL, tableIID);
		//table = GenericTransactionUtils.readData(this.dataBroker, LogicalDatastoreType.CONFIGURATION, tableIID);
		long endTableTime = System.nanoTime();
		// LOG.info("Monitor1Impl readData Elapsed Time(nano secs): {}", (endTime - startTime));

		if (table != null) {
			this.avgTableElapsedTime += (endTableTime - startTableTime);
			this.tableCount++;
			System.out.println("Monitor1Impl Table OPERATIONAL SUCCESS!!!");
			System.out.println("Monitor1Impl table info.: " + table);
			LOG.info("Monitor1Impl NodeID {} Table SUCCESS!!!", nodeId);
			//readFlowfromTable(table, tableKey, nodeId);
		} //End of if (table != null) statement
		else{
			System.out.println("Monitor1Impl Table OPERATIONAL is NULL!!!");
			//LOG.info("Monitor1Impl NodeID {} Table is null!!!", nodeId);
		}
	}

	private void readFlowfromTable(Table table, TableKey tableKey, NodeId nodeId){
		if (table.getFlow() != null) {
			int flowCount = 0;
			List<Flow> flows = table.getFlow();
			//GetAllFlowRulesFromASwitchOutputBuilder flowRulesOutputBuilder = new GetAllFlowRulesFromASwitchOutputBuilder();
			//List<FlowRules> flowRuleList = Lists.newArrayList();
			
			for (Iterator<Flow> iterator = flows.iterator(); iterator.hasNext();) {
				flowCount++;
				FlowKey flowKey = iterator.next().getKey();
		        InstanceIdentifier<Flow> flowIID = InstanceIdentifier.create(Nodes.class)
		        		.child(Node.class, new NodeKey(nodeId))
		        		.augmentation(FlowCapableNode.class)
		        		.child(Table.class, tableKey)
		        		.child(Flow.class, flowKey);
		        Flow flow = GenericTransactionUtils.readData(dataBroker, LogicalDatastoreType.OPERATIONAL, flowIID);
		        
		        if (flow==null){
		        	System.out.println("Monitor1Impl Flow OPERATIONAL is NULL!!!");
					flow = GenericTransactionUtils.readData(dataBroker, LogicalDatastoreType.CONFIGURATION, flowIID);
				}
				else{
					System.out.println("Monitor1Impl Table OPERATIONAL Not null!!!");
				}


		        if (flow != null){
		        	//FlowRulesBuilder flowBuilder = new FlowRulesBuilder();
		        	////---------Extract Flow properties information
		        	if (flow.getId() != null){
		        		//LOG.info("Monitor1Impl flowID is {}!!!", flow.getId().getValue());
		        	}
		        	else {
		        		return;
		        	}
		        	//flow.getPriority();
		        	//flow.getIdleTimeout();
		        	//flow.getHardTimeout();
		        	if (flow.getMatch().getInPort() != null){
		        		//flowBuilder.setInPortId((long)Integer.parseInt(flow.getMatch().getInPort().getValue().split(":")[2]));
		        		LOG.info("Monitor1Impl Inport is {}!!!", flow.getMatch().getInPort().getValue());
		        	}
		        	///---------------Extract IP and MAC address information
		        	if (flow.getMatch().getEthernetMatch().getEthernetSource() != null){
		        		flow.getMatch().getEthernetMatch().getEthernetSource().getAddress().getValue();
		        	}
		        	if (flow.getMatch().getEthernetMatch().getEthernetDestination() != null){
		        		flow.getMatch().getEthernetMatch().getEthernetDestination().getAddress().getValue();
		        	}
		        	if (flow.getMatch().getLayer3Match() != null){
		        		Ipv4Match ipv4Match = (Ipv4Match) flow.getMatch().getLayer3Match();
		        		if (ipv4Match.getIpv4Source() != null) {
		        			ipv4Match.getIpv4Source().getValue();
		        		}
		        		if (ipv4Match.getIpv4Destination() != null) {
		        			ipv4Match.getIpv4Destination().getValue();
		        		}
		        	}
		        	//----------Extract Port Information
		        	if (flow.getMatch().getLayer4Match() != null) {
		        		TcpMatch tcpMatch = (TcpMatch) flow.getMatch().getLayer4Match();
		        		if (tcpMatch.getTcpDestinationPort() != null) {
		        			tcpMatch.getTcpDestinationPort().getValue();
		        		}
		        		if (tcpMatch.getTcpSourcePort() != null) {
		        			tcpMatch.getTcpSourcePort().getValue();
		        		}
		        	}
		        	///---------------Extract Network Protocol information
		        	if (flow.getMatch().getIpMatch() != null && flow.getMatch().getIpMatch().getIpProto() != null){
		        		if (flow.getMatch().getIpMatch().getIpProto().getIntValue() == 1){
		        			LOG.info("Monitor1Impl TrafficType is ICMP!!!");
		        		}
		        		else if (flow.getMatch().getIpMatch().getIpProto().getIntValue() == 6){
		        			LOG.info("Monitor1Impl TrafficType is TCP!!!");
		        			if (flow.getMatch().getEthernetMatch().getEthernetType() != null) {
				        		if (flow.getMatch().getEthernetMatch().getEthernetType().getType().getValue() == 0x0800){
				        			if (flow.getMatch().getLayer4Match() != null) {
				        				TcpMatch tcpMatch = (TcpMatch) flow.getMatch().getLayer4Match();
				        				if (tcpMatch.getTcpDestinationPort() != null) {
				        					if (tcpMatch.getTcpDestinationPort().getValue() == 80) {
				        						LOG.info("Monitor1Impl TrafficType is HTTP!!!");
				        					}
				        					else if (tcpMatch.getTcpDestinationPort().getValue() == 443) {
				        						LOG.info("Monitor1Impl TrafficType is HTTPS!!!");
				        					}
				        				}
				        			}
				        		}
				        	}
		        		}
		        		else if (flow.getMatch().getIpMatch().getIpProto().getIntValue() == 17){
		        			LOG.info("Monitor1Impl TrafficType is UDP!!!");
		        			if (flow.getMatch().getEthernetMatch().getEthernetType() != null) {
				        		if (flow.getMatch().getEthernetMatch().getEthernetType().getType().getValue() == 0x0800){
				        			if (flow.getMatch().getLayer4Match() != null) {
				        				TcpMatch tcpMatch = (TcpMatch) flow.getMatch().getLayer4Match();
				        				if (tcpMatch.getTcpDestinationPort() != null) {
				        					if (tcpMatch.getTcpDestinationPort().getValue() == 53) {
				        						LOG.info("Monitor1Impl TrafficType is DNS!!!");
				        					}
				        					else if (tcpMatch.getTcpDestinationPort().getValue() == 67) {
				        						LOG.info("Monitor1Impl TrafficType is DHCP!!!");
				        					}
				        				}
				        			}
				        		}
				        	}
		        		}
		        		
		        	} //End of statement if (flow.getMatch().getIpMatch() != null && flow.getMatch().getIpMatch().getIpProto() != null)
		        	else{
						//LOG.info("Monitor1Impl No Network Protocol Info.!!!");
					}
		        	//---------------Extract ARP Traffic Info ---------------
		        	if (flow.getMatch().getEthernetMatch() != null && flow.getMatch().getEthernetMatch().getEthernetType() != null) {
		        		if (flow.getMatch().getEthernetMatch().getEthernetType().getType().getValue() == 0x0806){
		        			LOG.info("Monitor1Impl TrafficType is ARP!!!");
		        		}
		        	}
		        	else{
		        		//LOG.info("Monitor1Impl No ARP Traffic Info.!!!");
		        	}
		        	//----------------------------Extract Action Information ---------------------
		        	Instructions instructions = flow.getInstructions();
		        	for (Instruction instruction : instructions.getInstruction()){

		        		//System.out.println("Monitor1Impl getInstruction: " + instruction);

		        		ApplyActionsCase applyActionCase = (ApplyActionsCase) instruction.getInstruction();
		        		ApplyActions applyAction = applyActionCase.getApplyActions();
		        		for (Action action : applyAction.getAction()) {

		        			//System.out.println("Monitor1Impl getAction: " + action);

		        			if (action.getAction() instanceof OutputActionCase) {
		        				OutputActionCase outputCase = (OutputActionCase) action.getAction();
		        				String outputPort = outputCase.getOutputAction().getOutputNodeConnector().getValue().split(":")[2];
		        				LOG.info("Monitor1Impl Outport is {}!!!", outputPort);	
		        				//flowBuilder.setActionOutputPort(outputPort);
		        			
		        			} else if (action.getAction() instanceof DropActionCase){
		        				//flowBuilder.setActionOutputPort("0");
		        			
		        			} else if (action.getAction() instanceof SetNwSrcActionCase) {
		        				SetNwSrcActionCase setNwSrcCase = (SetNwSrcActionCase) action.getAction();
		        				Ipv4 ipv4Address = (Ipv4) setNwSrcCase.getSetNwSrcAction().getAddress();
		        				LOG.info("Monitor1Impl IPv4 Src address is {}", ipv4Address.getIpv4Address().getValue());
		        				//flowBuilder.setActionSetSourceIpv4Address(ipv4Address.getIpv4Address().getValue());
		        				
		        			} else if (action.getAction() instanceof SetNwDstActionCase) {
		        				SetNwDstActionCase setNwDstCase = (SetNwDstActionCase) action.getAction();
		        				Ipv4 ipv4Address = (Ipv4) setNwDstCase.getSetNwDstAction().getAddress();
		        				LOG.info("Monitor1Impl IPv4 Dst address is {}", ipv4Address.getIpv4Address().getValue());
		        				//flowBuilder.setActionSetDstIpv4Address(ipv4Address.getIpv4Address().getValue());
		        				
		        			} else if (action.getAction() instanceof SetTpSrcActionCase) {
		        				SetTpSrcActionCase setTpCase = (SetTpSrcActionCase) action.getAction();
		        				//flowBuilder.setActionSetTcpSrcPort(setTpCase.getSetTpSrcAction().getPort().getValue());
		        				
		        			} else if (action.getAction() instanceof SetTpDstActionCase) {
		        				SetTpDstActionCase setTpCase = (SetTpDstActionCase) action.getAction();
		        				//flowBuilder.setActionSetTcpDstPort(setTpCase.getSetTpDstAction().getPort().getValue());
		        				
		        			} else if (action.getAction() instanceof SetNwTosActionCase) {
		        				SetNwTosActionCase setNwTosCase = (SetNwTosActionCase) action.getAction();
		        				//flowBuilder.setActionSetIpv4Tos(setNwTosCase.getSetNwTosAction().getTos());
		        				
		        			} else if (action.getAction() instanceof SetNwTtlActionCase) {
		        				SetNwTtlActionCase setNwTtlCase = (SetNwTtlActionCase) action.getAction();
		        				//flowBuilder.setActionSetIpv4Ttl(setNwTtlCase.getSetNwTtlAction().getNwTtl());
		        			}
		        		}
		        	}
		        	//flowRuleList.add(flowBuilder.build());
		        }/////////////////////////////////////////////////////--------------------End of IF Flow != NULL
			} //...................End of For loop Iterating through Flows
			// ------------ Create output object of the RPC
			//flowRulesOutputBuilder.setFlowRules(flowRuleList);
			//return RpcResultBuilder.success(flowRulesOutputBuilder.build()).buildFuture();
		} // End of IF No Flows IN Table 0
		else{
			LOG.info("Monitor1Impl NodeID {} Table[0] has NO FLOW!!!", nodeId);
		} // End of the if (table.getFlow() != null) statement
	} // End of private void getFlowfromTable(Table table)

}