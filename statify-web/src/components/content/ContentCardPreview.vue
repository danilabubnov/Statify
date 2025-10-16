<template>
  <button
    type="button"
    class="group/card uxcard hover:uxcardhover focus-visible:uxfocus"
    :class="props.fluid ? 'w-full' : 'w-48 max-w-56'"
  >
    <div class="uxcardbody">
      <div class="uxcardmedia" :class="[props.round ? 'rounded-full' : 'rounded-lg']">
        <div class="relative w-full bg-base-300" style="padding-bottom: 100%;">
          <img
            :src="imageUrl"
            :alt="item.name"
            class="uxcardimg group-hover/card:uximgzoom absolute inset-0"
            decoding="async"
            loading="lazy"
            width="300"
            height="300"
          />
        </div>
      </div>

      <div class="flex flex-col h-[4.5rem]">
        <span
          class="line-clamp-2 text-left text-[length:var(--fs-card-title)] leading-snug font-normal hover:underline xl:font-medium"
          :title="item.name"
        >
          {{ item.name }}
        </span>
        <span
          v-if="title.length"
          class="text-base-content/70 line-clamp-2 text-left text-[length:var(--fs-card-meta)] font-normal tracking-[0.01em]"
        >
          <template v-for="(artist, i) in title" :key="artist">
            <span class="hover:underline">{{ artist.name }}</span>
            <span v-if="i < title.length - 1">, </span>
          </template>
        </span>
      </div>
    </div>
  </button>
</template>

<script setup lang="ts" generic="T extends Content">
  import { computed } from 'vue';
  import type { Content } from '../../graphql/types';

  const props = defineProps<{
    item: T;
    round?: boolean;
    fluid?: boolean;
  }>();

  const imageUrl = computed(() => {
    const imgs = 'covers' in props.item ? props.item.covers : props.item.images;
    return imgs[1]?.imageUrl ?? imgs[0]?.imageUrl ?? '';
  });

  const title = computed(() => {
    return 'artists' in props.item ? props.item.artists : [];
  });
</script>
