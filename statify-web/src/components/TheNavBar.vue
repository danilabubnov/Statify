<template>
  <nav class="navbar">
    <div class="navbar-start ml-6">
      <img :src="logo" alt="Statify Logo" class="h-10" />
    </div>
    <div class="navbar-end mr-6">
      <AnimatedButton v-if="!accessToken" @click="$emit('open-form')" text="Get Started" class="h-10" />
    </div>
  </nav>
</template>

<script setup lang="ts">
  import logo from '../assets/statify-logo.png';
  import { authStore } from '../store/auth/auth';
  import AnimatedButton from './button/AnimatedButton.vue';
  import { storeToRefs } from 'pinia';

  const store = authStore();
  const { accessToken } = storeToRefs(store);

  defineEmits(['open-form']);
</script>
