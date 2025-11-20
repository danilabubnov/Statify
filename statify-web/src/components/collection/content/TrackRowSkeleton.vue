<template>
  <div
    class="grid grid-cols-[1fr] md:grid-cols-[50px_1fr_80px] lg:grid-cols-[50px_1fr_1fr_120px_80px] rounded-md px-4 items-center"
    :style="{ height: `${DIMENSIONS.COVER_SIZE + 16}px` }"
  >
    <div class="hidden md:block">
      <div class="w-4 h-4 rounded animate-pulse bg-base-content/10"></div>
    </div>

    <div
      class="flex items-center gap-3 min-w-0"
      :style="{ maxWidth: `${DIMENSIONS.CONTENT_MAX_WIDTH_PERCENT}%` }"
    >
      <div
        class="rounded animate-pulse bg-base-content/10 flex-shrink-0"
        :style="{
          width: `${DIMENSIONS.COVER_SIZE}px`,
          height: `${DIMENSIONS.COVER_SIZE}px`
        }"
      ></div>
      <div class="flex flex-col min-w-0 flex-1">
        <div class="h-4 rounded animate-pulse bg-base-content/10" :style="{ width: SKELETON.NAME_WIDTH }"></div>
        <div class="flex items-center gap-2 mt-1">
          <div class="h-3 rounded animate-pulse bg-base-content/10" :style="{ width: SKELETON.ARTIST_WIDTH }"></div>
        </div>
      </div>
    </div>

    <div class="hidden lg:block" :style="{ paddingRight: `${DIMENSIONS.ALBUM_PADDING_RIGHT_PERCENT}%` }">
      <div class="h-3 rounded animate-pulse bg-base-content/10" :style="{ width: SKELETON.ALBUM_WIDTH }"></div>
    </div>

    <div class="hidden lg:block">
      <div class="h-3 rounded animate-pulse bg-base-content/10" :style="{ width: SKELETON.DATE_WIDTH }"></div>
    </div>

    <div class="hidden md:flex justify-end">
      <div class="h-3 rounded animate-pulse bg-base-content/10" :style="{ width: SKELETON.DURATION_WIDTH }"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { DIMENSIONS } from '../../../constants/track-collection';

  const SKELETON = {
    NAME_WIDTH: '60%',
    ARTIST_WIDTH: '40%',
    ALBUM_WIDTH: '128px',
    DATE_WIDTH: '96px',
    DURATION_WIDTH: '40px',
  } as const;
</script>
