# Use Node to build
FROM node:23-slim AS builder

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Use Nginx to serve
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
# Copy custom nginx config if needed
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
