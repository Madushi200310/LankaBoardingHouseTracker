# Lanka Boarding House Tracker

A full-stack mobile application to help users find boarding houses across Sri Lanka, allowing owners to list properties and admins to moderate them.

## Tech Stack
* **Backend:** Kotlin, Spring Boot 4, Spring Security (JWT), PostgreSQL
* **Android:** Kotlin, Jetpack Compose, Retrofit, DataStore

## Features Completed (Member B)
* **Security:** Role-based access (Admin, Owner, User), JWT expiration/invalidation, token versioning, environment variables for secrets, and a unified password policy.
* **User Experience:** Search boarding houses by district and town, filter by price range, and sort (Newest, Price Low-High, Price High-Low).
* **Favourites:** Users can save/unsave listings with a heart icon. The list cascades on deletion.
* **Contact:** Direct Call and WhatsApp integration for contacting owners.
* **Owner Features:** Add listings, edit and resubmit declined listings, mark listings as "Full" (hides from search), and delete listings.
* **Admin Features:** Approve/decline pending listings, and manage users (list all, disable/enable accounts).
* **Localization:** Full support for English, Sinhala, and Tamil.
* **Testing:** Backend integration tests (MockMvc) and Android ViewModel unit tests.

## How to Run

### Backend
1. Ensure PostgreSQL is running and create a database named `lanka_boarding_house_db`.
2. Copy `application-local.properties.example` to `application-local.properties` and fill in your DB password and a random JWT secret.
3. Add your default admin credentials to `application-local.properties`:
   ```properties
   admin.email=admin@lankaboarding.lk
   admin.password=Admin@123
