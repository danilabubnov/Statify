<template>
  <div class="px-4">
    <router-link :to="to" class="pl-2 text-2xl font-bold text-base-content hover:underline">
      {{ title }}
    </router-link>

    <ScrollableRow :items="items" :itemKey="itemKey" class="mt-2">
      <template #default="slotProps">
        <slot v-bind="slotProps" />
      </template>
    </ScrollableRow>
  </div>
</template>

<script setup lang="ts" generic="T">
  import ScrollableRow from './ScrollableRow.vue';

  defineProps<{
    title: string;
    to: any;
    items: T[];
    itemKey: (item: T, index: number) => string;
  }>();
</script>
