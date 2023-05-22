# BoardRoom Booking API

This project implements a REST API using Spring Boot to facilitate board room booking for meetings

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven

### Installing

1. Clone the repository:

   ```shell
   git clone https://github.com/swetha521/java-test-swetha.git
   ```

2. Navigate to the project directory:

   ```shell
   cd java-test-swetha
   ```

3. Build the project:

   ```shell
   mvn clean install
   ```

### Running the Application

1. Start the application:

   ```shell
   mvn spring-boot:run
   ```

2. The application will start on `http://localhost:8080`.

## API Endpoints

The following endpoints are available:
- **POST /api/v1/bookings**: Sends a booking request

Request format :

```shell
  [Office start time in HHmm format] [Office end time in HHmm format]
  [Request submission time, in YYYY-MM-DD HH:MM:SS format] [Employee id] 
  [Meeting start time, in YYYY-MM-DD HH:MM format] [Meeting duration in hours] 
   ```
 Example :
```shell
  0900 1730 
  2020-01-18 10:17:06 EMP001 
  2020-01-21 09:00 2 
  2020-01-18 12:34:56 EMP002 
  2020-01-21 09:00 2 
   ```
## API Documentation

API documentation is available at `http://localhost:8080/swagger-ui/index.html` when the application is running.
