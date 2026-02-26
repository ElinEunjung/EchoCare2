import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { feedbackService } from '../api/feedbackService';
import { playlistService } from '../api/playlistService';
import { profileService } from '../api/profileService';
import type { SubmitFeedbackRequest, Song, PatientProfile } from '../types';

export default function FeedbackForm() {
  const navigate = useNavigate();
  const { songId } = useParams();
  
  const [profiles, setProfiles] = useState<PatientProfile[]>([]);
  const [song, setSong] = useState<Song | null>(null);
  const [formData, setFormData] = useState<SubmitFeedbackRequest>({
    profileId: '',
    songId: songId || '',
    rating: 3,
    comment: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    loadProfiles();
    if (songId) {
      loadSong(songId);
      setFormData((prev) => ({ ...prev, songId }));
    }
  }, [songId]);

  const loadProfiles = async () => {
    try {
      const data = await profileService.getAllProfiles();
      setProfiles(data);
    } catch (err) {
      console.error('Failed to load profiles:', err);
    }
  };

  const loadSong = async (id: string) => {
    try {
      const data = await playlistService.getSongById(id);
      setSong(data);
    } catch (err) {
      console.error('Failed to load song:', err);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    setSuccess(false);

    try {
      await feedbackService.submitFeedback(formData);
      setSuccess(true);
      setTimeout(() => {
        navigate('/feedback');
      }, 2000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to submit feedback');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <div className="bg-white rounded-lg shadow-lg p-8">
        <h1 className="text-3xl font-bold text-gray-800 mb-8">
          ⭐ Submit Feedback
        </h1>

        {success && (
          <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded mb-4">
            ✓ Feedback submitted successfully! Redirecting...
          </div>
        )}

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
            {error}
          </div>
        )}

        {song && (
          <div className="bg-blue-50 p-4 rounded-lg mb-6">
            <h3 className="font-bold text-gray-800">{song.title}</h3>
            <p className="text-gray-600">{song.artist}</p>
            {song.year && <p className="text-sm text-gray-500">{song.year}</p>}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
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
                  {profile.name}
                </option>
              ))}
            </select>
          </div>

          {!songId && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Song ID *
              </label>
              <input
                type="text"
                required
                value={formData.songId}
                onChange={(e) => setFormData({ ...formData, songId: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Enter song ID"
              />
            </div>
          )}

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-4">
              Rating: {formData.rating} / 5 ⭐
            </label>
            <div className="flex gap-2">
              {[1, 2, 3, 4, 5].map((rating) => (
                <button
                  key={rating}
                  type="button"
                  onClick={() => setFormData({ ...formData, rating })}
                  className={`flex-1 py-4 rounded-lg font-bold text-2xl transition-all ${
                    formData.rating >= rating
                      ? 'bg-yellow-400 text-white shadow-lg scale-105'
                      : 'bg-gray-200 text-gray-400 hover:bg-gray-300'
                  }`}
                >
                  {rating}
                </button>
              ))}
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Comments (Optional)
            </label>
            <textarea
              value={formData.comment}
              onChange={(e) => setFormData({ ...formData, comment: e.target.value })}
              rows={4}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="How did the patient respond to this song?"
            />
          </div>

          <div className="flex gap-4 pt-4">
            <button
              type="submit"
              disabled={loading || success}
              className="flex-1 bg-blue-600 hover:bg-blue-700 text-white py-3 rounded-lg font-semibold transition-colors disabled:bg-gray-400"
            >
              {loading ? 'Submitting...' : 'Submit Feedback'}
            </button>
            <button
              type="button"
              onClick={() => navigate('/feedback')}
              className="flex-1 bg-gray-200 hover:bg-gray-300 text-gray-800 py-3 rounded-lg font-semibold transition-colors"
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
