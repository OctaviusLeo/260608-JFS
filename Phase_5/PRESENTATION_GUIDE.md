# Phase 5 Presentation Guide

Total time: 15 minutes
Format: Live demo walkthrough with one person driving the screen and one person talking.

---

## Before You Start (5 minutes before presenting)

Make sure all of this is ready before the presentation begins:

- Browser tabs open and ready:
  - Jenkins dashboard: http://ec2-3-138-107-74.us-east-2.compute.amazonaws.com:9090
  - Live frontend: http://todo-app-storage-jam98.s3-website-us-east-1.amazonaws.com
  - GitHub repo: https://github.com/OctaviusLeo/260608-JFS
- VS Code open with the project loaded
- Terminal open in the project root
- Jenkins is logged in as octaviusleo
- The app is currently working end to end (log in, create a task, confirm it saves)

---

## Step 1 — Introduce the Project (1 minute)

**Screen:** Show the live frontend at the S3 URL.

**Say:**
"This is our Todo application. It is a full stack app with an Angular frontend hosted on AWS S3 and a Spring Boot backend running in a Docker container on an EC2 instance. Everything you see here was built, tested, and deployed through an automated pipeline. We are going to walk you through that entire lifecycle right now."

---

## Step 2 — Show the Application Working (1 minute)

**Screen:** Stay on the live frontend.

**Do:** Register a new account, log in, create a task, mark it complete.

**Say:**
"The app is fully functional. Users can register, log in, and manage their tasks. The data is stored in a SQLite database that persists on the EC2 instance. The frontend talks to the backend through a reverse proxy configured in Nginx."

---

## Step 3 — Show the Architecture (1 minute)

**Screen:** Switch to the slide deck at the architecture slide.

**Say:**
"Here is how everything connects. When a user visits the S3 URL, they get the Angular app. Any API call the frontend makes goes to the EC2 instance running the Spring Boot backend in Docker. Jenkins runs on the same EC2 instance and watches the GitHub repository. When code is pushed to main, Jenkins automatically pulls it, builds it, tests it, and deploys it."

---

## Step 4 — Show the Jenkins Dashboard (1 minute)

**Screen:** Switch to Jenkins at http://ec2-3-138-107-74.us-east-2.compute.amazonaws.com:9090

**Say:**
"This is our Jenkins CI/CD server. You can see the todo-app-pipeline here. The most recent build shows green across all stages — Checkout, Build, Test, and Deploy. Jenkins is watching our GitHub repository and triggers automatically whenever code is pushed."

---

## Step 5 — The Live Code Push Demo (5 minutes)

This is the most important part of the presentation. Walk through it slowly.

**5a — Make a visible change**

**Screen:** Switch to VS Code.

Open a visible UI file in the Angular frontend. Change something small that will be visible in the browser, for example a heading or a button label.

**Say:**
"We are going to make a small change to the frontend right now and push it to GitHub. Watch what happens."

**Do:** Save the file.

---

**5b — Push to GitHub**

**Screen:** Stay in VS Code terminal or switch to terminal.

```bash
git add .
git commit -m "demo: update UI text for presentation"
git push origin main
```

**Say:**
"We just pushed to the main branch. Jenkins is configured to poll this repository. Within about a minute it will detect this push and automatically kick off a new build."

---

**5c — Show Jenkins picking it up**

**Screen:** Switch to Jenkins dashboard and refresh.

**Say:**
"You can see the pipeline has already started running. The first stage is Checkout — Jenkins is pulling the latest code from GitHub right now."

Point out each stage as it turns green:

- When Checkout goes green: "Code is pulled."
- When Build goes green: "The Spring Boot JAR was compiled and the Docker image was built."
- When Test goes green: "All automated tests passed. This is the quality gate — if anything failed here the pipeline would have stopped and nothing would have been deployed."
- When Deploy goes green: "Deployment is done. The new version is live."

---

**5d — Show the change is live**

**Screen:** Switch to the S3 URL and hard refresh the browser (Ctrl+Shift+R).

**Say:**
"And there it is. The change we pushed 60 seconds ago is now live on AWS. That is the full code-to-cloud lifecycle running automatically."

---

## Step 6 — Show the Failure Case (2 minutes)

**Screen:** Stay on Jenkins, then switch to VS Code.

**Say:**
"One of the most important things a CI/CD pipeline does is protect production from broken code. Let us show you what happens when a test fails."

**Do:** Open a test file and break an assertion intentionally — for example change an expected HTTP status from 201 to 999.

```bash
git add .
git commit -m "demo: intentional broken test"
git push origin main
```

**Screen:** Switch back to Jenkins and watch the Test stage go red.

**Say:**
"The Test stage failed. Jenkins stopped the pipeline immediately. The broken code never reached AWS. This is the pipeline acting as a quality gate — it caught the error before it could affect real users."

**Do:** Revert the change:
```bash
git revert HEAD --no-edit
git push origin main
```

**Say:**
"We reverted the bad commit. Jenkins will pick this up and the pipeline will go green again."

---

## Step 7 — Technical Deep Dive (2 minutes)

**Screen:** Switch to the slide deck.

Be ready to answer these questions. Here are short answers for each:

**Why did you structure the Jenkins stages the way you did?**
"Checkout, Build, Test, Deploy is the natural order of a delivery pipeline. Each stage depends on the previous one. We fail fast — if the build breaks we do not waste time running tests."

**How do your API tests and E2E tests give different confidence?**
"The REST Assured tests verify every endpoint returns the correct status codes and data. The Cucumber E2E tests verify the full flow from the browser — register, log in, create a task — the way a real user would. Together they cover both the contract and the experience."

**How did Docker help?**
"Docker means the app behaves identically in Jenkins and on EC2. There is no 'it works on my machine' problem because everyone is running the same image."

**How do S3 and EC2 work together?**
"S3 serves the static Angular files. EC2 runs the backend API. The Nginx config in the frontend Docker image proxies any request starting with /api to the EC2 instance, so the browser only ever talks to one origin."

---

## Step 8 — DevOps Reflection (1 minute)

**Screen:** Slide deck, last slide.

**Say:**
"Moving from manual deployment to an automated pipeline changed how we write code. Knowing that every push runs the full test suite made us more careful about what we commit. The biggest challenge was getting Docker, Jenkins, and AWS all talking to each other — especially the networking between the frontend proxy and the backend. But once the pipeline was green, deploying a new version became a single git push."

---

## Closing

**Say:**
"That is our full DevOps lifecycle. Code written locally, pushed to GitHub, automatically built and tested by Jenkins, and deployed to AWS. Thank you."

---

## Quick Reference

| Thing | Value |
| :--- | :--- |
| Frontend URL | http://todo-app-storage-jam98.s3-website-us-east-1.amazonaws.com |
| Backend URL | http://ec2-3-138-107-74.us-east-2.compute.amazonaws.com:8080 |
| Jenkins URL | http://ec2-3-138-107-74.us-east-2.compute.amazonaws.com:9090 |
| GitHub Repo | https://github.com/OctaviusLeo/260608-JFS |
| Jenkins User | octaviusleo |
