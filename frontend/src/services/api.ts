import type { CurrentUser, Employee, FlowDocument, LoginForm } from '../types/documents';

const createHeaders = (userId?: number, headers?: HeadersInit) => {
  const requestHeaders = new Headers(headers);
  requestHeaders.set('Content-Type', 'application/json');

  if (userId) {
    requestHeaders.set('X-User-Id', String(userId));
  }

  return requestHeaders;
};

const request = async <T>(url: string, options: RequestInit = {}, userId?: number) => {
  const response = await fetch(url, {
    ...options,
    headers: createHeaders(userId, options.headers),
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || 'Ошибка запроса');
  }

  return response.json() as Promise<T>;
};

export const loginRequest = (payload: LoginForm) =>
  request<CurrentUser>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload),
  });

export const fetchMyDocuments = (userId: number) => request<FlowDocument[]>('/api/documents/my', {}, userId);

export const fetchEmployees = (userId: number) => request<Employee[]>('/api/auth/employees', {}, userId);

export const createDocumentRequest = (userId: number, payload: Record<string, unknown>) =>
  request<FlowDocument>(
    '/api/documents/my',
    {
      method: 'POST',
      body: JSON.stringify(payload),
    },
    userId,
  );

export const signDocumentRequest = (userId: number, documentId: number) =>
  request<FlowDocument>(`/api/documents/${documentId}/sign`, { method: 'POST' }, userId);

export const declineDocumentRequest = (userId: number, documentId: number, reason: string) =>
  request<FlowDocument>(
    `/api/documents/${documentId}/decline`,
    {
      method: 'POST',
      body: JSON.stringify({ reason }),
    },
    userId,
  );
