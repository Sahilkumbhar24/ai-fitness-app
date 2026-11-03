# 🚀 AI-Powered Fitness Tracker — Local Startup & Execution Guide

This guide provides the exact step-by-step instructions to run the entire distributed microservices application on your Windows PC.

---

## 📌 Architecture & Port Mapping

| Service / Tool | Port | Purpose / Database |
| :--- | :--- | :--- |
| **PostgreSQL** | `5432` | Relational DB for `userservice` (`fitness-micro-user`) |
| **MongoDB** | `27017` | NoSQL DB for `activityservice` & `aiservice` |
| **Apache Kafka** | `9092` | Event streaming broker (`activity-events` topic) |
| **Keycloak** | `8181` | OAuth2 / PKCE Identity Provider (`fitness-app` realm) |
| **Eureka Discovery** | `8761` | Service Registry & Discovery Server |
| **Config Server** | `8888` | Centralized Configuration Server |
| **User Service** | `8081` | User profiles and account management |
| **Activity Service** | `8082` | Workout logging + Kafka Event Producer |
| **AI Service** | `8083` | Kafka Event Consumer + Google Gemini 1.5 Flash |
| **API Gateway** | `8080` | Central Entrypoint & Route Matching + JWT Security |
| **Frontend Dashboard** | `5173` | React 19 + Vite UI |

---

## 📋 Step 1: Start Infrastructure & Databases

You need 4 infrastructure services: **PostgreSQL**, **MongoDB**, **Kafka**, and **Keycloak**.

### ⚡ Option A (Recommended): One-Click via Docker Compose

Since **Docker Desktop** is installed and running on your machine, you can spin up all 4 services at once:

1. Double-click **`start-infrastructure.bat`** (or run in terminal):
   ```cmd
   docker compose up -d
   ```
2. This automatically sets up:
   - **PostgreSQL** on `localhost:5432` with database `fitness-micro-user` (password `242004`)
   - **MongoDB** on `localhost:27017`
   - **Kafka & ZooKeeper** on `localhost:9092`
   - **Keycloak** on `http://localhost:8181` with:
     - Realm: **`fitness-app`** (pre-imported)
     - Client: **`oauth2-pkce-client`** (pre-configured for `http://localhost:5173/*`)
     - Pre-configured user: **`testuser`** / password: **`password123`**
3. To stop all containers later: double-click **`stop-infrastructure.bat`** (or run `docker compose down`).

---

### 📦 Option B (Alternative): Native Windows Standalone

If you ever wish to run them natively without Docker:

#### 1. PostgreSQL (Port 5432)
1. Ensure your PostgreSQL service is started on port `5432`.
2. Connect to PostgreSQL (via pgAdmin or SQL Shell `psql`) and create the database:
   ```sql
   CREATE DATABASE "fitness-micro-user";
   ```
3. Verify your database password in `configserver/src/main/resources/config/user-service.properties`:
   - Default configured password: `242004`.

#### 2. MongoDB (Port 27017)
1. Start your local MongoDB service on `localhost:27017`.
2. The databases (`aiactivityfitness` and `airecommmendationfitness`) are created automatically when services write data.

#### 3. Apache Kafka (Port 9092)
Inside your Kafka installation directory on Windows, open two command prompts:
1. **Start ZooKeeper**:
   ```cmd
   bin\windows\zookeeper-server-start.bat config\zookeeper.properties
   ```
2. **Start Kafka Broker**:
   ```cmd
   bin\windows\kafka-server-start.bat config\server.properties
   ```

#### 4. Keycloak (Port 8181)
1. In your Keycloak directory, start the development server on port `8181`:
   ```cmd
   kc.bat start-dev --http-port=8181
   ```
2. In Keycloak Admin Console (`http://localhost:8181`), create realm `fitness-app`, client `oauth2-pkce-client` (Public, Redirect `http://localhost:5173/*`, Web Origins `+`), and a user with credentials.


---

## 🔑 Step 2: Set Your Gemini API Key

The AI Service calls Google Gemini 1.5 Flash to analyze workouts.
Get an API key for free at [Google AI Studio](https://aistudio.google.com/).

Set it in Windows PowerShell:
```powershell
[System.Environment]::SetEnvironmentVariable('GEMINI_API_KEY', 'YOUR_ACTUAL_GEMINI_API_KEY', 'User')
```
*(Or set it per terminal session with `$env:GEMINI_API_KEY="your_api_key"`).*

---

## ⚙️ Step 3: Start Backend Microservices (In Order)

Open separate PowerShell or Command Prompt terminals for each service and run them in this **exact order**:

### Terminal 1: Eureka Discovery Server (Port 8761)
```powershell
cd "d:\Java Projects\AI Fitness Application project\eureka"
.\mvnw.cmd spring-boot:run
```
> Wait until Eureka is running. Verify by navigating to **http://localhost:8761** in your browser.

---

### Terminal 2: Config Server (Port 8888)
```powershell
cd "d:\Java Projects\AI Fitness Application project\configserver"
.\mvnw.cmd spring-boot:run
```
> Wait ~10 seconds until Config Server logs `Started ConfigserverApplication`.

---

### Terminal 3: User Service (Port 8081)
```powershell
cd "d:\Java Projects\AI Fitness Application project\userservice"
.\mvnw.cmd spring-boot:run
```

---

### Terminal 4: Activity Service (Port 8082)
```powershell
cd "d:\Java Projects\AI Fitness Application project\activityservice"
.\mvnw.cmd spring-boot:run
```

---

### Terminal 5: AI Service (Port 8083)
```powershell
cd "d:\Java Projects\AI Fitness Application project\aiservice"
$env:GEMINI_API_KEY="your_actual_gemini_api_key_here"
.\mvnw.cmd spring-boot:run
```

---

### Terminal 6: API Gateway (Port 8080)
```powershell
cd "d:\Java Projects\AI Fitness Application project\gateway"
.\mvnw.cmd spring-boot:run
```

---

### ✅ Verify Service Registration
Visit **[http://localhost:8761](http://localhost:8761)** (Eureka Dashboard).
You should see all registered applications:
- `USER-SERVICE`
- `ACTIVITY-SERVICE`
- `AI-SERVICE`
- `API-GATEWAY`

---

## 💻 Step 4: Configure and Run Frontend (React + Vite)

### 1. Update Frontend `.env` File
Make sure `fitness project/.env` points to your local machine:
```env
VITE_API_URL=http://localhost:8080/api
VITE_KEYCLOAK_URL=http://localhost:8181
VITE_KEYCLOAK_REALM=fitness-app
VITE_KEYCLOAK_CLIENT_ID=oauth2-pkce-client
```

### 2. Start the Frontend
In Terminal 7:
```powershell
cd "d:\Java Projects\AI Fitness Application project\fitness project"
npm install
npm run dev
```

The frontend will start at **[http://localhost:5173](http://localhost:5173)**.

---

## 🧪 Step 5: Test the End-to-End Flow

1. Open **[http://localhost:5173](http://localhost:5173)** in your browser.
2. Click **Get Started / Login** $\rightarrow$ It will redirect to Keycloak login.
3. Enter your test user credentials.
4. Once redirected back to the dashboard, click **Log Workout / Add Activity**:
   - Choose activity: e.g. **Running**
   - Duration: **30 mins**
   - Calories: **320 kcal**
5. Submit the form:
   - Request goes to `API Gateway` (:8080) $\rightarrow$ routed to `activityservice` (:8082).
   - `activityservice` saves workout to MongoDB and fires Kafka message `activity-events`.
   - `aiservice` (:8083) consumes the Kafka message, queries Google Gemini 1.5 Flash.
   - Click on the activity card $\rightarrow$ view your AI-generated Performance Analysis, Targeted Improvements, Next Workout Plan, and Safety Guidelines!

---

## 🛠️ Common Troubleshooting

| Issue | Cause & Solution |
| :--- | :--- |
| **Port already in use** | Run `netstat -ano \| findstr <PORT>` and terminate the process with `taskkill /F /PID <PID>`. |
| **Keycloak connection error (Gateway fails on startup)** | The API Gateway connects to `http://localhost:8181/realms/fitness-app/protocol/openid-connect/certs`. Ensure Keycloak is running and the `fitness-app` realm exists before starting the Gateway. |
| **PostgreSQL Authentication Failed** | Update the password in `configserver/src/main/resources/config/user-service.properties` to match your local PostgreSQL `postgres` user password. |
| **Kafka Connection Refused** | Ensure both ZooKeeper and Kafka are running before starting `activityservice` and `aiservice`. |
| **Gemini AI Quota / Invalid Key** | Ensure `GEMINI_API_KEY` is properly exported and valid from [Google AI Studio](https://aistudio.google.com/). |
