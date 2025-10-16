<template>
  <div
    class="uxcard block"
    :class="props.fluid ? 'w-full' : 'w-48 max-w-56'"
  >
    <div class="uxcardbody">
      <div class="uxcardmedia" :class="[props.round ? 'rounded-full' : 'rounded-lg']">
        <div
          class="relative w-full animate-pulse"
          style="padding-bottom: 100%; background-color: var(--color-surface);"
        >
        </div>
      </div>

      <div class="flex flex-col h-[4.5rem] gap-2 pt-1">
        <div
          class="h-4 rounded animate-pulse"
          style="background-color: var(--color-surface); width: 85%;"
        ></div>
        <div
          class="h-3 rounded animate-pulse"
          style="background-color: var(--color-surface); width: 65%;"
        ></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  const props = defineProps<{
    round?: boolean;
    fluid?: boolean;
  }>();
</script>
