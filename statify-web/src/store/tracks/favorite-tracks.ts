import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { apolloClient } from '../../api/apollo';
import { GET_MY_FAVORITE_TRACKS_QUERY } from '../../graphql/queries';
import type {
  GetMyFavoriteTracksQuery,
  GetMyFavoriteTracksQueryVariables,
  FavTrack,
} from '../../graphql/generated/graphql';

const DEFAULT_PAGE_SIZE = 100;

export const favTrackStore = defineStore('favorite-tracks', () => {
  const loading = ref(false);
  const error = ref<string | null>(null);
  const tracksMap = ref<Map<number, FavTrack[]>>(new Map());
  const totalCount = ref<number>(0);
  const hasMore = ref(false);

  const currentTracks = computed(() => {
    const sparseArray: (FavTrack | undefined)[] = new Array(totalCount.value);

    for (const [offset, batch] of tracksMap.value.entries()) {
      for (let i = 0; i < batch.length; i++) {
        sparseArray[offset + i] = batch[i];
      }
    }

    return sparseArray;
  });

  const fetchTracksAtOffset = async (offset: number, size: number = DEFAULT_PAGE_SIZE) => {
    if (tracksMap.value.has(offset)) {
      return;
    }

    loading.value = true;
    error.value = null;

    try {
      const needTotal = totalCount.value === 0;

      const { data } = await apolloClient.query<
        GetMyFavoriteTracksQuery,
        GetMyFavoriteTracksQueryVariables
      >({
        query: GET_MY_FAVORITE_TRACKS_QUERY,
        variables: { size, offset, withTotal: needTotal },
        fetchPolicy: 'network-only',
      });

      const connection = data?.getMyFavoriteTracks;
      const items = connection?.items ?? [];

      const newMap = new Map(tracksMap.value);
      newMap.set(offset, items);
      tracksMap.value = newMap;

      if (connection?.totalCount !== undefined && connection.totalCount !== null) {
        totalCount.value = connection.totalCount;
      }
      hasMore.value = connection?.hasMore ?? false;
    } catch (err: any) {
      console.error('Failed to fetch favorite tracks:', err);
      error.value = err?.message ?? 'Unknown error';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const loadRange = async (startOffset: number, endOffset: number) => {
    const promises: Promise<void>[] = [];

    for (let offset = startOffset; offset < endOffset; offset += DEFAULT_PAGE_SIZE) {
      if (!tracksMap.value.has(offset)) {
        promises.push(fetchTracksAtOffset(offset));
      }
    }

    await Promise.all(promises);
  };

  const init = async () => {
    await fetchTracksAtOffset(0);
  };

  const reset = () => {
    tracksMap.value = new Map();
    totalCount.value = 0;
    hasMore.value = false;
    loading.value = false;
    error.value = null;
  };

  return {
    loading,
    error,
    totalCount,
    hasMore,
    currentTracks,
    init,
    fetchTracksAtOffset,
    loadRange,
    reset,
  };
});
