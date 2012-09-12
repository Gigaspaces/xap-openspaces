[![Build Status](https://secure.travis-ci.org/OpenSpaces/OpenSpaces.png)](http://travis-ci.org/OpenSpaces/OpenSpaces)


<h1>What is OpenSpaces?</h1>
OpenSpaces is designed to enable scaling-out of stateful applications in a simple way using the Spring Framework. 
It is shipped as an open source initiative from GigaSpaces, and supports the GigaSpaces Space-Based Architecture model out-of-the-box.

OpenSpaces is useful for Spring users, Service-Oriented Architecture (SOA) and Event-Driven Architecture (EDA) developers, transactional applications, real-time analytics, and Web 2.0 applications.

<h1>Who should use OpenSpaces?</h1>
* Spring Users – Spring users can use this framework for high-availability and scalability of their application – without being dependent on a J2EE container – by using a lightweight SLA-driven container. 
They can thus benefit from the simplicity of Spring throughout the entire application and deployment environment.

* SOA/EDA Developers – those looking to develop SOA/EDA applications will find OpenSpaces to be a simple and highly efficient approach for building such architectures in high-performance, stateful environments.

* Low-Latency Transactional Applications – typical transaction-processing applications, such as those for billing, trading and order management, 
involve handling incoming data-feeds, data enrichment (logic which turns those feeds into something meaningful), 
and a set of 'workers' that perform specific business logic on this data, such as matching. 
OpenSpaces provides the specific ingredients required for this process, such as POJO-driven event handlers, 
a Data Grid and Messaging Grid, which provide the mechanisms for processing the events, as well as the data and the workflow. 
It enables encapsulation of all those elements into a Processing Unit to ensure low latency. It can then be partitioned for scalability.

* Real-Time Analytics – real-time analytical applications, such as P&L calculation, reconciliation and fraud detection, are typically required to process high volumes of data at high speeds.
Writing such applications requires logic for loading data; optimization for reducing the memory footprint; 
in-memory query capabilities and parallel querying for efficient data retrieval and data aggregation; 
integration with external data sources (primarily relational databases); and scalability. 
These requirements specifically apply to the data capacity level, which will enable storing of data across a cluster of hundreds of units possibly holding several terabytes of data. 
OpenSpaces includes all of the above through GigaSpaces' Space-Based Architecture, and significantly simplifies the implementation of such applications using the OpenSpaces abstraction. 
This is done with event-handlers that act as the equivalent of stored-procedures, built-in support for a 'mirror-service' to enable synchronization with external databases, and Spring DAO support.

* Web 2.0 – Web 2.0 applications often have to support a very high number of concurrent users/connections. 
The read/write nature of Web 2.0 applications makes them more stateful than typical Web 1.0 applications. 
This makes them harder to scale through a simple load-balancer approach commonly used in existing stateless web-based applications. 
Spring already provides a rich environment, that simplifies the development of web-based applications through its MVC framework, 
and integrates with many other web frameworks and popular AJAX toolkits. With OpenSpaces, you can take these applications and simply scale them out by putting the data into in-memory partitions. 
Session information can also be stored into the In-Memory Data Grid (IMDG), thus eliminating a potential memory bottleneck, while having numerous concurrent users. 
In addition, the Processing Grid enables parallel processing of user requests asynchronously to avoid high latency. 
AJAX applications can also benefit from this framework by spreading the AJAX load between the different partitions. 
This allows handling a large number of consumers who are consuming real-time information through the system.
