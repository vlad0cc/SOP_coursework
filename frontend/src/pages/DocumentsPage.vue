<script setup lang="ts">
import { FileText } from '@lucide/vue';
import LoginModal from '../components/auth/LoginModal.vue';
import DocumentComposePanel from '../components/documents/DocumentComposePanel.vue';
import DocumentRegistry from '../components/documents/DocumentRegistry.vue';
import AppHeader from '../components/layout/AppHeader.vue';
import { useDocumentFlow } from '../composables/useDocumentFlow';

const {
  activeView,
  activeViewTitle,
  canAnswer,
  createDocument,
  currentUser,
  declineDocument,
  declineId,
  declineReason,
  documentForm,
  documentTypes,
  dueFilter,
  errorMessage,
  filteredDocuments,
  formatDate,
  loading,
  login,
  loginForm,
  loginOpen,
  logout,
  numberSearch,
  pendingIncomingCount,
  recipientOptions,
  signDocument,
  statusClass,
  statusText,
  typeFilter,
} = useDocumentFlow();
</script>

<template>
  <main class="app-shell">
    <section class="workspace">
      <AppHeader :current-user="currentUser" @logout="logout" @open-login="loginOpen = true" />

      <section v-if="!currentUser" class="locked-state">
        <FileText :size="42" />
        <p>Для работы с документами войдите в систему</p>
      </section>

      <section v-else class="layout-grid">
        <DocumentComposePanel
          :current-user-name="currentUser.fullName"
          :form="documentForm"
          :recipient-options="recipientOptions"
          @submit="createDocument"
        />

        <DocumentRegistry
          :active-view="activeView"
          :active-view-title="activeViewTitle"
          :can-answer="canAnswer"
          :decline-id="declineId"
          :decline-reason="declineReason"
          :document-types="documentTypes"
          :documents="filteredDocuments"
          :due-filter="dueFilter"
          :format-date="formatDate"
          :number-search="numberSearch"
          :pending-incoming-count="pendingIncomingCount"
          :status-class="statusClass"
          :status-text="statusText"
          :type-filter="typeFilter"
          @decline="declineDocument"
          @sign="signDocument"
          @update:active-view="activeView = $event"
          @update:decline-id="declineId = $event"
          @update:decline-reason="declineReason = $event"
          @update:due-filter="dueFilter = $event"
          @update:number-search="numberSearch = $event"
          @update:type-filter="typeFilter = $event"
        />
      </section>
    </section>

    <LoginModal
      :error-message="errorMessage"
      :loading="loading"
      :login-form="loginForm"
      :open="loginOpen"
      @close="loginOpen = false"
      @submit="login"
    />
  </main>
</template>
