import apiClient from './apiClient';
import type { GeneratePlaylistRequest, PlaylistResponse, Playlist, Song } from '../types';

export const playlistService = {
  generatePlaylist: async (data: GeneratePlaylistRequest): Promise<PlaylistResponse> => {
    const response = await apiClient.post('/api/playlists/generate', data);
    return response.data;
  },

  getPlaylistsByProfile: async (profileId: string): Promise<Playlist[]> => {
    const response = await apiClient.get(`/api/playlists/profile/${profileId}`);
    return response.data;
  },

  getSongById: async (songId: string): Promise<Song> => {
    const response = await apiClient.get(`/api/playlists/song/${songId}`);
    return response.data;
  },
};
