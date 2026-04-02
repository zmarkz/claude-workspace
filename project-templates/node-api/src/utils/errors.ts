import { FastifyReply, FastifyRequest } from 'fastify';
import { getLogger } from './logger.js';

export class AppError extends Error {
  constructor(
    public statusCode: number,
    public code: string,
    message: string,
  ) {
    super(message);
    Object.setPrototypeOf(this, AppError.prototype);
  }
}

export class ValidationError extends AppError {
  constructor(message: string) {
    super(400, 'VALIDATION_ERROR', message);
    Object.setPrototypeOf(this, ValidationError.prototype);
  }
}

export class AuthenticationError extends AppError {
  constructor(message: string = 'Unauthorized') {
    super(401, 'AUTHENTICATION_ERROR', message);
    Object.setPrototypeOf(this, AuthenticationError.prototype);
  }
}

export class AuthorizationError extends AppError {
  constructor(message: string = 'Forbidden') {
    super(403, 'AUTHORIZATION_ERROR', message);
    Object.setPrototypeOf(this, AuthorizationError.prototype);
  }
}

export class NotFoundError extends AppError {
  constructor(resource: string) {
    super(404, 'NOT_FOUND', `${resource} not found`);
    Object.setPrototypeOf(this, NotFoundError.prototype);
  }
}

export class ConflictError extends AppError {
  constructor(message: string) {
    super(409, 'CONFLICT', message);
    Object.setPrototypeOf(this, ConflictError.prototype);
  }
}

export async function errorHandler(
  error: Error,
  _request: FastifyRequest,
  reply: FastifyReply,
): Promise<void> {
  const logger = getLogger();

  if (error instanceof AppError) {
    logger.error(
      {
        code: error.code,
        statusCode: error.statusCode,
      },
      error.message,
    );

    await reply.code(error.statusCode).send({
      error: {
        code: error.code,
        message: error.message,
      },
    });
  } else {
    logger.error(error, 'Unexpected error');

    await reply.code(500).send({
      error: {
        code: 'INTERNAL_SERVER_ERROR',
        message: 'An unexpected error occurred',
      },
    });
  }
}
