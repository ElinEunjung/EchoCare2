import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import ProfileList from './components/ProfileList';
import ProfileForm from './components/ProfileForm';
import PlaylistGenerator from './components/PlaylistGenerator';
import PlaylistHistory from './components/PlaylistHistory';
import FeedbackList from './components/FeedbackList';
import FeedbackForm from './components/FeedbackForm';
import ProtectedRoute from './components/ProtectedRoute';
import { authService } from './api/authService';

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <Routes>
          {/* Public routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Protected routes */}
          <Route
            path="/"
            element={
              authService.isAuthenticated() ? (
                <Navigate to="/dashboard" replace />
              ) : (
                <Navigate to="/login" replace />
              )
            }
          />
          
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />

          {/* Profile routes */}
          <Route
            path="/profiles"
            element={
              <ProtectedRoute>
                <ProfileList />
              </ProtectedRoute>
            }
          />
          <Route
            path="/profiles/create"
            element={
              <ProtectedRoute>
                <ProfileForm />
              </ProtectedRoute>
            }
          />
          <Route
            path="/profiles/edit/:id"
            element={
              <ProtectedRoute>
                <ProfileForm />
              </ProtectedRoute>
            }
          />
          <Route
            path="/profiles/:id"
            element={
              <ProtectedRoute>
                <ProfileList />
              </ProtectedRoute>
            }
          />

          {/* Playlist routes */}
          <Route
            path="/playlists"
            element={
              <ProtectedRoute>
                <PlaylistGenerator />
              </ProtectedRoute>
            }
          />
          <Route
            path="/playlists/history"
            element={
              <ProtectedRoute>
                <PlaylistHistory />
              </ProtectedRoute>
            }
          />

          {/* Feedback routes */}
          <Route
            path="/feedback"
            element={
              <ProtectedRoute>
                <FeedbackList />
              </ProtectedRoute>
            }
          />
          <Route
            path="/feedback/create"
            element={
              <ProtectedRoute>
                <FeedbackForm />
              </ProtectedRoute>
            }
          />
          <Route
            path="/feedback/playlist/:playlistId"
            element={
              <ProtectedRoute>
                <FeedbackForm />
              </ProtectedRoute>
            }
          />

          {/* Catch all */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
