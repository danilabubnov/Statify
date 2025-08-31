export interface CoreErrorResponse {
  timestamp: string;
  message: string;
  errors?: Record<string, string>;
}
