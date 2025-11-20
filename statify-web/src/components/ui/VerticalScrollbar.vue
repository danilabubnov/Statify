<template>
  <div
    class="custom-scrollbar-wrapper"
    :class="wrapperClass"
  >
    <div
      class="scrollable-content"
      ref="scrollContainer"
      @scroll="handleScroll"
      :style="{ maxHeight: maxHeight }"
    >
      <slot></slot>
    </div>

    <div class="custom-scrollbar" v-if="hasScroll" :class="{ 'force-visible': props.forceShow, 'is-dragging': isDragging }">
      <div
        class="custom-scrollbar-thumb"
        :style="{ top: scrollbarTop + 'px' }"
        @mousedown="startDrag"
      ></div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { onMounted, onUnmounted, ref, computed, nextTick } from 'vue';
  import { SCROLLBAR_DEFAULTS } from '../../constants/scrollbar';

  interface Props {
    maxHeight?: string;
    wrapperClass?: string;
    thumbColor?: string;
    thumbHoverColor?: string;
    thumbWidth?: number;
    thumbHeight?: number;
    forceShow?: boolean;
  }

  const props = withDefaults(defineProps<Props>(), {
    maxHeight: '100%',
    wrapperClass: '',
    thumbColor: SCROLLBAR_DEFAULTS.THUMB_COLOR,
    thumbHoverColor: SCROLLBAR_DEFAULTS.THUMB_HOVER_COLOR,
    thumbWidth: SCROLLBAR_DEFAULTS.THUMB_WIDTH,
    thumbHeight: SCROLLBAR_DEFAULTS.THUMB_HEIGHT,
    forceShow: false,
  });

  const emit = defineEmits<{
    scroll: [event: { scrollTop: number; scrollHeight: number; clientHeight: number }];
  }>();

  const scrollContainer = ref<HTMLElement | null>(null);
  const scrollbarTop = ref(0);
  const isDragging = ref(false);
  const dragOffset = ref(0);
  const scrollHeight = ref(0);
  const clientHeight = ref(0);

  const hasScroll = computed(() => {
    if (props.forceShow) return true;
    return scrollHeight.value > clientHeight.value;
  });

  const updateScrollDimensions = () => {
    if (!scrollContainer.value) return;
    scrollHeight.value = scrollContainer.value.scrollHeight;
    clientHeight.value = scrollContainer.value.clientHeight;
  };

  const handleScroll = () => {
    if (!scrollContainer.value || isDragging.value) return;

    updateScrollDimensions();

    const { scrollTop, scrollHeight: sh, clientHeight: ch } = scrollContainer.value;
    const scrollPercentage = scrollTop / (sh - ch);
    const maxScrollbarTop = ch - props.thumbHeight;
    scrollbarTop.value = scrollPercentage * maxScrollbarTop;

    emit('scroll', { scrollTop, scrollHeight: sh, clientHeight: ch });
  };

  const startDrag = (e: MouseEvent) => {
    if (!scrollContainer.value) return;

    isDragging.value = true;
    const thumbElement = e.target as HTMLElement;
    const thumbRect = thumbElement.getBoundingClientRect();
    dragOffset.value = e.clientY - thumbRect.top;

    document.addEventListener('mousemove', onDrag);
    document.addEventListener('mouseup', stopDrag);
    e.preventDefault();
  };

  const onDrag = (e: MouseEvent) => {
    if (!isDragging.value || !scrollContainer.value) return;

    const { scrollHeight, clientHeight } = scrollContainer.value;
    const containerRect = scrollContainer.value.getBoundingClientRect();

    const newThumbTop = e.clientY - dragOffset.value - containerRect.top;
    const maxScrollbarTop = clientHeight - props.thumbHeight;
    const clampedThumbTop = Math.max(0, Math.min(newThumbTop, maxScrollbarTop));

    scrollbarTop.value = clampedThumbTop;

    const scrollPercentage = clampedThumbTop / maxScrollbarTop;
    const newScrollTop = scrollPercentage * (scrollHeight - clientHeight);
    scrollContainer.value.scrollTop = newScrollTop;

    emit('scroll', { scrollTop: newScrollTop, scrollHeight, clientHeight });
  };

  const stopDrag = () => {
    isDragging.value = false;
    document.removeEventListener('mousemove', onDrag);
    document.removeEventListener('mouseup', stopDrag);
  };

  let resizeObserver: ResizeObserver | null = null;

  onMounted(() => {
    nextTick(() => {
      updateScrollDimensions();

      if (scrollContainer.value) {
        resizeObserver = new ResizeObserver(updateScrollDimensions);
        resizeObserver.observe(scrollContainer.value);
      }
    });
  });

  onUnmounted(() => {
    document.removeEventListener('mousemove', onDrag);
    document.removeEventListener('mouseup', stopDrag);

    if (resizeObserver) {
      resizeObserver.disconnect();
    }
  });
</script>

<style scoped>
  .custom-scrollbar-wrapper {
    position: relative;
  }

  .scrollable-content {
    overflow-y: scroll;
    overflow-x: hidden;
    scrollbar-width: none;
    -ms-overflow-style: none;
  }

  .scrollable-content::-webkit-scrollbar {
    display: none;
  }

  .custom-scrollbar {
    position: absolute;
    right: v-bind('SCROLLBAR_DEFAULTS.POSITION_RIGHT');
    top: 0;
    bottom: 0;
    width: v-bind('props.thumbWidth + "px"');
    pointer-events: none;
    opacity: 0;
    transition: opacity v-bind('SCROLLBAR_DEFAULTS.TRANSITION_DURATION') ease-in-out;
    z-index: v-bind('SCROLLBAR_DEFAULTS.Z_INDEX');
  }

  .custom-scrollbar.force-visible,
  .custom-scrollbar.is-dragging {
    opacity: 1;
  }

  .custom-scrollbar-wrapper:hover .custom-scrollbar {
    opacity: 1;
  }

  .custom-scrollbar-thumb {
    position: absolute;
    width: v-bind('props.thumbWidth + "px"');
    height: v-bind('props.thumbHeight + "px"');
    background-color: v-bind('props.thumbColor');
    cursor: pointer;
    pointer-events: auto;
    transition: background-color v-bind('SCROLLBAR_DEFAULTS.TRANSITION_DURATION');
  }

  .custom-scrollbar-thumb:hover {
    background-color: v-bind('props.thumbHoverColor');
  }
</style>
