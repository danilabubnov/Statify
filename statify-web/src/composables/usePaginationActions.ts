import type { Ref } from 'vue';
import type { OffsetPageInfo } from '../graphql/generated/graphql';

interface PaginationActionsConfig {
  currentPage: Ref<number>;
  statePageInfo: Ref<OffsetPageInfo | null>;
  currentSize: Ref<number>;
  loading: Ref<boolean>;
  fetchData: (payload: { page: number; size?: number }) => Promise<void>;
  prefetchData?: (page: number, size: number) => Promise<void>;
  pageCount?: Ref<number>;
}

export const usePaginationActions = (config: PaginationActionsConfig) => {
  const { currentPage, statePageInfo, currentSize, loading, fetchData, prefetchData, pageCount } = config;

  const hasNext = (): boolean => {
    return !!statePageInfo.value?.hasNextPage;
  };

  const hasPrev = (): boolean => {
    return !!statePageInfo.value?.hasPreviousPage;
  };

  const nextPage = async () => {
    if (!hasNext()) return;

    currentPage.value = currentPage.value + 1;
    loading.value = true;

    await fetchData({ page: currentPage.value });

    if (prefetchData && hasNext()) {
      await prefetchData(currentPage.value + 1, currentSize.value);
    }
  };

  const prevPage = async () => {
    if (!hasPrev()) return;

    currentPage.value = currentPage.value - 1;
    loading.value = true;

    await fetchData({ page: currentPage.value });

    if (prefetchData && hasPrev()) {
      await prefetchData(currentPage.value - 1, currentSize.value);
    }
  };

  const followPage = async (p: number) => {
    currentPage.value = p - 1;
    loading.value = true;

    await fetchData({ page: p - 1, size: currentSize.value });
  };

  const prefetchPage = async (page: number) => {
    if (!prefetchData || !pageCount) return;
    if (page < 0 || page >= pageCount.value) return;

    await prefetchData(page, currentSize.value);
  };

  return {
    hasNext,
    hasPrev,
    nextPage,
    prevPage,
    followPage,
    prefetchPage,
  };
};
