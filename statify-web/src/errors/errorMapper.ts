import type { AxiosError } from 'axios'
import { errorMap } from './errorMap'
import type { CoreErrorResponse } from '../api/CoreErrorResponse'

export function mapErrorMessage(e: unknown): string {
  const error = e as AxiosError<CoreErrorResponse>
  const message = error?.response?.data?.message
  const errors = error?.response?.data?.errors

  if (message === 'Validation failed' && errors) {
    return Object.values(errors)[0] ?? 'Validation failed'
  }

  return errorMap[message ?? ''] ?? message ?? 'Something went wrong'
}