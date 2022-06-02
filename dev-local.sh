#!/bin/bash

# Install Nginx Ingress Controller to the K8s cluster
kubectl apply -f k8s/ingress-nginx/manifest.yaml

# Watch the Ingress Nginx rollout status
kubectl rollout status deployment ingress-nginx-controller -n ingress-nginx

# Initialize DevSpace to the correct namespace in the Rancher Desktop local K8s cluster
devspace use context rancher-desktop
devspace use namespace user-management

# Start up DevSpace local development
devspace dev