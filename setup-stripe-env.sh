#!/bin/bash
# Catchy Payment Integration - Environment Setup Script for Linux/Mac
# This script sets up the required environment variables for Stripe payment integration

echo ""
echo "========================================"
echo "Catchy Payment Integration Setup"
echo "========================================"
echo ""
echo "This script will configure Stripe API keys as environment variables."
echo ""

# Prompt user for Stripe keys
read -p "Enter your Stripe SECRET KEY (sk_test_xxx): " SECRET_KEY
read -p "Enter your Stripe PUBLISHABLE KEY (pk_test_xxx): " PUBLISHABLE_KEY
read -p "Enter your Stripe WEBHOOK SECRET (whsec_xxx): " WEBHOOK_SECRET

# Validate that keys are not empty
if [ -z "$SECRET_KEY" ]; then
    echo "ERROR: Secret key cannot be empty!"
    exit 1
fi

if [ -z "$PUBLISHABLE_KEY" ]; then
    echo "ERROR: Publishable key cannot be empty!"
    exit 1
fi

if [ -z "$WEBHOOK_SECRET" ]; then
    echo "WARNING: Webhook secret is empty. You can configure it later."
fi

# Export environment variables for current session
export STRIPE_SECRET_KEY="$SECRET_KEY"
export STRIPE_PUBLISHABLE_KEY="$PUBLISHABLE_KEY"
export STRIPE_WEBHOOK_SECRET="$WEBHOOK_SECRET"

# Add to shell profile for persistence
SHELL_PROFILE=""
if [ -f ~/.bash_profile ]; then
    SHELL_PROFILE=~/.bash_profile
elif [ -f ~/.bashrc ]; then
    SHELL_PROFILE=~/.bashrc
elif [ -f ~/.zshrc ]; then
    SHELL_PROFILE=~/.zshrc
fi

if [ ! -z "$SHELL_PROFILE" ]; then
    echo "" >> "$SHELL_PROFILE"
    echo "# Stripe Payment Integration" >> "$SHELL_PROFILE"
    echo "export STRIPE_SECRET_KEY=\"$SECRET_KEY\"" >> "$SHELL_PROFILE"
    echo "export STRIPE_PUBLISHABLE_KEY=\"$PUBLISHABLE_KEY\"" >> "$SHELL_PROFILE"
    echo "export STRIPE_WEBHOOK_SECRET=\"$WEBHOOK_SECRET\"" >> "$SHELL_PROFILE"
fi

echo ""
echo "========================================"
echo "✓ Environment Variables Configured"
echo "========================================"
echo ""
echo "Stripe Configuration:"
echo "- SECRET_KEY: ${SECRET_KEY:0:20}..."
echo "- PUBLISHABLE_KEY: ${PUBLISHABLE_KEY:0:20}..."
echo "- WEBHOOK_SECRET: ${WEBHOOK_SECRET:0:20}..."
echo ""
echo "Configuration saved to: $SHELL_PROFILE"
echo ""
echo "Next Steps:"
echo "1. Source your shell profile: source $SHELL_PROFILE"
echo "2. Run: mvn clean install"
echo "3. Run: mvn spring-boot:run"
echo "4. Test at: http://localhost:8080/checkout"
echo ""
echo "Use test card: 4242 4242 4242 4242"
echo ""
