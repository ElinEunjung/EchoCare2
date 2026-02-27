import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { playlistService } from '../api/playlistService';
import { profileService } from '../api/profileService';
import type { Playlist, PatientProfile } from '../types';

export default function PlaylistHistory() {
  const navigate = useNavigate();
  const [profiles, setProfiles] = useState<PatientProfile[]>([]);
  const [selectedProfileId, setSelectedProfileId] = useState('');
  const [playlists, setPlaylists] = useState<Playlist[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    loadProfiles();
  }, []);

  useEffect(() => {
    if (selectedProfileId) {
      loadPlaylists(selectedProfileId);
    }
  }, [selectedProfileId]);

  const loadProfiles = async () => {
    try {
      const data = await profileService.getAllProfiles();
      setProfiles(data);
      if (data.length > 0) {
        setSelectedProfileId(data[0].id);
      }
    } catch (err) {
      console.error('Failed to load profiles:', err);
    }
  };

  const loadPlaylists = async (profileId: string) => {
    setLoading(true);
    setError('');
    try {
      const data = await playlistService.getPlaylistsByProfile(profileId);
      setPlaylists(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load playlists');
    } finally {
      setLoading(false);
    }
  };

  const getProfileName = (patientId: string) => {
    const profile = profiles.find(p => p.id === patientId);
    return profile?.patientName || 'Unknown Patient';
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-800">📜 Playlist History</h1>
        <button
          onClick={() => navigate('/playlists')}
          className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-semibold transition-colors"
        >
          Generate New
        </button>
      </div>

      <div className="bg-white rounded-lg shadow-lg p-6 mb-6">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Select Patient
        </label>
        <select
          value={selectedProfileId}
          onChange={(e) => setSelectedProfileId(e.target.value)}
          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value="">Select patient</option>
          {profiles.map((profile) => (
            <option key={profile.id} value={profile.id}>
              {profile.patientName}
            </option>
          ))}
        </select>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {loading ? (
        <div className="text-center py-12">
          <div className="text-xl text-gray-600">Loading playlists...</div>
        </div>
      ) : playlists.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-8 text-center">
          <p className="text-gray-600 text-lg">No playlists found for this patient.</p>
          <button
            onClick={() => navigate('/playlists')}
            className="mt-4 text-blue-600 hover:text-blue-700 font-medium"
          >
            Generate your first playlist
          </button>
        </div>
      ) : (
        <div className="space-y-6">
          {playlists.map((playlist) => (
            <div key={playlist.playlistId} className="bg-white rounded-lg shadow-lg p-6">
              <div className="flex justify-between items-start mb-4">
                <div>
                  <h3 className="text-xl font-bold text-gray-800">
                    {getProfileName(playlist.patientId)}
                  </h3>
                  <div className="flex gap-4 mt-2">
                    <span className="text-sm text-gray-600">
                      Care Need: <span className="font-semibold">{playlist.careNeed.replace(/_/g, ' ').replace(/\b\w/g, (c: string) => c.toUpperCase())}</span>
                    </span>
                    <span className="text-sm text-gray-600">
                      Era: <span className="font-semibold">{playlist.era}</span>
                    </span>
                    <span className="text-sm text-gray-600">
                      Stage: <span className="font-semibold capitalize">{playlist.dementiaStage}</span>
                    </span>
                  </div>
                </div>
                <span className="bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm font-semibold">
                  {playlist.songs.length} songs
                </span>
              </div>

              <div className="space-y-2">
                {playlist.songs.slice(0, 5).map((song, index) => (
                  <div
                    key={song.id}
                    className="flex items-center gap-4 p-3 bg-gray-50 rounded-lg"
                  >
                    <div className="text-gray-400 font-bold w-6">{index + 1}</div>
                    <div className="flex-1">
                      <p className="font-semibold text-gray-800">{song.title}</p>
                      <p className="text-sm text-gray-600">{song.artist}</p>
                    </div>
                  </div>
                ))}
                {playlist.songs.length > 5 && (
                  <p className="text-center text-gray-500 text-sm pt-2">
                    +{playlist.songs.length - 5} more songs
                  </p>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
