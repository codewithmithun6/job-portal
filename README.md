# Workfield Job Portal

A Java 17+ learning project for candidates, recruiters, and administrators. Built with Spring Boot, Spring Security, Thymeleaf, Hibernate/JPA, and MySQL.

## Requirements

- Java 17 or newer
- MySQL 8+
- Maven 3.9+ or the included Maven Wrapper

## MySQL setup

Create a database and local application account in MySQL Workbench or the MySQL command line:

```sql
CREATE DATABASE job_portal CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'job_portal_app'@'localhost' IDENTIFIED BY 'choose-a-strong-local-password';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES ON job_portal.* TO 'job_portal_app'@'localhost';
```

If MySQL reports that the user already exists, use `ALTER USER 'job_portal_app'@'localhost' IDENTIFIED BY 'choose-a-strong-local-password';` and then run the `GRANT` statement.

In IntelliJ, open **Run → Edit Configurations…**, select the Spring Boot configuration for `JobPortalApplication`, then add these under **Environment variables** (without `$env:` or quotes):

```text
DB_URL=jdbc:mysql://localhost:3306/job_portal?useSSL=false&serverTimezone=UTC
DB_USERNAME=job_portal_app
DB_PASSWORD=your-local-password
```

Keep credentials out of source control. For a local terminal session in PowerShell, use:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/job_portal?useSSL=false&serverTimezone=UTC'
$env:DB_USERNAME = 'job_portal_app'
$env:DB_PASSWORD = 'your-local-password'
```

## Demo data and sign-in

Every application startup checks for the following demo accounts and three sample job listings. Missing records are created; existing records are left unchanged, so restarting does not add duplicates.

| Role | Email | Local demo password |
| --- | --- | --- |
| Candidate | `candidate@demo.local` | `CandidateDemo123!` |
| Recruiter | `recruiter@demo.local` | `RecruiterDemo123!` |
| Admin | `admin@demo.local` | `AdminDemo123!` |

These are public, learning-only demo credentials. Change them through `DEMO_CANDIDATE_PASSWORD`, `DEMO_RECRUITER_PASSWORD`, and `DEMO_ADMIN_PASSWORD` before using a shared or exposed environment. For a fresh database, set those variables before the first startup. Existing seeded accounts are not automatically assigned a new password on later starts.

Demo values can be overridden in IntelliJ's Environment variables field:

```text
DEMO_CANDIDATE_EMAIL=candidate@demo.local
DEMO_CANDIDATE_PASSWORD=your-own-long-candidate-password
DEMO_RECRUITER_EMAIL=recruiter@demo.local
DEMO_RECRUITER_PASSWORD=your-own-long-recruiter-password
DEMO_ADMIN_EMAIL=admin@demo.local
DEMO_ADMIN_PASSWORD=your-own-long-admin-password
```

## Run and test

Open this directory as a Maven project in IntelliJ, or run these commands from PowerShell here:

```powershell
.\mvnw.cmd spring-boot:run
.\mvnw.cmd test
```

The portal runs at <http://localhost:8082>. If you prefer installed Maven, use `mvn spring-boot:run` and `mvn test`. Tests use an in-memory H2 database and do not need MySQL.

## Workflows

- Candidates and recruiters register through the public registration page. Admin role is never selectable there.
- Candidates can search open listings by role/company, location, and work type, submit one cover letter per job, and view application status.
- Recruiters can publish openings and close their own jobs.
- Admins can review pending applications and verify or reject each submission. Reviews cannot be changed after the first decision.
- Account passwords are stored with BCrypt. Form actions use Spring Security CSRF protection.

This is a learning demo, not a production hiring platform. It does not verify candidate identity, employer legitimacy, or the truth of job listings. Do not collect sensitive personal data or deploy with demo credentials. Before production, use schema migrations, HTTPS, secret management, privacy/retention policies, rate limiting, audit controls, and a security review.
