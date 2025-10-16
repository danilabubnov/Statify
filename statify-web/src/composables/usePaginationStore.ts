import { computed, type ComputedRef } from 'vue';
import { albumStore } from '../store/albums/albums';
import { trackStore } from '../store/tracks/tracks';
import { artistStore } from '../store/artists/artists';
import type { PageInfo, NavigationActions } from '../types/pagination';

type StoreType = 'albums' | 'tracks' | 'artists';

interface PaginationStoreReturn {
  pageInfo: ComputedRef<PageInfo>;
  navigate: NavigationActions;
}

export const usePaginationStore = (type: StoreType): PaginationStoreReturn => {
  const stores = {
    albums: albumStore(),
    tracks: trackStore(),
    artists: artistStore()
  };

  const store = stores[type];

  const pageInfo = computed<PageInfo>(() => {
    return {
      page: store.currentPage,
      pageCount: store.pageCount,
      hasPrev: store.pageInfo?.hasPreviousPage ?? false,
      hasNext: store.pageInfo?.hasNextPage ?? false
    };
  });

  const navigate: NavigationActions = {
    prev: store.prevPage,
    next: store.nextPage,
    follow: store.followPage
  };

  return { pageInfo, navigate };
};
