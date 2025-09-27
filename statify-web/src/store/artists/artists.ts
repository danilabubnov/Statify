import { defineStore } from 'pinia';
import type { TopArtistsByPopularityQuery, TopArtistsByPopularityQueryVariables } from '../../graphql/generated/graphql';
import { apolloClient } from '../../api/apollo';
import { TOP_ARTISTS_QUERY } from '../../graphql/queries';

type TopArtists = TopArtistsByPopularityQuery['topArtistsByPopularity'];
type TopArtistItem = TopArtists[number];

export const artistStore = defineStore('artists', {
  state: () => ({
    loading: false,
    error: null as string | null,
    topArtists: [] as TopArtistItem[],
  }),

  actions: {
    async init() {
      await this.fetchTopArtists({ page: 0 });
    },

    async fetchTopArtists(payload: { page: number; size?: number }) {
      this.loading = true;
      this.error = null;

      try {
        const variables: TopArtistsByPopularityQueryVariables = {
          page: payload.page,
          size: payload.size ?? 25,
        };

        const { data } = await apolloClient.query({
          query: TOP_ARTISTS_QUERY,
          variables,
        });

        if (data?.topArtistsByPopularity) {
          this.topArtists = data.topArtistsByPopularity;
        } else {
          this.topArtists = [];
        }
      } catch (err: any) {
        console.error('Failed to fetch artists:', err);
        this.error = err.message || 'Unknown error';
      } finally {
        this.loading = false;
      }
    },
  },
});
