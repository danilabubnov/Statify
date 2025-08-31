<template>
  <div class="transition-wrapper">
    <Transition name="fade">
      <TheLoadingScreen v-if="loading" @animation-end="toggleLoading" />
      <div v-else>
        <NavBar @open-form="toggleForm" />
        <Transition name="modal-fade">
          <TheAuthForm v-if="formIsVisible" @close-modal="toggleForm" />
        </Transition>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
  import { onMounted, ref, watch } from 'vue';
  import NavBar from './components/TheNavBar.vue';
  import TheAuthForm from './components/form/TheAuthForm.vue';
  import { storeToRefs } from 'pinia';
  import TheLoadingScreen from './components/TheLoadingScreen.vue';
  import { authStore } from './store/auth/auth';

  const formIsVisible = ref(false);
  const loading = ref(true);
  const { accessToken } = storeToRefs(authStore());

  function toggleForm() {
    formIsVisible.value = !formIsVisible.value;
  }

  function toggleLoading() {
    loading.value = !loading.value;
  }

  watch(accessToken, (newToken, oldToken) => {
    if (!oldToken && newToken && formIsVisible.value) {
      toggleForm();
    }
  });

  onMounted(() => {
    if (import.meta.env.DEV) {
      authStore().logout();
    }
  });
</script>

<style scoped>
  .transition-wrapper {
    position: relative;
  }

  .transition-wrapper > * {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
  }

  .fade-enter-active {
    transition: opacity 1s linear;
  }
  .fade-leave-active {
    transition: opacity 1s linear;
  }

  .fade-enter-from {
    opacity: 0;
  }
  .fade-enter-to {
    opacity: 1;
  }

  .fade-leave-from {
    opacity: 1;
  }
  .fade-leave-to {
    opacity: 0;
  }

  .modal-fade-enter-active,
  .modal-fade-leave-active {
    transition: opacity 0.4s ease, transform 0.4s ease;
  }

  .modal-fade-enter-from,
  .modal-fade-leave-to {
    opacity: 0;
    transform: translateY(30px);
  }
</style>
