# Delay Queue Implementation using Spring Boot and Postgres

This is a Proof of Concept (POC) implementation of a Delay Queue using Spring Boot, Postgres. The Delay Queue stores a set of messages and their scheduled trigger time in a Postgres database. A Message Processor retrieves the messages from the Postgres database and stores them in a Redis Sorted Set. 
A Message Service retrieves the messages from the Redis Sorted Set and processes them.



## Requirements
- Java 8
- Spring Boot
- PostgreSQL



##  Getting Started

### Running the Application
Clone the repository: git clone https://github.com/satyajitnalavade/task_scheduler.git
Create a new database in PostgresSQL named tasks
Update the database configuration settings in the application.properties file as needed, including the database URL, username, and password.
Open a terminal window and navigate to the root directory of the project.
Run the application using the command: ./mvnw spring-boot:run

## Implementation Details
The Delay Queue consists of the following components:

### MessageProcessor
The MessageProcessor is responsible for processing tasks that are due for execution. 
The processMessages() method is called at a fixed interval, and it retrieves all tasks that are due for execution.
This is achieved through the use of the SchedulerConfig and TransactionConfig classes, which define the scheduling and transactional behavior of the application, respectively
For each task, it sends the message to a message broker (in this case, it just logs the message). Once the message is processed, the task is marked as completed in the database.

### MessageService
The MessageService is responsible for adding tasks to the delay queue, retrieving pending and completed tasks, and deleting tasks

## Usage
Schedule a New Task with Message
To schedule a new task with a message, use the following endpoint:
```
POST /messages/create
```

The MessageController class handles the scheduling of new tasks through the messages/create endpoint. The format of the request body for scheduling messages can be found in the ScheduleMessageInput class.
When a new task is scheduled, it is saved in the messages table in the PostgreSQL database.
The MessageProcessor runs as per the configuration provided in SchedulerConfig.java and TransactionConfig.java.
The MessageProcessor class is responsible for processing scheduled tasks by polling the database for any tasks that are due for execution. When a task is picked up for processing, the message is sent to the message service for processing. Once the message is processed successfully, the task is marked as completed in the messages table.


## Future Work
In the future, the application can be improved by incorporating Redis to avoid racing conditions and concurrency issues when processing messages.
By using Redis, we can implement a distributed lock mechanism that ensures only one instance of the MessageProcessor is running at a time. This will prevent race conditions and concurrency issues that may occur when multiple instances of the MessageProcessor try to process the same message.














