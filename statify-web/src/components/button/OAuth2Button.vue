<template>
  <button
    class="btn btn-lg btn-outline rounded-full border-contrast hover:border-white btn-hoverable"
    @click="$emit('click')"
    :disabled="disabled"
  >
    <slot name="icon" />
    <div class="btn-text text-white">Continue with {{ service }}</div>
  </button>
</template>

<script setup lang="ts">
  defineProps<{ service: string; disabled?: boolean }>();
  defineEmits(['click']);
</script>
