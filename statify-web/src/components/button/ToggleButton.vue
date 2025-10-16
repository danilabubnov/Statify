<template>
  <button
    type="button"
    class="utogglebtn"
    :class="isActive ? 'utoggleactive' : 'utoggleidletext hover:utogglehover'"
    @click="$emit('click', $event)"
  >
    <span class="text-center whitespace-nowrap" :class="isActive ? 'font-semibold' : 'font-light'">
      {{ text }}
    </span>
  </button>
</template>

<script setup lang="ts">
  import { computed } from 'vue';

  const props = withDefaults(
    defineProps<{
      text: string;
      active?: boolean;
    }>(),
    {
      active: false,
    },
  );

  const isActive = computed(() => props.active);

  defineEmits<{ (e: 'click', ev: MouseEvent): void }>();
</script>
