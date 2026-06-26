<script setup lang="ts">
import { LogIn } from '@lucide/vue';
import type { LoginForm } from '../../types/documents';

defineProps<{
  errorMessage: string;
  loading: boolean;
  loginForm: LoginForm;
  open: boolean;
}>();

defineEmits<{
  close: [];
  submit: [];
}>();
</script>

<template>
  <div v-if="open" class="modal-backdrop">
    <form class="login-modal" @submit.prevent="$emit('submit')">
      <div class="panel-title">
        <LogIn :size="22" />
        <h2>Авторизация</h2>
      </div>

      <label>
        <span>Логин</span>
        <input v-model.trim="loginForm.login" required autocomplete="username" />
      </label>

      <label>
        <span>Пароль</span>
        <input v-model="loginForm.password" required type="password" autocomplete="current-password" />
      </label>

      <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

      <div class="modal-actions">
        <button class="secondary-action" type="button" @click="$emit('close')">Закрыть</button>
        <button class="primary-action" type="submit" :disabled="loading">
          <LogIn :size="18" />
          Войти
        </button>
      </div>
    </form>
  </div>
</template>
