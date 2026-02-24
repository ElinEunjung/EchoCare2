# JWT Validation Explained

## What is JWT Validation?

**JWT (JSON Web Token) Validation** is the security mechanism that verifies the authenticity, integrity, and validity of tokens used for authentication and authorization in your EchoCare application.

### JWT Structure
A JWT consists of three parts separated by dots (`.`):
```
header.payload.signature
```

Example:
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NSIsInVzZXJuYW1lIjoiam9obiJ9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

1. **Header**: Algorithm and token type (`{"alg":"HS256","typ":"JWT"}`)
2. **Payload**: Claims (user data) - `{"sub":"12345","username":"john","exp":1234567890}`
3. **Signature**: Cryptographic signature ensuring integrity

### What JWT Validation Checks

In your EchoCare gateway, JWT validation verifies:

1. ✅ **Token Format** - Properly structured header.payload.signature
2. ✅ **Signature Validity** - Token hasn't been tampered with
3. ✅ **Expiration** - Token is not expired (24 hours in your app)
4. ✅ **Issuer** - Token was created by your auth service
5. ✅ **Claims** - Required data (caregiverId) is present

## How It Works in Your EchoCare Application

### 1. Token Generation (Profile Service)
Located in: `services/profile-service/src/main/java/no/kristiania/echocare/profile/config/JwtUtil.java`

```java
public String generateToken(UUID caregiverId, String username) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + EXPIRATION_TIME_MS); // 24 hours
    
    return Jwts.builder()
        .subject(caregiverId.toString())      // Who the token is for
        .claim("username", username)           // Additional claims
        .issuedAt(now)                        // When it was created
        .expiration(expiration)               // When it expires
        .signWith(secretKey)                  // Cryptographic signature
        .compact();
}
```

### 2. Token Validation (Gateway Service)
Located in: `services/gateway-service/src/main/java/no/kristiania/echocare/gateway/filter/JwtAuthenticationFilter.java`

Your gateway validates every request to protected endpoints:

```java
// Extract token from header
String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
if (!authHeader.startsWith("Bearer ")) {
    return onError(exchange, HttpStatus.UNAUTHORIZED);
}

String token = authHeader.substring(7); // Remove "Bearer " prefix

// Validate token signature and extract claims
Claims claims = Jwts.parser()
    .verifyWith(signingKey)              // Verify signature matches
    .build()
    .parseSignedClaims(token)            // Parse and validate
    .getPayload();

String caregiverId = claims.getSubject(); // Extract caregiver ID
```

### 3. Error Handling
Located in: `services/gateway-service/src/main/java/no/kristiania/echocare/gateway/exception/GlobalErrorWebExceptionHandler.java`

Your error handler catches JWT exceptions:

```java
if (ex instanceof JwtException) {
    status = HttpStatus.UNAUTHORIZED;
    message = "Invalid or expired JWT token";
}
```

## Why We Need to Test JWT Validation

### Security Reasons

1. **Prevent Unauthorized Access**
   - Without validation, anyone could access protected resources
   - Attackers could forge tokens to impersonate caregivers
   
2. **Protect Patient Data**
   - Your app handles sensitive patient profiles and playlists
   - Invalid tokens must be rejected to prevent data breaches

3. **Verify Token Tampering**
   - Attackers might modify token claims (e.g., changing caregiverId)
   - Signature validation catches these modifications

### Functional Reasons

4. **Token Expiration Handling**
   - Tokens expire after 24 hours
   - Must ensure expired tokens are rejected
   - Users get proper error messages to re-authenticate

5. **Service Communication**
   - Gateway extracts caregiverId and forwards via `X-Caregiver-Id` header
   - Downstream services (profile, playlist, feedback) rely on this
   - Invalid extraction breaks the entire system

6. **Error Flow Testing**
   - Missing tokens
   - Malformed tokens
   - Wrong secret key
   - Expired tokens
   - Each scenario needs specific error handling

## What Should Be Tested

### Test Scenarios

#### ✅ Valid Token Tests
```
✓ Valid token with correct signature → 200 OK
✓ Token contains correct caregiverId → Header forwarded correctly
✓ Token within expiration time → Access granted
```

#### ❌ Invalid Token Tests
```
✗ Missing Authorization header → 401 Unauthorized
✗ Authorization header without "Bearer " prefix → 401 Unauthorized
✗ Malformed token (not 3 parts) → 401 Unauthorized
✗ Invalid signature (wrong secret key) → 401 Unauthorized
✗ Expired token → 401 Unauthorized
✗ Token with missing claims → 401 Unauthorized
```

#### 🔓 Public Endpoint Tests
```
✓ /api/auth/** endpoints skip JWT validation
✓ Login and register work without tokens
```

## Real-World Attack Scenarios

### 1. Token Forgery
**Attack**: Hacker creates their own token claiming to be caregiverId="admin"
**Defense**: Signature validation fails because they don't have your secret key

### 2. Token Replay
**Attack**: Hacker steals a valid token and reuses it
**Defense**: Token expiration limits the damage window to 24 hours

### 3. Token Modification
**Attack**: Hacker modifies caregiverId in a stolen token
**Defense**: Signature becomes invalid when payload is changed

### 4. Missing Validation
**Attack**: Gateway accepts any string as a token
**Defense**: Your filter rejects requests without valid Bearer tokens

## Testing Strategy

### Unit Tests
Test individual components in isolation:
- `JwtUtil.generateToken()` creates valid tokens
- `JwtUtil.isTokenValid()` correctly validates/rejects
- `JwtUtil.extractCaregiverId()` extracts correct UUID

### Integration Tests
Test gateway filter with real HTTP requests:
- Send requests with valid tokens → 200 OK
- Send requests with expired tokens → 401 Unauthorized
- Send requests without tokens → 401 Unauthorized
- Verify X-Caregiver-Id header is forwarded

### End-to-End Tests
Test complete user flows:
1. Login → Receive JWT
2. Use JWT to create profile → Success
3. Use JWT to generate playlist → Success
4. Wait 24+ hours → Token expires
5. Try to use expired token → 401 Error
6. Login again → New JWT → Success

## Your Current Implementation

### Gateway JWT Filter
- ✅ Validates JWT on all routes except `/api/auth/**`
- ✅ Uses JJWT 0.12.x library with modern API
- ✅ Extracts caregiverId from token subject
- ✅ Forwards caregiverId to downstream services via header
- ✅ Returns 401 for invalid/missing tokens
- ✅ Logs validation attempts for debugging

### Profile Service JWT Filter
- ✅ Validates JWT for protected endpoints
- ✅ Sets Spring Security authentication context
- ✅ Extracts caregiverId for request processing

### Error Handling
- ✅ Global exception handler catches JWT exceptions
- ✅ Returns proper HTTP status codes
- ✅ Provides JSON error responses

## Testing Tools You Can Use

### Manual Testing
```powershell
# Test with valid token (after login)
$token = "eyJhbGc..."
curl -H "Authorization: Bearer $token" http://localhost:8080/api/profiles

# Test without token
curl http://localhost:8080/api/profiles

# Test with invalid token
curl -H "Authorization: Bearer invalid123" http://localhost:8080/api/profiles
```

### Automated Testing Scripts
Your test-data folder already has:
- `test-auth.ps1` - Tests authentication endpoints
- `test-gateway-feedback-e2e.ps1` - End-to-end gateway tests
- `test-rest-integration.ps1` - Integration tests

### JUnit Tests
Located in:
- `services/gateway-service/src/test/java/`
- Should include `JwtAuthenticationFilterTest.java`

## Best Practices

1. ✅ **Use Strong Secrets**: Your app uses Base64-encoded 256-bit keys
2. ✅ **Set Expiration**: 24-hour token lifetime balances security and UX
3. ✅ **Validate on Gateway**: Centralized validation at entry point
4. ✅ **Forward Identity**: X-Caregiver-Id header for downstream services
5. ✅ **Log Failures**: Security logging for debugging and monitoring
6. ✅ **Proper Error Messages**: Don't leak security details in errors
7. ✅ **Skip Public Endpoints**: Auth endpoints don't require tokens

## Common JWT Vulnerabilities (Avoided in Your Code)

❌ **Algorithm Confusion** - Accepting `none` algorithm
✅ Your code: Forces HMAC-SHA256 (`HS256`)

❌ **Weak Secrets** - Short or predictable keys
✅ Your code: Uses 256-bit Base64-encoded secrets

❌ **No Expiration** - Tokens valid forever
✅ Your code: 24-hour expiration enforced

❌ **No Validation** - Accepting any token
✅ Your code: Validates signature, expiration, format

## Summary

**JWT Validation = The Gatekeeper of Your Application**

It ensures:
- Only authenticated caregivers access the system
- Tokens haven't been forged or tampered with
- Expired credentials are rejected
- Patient data remains protected

**Testing is Critical Because:**
- Security bugs can expose sensitive patient data
- Token handling affects every API call
- Subtle bugs (wrong secret, missing expiration) have major impact
- Compliance and trust depend on proper authentication

Your EchoCare application handles sensitive healthcare data, making JWT validation and its testing absolutely essential for security, compliance, and user trust.

