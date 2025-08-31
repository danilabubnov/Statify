<template>
  <div>
    <button class="font-bold underline hover:text-secondary" @click="$emit('click')">{{ text }}</button>
  </div>
</template>

<script setup lang="ts">
  defineProps({ text: { type: String, required: true } });
  defineEmits(['click']);
</script>

<style scoped></style>
