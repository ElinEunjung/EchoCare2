import { useNavigate, Link, useLocation } from 'react-router-dom';
import { authService } from '../api/authService';

export default function Navbar() {
  const navigate = useNavigate();
  const location = useLocation();
  const isAuthenticated = authService.isAuthenticated();

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  const isActive = (path: string) => {
    return location.pathname === path
      ? 'bg-blue-700 text-white'
      : 'text-blue-100 hover:bg-blue-600';
  };

  if (!isAuthenticated) {
    return null;
  }

  return (
    <nav className="bg-blue-600 shadow-lg">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex space-x-4 items-center">
            <Link
              to="/dashboard"
              className="text-white text-xl font-bold hover:text-blue-200 transition-colors"
            >
              🎵 EchoCare
            </Link>
            <Link
              to="/dashboard"
              className={`px-3 py-2 rounded-md text-sm font-medium ${isActive('/dashboard')}`}
            >
              Dashboard
            </Link>
            <Link
              to="/profiles"
              className={`px-3 py-2 rounded-md text-sm font-medium ${isActive('/profiles')}`}
            >
              Patient Profiles
            </Link>
            <Link
              to="/playlists"
              className={`px-3 py-2 rounded-md text-sm font-medium ${isActive('/playlists')}`}
            >
              Playlists
            </Link>
            <Link
              to="/feedback"
              className={`px-3 py-2 rounded-md text-sm font-medium ${isActive('/feedback')}`}
            >
              Feedback
            </Link>
          </div>
          <div className="flex items-center">
            <button
              onClick={handleLogout}
              className="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
            >
              Logout
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
}
