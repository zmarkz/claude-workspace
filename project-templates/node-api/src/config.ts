import { z } from 'zod';

const configSchema = z.object({
  DATABASE_URL: z.string().url('DATABASE_URL must be a valid database URL'),
  PORT: z.coerce.number().default(3000),
  LOG_LEVEL: z.enum(['fatal', 'error', 'warn', 'info', 'debug', 'trace']).default('info'),
  JWT_SECRET: z.string().min(32, 'JWT_SECRET must be at least 32 characters'),
  NODE_ENV: z.enum(['development', 'production', 'test']).default('development'),
});

export type Config = z.infer<typeof configSchema>;

export function loadConfig(): Config {
  const env = process.env;

  const result = configSchema.safeParse(env);

  if (!result.success) {
    console.error('Invalid environment configuration:');
    result.error.errors.forEach(error => {
      console.error(`  ${error.path.join('.')}: ${error.message}`);
    });
    throw new Error('Failed to load configuration');
  }

  return result.data;
}
