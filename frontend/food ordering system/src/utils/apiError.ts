import axios from "axios";

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;

  fieldErrors: Record<
    string,
    string
  >;
}

export function getApiErrorMessage(
  error: unknown
): string {

  if (
    axios.isAxiosError<
      ApiErrorResponse
    >(error)
  ) {

    if (!error.response) {
      return "We could not complete the request. Please try again in a moment.";
    }

    return (
      error.response
        ?.data
        ?.message
      ?? "The request could not be completed."
    );
  }

  if (
    error instanceof Error
    && error.message.trim()
  ) {
    return error.message;
  }

  return "An unexpected error occurred.";
}

export function getApiFieldErrors(
  error: unknown
): Record<string, string> {

  if (
    axios.isAxiosError<
      ApiErrorResponse
    >(error)
  ) {

    if (!error.response) {
      return {};
    }

    return (
      error.response
        ?.data
        ?.fieldErrors
      ?? {}
    );
  }

  return {};
}
