# Manual Redeployment on AWS Elastic Beanstalk

## Option 1: Using AWS Console (Easiest)

1. **Log in to AWS Console**
   - Go to https://console.aws.amazon.com
   - Search for "Elastic Beanstalk"

2. **Select Your Environment**
   - Click on your environment (e.g., `thecuriositycorner-env`)

3. **Redeploy Latest Version**
   - Click the **"Deploy"** button (top right, or in the Actions dropdown)
   - Choose **"Deploy the last version uploaded"** or **"Upload and deploy"**
   - If uploading a new JAR:
     - Click **"Upload and Deploy"**
     - Select your `target/catchy-0.0.1-SNAPSHOT.jar` file
   - Click **"Deploy"**

4. **Monitor Deployment**
   - Watch the "Recent Deployments" section
   - Status will change: `Uploading` → `Building` → `Deploying` → `Updating` → `Ready`
   - Takes 2-5 minutes typically

---

## Option 2: Using AWS CLI (Automated)

### Prerequisites
- AWS CLI installed: https://aws.amazon.com/cli/
- Configured with credentials: `aws configure`

### Steps

```bash
# 1. Build the JAR locally (if needed)
cd C:\Users\HP\Downloads\catchy\catchy
mvn clean package -DskipTests

# 2. Create a version label (unique identifier)
$VERSION="catchy-$(Get-Date -Format 'yyyyMMdd-HHmmss')"

# 3. Upload JAR to Elastic Beanstalk S3 bucket
# (Replace BUCKET_NAME and REGION with your actual values)
aws s3 cp target/catchy-0.0.1-SNAPSHOT.jar `
  s3://elasticbeanstalk-us-east-1-430118812195/catchy/$VERSION.jar

# 4. Create an application version
aws elasticbeanstalk create-app-version `
  --application-name catchy `
  --version-label $VERSION `
  --source-bundle S3Bucket=elasticbeanstalk-us-east-1-430118812195,S3Key=catchy/$VERSION.jar `
  --region us-east-1

# 5. Update the environment to use the new version
aws elasticbeanstalk update-environment `
  --environment-name thecuriositycorner-env `
  --version-label $VERSION `
  --region us-east-1

# 6. Monitor deployment (optional)
aws elasticbeanstalk describe-environment-health `
  --environment-name thecuriositycorner-env `
  --attribute-keys All `
  --region us-east-1
```

---

## Option 3: Automatic Deploy on Git Push

If you want deployments to trigger automatically when you push to `main`:

1. **In AWS Elastic Beanstalk Console:**
   - Go to your environment
   - Click **"Configuration"**
   - Under "Deployment", enable **"Auto-deploy when repository branch is updated"**
   - Select branch: `main`
   - Configure webhook (Elastic Beanstalk will guide you)

2. **In GitHub Repository Settings:**
   - Go to Settings → Webhooks
   - Add webhook for AWS Elastic Beanstalk
   - Elastic Beanstalk will provide the webhook URL

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Deployment fails to start | Check IAM permissions for your AWS user |
| App crashes after deploy | Check logs: Environment → Logs → Request logs |
| Database connection issues | Verify RDS security group allows Elastic Beanstalk instance access |
| JAR file not found | Ensure `target/catchy-0.0.1-SNAPSHOT.jar` exists after `mvn package` |

---

## Quick Reference: Find Your EB Details

```powershell
# List all Elastic Beanstalk environments
aws elasticbeanstalk describe-environments --region us-east-1

# Get environment status
aws elasticbeanstalk describe-environments `
  --environment-names thecuriositycorner-env `
  --region us-east-1 `
  --query 'Environments[0].[EnvironmentName,Status,CNAME,VersionLabel]' `
  --output table
```

---

## Summary

- **Easiest:** Use AWS Console → Deploy button
- **Fastest for CI/CD:** Use AWS CLI or enable auto-deploy on Git push
- **Current fix:** The database schema fix is already in `main` branch, so next deployment will auto-create tables
