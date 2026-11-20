# Vehicle Rental System (VRS)

A clean, database-driven Vehicle Rental System built with Java (Spring Boot REST API), plain JDBC with prepared statements, MySQL, JWT middleware, and a lightweight HTML/CSS/JavaScript frontend.

## Database Setup

1. Make sure MySQL server is running locally on port `3306`.
2. Connect to MySQL and create the required schemas:
   ```sql
   CREATE DATABASE IF NOT EXISTS vrs;
   CREATE DATABASE IF NOT EXISTS vrs_test;
   ```

The schema tables (`Customer`, `Vehicle`, `Reservation`, `Bill`) will be auto-generated at application startup via `src/main/resources/schema.sql`.

## Environment Variables

Copy `.env.example` to `.env` and fill in your values:

```bash
cp .env.example .env
```

| Variable | Description | Example |
|---|---|---|
| `DB_PASSWORD` | MySQL root user password | `rootpassword` |
| `JWT_SECRET` | Secret key for HS256 tokens (min 32 characters) | `my_very_secure_jwt_secret_key_32_chars` |
| `ADMIN_PASSWORD` | Default password for seeded admin account | `adminSecret123` |

### Setting variables for the current session in PowerShell:
```powershell
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="my_very_secure_jwt_secret_key_32_chars"
$env:ADMIN_PASSWORD="adminSecret123"
```

## Running the Application

Using Maven:
```bash
mvn spring-boot:run
```

Once started:
- Access the application at: `http://localhost:8080` (or `http://127.0.0.1:8080`)
- Initial default Admin Account:
  - **Contact Number:** `9999999999`
  - **Password:** `<ADMIN_PASSWORD>` (configured above)

## Running the Tests

To run the complete automated test suite (unit tests, DAO integration tests, and full MockMvc end-to-end flows against `vrs_test`):

```bash
mvn test
```

## Architecture & Security Notes

- **JWT Middleware**: Built using a plain servlet filter (`JwtFilter`) without full Spring Security overhead.
- **Passwords**: Hashed with BCrypt (`spring-security-crypto`).
- **SQL Security**: All queries use `PreparedStatement` with parameterized placeholders (`?`).
- **Data Model**: Follows 3NF; `totalCost` is derived in SQL (`BasePrice * ReservPeriod`) and never redundantly stored.
- **Token Storage Tradeoff**: The frontend stores JWTs in `localStorage` for state management. In a production enterprise system, `HttpOnly` and `Secure` cookies with CSRF tokens would be preferred to mitigate XSS exposure.

