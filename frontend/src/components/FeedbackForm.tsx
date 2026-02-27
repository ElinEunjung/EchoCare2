import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { feedbackService } from '../api/feedbackService';
import { profileService } from '../api/profileService';
import type { SubmitFeedbackRequest, PatientProfile } from '../types';

export default function FeedbackForm() {
  const navigate = useNavigate();
  const { playlistId } = useParams();
  
  const [profiles, setProfiles] = useState<PatientProfile[]>([]);
  const [formData, setFormData] = useState<SubmitFeedbackRequest>({
    patientProfileId: '',
    playlistId: playlistId || '',
    liked: true,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    loadProfiles();
    if (playlistId) {
      setFormData((prev) => ({ ...prev, playlistId }));
    }
  }, [playlistId]);

  const loadProfiles = async () => {
    try {
      const data = await profileService.getAllProfiles();
      setProfiles(data);
    } catch (err) {
      console.error('Failed to load profiles:', err);
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
          💬 Submit Playlist Feedback
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

        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Patient Profile *
            </label>
            <select
              required
              value={formData.patientProfileId}
              onChange={(e) => setFormData({ ...formData, patientProfileId: e.target.value })}
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

          {!playlistId && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Playlist ID *
              </label>
              <input
                type="text"
                required
                value={formData.playlistId}
                onChange={(e) => setFormData({ ...formData, playlistId: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Enter playlist ID"
              />
            </div>
          )}

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-4">
              Did the patient enjoy this playlist?
            </label>
            <div className="flex gap-4">
              <button
                type="button"
                onClick={() => setFormData({ ...formData, liked: true })}
                className={`flex-1 py-6 rounded-lg font-bold text-xl transition-all ${
                  formData.liked
                    ? 'bg-green-500 text-white shadow-lg scale-105'
                    : 'bg-gray-200 text-gray-400 hover:bg-gray-300'
                }`}
              >
                👍 Liked
              </button>
              <button
                type="button"
                onClick={() => setFormData({ ...formData, liked: false })}
                className={`flex-1 py-6 rounded-lg font-bold text-xl transition-all ${
                  !formData.liked
                    ? 'bg-red-500 text-white shadow-lg scale-105'
                    : 'bg-gray-200 text-gray-400 hover:bg-gray-300'
                }`}
              >
                👎 Disliked
              </button>
            </div>
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
