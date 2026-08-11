import { HttpErrorResponse, HttpEvent, HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { HttpFeedbackMessage } from '../models/http-feedback-message.model';
import { HttpFeedbackService } from './http-feedback.service';

const MUTATING_METHODS = new Set(['POST', 'PUT', 'PATCH', 'DELETE']);
const BUSINESS_CODE_HEADER = 'X-Business-Code';

export const httpFeedbackInterceptor: HttpInterceptorFn = (request, next): Observable<HttpEvent<unknown>> => {
  const feedback = inject(HttpFeedbackService);

  return next(request).pipe(
    tap((event) => {
      if (!(event instanceof HttpResponse)) {
        return;
      }

      if (!shouldNotifySuccess(request.method, event.status)) {
        return;
      }

      if (isSilentSuccessRoute(request.method, request.url)) {
        return;
      }

      feedback.push(buildSuccessMessage(request.method, request.url, event));
    }),
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        feedback.push(buildErrorMessage(request.method, request.url, error));
      }
      return throwError(() => error);
    })
  );
};

function shouldNotifySuccess(method: string, status: number): boolean {
  return MUTATING_METHODS.has(method.toUpperCase()) && status >= 200 && status < 300;
}

function isSilentSuccessRoute(method: string, endpoint: string): boolean {
  const normalizedMethod = method.toUpperCase();

  if ((normalizedMethod === 'PUT' || normalizedMethod === 'PATCH')
    && endpoint.includes('/step-parameters/')) {
    return true;
  }

  if (normalizedMethod === 'POST' && endpoint.includes('/transitions/next')) {
    return true;
  }

  if (normalizedMethod === 'POST' && endpoint.includes('/decision-executions/finalize')) {
    return true;
  }

  if (normalizedMethod === 'POST' && endpoint.includes('/recipe-compatibility/compatible-machines')) {
    return true;
  }

  if (normalizedMethod === 'POST' && endpoint.includes('/parameter-definitions/reorder')) {
    return true;
  }

  if (normalizedMethod === 'POST' && endpoint.includes('/parameter-definitions/move')) {
    return true;
  }

  if (normalizedMethod === 'POST' && endpoint.includes('/parameter-groups/reorder')) {
    return true;
  }

  return false;
}

function buildSuccessMessage(method: string, endpoint: string, response: HttpResponse<unknown>): HttpFeedbackMessage {
  const info = extractBackendInfo(response.body);
  const code = response.headers.get(BUSINESS_CODE_HEADER) ?? info.code ?? null;
  const description = mapSuccessCodeToMessage(code) ?? fallbackSuccessMessage(method);

  return {
    severity: 'success',
    title: 'Success',
    description,
    backendCode: code ?? undefined,
    timestamp: new Date().toISOString()
  };
}

function buildErrorMessage(method: string, endpoint: string, error: HttpErrorResponse): HttpFeedbackMessage {
  const info = extractBackendInfo(error.error);
  const code = error.headers?.get(BUSINESS_CODE_HEADER) ?? info.code ?? null;
  const status = Number.isFinite(error.status) ? error.status : 0;

  const codedMessage = mapErrorCodeToMessage(code, info.description);
  if (codedMessage) {
    return {
      severity: 'error',
      title: 'Action failed',
      description: codedMessage,
      backendCode: code ?? undefined,
      timestamp: new Date().toISOString()
    };
  }

  const backendTextMessage = mapBackendDescriptionToMessage(info.description, status);
  if (backendTextMessage) {
    return {
      severity: 'error',
      title: 'Action failed',
      description: backendTextMessage,
      backendCode: code ?? undefined,
      timestamp: new Date().toISOString()
    };
  }

  if (status === 0) {
    return {
      severity: 'error',
      title: 'Connection failed',
      description: 'The server is unreachable. Check your connection and try again in a moment.',
      timestamp: new Date().toISOString()
    };
  }

  if (status === 400) {
    return {
      severity: 'error',
      title: 'Invalid request',
      description: 'Some provided values are invalid. Please review the form and try again.',
      timestamp: new Date().toISOString()
    };
  }

  if (status === 401 || status === 403) {
    return {
      severity: 'error',
      title: 'Access denied',
      description: 'You do not have permission to perform this action.',
      timestamp: new Date().toISOString()
    };
  }

  if (status === 404) {
    return {
      severity: 'error',
      title: 'Item not found',
      description: 'The requested item was not found or has already been deleted.',
      timestamp: new Date().toISOString()
    };
  }

  if (status === 409) {
    return {
      severity: 'error',
      title: 'Data conflict',
      description: 'This action conflicts with current data. Reload and try again.',
      timestamp: new Date().toISOString()
    };
  }

  if (status >= 500) {
    return {
      severity: 'error',
      title: 'Server error',
      description: 'An internal error occurred. Please try again later.',
      timestamp: new Date().toISOString()
    };
  }

  return {
    severity: 'error',
    title: 'Action failed',
    description: 'The action could not be completed. Please review your data and try again.',
    backendCode: code ?? undefined,
    timestamp: new Date().toISOString()
  };
}

function fallbackSuccessMessage(method: string): string {
  const action = methodToAction(method);
  if (action) {
    return `${action} completed successfully.`;
  }
  return 'Action completed successfully.';
}

function methodToAction(method: string): string | null {
  const normalized = method.toUpperCase();
  if (normalized === 'POST') {
    return 'Create';
  }
  if (normalized === 'PUT' || normalized === 'PATCH') {
    return 'Update';
  }
  if (normalized === 'DELETE') {
    return 'Deletion';
  }
  return null;
}

function mapSuccessCodeToMessage(code: string | null): string | null {
  if (!code) {
    return null;
  }

  const messages: Record<string, string> = {
    RECIPE_CREATED: 'Recipe created successfully.',
    RECIPE_UPDATED: 'Recipe updated successfully.',
    RECIPE_DELETED: 'Recipe deleted successfully.',
    STEP_CREATED: 'Step created successfully.',
    STEP_UPDATED: 'Step updated successfully.',
    STEP_DELETED: 'Step deleted successfully.',
    STEP_PARAMETER_CREATED: 'Step parameter added successfully.',
    STEP_PARAMETER_UPDATED: 'Step parameter updated successfully.',
    STEP_PARAMETER_DELETED: 'Step parameter deleted successfully.',
    PARAMETER_DEFINITION_CREATED: 'Parameter definition created successfully.',
    PARAMETER_DEFINITION_UPDATED: 'Parameter definition updated successfully.',
    PARAMETER_DEFINITION_DELETED: 'Parameter definition deleted successfully.',
    PARAMETER_OPTION_CREATED: 'Parameter option created successfully.',
    PARAMETER_OPTION_UPDATED: 'Parameter option updated successfully.',
    PARAMETER_OPTION_DELETED: 'Parameter option deleted successfully.',
    PARAMETER_DEPENDENCY_RULE_CREATED: 'Dependency rule created successfully.',
    PARAMETER_DEPENDENCY_RULE_UPDATED: 'Dependency rule updated successfully.',
    PARAMETER_DEPENDENCY_RULE_DELETED: 'Dependency rule deleted successfully.',
    RESOURCE_CREATED: 'Item created successfully.',
    RESOURCE_UPDATED: 'Item updated successfully.',
    RESOURCE_DELETED: 'Item deleted successfully.',
    REQUEST_SUCCEEDED: 'Action completed successfully.'
  };

  return messages[code] ?? null;
}

function mapErrorCodeToMessage(code: string | null, backendDescription?: string): string | null {
  if (!code) {
    return null;
  }

  const messages: Record<string, string> = {
    RECIPE_HAS_DERIVATIVES: 'This recipe is used by derived recipes. Remove derived recipes first, then try again.',
    PARAMETER_OPTION_IN_USE: 'This option is already used in a recipe. Update dependent values first, then try again.',
    PARAMETER_DEFINITION_IN_USE: 'This parameter is already used in a recipe. Remove its usage first, then try again.',
    PARAMETER_DEPENDENCY_RULE_CONFLICT: 'This dependency rule is invalid or already exists. Check source, trigger option, and target, then try again.',
    RESOURCE_NOT_FOUND: 'The requested item was not found or has already been deleted.',
    VALIDATION_ERROR: 'Some provided values are invalid. Please review the form and try again.',
    BUSINESS_CONFLICT: backendDescription?.trim() || 'This action is not allowed with the current data state.',
    INTERNAL_ERROR: 'An internal error occurred. Please try again later.'
  };

  return messages[code] ?? null;
}

function mapBackendDescriptionToMessage(description: string | undefined, status: number): string | null {
  if (!description || description.trim().length === 0) {
    return null;
  }

  const normalized = description.toLowerCase();

  if (
    normalized.includes('duplicate entry')
    || normalized.includes('already exists')
    || normalized.includes('uq_pdr_source_trigger_required_target')
  ) {
    return 'This dependency rule already exists. Choose another source/trigger/context/target combination.';
  }

  if (
    normalized.includes('deadlock')
    || normalized.includes('sqlstate: 40001')
    || normalized.includes('error: 1213')
  ) {
    return 'A temporary database lock conflict occurred. Please retry the action.';
  }

  if (
    normalized.includes('record has changed since last read')
    || normalized.includes('error: 1020')
  ) {
    return 'Data changed while saving. Please reload the page and try again.';
  }

  if (status === 409) {
    return 'This action conflicts with current data. Reload and try again.';
  }

  return null;
}

function extractBackendInfo(payload: unknown): { description?: string; code?: string; details?: string } {
  if (payload === null || payload === undefined) {
    return {};
  }

  if (typeof payload === 'string') {
    return {
      description: payload,
      details: payload
    };
  }

  if (typeof payload !== 'object') {
    return {
      description: String(payload),
      details: String(payload)
    };
  }

  const record = payload as Record<string, unknown>;
  const description = pickFirstString(record, ['message', 'error', 'description', 'detail', 'title']);
  const code = pickFirstValue(record, ['code', 'errorCode', 'statusCode', 'backendCode']);

  let details: string | undefined;
  try {
    details = JSON.stringify(payload, null, 2);
  } catch {
    details = undefined;
  }

  return {
    description,
    code,
    details
  };
}

function pickFirstString(record: Record<string, unknown>, keys: string[]): string | undefined {
  for (const key of keys) {
    const value = record[key];
    if (typeof value === 'string' && value.trim().length > 0) {
      return value;
    }
  }
  return undefined;
}

function pickFirstValue(record: Record<string, unknown>, keys: string[]): string | undefined {
  for (const key of keys) {
    const value = record[key];
    if (typeof value === 'string' && value.trim().length > 0) {
      return value;
    }
    if (typeof value === 'number' && Number.isFinite(value)) {
      return String(value);
    }
  }
  return undefined;
}
