# 🌐 Complete Deployment Guide — AI Fitness Application

This guide walks you through deploying the complete full-stack application.

---

## 📑 Table of Contents

- [Architecture Overview](#-architecture-overview)
- [Pre-requisites: Cloud Free Accounts](#-pre-requisites-cloud-free-accounts)
- [Method 1: Deploy to Render (Recommended for Free Tier)](#-method-1-deploy-to-render-recommended-for-free-tier)
  - [Step 1: MongoDB Atlas (Free NoSQL DB)](#step-1-mongodb-atlas-free-nosql-db)
  - [Step 2: Upstash Kafka (Free Managed Kafka)](#step-2-upstash-kafka-free-managed-kafka)
  - [Step 3: Push Code to GitHub](#step-3-push-code-to-github)
  - [Step 4: Deploy with Render Blueprint](#step-4-deploy-with-render-blueprint)
  - [Step 5: Post-Deployment URL Configuration](#step-5-post-deployment-url-configuration)
- [Method 2: Self-Hosted on Any Cloud VM (AWS / DigitalOcean / Hetzner)](#-method-2-self-hosted-on-any-cloud-vm-aws--digitalocean--hetzner)
- [Method 3: Multi-Service Distributed Deploy (Paid Render / Railway)](#-method-3-multi-service-distributed-deploy-paid-render--railway)
- [Environment Variables Reference](#-environment-variables-reference)
- [Troubleshooting & Verification](#-troubleshooting--verification)

---

## 🏗️ Architecture Overview

| Component | Technology | Render Free-Tier Deployment |
| :--- | :--- | :--- |
| **Frontend** | React 19 + Vite + MUI | **Render Static Site** (100% Free, Global CDN, 0 sleep) |
| **Backend Core** | 5 Spring Boot Services (Gateway, Eureka, User, Activity, AI) | **Render Web Service** (Docker All-in-One container) |
| **Identity / Auth** | Keycloak 24 (OAuth2 + PKCE) | **Render Web Service** (Docker) |
| **Relational DB** | PostgreSQL 15 | **Render Managed PostgreSQL** (Free) |
| **Document DB** | MongoDB 7.0 | **MongoDB Atlas M0** (Free, 512MB) |
| **Message Broker**| Apache Kafka | **Upstash Kafka** (Free Serverless) |
| **AI Engine** | Google Gemini 1.5 Flash | **Google AI Studio API Key** (Free) |

> 💡 **Why All-in-One Backend for Render Free Tier?**  
> Render provides **750 free web service hours/month**. Running 6 separate microservices would exhaust the 750 hours in 5 days. By packaging the backend services into a single multi-process container using the root `Dockerfile` and `start.sh`, you only consume **1 service = 720 hours/month**, staying completely within the free limits!

---

## 📋 Pre-requisites: Cloud Free Accounts

Before deploying, sign up for these free accounts (all require no credit card or have 100% free tiers):

1. **[GitHub](https://github.com/)** — For your repository
2. **[Render](https://render.com/)** — Web hosting & PostgreSQL
3. **[MongoDB Atlas](https://www.mongodb.com/cloud/atlas/register)** — Managed MongoDB M0 Sandbox
4. **[Upstash](https://upstash.com/)** — Serverless Kafka
5. **[Google AI Studio](https://aistudio.google.com/)** — Gemini API Key

---

## 🚀 Method 1: Deploy to Render (Recommended for Free Tier)

### Step 1: MongoDB Atlas (Free NoSQL DB)

1. Go to [MongoDB Atlas](https://cloud.mongodb.com/) and create a free **M0 Cluster** (choose AWS / us-east-1).
2. Under **Security → Database Access**, create a user (e.g. `fitness_user` + password).
3. Under **Security → Network Access**, add IP address `0.0.0.0/0` (Allow access from anywhere).
4. Click **Connect → Drivers → Java (or Standard)** and copy your connection string:
   ```
   mongodb+srv://<username>:<password>@cluster0.xxxxx.mongodb.net/aiactivityfitness?retryWrites=true&w=majority
   ```

### Step 2: Upstash Kafka (Free Managed Kafka)

1. Go to [Upstash Console](https://console.upstash.com/kafka) and click **Create Cluster**.
   - Name: `fitness-kafka`
   - Region: `us-east-1` (same as Render / Atlas)
2. Under **Topics**, click **Create Topic**:
   - Topic Name: `activity-events`
   - Partitions: `1`
3. Click on the cluster **Details** tab and copy:
   - **Endpoint** (e.g., `selected-mammal-12345.upstash.io:9092`)
   - **Username** & **Password** (if SASL is enabled)

### Step 3: Push Code to GitHub

Make sure all changes are committed and pushed to your GitHub repository:

```bash
git add .
git commit -m "feat: add production Dockerfiles, render blueprint, and deployment configs"
git push origin main
```

### Step 4: Deploy with Render Blueprint

1. Log in to [Render Dashboard](https://dashboard.render.com/).
2. Click **New +** → **Blueprint**.
3. Connect your GitHub repository: `ai-fitness-app`.
4. Render will automatically detect `render.yaml` and show the resources:
   - `fitness-postgres` (PostgreSQL Database)
   - `fitness-keycloak` (Web Service)
   - `fitness-backend` (Web Service)
   - `fitness-frontend` (Static Site)
5. Fill in the prompted environment variables:
   - `GEMINI_API_KEY`: Your key from Google AI Studio
   - `SPRING_DATA_MONGODB_URI`: Your MongoDB Atlas URI from Step 1
   - `SPRING_KAFKA_BOOTSTRAP_SERVERS`: Your Upstash Kafka endpoint from Step 2
6. Click **Apply**. Render will begin provisioning all resources.

### Step 5: Post-Deployment URL Configuration

Once the services are deployed, update the URLs in Render:

1. **Get your deployed URLs from Render dashboard:**
   - Keycloak URL: `https://fitness-keycloak.onrender.com`
   - Backend URL: `https://fitness-backend.onrender.com`
   - Frontend URL: `https://fitness-frontend.onrender.com`

2. **Update Environment Variables on Render:**
   - In `fitness-backend` service:
     - Set `KEYCLOAK_JWK_URI` = `https://fitness-keycloak.onrender.com/realms/fitness-app/protocol/openid-connect/certs`
   - In `fitness-frontend` static site:
     - Set `VITE_API_URL` = `https://fitness-backend.onrender.com/api`
     - Set `VITE_KEYCLOAK_URL` = `https://fitness-keycloak.onrender.com`
     - Set `VITE_REDIRECT_URI` = `https://fitness-frontend.onrender.com`

3. **Trigger Redeploy** of `fitness-frontend` and `fitness-backend` to apply the updated environment variables.

---

## 🖥️ Method 2: Self-Hosted on Any Cloud VM (AWS / DigitalOcean / Hetzner)

If you have a Linux VPS (Ubuntu 22.04 / 24.04) on AWS EC2, DigitalOcean (\$6–12/mo Droplet), or Hetzner:

### 1. Install Docker & Git on the VM
```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y git curl docker.io docker-compose-plugin
sudo systemctl enable --now docker
sudo usermod -aG docker $USER
newgrp docker
```

### 2. Clone Repository
```bash
git clone https://github.com/your-username/ai-fitness-app.git
cd ai-fitness-app
```

### 3. Create `.env` file
```bash
cp .env.example .env
nano .env
# Fill in GEMINI_API_KEY, secure passwords, and your VM domain/IP
```

### 4. Start the Entire Stack with One Command
```bash
docker compose -f docker-compose.prod.yml up -d --build
```

### 5. Verify Running Services
```bash
docker compose -f docker-compose.prod.yml ps
```

The application is now accessible:
- **Frontend Dashboard:** `http://<your-server-ip>:3000`
- **API Gateway:** `http://<your-server-ip>:8080`
- **Keycloak Console:** `http://<your-server-ip>:8181`
- **Eureka Dashboard:** `http://<your-server-ip>:8761`

---

## 🌐 Method 3: Multi-Service Distributed Deploy (Paid Render / Railway)

If you have a **Render paid plan** or **Railway** and prefer each microservice running as an independent container:

1. Use `render-microservices.yaml` instead of `render.yaml`:
   ```bash
   cp render-microservices.yaml render.yaml
   git add render.yaml && git commit -m "chore: switch to microservices blueprint" && git push
   ```
2. Render will spin up:
   - `fitness-eureka` (port 8761)
   - `fitness-configserver` (port 8888)
   - `fitness-userservice` (port 8081)
   - `fitness-activityservice` (port 8082)
   - `fitness-aiservice` (port 8083)
   - `fitness-gateway` (port 8080)
   - `fitness-keycloak` (port 8181)
   - `fitness-frontend` (Static Site)

---

## 🔑 Environment Variables Reference

| Variable | Target Service | Purpose / Sample Value |
| :--- | :--- | :--- |
| `GEMINI_API_KEY` | `aiservice` / `fitness-backend` | Google Gemini API key (`AIzaSy...`) |
| `SPRING_DATA_MONGODB_URI` | `activityservice`, `aiservice` | `mongodb+srv://user:pass@cluster.mongodb.net/aiactivityfitness` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `activityservice`, `aiservice` | Kafka host:port (e.g. `localhost:9092` or Upstash) |
| `SPRING_DATASOURCE_URL` | `userservice` | PostgreSQL JDBC connection string |
| `SPRING_DATASOURCE_USERNAME` | `userservice` | PostgreSQL username (`postgres`) |
| `SPRING_DATASOURCE_PASSWORD` | `userservice` | PostgreSQL password |
| `KEYCLOAK_JWK_URI` | `gateway` | `https://<keycloak-host>/realms/fitness-app/protocol/openid-connect/certs` |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | All backend services | `http://<eureka-host>:8761/eureka/` |
| `VITE_API_URL` | `fitness project` | `https://<gateway-host>/api` |
| `VITE_KEYCLOAK_URL` | `fitness project` | `https://<keycloak-host>` |
| `VITE_REDIRECT_URI` | `fitness project` | `https://<frontend-host>` |

---

## 🩺 Troubleshooting & Verification

### 1. Verify Keycloak Realm Import
Open `https://<keycloak-url>/admin` and log in with your admin credentials. Ensure the **`fitness-app`** realm is present and the client **`oauth2-pkce-client`** is listed.

### 2. Verify Microservice Registration
Open the Eureka dashboard at `http://<eureka-url>:8761`. You should see:
- `USER-SERVICE`
- `ACTIVITY-SERVICE`
- `AI-SERVICE`
- `API-GATEWAY`

### 3. Verify Frontend Auth & Workout Logging
1. Open the frontend in your browser.
2. Click **Login** → You will be redirected to the Keycloak login screen.
3. Use the default test user:
   - Username: `testuser`
   - Password: `password123`
4. Log a workout (e.g., Running, 30 mins, 300 kcal).
5. Watch the activity appear in the dashboard and click it to view the AI Insights generated via Gemini!

