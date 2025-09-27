<template>
  <BaseButton type="submit" variant="secondary" size="lg" shape="pill" :text="text" :disabled="disabled" @click="$emit('click', $event)" />
</template>

<script setup lang="ts">
  import BaseButton from './BaseButton.vue';

  defineProps<{ text: string; disabled?: boolean }>();
  defineEmits<{ (e: 'click', ev: MouseEvent): void }>();
</script>
