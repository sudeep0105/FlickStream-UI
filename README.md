# FlickStream

A Spring Boot streaming platform starter with a separate HTML/CSS/JavaScript front end and MySQL-backed account storage.

Visitors must sign in or create an account before the catalog is shown. The sign-in screen appears on every fresh page load instead of silently restoring an existing session; signing out also returns to that screen.

## Requirements

- JDK 24 (Spring Boot 4.1.1 supports Java 17 through 26)
- MySQL 8
- IntelliJ IDEA with Maven support, or Maven 3.6.3+

## Set up MySQL

Open `schema.sql` in MySQL Workbench and run it. It creates the `flickstream` database and the `users` table. The application checks this schema at startup; it does not create or drop tables automatically.

## Run from IntelliJ IDEA

Open this folder or its `pom.xml` as a Maven project and let IntelliJ import the Maven dependencies. Create a run configuration for `com.flickstream.FlickStreamApplication` and set these environment variables:

```text
DB_URL=jdbc:mysql://127.0.0.1:3306/flickstream?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USER=root
DB_PASSWORD=your_mysql_password
COOKIE_SECURE=false
```

Run `FlickStreamApplication` and visit `http://localhost:3000`.

If Maven is installed, the same variables can be set in PowerShell and the application started from this folder:

```powershell
$env:DB_URL = "jdbc:mysql://127.0.0.1:3306/flickstream?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USER = "root"
$env:DB_PASSWORD = "your_mysql_password"
$env:COOKIE_SECURE = "false"
mvn spring-boot:run
```

Set `COOKIE_SECURE=true` when deploying over HTTPS. Spring Security stores the login in a server-side session cookie; passwords are hashed with BCrypt before they are saved to MySQL. The browser sends CSRF tokens with sign-up, sign-in, and sign-out requests.

## REST endpoints

- `GET /api/auth/csrf` — get a CSRF token for form requests
- `POST /api/auth/signup` — create an account and sign in
- `POST /api/auth/signin` — sign in
- `GET /api/auth/me` — get the signed-in user
- `POST /api/auth/logout` — end the session

## Project layout

- `src/main/resources/static/`: `index.html`, `style.css`, `app.js`
- `src/main/java/com/flickstream/`: Spring Boot app, REST authentication, security, and JPA user repository
- `src/main/resources/application.properties`: database and session configuration
- `schema.sql`: MySQL database and user table
- `pom.xml`: Spring Boot and Java dependencies

This starter uses in-memory HTTP sessions, so signing in again is required after restarting the server. Before public deployment, use HTTPS, a persistent session store, database backups, and production MySQL credentials. Streaming titles still need a licensed video source and playback service.

GitHub can store this source code, but GitHub Pages cannot run the Spring Boot API or MySQL database. To publish a live site, deploy the Spring Boot app and MySQL database on a hosting service, then point the domain or front end to that service.

## Container deployment

The included `Dockerfile` builds the Spring Boot app into a container. A hosting service that supports Docker can build it from this repository. Configure these environment variables in the host's dashboard:

- `DB_URL`: JDBC URL for the managed MySQL database
- `DB_USER` and `DB_PASSWORD`: database credentials
- `COOKIE_SECURE=true`: use secure session cookies when the site is served over HTTPS
- `PORT`: set by most hosting services automatically

Run `schema.sql` against the hosted database before the first deployment. Do not put database credentials in GitHub or in the Docker image. The MySQL users table preserves accounts across deployments; the default in-memory login sessions reset when the server restarts.
