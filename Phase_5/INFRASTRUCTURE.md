# Phase 5 — Infrastructure Documentation

## S3 Frontend Hosting

| Field       | Value                                                                    |
| :---------- | :----------------------------------------------------------------------- |
| Provider    | AWS S3                                                                   |
| Bucket name | todo-app-storage-jam98                                                   |
| Region      | US East (N. Virginia) us-east-1                                          |
| Public URL  | http://todo-app-storage-jam98.s3-website-us-east-1.amazonaws.com        |

---

## EC2 Backend Deployment

### Instance Details

| Field         | Value                              |
| :------------ | :--------------------------------- |
| Provider      | AWS EC2                            |
| Instance name | todo-app-spring-server             |
| Instance type | t2.micro (free tier eligible)      |
| OS            | Amazon Linux 2023 (or Ubuntu 24.04 LTS) |
| Region        | US East (Ohio) us-east-2           |
| Public IP/DNS | ec2-3-138-107-74.us-east-2.compute.amazonaws.com |

---

### Security Group Rules

| Port | Protocol | Source        | Purpose                              |
| :--- | :------- | :------------ | :----------------------------------- |
| 22   | TCP      | Your IP only  | SSH access for deployment            |
| 8080 | TCP      | 0.0.0.0/0     | Spring Boot API — public during dev  |

> Once the S3 frontend URL is known, consider locking port 8080 to that origin only.
> S3 frontend URL: `http://todo-app-storage-jam98.s3-website-us-east-1.amazonaws.com`

---

### Docker Setup on EC2

Install Docker on a fresh Amazon Linux 2023 instance:

```bash
sudo dnf update -y
sudo dnf install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user
# Log out and back in for the group change to take effect
```

---

### How to Deploy

The `deploy.sh` script at `Project1_TheBlind/todo-app/backend/deploy.sh` handles the full
build-and-deploy cycle from your local machine.

**One-time setup:**
```bash
# 1. Copy the example env file and fill in real values
cp Project1_TheBlind/todo-app/backend/.env.example \
   Project1_TheBlind/todo-app/backend/.env

# 2. Make the script executable
chmod +x Project1_TheBlind/todo-app/backend/deploy.sh
```

**Run a deployment:**
```bash
cd Project1_TheBlind/todo-app/backend
./deploy.sh <EC2_USER> <EC2_PUBLIC_DNS> <PATH_TO_PEM_KEY>

# Example (using the actual instance):
./deploy.sh ec2-user ec2-3-138-107-74.us-east-2.compute.amazonaws.com ../todo-app-key.pem
```

The script will:
1. Run `./gradlew bootJar -x test` to build the fat JAR
2. Build the Docker image locally
3. Save and `scp` the image to EC2
4. Load the image on EC2, stop any old container, and start a fresh one

---

### Environment Variables

These are passed to the container at runtime. **Never hardcode real values here or in the image.**
Copy `.env.example` to `.env` and fill in actual values — `.env` is already in `.gitignore`.

| Variable                    | Maps to (application.properties)    | Description                                      |
| :-------------------------- | :----------------------------------- | :----------------------------------------------- |
| `SPRING_DATASOURCE_URL`     | `spring.datasource.url`              | JDBC URL — should point to `/opt/app/data/todo.db` |
| `SECURITY_JWT_SECRET_KEY`   | `security.jwt.secret-key`            | Base64-encoded HMAC secret for signing JWTs      |
| `SECURITY_JWT_EXPIRATION_TIME` | `security.jwt.expiration-time`    | Token lifetime in milliseconds (e.g. `3600000` = 1 hr) |
| `CORS_ALLOWED_ORIGINS`      | `cors.allowed-origins` *(see note)*  | Frontend URL allowed to make cross-origin requests |

> **CORS note:** `CORS_ALLOWED_ORIGINS` requires Dev 3's `WebConfig.java` change to be
> merged first. Once that's in, set this to `http://todo-app-storage-jam98.s3-website-us-east-1.amazonaws.com` — already set in `.env.example`.

---

### SQLite Persistence

SQLite writes `todo.db` to the container's working directory. The Dockerfile sets
`WORKDIR /opt/app` and the `entrypoint.sh` creates `/opt/app/data/` at startup.

The container is started with a host volume mount:

```
Host path (EC2):        ~/todo-data/
Container path:         /opt/app/data/
Database file:          /opt/app/data/todo.db
```

This means `todo.db` **survives container restarts and redeployments** as long as
`~/todo-data/` exists on the EC2 host. The `deploy.sh` script creates this directory
automatically before starting the container.

To back up the database manually:
```bash
scp -i ../todo-app-key.pem ec2-user@ec2-3-138-107-74.us-east-2.compute.amazonaws.com:~/todo-data/todo.db ./todo-backup.db
```

---

### Verification

After deployment, run this smoke test from your local machine:

```bash
# Register a test user — expect HTTP 201
curl -s -o /dev/null -w "%{http_code}\n" \
  http://ec2-3-138-107-74.us-east-2.compute.amazonaws.com:8080/api/register \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"username": "smoketest", "password": "smoketest123"}'

# Log in — expect HTTP 200 and a JWT in the response body
curl -s \
  http://ec2-3-138-107-74.us-east-2.compute.amazonaws.com:8080/api/auth/login \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"username": "smoketest", "password": "smoketest123"}'
```

---

### Sharing with Teammates

Once the EC2 instance is running, share the following:

- **Dev 1 (Frontend / S3):** EC2 DNS is `ec2-3-138-107-74.us-east-2.compute.amazonaws.com` — needed to set `apiUrl` in
  `src/environments/environment.prod.ts` before the Angular production build.
- **Dev 3 (CORS):** EC2 DNS above + S3 URL `http://todo-app-storage-jam98.s3-website-us-east-1.amazonaws.com` — needed to add `CORS_ALLOWED_ORIGINS` to
  the `docker run` command, and the S3 URL to configure `WebConfig.java`.

---

### Redeployment Steps

When code changes and you need to push a new version:

```bash
# From the backend directory
./deploy.sh ec2-user ec2-3-138-107-74.us-east-2.compute.amazonaws.com ../todo-app-key.pem
```

The script automatically stops the old container and starts a fresh one.
The `~/todo-data/` volume is preserved — no data is lost.
