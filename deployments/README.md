# Log Ingestion Deployment

The log-ingestion service is deployed to the shared `ai-monitoring` namespace in AKS. It receives log entries via the API Gateway and publishes them to RabbitMQ for downstream processing.

## Prerequisites

1. **Shared infrastructure** – Run `install-dependencies.sh` from `ai-monitoring-alert-service/deployments/` first. This creates:
   - `ai-monitoring-secrets` (must include `rabbitmq-username`, `rabbitmq-password`, and other shared credentials)
   - PostgreSQL, Elasticsearch, Redis, RabbitMQ

2. **GitHub secrets** (for CI/CD deploy on main):
   - `AZURE_CREDENTIALS` – Azure service principal JSON
   - `AKS_RESOURCE_GROUP` – AKS resource group
   - `AKS_CLUSTER_NAME` – AKS cluster name

## Manual deployment

```bash
# Ensure secret exists (run install-dependencies.sh from alert-service first)
helm upgrade --install log-ingestion ./charts \
  --namespace ai-monitoring \
  -f charts/values.yaml
```

## Dependencies

- **RabbitMQ** – Uses `rabbitmq` (K8s service) on port 5672. Credentials come from `ai-monitoring-secrets`:
  - `rabbitmq-username`
  - `rabbitmq-password`

If `ai-monitoring-secrets` does not include `rabbitmq-username`, add it (e.g. `user` for Bitnami RabbitMQ) before deploying.
