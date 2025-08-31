<template>
  <button class="btn btn-outline btn-lg btn-hoverable rounded-3xl hover:rounded-lg hover:border-current" @click="$emit('click')">
    <span>{{ text }}</span>
  </button>
</template>

<script setup lang="ts">
  defineProps<{
    text: string;
  }>();

  defineEmits(['click']);
</script>

<style scoped></style>
