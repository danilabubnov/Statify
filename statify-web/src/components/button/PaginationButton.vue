<template>
  <button
    ref="el"
    type="button"
    class="relative z-10 flex h-[35px] items-center justify-center rounded-xl sm:h-[43px]"
    :class="[
      isActive ? '' : 'hover:bg-surface',
      isEllipsis ? 'w-[40px] sm:w-[50px]' : 'w-[32px] sm:w-[40px]',
      props.disabled ? 'cursor-not-allowed opacity-30' : ''
    ]"
    :disabled="props.disabled"
    @click="handleClick"
  >
    <span class="md:text-base text-sm" :class="isActive ? 'font-medium utextblack' : 'utextwhite'">{{
      props.text
    }}</span>
  </button>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';

  const el = ref<HTMLButtonElement | null>(null);
  const props = withDefaults(
    defineProps<{
      text: string | number;
      active?: boolean;
      disabled?: boolean;
    }>(),
    {
      active: false,
      disabled: false,
    },
  );

  const isActive = computed(() => props.active);
  const isEllipsis = computed(() => props.text === '…');

  const emit = defineEmits<{
    (e: 'follow', value: number): void;
    (e: 'click'): void;
  }>();

  const handleClick = () => {
    if (typeof props.text === 'number') {
      emit('follow', props.text);
    } else {
      emit('click');
    }
  };

  defineExpose({ el });
</script>
