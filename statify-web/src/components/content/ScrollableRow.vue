<template>
  <div class="relative group/row">
    <div
      class="pointer-events-none absolute left-0 top-0 h-full w-12 bg-gradient-to-r from-base-200 to-transparent transition-opacity duration-300 opacity-0 z-10"
      :class="{ 'opacity-100': canScrollLeft }"
    />

    <ArrowIconButton
      direction="left"
      class="absolute left-2 top-1/2 -translate-y-1/2 bg-base-100 opacity-0 group-hover/row:opacity-100 transition z-20"
      :class="{ 'pointer-events-auto': canScrollLeft, 'pointer-events-none': !canScrollLeft }"
      v-show="canScrollLeft"
      @click="scrollByX(-scrollStep)"
    />

    <div
      ref="scrollContainer"
      class="flex overflow-x-auto overflow-y-hidden scroll-smooth scrollbar-hide bg-base-200 snap-x snap-mandatory scroll-ps-2"
      @scroll="update"
    >
      <div
        v-for="(item, i) in items"
        :key="itemKey ? itemKey(item, i) : defaultKey(item, i)"
        class="snap-start shrink-0"
      >
        <slot :item="item" :index="i" />
      </div>
    </div>

    <ArrowIconButton
      direction="right"
      class="absolute right-2 top-1/2 -translate-y-1/2 bg-base-100 opacity-0 group-hover/row:opacity-100 transition z-20"
      :class="{ 'pointer-events-auto': canScrollRight, 'pointer-events-none': !canScrollRight }"
      v-show="canScrollRight"
      @click="scrollByX(scrollStep)"
    />

    <div
      class="pointer-events-none absolute right-0 top-0 h-full w-12 bg-gradient-to-l from-base-200 to-transparent transition-opacity duration-300 opacity-0 z-10"
      :class="{ 'opacity-100': canScrollRight }"
    />
  </div>
</template>

<script setup lang="ts" generic="T">
  import { ref, onMounted, onBeforeUnmount, computed } from 'vue';
  import ArrowIconButton from '../button/ArrowIconButton.vue';

  defineProps<{
    items: T[];
    itemKey?: (item: T, index: number) => string;
  }>();

  defineSlots<{ default(props: { item: T; index: number }): any }>();

  const defaultKey = (item: unknown, i: number) => (item as any)?.id ?? i;

  const scrollContainer = ref<HTMLDivElement | null>(null);
  const canScrollLeft = ref(false);
  const canScrollRight = ref(false);
  const scrollStep = computed(() => {
    const scrollContainerValue = scrollContainer.value;

    if (!scrollContainerValue) return 0;

    const firstItem = scrollContainerValue.children[0];

    if (!firstItem) return 0;

    const itemWidth = firstItem.clientWidth;
    const visibleCount = Math.floor(scrollContainerValue.clientWidth / itemWidth);

    return itemWidth * Math.floor(visibleCount / 2);
  });

  function update() {
    const el = scrollContainer.value;

    if (!el) return;

    const maxLeft = el.scrollWidth - el.clientWidth;
    const x = el.scrollLeft;

    canScrollLeft.value = x > 0;
    canScrollRight.value = x < maxLeft;
  }

  function scrollByX(offset: number) {
    const el = scrollContainer.value;

    if (!el) return;

    el.scrollBy({ left: offset, behavior: 'smooth' });
  }

  let ro: ResizeObserver | null = null;

  onMounted(() => {
    update();
    if ('ResizeObserver' in window && scrollContainer.value) {
      ro = new ResizeObserver(update);
      ro.observe(scrollContainer.value);
    }
    window.addEventListener('resize', update);
  });

  onBeforeUnmount(() => {
    window.removeEventListener('resize', update);
    ro?.disconnect();
  });
</script>
