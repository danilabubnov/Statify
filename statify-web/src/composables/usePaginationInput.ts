import { nextTick, reactive, type Ref } from 'vue';
import { PAGINATION } from '../constants/pagination';
import type { InputState, NavigationActions, PageInfo } from '../types/pagination';

const { SHAKE_DURATION } = PAGINATION;

export const usePaginationInput = (
  pageInfo: Ref<PageInfo>,
  navigate: NavigationActions,
  inputRefs: Ref<Record<number, HTMLInputElement>>
) => {
  const state = reactive<InputState>({
    value: '',
    isInvalid: false,
    isShaking: false,
    activeIndex: null
  });

  const resetState = () => {
    state.isInvalid = false;
    state.isShaking = false;
  };

  const validateAndShake = (value: string) => {
    const { pageCount } = pageInfo.value;
    if (!pageCount) return;

    if (value === '') {
      resetState();
      return;
    }

    const pageNum = parseInt(value, 10);

    if (!isNaN(pageNum)) {
      if (pageNum < 1 || pageNum > pageCount) {
        state.isInvalid = true;
        state.isShaking = true;

        state.value = pageNum < 1 ? '1' : pageCount.toString();

        setTimeout(() => {
          state.isShaking = false;
        }, SHAKE_DURATION);
      } else {
        resetState();
      }
    } else {
      resetState();
    }
  };

  const handleEllipsisClick = (index: number) => {
    state.activeIndex = index;
    state.value = '';
    resetState();

    nextTick(() => {
      inputRefs.value[index]?.focus();
    });
  };

  const handleInputChange = () => {
    validateAndShake(state.value);
  };

  const handlePageInput = (event: KeyboardEvent) => {
    if (event.key === 'Enter') {
      const { pageCount } = pageInfo.value;
      if (!pageCount) return;

      if (state.value === '') {
        state.activeIndex = null;
        return;
      }

      const pageNum = parseInt(state.value, 10);
      if (isNaN(pageNum) || pageNum < 1 || pageNum > pageCount) {
        state.activeIndex = null;
        return;
      }

      navigate.follow(pageNum);
      state.activeIndex = null;
    } else if (event.key === 'Escape') {
      state.activeIndex = null;
    }
  };

  const handleInputBlur = () => {
    state.activeIndex = null;
    resetState();
  };

  return {
    state,
    handleEllipsisClick,
    handleInputChange,
    handlePageInput,
    handleInputBlur
  };
};
