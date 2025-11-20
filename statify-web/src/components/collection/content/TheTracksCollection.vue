<template>
  <div class="lg:mx-4 mb-4 lg:rounded-lg overflow-hidden bg-base-200 tracks-container">
    <component :is="isMobile ? 'div' : VerticalScrollbar" :wrapper-class="!isMobile ? 'h-full' : undefined" @scroll="handleScroll" :class="isMobile ? 'h-full overflow-y-auto scrollbar-hide' : ''">
      <header>
        <div class="flex flex-row items-end header-container">
          <img
            class="hidden lg:block rounded-md flex-shrink-0 header-icon"
            :src="ASSETS.FAVORITE_TRACKS_ICON"
            :alt="`${LABELS.FAVORITE_TRACKS} Icon`"
            decoding="async"
            loading="lazy"
          />
          <div class="flex flex-col justify-end min-w-0 pb-1">
            <span class="hidden lg:block header-label font-medium text-base-content">{{ LABELS.PLAYLIST_TYPE }}</span>
            <h1 class="font-bold text-base-content truncate header-title">
              {{ LABELS.FAVORITE_TRACKS }}
            </h1>
            <div class="flex flex-row items-center gap-1 header-info">
              <span v-if="displayName" class="hidden lg:inline font-bold text-base-content">{{ displayName }}</span>
              <span v-if="displayName" class="hidden lg:inline text-base-content/70">•</span>
              <span class="text-base-content/70">{{ tracks.length }} tracks</span>
            </div>
          </div>
        </div>
      </header>

      <div class="sticky top-0 z-10 transition-all duration-300" :style="{ backgroundColor: stickyHeaderBg, backdropFilter: stickyHeaderBlur }">
        <div class="grid grid-cols-[1fr] md:grid-cols-[50px_1fr_80px] lg:grid-cols-[50px_1fr_1fr_120px_80px] border-b border-base-content/10 py-2 px-8 text-base-content/70 font-normal text-sm">
          <div class="text-left hidden md:block">{{ LABELS.COLUMNS.NUMBER }}</div>
          <div class="text-left">{{ LABELS.COLUMNS.NAME }}</div>
          <div class="text-left hidden lg:block">{{ LABELS.COLUMNS.ALBUM }}</div>
          <div class="text-left hidden lg:block">{{ LABELS.COLUMNS.ADDED_AT }}</div>
          <div class="text-right hidden md:block">{{ LABELS.COLUMNS.DURATION }}</div>
        </div>
      </div>

      <div class="px-4 pt-4 pb-4">
        <VirtualList
          :items="tracks"
          :item-height="ITEM_HEIGHT"
          :buffer="VIRTUAL_LIST_BUFFER"
          :scroll-top="scrollState.scrollTop"
          :container-height="scrollState.containerHeight"
          :total-count="store.totalCount"
          key-field="id"
        >
          <template #default="{ item: track, index }">
            <div
              class="grid grid-cols-[1fr] md:grid-cols-[50px_1fr_80px] lg:grid-cols-[50px_1fr_1fr_120px_80px] group transition-[background-color] duration-200 rounded-md px-4 items-center"
              :class="`h-[${DIMENSIONS.COVER_SIZE + 16}px]`"
              :style="{ '--hover-bg': THEME_COLORS.HOVER_BG }"
            >
              <div class="text-base-content/70 text-sm hidden md:block">{{ index + 1 }}</div>
              <div
                class="flex items-center gap-3 min-w-0 overflow-hidden"
                :style="{ maxWidth: `${DIMENSIONS.CONTENT_MAX_WIDTH_PERCENT}%` }"
              >
                <img
                  v-if="track.covers[0]"
                  :src="track.covers[0].imageUrl"
                  :alt="track.name"
                  :width="DIMENSIONS.COVER_SIZE"
                  :height="DIMENSIONS.COVER_SIZE"
                  class="rounded flex-shrink-0"
                  :style="{ width: `${DIMENSIONS.COVER_SIZE}px`, height: `${DIMENSIONS.COVER_SIZE}px` }"
                  loading="lazy"
                  decoding="async"
                />
                <div class="flex flex-col min-w-0 flex-1">
                  <span class="text-base-content font-medium truncate">{{ track.name }}</span>
                  <div class="flex items-center gap-2 min-w-0">
                    <span
                      v-if="track.explicit"
                      class="flex-shrink-0 rounded-sm flex items-center justify-center text-[10px] font-semibold text-black"
                      :style="{
                        width: `${DIMENSIONS.EXPLICIT_BADGE_SIZE}px`,
                        height: `${DIMENSIONS.EXPLICIT_BADGE_SIZE}px`,
                        backgroundColor: THEME_COLORS.EXPLICIT_BADGE_BG
                      }"
                    >
                      E
                    </span>
                    <span class="text-base-content/70 text-sm truncate">
                      {{ formatArtistNames(track.artists) }}
                    </span>
                  </div>
                </div>
              </div>
              <div
                class="text-base-content/70 text-sm hidden lg:block truncate"
                :style="{ paddingRight: `${DIMENSIONS.ALBUM_PADDING_RIGHT_PERCENT}%` }"
              >
                {{ track.album.name }}
              </div>
              <div class="text-base-content/70 text-sm hidden lg:block">
                {{ formatRelativeDate(track.addedAt) }}
              </div>
              <div class="text-base-content/70 text-sm text-right hidden md:block">
                {{ formatDuration(track.durationMs) }}
              </div>
            </div>
          </template>

          <template #skeleton>
            <TrackRowSkeleton />
          </template>
        </VirtualList>
      </div>
    </component>
  </div>
</template>

<script setup lang="ts">
  import { onMounted, reactive, computed } from 'vue';
  import { storeToRefs } from 'pinia';
  import { useMediaQuery } from '@vueuse/core';
  import { favTrackStore } from '../../../store/tracks/favorite-tracks';
  import { authStore } from '../../../store/auth/auth';
  import VerticalScrollbar from '../../ui/VerticalScrollbar.vue';
  import VirtualList from '../../ui/VirtualList.vue';
  import TrackRowSkeleton from './TrackRowSkeleton.vue';
  import { SCROLL_CONFIG, FADE_CONFIG, HEADER_BG_COLOR, ASSETS, LABELS, DIMENSIONS, THEME_COLORS } from '../../../constants/track-collection';
  import { formatDuration, formatRelativeDate, formatArtistNames } from '../../../utils/formatters';
  import { calculateFadeProgress } from '../../../utils/gradient';

  const { ITEM_HEIGHT, PAGE_SIZE, DEBOUNCE_MS, DEFAULT_CONTAINER_HEIGHT, VIRTUAL_LIST_BUFFER } = SCROLL_CONFIG;

  const isTouch = useMediaQuery('(pointer: coarse)');
  const isMobile = computed(() => isTouch.value);

  const store = favTrackStore();
  const { currentTracks: tracks } = storeToRefs(store);

  const auth = authStore();
  const displayName = computed(() => auth.currentUser?.displayName);

  const scrollState = reactive<{
    scrollTop: number;
    containerHeight: number;
  }>({
    scrollTop: 0,
    containerHeight: DEFAULT_CONTAINER_HEIGHT,
  });

  const stickyHeaderBg = computed(() => {
    const { START, END, MAX_OPACITY } = FADE_CONFIG.HEADER;
    const progress = calculateFadeProgress(scrollState.scrollTop, START, END);
    const opacity = progress * MAX_OPACITY;
    const { l, c, h } = HEADER_BG_COLOR;

    return `oklch(${l} ${c} ${h} / ${opacity})`;
  });

  const stickyHeaderBlur = computed(() => {
    const { START, END, MAX_BLUR } = FADE_CONFIG.HEADER;
    const progress = calculateFadeProgress(scrollState.scrollTop, START, END);
    const blurAmount = progress * MAX_BLUR;

    return blurAmount > 0 ? `blur(${blurAmount}px)` : 'none';
  });

  let scrollDebounceTimer: number | null = null;

  const loadVisibleRange = async () => {
    const visibleStartIndex = Math.floor(scrollState.scrollTop / ITEM_HEIGHT);
    const visibleEndIndex = Math.ceil((scrollState.scrollTop + scrollState.containerHeight) / ITEM_HEIGHT);

    const bufferSize = PAGE_SIZE;
    const loadStartIndex = Math.max(0, visibleStartIndex - bufferSize);
    const loadEndIndex = Math.min(store.totalCount, visibleEndIndex + bufferSize);

    const startOffset = Math.floor(loadStartIndex / PAGE_SIZE) * PAGE_SIZE;
    const endOffset = Math.ceil(loadEndIndex / PAGE_SIZE) * PAGE_SIZE;

    await store.loadRange(startOffset, endOffset);
  };

  const handleScroll = (event: any) => {
    let scrollTop: number;
    let clientHeight: number;

    if (event.scrollTop !== undefined) {
      scrollTop = event.scrollTop;
      clientHeight = event.clientHeight;
    } else {
      const target = event.target as HTMLElement;
      scrollTop = target.scrollTop;
      clientHeight = target.clientHeight;
    }

    scrollState.scrollTop = scrollTop;
    scrollState.containerHeight = clientHeight;

    if (scrollDebounceTimer !== null) {
      clearTimeout(scrollDebounceTimer);
    }

    scrollDebounceTimer = window.setTimeout(() => {
      loadVisibleRange();
      scrollDebounceTimer = null;
    }, DEBOUNCE_MS);
  };

  onMounted(async () => {
    if (tracks.value.length === 0) {
      await store.init();
    }
  });
</script>

<style scoped>
  .tracks-container {
    height: 100vh;
    position: relative;
  }

  .group:hover {
    background-color: var(--hover-bg, oklch(1 0 0 / 0.298));
  }

  .header-container {
    padding: clamp(1rem, 1.5vw + 0.5rem, 2.5rem);
    gap: clamp(1rem, 1vw + 0.5rem, 1.75rem);
  }

  .header-icon {
    width: clamp(6rem, 12vw + 2rem, 14.5rem);
    height: clamp(6rem, 12vw + 2rem, 14.5rem);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5);
  }

  .header-label {
    font-size: clamp(0.6875rem, 0.2vw + 0.625rem, 0.875rem);
    margin-bottom: clamp(0.125rem, 0.15vw + 0.05rem, 0.25rem);
    letter-spacing: 0.05em;
    text-transform: capitalize;
  }

  .header-title {
    font-size: clamp(2rem, 3.5vw + 0.75rem, 4.5rem);
    line-height: 1.1;
    margin-bottom: clamp(0.5rem, 0.5vw + 0.25rem, 1rem);
    letter-spacing: -0.02em;
    margin-left: -0.04em;
  }

  .header-info {
    font-size: clamp(0.8125rem, 0.2vw + 0.75rem, 1rem);
  }
</style>
