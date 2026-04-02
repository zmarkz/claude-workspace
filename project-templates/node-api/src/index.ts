import fastify from 'fastify';
import cors from '@fastify/cors';
import swagger from '@fastify/swagger';
import swaggerUI from '@fastify/swagger-ui';
import { loadConfig } from './config.js';
import { initializeLogger, getLogger } from './utils/logger.js';
import { errorHandler } from './utils/errors.js';
import { initializeDatabase, closeDatabase } from './db/index.js';
import { registerHealthRoutes } from './routes/health.js';
import { registerAuthRoutes } from './routes/auth.js';
import { createAuthMiddleware } from './middleware/auth.js';

async function start() {
  const config = loadConfig();
  const logger = initializeLogger(config);

  const app = fastify({
    logger: true,
  });

  try {
    // Initialize database
    await initializeDatabase(config.DATABASE_URL);
    logger.info('Database connected');

    // Register plugins
    await app.register(cors, {
      origin: true,
    });

    await app.register(swagger, {
      swagger: {
        info: {
          title: 'API Documentation',
          version: '1.0.0',
        },
        schemes: ['http', 'https'],
        consumes: ['application/json'],
        produces: ['application/json'],
      },
    });

    await app.register(swaggerUI, {
      routePrefix: '/docs',
    });

    // Register routes
    await registerHealthRoutes(app);
    await registerAuthRoutes(app, config);

    // Error handler
    app.setErrorHandler(errorHandler);

    // Graceful shutdown
    process.on('SIGTERM', async () => {
      logger.info('SIGTERM received, shutting down gracefully');
      await app.close();
      await closeDatabase();
      process.exit(0);
    });

    process.on('SIGINT', async () => {
      logger.info('SIGINT received, shutting down gracefully');
      await app.close();
      await closeDatabase();
      process.exit(0);
    });

    // Start server
    await app.listen({ port: config.PORT, host: '0.0.0.0' });
    logger.info(`Server running on http://0.0.0.0:${config.PORT}`);
    logger.info(`Swagger docs available at http://0.0.0.0:${config.PORT}/docs`);
  } catch (error) {
    logger.error(error, 'Failed to start server');
    process.exit(1);
  }
}

start();
