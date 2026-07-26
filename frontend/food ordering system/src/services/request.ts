import type {
  AxiosResponse,
} from "axios";
import axios from "axios";

import {
  type ApiErrorResponse,
  getApiErrorMessage,
} from "../utils/apiError";

export class ApiRequestError extends Error {
  status?: number;
  retryAfterSeconds?: number;
  isNetworkError: boolean;

  constructor(
    message: string,
    options: {
      status?: number;
      retryAfterSeconds?: number;
      isNetworkError?: boolean;
    } = {}
  ) {
    super(message);
    this.name = "ApiRequestError";
    this.status = options.status;
    this.retryAfterSeconds =
      options.retryAfterSeconds;
    this.isNetworkError =
      options.isNetworkError ?? false;
  }
}

export function toApiRequestError(
  error: unknown,
  fallbackMessage: string
): ApiRequestError {
  const message =
    getApiErrorMessage(error)
    || fallbackMessage;

  if (
    axios.isAxiosError<
      ApiErrorResponse
    >(error)
  ) {
    const retryAfter =
      Number(
        error.response
          ?.headers?.[
            "retry-after"
          ]
      );

    return new ApiRequestError(
      message,
      {
        status:
          error.response?.status,
        retryAfterSeconds:
          Number.isFinite(retryAfter)
          && retryAfter > 0
            ? retryAfter
            : undefined,
        isNetworkError:
          !error.response,
      }
    );
  }

  return new ApiRequestError(
    message
  );
}

export async function requestData<T>(
  action: () => Promise<AxiosResponse<T>>,
  fallbackMessage: string
): Promise<T> {
  try {
    const response =
      await action();

    return response.data;
  } catch (error) {
    throw toApiRequestError(
      error,
      fallbackMessage
    );
  }
}

export async function requestVoid(
  action: () => Promise<AxiosResponse<unknown>>,
  fallbackMessage: string
): Promise<void> {
  try {
    await action();
  } catch (error) {
    throw toApiRequestError(
      error,
      fallbackMessage
    );
  }
}
