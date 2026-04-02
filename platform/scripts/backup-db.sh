#!/bin/bash
set -e

# Database backup script
# Creates timestamped backups of MySQL and PostgreSQL databases
# Usage: ./scripts/backup-db.sh [--upload-s3]

BACKUP_DIR="./backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
MYSQL_BACKUP="$BACKUP_DIR/mysql_backup_$TIMESTAMP.sql"
PG_BACKUP="$BACKUP_DIR/postgres_backup_$TIMESTAMP.sql"

# Create backup directory
mkdir -p "$BACKUP_DIR"

echo "Starting database backups..."

# Backup MySQL
echo "Backing up MySQL..."
docker compose exec -T mysql mysqldump -u dev -pdevpass --all-databases > "$MYSQL_BACKUP"
echo "MySQL backup saved to $MYSQL_BACKUP"

# Backup PostgreSQL
echo "Backing up PostgreSQL..."
docker compose exec -T postgres pg_dump -U dev mcp_farm > "$PG_BACKUP"
echo "PostgreSQL backup saved to $PG_BACKUP"

# Optional S3 upload
if [ "$1" == "--upload-s3" ]; then
  if [ -z "$AWS_S3_BUCKET" ]; then
    echo "Error: AWS_S3_BUCKET not set"
    exit 1
  fi

  echo "Uploading to S3..."
  aws s3 cp "$MYSQL_BACKUP" "s3://$AWS_S3_BUCKET/backups/mysql_backup_$TIMESTAMP.sql"
  aws s3 cp "$PG_BACKUP" "s3://$AWS_S3_BUCKET/backups/postgres_backup_$TIMESTAMP.sql"
  echo "Backups uploaded to S3"
fi

echo "Backup complete!"
