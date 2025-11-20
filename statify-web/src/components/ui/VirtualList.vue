<template>
  <div class="virtual-list-wrapper" :style="{ height: totalHeight + 'px' }">
    <div
      v-for="{ item, index, isLoaded } in visibleItems"
      :key="keyField && item ? item[keyField] : index"
      class="virtual-list-item"
      :style="{ transform: `translateY(${index * itemHeight}px)` }"
    >
      <slot v-if="isLoaded" :item="item" :index="index"></slot>
      <slot v-else name="skeleton"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { VIRTUAL_LIST_DEFAULTS } from '../../constants/virtual-list';

interface Props {
  items: any[];
  itemHeight: number;
  buffer?: number;
  keyField?: string;
  scrollTop?: number;
  containerHeight?: number;
  totalCount?: number;
}

const props = withDefaults(defineProps<Props>(), {
  buffer: VIRTUAL_LIST_DEFAULTS.BUFFER,
  keyField: VIRTUAL_LIST_DEFAULTS.KEY_FIELD,
  scrollTop: VIRTUAL_LIST_DEFAULTS.SCROLL_TOP,
  containerHeight: VIRTUAL_LIST_DEFAULTS.CONTAINER_HEIGHT,
  totalCount: VIRTUAL_LIST_DEFAULTS.TOTAL_COUNT,
});

const totalHeight = computed(() => {
  const count = props.totalCount > 0 ? props.totalCount : props.items.length;
  return count * props.itemHeight;
});

const startIndex = computed(() => {
  const index = Math.floor(props.scrollTop / props.itemHeight) - props.buffer;
  return Math.max(0, index);
});

const endIndex = computed(() => {
  const count = props.totalCount > 0 ? props.totalCount : props.items.length;
  const index = Math.ceil((props.scrollTop + props.containerHeight) / props.itemHeight) + props.buffer;
  return Math.min(count, index);
});

const visibleItems = computed(() => {
  const result = [];

  for (let i = startIndex.value; i < endIndex.value; i++) {
    const item = props.items[i];
    const isLoaded = item !== undefined;

    result.push({
      item: item || null,
      index: i,
      isLoaded
    });
  }

  return result;
});
</script>

<style scoped>
.virtual-list-wrapper {
  position: relative;
  width: 100%;
}

.virtual-list-item {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  will-change: transform;
}
</style>
