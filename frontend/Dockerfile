# Build frontend
FROM node:23-slim AS builder

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Serve frontend using a simple Node HTTP server (optional)
FROM node:23-slim

WORKDIR /app
RUN npm install -g serve

# Copy built frontend
COPY --from=builder /app/dist /app/dist

EXPOSE 80
CMD ["serve", "-s", "dist", "-l", "80"]
