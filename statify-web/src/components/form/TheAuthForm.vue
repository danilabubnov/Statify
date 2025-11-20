<template>
  <dialog class="modal" ref="dialogElement" @click.self="closeModal">
    <div class="modal-box custom-shadow rounded-xl transition-all ease-out">
      <div class="flex flex-col gap-4">
        <h2 class="text-center text-3xl font-bold text-white">Sign {{ isRegisterMode ? 'up' : 'in' }} for Statify</h2>

        <div class="mt-2 flex justify-center">
          <OAuth2Button class="w-3/5" service="Spotify" @click="handleSpotifyOAuth" :disabled="pending">
            <template #icon>
              <img :src="spotifyLogo" alt="Spotify logo" class="mr-2 h-6" />
            </template>
            Continue with Spotify
          </OAuth2Button>
        </div>

        <div class="divider">or continue with email</div>

        <FormRow>
          <BaseInput
            placeholder="Email address"
            v-model="email"
            type="email"
            inputmode="email"
            @input="onEmailInput"
            @keyup.enter="handleLogin"
          />
          <FormError v-if="emailError">{{ emailError }}</FormError>
        </FormRow>

        <FormRow>
          <BaseInput placeholder="Password" v-model="password" type="password" @input="onPasswordInput" @keyup.enter="handleLogin" />
          <FormError v-if="passwordError">{{ passwordError }}</FormError>
        </FormRow>

        <div
          class="-mt-2.5 transition-[max-height,opacity] [transition-duration:var(--anim-duration)] [transition-timing-function:var(--anim-ease)]"
          :class="isRegisterMode ? 'max-h-36 opacity-100' : 'max-h-0 opacity-0'"
        >
          <FormRow>
            <Transition name="fade">
              <p v-if="showPasswordHint" class="text-center text-xs">Use at least {{ PASSWORD_MIN_LENGTH }} characters.</p>
            </Transition>

            <BaseInput
              class="mt-2"
              placeholder="Confirm password"
              v-model="confirmPassword"
              type="password"
              @input="onConfirmPasswordInput"
              @keyup.enter="handleLogin"
            />

            <FormError v-if="confirmPasswordError">{{ confirmPasswordError }}</FormError>
          </FormRow>
        </div>

        <FormError v-if="submitError !== null">{{ submitError }}</FormError>

        <div class="flex justify-center">
          <CommonButton class="w-3/5" :text="isRegisterMode ? 'Create account' : 'Sign in'" @click="handleLogin" :disabled="pending" />
        </div>

        <div class="flex justify-center gap-1 text-sm">
          <template v-if="isRegisterMode">
            <span>Already have an account?</span>
            <LinkButton text="Sign in" @click="toggleForm" />
          </template>
          <template v-else>
            <span>New here?</span>
            <LinkButton text="Create a Statify account" @click="toggleForm" />
          </template>
        </div>
      </div>
    </div>
  </dialog>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue';
  import { isEmail } from 'validator';

  import BaseInput from '../input/BaseInput.vue';
  import CommonButton from '../button/CommonButton.vue';
  import FormError from './formElement/FormError.vue';
  import FormRow from './formElement/FormRow.vue';
  import LinkButton from '../button/LinkButton.vue';
  import OAuth2Button from '../button/OAuth2Button.vue';
  import spotifyLogo from '../../assets/spotify-logo.svg';
  import { mapErrorMessage } from '../../errors/errorMapper';
  import { authStore } from '../../store/auth/auth';

  type AuthType = 'login' | 'register';

  const PASSWORD_MIN_LENGTH = 8;

  const BLANK_EMAIL_ERROR = 'Please enter your email';
  const INVALID_EMAIL_ERROR = 'Please enter a valid email address';

  const BLANK_PASSWORD_ERROR = 'Please enter your password';
  const INVALID_PASSWORD_ERROR = `Password must be at least ${PASSWORD_MIN_LENGTH} characters.`;

  const BLANK_CONFIRM_ERROR = 'Please confirm your password';
  const PASSWORD_MUST_MATCH_ERROR = 'Passwords do not match';

  const dialogElement = ref<HTMLDialogElement | null>(null);
  const mode = ref<AuthType>('login');
  const email = ref('');
  const password = ref('');
  const confirmPassword = ref('');

  const store = authStore();
  const pending = ref(false);
  const submitAttempted = ref(false);
  const emailTouched = ref(false);
  const passwordTouched = ref(false);
  const confirmTouched = ref(false);

  const submitError = ref<string | null>(null);

  const isRegisterMode = computed(() => mode.value === 'register');

  const emailError = computed(() => {
    const v = email.value.trim();

    if (!(submitAttempted.value || emailTouched.value)) return '';
    if (!v) return BLANK_EMAIL_ERROR;
    if (submitAttempted.value && !isEmail(v)) {
      return INVALID_EMAIL_ERROR;
    }

    return '';
  });

  const passwordError = computed(() => {
    if (!submitAttempted.value) return '';
    if (!password.value) return BLANK_PASSWORD_ERROR;
    if (isRegisterMode.value && password.value.length < PASSWORD_MIN_LENGTH) return INVALID_PASSWORD_ERROR;
    return '';
  });

  const confirmPasswordError = computed(() => {
    if (!isRegisterMode.value) return '';
    if (!(submitAttempted.value || confirmTouched.value)) return '';
    if (!confirmPassword.value) return BLANK_CONFIRM_ERROR;
    return confirmPassword.value === password.value ? '' : PASSWORD_MUST_MATCH_ERROR;
  });

  const showPasswordHint = computed(
    () => isRegisterMode.value && password.value.length > 0 && password.value.length < PASSWORD_MIN_LENGTH && !submitAttempted.value,
  );

  const emit = defineEmits(['close-modal']);

  function closeModal() {
    emit('close-modal');
  }

  async function handleLogin() {
    if (pending.value) return;
    submitError.value = null;
    submitAttempted.value = true;

    if (emailError.value || passwordError.value || confirmPasswordError.value) return;

    try {
      pending.value = true;

      const emailTrimmed = email.value.trim();

      if (isRegisterMode.value) {
        await store.registerWithoutStateUpdate({
          email: emailTrimmed,
          password: password.value,
        });
      } else {
        await store.loginWithoutStateUpdate({
          email: emailTrimmed,
          password: password.value,
        });
      }

      window.location.reload();
    } catch (e: any) {
      submitError.value = mapErrorMessage(e);
    } finally {
      pending.value = false;
    }
  }

  function toggleForm() {
    mode.value = isRegisterMode.value ? 'login' : 'register';

    confirmPassword.value = '';
    confirmTouched.value = false;

    submitAttempted.value = false;
    submitError.value = null;
  }

  function handleSpotifyOAuth() {}

  function onEmailInput() {
    emailTouched.value = true;
  }

  function onPasswordInput() {
    passwordTouched.value = true;
  }

  function onConfirmPasswordInput() {
    if (!isRegisterMode.value) return;
    confirmTouched.value = true;
  }

  onMounted(() => {
    dialogElement.value?.showModal();
  });
</script>

<style scoped>
  .modal-box {
    --anim-duration: 500ms;
    --anim-ease: linear;
  }

  .custom-shadow {
    box-shadow:
      0 0 40px 0px color-mix(in srgb, var(--color-secondary) 50%, transparent),
      0 0 10px 0px color-mix(in srgb, var(--color-secondary) 10%, transparent);
  }

  .fade-enter-active,
  .fade-leave-active {
    transition: opacity var(--anim-duration) var(--anim-ease);
  }
  .fade-enter-from,
  .fade-leave-to {
    opacity: 0;
  }
</style>
