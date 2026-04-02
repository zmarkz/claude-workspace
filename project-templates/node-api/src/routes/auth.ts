import { FastifyInstance } from 'fastify';
import { z } from 'zod';
import { hash, compare } from 'bcryptjs';
import { sign } from 'jsonwebtoken';
import { eq } from 'drizzle-orm';
import { getDatabase, schema } from '../db/index.js';
import { Config } from '../config.js';
import {
  ValidationError,
  AuthenticationError,
  ConflictError,
} from '../utils/errors.js';

const registerSchema = z.object({
  email: z.string().email('Invalid email address'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
});

const loginSchema = z.object({
  email: z.string().email('Invalid email address'),
  password: z.string().min(1, 'Password is required'),
});

export interface AuthResponse {
  token: string;
  user: {
    id: string;
    email: string;
  };
}

export async function registerAuthRoutes(
  fastify: FastifyInstance,
  config: Config,
): Promise<void> {
  const db = getDatabase();

  fastify.post<{ Body: unknown; Reply: AuthResponse }>(
    '/auth/register',
    async (request, reply) => {
      let body: z.infer<typeof registerSchema>;

      try {
        body = registerSchema.parse(request.body);
      } catch (error) {
        if (error instanceof z.ZodError) {
          throw new ValidationError(error.errors[0].message);
        }
        throw error;
      }

      const existing = await db.query.users.findFirst({
        where: eq(schema.users.email, body.email),
      });

      if (existing) {
        throw new ConflictError('User with this email already exists');
      }

      const passwordHash = await hash(body.password, 10);

      const [user] = await db
        .insert(schema.users)
        .values({
          email: body.email,
          passwordHash,
        })
        .returning();

      const token = sign(
        {
          userId: user.id,
          email: user.email,
        },
        config.JWT_SECRET,
        {
          expiresIn: '24h',
        },
      );

      return reply.code(201).send({
        token,
        user: {
          id: user.id,
          email: user.email,
        },
      });
    },
  );

  fastify.post<{ Body: unknown; Reply: AuthResponse }>(
    '/auth/login',
    async (request, reply) => {
      let body: z.infer<typeof loginSchema>;

      try {
        body = loginSchema.parse(request.body);
      } catch (error) {
        if (error instanceof z.ZodError) {
          throw new ValidationError(error.errors[0].message);
        }
        throw error;
      }

      const user = await db.query.users.findFirst({
        where: eq(schema.users.email, body.email),
      });

      if (!user) {
        throw new AuthenticationError('Invalid credentials');
      }

      const isValid = await compare(body.password, user.passwordHash);

      if (!isValid) {
        throw new AuthenticationError('Invalid credentials');
      }

      const token = sign(
        {
          userId: user.id,
          email: user.email,
        },
        config.JWT_SECRET,
        {
          expiresIn: '24h',
        },
      );

      return reply.send({
        token,
        user: {
          id: user.id,
          email: user.email,
        },
      });
    },
  );
}
