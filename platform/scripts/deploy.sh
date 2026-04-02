#!/bin/bash
set -e

# Deploy script for platform orchestration
# Usage: ./scripts/deploy.sh [EC2_HOST]
# Or set EC2_HOST in .env.prod

if [ -z "$1" ]; then
  # Try to read from .env.prod
  if [ -f ".env.prod" ]; then
    EC2_HOST=$(grep "^EC2_HOST=" .env.prod | cut -d '=' -f2)
  fi
fi

if [ -z "$EC2_HOST" ]; then
  echo "Error: EC2_HOST not provided and not found in .env.prod"
  echo "Usage: ./scripts/deploy.sh <EC2_HOST>"
  exit 1
fi

# Load environment
if [ ! -f ".env.prod" ]; then
  echo "Error: .env.prod not found"
  exit 1
fi

set -a
source .env.prod
set +a

EC2_USER=${EC2_USER:-ubuntu}
EC2_KEY_PATH=${EC2_KEY_PATH:-.pem}

echo "Deploying to $EC2_HOST..."

# SSH into EC2 and deploy
ssh -i "$EC2_KEY_PATH" "$EC2_USER@$EC2_HOST" << 'EOF'
  set -e
  cd ~/platform || exit 1

  echo "Pulling latest code..."
  git pull origin main

  echo "Building and starting services..."
  docker compose -f docker-compose.prod.yml up -d --build

  echo "Waiting for services to be healthy..."
  sleep 10

  echo "Service status:"
  docker compose -f docker-compose.prod.yml ps

  echo "Deployment complete!"
EOF

echo "Deploy successful!"
