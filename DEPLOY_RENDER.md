# Deploying to Render

Steps to deploy this Spring Boot application to Render using the provided `render.yaml` and `Dockerfile`:

1. Push this branch to the GitHub repository (branch `main`).

2. In Render, create a new service and connect your GitHub repository, or let Render detect the `render.yaml` file:
   - If using the UI: choose "Web Service" and select Docker as the runtime, or import via `render.yaml`.
   - If using `render.yaml`, Render will create the `catchy-web` service defined in the file.

3. Add required environment variables in the Render dashboard (Settings → Environment) or fill them in the `render.yaml` before connecting:
   - `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
   - `STRIPE_SECRET_KEY`
   - `MAIL_USERNAME`, `MAIL_PASSWORD`

4. Render will build the Docker image using the `Dockerfile`. The app listens on port `8080` by default.

Notes:
- If you prefer Render to build with Maven natively (no Docker), change `render.yaml` to use `env: static` and provide a `buildCommand`/`startCommand` instead.
- Remove large files from this repository or enable Git LFS if you want to keep them (e.g., `catchy-eb-deploy.zip`).
