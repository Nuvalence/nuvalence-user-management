#!/bin/bash

export POSTGRES_PASSWORD=$(kubectl get secret --namespace user-management user-management-postgres-postgresql -o jsonpath="{.data.postgres-password}" | base64 -d)

# Start the Postgres Client
kubectl run user-management-postgres-postgresql-client --rm --tty -i --restart='Never' --namespace user-management --image docker.io/bitnami/postgresql:14.3.0-debian-10-r22 --env="PGPASSWORD=$POSTGRES_PASSWORD" \
      --command -- psql --host user-management-postgres-postgresql -U postgres -d postgres -p 5432