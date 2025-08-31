<template>
  <button
    class="btn btn-lg btn-secondary btn-hoverable rounded-full"
    @click="$emit('click')"
    :disabled="disabled"
  >
    <span>{{ text }}</span>
  </button>
</template>

<script setup lang="ts">
defineProps<{ text: string; disabled?: boolean }>();
defineEmits(["click"]);
</script>
