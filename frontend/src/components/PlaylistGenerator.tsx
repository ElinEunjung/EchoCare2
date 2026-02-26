import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { playlistService } from '../api/playlistService';
import { profileService } from '../api/profileService';
import type { GeneratePlaylistRequest, PatientProfile, PlaylistResponse } from '../types';

export default function PlaylistGenerator() {
  const navigate = useNavigate();
  const [profiles, setProfiles] = useState<PatientProfile[]>([]);
  const [generatedPlaylist, setGeneratedPlaylist] = useState<PlaylistResponse | null>(null);
  const [formData, setFormData] = useState<GeneratePlaylistRequest>({
    profileId: '',
    situation: '',
    timeOfDay: '',
    moodPreference: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    loadProfiles();
  }, []);

  const loadProfiles = async () => {
    try {
      const data = await profileService.getAllProfiles();
      setProfiles(data);
    } catch (err) {
      console.error('Failed to load profiles:', err);
    }
  };

  const handleGenerate = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    setGeneratedPlaylist(null);

    try {
      const response = await playlistService.generatePlaylist(formData);
      setGeneratedPlaylist(response);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to generate playlist');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-800">🎵 Generate Playlist</h1>
        <button
          onClick={() => navigate('/playlists/history')}
          className="bg-gray-600 hover:bg-gray-700 text-white px-6 py-3 rounded-lg font-semibold transition-colors"
        >
          View History
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Form */}
        <div className="bg-white rounded-lg shadow-lg p-8">
          <h2 className="text-xl font-bold text-gray-800 mb-6">
            Playlist Configuration
          </h2>

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
              {error}
            </div>
          )}

          <form onSubmit={handleGenerate} className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Patient Profile *
              </label>
              <select
                required
                value={formData.profileId}
                onChange={(e) => setFormData({ ...formData, profileId: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">Select patient</option>
                {profiles.map((profile) => (
                  <option key={profile.id} value={profile.id}>
                    {profile.name} - {profile.musicalEra}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Situation
              </label>
              <input
                type="text"
                value={formData.situation}
                onChange={(e) => setFormData({ ...formData, situation: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="e.g., Morning routine, Lunch time"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Time of Day
              </label>
              <select
                value={formData.timeOfDay}
                onChange={(e) => setFormData({ ...formData, timeOfDay: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">Select time</option>
                <option value="MORNING">Morning</option>
                <option value="AFTERNOON">Afternoon</option>
                <option value="EVENING">Evening</option>
                <option value="NIGHT">Night</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Mood Preference
              </label>
              <select
                value={formData.moodPreference}
                onChange={(e) => setFormData({ ...formData, moodPreference: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">Select mood</option>
                <option value="ENERGETIC">Energetic</option>
                <option value="CALM">Calm</option>
                <option value="HAPPY">Happy</option>
                <option value="NOSTALGIC">Nostalgic</option>
                <option value="RELAXING">Relaxing</option>
              </select>
            </div>

            <button
              type="submit"
              disabled={loading || !formData.profileId}
              className="w-full bg-blue-600 hover:bg-blue-700 text-white py-3 rounded-lg font-semibold transition-colors disabled:bg-gray-400"
            >
              {loading ? 'Generating...' : '🎵 Generate Playlist'}
            </button>
          </form>
        </div>

        {/* Generated Playlist */}
        <div className="bg-white rounded-lg shadow-lg p-8">
          <h2 className="text-xl font-bold text-gray-800 mb-6">
            Generated Playlist
          </h2>

          {!generatedPlaylist ? (
            <div className="text-center text-gray-500 py-12">
              <div className="text-6xl mb-4">🎵</div>
              <p>Generate a playlist to see songs here</p>
            </div>
          ) : (
            <div>
              <div className="mb-4 p-4 bg-blue-50 rounded-lg">
                <p className="text-sm text-gray-600">
                  <strong>Generated:</strong>{' '}
                  {new Date(generatedPlaylist.generatedAt).toLocaleString()}
                </p>
                {generatedPlaylist.recommendationReason && (
                  <p className="text-sm text-gray-600 mt-2">
                    <strong>Reason:</strong> {generatedPlaylist.recommendationReason}
                  </p>
                )}
              </div>

              <div className="space-y-3 max-h-96 overflow-y-auto">
                {generatedPlaylist.songs.map((song, index) => (
                  <div
                    key={song.id}
                    className="flex items-center gap-4 p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
                  >
                    <div className="text-lg font-bold text-gray-400 w-8">
                      {index + 1}
                    </div>
                    <div className="flex-1">
                      <h4 className="font-semibold text-gray-800">{song.title}</h4>
                      <p className="text-sm text-gray-600">{song.artist}</p>
                      {song.year && (
                        <p className="text-xs text-gray-500">{song.year}</p>
                      )}
                    </div>
                    <button
                      onClick={() => navigate(`/feedback/song/${song.id}`)}
                      className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors"
                    >
                      Rate
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
