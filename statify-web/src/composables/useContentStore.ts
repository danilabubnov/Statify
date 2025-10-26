import { computed, type ComputedRef } from 'vue';
import { storeToRefs } from 'pinia';
import { trackStore } from '../store/tracks/tracks';
import { albumStore } from '../store/albums/albums';
import { artistStore } from '../store/artists/artists';
import { DEFAULT_PAGE } from '../constants/pagination';
import type { TimeRange } from '../types/filters';

type ContentType = 'tracks' | 'albums' | 'artists';

interface ContentStoreReturn {
  store: ReturnType<typeof trackStore> | ReturnType<typeof albumStore> | ReturnType<typeof artistStore>;
  items: ComputedRef<any[]>;
  currentSize: ComputedRef<number>;
  resetAndFetch: (timeRange?: TimeRange) => Promise<void>;
  rollback: (oldRange?: TimeRange) => void;
}

export const useContentStore = (type: ContentType): ContentStoreReturn => {
  const tracks = trackStore();
  const albums = albumStore();
  const artists = artistStore();

  const { currentTracks } = storeToRefs(tracks);
  const { currentAlbums } = storeToRefs(albums);
  const { currentArtists } = storeToRefs(artists);

  const storeMap = {
    tracks,
    albums,
    artists,
  };

  const itemsMap = {
    tracks: currentTracks,
    albums: currentAlbums,
    artists: currentArtists,
  };

  const store = storeMap[type];
  const items = computed(() => itemsMap[type].value);
  const currentSize = computed(() => store.currentSize);

  const resetAndFetch = async (timeRange?: TimeRange) => {
    if (timeRange && 'timeRangeFilter' in store) {
      store.timeRangeFilter = timeRange;
    }

    store.currentPage = DEFAULT_PAGE;
    store.isInitialLoad = true;

    if (type === 'tracks') {
      await tracks.fetchTopTracks({ page: DEFAULT_PAGE });
    } else if (type === 'albums') {
      await albums.fetchTopAlbums({ page: DEFAULT_PAGE });
    } else {
      await artists.fetchTopArtists({ page: DEFAULT_PAGE });
    }
  };

  const rollback = (oldRange?: TimeRange) => {
    if (oldRange && 'timeRangeFilter' in store) {
      store.timeRangeFilter = oldRange;
    }
  };

  return {
    store,
    items,
    currentSize,
    resetAndFetch,
    rollback
  };
};
