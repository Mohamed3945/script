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

  return false;
}

function buildSuccessMessage(method: string, endpoint: string, response: HttpResponse<unknown>): HttpFeedbackMessage {
  const info = extractBackendInfo(response.body);
  const code = response.headers.get(BUSINESS_CODE_HEADER) ?? info.code ?? null;
  const description = mapSuccessCodeToMessage(code) ?? fallbackSuccessMessage(method);

  return {
    severity: 'success',
    title: 'Succes',
    description,
    backendCode: code ?? undefined,
    timestamp: new Date().toISOString()
  };
}

function buildErrorMessage(method: string, endpoint: string, error: HttpErrorResponse): HttpFeedbackMessage {
  const info = extractBackendInfo(error.error);
  const code = error.headers?.get(BUSINESS_CODE_HEADER) ?? info.code ?? null;
  const status = Number.isFinite(error.status) ? error.status : 0;

  const codedMessage = mapErrorCodeToMessage(code);
  if (codedMessage) {
    return {
      severity: 'error',
      title: 'Operation impossible',
      description: codedMessage,
      backendCode: code ?? undefined,
      timestamp: new Date().toISOString()
    };
  }

  if (status === 0) {
    return {
      severity: 'error',
      title: 'Connexion impossible',
      description: 'Le serveur est injoignable. Verifiez votre connexion ou reessayez dans quelques instants.',
      timestamp: new Date().toISOString()
    };
  }

  if (status === 400) {
    return {
      severity: 'error',
      title: 'Demande invalide',
      description: 'Certaines informations saisies sont invalides. Verifiez les champs et reessayez.',
      timestamp: new Date().toISOString()
    };
  }

  if (status === 401 || status === 403) {
    return {
      severity: 'error',
      title: 'Acces refuse',
      description: 'Vous n\'avez pas les droits necessaires pour effectuer cette action.',
      timestamp: new Date().toISOString()
    };
  }

  if (status === 404) {
    return {
      severity: 'error',
      title: 'Element introuvable',
      description: 'L\'element demande n\'a pas ete trouve ou a deja ete supprime.',
      timestamp: new Date().toISOString()
    };
  }

  if (status >= 500) {
    return {
      severity: 'error',
      title: 'Erreur serveur',
      description: 'Une erreur interne est survenue. Veuillez reessayer plus tard.',
      timestamp: new Date().toISOString()
    };
  }

  return {
    severity: 'error',
    title: 'Operation impossible',
    description: 'L\'operation n\'a pas pu etre effectuee. Veuillez verifier vos donnees puis reessayer.',
    backendCode: code ?? undefined,
    timestamp: new Date().toISOString()
  };
}

function fallbackSuccessMessage(method: string): string {
  const action = methodToAction(method);
  if (action) {
    return `${action} effectuee avec succes.`;
  }
  return 'Operation effectuee avec succes.';
}

function methodToAction(method: string): string | null {
  const normalized = method.toUpperCase();
  if (normalized === 'POST') {
    return 'Creation';
  }
  if (normalized === 'PUT' || normalized === 'PATCH') {
    return 'Mise a jour';
  }
  if (normalized === 'DELETE') {
    return 'Suppression';
  }
  return null;
}

function mapSuccessCodeToMessage(code: string | null): string | null {
  if (!code) {
    return null;
  }

  const messages: Record<string, string> = {
    RECIPE_CREATED: 'Recette creee avec succes.',
    RECIPE_UPDATED: 'Recette mise a jour avec succes.',
    RECIPE_DELETED: 'Recette supprimee avec succes.',
    STEP_CREATED: 'Etape creee avec succes.',
    STEP_UPDATED: 'Etape mise a jour avec succes.',
    STEP_DELETED: 'Etape supprimee avec succes.',
    STEP_PARAMETER_CREATED: 'Parametre d\'etape ajoute avec succes.',
    STEP_PARAMETER_UPDATED: 'Parametre d\'etape mis a jour avec succes.',
    STEP_PARAMETER_DELETED: 'Parametre d\'etape supprime avec succes.',
    PARAMETER_DEFINITION_CREATED: 'Definition de parametre creee avec succes.',
    PARAMETER_DEFINITION_UPDATED: 'Definition de parametre mise a jour avec succes.',
    PARAMETER_DEFINITION_DELETED: 'Definition de parametre supprimee avec succes.',
    PARAMETER_OPTION_CREATED: 'Option de parametre creee avec succes.',
    PARAMETER_OPTION_UPDATED: 'Option de parametre mise a jour avec succes.',
    PARAMETER_OPTION_DELETED: 'Option de parametre supprimee avec succes.',
    RESOURCE_CREATED: 'Element cree avec succes.',
    RESOURCE_UPDATED: 'Element mis a jour avec succes.',
    RESOURCE_DELETED: 'Element supprime avec succes.',
    REQUEST_SUCCEEDED: 'Operation effectuee avec succes.'
  };

  return messages[code] ?? null;
}

function mapErrorCodeToMessage(code: string | null): string | null {
  if (!code) {
    return null;
  }

  const messages: Record<string, string> = {
    RECIPE_HAS_DERIVATIVES: 'Cette recette est utilisee par d\'autres recettes derivees. Supprimez d\'abord les derivees puis reessayez.',
    PARAMETER_OPTION_IN_USE: 'Cette option est deja utilisee dans une recette. Modifiez d\'abord les valeurs qui l\'utilisent puis reessayez.',
    PARAMETER_DEFINITION_IN_USE: 'Ce parametre est deja utilise dans une recette. Retirez d\'abord son utilisation puis reessayez.',
    RESOURCE_NOT_FOUND: 'L\'element demande est introuvable ou a deja ete supprime.',
    VALIDATION_ERROR: 'Certaines informations saisies sont invalides. Verifiez les champs et reessayez.',
    BUSINESS_CONFLICT: 'Cette action est impossible dans l\'etat actuel des donnees.',
    INTERNAL_ERROR: 'Une erreur interne est survenue. Veuillez reessayer plus tard.'
  };

  return messages[code] ?? null;
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
