# basic-network-monitoring

The Network Monitoring project explores an application of graph data structures, with a focus on network analysis. While providing functionalities for network monitoring, its primary emphasis lies in facilitating understanding and utilization of graph data structures.

Apache Maven has been used to streamline the development process of this network monitoring project. Leveraging Maven's dependency management, the project integrates essential libraries like JGraphT and Apache Commons Math. Maven simplifies project management, enabling efficient development and dependency resolution for enhanced productivity.

# Network Monitoring Project

This project offers comprehensive network monitoring and analysis capabilities, leveraging robust data structures and algorithms for efficient data management and processing.

## Features

### 1. Connecting Devices

The `connectDevices()` method employs sophisticated data structures like HashMaps and Lists to efficiently parse device information and establish connections based on the provided CSV file. It utilizes a `DefaultDirectedGraph` from the JGraphT library to represent the network graph, enabling fast and intuitive graph operations.

### 2. Anomaly Detection

Anomaly detection is powered by statistical analysis, facilitated by the `DescriptiveStatistics` class from Apache Commons Math. The method `anomalyDetection()` utilizes ArrayLists to store latency and packet loss data for each device. By employing Z-scores calculated using mean and standard deviation, it efficiently identifies anomalies in network performance.

### 3. Bandwidth Utilization Monitoring

The `bandwidthUtilization()` method efficiently tracks bandwidth utilization across network devices. It utilizes HashMaps to store devices and their corresponding bandwidth usage, allowing for quick access and calculation. The method intelligently identifies the device with maximum bandwidth utilization and computes average utilization using well-structured data handling techniques.

### 4. Security Event Monitoring

Security event monitoring employs Lists to store instances of security events retrieved from the CSV file. The `securityEventMonitoring()` method efficiently processes and filters security events, focusing on non-successful events for detailed analysis. By leveraging Lists and custom data structures, it provides a structured overview of network security incidents.

### 5. Network Topology Visualization

The `networkTopologyMapping()` method utilizes JGraphX, a powerful graph visualization library. It efficiently constructs a graphical representation of the network topology using data structures such as HashMaps to map devices to their corresponding vertices. This visualization provides an intuitive insight into the network structure, enhancing network management and analysis.

## Usage

To utilize this project effectively:

1. Ensure you have the necessary dependencies installed.
2. Customize file paths in the `main()` method to match your environment.
3. Execute the `main()` method to activate network monitoring functionalities.
4. Review the generated report file for detailed insights into network performance and security.

## Dependencies

- JGraphX: Powerful visualization library for graph data.
- JGraphT: Versatile library for graph data structures and algorithms.
- Apache Commons Math: Comprehensive library for mathematical functions and statistical analysis.

## Author

This project was developed by Kavya Shanker, emphasizing the efficient utilization of data structures for robust network monitoring and analysis.
