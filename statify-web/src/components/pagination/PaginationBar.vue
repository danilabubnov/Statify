<template>
  <div ref="container" class="relative flex w-full flex-row justify-center gap-0.5 rounded-xl px-1 py-1 sm:w-min sm:gap-1">
    <div
      class="pointer-events-none absolute top-1 left-1 h-[35px] rounded-xl sm:h-[43px]"
      :style="indicatorStyle"
    ></div>

    <PaginationButton text="&laquo;" :disabled="pageInfo.page === 0" @click="() => navigate.follow(1)" />

    <template v-for="(p, i) in visiblePages" :key="typeof p === 'number' ? `n-${p}` : `s-${i}`">
      <input
        v-if="p === PAGINATION.ELLIPSIS && state.activeIndex === i"
        v-model="state.value"
        :ref="(el) => setInputRef(i, el as HTMLInputElement)"
        type="number"
        :class="[
          'upaginationinput',
          state.isInvalid ? 'text-red-500' : 'utextwhite',
          state.isShaking ? 'animate-shake' : ''
        ]"
        :min="1"
        :max="pageInfo.pageCount"
        @keydown="handlePageInput"
        @input="handleInputChange"
        @blur="handleInputBlur"
      />
      <PaginationButton
        v-else
        :ref="(el: any) => setBtnRef(p, el)"
        :text="p"
        :active="isActive(p)"
        @follow="navigate.follow"
        @click="p === PAGINATION.ELLIPSIS ? handleEllipsisClick(i) : undefined"
      />
    </template>

    <PaginationButton text="&raquo;" :disabled="pageInfo.page === pageInfo.pageCount - 1" @click="() => navigate.follow(pageInfo.pageCount)" />
  </div>
</template>

<script setup lang="ts">
  import { reactive, ref, watch } from 'vue';
  import { useRoute } from 'vue-router';
  import PaginationButton from '../button/PaginationButton.vue';
  import { breakpointsTailwind, useBreakpoints, useMediaQuery } from '@vueuse/core';
  import { usePaginationStore } from '../../composables/usePaginationStore';
  import { usePaginationPages } from '../../composables/usePaginationPages';
  import { usePaginationIndicator } from '../../composables/usePaginationIndicator';
  import { usePaginationInput } from '../../composables/usePaginationInput';
  import { PAGINATION } from '../../constants/pagination';

  const route = useRoute();
  const type = route.meta.type as 'albums' | 'tracks' | 'artists';

  const container = ref<HTMLDivElement | null>(null);
  const btnMap = reactive(new Map<number, HTMLButtonElement>());
  const inputRefs = ref<Record<number, HTMLInputElement>>({});

  const breakpoints = useBreakpoints(breakpointsTailwind);
  const isXl = breakpoints.greaterOrEqual('xl');
  const isSm = breakpoints.greaterOrEqual('sm');
  const isTouch = useMediaQuery('(pointer: coarse)');

  const { pageInfo, navigate } = usePaginationStore(type);
  const { visiblePages, isActive } = usePaginationPages({ pageInfo, isXl, isSm, isTouch });
  const { indicatorStyle, syncIndicator } = usePaginationIndicator(container, btnMap, isActive);
  const { state, handleEllipsisClick, handleInputChange, handlePageInput, handleInputBlur } = usePaginationInput(
    pageInfo,
    navigate,
    inputRefs
  );

  const setBtnRef = (p: number | string, comp: { el: HTMLButtonElement } | null) => {
    if (typeof p === 'number') {
      if (comp?.el) btnMap.set(p, comp.el);
      else btnMap.delete(p);
    }
  };

  const setInputRef = (index: number, el: HTMLInputElement | null) => {
    if (el) inputRefs.value[index] = el;
    else delete inputRefs.value[index];
  };

  watch(() => [...btnMap.keys()], syncIndicator);
  watch(() => visiblePages, syncIndicator, { deep: true });
  watch(() => pageInfo.value, syncIndicator);
</script>

<style scoped>
  input::-webkit-outer-spin-button,
  input::-webkit-inner-spin-button {
    -webkit-appearance: none;
    margin: 0;
  }

  input[type='number'] {
    -moz-appearance: textfield;
    appearance: textfield;
  }

  @keyframes shake {
    0%, 100% { transform: translateX(0); }
    10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
    20%, 40%, 60%, 80% { transform: translateX(5px); }
  }

  .animate-shake {
    animation: shake 0.5s ease-in-out;
  }
</style>
