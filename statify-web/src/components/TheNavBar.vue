<template>
  <nav class="navbar bg-black outline-1 outline-base-200">
    <div class="navbar-start ml-6">
      <!-- <img :src="logo" alt="Statify Logo" class="h-10" /> -->
    </div>
    <div class="navbar-end mr-6">
      <AnimatedButton v-if="!accessToken" @click="$emit('open-form')" text="Get Started" class="h-10" />
      <UserAvatar v-else-if="currentUser" :user="currentUser" @logout="$emit('logout')" />
    </div>
  </nav>
</template>

<script setup lang="ts">
  import { authStore } from '../store/auth/auth';
  import AnimatedButton from './button/AnimatedButton.vue';
  import UserAvatar from './user/UserAvatar.vue';
  import { storeToRefs } from 'pinia';

  const store = authStore();
  const { accessToken, currentUser } = storeToRefs(store);

  defineEmits(['open-form', 'logout']);
</script>
