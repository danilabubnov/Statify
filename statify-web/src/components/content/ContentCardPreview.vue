<template>
  <button
    type="button"
    class="group/card block bg-base-200 hover:bg-surface hover:rounded-sm transition-colors duration-200"
    :class="props.fluid ? 'max-w-none h-full' : 'w-48 max-w-56'"
  >
    <div class="flex flex-col gap-2 p-2">
      <div class="overflow-hidden shrink-0" :class="[props.round ? 'rounded-full' : 'rounded-lg']">
        <div class="aspect-square">
          <img
            :src="imageUrl"
            :alt="item.name"
            class="w-full h-full object-cover object-center transition duration-200 scale-100 group-hover/card:scale-[1.02]"
            loading="lazy"
            decoding="async"
          />
        </div>
      </div>

      <div class="flex flex-col mb-2">
        <span class="text-left text-base font-medium leading-snug line-clamp-2 hover:underline" :title="item.name">
          {{ item.name }}
        </span>
        <span v-if="title.length" class="text-left text-sm font-normal text-base-content/65 line-clamp-2">
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
