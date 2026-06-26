<script setup lang="ts">
import { Check, FileText, X } from '@lucide/vue';
import type { FlowDocument } from '../../types/documents';

defineProps<{
  canAnswer: boolean;
  declineId: number | null;
  declineReason: string;
  document: FlowDocument;
  formatDate: (value?: string) => string;
  statusClass: (status: string) => Record<string, boolean>;
  statusText: (status: string) => string;
}>();

defineEmits<{
  'update:declineId': [value: number | null];
  'update:declineReason': [value: string];
  decline: [documentId: number];
  sign: [documentId: number];
}>();
</script>

<template>
  <article class="document-card">
    <div class="document-card__head">
      <FileText :size="22" />
      <div>
        <h4>{{ document.title }}</h4>
        <p>{{ document.documentNumber }} · {{ document.type }}</p>
      </div>
    </div>

    <div class="badges">
      <span :class="['badge', statusClass(document.status)]">{{ statusText(document.status) }}</span>
      <span class="badge priority-normal">{{ document.priority }}</span>
    </div>

    <dl class="document-meta">
      <div>
        <dt>Отправитель</dt>
        <dd>{{ document.sender.fullName }}</dd>
      </div>
      <div>
        <dt>Получатель</dt>
        <dd>{{ document.recipient.fullName }}</dd>
      </div>
      <div>
        <dt>Создан</dt>
        <dd>{{ formatDate(document.createdAt) }}</dd>
      </div>
      <div>
        <dt>Срок</dt>
        <dd>{{ formatDate(document.dueDate) }}</dd>
      </div>
    </dl>

    <p class="description">{{ document.description }}</p>
    <p v-if="document.auditComment" class="note">{{ document.auditComment }}</p>
    <p v-if="document.declineReason" class="note">{{ document.declineReason }}</p>
    <p v-if="document.signature" class="note">Подпись: {{ document.signature }}</p>

    <div v-if="canAnswer" class="actions">
      <button class="approve-action" type="button" @click="$emit('sign', document.id)">
        <Check :size="17" />
        Подписать
      </button>

      <button class="danger-action" type="button" @click="$emit('update:declineId', document.id)">
        <X :size="17" />
        Отказать
      </button>
    </div>

    <div v-if="canAnswer && declineId === document.id" class="decline-box">
      <label>
        <span>Причина отказа</span>
        <textarea
          :value="declineReason"
          rows="3"
          @input="$emit('update:declineReason', ($event.target as HTMLTextAreaElement).value)"
        />
      </label>

      <button class="danger-action" type="button" @click="$emit('decline', document.id)">Отправить отказ</button>
    </div>
  </article>
</template>
