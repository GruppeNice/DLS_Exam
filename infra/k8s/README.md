# Local Kubernetes (DLS Platform)

Kubernetes manifests for running the full DLS stack locally (Docker Desktop Kubernetes, Minikube, or k3s).

Based on the `origin/k8s` branch pattern (single manifest, secrets, NodePort services), updated for:

- **MySQL 8.4** (7 databases)
- **All 7 microservices** + **frontend**
- **RabbitMQ** + **MailHog**

## Prerequisites

- kubectl
- A local cluster (Docker Desktop → Enable Kubernetes, or Minikube/k3s)
- Docker, to build images

## 1. Build images

From the **repository root**:

```powershell
.\infra\k8s\build-images.ps1
```

This tags all **8 app images** as `dls/<service>:local` (same convention as the original k8s branch):

| Image | Service |
|-------|---------|
| `dls/user-service:local` | user-service |
| `dls/billing-service:local` | billing-service |
| `dls/streaming-service:local` | streaming-service |
| `dls/catalog-service:local` | catalog-service |
| `dls/recommendation-service:local` | recommendation-service |
| `dls/review-rating-service:local` | review-rating-service |
| `dls/engagement-service:local` | engagement-service |
| `dls/frontend:local` | frontend |

MySQL, RabbitMQ, and MailHog are **not** built — Kubernetes pulls those from Docker Hub.

### Load images into Minikube (if not using Docker Desktop's built-in K8s)

```powershell
.\infra\k8s\build-images.ps1 -LoadToMinikube
```

Or manually:

```bash
minikube image load dls/user-service:local
minikube image load dls/billing-service:local
minikube image load dls/streaming-service:local
minikube image load dls/catalog-service:local
minikube image load dls/recommendation-service:local
minikube image load dls/review-rating-service:local
minikube image load dls/engagement-service:local
minikube image load dls/frontend:local
```

Docker Desktop Kubernetes can use local images directly when `imagePullPolicy: IfNotPresent`.

## 2. Deploy

```bash
kubectl apply -f infra/k8s/dls-local.yaml
```

Wait for pods:

```bash
kubectl get pods -w
```

## 3. Access services

NodePort services expose app ports. With Docker Desktop, use `localhost` and the allocated node ports:

```bash
kubectl get svc
```

| Service | Container port |
|---------|----------------|
| frontend | 3000 |
| user-service | 8081 |
| catalog-service | 8082 |
| streaming-service | 8083 |
| billing-service | 8084 |
| review-rating-service | 8085 |
| engagement-service | 8086 |
| recommendation-service | 8090 |

MailHog UI (ClusterIP): port-forward if needed:

```bash
kubectl port-forward svc/mailhog 8025:8025
```

## 4. Tear down

```bash
kubectl delete -f infra/k8s/engagement-scaledjob.yaml
kubectl delete -f infra/k8s/dls-local.yaml
```

## 5. KEDA ScaledJob (Engagement notification workers)

The engagement **Deployment** (`ENGAGEMENT_MODE=server`) exposes the REST API and consumes domain events. Outbound email delivery is handled by short-lived **job** pods scaled by [KEDA](https://keda.sh/) on `notification-queue` depth.

### Install KEDA (once per cluster)

```bash
kubectl apply --server-side -f https://github.com/kedacore/keda/releases/download/v2.16.1/keda-2.16.1.yaml
```

Verify:

```bash
kubectl get pods -n keda
```

### Deploy the ScaledJob

After `dls-local.yaml` is running and RabbitMQ is healthy:

```bash
kubectl apply -f infra/k8s/engagement-scaledjob.yaml
```

Check workers:

```bash
kubectl get scaledjob
kubectl get jobs -w
```

Each job pod runs `ENGAGEMENT_MODE=job`: receives one message from `notification-queue`, sends the email via MailHog, then exits. KEDA creates more jobs when the queue length exceeds the trigger threshold (`value: "1"`).

To remove workers only:

```bash
kubectl delete -f infra/k8s/engagement-scaledjob.yaml
```

## Notes

- DB volumes use `emptyDir` (data is lost when pods restart). Use PVCs for persistence in a real deployment.
- No readiness probes or init containers yet — services may need a minute to start after DBs are ready.
- Secrets in `dls-local.yaml` are for **local dev only**; do not use in production.
