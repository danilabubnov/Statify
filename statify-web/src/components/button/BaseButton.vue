<template>
  <component
    :is="asTag"
    v-bind="attrs"
    :type="asTag === 'button' ? 'button' : undefined"
    :class="[baseClass]"
    :disabled="asTag === 'button' ? disabled : undefined"
    @click="$emit('click', $event)"
  >
    <slot name="icon-left" />

    <span v-if="hasTextSlot" class="btn-text">
      <slot>{{ text }}</slot>
    </span>
  </component>
</template>

<script setup lang="ts">
  import { computed, useSlots } from 'vue';
  import { SHAPE_CLASSES, SIZE_CLASSES, VARIANT_CLASSES, type ButtonShape, type ButtonSize, type ButtonVariant } from './button.tokens';

  type As = 'button' | 'router-link';

  const props = withDefaults(
    defineProps<{
      text?: string;
      variant: ButtonVariant;
      shape: ButtonShape;
      size: ButtonSize;
      as?: As;
      to?: Record<string, unknown>;
      disabled?: boolean;
    }>(),
    {
      as: 'button',
      disabled: false,
    },
  );

  defineEmits<{ (e: 'click', ev: MouseEvent): void }>();

  const asTag = computed(() => {
    if (props.as === 'router-link') return 'RouterLink';
    return 'button';
  });
  const attrs = computed(() => {
    if (props.as === 'router-link') return { to: props.to };
    return {};
  });

  const baseClass = computed(() => {
    return [VARIANT_CLASSES[props.variant], SIZE_CLASSES[props.size], SHAPE_CLASSES[props.shape]].join(' ');
  });

  const slots = useSlots();
  const hasTextSlot = computed(() => !!props.text || !!slots.default);
</script>
