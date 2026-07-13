# Stage 1: Build the Angular application
FROM node:22-alpine AS build

WORKDIR /app

# Copy dependency manifests first to leverage Docker layer caching
COPY package.json package-lock.json ./
# Install exact versions from lockfile (no devDependency pruning—needed for build)
RUN npm ci

# Copy source code and build the production bundle
COPY . .
RUN npx ng build

# Stage 2: Serve with nginx
FROM nginx:alpine

# Replace the default nginx config with our custom one
RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy only the compiled static assets from the build stage
COPY --from=build /app/dist/todo/browser /usr/share/nginx/html

# Nginx listens on port 80
EXPOSE 80

# Run nginx in the foreground so the container stays alive
CMD ["nginx", "-g", "daemon off;"]