<template>
  <BaseButton variant="link" size="md" shape="none" :text="text" @click="$emit('click', $event)" />
</template>

<script setup lang="ts">
  import BaseButton from './BaseButton.vue';

  defineProps<{ text: string }>();
  defineEmits<{ (e: 'click', ev: MouseEvent): void }>();
</script>
