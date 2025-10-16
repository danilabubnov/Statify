<template>
  <div class="utogglegroup">
    <ToggleButton
      v-for="(label, key) in buttons"
      :key="key"
      :text="label"
      :active="modelValue === key"
      @click="$emit('update:modelValue', key)"
    />
  </div>
</template>

<script setup lang="ts">
  import ToggleButton from './button/ToggleButton.vue';
  import type { TimeRange } from '../types/filters';

  defineProps<{
    modelValue: TimeRange;
  }>();

  const buttons: Record<TimeRange, string> = {
    month: 'This month',
    year: 'This year',
    'all-time': 'All time',
  };

  defineEmits<{
    (e: 'update:modelValue', value: TimeRange): void;
  }>();
</script>
