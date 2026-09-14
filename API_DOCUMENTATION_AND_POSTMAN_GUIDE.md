# 📘 WealthOps Backend - Complete API Documentation & Postman Guide

Welcome to the comprehensive API documentation and testing guide for the **WealthOps** platform. This document covers architecture, role hierarchy, DB seed instructions, end-to-end user/client onboarding workflows, and exhaustive details for all endpoints.

---

## 📑 Table of Contents
1. [Project Overview & Architecture](#-project-overview--architecture)
2. [User Roles & Permissions Matrix](#-user-roles--permissions-matrix)
3. [Important: User vs. Client Concept](#-important-user-vs-client-concept)
4. [Prerequisites & Initial Database Setup](#-prerequisites--initial-database-setup)
5. [End-to-End Workflow for Testing](#-end-to-end-workflow-for-testing)
6. [Detailed API Reference (Postman Ready)](#-detailed-api-reference-postman-ready)
   - [Module 1: Authentication (`/api/v1/auth`)](#module-1-authentication-apiv1auth)
   - [Module 2: Registration (`/api/v1/register`)](#module-2-registration-apiv1register)
   - [Module 3: Branches (`/api/v1/branches`)](#module-3-branches-apiv1branches)
   - [Module 4: Clients (`/api/v1/clients`)](#module-4-clients-apiv1clients)
   - [Module 5: Portfolios & Holdings (`/api/v1/portfolios`)](#module-5-portfolios--holdings-apiv1portfolios)
   - [Module 6: Transactions & Approval Workflow (`/api/v1/transactions`)](#module-6-transactions--approval-workflow-apiv1transactions)
7. [Postman Environment Setup](#-postman-environment-setup)

---

## 🏛 Project Overview & Architecture

- **Stack**: Java 17, Spring Boot 3.3.4, Spring Security 6 (Stateless JWT), Spring Data JPA, PostgreSQL, Hibernate.
- **Port**: `8080` (Default)
- **Base URL**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

---

## 👥 User Roles & Permissions Matrix

The system enforces 5 distinct roles:

| Role | Scope | Allowed Actions |
|---|---|---|
| `SUPER_ADMIN` | Organization-wide | Create/manage branches, register staff, delete clients, approve any transaction. *(Cannot be registered via API — must be seeded directly in DB).* |
| `BRANCH_MANAGER` | Branch-specific | Register staff for own branch, onboard clients, view branch clients, approve transactions below ₹1,00,000. |
| `RELATIONSHIP_MANAGER` (RM) | Assigned Clients | View assigned clients, initiate BUY/SELL/SWITCH transactions on behalf of assigned clients, view portfolios. |
| `COMPLIANCE_OFFICER` | Organization-wide | Review and Approve/Reject high-value transactions (≥ ₹1,00,000). |
| `CLIENT` | Self only | Self-register login, view own profile, initiate transactions on own account, view own transactions. |

---

## 💡 Important: User vs. Client Concept

A critical architectural distinction in WealthOps:

1. **`User` (Table: `users`)**:
   - Represents the **authentication identity** (email, BCrypt password, role, active status).
   - Used for login (`/api/v1/auth/login`) to receive a JWT Bearer token.
   - Both staff members and client portal users have a row in `users`.

2. **`Client` (Table: `clients`)**:
   - Represents the **financial / investor record** (KYC data: PAN, DOB, address, branchId, assignedRmId).
   - Created by `SUPER_ADMIN` or `BRANCH_MANAGER` via `POST /api/v1/clients`.
   - Creating a `Client` **automatically creates an empty `Portfolio`**.

3. **How they connect**:
   - When an investor logs in, the backend links the `User` email (from JWT) with the `Client` table record with the same email.
   - When calling `/api/v1/transactions/my` or `/api/v1/transactions`, the system matches `Client.email == User.email`.

---

## 🛠 Prerequisites & Initial Database Setup

Because `SUPER_ADMIN` accounts cannot be created via the public or staff registration APIs, you must seed the initial Super Admin in PostgreSQL directly.

### 1. PostgreSQL DB Details (from `application.properties`):
- **Host**: `localhost`
- **Port**: `5433` (verify your local port)
- **Database**: `wealthops_db`
- **Username**: `postgres`
- **Password**: Set in your environment variable `${DB_PASSWORD}`

### 2. SQL Query to Seed Super Admin:
Run this SQL query in your database:
```sql
-- Password: Admin@123
-- BCrypt Hash: $2a$10$e8wF5qKqDk5yV00EvycRfeWj7q61mQ2oF/8f0h0G5rM0K5wS7GZKe
INSERT INTO users (full_name, email, phone, password_hash, role, active, created_at)
VALUES (
    'Super Admin',
    'admin@wealthops.com',
    '9876543210',
    '$2a$10$e8wF5qKqDk5yV00EvycRfeWj7q61mQ2oF/8f0h0G5rM0K5wS7GZKe',
    'SUPER_ADMIN',
    true,
    NOW()
);
```

---

## 🚀 End-to-End Workflow for Testing

Follow this exact order when testing in Postman:

1. **Login as Super Admin** (`POST /api/v1/auth/login`) -> Copy `accessToken`.
2. **Create Branch** (`POST /api/v1/branches`) -> e.g., creates Branch ID `1`.
3. **Create Staff** (`POST /api/v1/register/staff`):
   - Create a `BRANCH_MANAGER` with `branchId: 1`.
   - Create a `RELATIONSHIP_MANAGER` with `branchId: 1` -> e.g., User ID `3`.
   - Create a `COMPLIANCE_OFFICER` with `branchId: null`.
4. **Create Client**:
   - Step A: Client self-registers user identity (`POST /api/v1/register/client`).
   - Step B: Super Admin or Branch Manager creates wealth profile (`POST /api/v1/clients`) with `branchId: 1` and `assignedRmId: 3`. (Portfolio is created automatically).
5. **Add Holdings or Initiate Transactions**:
   - Client or RM creates transaction (`POST /api/v1/transactions`).
   - If amount < 100,000: Branch Manager approves (`PUT /api/v1/transactions/{id}/approve`).
   - If amount >= 100,000: Compliance Officer approves.
6. **Verify Portfolio Updated** (`GET /api/v1/portfolios/client/{clientId}`).

---

## 📡 Detailed API Reference (Postman Ready)

---

### Module 1: Authentication (`/api/v1/auth`)

#### 1.1 Login
- **Endpoint**: `POST /api/v1/auth/login`
- **Auth**: None (Public)
- **Headers**:
  ```http
  Content-Type: application/json
  ```
- **Request Body**:
  ```json
  {
    "email": "admin@wealthops.com",
    "password": "Admin@123"
  }
  ```
- **Validation Rules**:
  - `email`: Valid email format, required.
  - `password`: Required.
- **Success Response (200 OK)**:
  ```json
  {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresInMs": 86400000,
    "userId": 1,
    "fullName": "Super Admin",
    "email": "admin@wealthops.com",
    "role": "SUPER_ADMIN",
    "branchId": null
  }
  ```

---

### Module 2: Registration (`/api/v1/register`)

#### 2.1 Register Client (Self-Service)
- **Endpoint**: `POST /api/v1/register/client`
- **Auth**: None (Public)
- **Headers**:
  ```http
  Content-Type: application/json
  ```
- **Request Body**:
  ```json
  {
    "fullName": "Rahul Sharma",
    "email": "rahul.sharma@example.com",
    "phone": "9876543211",
    "password": "Password@123"
  }
  ```
- **Validation Rules**:
  - `fullName`: 2 to 100 characters.
  - `email`: Valid email, unique in system.
  - `phone`: Exactly 10 digits starting with 6, 7, 8, or 9 (`^[6-9]\d{9}$`).
  - `password`: Min 8 chars, at least one uppercase, one lowercase, one digit (`^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$`).
- **Success Response (201 Created)**:
  ```json
  {
    "id": 2,
    "fullName": "Rahul Sharma",
    "email": "rahul.sharma@example.com",
    "role": "CLIENT",
    "branchId": null,
    "createdAt": "2026-09-02T23:30:00"
  }
  ```

#### 2.2 Register Staff Account
- **Endpoint**: `POST /api/v1/register/staff`
- **Auth**: `Bearer <SUPER_ADMIN_TOKEN>` or `Bearer <BRANCH_MANAGER_TOKEN>`
- **Headers**:
  ```http
  Content-Type: application/json
  Authorization: Bearer <TOKEN>
  ```
- **Request Body (Relationship Manager)**:
  ```json
  {
    "fullName": "Amit Verma",
    "email": "amit.rm@wealthops.com",
    "phone": "9811223344",
    "password": "Password@123",
    "role": "RELATIONSHIP_MANAGER",
    "branchId": 1
  }
  ```
- **Request Body (Compliance Officer)**:
  ```json
  {
    "fullName": "Neha Kapoor",
    "email": "neha.compliance@wealthops.com",
    "phone": "9822334455",
    "password": "Password@123",
    "role": "COMPLIANCE_OFFICER",
    "branchId": null
  }
  ```
- **Validation Rules**:
  - `role`: One of `BRANCH_MANAGER`, `RELATIONSHIP_MANAGER`, `COMPLIANCE_OFFICER`. (`SUPER_ADMIN` is forbidden).
  - `branchId`: Mandatory for `BRANCH_MANAGER` and `RELATIONSHIP_MANAGER`.
- **Success Response (201 Created)**:
  ```json
  {
    "id": 3,
    "fullName": "Amit Verma",
    "email": "amit.rm@wealthops.com",
    "role": "RELATIONSHIP_MANAGER",
    "branchId": 1,
    "createdAt": "2026-09-02T23:32:00"
  }
  ```

---

### Module 3: Branches (`/api/v1/branches`)

#### 3.1 Create Branch
- **Endpoint**: `POST /api/v1/branches`
- **Auth**: `Bearer <SUPER_ADMIN_TOKEN>`
- **Headers**:
  ```http
  Content-Type: application/json
  Authorization: Bearer <SUPER_ADMIN_TOKEN>
  ```
- **Request Body**:
  ```json
  {
    "name": "Mumbai Bandra Branch",
    "branchCode": "BDR001"
  }
  ```
- **Success Response (201 Created)**:
  ```json
  {
    "id": 1,
    "name": "Mumbai Bandra Branch",
    "branchCode": "BDR001"
  }
  ```

#### 3.2 Get All Branches
- **Endpoint**: `GET /api/v1/branches`
- **Auth**: `SUPER_ADMIN`, `BRANCH_MANAGER`, `RELATIONSHIP_MANAGER`
- **Response (200 OK)**:
  ```json
  [
    {
      "id": 1,
      "name": "Mumbai Bandra Branch",
      "branchCode": "BDR001"
    }
  ]
  ```

#### 3.3 Get Branch by ID
- **Endpoint**: `GET /api/v1/branches/{id}`
- **Auth**: `SUPER_ADMIN`, `BRANCH_MANAGER`, `RELATIONSHIP_MANAGER`
- **Response (200 OK)**: Single branch object.

#### 3.4 Update Branch
- **Endpoint**: `PUT /api/v1/branches/{id}`
- **Auth**: `SUPER_ADMIN`
- **Request Body**:
  ```json
  {
    "name": "Mumbai Bandra West Branch",
    "branchCode": "BDR001-W"
  }
  ```

#### 3.5 Delete Branch
- **Endpoint**: `DELETE /api/v1/branches/{id}`
- **Auth**: `SUPER_ADMIN`
- **Response**: `204 No Content`

---

### Module 4: Clients (`/api/v1/clients`)

#### 4.1 Create Client (Onboarding)
- **Endpoint**: `POST /api/v1/clients`
- **Auth**: `SUPER_ADMIN`, `BRANCH_MANAGER`
- **Headers**:
  ```http
  Content-Type: application/json
  Authorization: Bearer <TOKEN>
  ```
- **Request Body**:
  ```json
  {
    "fullName": "Rahul Sharma",
    "email": "rahul.sharma@example.com",
    "phone": "9876543211",
    "panNumber": "ABCDE1234F",
    "dateOfBirth": "1990-05-15",
    "address": "Flat 301, Heights, Bandra, Mumbai",
    "branchId": 1,
    "assignedRmId": 3
  }
  ```
- **Validation Rules**:
  - `panNumber`: Regex `[A-Z]{5}[0-9]{4}[A-Z]{1}` (5 letters, 4 digits, 1 letter).
  - `assignedRmId`: User MUST have role `RELATIONSHIP_MANAGER`.
  - `branchId`: Branch must exist.
- **Side-Effect**: Automatically creates an empty `Portfolio` associated with this client.
- **Success Response (201 Created)**:
  ```json
  {
    "id": 1,
    "fullName": "Rahul Sharma",
    "email": "rahul.sharma@example.com",
    "phone": "9876543211",
    "panNumber": "ABCDE1234F",
    "dateOfBirth": "1990-05-15",
    "address": "Flat 301, Heights, Bandra, Mumbai",
    "status": "ACTIVE",
    "branchId": 1,
    "branchName": "Mumbai Bandra Branch",
    "assignedRmId": 3,
    "assignedRmName": "Amit Verma",
    "createdAt": "2026-09-02T23:35:00"
  }
  ```

#### 4.2 Get Client by ID
- **Endpoint**: `GET /api/v1/clients/{id}`
- **Auth**: `SUPER_ADMIN`, `BRANCH_MANAGER`, `RELATIONSHIP_MANAGER`

#### 4.3 Get All Clients
- **Endpoint**: `GET /api/v1/clients`
- **Auth**: `SUPER_ADMIN`, `BRANCH_MANAGER`

#### 4.4 Get Clients by Branch
- **Endpoint**: `GET /api/v1/clients/branch/{branchId}`
- **Auth**: `SUPER_ADMIN`, `BRANCH_MANAGER`

#### 4.5 Get Clients by RM
- **Endpoint**: `GET /api/v1/clients/rm/{rmId}`
- **Auth**: `SUPER_ADMIN`, `BRANCH_MANAGER`, `RELATIONSHIP_MANAGER`

#### 4.6 Update Client
- **Endpoint**: `PUT /api/v1/clients/{id}`
- **Auth**: `SUPER_ADMIN`, `BRANCH_MANAGER`
- **Request Body**: Same format as Create Client.

#### 4.7 Delete Client
- **Endpoint**: `DELETE /api/v1/clients/{id}`
- **Auth**: `SUPER_ADMIN`
- **Response**: `204 No Content`

---

### Module 5: Portfolios & Holdings (`/api/v1/portfolios`)

#### 5.1 Get Portfolio by Portfolio ID
- **Endpoint**: `GET /api/v1/portfolios/{portfolioId}`
- **Auth**: `SUPER_ADMIN`, `BRANCH_MANAGER`, `RELATIONSHIP_MANAGER`
- **Response (200 OK)**:
  ```json
  {
    "id": 1,
    "clientId": 1,
    "clientName": "Rahul Sharma",
    "totalInvestedAmount": 100000.00,
    "totalCurrentValue": 112000.00,
    "createdAt": "2026-09-02T23:35:00",
    "holdings": [
      {
        "id": 1,
        "fundName": "HDFC Top 100 Fund",
        "folioNumber": "HDFC-100234",
        "units": 250.00,
        "investedAmount": 100000.00,
        "currentValue": 112000.00,
        "purchaseDate": "2026-02-01"
      }
    ]
  }
  ```

#### 5.2 Get Portfolio by Client ID
- **Endpoint**: `GET /api/v1/portfolios/client/{clientId}`
- **Auth**: `SUPER_ADMIN`, `BRANCH_MANAGER`, `RELATIONSHIP_MANAGER`

#### 5.3 Add Holding Directly
- **Endpoint**: `POST /api/v1/portfolios/{portfolioId}/holdings`
- **Auth**: `SUPER_ADMIN`, `BRANCH_MANAGER`, `RELATIONSHIP_MANAGER`
- **Request Body**:
  ```json
  {
    "fundName": "SBI Bluechip Fund",
    "folioNumber": "SBI-998877",
    "units": 100.50,
    "investedAmount": 50000.00,
    "currentValue": 53000.00,
    "purchaseDate": "2026-03-01"
  }
  ```
- **Response (201 Created)**: Holding object.

#### 5.4 Update Holding
- **Endpoint**: `PUT /api/v1/portfolios/{portfolioId}/holdings/{holdingId}`
- **Auth**: `SUPER_ADMIN`, `BRANCH_MANAGER`, `RELATIONSHIP_MANAGER`
- **Request Body**: Same format as Add Holding.

#### 5.5 Delete Holding
- **Endpoint**: `DELETE /api/v1/portfolios/{portfolioId}/holdings/{holdingId}`
- **Auth**: `SUPER_ADMIN`, `BRANCH_MANAGER`
- **Response**: `204 No Content`

---

### Module 6: Transactions & Approval Workflow (`/api/v1/transactions`)

#### Approval Rules & Thresholds:
- When a transaction is created, it is marked as `PENDING`.
- **Low Value (< ₹1,00,000)**: Approved/Rejected by `BRANCH_MANAGER` of the client's branch.
- **High Value (≥ ₹1,00,000)**: Approved/Rejected by `COMPLIANCE_OFFICER` (or `SUPER_ADMIN`).
- Upon **Approval**, the system automatically modifies or creates the client's `Holding` in their `Portfolio` (`BUY` increases units & value; `SELL`/`SWITCH` deducts units & value).

#### 6.1 Create Transaction
- **Endpoint**: `POST /api/v1/transactions`
- **Auth**: `CLIENT` (for their own client account) OR `RELATIONSHIP_MANAGER` (for their assigned clients).
- **Headers**:
  ```http
  Content-Type: application/json
  Authorization: Bearer <TOKEN>
  ```
- **Request Body (New Fund BUY)**:
  ```json
  {
    "clientId": 1,
    "holdingId": null,
    "type": "BUY",
    "fundName": "Nippon India Small Cap Fund",
    "folioNumber": "NIP-445566",
    "amount": 150000.00,
    "units": 120.50
  }
  ```
- **Request Body (Existing Holding SELL)**:
  ```json
  {
    "clientId": 1,
    "holdingId": 1,
    "type": "SELL",
    "fundName": "HDFC Top 100 Fund",
    "folioNumber": "HDFC-100234",
    "amount": 25000.00,
    "units": 50.00
  }
  ```
- **Types**: `BUY`, `SELL`, `SWITCH`.
- **Success Response (201 Created)**:
  ```json
  {
    "id": 1,
    "clientId": 1,
    "clientName": "Rahul Sharma",
    "portfolioId": 1,
    "holdingId": null,
    "type": "BUY",
    "fundName": "Nippon India Small Cap Fund",
    "folioNumber": "NIP-445566",
    "amount": 150000.00,
    "units": 120.50,
    "status": "PENDING",
    "requestedBy": "Rahul Sharma",
    "approvedBy": null,
    "remarks": null,
    "createdAt": "2026-09-02T23:45:00",
    "updatedAt": "2026-09-02T23:45:00"
  }
  ```

#### 6.2 Approve Transaction
- **Endpoint**: `PUT /api/v1/transactions/{id}/approve`
- **Auth**: 
  - If Amount < ₹1,00,000: `BRANCH_MANAGER` (same branch as client).
  - If Amount ≥ ₹1,00,000: `COMPLIANCE_OFFICER` or `SUPER_ADMIN`.
- **Request Body (Optional)**:
  ```json
  {
    "remarks": "KYC and fund allocation verified."
  }
  ```
- **Success Response (200 OK)**: Transaction with `status: "APPROVED"`, `approvedBy` populated, and holding updated.

#### 6.3 Reject Transaction
- **Endpoint**: `PUT /api/v1/transactions/{id}/reject`
- **Auth**: `BRANCH_MANAGER`, `COMPLIANCE_OFFICER`, `SUPER_ADMIN`
- **Request Body**:
  ```json
  {
    "remarks": "Exceeds daily trading limit for risk category."
  }
  ```
- **Success Response (200 OK)**: Transaction with `status: "REJECTED"`.

#### 6.4 Get Transaction by ID
- **Endpoint**: `GET /api/v1/transactions/{id}`
- **Auth**: Authenticated (Access rules enforced based on role and ownership).

#### 6.5 Get My Transactions (Client Only)
- **Endpoint**: `GET /api/v1/transactions/my`
- **Auth**: `CLIENT` (fetches transactions linked to the client whose email matches the caller's JWT).
- **Response (200 OK)**: List of transaction objects.

#### 6.6 Get All Transactions
- **Endpoint**: `GET /api/v1/transactions`
- **Auth**: `SUPER_ADMIN`, `COMPLIANCE_OFFICER`, `BRANCH_MANAGER` (branch filtered), `RELATIONSHIP_MANAGER` (assigned client filtered).

#### 6.7 Get Pending Transactions
- **Endpoint**: `GET /api/v1/transactions/pending`
- **Auth**: `SUPER_ADMIN`, `COMPLIANCE_OFFICER`, `BRANCH_MANAGER`, `RELATIONSHIP_MANAGER` (scoped automatically).

---

## 💻 Postman Environment Setup

Create an Environment in Postman named `WealthOps-Local` with the following variables:

| Variable | Initial Value | Current Value |
|---|---|---|
| `baseUrl` | `http://localhost:8080` | `http://localhost:8080` |
| `adminToken` | *(paste after super admin login)* | |
| `bmToken` | *(paste after branch manager login)* | |
| `rmToken` | *(paste after RM login)* | |
| `clientToken` | *(paste after client login)* | |
| `complianceToken` | *(paste after compliance officer login)* | |

### Postman Test Script (Auto-save Token):
In the **Tests** tab of your `POST /api/v1/auth/login` request, paste:
```javascript
var jsonData = pm.response.json();
if (jsonData.accessToken) {
    pm.environment.set("token", jsonData.accessToken);
}
```
Then for all subsequent requests, in the **Authorization** tab, set:
- **Type**: Bearer Token
- **Token**: `{{token}}`
