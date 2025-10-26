<template>
  <main class="mt-4 lg:mx-4 lg:mt-2 grid grid-rows-[auto_1fr] min-h-[calc(100vh-5rem)]">
    <section class="flex flex-col items-center justify-between lg:mx-5 lg:my-2 lg:flex-row">
      <div class="flex flex-col items-center lg:items-start">
        <span class="usectiontitle">{{ pageTitle }}</span>
        <span class="text-base-content/70 usectionsub font-normal"> Updated 2 days ago</span>
      </div>

      <SwitchTabs class="mx-2 mt-4 mb-2 w-full max-w-xs lg:mx-0 lg:mt-0" v-model="timeRange" />
    </section>

    <div class="flex flex-col">
      <div ref="scrollAnchor"></div>
      <div class="ugridtracks">
        <template v-if="store.loading">
          <div v-for="i in currentSize" :key="`skeleton-${i}`" class="self-start ucontentvisibility">
            <ContentCardSkeleton fluid />
          </div>
        </template>
        <template v-else>
          <div v-for="item in items" :key="item.id" class="self-start transition-[opacity,transform] duration-200 ucontentvisibility">
            <ContentCardPreview :item="item" fluid />
          </div>
        </template>
      </div>

      <div class="flex justify-center mt-auto">
        <PaginationBar class="my-4" />
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
  import { computed, ref, watch, onMounted } from 'vue';
  import SwitchTabs from '../SwitchTabs.vue';
  import { useRoute, useRouter } from 'vue-router';
  import ContentCardPreview from './ContentCardPreview.vue';
  import ContentCardSkeleton from './ContentCardSkeleton.vue';
  import PaginationBar from '../pagination/PaginationBar.vue';
  import { useContentStore } from '../../composables/useContentStore';
  import { CONTENT_TITLES } from '../../constants/content';
  import type { TimeRange } from '../../types/filters';

  const route = useRoute();
  const router = useRouter();
  const type = route.meta.type as 'tracks' | 'albums';

  const timeRange = ref<TimeRange>('all-time');
  const scrollAnchor = ref<HTMLDivElement | null>(null);

  const { store, items, currentSize, resetAndFetch, rollback } = useContentStore(type);

  const pageTitle = computed(() => CONTENT_TITLES[type]);

  const scrollToContent = () => {
    scrollAnchor.value?.scrollIntoView({
      behavior: 'smooth',
      block: 'start'
    });
  };

  watch(
    timeRange,
    async (newRange, oldRange) => {
      try {
        await resetAndFetch(newRange);
        scrollToContent();
      } catch (error) {
        console.error(`Failed to fetch ${type} with new filter:`, error);
        rollback(oldRange);
        timeRange.value = oldRange;
      }
    },
    { deep: false }
  );

  watch(() => store.currentPage, scrollToContent);

  onMounted(async () => {
    store.setRouter(router);

    timeRange.value = store.activeTimeRangeFilter;

    const pageParam = route.query.page;
    if (pageParam && typeof pageParam === 'string') {
      const pageNumber = parseInt(pageParam, 10);
      if (!isNaN(pageNumber) && pageNumber > 0) {
        store.isInitialLoad = false;
        store.currentPage = pageNumber - 1;

        if (type === 'tracks' && 'fetchTopTracks' in store) {
          await store.fetchTopTracks({ page: pageNumber - 1 });
        } else if (type === 'albums' && 'fetchTopAlbums' in store) {
          await store.fetchTopAlbums({ page: pageNumber - 1 });
        }
      }
    }
  });
</script>
