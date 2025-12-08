# Deploying `catchy` to Elastic Beanstalk (Java SE)

Quick steps:

1. Build the jar:

```powershell
.\mvnw -DskipTests package
```

2. Package the app for Elastic Beanstalk:

```powershell
.\scripts\package-eb.ps1
```

3. Install EB CLI and initialize an app (one-time):

```bash
pip install --user awsebcli
eb init --platform "Java SE" --region us-east-1
```

4. Create an environment and deploy:

```bash
eb create catchy-env --single --instance_type t3.small
eb deploy
```

Configuration notes:
- Use `eb setenv` or the Elastic Beanstalk console to set DB and mail environment variables.
- If Java 21 is not available on the selected EB platform, choose a Docker platform and use the included `Dockerfile`.
