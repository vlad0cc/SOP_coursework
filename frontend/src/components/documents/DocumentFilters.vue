<script setup lang="ts">
import { Search } from '@lucide/vue';
import { dueFilterOptions } from '../../constants/document-options';
import type { DocumentView } from '../../types/documents';

defineProps<{
  activeView: DocumentView;
  documentTypes: string[];
  dueFilter: string;
  numberSearch: string;
  pendingIncomingCount: number;
  typeFilter: string;
}>();

defineEmits<{
  'update:activeView': [value: DocumentView];
  'update:dueFilter': [value: string];
  'update:numberSearch': [value: string];
  'update:typeFilter': [value: string];
}>();
</script>

<template>
  <div class="registry-toolbar">
    <div class="panel-title">
      <Search :size="22" />
      <h2>Мои документы</h2>
    </div>

    <div class="pending-info">
      <strong>{{ pendingIncomingCount }}</strong>
      <span>ожидают подписи</span>
    </div>
  </div>

  <div class="filters-panel">
    <div class="view-tabs">
      <button :class="{ active: activeView === 'incoming' }" type="button" @click="$emit('update:activeView', 'incoming')">
        Входящие
      </button>
      <button :class="{ active: activeView === 'outgoing' }" type="button" @click="$emit('update:activeView', 'outgoing')">
        Исходящие
      </button>
      <button :class="{ active: activeView === 'archive' }" type="button" @click="$emit('update:activeView', 'archive')">
        Архив
      </button>
    </div>

    <label class="search-field">
      <span>Поиск по номеру</span>
      <div>
        <Search :size="17" />
        <input :value="numberSearch" @input="$emit('update:numberSearch', ($event.target as HTMLInputElement).value)" />
      </div>
    </label>

    <div class="filter-row">
      <label>
        <span>Тип документа</span>
        <select :value="typeFilter" @change="$emit('update:typeFilter', ($event.target as HTMLSelectElement).value)">
          <option v-for="type in documentTypes" :key="type">{{ type }}</option>
        </select>
      </label>

      <label>
        <span>Срок</span>
        <select :value="dueFilter" @change="$emit('update:dueFilter', ($event.target as HTMLSelectElement).value)">
          <option v-for="option in dueFilterOptions" :key="option">{{ option }}</option>
        </select>
      </label>
    </div>
  </div>
</template>
