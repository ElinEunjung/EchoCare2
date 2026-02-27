import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { profileService } from '../api/profileService';
import type { PatientProfile } from '../types';

export default function ProfileList() {
  const navigate = useNavigate();
  const [profiles, setProfiles] = useState<PatientProfile[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadProfiles();
  }, []);

  const loadProfiles = async () => {
    try {
      const data = await profileService.getAllProfiles();
      setProfiles(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load profiles');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-xl text-gray-600">Loading profiles...</div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-800">Patient Profiles</h1>
        <button
          onClick={() => navigate('/profiles/create')}
          className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-semibold transition-colors"
        >
          + Create New Profile
        </button>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {profiles.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-8 text-center">
          <p className="text-gray-600 text-lg">No patient profiles found.</p>
          <button
            onClick={() => navigate('/profiles/create')}
            className="mt-4 text-blue-600 hover:text-blue-700 font-medium"
          >
            Create your first profile
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {profiles.map((profile) => (
            <div
              key={profile.id}
              className="bg-white rounded-lg shadow hover:shadow-lg transition-shadow p-6 cursor-pointer"
              onClick={() => navigate(`/profiles/${profile.id}`)}
            >
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-xl font-bold text-gray-800">{profile.patientName}</h3>
                <span className="text-2xl">👤</span>
              </div>
              <div className="space-y-2 text-sm text-gray-600">
                <p>
                  <strong>Musical Era:</strong> {profile.era}
                </p>
                <p>
                  <strong>Favorite Artists:</strong>{' '}
                  {profile.favoriteArtists.slice(0, 2).join(', ')}
                  {profile.favoriteArtists.length > 2 && '...'}
                </p>
                <p>
                  <strong>Dementia Stage:</strong> {profile.dementiaStage.charAt(0).toUpperCase() + profile.dementiaStage.slice(1)} 
                </p>
              </div>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  navigate(`/profiles/edit/${profile.id}`);
                }}
                className="mt-4 w-full bg-gray-100 hover:bg-gray-200 text-gray-800 py-2 rounded-lg text-sm font-medium transition-colors"
              >
                Edit Profile
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
