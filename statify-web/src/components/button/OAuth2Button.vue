<template>
  <BaseButton
    variant="oauth"
    size="lg"
    shape="pill"
    class="border-contrast hover:border-white text-white"
    :disabled="disabled"
    @click="$emit('click', $event)"
  >
    <template #icon-left>
      <slot name="icon" />
    </template>
    Continue with {{ service }}
  </BaseButton>
</template>

<script setup lang="ts">
  import BaseButton from './BaseButton.vue';

  defineProps<{ service: string; disabled?: boolean }>();
  defineEmits<{ (e: 'click', ev: MouseEvent): void }>();
</script>
