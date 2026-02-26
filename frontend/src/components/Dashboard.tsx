import { Link } from 'react-router-dom';

export default function Dashboard() {
  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <h1 className="text-4xl font-bold text-gray-800 mb-8">
        Welcome to EchoCare 🎵
      </h1>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {/* Patient Profiles Card */}
        <Link
          to="/profiles"
          className="bg-gradient-to-br from-blue-500 to-blue-600 rounded-lg shadow-lg p-8 hover:shadow-xl transition-all transform hover:-translate-y-1"
        >
          <div className="text-white">
            <div className="text-5xl mb-4">👥</div>
            <h2 className="text-2xl font-bold mb-2">Patient Profiles</h2>
            <p className="text-blue-100">
              Manage patient information, musical preferences, and symptoms
            </p>
          </div>
        </Link>

        {/* Playlists Card */}
        <Link
          to="/playlists"
          className="bg-gradient-to-br from-purple-500 to-purple-600 rounded-lg shadow-lg p-8 hover:shadow-xl transition-all transform hover:-translate-y-1"
        >
          <div className="text-white">
            <div className="text-5xl mb-4">🎵</div>
            <h2 className="text-2xl font-bold mb-2">Generate Playlists</h2>
            <p className="text-purple-100">
              Create personalized music playlists based on patient preferences
            </p>
          </div>
        </Link>

        {/* Feedback Card */}
        <Link
          to="/feedback"
          className="bg-gradient-to-br from-green-500 to-green-600 rounded-lg shadow-lg p-8 hover:shadow-xl transition-all transform hover:-translate-y-1"
        >
          <div className="text-white">
            <div className="text-5xl mb-4">⭐</div>
            <h2 className="text-2xl font-bold mb-2">Feedback</h2>
            <p className="text-green-100">
              Track and review patient responses to music therapy
            </p>
          </div>
        </Link>
      </div>

      {/* Quick Actions */}
      <div className="mt-12 bg-white rounded-lg shadow-lg p-8">
        <h2 className="text-2xl font-bold text-gray-800 mb-6">Quick Actions</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Link
            to="/profiles/create"
            className="flex items-center gap-4 p-4 border-2 border-gray-200 rounded-lg hover:border-blue-500 hover:bg-blue-50 transition-all"
          >
            <span className="text-3xl">➕</span>
            <div>
              <h3 className="font-semibold text-gray-800">Create New Patient</h3>
              <p className="text-sm text-gray-600">Register a new patient profile</p>
            </div>
          </Link>

          <Link
            to="/playlists"
            className="flex items-center gap-4 p-4 border-2 border-gray-200 rounded-lg hover:border-purple-500 hover:bg-purple-50 transition-all"
          >
            <span className="text-3xl">🎼</span>
            <div>
              <h3 className="font-semibold text-gray-800">Generate Playlist</h3>
              <p className="text-sm text-gray-600">Create a new therapeutic playlist</p>
            </div>
          </Link>

          <Link
            to="/feedback/create"
            className="flex items-center gap-4 p-4 border-2 border-gray-200 rounded-lg hover:border-green-500 hover:bg-green-50 transition-all"
          >
            <span className="text-3xl">💬</span>
            <div>
              <h3 className="font-semibold text-gray-800">Submit Feedback</h3>
              <p className="text-sm text-gray-600">Record patient music response</p>
            </div>
          </Link>

          <Link
            to="/playlists/history"
            className="flex items-center gap-4 p-4 border-2 border-gray-200 rounded-lg hover:border-yellow-500 hover:bg-yellow-50 transition-all"
          >
            <span className="text-3xl">📜</span>
            <div>
              <h3 className="font-semibold text-gray-800">View History</h3>
              <p className="text-sm text-gray-600">Browse past playlists</p>
            </div>
          </Link>
        </div>
      </div>

      {/* Info Section */}
      <div className="mt-8 bg-gradient-to-r from-indigo-50 to-blue-50 rounded-lg p-8">
        <h3 className="text-xl font-bold text-gray-800 mb-4">About EchoCare</h3>
        <p className="text-gray-700 leading-relaxed">
          EchoCare is a comprehensive music therapy management system designed to help
          caregivers create personalized therapeutic music experiences for patients. Track
          preferences, generate customized playlists, and monitor patient responses to
          improve care quality.
        </p>
      </div>
    </div>
  );
}
