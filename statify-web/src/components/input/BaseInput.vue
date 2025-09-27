<template>
  <input
    :type="type"
    :placeholder="placeholder"
    class="input rounded-sm px-4 py-2 focus:outline-none hover:border-white"
    :value="modelValue"
    @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
  />
</template>

<script setup lang="ts">
  defineProps({
    placeholder: { type: String, required: false },
    modelValue: { type: String, required: false },
    type: { type: String, required: false, default: 'text' },
  });
  defineEmits<{ (e: 'update:modelValue', value: string): void }>();
</script>
