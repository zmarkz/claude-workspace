import { FastifyRequest, FastifyReply } from 'fastify';
import { verify } from 'jsonwebtoken';
import { AuthenticationError } from '../utils/errors.js';
import { Config } from '../config.js';

export interface TokenPayload {
  userId: string;
  email: string;
  iat: number;
  exp: number;
}

export async function createAuthMiddleware(config: Config) {
  return async function authPreHandler(
    request: FastifyRequest,
    reply: FastifyReply,
  ): Promise<void> {
    try {
      const authHeader = request.headers.authorization;

      if (!authHeader || !authHeader.startsWith('Bearer ')) {
        throw new AuthenticationError('Missing or invalid authorization header');
      }

      const token = authHeader.slice(7);

      const payload = verify(token, config.JWT_SECRET) as TokenPayload;

      request.user = {
        userId: payload.userId,
        email: payload.email,
      };
    } catch (error) {
      if (error instanceof AuthenticationError) {
        throw error;
      }
      throw new AuthenticationError('Invalid token');
    }
  };
}

declare global {
  namespace Express {
    interface Request {
      user?: {
        userId: string;
        email: string;
      };
    }
  }
}
