#!/bin/bash

export NAMESPACE="user-management"

# Create the $NAMESPACE if it doesn't exist already
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# Install Bitnami Postgres to the K8s cluster
helm repo add bitnami https://charts.bitnami.com/bitnami
helm upgrade --install "$NAMESPACE-postgres" bitnami/postgresql \
  --version 11.6.3 \
  --namespace "$NAMESPACE"

# Install Nginx Ingress Controller to the K8s cluster
helm upgrade --install ingress-nginx ingress-nginx \
  --repo https://kubernetes.github.io/ingress-nginx \
  --namespace ingress-nginx --create-namespace

# Watch the Ingress Nginx rollout status
kubectl rollout status deployment ingress-nginx-controller -n ingress-nginx

# Initialize DevSpace to the correct namespace in the Rancher Desktop local K8s cluster
devspace use context rancher-desktop
devspace use namespace "$NAMESPACE"

# Start up DevSpace local development
devspace dev