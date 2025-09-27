import { defineStore } from 'pinia';
import { apolloClient } from '../../api/apollo';
import { TOP_ALBUMS_QUERY } from '../../graphql/queries';
import type { TopAlbumsByPopularityQuery, TopAlbumsByPopularityQueryVariables } from '../../graphql/generated/graphql';

type TopAlbums = TopAlbumsByPopularityQuery['topAlbumsByPopularity'];
type TopAlbumItem = TopAlbums[number];

export const albumStore = defineStore('albums', {
  state: () => ({
    loading: false,
    error: null as string | null,
    topAlbums: [] as TopAlbumItem[],
  }),

  actions: {
    async init() {
      await this.fetchTopAlbums({ page: 0
        // , year: 2025 
      });
    },

    async fetchTopAlbums(payload: { page: number; size?: number; year?: number }) {
      this.loading = true;
      this.error = null;

      try {
        const variables: TopAlbumsByPopularityQueryVariables = {
          page: payload.page,
          size: payload.size ?? 25,
          year: payload.year,
          albumType: 'ALBUM',
        };

        const { data } = await apolloClient.query({
          query: TOP_ALBUMS_QUERY,
          variables,
        });

        if (data?.topAlbumsByPopularity) {
          this.topAlbums = data.topAlbumsByPopularity;
        } else {
          this.topAlbums = [];
        }
      } catch (err: any) {
        console.error('Failed to fetch albums:', err);
        this.error = err.message || 'Unknown error';
      } finally {
        this.loading = false;
      }
    },
  },
});
