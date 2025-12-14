# e-commerce-app

This repository contains the Catchy sample e-commerce Spring Boot application.

**Purpose of this branch:** trimmed to be a local-first development workspace. Deployment-related artifacts and automated deploy workflows have been removed to keep the repository focused on running and testing locally.

**Quick local run**

- Prerequisites: Java 21+ and Git. The repository contains the Maven Wrapper (`mvnw.cmd` / `mvnw`).
- Start the app using the `local` profile (H2 in-memory DB):

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
.\mvnw.cmd -DskipTests spring-boot:run
```

- The app will be available at `http://localhost:8080`.
- H2 console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:catchydb`, user `sa`, no password).

**Test accounts**

- Admin: `chandukiranpotru0@gmail.com` / `admin123`
- Test user: `user@catchy.com` / `user123`

Note: These seeded accounts are created and enabled automatically in the `local` profile for easy testing. Change passwords after first use if necessary.

**Running tests**

```powershell
.\mvnw.cmd test
```

**If you want to restore deployment automation**

If you need CI or deployment pipelines again (Github Actions, Dockerfile, Render/EB scripts), restore or re-add those files and adjust settings to your environment. For safety and clarity, those files were removed from the main branch.

**Contributions & next steps**

- If you'd like, I can add a short contributor guide, reintroduce CI with an updated pipeline, or create Git tags/releases for the current stable state.
