/*
 * Copyright © 2016 opmdc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.opmdc.impl;

import java.util.concurrent.Future;
import java.util.Arrays;
import com.google.common.collect.Lists;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;

//import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.opmdc.rev150105.OpmdcService;
//import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.opmdc.rev150105.SendPacketInput;
//import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.opmdc.rev150105.SendPacketOutput;
//import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.opmdc.rev150105.SendPacketOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketHandler implements PacketProcessingListener  {
//public class OpmdcImpl implements OpmdcService, PacketProcessingListener  {
//public class OpmdcImpl implements OpmdcService{
    private PacketProcessingService packetProcessingService;
    private static final Logger LOG = LoggerFactory.getLogger(OpmdcProvider.class);
    private DataBroker dataBroker;
    private final static long FLOOD_PORT_NUMBER = 0xfffffffbL;

    public PacketHandler(DataBroker dataBroker, PacketProcessingService packetProcessingService) {
        LOG.info("PacketHandler Session Initiated");
        this.dataBroker = dataBroker;
        this.packetProcessingService = packetProcessingService;
    }

    //for RPC handle
    //public PacketHandler(PacketProcessingService packetProcessingService) {
    //    this.packetProcessingService = packetProcessingService;
    //}
    
    /**
     * size of MAC address in octets (6*8 = 48 bits)
     */
    private static final int MAC_ADDRESS_SIZE = 6;

   /**
    * length of IP address in array
    */
    private static final int IP_LENGTH = 4;

    /**
     * length of source TCP/UDP Port in array
     */
    private static final int PORT_LENGTH = 2;

    /**
     * start position of destination MAC address in array
     */
    private static final int DST_MAC_START_POSITION = 0;

    /**
     * end position of destination MAC address in array
     */
    private static final int DST_MAC_END_POSITION = 6;

    /**
     * start position of source MAC address in array
     */
    private static final int SRC_MAC_START_POSITION = 6;

    /**
     * end position of source MAC address in array
     */
    private static final int SRC_MAC_END_POSITION = 12;


    /**
     * start position of ethernet type in array
     */
    private static final int ETHER_TYPE_START_POSITION = 12;

    /**
     * end position of ethernet type in array
     */
    private static final int ETHER_TYPE_END_POSITION = 14;

    private static final int IP_PROTOCOL_START_POSITION = 23;

    /**
     * start position of source IP address in array
     */
    private static final int SRC_IP_START_POSITION = 26;

    /**
     * end position of source IP address in array
     */
    private static final int SRC_IP_END_POSITION = 30;

    /**
     * start position of Destination IP address in array
     */
    private static final int DST_IP_START_POSITION = 30;

    /**
     * end position of Destination IP address in array
     */
    private static final int DST_IP_END_POSITION = 34;

    /**
     * start position of source TCP/UDP Port in array
     */
    private static final int SRC_PORT_START_POSITION = 34;

    /**
     * end position of source TCP/UDP Port  in array
     */
    private static final int SRC_PORT_END_POSITION = 36;

    /**
     * start position of Destination TCP/UDP Port  in array
     */
    private static final int DST_PORT_START_POSITION = 36;

    /**
     * end position of DestinationTCP/UDP Port in array
     */
    private static final int DST_PORT_END_POSITION = 38;

    /**
     * start position of multicast record
     */
    private static final int MUL_RECORD_START_POSITION = 46;

    /**
     * end position of multicast record
     */
    private static final int MUL_RECORD_END_POSITION = 47;

    /**
     * start position of multicast source numbers
     */
    private static final int MUL_SRCNUM_START_POSITION = 48;

    /**
     * end position of multicast source numbers
     */
    private static final int MUL_SRCNUM_END_POSITION = 50;

    /**
     * start position of multicast group address
     */
    private static final int MUL_IP_START_POSITION = 50;

    /**
     * end position of multicast group address
     */
    private static final int MUL_IP_END_POSITION = 54;
   
    private static final int POWER = 256;

    /*
     * @param payload
     * @return destination IP address
     */
    public static byte[] extractDstIP(final byte[] payload) {
        return Arrays.copyOfRange(payload, DST_IP_START_POSITION, DST_IP_END_POSITION);
    }

    /*
     * @param payload
     * @return source IP address
     */
    public static byte[] extractSrcIP(final byte[] payload) {
        return Arrays.copyOfRange(payload, SRC_IP_START_POSITION, SRC_IP_END_POSITION);
    }

    /*
     * @param payload
     * @return multicast group IP address
     */
    public static byte[] extractMulIP(final byte[] payload) {
        return Arrays.copyOfRange(payload, MUL_IP_START_POSITION, MUL_IP_END_POSITION);
    }

    /*
     * @param payload
     * @return multicast group record type
     */
    public static byte[] extractMulrecord(final byte[] payload) {
        return Arrays.copyOfRange(payload, MUL_RECORD_START_POSITION, MUL_RECORD_END_POSITION);
    }

    /*
     * @param payload
     * @return multicast group source numbers
     */
    public static byte[] extractMulsrcnum(final byte[] payload) {
        return Arrays.copyOfRange(payload, MUL_SRCNUM_START_POSITION, MUL_SRCNUM_END_POSITION);
    }

    /*
     * @param payload
     * @return Source TCP Port
     */
    public static byte[] extractSrcPort(final byte[] payload) {
        return Arrays.copyOfRange(payload, SRC_PORT_START_POSITION, SRC_PORT_END_POSITION);
    }

    /*
     * @param payload
     * @return Destination TCP Port
     */
    public static byte[] extractDstPort(final byte[] payload) {
        return Arrays.copyOfRange(payload, DST_PORT_START_POSITION, DST_PORT_END_POSITION);
    }

    /**
     */
    public static byte[] extractDstMac(final byte[] payload) {
        return Arrays.copyOfRange(payload, DST_MAC_START_POSITION, DST_MAC_END_POSITION);
    }

    /**
     */
    public static byte[] extractSrcMac(final byte[] payload) {
        return Arrays.copyOfRange(payload, SRC_MAC_START_POSITION, SRC_MAC_END_POSITION);
    }

    /**
     */
    public static byte[] extractEtherType(final byte[] payload) {
        return Arrays.copyOfRange(payload, ETHER_TYPE_START_POSITION, ETHER_TYPE_END_POSITION);
    }

    public static byte extractIPprotocol(final byte[] payload) {
        return payload[IP_PROTOCOL_START_POSITION];
    }

    /*
     * @param rawdata
     * @return String number
     */
    public static String rawdataToString(byte[] rawdata) {
        if (rawdata != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0 ; i<rawdata.length; i++){
                    sb.append(String.format("%d",rawdata[i] & 0xff));

            }
            return sb.substring(0,sb.length());
        }
        return null;
    }

    /*
     * @param rawIP
     * @return String IPAddress
     */
    public static String rawIPToString(byte[] rawIP) {
        if (rawIP != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0 ; i<rawIP.length; i++){
                sb.append(String.format("%d",rawIP[i] & 0xff));
                sb.append(".");
            }
            return sb.substring(0,sb.length()-1);
        }
        return null;
    }

    /*
     * @param rawPort
     * @return int TCPPort
     */
    public static int rawPortToInteger(byte[] rawPort) {
        int intOctet =0;
        int intOctetSum = 0;
        int iter = 1;
        if (rawPort != null && rawPort.length == PORT_LENGTH) {
            for (byte octet : rawPort) {
                intOctet = octet & 0xff;
                intOctetSum = (int) (intOctetSum + intOctet *  Math.pow(POWER,iter));
                iter--;
            }
            return intOctetSum;
        }
        return 0;
    }

    /**
     */
    public static String byteToHexStr(final byte[] bts, String delimit) {
        StringBuffer macStr = new StringBuffer();

        for (int i = 0; i < bts.length; i++) {
            String str = Integer.toHexString(bts[i] & 0xFF);
            if( str.length()<=1 ){
                macStr.append("0");
            }
            macStr.append(str);

            if( i < bts.length - 1 ) { //not last delimit string
                macStr.append(delimit);
            }
        } // end of for !!
        return macStr.toString();
    }

    @Override
    public void onPacketReceived(PacketReceived notification) {
        // TODO Auto-generated method stub
        LOG.info("PacketHandler Received packet {}", InventoryUtils.getNodeConnectorId(notification.getIngress()));
        

        NodeConnectorRef ingressNodeConnectorRef = notification.getIngress();
        NodeConnectorId ingressNodeConnectorId = InventoryUtils.getNodeConnectorId(ingressNodeConnectorRef);
        NodeId ingressNodeId = InventoryUtils.getNodeId(ingressNodeConnectorRef);
        NodeRef ingressNodeRef = InventoryUtils.getNodeRef(ingressNodeConnectorRef);
        
        NodeConnectorId floodNodeConnectorId = InventoryUtils.getNodeConnectorId(ingressNodeId, FLOOD_PORT_NUMBER);
        NodeConnectorRef floodNodeConnectorRef = InventoryUtils.getNodeConnectorRef(floodNodeConnectorId);

        byte[] payload = notification.getPayload();
        byte[] dstMacRaw = extractDstMac(payload);
        byte[] srcMacRaw = extractSrcMac(payload);
        byte[] ethType   = extractEtherType(payload);
        byte[] srcIPRaw  = extractSrcIP(payload);
        byte[] dstIPRaw  = extractDstIP(payload);
        byte[] mulIPRaw  = extractMulIP(payload);
        byte[] mulrecordRaw = extractMulrecord(payload);
        byte[] mulsrcnumRaw = extractMulsrcnum(payload);
        

        String dstMac = byteToHexStr(dstMacRaw, ":");
        String srcMac = byteToHexStr(srcMacRaw, ":");
        String ethStr = byteToHexStr(ethType, "");
        String srcIP  = rawIPToString(srcIPRaw);
        String dstIP  = rawIPToString(dstIPRaw);
        String mulIP  = rawIPToString(mulIPRaw);
        String mulrecord = rawIPToString(mulrecordRaw);
        String mulsrcnum = rawdataToString(mulsrcnumRaw);

        if (dstIP.equals("255.255.255.255") || dstMac.equals("ff:ff:ff:ff:ff:ff") ){
            LOG.info("Ignore broadcast");
        }
        else{
            // multicast join or leave msg
            if (dstIP.equals("224.0.0.22")){
                if ( mulrecord.equals("4") && mulsrcnum.equals("0")){ // join group
                    LOG.info("Join {} group",mulIP);
                }
                else if ( mulrecord.equals("3") && mulsrcnum.equals("0")){ // leave group
                    LOG.info("Leave {} group",mulIP);
                }
            }
            else {
                LOG.info("Unitcast DstIP = {}",dstIP);
            }
            
            // rwa algorithm
            NodeConnectorId egressNodeConnectorId;
            if (ingressNodeConnectorId.equals(InventoryUtils.getNodeConnectorId(ingressNodeId,14)))
                egressNodeConnectorId = InventoryUtils.getNodeConnectorId(ingressNodeId,16);
            else
                egressNodeConnectorId = InventoryUtils.getNodeConnectorId(ingressNodeId,14);
        
            dstIP += "/32";
            // flow modify
            programFlow(ingressNodeId, dstIP, ingressNodeConnectorId, egressNodeConnectorId);
    
            NodeConnectorRef egressNodeConnectorRef = InventoryUtils.getNodeConnectorRef(egressNodeConnectorId);
            packetOut(ingressNodeRef ,egressNodeConnectorRef, notification.getPayload());  //send packet!!
            LOG.info("PacketHandler Packet out");
        }

    }

    private void packetOut(NodeRef egressNodeRef, NodeConnectorRef egressNodeConnectorRef, byte[] payload) {
            // Construct input for RPC call to packet processing service
            TransmitPacketInput input = new TransmitPacketInputBuilder().setPayload(payload).setNode(egressNodeRef)
                            .setEgress(egressNodeConnectorRef).build();

            packetProcessingService.transmitPacket(input);

    }
    
    private void programFlow(NodeId nodeId, String dstIP, NodeConnectorId ingressNodeConnectorId, NodeConnectorId egressNodeConnectorId) {

        /* Programming a flow involves:
         * 1. Creating a Flow object that has a match and a list of instructions,
         * 2. Adding Flow object as an augmentation to the Node object in the inventory. 
         * 3. FlowProgrammer module of OpenFlowPlugin will pick up this data change and eventually program the switch.
        */

        //Creating match object
        MatchBuilder matchBuilder = new MatchBuilder();
        MatchUtils.createDstL3IPv4Match(matchBuilder, new Ipv4Prefix(dstIP));
        //MatchUtils.createEthDstMatch(matchBuilder, new MacAddress(dstMac), null);
        MatchUtils.createInPortMatch(matchBuilder, ingressNodeConnectorId);

        // Instructions List Stores Individual Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = Lists.newArrayList();
        InstructionBuilder ib = new InstructionBuilder();
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        ActionBuilder ab = new ActionBuilder();
        List<Action> actionList = Lists.newArrayList();

        // Set output action
        OutputActionBuilder output = new OutputActionBuilder();
        output.setOutputNodeConnector(egressNodeConnectorId);
        output.setMaxLength(65535); //Send full packet and No buffer
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        // Create Apply Actions Instruction
        aab.setAction(actionList);
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());

        // Create Flow
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.setMatch(matchBuilder.build());

        //String flowId = "L2_Rule_" + dstMac;
        String flowId = "L3_Rule_" + dstIP;
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId((short)0);
        flowBuilder.setKey(key);
        flowBuilder.setPriority(32768);
        flowBuilder.setFlowName(flowId);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);
        flowBuilder.setInstructions(isb.setInstruction(instructions).build());

        InstanceIdentifier<Flow> flowIID = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(nodeId))
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flowBuilder.getTableId()))
                .child(Flow.class, flowBuilder.getKey())
                .build();
        GenericTransactionUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION, flowIID, flowBuilder.build(), true);
    }
    
    //impl packet out RPC (no impl)
    /*
    @Override
    public Future<RpcResult<SendPacketOutput>> sendPacket(SendPacketInput input) {
        SendPacketOutputBuilder packetBuilder = new SendPacketOutputBuilder();

        //LOG.info("Packetout Initiated");
        NodeConnectorRef egress = input.getEgress();
        NodeRef node = input.getNode();
        byte[] payload = input.getRawpacket();
        packetOut(node,egress, payload);  //send packet!!

        packetBuilder.setResult("OK");
        return RpcResultBuilder.success(packetBuilder.build()).buildFuture();
    }    
    */
}
