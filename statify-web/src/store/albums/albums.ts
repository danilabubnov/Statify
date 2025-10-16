import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { apolloClient } from '../../api/apollo';
import { TOP_ALBUMS_QUERY } from '../../graphql/queries';
import type {
  AlbumPreview,
  AlbumType,
  OffsetPageInfo,
  TopAlbumsByPopularityQuery,
  TopAlbumsByPopularityQueryVariables,
} from '../../graphql/generated/graphql';
import type { TimeRange } from '../../types/filters';
import { DEFAULT_SIZE } from '../../constants/pagination';
import { useDateRangeFilter } from '../../composables/useDateRangeFilter';
import { useAbortController } from '../../composables/useAbortController';
import { usePaginationActions } from '../../composables/usePaginationActions';

export const albumStore = defineStore('albums', () => {
  const loading = ref(false);
  const error = ref<string | null>(null);

  const albums = ref<AlbumPreview[]>([]);
  const statePageInfo = ref<OffsetPageInfo | null>(null);
  const totalAlbums = ref<number | null>(null);

  const currentPage = ref(0);
  const currentSize = ref<number>(DEFAULT_SIZE.ALBUMS);
  const timeRangeFilter = ref<TimeRange>('all-time');
  const albumType = ref<AlbumType>('ALBUM');

  const { dateRange } = useDateRangeFilter(timeRangeFilter);
  const { createController, isCurrentController, clearController } = useAbortController();

  const currentAlbums = computed(() => albums.value);
  const pageInfo = computed(() => statePageInfo.value);
  const pageCount = computed(() => {
    if (!totalAlbums.value || !currentSize.value) return 0;
    return Math.ceil(totalAlbums.value / currentSize.value);
  });
  const activeTimeRangeFilter = computed(() => timeRangeFilter.value);

  const fetchTopAlbums = async (payload: { page?: number; size?: number; albumType?: AlbumType; withTotal?: boolean }) => {
    const controller = createController();
    const signal = controller.signal;

    loading.value = true;
    error.value = null;

    const page = payload.page ?? currentPage.value;
    const size = payload.size ?? currentSize.value;
    const _albumType = payload.albumType ?? albumType.value;
    const withTotal = payload.withTotal ?? true;
    const { from, to } = dateRange.value;

    try {
      const variables: TopAlbumsByPopularityQueryVariables = {
        page,
        size,
        from,
        to,
        albumType: _albumType,
        withTotal,
      };

      const { data } = await apolloClient.query<
        TopAlbumsByPopularityQuery,
        TopAlbumsByPopularityQueryVariables
      >({
        query: TOP_ALBUMS_QUERY,
        variables,
        fetchPolicy: 'cache-first',
        context: {
          fetchOptions: {
            signal,
          },
        },
      });

      if (!isCurrentController(controller)) {
        return;
      }

      const pageData = data?.topAlbumsByPopularity;
      const items = pageData?.items ?? [];
      const info: OffsetPageInfo =
        pageData?.pageInfo ??
        ({ page, size, hasNextPage: items.length >= size, hasPreviousPage: page > 0 } as OffsetPageInfo);

      albums.value = items;
      statePageInfo.value = info;

      if (withTotal) totalAlbums.value = pageData?.totalCount ?? null;

      currentSize.value = size;
      albumType.value = _albumType;

      if (info.hasNextPage) {
        await prefetchPage(page + 1);
      }
    } catch (err: any) {
      if (err.name === 'AbortError') {
        console.debug('Request was aborted');
        return;
      }

      if (!isCurrentController(controller)) {
        return;
      }

      console.error('Failed to fetch albums:', err);

      error.value = err?.message ?? 'Unknown error';

      throw err;
    } finally {
      if (isCurrentController(controller)) {
        loading.value = false;
        clearController(controller);
      }
    }
  };

  const prefetchData = async (page: number, size: number) => {
    if (page < 0 || page >= pageCount.value) return;

    const { from, to } = dateRange.value;

    try {
      await apolloClient.query<TopAlbumsByPopularityQuery, TopAlbumsByPopularityQueryVariables>({
        query: TOP_ALBUMS_QUERY,
        variables: {
          page,
          size,
          from,
          to,
          albumType: albumType.value,
          withTotal: false,
        },
        fetchPolicy: 'cache-first',
      });
    } catch (err) {
      console.debug('Prefetch failed:', err);
    }
  };

  const { nextPage, prevPage, followPage, prefetchPage } = usePaginationActions({
    currentPage,
    statePageInfo,
    currentSize,
    loading,
    fetchData: fetchTopAlbums,
    prefetchData,
    pageCount,
  });

  const init = async () => {
    await fetchTopAlbums({ withTotal: true });
  };

  return {
    loading,
    error,
    albums,
    statePageInfo,
    totalAlbums,
    currentPage,
    currentSize,
    timeRangeFilter,
    albumType,
    currentAlbums,
    pageInfo,
    pageCount,
    activeTimeRangeFilter,
    dateRange,
    init,
    fetchTopAlbums,
    nextPage,
    prevPage,
    followPage,
    prefetchPage,
  };
});
