# Build all local app images referenced by infra/k8s/dls-local.yaml
# Run from repository root: .\infra\k8s\build-images.ps1
#
# Infra images (NOT built here — pulled by Kubernetes):
#   mysql:8.4, rabbitmq:3-management, mailhog/mailhog

param(
    [switch]$LoadToMinikube
)

$ErrorActionPreference = "Stop"
$Root = Resolve-Path (Join-Path $PSScriptRoot "../..")

function Build-SpringService {
    param(
        [string]$Module,
        [string]$Artifact,
        [int]$Port
    )
    Write-Host "Building dls/${Artifact}:local ..."
    docker build -f "$Root/infra/docker/Dockerfile.spring-service" `
        --build-arg SERVICE_MODULE="services/$Module" `
        --build-arg SERVICE_ARTIFACT=$Artifact `
        --build-arg SERVICE_PORT=$Port `
        -t "dls/${Artifact}:local" `
        $Root
}

function Build-StandaloneService {
    param(
        [string]$Artifact,
        [string]$Context
    )
    Write-Host "Building dls/${Artifact}:local ..."
    docker build -t "dls/${Artifact}:local" $Context
}

# All 7 microservices + frontend (matches dls-local.yaml)
$images = @(
    "dls/user-service:local",
    "dls/billing-service:local",
    "dls/streaming-service:local",
    "dls/catalog-service:local",
    "dls/recommendation-service:local",
    "dls/review-rating-service:local",
    "dls/engagement-service:local",
    "dls/frontend:local"
)

Write-Host "=== DLS local image build (8 app images) ==="

Build-SpringService -Module "user-service" -Artifact "user-service" -Port 8081
Build-SpringService -Module "billing-service" -Artifact "billing-service" -Port 8084
Build-SpringService -Module "streaming-service" -Artifact "streaming-service" -Port 8083
Build-SpringService -Module "catalog-service" -Artifact "catalog-service" -Port 8082

Write-Host "Building dls/recommendation-service:local ..."
docker build -f "$Root/infra/docker/Dockerfile.python-service" `
    --build-arg SERVICE_PATH=services/recommendation-service `
    --build-arg SERVICE_PORT=8090 `
    -t dls/recommendation-service:local `
    $Root

Build-StandaloneService -Artifact "review-rating-service" -Context "$Root/services/review-rating-service"
Build-StandaloneService -Artifact "engagement-service" -Context "$Root/services/engagement-service"
Build-StandaloneService -Artifact "frontend" -Context "$Root/frontend"

Write-Host ""
Write-Host "Built images:"
foreach ($image in $images) {
    Write-Host "  - $image"
}

if ($LoadToMinikube) {
    if (-not (Get-Command minikube -ErrorAction SilentlyContinue)) {
        throw "minikube not found on PATH. Install minikube or omit -LoadToMinikube."
    }
    Write-Host ""
    Write-Host "Loading images into minikube..."
    foreach ($image in $images) {
        minikube image load $image
    }
}

Write-Host ""
Write-Host "Done."
