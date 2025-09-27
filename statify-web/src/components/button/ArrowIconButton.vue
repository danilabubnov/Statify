<template>
  <BaseButton variant="carousel-arrow" shape="pill" size="icon-sm" @click="$emit('click', $event)">
    <svg viewBox="0 0 16 16" fill="currentColor" class="w-4 h-4" :class="rotationClass">
      <path d="M4.97.47a.75.75 0 0 0 0 1.06L11.44 8l-6.47 6.47a.75.75 0 1 0 1.06 1.06L13.56 8 6.03.47a.75.75 0 0 0-1.06 0" />
    </svg>
  </BaseButton>
</template>

<script setup lang="ts">
  import { computed } from 'vue';
  import BaseButton from './BaseButton.vue';

  const props = defineProps<{
    direction?: 'left' | 'right' | 'up' | 'down';
  }>();

  const rotationClass = computed(() => {
    switch (props.direction) {
      case 'left':
        return 'rotate-180';
      case 'up':
        return '-rotate-90';
      case 'down':
        return 'rotate-90';
      default:
        return '';
    }
  });

  defineEmits<{ (e: 'click', ev: MouseEvent): void }>();
</script>
