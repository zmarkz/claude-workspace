import { FastifyInstance } from 'fastify';

export interface HealthResponse {
  status: string;
  timestamp: string;
}

export async function registerHealthRoutes(fastify: FastifyInstance): Promise<void> {
  fastify.get<{ Reply: HealthResponse }>('/health', async (_request, reply) => {
    return reply.send({
      status: 'ok',
      timestamp: new Date().toISOString(),
    });
  });
}
