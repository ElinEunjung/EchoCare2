# EchoCare Frontend Testing Guide

## 🧪 Complete Testing Checklist

### Prerequisites
✅ Backend services running (docker-compose up)
✅ Frontend dev server running (http://localhost:5173)
✅ Gateway service accessible on http://localhost:8080

---

## 1️⃣ Authentication Flow Testing

### Test 1: User Registration
**Endpoint**: `POST /api/auth/register`

1. Navigate to http://localhost:5173
2. Click "Register here" link
3. Fill in the registration form:
   - Full Name: "John Caregiver"
   - Email: "john@example.com"
   - Password: "password123"
   - Role: "CAREGIVER"
4. Click "Register"
5. **Expected**: Redirect to dashboard with successful login

**Verify**:
- ✅ JWT token stored in localStorage
- ✅ Redirected to /dashboard
- ✅ Navbar appears with navigation links

### Test 2: User Login
**Endpoint**: `POST /api/auth/login`

1. Click "Logout" from navbar
2. Login with:
   - Email: "john@example.com"
   - Password: "password123"
3. Click "Login"
4. **Expected**: Redirect to dashboard

**Verify**:
- ✅ Successful login
- ✅ Token stored
- ✅ Dashboard loads

### Test 3: Protected Route Access
1. Logout from the application
2. Try to access: http://localhost:5173/profiles
3. **Expected**: Redirect to /login

---

## 2️⃣ Patient Profile Management

### Test 4: View All Profiles
**Endpoint**: `GET /api/profiles`

1. Login to the application
2. Click "Patient Profiles" in navbar
3. **Expected**: Display list of existing profiles (or empty state)

**Verify**:
- ✅ Profiles displayed in grid
- ✅ Shows patient name, musical era, artists
- ✅ "Create New Profile" button visible

### Test 5: Create New Patient Profile
**Endpoint**: `POST /api/profiles`

1. From Profiles page, click "+ Create New Profile"
2. Fill in the form:
   - **Patient Name**: "Emma Johnson"
   - **Date of Birth**: "1945-06-15"
   - **Musical Era**: "1960s"
   - **Favorite Artists**: 
     - Type "The Beatles" and click "Add"
     - Type "Elvis Presley" and click "Add"
   - **Symptoms**:
     - Type "Memory Loss" and click "Add"
     - Type "Anxiety" and click "Add"
   - **Caregiver**: Select from dropdown
3. Click "Create Profile"
4. **Expected**: Redirect to profiles list with new profile visible

**Verify**:
- ✅ Profile created successfully
- ✅ New profile appears in list
- ✅ All details saved correctly

### Test 6: Edit Existing Profile
**Endpoint**: `PUT /api/profiles/:id`

1. From profiles list, click "Edit Profile" on any profile
2. Update fields:
   - Add new artist: "Frank Sinatra"
   - Add new symptom: "Agitation"
3. Click "Update Profile"
4. **Expected**: Profile updated and redirected to list

**Verify**:
- ✅ Changes saved
- ✅ Updated data displayed

### Test 7: View Caregivers
**Endpoint**: `GET /api/caregivers`

1. When creating/editing profile
2. Open the "Caregiver" dropdown
3. **Expected**: List of all registered caregivers

**Verify**:
- ✅ Caregivers loaded
- ✅ Shows name and email

---

## 3️⃣ Playlist Generation & Management

### Test 8: Generate Playlist
**Endpoint**: `POST /api/playlists/generate`

1. Click "Playlists" in navbar (or "Generate Playlists" from dashboard)
2. Fill in playlist configuration:
   - **Patient Profile**: Select "Emma Johnson"
   - **Situation**: "Morning routine"
   - **Time of Day**: "Morning"
   - **Mood Preference**: "Calm"
3. Click "🎵 Generate Playlist"
4. **Expected**: Playlist appears on right side with songs

**Verify**:
- ✅ Playlist generated successfully
- ✅ Songs displayed with title, artist, year
- ✅ "Rate" button on each song
- ✅ Generation timestamp shown

### Test 9: View Playlist History
**Endpoint**: `GET /api/playlists/profile/:profileId`

1. Click "View History" button
2. Select a patient from dropdown
3. **Expected**: Display all past playlists for that patient

**Verify**:
- ✅ Historical playlists loaded
- ✅ Shows generation date
- ✅ Song count displayed
- ✅ Preview of songs shown

### Test 10: Get Song Details
**Endpoint**: `GET /api/playlists/song/:songId`

1. From generated playlist, note a song ID
2. Click "Rate" button on any song
3. **Expected**: Navigate to feedback form with song details

**Verify**:
- ✅ Song information displayed
- ✅ Song title and artist shown

---

## 4️⃣ Feedback System

### Test 11: Submit Feedback from Playlist
**Endpoint**: `POST /api/feedback`

1. Generate a playlist (Test 8)
2. Click "Rate" on any song
3. Fill feedback form:
   - **Patient Profile**: (pre-filled or select)
   - **Rating**: Click on "5" stars
   - **Comments**: "Patient smiled and sang along"
4. Click "Submit Feedback"
5. **Expected**: Success message, redirect to feedback list

**Verify**:
- ✅ Feedback submitted
- ✅ Success notification shown
- ✅ Redirected to feedback list

### Test 12: Submit Feedback Manually
**Endpoint**: `POST /api/feedback`

1. Go to Feedback page
2. Click "+ Submit Feedback"
3. Fill form:
   - **Patient Profile**: Select patient
   - **Song ID**: Enter a valid UUID
   - **Rating**: 4 stars
   - **Comments**: "Calming effect observed"
4. Submit

**Verify**:
- ✅ Manual feedback works
- ✅ Accepts song ID input

### Test 13: View Feedback History
**Endpoint**: `GET /api/feedback/profile/:profileId`

1. Navigate to "Feedback" from navbar
2. Select a patient from dropdown
3. **Expected**: Display all feedback for that patient

**Verify**:
- ✅ Feedback list loaded
- ✅ Statistics dashboard shows:
  - Total feedback count
  - Average rating
  - High ratings count (4-5 stars)
- ✅ Each feedback shows:
  - Song ID
  - Rating with stars
  - Timestamp
  - Comments

### Test 14: Rating Filter Visual
1. Submit various ratings (1-5 stars)
2. View feedback list
3. **Expected**: Visual differentiation of ratings

**Verify**:
- ✅ High ratings (4-5) in green
- ✅ Medium ratings (3) in yellow
- ✅ Low ratings (1-2) in red

---

## 5️⃣ Navigation & UX Testing

### Test 15: Dashboard Quick Actions
1. Go to Dashboard
2. Test all quick action cards:
   - "Create New Patient" → /profiles/create
   - "Generate Playlist" → /playlists
   - "Submit Feedback" → /feedback/create
   - "View History" → /playlists/history

**Verify**:
- ✅ All links work
- ✅ Smooth navigation

### Test 16: Navbar Navigation
1. Test all navbar links:
   - Dashboard
   - Patient Profiles
   - Playlists
   - Feedback
2. **Expected**: Active link highlighted

**Verify**:
- ✅ Active route highlighted with blue background
- ✅ All routes accessible

### Test 17: Logout Flow
1. Click "Logout" button in navbar
2. **Expected**: 
   - Redirect to login page
   - Token removed from localStorage
   - Cannot access protected routes

**Verify**:
- ✅ Logged out successfully
- ✅ Navbar hidden on login page
- ✅ Token cleared

---

## 6️⃣ Error Handling Testing

### Test 18: Invalid Login
1. Try to login with wrong credentials
2. **Expected**: Error message displayed

### Test 19: Network Error Handling
1. Stop backend services
2. Try any API operation
3. **Expected**: User-friendly error message

### Test 20: Form Validation
1. Try submitting forms with empty required fields
2. **Expected**: Browser validation prevents submission

### Test 21: 401 Unauthorized Handling
1. Manually delete token from localStorage
2. Try to access any protected route
3. **Expected**: Auto-redirect to login

---

## 7️⃣ Responsive Design Testing

### Test 22: Mobile View
1. Open Chrome DevTools (F12)
2. Toggle device toolbar (Ctrl+Shift+M)
3. Test on different screen sizes:
   - iPhone SE (375px)
   - iPad (768px)
   - Desktop (1920px)

**Verify**:
- ✅ Grid layouts adapt
- ✅ Buttons and forms usable
- ✅ No horizontal scrolling
- ✅ Text readable

---

## 8️⃣ Performance Testing

### Test 23: Build Performance
```bash
cd frontend
npm run build
```

**Verify**:
- ✅ Build completes without errors
- ✅ Bundle size reasonable (~300KB JS, ~18KB CSS)
- ✅ Gzipped size optimized

### Test 24: Page Load Performance
1. Open Network tab in DevTools
2. Refresh pages
3. **Expected**: Fast page loads (<2s)

---

## 🐛 Known Issues & Edge Cases

### Edge Case 1: No Profiles Available
1. Fresh database with no profiles
2. Try to generate playlist
3. **Expected**: Dropdown shows "Select patient" with no options

### Edge Case 2: Empty Feedback List
1. View feedback for patient with no feedback
2. **Expected**: Empty state message with CTA

### Edge Case 3: Token Expiration
1. Wait for JWT token to expire (if configured)
2. **Expected**: Auto-redirect to login on next API call

---

## 📊 Testing Results Template

```
Date: __________
Tester: __________

| Test # | Test Name | Status | Notes |
|--------|-----------|--------|-------|
| 1 | User Registration | ⬜ Pass / ⬜ Fail | |
| 2 | User Login | ⬜ Pass / ⬜ Fail | |
| 3 | Protected Routes | ⬜ Pass / ⬜ Fail | |
| 4 | View Profiles | ⬜ Pass / ⬜ Fail | |
| 5 | Create Profile | ⬜ Pass / ⬜ Fail | |
| 6 | Edit Profile | ⬜ Pass / ⬜ Fail | |
| 7 | View Caregivers | ⬜ Pass / ⬜ Fail | |
| 8 | Generate Playlist | ⬜ Pass / ⬜ Fail | |
| 9 | Playlist History | ⬜ Pass / ⬜ Fail | |
| 10 | Get Song Details | ⬜ Pass / ⬜ Fail | |
| 11 | Submit Feedback (Quick) | ⬜ Pass / ⬜ Fail | |
| 12 | Submit Feedback (Manual) | ⬜ Pass / ⬜ Fail | |
| 13 | View Feedback | ⬜ Pass / ⬜ Fail | |
| 14 | Rating Visuals | ⬜ Pass / ⬜ Fail | |
| 15 | Dashboard Actions | ⬜ Pass / ⬜ Fail | |
| 16 | Navbar Navigation | ⬜ Pass / ⬜ Fail | |
| 17 | Logout Flow | ⬜ Pass / ⬜ Fail | |
| 18 | Invalid Login | ⬜ Pass / ⬜ Fail | |
| 19 | Network Errors | ⬜ Pass / ⬜ Fail | |
| 20 | Form Validation | ⬜ Pass / ⬜ Fail | |
| 21 | 401 Handling | ⬜ Pass / ⬜ Fail | |
| 22 | Responsive Design | ⬜ Pass / ⬜ Fail | |
| 23 | Build Success | ⬜ Pass / ⬜ Fail | |
| 24 | Performance | ⬜ Pass / ⬜ Fail | |
```

---

## 🚀 Quick Start for Testing

### Terminal 1: Start Backend
```bash
cd EchoCare2
docker-compose up
```

### Terminal 2: Start Frontend
```bash
cd EchoCare2/frontend
npm install
npm run dev
```

### Browser
Open: http://localhost:5173

---

## 📸 What to Test in Each Screen

### Login/Register Screen
- [ ] Form validation
- [ ] Error messages
- [ ] Successful authentication
- [ ] Redirect after login

### Dashboard
- [ ] All cards clickable
- [ ] Quick actions work
- [ ] Statistics load (if implemented)

### Profiles Page
- [ ] Grid display
- [ ] Create button
- [ ] Edit button
- [ ] Profile cards
- [ ] Empty state

### Profile Form
- [ ] All inputs work
- [ ] Tag system (artists, symptoms)
- [ ] Caregiver dropdown
- [ ] Validation
- [ ] Save/Cancel

### Playlists Page
- [ ] Form inputs
- [ ] Generate button
- [ ] Real-time results
- [ ] Song list
- [ ] Rate buttons

### Feedback Page
- [ ] Patient selector
- [ ] Statistics display
- [ ] Feedback list
- [ ] Rating visuals
- [ ] Timestamps

---

## ✅ Success Criteria

All tests should:
- ✅ Complete without errors
- ✅ Display proper UI feedback
- ✅ Handle errors gracefully
- ✅ Navigate correctly
- ✅ Maintain responsive design
- ✅ Show loading states
- ✅ Persist data correctly

---

**Happy Testing! 🎉**
