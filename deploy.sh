# !/bin/bash
# This script is used to install and run the project on Ubuntu 24.04

# NOTE: Before running this script, make sure you have the following:
# 1. Install Git
# 2. Clone this repository
# 3. Switch to the cloud-deploy branch
# 4. Make this script executable: chmod +x deploy.sh
# 5. Run this script: ./deploy.sh

# Remove old versions of docker
for pkg in docker.io docker-doc docker-compose docker-compose-v2 podman-docker containerd runc; do sudo apt-get remove $pkg; done

# Install dependencies
# Add Docker's official GPG key:
sudo apt-get update
sudo apt-get install ca-certificates curl
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

# Add the repository to Apt sources:
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update

# Install Docker Engine, containerd, and Docker Compose
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin -y

# Start the services
# Make sure to run this script from the root of the project
set -e

echo "🚀 Building and starting backend..."
cd backend
sudo docker compose up -d --build

echo "🚀 Building and starting frontend..."
cd ../frontend
sudo docker compose up -d --build

echo "✅ All services are up and running!"
