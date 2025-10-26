<template>
  <main class="flex-1 bg-base-200 rounded-xl m-2 p-4">
    <SectionRow title="Popular Albums and Singles" :to="{ name: 'albums-popular' }" :items="currentAlbums" :itemKey="(a) => a.id">
      <template #default="{ item }">
        <ContentCardPreview :item="item" />
      </template>
    </SectionRow>

    <SectionRow title="Popular Tracks" :to="{ name: 'tracks-popular' }" :items="currentTracks" :itemKey="(a) => a.id">
      <template #default="{ item }">
        <ContentCardPreview :item="item" />
      </template>
    </SectionRow>

    <SectionRow title="Popular Artists" :to="{ name: 'tracks-popular' }" :items="currentArtists" :itemKey="(a) => a.id">
      <template #default="{ item }">
        <ContentCardPreview :item="item" round />
      </template>
    </SectionRow>
  </main>
</template>

<script setup lang="ts">
  import { onMounted } from 'vue';
  import SectionRow from './SectionRow.vue';
  import ContentCardPreview from './ContentCardPreview.vue';
  import { albumStore } from '../../store/albums/albums';
  import { trackStore } from '../../store/tracks/tracks';
  import { artistStore } from '../../store/artists/artists';
  import { storeToRefs } from 'pinia';

  const albums = albumStore();
  const tracks = trackStore();
  const artists = artistStore();

  const { currentAlbums } = storeToRefs(albums);
  const { currentTracks } = storeToRefs(tracks);
  const { currentArtists } = storeToRefs(artists);

  onMounted(async () => {
    tracks.currentPage = 0;
    albums.currentPage = 0;
    artists.currentPage = 0;

    await Promise.all([
      tracks.fetchTopTracks({ page: 0, withTotal: false }),
      albums.fetchTopAlbums({ page: 0, withTotal: false }),
      artists.fetchTopArtists({ page: 0, withTotal: false })
    ]);
  });
</script>

<style scoped></style>
