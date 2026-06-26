<script setup lang="ts">
import DocumentCard from './DocumentCard.vue';
import DocumentFilters from './DocumentFilters.vue';
import type { DocumentView, FlowDocument } from '../../types/documents';

defineProps<{
  activeView: DocumentView;
  activeViewTitle: string;
  canAnswer: (document: FlowDocument) => boolean;
  declineId: number | null;
  declineReason: string;
  documentTypes: string[];
  documents: FlowDocument[];
  dueFilter: string;
  formatDate: (value?: string) => string;
  numberSearch: string;
  pendingIncomingCount: number;
  statusClass: (status: string) => Record<string, boolean>;
  statusText: (status: string) => string;
  typeFilter: string;
}>();

defineEmits<{
  'update:activeView': [value: DocumentView];
  'update:declineId': [value: number | null];
  'update:declineReason': [value: string];
  'update:dueFilter': [value: string];
  'update:numberSearch': [value: string];
  'update:typeFilter': [value: string];
  decline: [documentId: number];
  sign: [documentId: number];
}>();
</script>

<template>
  <section class="registry-panel">
    <DocumentFilters
      :active-view="activeView"
      :document-types="documentTypes"
      :due-filter="dueFilter"
      :number-search="numberSearch"
      :pending-incoming-count="pendingIncomingCount"
      :type-filter="typeFilter"
      @update:active-view="$emit('update:activeView', $event)"
      @update:due-filter="$emit('update:dueFilter', $event)"
      @update:number-search="$emit('update:numberSearch', $event)"
      @update:type-filter="$emit('update:typeFilter', $event)"
    />

    <div class="document-section">
      <h3>{{ activeViewTitle }}</h3>

      <div v-if="documents.length" class="document-list">
        <DocumentCard
          v-for="document in documents"
          :key="document.id"
          :can-answer="canAnswer(document)"
          :decline-id="declineId"
          :decline-reason="declineReason"
          :document="document"
          :format-date="formatDate"
          :status-class="statusClass"
          :status-text="statusText"
          @decline="$emit('decline', $event)"
          @sign="$emit('sign', $event)"
          @update:decline-id="$emit('update:declineId', $event)"
          @update:decline-reason="$emit('update:declineReason', $event)"
        />
      </div>

      <div v-else class="empty-state">
        <p>Документы не найдены</p>
      </div>
    </div>
  </section>
</template>
