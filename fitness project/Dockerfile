# =============================================================================
# AI Fitness Application - React Frontend
# Multi-stage Docker build for production deployment on Render
# =============================================================================

# ---------------------------------------------------------------------------
# Stage 1: Install dependencies and build the Vite/React application
# ---------------------------------------------------------------------------
FROM node:20-alpine AS build

WORKDIR /app

# Copy dependency manifests first for better Docker layer caching.
# This layer is only rebuilt when package.json or package-lock.json change.
COPY package.json package-lock.json ./
RUN npm ci --ignore-scripts

# Copy the rest of the source code and build the production bundle.
# VITE_* environment variables are baked in at build time. On Render, set them
# as build-time environment variables so they are available during `vite build`.
COPY . .

# Accept build-time environment variables for Vite
# These override defaults in .env when provided via --build-arg or Render dashboard
ARG VITE_API_URL
ARG VITE_KEYCLOAK_URL
ARG VITE_KEYCLOAK_REALM
ARG VITE_KEYCLOAK_CLIENT_ID
ARG VITE_REDIRECT_URI

RUN npm run build

# ---------------------------------------------------------------------------
# Stage 2: Serve the static build with Nginx on a minimal Alpine image
# ---------------------------------------------------------------------------
FROM nginx:alpine AS production

# Add labels for container metadata
LABEL maintainer="AI Fitness Application Team"
LABEL description="React frontend for the AI Fitness Application"

# Remove the default Nginx configuration
RUN rm /etc/nginx/conf.d/default.conf

# Copy our custom Nginx config (SPA routing, gzip, caching, security headers)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy the built static assets from the build stage
COPY --from=build /app/dist /usr/share/nginx/html

# Run Nginx as a non-root user for security
# Create a non-root user and set ownership of required directories
RUN chown -R nginx:nginx /usr/share/nginx/html \
    && chown -R nginx:nginx /var/cache/nginx \
    && chown -R nginx:nginx /var/log/nginx \
    && touch /var/run/nginx.pid \
    && chown -R nginx:nginx /var/run/nginx.pid

USER nginx

# Expose the HTTP port
EXPOSE 80

# Health check: verify Nginx is responding
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:80/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
