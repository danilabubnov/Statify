<template>
  <BaseButton variant="outline" size="lg" shape="pill" :text="text" @click="$emit('click', $event)" />
</template>

<script setup lang="ts">
  import BaseButton from './BaseButton.vue';
  defineProps<{ text: string }>();
  defineEmits<{ (e: 'click', ev: MouseEvent): void }>();
</script>

<style scoped></style>
