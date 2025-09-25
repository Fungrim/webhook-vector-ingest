#/bin/bash

TAG="$1"

if [ -z "$TAG" ]; then
  echo "Usage: $0 <tag>"
  exit 1
fi

BASE="fungrim/webhook-vector-ingest"

docker tag $BASE:latest "ghcr.io/$BASE:$TAG"
docker tag $BASE:latest "ghcr.io/$BASE:latest"
docker push "ghcr.io/$BASE:$TAG"
docker push "ghcr.io/$BASE:latest"