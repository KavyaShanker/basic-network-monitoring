package com.example;

import com.mxgraph.layout.mxIGraphLayout;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.Serializable;
import java.util.*;

import javax.swing.JFrame;

import org.jgrapht.graph.DefaultDirectedGraph;



class NetworkDevice implements Serializable{
    String name;
    String ip;
    String mac;
    String status;

    NetworkDevice(String name, String ip, String status) {
        this.name = name;
        this.ip = ip;
        this.status = status;
    }
}

    class SecurityEvent {
        String eventType;
        String sourceIP;
        String affectedDevice;
        String description;
    
        SecurityEvent(String eventType, String sourceIP, String affectedDevice, String description) {
            this.eventType = eventType;
            this.sourceIP = sourceIP;
            this.affectedDevice = affectedDevice;
            this.description = description;
        }
    }
    

public class NetworkMonitoring {

    ArrayList<String> devices = new ArrayList<>();
    Graph<NetworkDevice, DefaultEdge> networkGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
    int deviceCount = 0;

    Map<NetworkDevice, List<NetworkDevice>> connections = new HashMap<>();
    
    // CREATING CONNECTIONS
    public void connectDevices(String csvFilePath) {
        Map<String, NetworkDevice> deviceMap = new HashMap<>();
        List<String[]> connections = new ArrayList<>();

        try {
            // Read the CSV file and store device information and connections
            BufferedReader reader = new BufferedReader(new FileReader(csvFilePath));
            String line; // to store each line of the CSV
            reader.readLine(); // skip the header line

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                
                String snmpDeviceName = data[10].trim(); // Column 10: SNMPDevice
                String connectedDeviceName = data[16].trim(); // Column 16: ConnectedDevice

                String ip = data[14];
                String status = data[8];

                if (!deviceMap.containsKey(snmpDeviceName)) {
                    NetworkDevice device = new NetworkDevice(snmpDeviceName, ip, status);
                    deviceMap.put(snmpDeviceName, device);
                }

                if (!deviceMap.containsKey(connectedDeviceName)) {
                    NetworkDevice device = new NetworkDevice(connectedDeviceName, ip, "");
                    deviceMap.put(connectedDeviceName, device);
                    deviceCount++;
                }

                connections.add(new String[]{snmpDeviceName, connectedDeviceName});
            }

            reader.close();
        } 
        
        catch (IOException e) {
            e.printStackTrace();
        }

        for (String[] connection : connections) {
            String sourceName = connection[0];
            String destinationName = connection[1];
            
            NetworkDevice source = deviceMap.get(sourceName);
            NetworkDevice destination = deviceMap.get(destinationName);

            networkGraph.addVertex(source);
            networkGraph.addVertex(destination);
            networkGraph.addEdge(source, destination);
        }
    }
    
    //DETECTING ANOMALIES
    public String anomalyDetection() { //can be done in several ways, using statistics based thresholds here 
        String retval = "";

        double alpha = 0.05; // level of significane
        boolean anomaly_flag = false;
    
        ArrayList<Double> latencies = new ArrayList<>();
        ArrayList<Double> packetlosses = new ArrayList<>();
    
        for (NetworkDevice device : networkGraph.vertexSet()) {
            double latency = getLatency(device); 
            double packetLoss = getPacketLoss(device); 
            latencies.add(latency);
            packetlosses.add(packetLoss);
        }
    
        //calcluate statisctics for latency and packet loss data, then generate zscore for 
        DescriptiveStatistics latency_stats = new DescriptiveStatistics();
        latencies.forEach(latency_stats::addValue);

        DescriptiveStatistics pktloss_stats = new DescriptiveStatistics();
        packetlosses.forEach(pktloss_stats::addValue);

        double meanLatency = latency_stats.getMean();
        double meanPacketLoss = pktloss_stats.getMean();
        double stdDevLatency = latency_stats.getStandardDeviation();

        double stdDevPacketLoss = pktloss_stats.getStandardDeviation();
    
        // Initialize a normal distribution for Z-score calculations
        NormalDistribution normalDistribution = new NormalDistribution();
    
        // Check for anomalies using Z-scores
        for (int i = 0; i < latencies.size(); i++) {
            double latencyZScore = retZScore(latencies.get(i), meanLatency, stdDevLatency);
            //System.out.println(latencyZScore);
            double packetLossZScore = retZScore(packetlosses.get(i), meanPacketLoss, stdDevPacketLoss);
            
            NetworkDevice current_device = (NetworkDevice) networkGraph.vertexSet().toArray()[i];
            //System.out.println(normalDistribution.inverseCumulativeProbability(1 - alpha / 2));                
            // Check if the Z-scores exceed the critical value for the significance level
            if (Math.abs(latencyZScore) > normalDistribution.inverseCumulativeProbability(1 - alpha / 2)
                    || Math.abs(packetLossZScore) > normalDistribution.inverseCumulativeProbability(1 - alpha / 2)) {
                
                retval = "Anomaly detected for " + current_device.name + ": Latency Z-score = " + latencyZScore + ", Packet Loss Z-score = " + packetLossZScore;
            
            }
            else{
                anomaly_flag = true;
                //System.out.println("No anomaly detected for "+ current_device.name + ": Latency Z-score = " + latencyZScore + ", Packet Loss Z-score = " + packetLossZScore);
            }
        }

        if(anomaly_flag)
            retval = "No anomalies found.";

        return retval;
    }
    
    public double retZScore(double value, double mean, double stdDev) {
        return (value - mean) / stdDev;
    }
    
    public double getLatency(NetworkDevice device) {
        try{
            BufferedReader reader = new BufferedReader(new FileReader("C:/Users/Kavya/kavVSCode/ADSLabs/kavADSProject/ADS dataset.csv"));
            String line; 
            reader.readLine(); 

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");

                String name = data[10].trim(); // Column 10: SNMPDevice   

                if(name.equals(device.name)){
                    return Double.parseDouble(data[2]);
                }
            }
                reader.close();    
            }
            catch (IOException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    
    public double getPacketLoss(NetworkDevice device) {
        try{
            BufferedReader reader = new BufferedReader(new FileReader("C:/Users/Kavya/kavVSCode/ADSLabs/kavADSProject/ADS dataset.csv"));
            String line; 
            reader.readLine(); 

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");

                String name = data[10].trim(); // Column 10: SNMPDevice   

                if(name.equals(device.name)){
                    return Double.parseDouble(data[3]);
                }
            }
                    reader.close();
            }
            catch (IOException e) {
            e.printStackTrace();
        }
        return 0.0;  
    }

    //MONITOR BANDWIDTH UTILISATION
    public String bandwidthUtilization() {
        String retval = "";

        double totalBandwidth = 0.0;
        double maxBW = 0.0;
        NetworkDevice maxBWDevice = null;

        for (NetworkDevice device : networkGraph.vertexSet()) {
            totalBandwidth += getBandwidthUsage(device);

            if (getBandwidthUsage(device) > maxBW) {
                maxBW = getBandwidthUsage(device);
                maxBWDevice = device;
            }
        }

        // System.out.println("Device Count: "+deviceCount);
    
        if (deviceCount > 0) {
            double averageBandwidthUtilization = totalBandwidth / deviceCount;
            retval = ("Device utilizing maximum bandwidth: "+maxBWDevice.name+ "\t((Bandwidth: " + maxBW+ " Mbps))") + "\n" +
            ("Average Bandwidth Utilization: " + averageBandwidthUtilization + " Mbps");
        } 
        else {
            retval = "No devices found.";
        }

        return retval;
    }
    
    public double getBandwidthUsage(NetworkDevice device){
        try{
            BufferedReader reader = new BufferedReader(new FileReader("C:/Users/Kavya/kavVSCode/ADSLabs/kavADSProject/ADS dataset.csv"));
            String line; 
            reader.readLine(); 

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");

                String name = data[10].trim(); // Column 10: SNMPDevice   

                if(name.equals(device.name)){
                    return Double.parseDouble(data[4]);
                }
            }
            
            reader.close();
            
            }
            catch (IOException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    //MONITOR SECURITY EVENTS
    public String securityEventMonitoring() {
        String retval = "";

        List<SecurityEvent> securityEvents = getSecurityEvent();
    
        for (SecurityEvent event : securityEvents) {
            
            if (!(event.eventType).trim().contains("Success")) {
                retval += logSecurityEvent(event)+ "\n";
                
            }
        }

        return retval;
    }
    
    public List<SecurityEvent> getSecurityEvent() {
        List<SecurityEvent> securityEvents = new ArrayList<>();
    
        try{
            BufferedReader reader = new BufferedReader(new FileReader("C:/Users/Kavya/kavVSCode/ADSLabs/kavADSProject/ADS dataset.csv"));
            String line; 
            reader.readLine(); 

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");

                String security_event_type = data[12].trim(); // Column 10: SNMPDevice   
                String sourceIP = data[6];
                String affectedDevice = data[16];
                String description = data[15];
                securityEvents.add(new SecurityEvent(security_event_type, sourceIP, affectedDevice, description));
            }
                reader.close();    
            }
            catch (IOException e) {
            e.printStackTrace();
        }
        return securityEvents;
    }
    
    private String logSecurityEvent(SecurityEvent event) {
        String securityeventlog = "";

        securityeventlog = ("Security Event: " + event.eventType) + "\n" +
        ("Source IP: " + event.sourceIP) + "\n" +
        ("Affected Device: " + event.affectedDevice)+ "\n" +
        ("Description: " + event.description)+ "\n" ;

        return securityeventlog;
    }

    //VISUALISE NETWORK
    public void networkTopologyMapping() {
        // Create a JGraphX graph
        mxGraph visualGraph = new mxGraph();
        Object parent = visualGraph.getDefaultParent();
    
        visualGraph.getModel().beginUpdate();
        try {
            // Add vertices to the JGraphX graph
            Map<NetworkDevice, Object> vertexMap = new HashMap<>();
            for (NetworkDevice device : networkGraph.vertexSet()) {
                Object vertex = visualGraph.insertVertex(parent, null, device.name, 20, 20, 80, 30);
                vertexMap.put(device, vertex);
            }
    
            // Add edges to the JGraphX graph
            for (DefaultEdge edge : networkGraph.edgeSet()) {
                NetworkDevice source = networkGraph.getEdgeSource(edge);
                NetworkDevice target = networkGraph.getEdgeTarget(edge);
                visualGraph.insertEdge(parent, null, "", vertexMap.get(source), vertexMap.get(target));
            }
        } finally {
            visualGraph.getModel().endUpdate();
        }
    
        // Set up a JFrame to display the JGraphX graph
        JFrame frame = new JFrame();
        frame.setTitle("Network Topology Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
        mxGraphComponent graphComponent = new mxGraphComponent(visualGraph);
        frame.add(graphComponent);
    
        // Apply a layout to the graph
        mxIGraphLayout layout = new mxHierarchicalLayout(visualGraph);
        layout.execute(parent);
    
        frame.pack();
        frame.setVisible(true);
    }

    public static void generateReport(String string1, String string2, String string3, String fileName) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

            // Write the strings to the file
            writer.write("\t\tYour Network Report");
            writer.newLine();
            writer.write("--------------------------------------");
            writer.newLine();
            writer.newLine();


            writer.write("Results of anomaly detection: ");
            writer.newLine();
            writer.write(string1);
            writer.newLine();
            writer.newLine();

            writer.write("Results of bandwidth monitoring: ");
            writer.newLine();
            writer.write(string2);
            writer.newLine();
            writer.newLine();
        
            writer.write("Results of security check: ");
            writer.newLine();
            writer.write(string3);

            // Close the file
            writer.close();

            System.out.println("Report generated successfully in file: " + fileName);
        } catch (IOException e) {
            System.err.println("An error occurred while generating the report: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        NetworkMonitoring networkMonitor = new NetworkMonitoring();

        String csvFilePath = "ADS dataset.csv";
        String reportFilePath = "YourNetworkReport.txt";
        networkMonitor.connectDevices(csvFilePath);

        String anomalyDetection = networkMonitor.anomalyDetection();
        String bandwidthUtilisation = networkMonitor.bandwidthUtilization();
        String securityeventmonitor = networkMonitor.securityEventMonitoring();

        generateReport(anomalyDetection, bandwidthUtilisation, securityeventmonitor, reportFilePath);
        networkMonitor.networkTopologyMapping();
    }
}
