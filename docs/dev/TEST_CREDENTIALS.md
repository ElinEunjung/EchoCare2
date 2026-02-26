# Test Credentials for Profile Service

## Caregiver Test Accounts

All test caregivers use the same password for development simplicity.

| Username | Password | Email | Patients Assigned |
|----------|----------|-------|-------------------|
| `sarah_wilson` | `password123` | sarah.wilson@echocare.com | Margaret Anderson, Robert Johnson, Dorothy Williams |
| `john_martinez` | `password123` | john.martinez@echocare.com | James Davis, Elizabeth Miller, Charles Brown |
| `emma_thompson` | `password123` | emma.thompson@echocare.com | Helen Garcia, Arthur Martinez, Patricia Lee |

## Quick Login Test

```bash
# Test login endpoint (if implemented)
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "sarah_wilson",
    "password": "password123"
  }'

# Expected: JWT token or session object
```

## Password Hash Info

- **Algorithm:** BCrypt
- **Plain Text:** `password123`
- **Hash:** `$2a$10$slYQmyNdGzin7olVN3p2OPST9/PgBkqquzi8Ay0IQI7dsgXsZ3H7K`
- **Updated by:** V3__update_caregiver_passwords.sql migration

## ⚠️ Important Notes

- **Development Only:** These credentials are for local testing only
- **Not Production Ready:** Use proper password management in production
- **Updated:** V3__update_caregiver_passwords.sql contains proper BCrypt hashes
- **Functional:** Passwords now work with Spring Security authentication
- **Easy to Type:** Simple password `password123` for frontend testing

## Testing Steps

1. **Start profile-service:**
   ```bash
   docker-compose up profile-service
   ```

2. **Wait for success message:**
   - "Successfully applied 3 migrations" in logs
   - Should show V1, V2, and V3 executed

3. **Login with test credentials:**
   - Username: `sarah_wilson`
   - Password: `password123`

4. **Access patient profiles:**
   - Margaret Anderson (1950s, Mild)
   - Robert Johnson (1960s, Moderate)
   - And 8 more...

5. **Test functionality:**
   - View patient details
   - Check symptoms
   - View favorite artists
   - Edit patient information (if enabled)

## API Endpoints for Testing

```bash
# Get all patients
curl http://localhost:8081/api/profiles

# Get specific patient
curl http://localhost:8081/api/profiles/750e8400-e29b-41d4-a716-446655440001

# Get patient by caregiver ID
curl http://localhost:8081/api/profiles/caregiver/650e8400-e29b-41d4-a716-446655440001

# Create new patient (if enabled)
curl -X POST http://localhost:8081/api/profiles \
  -H "Content-Type: application/json" \
  -d '{
    "patientName": "New Patient",
    "era": "1970-1980",
    "dementiaStage": "mild",
    "symptoms": ["memory_loss"],
    "favoriteArtists": ["The Beatles"]
  }'
```

## Copy-Paste Ready

### For Frontend Testing
```
Username: sarah_wilson
Password: password123
```

### For Integration Testing
```
Username: john_martinez
Password: password123
```

---

**Last Updated:** February 25, 2026  
**Migration Version:** V3__update_caregiver_passwords.sql  
**Status:** ✅ Ready to use

