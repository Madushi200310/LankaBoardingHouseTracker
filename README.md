# Lanka Boarding House Tracker

A platform to help boarding house owners in Sri Lanka list their properties,
and help users search and find boarding houses by district and town.

## Structure
- `android-app/` — Kotlin Android app (Admin, Owner, and User dashboards)
- `backend/` — Kotlin + Spring Boot REST API
- Database: PostgreSQL

## Architecture

Android App (Kotlin) → REST API (Kotlin + Spring Boot) → PostgreSQL

All three roles are accessed from a single Android app, routed to their respective
dashboards after login based on assigned role.

## Roles & Features

### 1. Admin
- Reviews boarding house listing requests submitted by owners
- Accepts or declines each request before it goes live in the app
- Manages published/rejected listings

### 2. Boarding House Owner
- Requests to publish their boarding house on the platform (subject to Admin approval)
- Provides boarding house details when submitting a request:
  - Location (district & town)
  - Price
  - Rules and regulations (e.g. curfew, visitor policy, gender restrictions, etc.)
- Can be contacted by interested users through the app

### 3. User (Searcher)
- Searches for boarding houses using a two-step filter:
  1. Select **District**
  2. Select **Town** (within the chosen district)
- Views boarding house details, including rules, location, and price
- Contacts the boarding house owner directly through the app

## Status
🚧 In development
