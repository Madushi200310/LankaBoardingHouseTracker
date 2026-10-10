# Lanka Boarding House Tracker

A platform to help boarding house owners in Sri Lanka list their properties,
and help users search and find boarding houses by district and town.

## Structure
- `android-app/` — Kotlin Android app (Jetpack Compose) with Admin, Owner and User dashboards
- `backend/` — Kotlin + Spring Boot REST API
- Database: PostgreSQL

## Architecture

Android App (Kotlin) → REST API (Kotlin + Spring Boot) → PostgreSQL

All three roles use a single Android app. After login, the user is routed to
their dashboard based on their role. The API is secured with JWT tokens.

## Roles & Features

### 1. Admin
- Reviews boarding house listing requests submitted by owners
- Approves a request, which publishes the listing
- Declines a request with a written reason that the owner can see

### 2. Boarding House Owner
- Submits a listing request (needs admin approval before it goes live), including:
  - Location (district and town)
  - Price
  - Rules and regulations
- Sees the status of each listing (Pending, Approved, Declined)
- If a listing is declined, sees the reason, edits it and resubmits

### 3. User (Searcher)
- Searches by selecting a **District**, then a **Town**
- Views approved listings with price, address and rules
- Contacts the owner with a Call button (phone dialer) or a WhatsApp button

Users can register in the app as an Owner or a User. Admin accounts cannot be
self-registered.

## Tech Stack
- **Backend:** Kotlin, Spring Boot 4.1.1, Spring Data JPA (Hibernate), Spring Security, JWT (jjwt), Gradle (Kotlin DSL), JDK 21
- **Database:** PostgreSQL
- **Android:** Kotlin, Jetpack Compose (Material 3), Navigation Compose, Retrofit + OkHttp, minSdk 26

## Prerequisites
- JDK 21
- PostgreSQL (with `psql` available, or pgAdmin)
- Android Studio (it can open both the `backend` and `android-app` folders)
- An Android emulator (AVD) or a physical device

## Running the backend

1. **Create the database**

```sql
   CREATE DATABASE lanka_boarding_house_db;
```

2. **Create your local config.** Copy
   `backend/src/main/resources/application-local.properties.example` to
   `backend/src/main/resources/application-local.properties` and fill in your
   PostgreSQL password and a JWT secret (32 or more characters).
   This file is git-ignored, so your secrets are never committed.

3. **Start the backend.** Open the `backend` folder in Android Studio and run
   `BackendApplication.kt`, or from a terminal:

```
   cd backend
   .\gradlew.bat bootRun
```

   The API runs at `http://localhost:8080`.

On first start, Hibernate creates the tables automatically, and the app seeds:
- All 25 districts and 79 towns (a starter set of major towns per district)
- A default admin account:

  | Email | Password |
  |---|---|
  | `admin@lankaboarding.lk` | `Admin@123` |

  **Change this password before any real deployment.**

Owner and User accounts are created by registering in the app.

## Running the Android app

1. Open the `android-app` folder in Android Studio and let Gradle sync.
2. Make sure the backend is running.
3. Run the app on an emulator.

**Networking:** the emulator reaches your PC's `localhost` through the special
address `10.0.2.2`, which is what the app uses
(`BASE_URL` in `data/remote/RetrofitClient.kt`).

To use a **physical phone** instead:
1. Put the phone and PC on the same Wi-Fi.
2. Set `BASE_URL` to `http://<your-PC-LAN-IP>:8080/`.
3. Add that IP as a `<domain>` in
   `app/src/main/res/xml/network_security_config.xml`
   (plain HTTP is only allowed for listed domains).
4. Allow port 8080 through Windows Firewall if the connection is refused.

## API overview

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public (returns a JWT in `token`) |
| GET | `/api/districts` | Public |
| GET | `/api/towns?districtId=` | Public |
| GET | `/api/boarding-houses/search?townId=` | Any logged-in user |
| POST | `/api/boarding-houses/request` | Owner |
| GET | `/api/boarding-houses/owner/{ownerId}` | Owner (own listings only) |
| PUT | `/api/boarding-houses/{id}` | Owner (edit own declined listing and resubmit) |
| GET | `/api/boarding-houses/pending` | Admin |
| GET | `/api/boarding-houses` | Admin |
| PUT | `/api/boarding-houses/{id}/approve` | Admin |
| PUT | `/api/boarding-houses/{id}/decline` | Admin (body: `{"reason": "..."}`) |

Protected endpoints need the header `Authorization: Bearer <token>`.
A missing or invalid token returns `401`; a valid token with the wrong role
returns `403`.

## Known limitations
- No photo uploads yet
- No search filters beyond district and town
- Owners cannot edit or remove an approved listing
- The WhatsApp button needs WhatsApp installed (it does not work on a bare emulator)
- The backend must be reachable from the device; there is no hosted deployment yet

## Status
In development