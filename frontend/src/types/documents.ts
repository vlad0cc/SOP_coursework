export interface Employee {
  id: number;
  fullName: string;
  position: string;
}

export interface CurrentUser {
  login: string;
  userId: number;
  fullName: string;
  position: string;
}

export interface FlowDocument {
  id: number;
  title: string;
  documentNumber: string;
  type: string;
  sender: Employee;
  recipient: Employee;
  createdAt: string;
  dueDate: string;
  priority: string;
  description: string;
  status: string;
  auditComment?: string;
  declineReason?: string;
  signature?: string;
}

export interface LoginForm {
  login: string;
  password: string;
}

export interface DocumentForm {
  title: string;
  documentNumber: string;
  type: string;
  recipientId: string;
  dueDate: string;
  priority: string;
  description: string;
}

export type DocumentView = 'incoming' | 'outgoing' | 'archive';
