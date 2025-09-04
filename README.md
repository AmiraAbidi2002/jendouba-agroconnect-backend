# Jendouba AgroConnect - Backend

## 1. Overview
This is the backend API for **Jendouba AgroConnect**, built with **Dropwizard (Java)**.  
It provides REST endpoints for authentication, crop listings, messaging, weather integration, and farm location data.

## 2. Features
- JWT-based authentication (Login / Register)
- User management: Farmers and Buyers
- Crop listing CRUD (Create, Read, Update, Delete)
- Messaging system between users
- Weather integration using OpenWeather API
- Farm location coordinates
- PostgreSQL  persistence with Hibernate ORM
- Input validation and error handling

## 3. Technologies
- Java 17+
- Dropwizard 3.x
- Hibernate ORM
- PostgreSQL
- Maven for dependency management
- JWT for authentication
- External APIs: OpenWeather

## 4. Setup Instructions

### 4.1 Prerequisites
- Java 17+
- Maven
- PostgreSQL installed and running

### 4.2 Database Setup
1. Create PostgreSQL database:
```sql
CREATE DATABASE agroconnect;
CREATE USER agro_user WITH PASSWORD '12373291';
GRANT ALL PRIVILEGES ON DATABASE agroconnect TO agro_user;
```

2. The database schema will be automatically created via Hibernate if hbm2ddl.auto is set to update.

### 4.3 Configure Application
Edit config.yml:
```yaml
server:
  applicationConnectors:
    - type: http
      port: 8080

database:
  driverClass: org.postgresql.Driver
  user: agro_user
  password: 12373291
  url: jdbc:postgresql://localhost:5432/agroconnect
  properties:
  charSet: UTF-8
  hibernate.hbm2ddl.auto: update

```
### 4.4 Build and Run
1. Build the project with Maven:
```bash
mvn clean package
```

2. Run the Dropwizard application:
```bash
java -jar target/agroconnect-backend-1.0-SNAPSHOT.jar server config.yml
```
- The API will run on http://localhost:8080

## 5. Endpoints
1. Authentication :
- POST /api/auth/register - Register user
- POST /api/auth/login - Login user (returns JWT)

2. Crops :
- GET /api/crops - List all crops
- POST /api/crops - Create a crop
- PUT /api/crops/{id} - Update a crop
- DELETE /api/crops/{id} - Delete a crop

3. Messages :
- GET /api/messages/{userId} - Get messages for a user
- POST /api/messages - Send a message

4. Weather :
- GET /api/weather - Fetch weather forecast for Jendouba

## 6. Project Structure
   src/main/java/
   com.jendouba.agroconnect/
   api/        # Resource classes (REST endpoints)
   core/       # Entity classes (User, Crop, Message)
   db/         # Hibernate DAOs
   auth/       # JWT authentication


## 7. Notes
- Make sure PostgreSQL is running and accessible.
- Use a valid OpenWeather API key for weather endpoints.
- JWT token must be included in the Authorization header for protected routes.
- Input validation and error handling are implemented to ensure API stability.
- The backend may take 30–60 seconds to wake up on Render free tier.
  If you see "Application loading..." just wait a bit and refresh.


## 8. Live Demo
- Backend via Render: https://jendouba-agroconnect-backend-1.onrender.com