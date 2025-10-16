import { computed, type ComputedRef, type Ref } from 'vue';
import { PAGINATION } from '../constants/pagination';
import type { PageInfo, PageItem } from '../types/pagination';

const { ELLIPSIS, MIN_PAGES_FOR_ELLIPSIS } = PAGINATION;

const range = (start: number, end: number): number[] => {
  const result: number[] = [];
  for (let i = start; i <= end; i++) result.push(i);
  return result;
};

const pushRange = (arr: PageItem[], start: number, end: number): void => {
  arr.push(...range(start, end));
};

const buildPageRange = (start: number, end: number, pageCount: number): PageItem[] => {
  const result: PageItem[] = [];

  if (start > 1) {
    result.push(1);
    if (start > 2) result.push(ELLIPSIS);
    else if (start === 2) result.push(2);
  }

  pushRange(result, Math.max(start, 1), Math.min(end, pageCount));

  if (end < pageCount) {
    if (end < pageCount - 1) result.push(ELLIPSIS);
    else if (end === pageCount - 1) result.push(pageCount - 1);
    result.push(pageCount);
  }

  return result;
};

const buildMobilePages = (current: number, pageCount: number): PageItem[] => {
  const start = Math.max(1, current - 1);
  const end = Math.min(pageCount, current + 1);
  const result: PageItem[] = range(start, end);
  if (end < pageCount) result.push(ELLIPSIS, pageCount);
  return result;
};

const buildDesktopPages = (current: number, pageCount: number): PageItem[] => {
  if (current <= 2) return buildPageRange(1, Math.min(3, pageCount - 1), pageCount);
  if (current === 3) return buildPageRange(1, Math.min(4, pageCount - 1), pageCount);

  if (current >= pageCount) return buildPageRange(Math.max(1, pageCount - 2), pageCount, pageCount);
  if (current === pageCount - 1) return buildPageRange(Math.max(1, pageCount - 2), pageCount, pageCount);
  if (current === pageCount - 2) return buildPageRange(Math.max(1, pageCount - 3), pageCount, pageCount);

  return buildPageRange(current - 1, current + 1, pageCount);
};

const buildXlPages = (current: number, pageCount: number): PageItem[] => {
  if (current <= 2) return buildPageRange(1, Math.min(4, pageCount - 1), pageCount);
  if (current === 3) return buildPageRange(1, Math.min(5, pageCount - 1), pageCount);
  if (current === 4) return buildPageRange(1, Math.min(6, pageCount - 1), pageCount);

  if (current >= pageCount) return buildPageRange(Math.max(1, pageCount - 3), pageCount, pageCount);
  if (current === pageCount - 1) return buildPageRange(Math.max(1, pageCount - 3), pageCount, pageCount);
  if (current === pageCount - 2) return buildPageRange(Math.max(1, pageCount - 4), pageCount, pageCount);
  if (current === pageCount - 3) return buildPageRange(Math.max(1, pageCount - 5), pageCount, pageCount);

  return buildPageRange(current - 2, current + 2, pageCount);
};

interface UsePaginationPagesOptions {
  pageInfo: ComputedRef<PageInfo>;
  isXl: Ref<boolean>;
  isSm: Ref<boolean>;
  isTouch: Ref<boolean>;
}

export const usePaginationPages = ({ pageInfo, isXl, isSm, isTouch }: UsePaginationPagesOptions) => {
  const visiblePages = computed<PageItem[]>(() => {
    const { page, pageCount } = pageInfo.value;

    if (page == null) return [];
    if (!pageCount || pageCount === 0) return [1];
    if (pageCount <= MIN_PAGES_FOR_ELLIPSIS) return range(1, pageCount);

    const currentPage = typeof page === 'number' ? page : Number(page);
    const current = Math.max(1, Math.min(currentPage + 1, pageCount));
    const isMobile = !isSm.value || isTouch.value;

    if (isMobile) return buildMobilePages(current, pageCount);
    if (isXl.value) return buildXlPages(current, pageCount);
    return buildDesktopPages(current, pageCount);
  });

  const isActive = (p: PageItem) => {
    const currentPage = typeof pageInfo.value.page === 'number' ? pageInfo.value.page : Number(pageInfo.value.page);
    return typeof p === 'number' && p - 1 === currentPage;
  };

  return { visiblePages, isActive };
};
