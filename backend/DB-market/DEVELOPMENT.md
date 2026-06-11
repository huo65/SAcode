# DB-market Development Notes

## Database

Initialize the database with the single schema script:

```sql
source src/main/resources/schema.sql;
```

`schema.sql` contains the full table structure, indexes, delivery/order fields, demo users, and seed addresses. Existing plaintext demo passwords are converted to SHA-256 hashes after a successful login.

## Configuration

Keep `src/main/resources/application.example.yml` as the committed template. Use a local `application.yml` or environment variables for real database, JWT, and Alipay secrets.

The JWT secret is read from `DBMARKET_JWT_SECRET`; if it is absent, the app uses a development fallback.

## Verification

The checked-in Maven Wrapper is incomplete in the original project. Use a working local Maven installation:

```powershell
mvn test
```

If Maven is configured to use a private mirror and dependency resolution fails, fix the local Maven `settings.xml` or switch to a reachable mirror before treating failures as code failures.

This project also includes `maven-settings-public.xml` for bypassing a broken local mirror:

```powershell
mvn -s maven-settings-public.xml test
```

If a global Maven settings file still injects a broken mirror, override both global and user settings:

```powershell
mvn -gs maven-settings-public.xml -s maven-settings-public.xml test
```
