<template>
  <main class="flex-1 bg-base-200 rounded-xl m-2 p-4">
    <div>
      <span class="block text-2xl font-bold text-base-content p-1">Popular Albums and Singles</span>
      <ScrollableRow :items="topAlbums" :itemKey="(a) => a.id">
        <template #default="{ item }">
          <ContentCardPreview :item="item" />
        </template>
      </ScrollableRow>
    </div>
    <div>
      <span class="block text-2xl font-bold text-base-content p-1">Popular Tracks</span>
      <ScrollableRow :items="topTracks" :itemKey="(a) => a.id">
        <template #default="{ item }">
          <ContentCardPreview :item="item" />
        </template>
      </ScrollableRow>
    </div>
    <div>
      <span class="block text-2xl font-bold text-base-content p-1">Popular Artists</span>
      <ScrollableRow :items="topArtists" :itemKey="(a) => a.id">
        <template #default="{ item }">
          <ContentCardPreview :item="item" round />
        </template>
      </ScrollableRow>
    </div>
  </main>
</template>

<script setup lang="ts">
  import ScrollableRow from './content/ScrollableRow.vue';
  import ContentCardPreview from './content/ContentCardPreview.vue';
  import { albumStore } from '../store/albums/albums';
  import { trackStore } from '../store/tracks/tracks';
  import { artistStore } from '../store/artists/artists';
  import { storeToRefs } from 'pinia';

  const { topAlbums } = storeToRefs(albumStore());
  const { topTracks } = storeToRefs(trackStore());
  const { topArtists } = storeToRefs(artistStore());
</script>

<style scoped></style>
