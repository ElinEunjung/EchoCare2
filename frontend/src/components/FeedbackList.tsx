import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { feedbackService } from '../api/feedbackService';
import { playlistService } from '../api/playlistService';
import { profileService } from '../api/profileService';
import type { FeedbackResponse, PatientProfile, Playlist } from '../types';

export default function FeedbackList() {
  const navigate = useNavigate();
  const [profiles, setProfiles] = useState<PatientProfile[]>([]);
  const [selectedProfileId, setSelectedProfileId] = useState('');
  const [feedbacks, setFeedbacks] = useState<FeedbackResponse[]>([]);
  const [playlists, setPlaylists] = useState<Playlist[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    loadProfiles();
  }, []);

  useEffect(() => {
    if (selectedProfileId) {
      loadFeedbacks(selectedProfileId);
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

  const loadFeedbacks = async (profileId: string) => {
    setLoading(true);
    setError('');
    try {
      const data = await feedbackService.getFeedbackForProfile(profileId);
      setFeedbacks(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load feedback');
    } finally {
      setLoading(false);
    }
  };

  const loadPlaylists = async (profileId: string) => {
    try {
      const data = await playlistService.getPlaylistsByProfile(profileId);
      setPlaylists(data);
    } catch (err) {
      console.error('Failed to load playlists:', err);
    }
  };

  const getPlaylistDetails = (playlistId: string) => {
    return playlists.find((p) => p.playlistId === playlistId);
  };

  const likedCount = feedbacks.filter((f) => f.liked).length;
  const dislikedCount = feedbacks.filter((f) => !f.liked).length;

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-800">💬 Feedback History</h1>
        <button
          onClick={() => navigate('/feedback/create')}
          className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-semibold transition-colors"
        >
          + Submit Feedback
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
          <div className="text-xl text-gray-600">Loading feedback...</div>
        </div>
      ) : feedbacks.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-8 text-center">
          <p className="text-gray-600 text-lg">No feedback found for this patient.</p>
          <button
            onClick={() => navigate('/feedback/create')}
            className="mt-4 text-blue-600 hover:text-blue-700 font-medium"
          >
            Submit your first feedback
          </button>
        </div>
      ) : (
        <div className="space-y-4">
          <div className="bg-blue-50 p-4 rounded-lg mb-4">
            <div className="grid grid-cols-3 gap-4 text-center">
              <div>
                <p className="text-2xl font-bold text-blue-600">{feedbacks.length}</p>
                <p className="text-sm text-gray-600">Total Feedback</p>
              </div>
              <div>
                <p className="text-2xl font-bold text-green-600">
                  👍 {likedCount}
                </p>
                <p className="text-sm text-gray-600">Liked</p>
              </div>
              <div>
                <p className="text-2xl font-bold text-red-600">
                  👎 {dislikedCount}
                </p>
                <p className="text-sm text-gray-600">Disliked</p>
              </div>
            </div>
          </div>

          {feedbacks.map((feedback) => {
            const playlist = getPlaylistDetails(feedback.playlistId);


            return (
              <div key={feedback.id} className="bg-white rounded-lg shadow-lg p-6">
                <div className="flex justify-between items-start mb-4">
                  <div className="flex-1">
                    <p className="text-sm text-gray-500 mb-1">
                      Playlist ID: {feedback.playlistId}
                    </p>
                    <p className="text-xs text-gray-400">
                      {new Date(feedback.createdAt).toLocaleString()}
                    </p>
                  </div>
                  <div className={`text-4xl ${feedback.liked ? 'text-green-500' : 'text-red-500'}`}>
                    {feedback.liked ? '👍' : '👎'}
                  </div>
                </div>

                {playlist && (
                  <div className="grid grid-cols-3 gap-3 mt-4 pt-4 border-t border-gray-200">
                    <div className="bg-blue-50 p-3 rounded">
                      <p className="text-xs text-gray-500 mb-1">Care Need</p>
                      <p className="text-sm font-semibold text-gray-800">
                        {playlist.careNeed.replace(/_/g, ' ').replace(/\b\w/g, (c: string) => c.toUpperCase())}
                      </p>
                    </div>
                    <div className="bg-purple-50 p-3 rounded">
                      <p className="text-xs text-gray-500 mb-1">Era</p>
                      <p className="text-sm font-semibold text-gray-800">
                        {playlist.era}
                      </p>
                    </div>
                    <div className="bg-green-50 p-3 rounded">
                      <p className="text-xs text-gray-500 mb-1">Dementia Stage</p>
                      <p className="text-sm font-semibold text-gray-800 capitalize">
                        {playlist.dementiaStage}
                      </p>
                    </div>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
