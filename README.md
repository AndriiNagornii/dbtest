# Database load testing

Requirments:
Java 8,
PostgresSQL (sorry, I have some problem with MySQL installing on my PC),
Gradle.

Run
1. gradle build 
2. gradle run

Description:
 - Used some concepts described below

1. <b>Stationarity mode in queueing theory</b> (https://en.wikipedia.org/wiki/M/M/1_queue)

System for successful functioning need to be in stationarity mode. When incoming intensity less than 
outcoming.
If incoming request intensity more than outcoming, queue will be accumulate requests. 
So we can use this approach to resolve particular problem

2. <b>Java Blocking Queue</b> for producer-consumer problem(https://en.wikipedia.org/wiki/Producer%E2%80%93consumer_problem)
I tried to use Java blocking queue (LinkedBlockingQueue) to investigate state of stationarity, when insert data in DB.

#### How it works

 - Configuration.java contains all config information.
 - Application is trying to increase intensity pear second from INITIAL_INTENSITY_PEAR_SECOND to FINAL_INTENSITY_PEAR_SECOND.
 - On each step refresh DB and use SECOND_FOR_PHASE time for <b>pushing</b> message to blocking queue (<b>producer</b>).
 - Threads consumer <b>pull out</b> message from queue and insert data to DB over transaction (<b>consuming</b>).
 - If size of blocking queue more then BLOCKING_QUEUE_DELIMITER, application break processing with message of queue overloading.  
 
 #### Project structure 
 Project consists from two simple modules. These modules are low depended on each other.
 1. TransactionManager
 2. TransactionExecutor
 
 TransactionExecutor invokes only one method of TransactionManager, it execute executeAtomicTransaction() for inserting data. It means that TransactionExecutor can be applied to different types of TransactionManager, e.g. different SQL Database, NoSQL Database or remote method invocation (http-requests).
