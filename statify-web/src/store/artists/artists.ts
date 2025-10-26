import { defineStore } from 'pinia';
import { ref, computed, shallowRef } from 'vue';
import type { Router } from 'vue-router';
import { apolloClient } from '../../api/apollo';
import { TOP_ARTISTS_QUERY } from '../../graphql/queries';
import type {
  ArtistPreview,
  OffsetPageInfo,
  TopArtistsByPopularityQuery,
  TopArtistsByPopularityQueryVariables,
} from '../../graphql/generated/graphql';
import { DEFAULT_SIZE } from '../../constants/pagination';
import { useAbortController } from '../../composables/useAbortController';

export const artistStore = defineStore('artists', () => {
  const loading = ref(false);
  const error = ref<string | null>(null);

  const topArtists = ref<Record<number, ArtistPreview[]>>({});
  const pagesInfo = ref<Record<number, OffsetPageInfo>>({});
  const totalCount = ref<number | null>(null);

  const currentPage = ref(0);
  const currentSize = ref<number>(DEFAULT_SIZE.ARTISTS);
  const isInitialLoad = ref(true);

  const router = shallowRef<Router | undefined>(undefined);

  const { createController, isCurrentController, clearController } = useAbortController();

  const updateURLQuery = (page: number) => {
    if (!router.value) return;

    if (isInitialLoad.value && page === 0) {
      return;
    }

    const currentQuery = router.value.currentRoute.value.query;
    const query = page === 0
      ? Object.fromEntries(Object.entries(currentQuery).filter(([key]) => key !== 'page'))
      : { ...currentQuery, page: String(page + 1) };

    router.value.replace({ query });

    if (isInitialLoad.value) {
      isInitialLoad.value = false;
    }
  };

  const currentArtists = computed(() => topArtists.value[currentPage.value] ?? []);
  const pageInfo = computed(() => pagesInfo.value[currentPage.value] ?? null);
  const page = computed(() => pageInfo.value?.page ?? 0);
  const size = computed(() => pageInfo.value?.size ?? DEFAULT_SIZE.ARTISTS);
  const hasNext = computed(() => !!pageInfo.value?.hasNextPage);
  const hasPrev = computed(() => !!pageInfo.value?.hasPreviousPage);
  const activeTimeRangeFilter = computed(() => 'all-time' as const);
  const pageCount = computed(() => {
    if (!totalCount.value || !currentSize.value) return 0;
    return Math.ceil(totalCount.value / currentSize.value);
  });

  const fetchTopArtists = async (payload: { page: number; size?: number; withTotal?: boolean }) => {
    const controller = createController();
    const signal = controller.signal;

    loading.value = true;
    error.value = null;

    const _page = payload.page;
    const _size = payload.size ?? size.value ?? DEFAULT_SIZE.ARTISTS;
    const withTotal = payload.withTotal ?? false;

    try {
      const variables: TopArtistsByPopularityQueryVariables = {
        page: _page,
        size: _size,
        withTotal,
      };

      const { data } = await apolloClient.query<TopArtistsByPopularityQuery, TopArtistsByPopularityQueryVariables>({
        query: TOP_ARTISTS_QUERY,
        variables,
        context: {
          fetchOptions: {
            signal,
          },
        },
      });

      if (!isCurrentController(controller)) {
        return;
      }

      const pageData = data?.topArtistsByPopularity;
      const items = pageData?.items ?? [];

      topArtists.value[_page] = items;
      pagesInfo.value[_page] =
        pageData?.pageInfo ??
        ({
          page: _page,
          size: _size,
          hasNextPage: items.length >= _size,
          hasPreviousPage: _page > 0,
        } as OffsetPageInfo);

      if (withTotal) totalCount.value = pageData?.totalCount ?? null;
    } catch (err: any) {
      if (err.name === 'AbortError') {
        console.debug('Request was aborted');
        return;
      }

      if (!isCurrentController(controller)) {
        return;
      }

      console.error('Failed to fetch artists:', err);
      error.value = err.message || 'Unknown error';
    } finally {
      if (isCurrentController(controller)) {
        loading.value = false;
        clearController(controller);
      }
    }
  };

  const nextPage = async () => {
    if (!hasNext.value) return;

    currentPage.value = page.value + 1;
    loading.value = true;

    updateURLQuery(currentPage.value);

    await fetchTopArtists({ page: currentPage.value, size: size.value });
  };

  const prevPage = async () => {
    if (!hasPrev.value) return;

    currentPage.value = page.value - 1;
    loading.value = true;

    updateURLQuery(currentPage.value);

    await fetchTopArtists({ page: currentPage.value, size: size.value });
  };

  const followPage = async (p: number) => {
    currentPage.value = p - 1;
    loading.value = true;

    updateURLQuery(currentPage.value);

    await fetchTopArtists({ page: p - 1, size: size.value });
  };

  const setRouter = (r: Router) => {
    router.value = r;
  };

  const init = async () => {
    await fetchTopArtists({ page: 0, withTotal: true });
  };

  return {
    loading,
    error,
    topArtists,
    pagesInfo,
    totalCount,
    currentPage,
    currentSize,
    isInitialLoad,
    currentArtists,
    pageInfo,
    page,
    size,
    hasNext,
    hasPrev,
    activeTimeRangeFilter,
    pageCount,
    init,
    fetchTopArtists,
    nextPage,
    prevPage,
    followPage,
    setRouter,
  };
});
