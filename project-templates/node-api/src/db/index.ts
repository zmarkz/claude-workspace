import { Pool } from 'pg';
import { drizzle } from 'drizzle-orm/node-postgres';
import * as schema from './schema.js';

let pool: Pool | null = null;

export async function initializeDatabase(databaseUrl: string) {
  pool = new Pool({
    connectionString: databaseUrl,
  });

  // Verify connection
  const client = await pool.connect();
  await client.query('SELECT 1');
  client.release();

  return getDatabase();
}

export function getDatabase() {
  if (!pool) {
    throw new Error('Database not initialized. Call initializeDatabase first.');
  }
  return drizzle(pool, { schema });
}

export async function closeDatabase() {
  if (pool) {
    await pool.end();
    pool = null;
  }
}

export { schema };
