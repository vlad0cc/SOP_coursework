<script setup lang="ts">
import { FilePlus2, Send } from '@lucide/vue';
import { documentPriorityOptions, documentTypeOptions } from '../../constants/document-options';
import type { DocumentForm, Employee } from '../../types/documents';

defineProps<{
  currentUserName: string;
  form: DocumentForm;
  recipientOptions: Employee[];
}>();

defineEmits<{
  submit: [];
}>();
</script>

<template>
  <form class="compose-panel" @submit.prevent="$emit('submit')">
    <div class="panel-title">
      <FilePlus2 :size="22" />
      <h2>Создание документа</h2>
    </div>

    <label>
      <span>Отправитель</span>
      <input :value="currentUserName" disabled />
    </label>

    <label>
      <span>Получатель</span>
      <select v-model="form.recipientId" required>
        <option value=""></option>
        <option v-for="employee in recipientOptions" :key="employee.id" :value="employee.id">
          {{ employee.fullName }}
        </option>
      </select>
    </label>

    <label>
      <span>Название</span>
      <input v-model.trim="form.title" required maxlength="120" />
    </label>

    <div class="form-row">
      <label>
        <span>Номер</span>
        <input v-model.trim="form.documentNumber" required maxlength="9" pattern="[A-Za-z]-[0-9]{3}-[0-9]{3}" />
      </label>

      <label>
        <span>Тип</span>
        <select v-model="form.type">
          <option v-for="option in documentTypeOptions" :key="option">{{ option }}</option>
        </select>
      </label>
    </div>

    <div class="form-row">
      <label>
        <span>Срок</span>
        <input v-model="form.dueDate" type="date" />
      </label>

      <label>
        <span>Приоритет</span>
        <select v-model="form.priority">
          <option v-for="option in documentPriorityOptions" :key="option">{{ option }}</option>
        </select>
      </label>
    </div>

    <label>
      <span>Описание</span>
      <textarea v-model.trim="form.description" required rows="5" maxlength="1200" />
    </label>

    <button class="primary-action" type="submit">
      <Send :size="18" />
      Отправить на подпись
    </button>
  </form>
</template>
