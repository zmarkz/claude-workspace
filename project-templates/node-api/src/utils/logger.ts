import pino from 'pino';
import { Config } from '../config.js';

let logger: pino.Logger;

export function initializeLogger(config: Config): pino.Logger {
  logger = pino({
    level: config.LOG_LEVEL,
    transport:
      config.NODE_ENV === 'development'
        ? {
            target: 'pino-pretty',
            options: {
              colorize: true,
              translateTime: 'SYS:standard',
              ignore: 'pid,hostname',
            },
          }
        : undefined,
  });

  return logger;
}

export function getLogger(): pino.Logger {
  if (!logger) {
    throw new Error('Logger not initialized. Call initializeLogger first.');
  }
  return logger;
}
