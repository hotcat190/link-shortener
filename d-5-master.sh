#!/bin/bash

# Run this script on the MASTER node of the Kubernetes cluster.

kubectl apply -f https://raw.githubusercontent.com/metallb/metallb/v0.13.12/config/manifests/metallb-native.yaml
kubectl apply -f metallb-config.yaml


kubectl apply -f /db/deploy.yaml
kubectl apply -f /backend/deploy.yaml
kubectl apply -f /frontend/deploy.yaml
