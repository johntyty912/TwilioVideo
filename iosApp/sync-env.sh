#!/bin/bash

# Script to sync .env.local to Config.local.xcconfig
# Run this before building iOS app

ENV_FILE="../.env.local"
CONFIG_FILE="Configuration/Config.local.xcconfig"

if [ ! -f "$ENV_FILE" ]; then
    echo "⚠️  .env.local not found at $ENV_FILE"
    exit 0
fi

echo "🔄 Syncing .env.local to iOS config..."

# Read TWILIO_TOKEN_URL from .env.local
TOKEN_URL=$(grep "^TWILIO_TOKEN_URL=" "$ENV_FILE" | cut -d '=' -f2- | tr -d '"' | tr -d "'")

# Remove https:// prefix if present
TOKEN_URL_CLEAN=$(echo "$TOKEN_URL" | sed 's|^https://||' | sed 's|^http://||')

# Read USER_IDENTITY from .env.local
USER_IDENTITY=$(grep "^TEST_USER_IDENTITY=" "$ENV_FILE" | cut -d '=' -f2- | tr -d '"' | tr -d "'")
if [ -z "$USER_IDENTITY" ]; then
    USER_IDENTITY="user"
fi

# Update Config.local.xcconfig
cat > "$CONFIG_FILE" << EOF
// Local configuration - DO NOT COMMIT TO GIT
// Copy this file to Config.local.xcconfig and fill in your values

TWILIO_TOKEN_URL=$TOKEN_URL_CLEAN
TEST_USER_IDENTITY=$USER_IDENTITY
EOF

echo "✅ Updated $CONFIG_FILE with:"
echo "   TWILIO_TOKEN_URL=$TOKEN_URL_CLEAN"
echo "   TEST_USER_IDENTITY=$USER_IDENTITY" 