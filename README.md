# 🔐 LegacyVault

LegacyVault is a secure digital inheritance and password vault backend built using **Java, Spring Boot, Spring Security, PostgreSQL, and AES-256-GCM encryption**.

It allows users to securely store sensitive account credentials and assign trusted beneficiaries who can receive selected vault entries through a controlled release process.

## 🚀 Features

- User registration and authentication
- Secure password vault
- AES-256-GCM encryption for stored passwords
- Password reveal with account-password verification
- Beneficiary management
- Beneficiary email verification using secure tokens
- Selective vault-entry assignment to beneficiaries
- Release request workflow
- Admin approval system
- Time-based release eligibility
- Single-use beneficiary access tokens
- Secure beneficiary vault access
- Role-based authorization using Spring Security

## 🛠️ Tech Stack

**Backend**
- Java
- Spring Boot
- Spring Security
- Spring Data JPA / Hibernate

**Database**
- PostgreSQL

**Security**
- AES-256-GCM encryption
- BCrypt password hashing
- Cryptographically secure access tokens
- Role-based access control

**Build Tool**
- Maven

## 🏗️ Project Structure

```text
src/main/java/com/legacyvault/legacyvault/
│
├── config/
│   └── SecurityConfig.java
│
├── controller/
│   ├── AuthController.java
│   ├── VaultController.java
│   ├── BeneficiaryController.java
│   ├── BeneficiaryVerificationController.java
│   ├── BeneficiaryAccessController.java
│   ├── ReleaseRequestController.java
│   └── AdminReleaseController.java
│
├── model/
├── repository/
├── service/
├── dto/
│
└── LegacyvaultApplication.java
```

## 🔐 Encryption

Vault passwords are encrypted before being stored in the database using:

```text
AES/GCM/NoPadding
```

LegacyVault uses a **256-bit master encryption key** supplied through an environment variable.

The encryption key is never stored in the source code or committed to Git.

## 🔑 Environment Variable

Before starting the application, configure:

```bash
export LEGACYVAULT_MASTER_KEY="YOUR_BASE64_ENCODED_256_BIT_KEY"
```

Do **not** commit the real encryption key to GitHub.

## ▶️ Running the Application

Clone the repository:

```bash
git clone https://github.com/aaryaanmiraje/legacyvault.git
cd legacyvault
```

Set the required environment variable and configure your PostgreSQL database.

Then run:

```bash
./mvnw spring-boot:run
```

The application runs by default at:

```text
http://localhost:8080
```

## 🔄 Legacy Release Workflow

```text
User
 │
 ├── Stores encrypted vault entries
 │
 ├── Adds beneficiary
 │
 ├── Assigns selected vault entries
 │
 └── Creates release request
              │
              ▼
        Admin Verification
              │
              ▼
       Release Approved
              │
              ▼
     Release Eligibility
              │
              ▼
   Beneficiary Access Token
              │
              ▼
      Beneficiary Redeems
              │
              ▼
   Assigned Vault Entries
```

## 🛡️ Security Design

LegacyVault follows several security principles:

- Sensitive vault passwords are never stored as plaintext.
- Encryption keys are kept outside the source code.
- Beneficiaries only receive explicitly assigned vault entries.
- Beneficiary access tokens are temporary and single-use.
- Administrative endpoints require the `ADMIN` role.
- Authentication and authorization are handled through Spring Security.

## 📌 Project Status

Backend MVP completed.

Future improvements may include:

- React frontend
- Email delivery integration
- Docker deployment
- Automated testing
- Audit logging
- Production database deployment
- Improved key-management infrastructure

## 👨‍💻 Author

**Aaryan Miraje**

Built as a backend security project using Java and Spring Boot.