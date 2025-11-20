<template>
  <div class="dropdown dropdown-end">
    <div
      tabindex="0"
      role="button"
      class="cursor-pointer select-none"
    >
      <div class="w-8 h-8 rounded-full bg-neutral text-neutral-content ring-2 ring-base-300 ring-offset-2 ring-offset-black transition-transform hover:scale-110 flex items-center justify-center">
        <span class="text-sm font-semibold leading-none">{{ initial }}</span>
      </div>
    </div>
    <ul
      tabindex="0"
      class="dropdown-content menu bg-base-100 rounded-box z-[1] w-32 p-2 shadow-lg mt-2"
    >
      <li>
        <a @click="handleLogout">
          Logout
        </a>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue';
  import type { AuthUser } from '../../store/auth/types';

  const props = defineProps<{
    user: AuthUser;
  }>();

  const emit = defineEmits<{
    logout: [];
  }>();

  const initial = computed(() => {
    const name = props.user.displayName || props.user.email;
    return name.charAt(0).toUpperCase();
  });

  const handleLogout = () => {
    emit('logout');
  };
</script>
