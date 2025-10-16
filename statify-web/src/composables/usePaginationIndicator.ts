import { computed, nextTick, onBeforeUnmount, onMounted, reactive, type Ref } from 'vue';
import { PAGINATION } from '../constants/pagination';
import type { IndicatorPosition, PageItem } from '../types/pagination';

const { CONTAINER_OFFSET, TRANSITION_TIMING, TRANSITION_DURATION } = PAGINATION;

export const usePaginationIndicator = (
  container: Ref<HTMLDivElement | null>,
  btnMap: Map<number, HTMLButtonElement>,
  isActive: (p: PageItem) => boolean
) => {
  const indicator = reactive<IndicatorPosition>({
    x: 0,
    w: 0,
  });

  const indicatorStyle = computed(() => ({
    transform: `translateX(${indicator.x}px)`,
    width: `${indicator.w}px`,
    transition: `transform ${TRANSITION_DURATION}ms ${TRANSITION_TIMING}, width ${TRANSITION_DURATION}ms ${TRANSITION_TIMING}`,
    background: 'oklch(1 0 0)',
    zIndex: 0,
  }));

  const updateIndicator = () => {
    const activeEntry = [...btnMap.entries()].find(([page]) => isActive(page));
    if (!activeEntry || !container.value) return;

    const [, el] = activeEntry;
    const containerRect = container.value.getBoundingClientRect();
    const btnRect = el.getBoundingClientRect();

    indicator.x = btnRect.left - containerRect.left - CONTAINER_OFFSET;
    indicator.w = btnRect.width;
  };

  const syncIndicator = async () => {
    await nextTick();
    updateIndicator();
  };

  onMounted(() => {
    syncIndicator();
    window.addEventListener('resize', updateIndicator, { passive: true });
  });

  onBeforeUnmount(() => {
    window.removeEventListener('resize', updateIndicator);
  });

  return {
    indicatorStyle,
    syncIndicator
  };
};
