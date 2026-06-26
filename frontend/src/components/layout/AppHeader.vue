<script setup lang="ts">
import { LogIn, LogOut } from '@lucide/vue';
import type { CurrentUser } from '../../types/documents';

defineProps<{
  currentUser: CurrentUser | null;
}>();

defineEmits<{
  openLogin: [];
  logout: [];
}>();
</script>

<template>
  <header class="topbar">
    <div>
      <p class="eyebrow">ООО «ГК "ФСК"»</p>
      <h1>Электронный документооборот</h1>
    </div>

    <div class="header-actions">
      <div v-if="currentUser" class="user-chip">
        <strong>{{ currentUser.fullName }}</strong>
        <span>{{ currentUser.position }}</span>
      </div>

      <button v-if="!currentUser" class="primary-action" type="button" @click="$emit('openLogin')">
        <LogIn :size="18" />
        Авторизация
      </button>

      <button v-else class="secondary-action" type="button" @click="$emit('logout')">
        <LogOut :size="18" />
        Выйти
      </button>
    </div>
  </header>
</template>
