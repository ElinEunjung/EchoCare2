import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { profileService } from '../api/profileService';
import type { CreatePatientProfileRequest } from '../types';

export default function ProfileForm() {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEditMode = Boolean(id);

  // const [caregivers, setCaregivers] = useState<Caregiver[]>([]);
  const [formData, setFormData] = useState<CreatePatientProfileRequest>({
    patientName: '',
    dementiaStage: 'mild',
    era: '',
    favoriteArtists: [],
    symptoms: [],
  });
  const [artistInput, setArtistInput] = useState('');
  const [symptomInput, setSymptomInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    // loadCaregivers();
    if (isEditMode && id) {
      loadProfile(id);
    }
  }, [id, isEditMode]);

  // const loadCaregivers = async () => {
  //   try {
  //     const data = await profileService.getCaregivers();
  //     setCaregivers(data);
  //   } catch (err) {
  //     console.error('Failed to load caregivers:', err);
  //   }
  // };

  const loadProfile = async (profileId: string) => {
    try {
      const profile = await profileService.getProfileById(profileId);
      setFormData({
        patientName: profile.patientName,
        dementiaStage: profile.dementiaStage,
        era: profile.era,
        favoriteArtists: profile.favoriteArtists,
        symptoms: profile.symptoms,
      });
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load profile');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (isEditMode && id) {
        await profileService.updateProfile(id, formData);
      } else {
        await profileService.createProfile(formData);
      }
      navigate('/profiles');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save profile');
    } finally {
      setLoading(false);
    }
  };

  const addArtist = () => {
    if (artistInput.trim() && !formData.favoriteArtists.includes(artistInput.trim())) {
      setFormData({
        ...formData,
        favoriteArtists: [...formData.favoriteArtists, artistInput.trim()],
      });
      setArtistInput('');
    }
  };

  const removeArtist = (artist: string) => {
    setFormData({
      ...formData,
      favoriteArtists: formData.favoriteArtists.filter((a) => a !== artist),
    });
  };

  const addSymptom = () => {
    if (symptomInput.trim() && !formData.symptoms.includes(symptomInput.trim())) {


      setFormData({
        ...formData,
        symptoms: [...formData.symptoms, symptomInput.trim()],
      });
      setSymptomInput('');
    }
  };

  const removeSymptom = (symptom: string) => {
    setFormData({
      ...formData,
      symptoms: formData.symptoms.filter((s) => s !== symptom),
    });
  };

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <div className="bg-white rounded-lg shadow-lg p-8">
        <h1 className="text-3xl font-bold text-gray-800 mb-8">
          {isEditMode ? 'Edit Patient Profile' : 'Create New Patient Profile'}
        </h1>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Patient Name *
            </label>
            <input
              type="text"
              required
              value={formData.patientName}
              onChange={(e) => setFormData({ ...formData, patientName: e.target.value })}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Enter patient name"
            />
          </div>

          <label className="block text-sm font-medium text-gray-700 mb-2">
              Dementia Stage *
            </label>
            <select
              required
              value={formData.dementiaStage}
              onChange={(e) => setFormData({ ...formData, dementiaStage: e.target.value as 'mild' | 'moderate' | 'severe' })}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="">Select Dementia</option>
              <option value="mild">Mild</option>
              <option value="moderate">Moderate</option>
              <option value="severe">Severe</option>
            </select>

          {/* <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Dementia Stage *
            </label>
            <input
              type="text"
              required
              value={formData.dementiaStage}
              onChange={(e) => setFormData({ ...formData, dementiaStage: e.target.value })}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div> */}

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Musical Era *
            </label>
            <select
              required
              value={formData.era}
              onChange={(e) => setFormData({ ...formData, era: e.target.value })}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="">Select era</option>
              <option value="1940s">1940s</option>
              <option value="1950s">1950s</option>
              <option value="1960s">1960s</option>
              <option value="1970s">1970s</option>
              <option value="1980s">1980s</option>
              <option value="1990s">1990s</option>
              <option value="2000s">2000s</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Favorite Artists
            </label>
            <div className="flex gap-2 mb-2">
              <input
                type="text"
                value={artistInput}
                onChange={(e) => setArtistInput(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), addArtist())}
                className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Add artist name"
              />
              <button
                type="button"
                onClick={addArtist}
                className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-lg font-medium transition-colors"
              >
                Add
              </button>
            </div>
            <div className="flex flex-wrap gap-2">
              {formData.favoriteArtists.map((artist) => (
                <span
                  key={artist}
                  className="bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm flex items-center gap-2"
                >
                  {artist}
                  <button
                    type="button"
                    onClick={() => removeArtist(artist)}
                    className="text-blue-600 hover:text-blue-800 font-bold"
                  >
                    ×
                  </button>
                </span>
              ))}
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Symptoms
            </label>
            <div className="flex gap-2 mb-2">
              <input
                type="text"
                value={symptomInput}
                onChange={(e) => setSymptomInput(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), addSymptom())}
                className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Add symptom"
              />
              <button
                type="button"
                onClick={addSymptom}
                className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-lg font-medium transition-colors"
              >
                Add
              </button>
            </div>
            <div className="flex flex-wrap gap-2">
              {formData.symptoms.map((symptom) => (
                <span
                  key={symptom}
                  className="bg-purple-100 text-purple-800 px-3 py-1 rounded-full text-sm flex items-center gap-2"
                >
                  {symptom}
                  <button
                    type="button"
                    onClick={() => removeSymptom(symptom)}
                    className="text-purple-600 hover:text-purple-800 font-bold"
                  >
                    ×
                  </button>
                </span>
              ))}
            </div>
          </div>

          <div className="flex gap-4 pt-4">
            <button
              type="submit"
              disabled={loading}
              className="flex-1 bg-blue-600 hover:bg-blue-700 text-white py-3 rounded-lg font-semibold transition-colors disabled:bg-gray-400"
            >
              {loading ? 'Saving...' : isEditMode ? 'Update Profile' : 'Create Profile'}
            </button>
            <button
              type="button"
              onClick={() => navigate('/profiles')}
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
