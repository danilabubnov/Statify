import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { apolloClient } from '../../api/apollo';
import { TOP_TRACKS_QUERY } from '../../graphql/queries';
import type {
  TrackPreview,
  OffsetPageInfo,
  TopTracksByPopularityQuery,
  TopTracksByPopularityQueryVariables,
} from '../../graphql/generated/graphql';
import type { TimeRange } from '../../types/filters';
import { DEFAULT_SIZE } from '../../constants/pagination';
import { useDateRangeFilter } from '../../composables/useDateRangeFilter';
import { useAbortController } from '../../composables/useAbortController';
import { usePaginationActions } from '../../composables/usePaginationActions';

export const trackStore = defineStore('tracks', () => {
  const loading = ref(false);
  const error = ref<string | null>(null);

  const tracks = ref<TrackPreview[]>([]);
  const statePageInfo = ref<OffsetPageInfo | null>(null);
  const totalTracks = ref<number | null>(null);

  const currentPage = ref(0);
  const currentSize = ref<number>(DEFAULT_SIZE.TRACKS);
  const timeRangeFilter = ref<TimeRange>('all-time');

  const { dateRange } = useDateRangeFilter(timeRangeFilter);
  const { createController, isCurrentController, clearController } = useAbortController();

  const currentTracks = computed(() => tracks.value);
  const pageInfo = computed(() => statePageInfo.value);
  const pageCount = computed(() => {
    if (!totalTracks.value || !currentSize.value) return 0;
    return Math.ceil(totalTracks.value / currentSize.value);
  });
  const activeTimeRangeFilter = computed(() => timeRangeFilter.value);

  const fetchTopTracks = async (payload: { page?: number; size?: number; withTotal?: boolean }) => {
    const controller = createController();
    const signal = controller.signal;

    loading.value = true;
    error.value = null;

    const page = payload.page ?? currentPage.value;
    const size = payload.size ?? currentSize.value;
    const withTotal = payload.withTotal ?? true;
    const { from, to } = dateRange.value;

    try {
      const { data } = await apolloClient.query<
        TopTracksByPopularityQuery,
        TopTracksByPopularityQueryVariables
      >({
        query: TOP_TRACKS_QUERY,
        variables: { page, size, from, to, withTotal },
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

      const pageData = data?.topTracksByPopularity;
      const items = pageData?.items ?? [];
      const info: OffsetPageInfo =
        pageData?.pageInfo ??
        ({ page, size, hasNextPage: items.length >= size, hasPreviousPage: page > 0 } as OffsetPageInfo);

      tracks.value = items;
      statePageInfo.value = info;

      if (withTotal) totalTracks.value = pageData?.totalCount ?? null;

      currentSize.value = size;

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

      console.error('Failed to fetch tracks:', err);

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
      await apolloClient.query<TopTracksByPopularityQuery, TopTracksByPopularityQueryVariables>({
        query: TOP_TRACKS_QUERY,
        variables: { page, size, from, to, withTotal: false },
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
    fetchData: fetchTopTracks,
    prefetchData,
    pageCount,
  });

  const init = async () => {
    await fetchTopTracks({ withTotal: true });
  };

  return {
    loading,
    error,
    tracks,
    statePageInfo,
    totalTracks,
    currentPage,
    currentSize,
    timeRangeFilter,
    currentTracks,
    pageInfo,
    pageCount,
    activeTimeRangeFilter,
    dateRange,
    init,
    fetchTopTracks,
    nextPage,
    prevPage,
    followPage,
    prefetchPage,
  };
});
