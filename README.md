# Food Ordering System

A full-stack web application for online food ordering, restaurant management, rider delivery tracking, and payment processing with M-Pesa and Cash on Delivery.

---

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [User Roles](#user-roles)
- [Technology Stack](#technology-stack)
- [System Architecture](#system-architecture)
- [Prerequisites](#prerequisites)
- [Local Setup and Installation](#local-setup-and-installation)
  - [1. Database Setup](#1-database-setup)
  - [2. Backend Setup](#2-backend-setup)
  - [3. Frontend Setup](#3-frontend-setup)
- [Environment Variables Configuration](#environment-variables-configuration)
  - [Backend Environment Variables](#backend-environment-variables)
  - [Frontend Environment Variables](#frontend-environment-variables)
- [Running Automated Tests](#running-automated-tests)
- [Deployment Guide](#deployment-guide)
  - [Backend Deployment (Render)](#backend-deployment-render)
  - [Frontend Deployment (Vercel)](#frontend-deployment-vercel)
- [Security and Data Protection](#security-and-data-protection)

---

## Overview

The Food Ordering System connects customers, restaurant owners, delivery riders, and administrators in a single platform. Customers can explore restaurants, browse menus, add items to cart, place orders, make payments via Safaricom M-Pesa STK Push or Cash on Delivery, and track live order progress. Restaurant owners manage their menus, business hours, and incoming orders. Riders receive delivery requests and update delivery statuses. Administrators manage platform users and approve restaurant applications.

---

## Key Features

- User Authentication: Secure registration, login, JWT authentication, refresh token rotation, and password reset via email.
- Restaurant Discovery: Browse approved restaurants by category, view menus, and check real-time open or closed status based on East Africa Time (EAT).
- Shopping Cart: Persistent shopping cart per customer with quantity controls and real-time total calculation.
- Payment Options:
  - M-Pesa STK Push (Live Daraja integration for production and instant simulation mode for local testing).
  - Cash on Delivery.
- Order Lifecycle Management: Real-time status tracking from Placed, Confirmed, Preparing, In Delivery, to Delivered.
- Delivery Management: Rider dispatching, delivery request acceptance, and order tracking.
- Reviews and Ratings: Verified customers can rate and review restaurants and menu items after order completion.
- Admin Panel: Platform statistics, user role management, and restaurant approval workflow.

---

## User Roles

1. Customer
   - Register and manage profile.
   - Browse restaurants, categories, and menus.
   - Add items to cart and customize order notes.
   - Checkout using M-Pesa or Cash on Delivery.
   - Track order status and view order history.
   - Write reviews for completed orders.

2. Restaurant Owner
   - Create and manage restaurant profile, operating hours, and location.
   - Manage menu categories and food items (pricing, availability, images).
   - View incoming customer orders.
   - Accept, prepare, and update order statuses.
   - Request delivery riders for ready orders.

3. Delivery Rider
   - View available delivery requests.
   - Accept delivery assignments.
   - Update delivery progress (Picked Up, Delivered).

4. Admin / Super Admin
   - Review and approve or reject pending restaurant registrations.
   - Manage user accounts, roles, and status (activate/disable).
   - View system-wide order logs and audit trails.

---

## Technology Stack

### Backend
- Language: Java 19+
- Framework: Spring Boot 3
- Security: Spring Security, JSON Web Tokens (JWT), BCrypt password hashing
- Database Access: Spring Data JPA, Hibernate, PostgreSQL Driver
- Database Migrations: Flyway
- Build Tool: Maven (mvnw)
- Payment Gateway: Safaricom Daraja API (M-Pesa STK Push)
- Maps and Geocoding: Google Maps API

### Frontend
- Framework: React 18 with TypeScript
- Build Tool: Vite
- Routing: React Router v6
- Styling: Tailwind CSS
- HTTP Client: Axios (with request/response interceptors for token refresh)
- State Management: React Context API

### Database
- PostgreSQL (relational database)

---

## System Architecture

```
[ Frontend: React + TypeScript (Vercel) ]
                  |
             HTTPS / REST API
                  |
                  v
[ Backend: Spring Boot 3 (Render) ]
   |                  |               |
   v                  v               v
[ PostgreSQL ]   [ Safaricom Daraja ]   [ Google Maps API ]
```

---

## Prerequisites

Before running the project, ensure you have the following installed:

- Java Development Kit (JDK 19 or higher)
- Node.js (version 18 or higher) and npm
- PostgreSQL (version 14 or higher)
- Git

---

## Local Setup and Installation

### 1. Database Setup

1. Open PostgreSQL using pgAdmin or the command-line tool (`psql`).
2. Create the database:

```sql
CREATE DATABASE "Food_Ordering_db";
```

3. Create a dedicated user (optional, or use existing postgres credentials):

```sql
CREATE USER food_user WITH ENCRYPTED PASSWORD 'food_password';
GRANT ALL PRIVILEGES ON DATABASE "Food_Ordering_db" TO food_user;
```

---

### 2. Backend Setup

1. Navigate to the backend directory:

```bash
cd backend
```

2. Configure environment settings in `src/main/resources/application.yml` or set environment variables:

```bash
export DB_URL="jdbc:postgresql://localhost:5432/Food_Ordering_db"
export DB_USERNAME="food_user"
export DB_PASSWORD="food_password"
export JWT_SECRET="your-256-bit-secret-key-must-be-at-least-32-characters-long"
```

3. Run the backend application:

On Windows:
```cmd
.\mvnw.cmd spring-boot:run
```

On Linux/macOS:
```bash
./mvnw spring-boot:run
```

The backend starts at `http://localhost:8080`.

---

### 3. Frontend Setup

1. Open a new terminal and navigate to the frontend directory:

```bash
cd "frontend/food ordering system"
```

2. Install project dependencies:

```bash
npm install
```

3. Create a `.env` file in the frontend root directory:

```env
VITE_API_URL=http://localhost:8080/api
```

4. Start the frontend development server:

```bash
npm run dev
```

The frontend will be available at `http://localhost:5173`.

---

## Environment Variables Configuration

### Backend Environment Variables

| Variable | Description | Default / Example |
| :--- | :--- | :--- |
| `PORT` | HTTP port for the backend service | `8080` |
| `DB_URL` | PostgreSQL JDBC connection URL | `jdbc:postgresql://localhost:5432/Food_Ordering_db` |
| `DB_USERNAME` | Database username | `food_user` |
| `DB_PASSWORD` | Database password | `your_db_password` |
| `JWT_SECRET` | Secret key for signing JWT tokens (min 32 chars) | `your_secure_secret_key_minimum_32_characters` |
| `JWT_ACCESS_EXPIRATION_MS` | Access token lifespan in milliseconds | `900000` (15 minutes) |
| `JWT_REFRESH_EXPIRATION_MS` | Refresh token lifespan in milliseconds | `604800000` (7 days) |
| `FRONTEND_BASE_URL` | Base URL of the frontend application | `http://localhost:5173` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated list of allowed origins | `http://localhost:5173,https://your-domain.vercel.app` |
| `APP_TIMEZONE` | Timezone for restaurant opening hours | `Africa/Nairobi` |
| `MPESA_ENABLED` | Enable live M-Pesa integration (`true`/`false`) | `false` |
| `MPESA_ENVIRONMENT` | Daraja environment (`sandbox` or `production`) | `sandbox` |
| `MPESA_TRANSACTION_TYPE` | Daraja transaction type | `CustomerPayBillOnline` |
| `MPESA_CONSUMER_KEY` | Safaricom Daraja Consumer Key | `your_consumer_key` |
| `MPESA_CONSUMER_SECRET` | Safaricom Daraja Consumer Secret | `your_consumer_secret` |
| `MPESA_SHORTCODE` | Business Paybill or Till Shortcode | `174379` |
| `MPESA_PASSKEY` | Lipa Na M-Pesa Online Passkey | `your_passkey` |
| `MPESA_CALLBACK_URL` | Public backend webhook endpoint for M-Pesa | `https://your-backend.onrender.com/api/payments/mpesa/callback` |
| `MPESA_CALLBACK_SECRET` | Secret token verified in webhook headers | `your_callback_secret` |
| `GOOGLE_MAPS_API_KEY` | Google Maps API key for geocoding | `your_google_maps_api_key` |

---

### Frontend Environment Variables

| Variable | Description | Example |
| :--- | :--- | :--- |
| `VITE_API_URL` | Base URL of the backend REST API | `https://your-backend.onrender.com/api` |

---

## Running Automated Tests

The backend includes a test suite covering business logic, security controls, access control, JWT verification, rate limiting, and payment flows.

To run all backend tests:

On Windows:
```cmd
cd backend
.\mvnw.cmd test
```

On Linux/macOS:
```bash
cd backend
./mvnw test
```

To run a frontend production build check:
```bash
cd "frontend/food ordering system"
npm run build
```

---

## Deployment Guide

### Backend Deployment (Render)

1. Connect your GitHub repository to Render.
2. Create a new Web Service and select the `backend` directory.
3. Choose the Environment: `Java` or `Docker`.
4. Build Command:
   ```bash
   ./mvnw clean package -DskipTests
   ```
5. Start Command:
   ```bash
   java -jar target/backend-0.0.1-SNAPSHOT.jar
   ```
6. In the Environment tab, add the necessary Environment Variables listed in the backend configuration table above.

---

### Frontend Deployment (Vercel)

1. Import your GitHub repository into Vercel.
2. Set the Root Directory to:
   `frontend/food ordering system`
3. Framework Preset: `Vite`
4. Build Command: `npm run build`
5. Output Directory: `dist`
6. Add the Environment Variable:
   - `VITE_API_URL`: `https://your-backend.onrender.com/api`
7. Click Deploy.

---

## Security and Data Protection

The system implements multiple cybersecurity best practices:

- Access Control: Role-Based Access Control (RBAC) enforced on all endpoints (`ROLE_CUSTOMER`, `ROLE_RESTAURANT_OWNER`, `ROLE_RIDER`, `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`).
- Broken Object Level Authorization (BOLA) Prevention: Customers can only view and modify their own carts, orders, and payment records. Restaurant owners can only manage their own restaurants.
- Data Minimization: Customer private data and rider GPS locations are masked once orders are completed or cancelled.
- Rate Limiting: Built-in in-memory rate limiting to protect authentication, payment initiation, and review submission endpoints from brute-force attacks and abuse.
- Secure Webhooks: M-Pesa callbacks require signature verification and secret headers (`X-Callback-Secret`) to prevent forged payment events.
- Audit Logging: Security events, order modifications, and role updates are recorded in the audit trail.
