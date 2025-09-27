import { defineStore } from 'pinia';
import { TOP_TRACKS_QUERY } from '../../graphql/queries';
import type { TopTracksByPopularityQuery, TopTracksByPopularityQueryVariables } from '../../graphql/generated/graphql';
import { apolloClient } from '../../api/apollo';

type TopTracks = TopTracksByPopularityQuery['topTracksByPopularity'];
type TopTrackItem = TopTracks[number];

export const trackStore = defineStore('tracks', {
  state: () => ({
    loading: false,
    error: null as string | null,
    topTracks: [] as TopTrackItem[],
  }),

  actions: {
    async init() {
      await this.fetchTopTracks({ page: 0
        // , year: 2025 
      });
    },

    async fetchTopTracks(payload: { page: number; size?: number; year?: number }) {
      this.loading = true;
      this.error = null;

      try {
        const variables: TopTracksByPopularityQueryVariables = {
          page: payload.page,
          size: payload.size ?? 25,
          year: payload.year,
        };

        const { data } = await apolloClient.query({
          query: TOP_TRACKS_QUERY,
          variables,
        });

        if (data?.topTracksByPopularity) {
          this.topTracks = data.topTracksByPopularity;
        } else {
          this.topTracks = [];
        }
      } catch (err: any) {
        console.error('Failed to fetch tracks:', err);
        this.error = err.message || 'Unknown error';
      } finally {
        this.loading = false;
      }
    },
  },
});
