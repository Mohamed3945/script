export type HttpFeedbackSeverity = 'success' | 'error' | 'info';

export interface HttpFeedbackMessage {
  severity: HttpFeedbackSeverity;
  title: string;
  description: string;
  httpStatus?: number;
  backendCode?: string;
  method?: string;
  endpoint?: string;
  details?: string;
  timestamp: string;
}
