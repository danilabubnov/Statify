<template>
  <div class="bg-base-200">
    <div class="icon">
      <svg xmlns="http://www.w3.org/2000/svg" :viewBox="`0 0 ${screenWidth} ${screenHeight}`" :width="screenWidth" :height="screenHeight">
        <circle ref="circleEl" :cx="centerX" :cy="centerY" :r="radius" fill="none" stroke="black" stroke-width="4" />
      </svg>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, onMounted, onUnmounted } from 'vue';
  import { animate } from 'animejs';

  const screenWidth = ref(window.innerWidth);
  const screenHeight = ref(window.innerHeight);

  const centerX = computed(() => screenWidth.value / 2);
  const centerY = computed(() => screenHeight.value / 2);
  const radius = computed(() => Math.min(screenWidth.value, screenHeight.value) * 0.02);

  const circleEl = ref<SVGCircleElement | null>(null);

  function updateSize() {
    screenWidth.value = window.innerWidth;
    screenHeight.value = window.innerHeight;
  }

  const emit = defineEmits<{
    (e: 'animation-end'): void;
  }>();

  onMounted(() => {
    window.addEventListener('resize', updateSize);

    if (circleEl.value) {
      const length = 2 * Math.PI * radius.value;
      circleEl.value.style.strokeDasharray = `${length}`;
      circleEl.value.style.strokeDashoffset = `${length}`;

      animate([circleEl.value], {
        strokeDashoffset: [length, 0],
        easing: 'linear',
        duration: 1000,
        loop: false,
        stroke: ['oklch(96% 0.067 122.328)', 'oklch(82% 0.21 140)'],
        onComplete: () => {
          emit('animation-end');
        },
      });
    }
  });

  onUnmounted(() => {
    window.removeEventListener('resize', updateSize);
  });
</script>
