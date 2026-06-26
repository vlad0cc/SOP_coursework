import { computed, onUnmounted, reactive, ref } from 'vue';
import { documentPriorityOptions, documentTypeOptions } from '../constants/document-options';
import {
  createDocumentRequest,
  declineDocumentRequest,
  fetchEmployees,
  fetchMyDocuments,
  loginRequest,
  signDocumentRequest,
} from '../services/api';
import type { CurrentUser, DocumentForm, DocumentView, Employee, FlowDocument, LoginForm } from '../types/documents';

export const useDocumentFlow = () => {
  const currentUser = ref<CurrentUser | null>(null);
  const documents = ref<FlowDocument[]>([]);
  const employees = ref<Employee[]>([]);
  const loginOpen = ref(false);
  const loading = ref(false);
  const errorMessage = ref('');
  const declineId = ref<number | null>(null);
  const declineReason = ref('');
  const syncTimer = ref<number | null>(null);
  const activeView = ref<DocumentView>('incoming');
  const typeFilter = ref('Все типы');
  const dueFilter = ref('Все сроки');
  const numberSearch = ref('');

  const loginForm = reactive<LoginForm>({
    login: '',
    password: '',
  });

  const documentForm = reactive<DocumentForm>({
    title: '',
    documentNumber: '',
    type: documentTypeOptions[0],
    recipientId: '',
    dueDate: '',
    priority: documentPriorityOptions[1],
    description: '',
  });

  const loadData = async () => {
    if (!currentUser.value) {
      return;
    }

    documents.value = await fetchMyDocuments(currentUser.value.userId);
    employees.value = await fetchEmployees(currentUser.value.userId);
  };

  const syncDocuments = async () => {
    if (!currentUser.value) {
      return;
    }

    documents.value = await fetchMyDocuments(currentUser.value.userId);
  };

  const startSync = () => {
    if (syncTimer.value) {
      window.clearInterval(syncTimer.value);
    }

    syncTimer.value = window.setInterval(syncDocuments, 3000);
  };

  const stopSync = () => {
    if (!syncTimer.value) {
      return;
    }

    window.clearInterval(syncTimer.value);
    syncTimer.value = null;
  };

  const resetDocumentForm = () => {
    documentForm.title = '';
    documentForm.documentNumber = '';
    documentForm.type = documentTypeOptions[0];
    documentForm.recipientId = '';
    documentForm.dueDate = '';
    documentForm.priority = documentPriorityOptions[1];
    documentForm.description = '';
  };

  const login = async () => {
    errorMessage.value = '';
    loading.value = true;

    try {
      const user = await loginRequest(loginForm);
      currentUser.value = user;
      loginOpen.value = false;
      loginForm.password = '';
      await loadData();
      startSync();
    } catch {
      errorMessage.value = 'Неверный логин или пароль';
    } finally {
      loading.value = false;
    }
  };

  const logout = () => {
    stopSync();
    currentUser.value = null;
    documents.value = [];
    employees.value = [];
    loginForm.login = '';
    loginForm.password = '';
    declineId.value = null;
    declineReason.value = '';
  };

  const createDocument = async () => {
    if (!currentUser.value) {
      return;
    }

    await createDocumentRequest(currentUser.value.userId, {
      title: documentForm.title,
      documentNumber: documentForm.documentNumber,
      type: documentForm.type,
      recipientId: Number(documentForm.recipientId),
      dueDate: documentForm.dueDate || null,
      priority: documentForm.priority,
      description: documentForm.description,
    });

    resetDocumentForm();
    await loadData();
  };

  const signDocument = async (documentId: number) => {
    if (!currentUser.value) {
      return;
    }

    if (declineId.value === documentId) {
      declineId.value = null;
      declineReason.value = '';
    }

    await signDocumentRequest(currentUser.value.userId, documentId);
    await loadData();
  };

  const declineDocument = async (documentId: number) => {
    if (!currentUser.value || !declineReason.value.trim()) {
      return;
    }

    await declineDocumentRequest(currentUser.value.userId, documentId, declineReason.value);
    declineId.value = null;
    declineReason.value = '';
    await loadData();
  };

  const recipientOptions = computed(() =>
    employees.value.filter((employee) => employee.id !== currentUser.value?.userId),
  );

  const incomingDocuments = computed(() =>
    documents.value.filter(
      (document) => document.recipient?.id === currentUser.value?.userId && document.status === 'SENT',
    ),
  );

  const outgoingDocuments = computed(() =>
    documents.value.filter(
      (document) => document.sender?.id === currentUser.value?.userId && document.status === 'SENT',
    ),
  );

  const archivedDocuments = computed(() =>
    documents.value.filter(
      (document) =>
        document.status !== 'SENT' &&
        (document.sender?.id === currentUser.value?.userId || document.recipient?.id === currentUser.value?.userId),
    ),
  );

  const pendingIncomingCount = computed(() => incomingDocuments.value.length);

  const documentTypes = computed(() => {
    const types = new Set(documents.value.map((document) => document.type).filter(Boolean));
    return ['Все типы', ...Array.from(types).sort((a, b) => a.localeCompare(b, 'ru'))];
  });

  const selectedDocuments = computed(() => {
    if (activeView.value === 'outgoing') {
      return outgoingDocuments.value;
    }

    if (activeView.value === 'archive') {
      return archivedDocuments.value;
    }

    return incomingDocuments.value;
  });

  const matchesDueFilter = (document: FlowDocument) => {
    if (dueFilter.value === 'Все сроки') {
      return true;
    }

    if (!document.dueDate) {
      return dueFilter.value === 'Без срока';
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const dueDateValue = new Date(document.dueDate);
    dueDateValue.setHours(0, 0, 0, 0);

    const weekLimit = new Date(today);
    weekLimit.setDate(today.getDate() + 7);

    if (dueFilter.value === 'Просрочены') {
      return dueDateValue < today;
    }

    if (dueFilter.value === 'Сегодня') {
      return dueDateValue.getTime() === today.getTime();
    }

    if (dueFilter.value === 'На неделе') {
      return dueDateValue >= today && dueDateValue <= weekLimit;
    }

    return true;
  };

  const filteredDocuments = computed(() => {
    const searchValue = numberSearch.value.trim().toLowerCase();

    return selectedDocuments.value.filter((document) => {
      const matchesType = typeFilter.value === 'Все типы' || document.type === typeFilter.value;
      const matchesNumber = !searchValue || document.documentNumber.toLowerCase().includes(searchValue);
      return matchesType && matchesNumber && matchesDueFilter(document);
    });
  });

  const activeViewTitle = computed(() => {
    const titles: Record<DocumentView, string> = {
      incoming: 'Входящие документы',
      outgoing: 'Исходящие документы',
      archive: 'Архив документов',
    };

    return titles[activeView.value];
  });

  const canAnswer = (document: FlowDocument) =>
    document.status === 'SENT' && document.recipient?.id === currentUser.value?.userId;

  const statusText = (status: string) => {
    const map: Record<string, string> = {
      SENT: 'Ожидает подписи',
      SIGNED: 'Подписан',
      DECLINED: 'Отказано',
      RETURNED_BY_AUDIT: 'Возвращен аудитом',
    };

    return map[status] || status;
  };

  const statusClass = (status: string) => ({
    'status-review': status === 'SENT',
    'status-signed': status === 'SIGNED',
    'status-fix': status === 'DECLINED' || status === 'RETURNED_BY_AUDIT',
  });

  const formatDate = (value?: string) => {
    if (!value) {
      return 'Не указан';
    }

    return new Intl.DateTimeFormat('ru-RU').format(new Date(value));
  };

  onUnmounted(stopSync);

  return {
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
  };
};
