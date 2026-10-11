#!/usr/bin/env bash
set -Eeuo pipefail
image="senproxi-backend:${1:?Missing image tag}"
archive="${2:?Missing archive}"
[[ "$1" =~ ^[a-f0-9]{40}$ ]]
[[ "$archive" == /tmp/senproxi-backend-*.tar.gz ]]
trap 'rm -f -- "$archive"' EXIT
command -v docker >/dev/null
command -v curl >/dev/null
docker info >/dev/null
test -s /opt/sen-proxi-teranga/backend.env
test -s /opt/sen-proxi-teranga/mysql.env
gzip -dc "$archive" | docker load
docker network inspect senproxi >/dev/null 2>&1 || docker network create senproxi >/dev/null
docker volume create senproxi-mysql >/dev/null
if ! docker container inspect senproxi-mysql >/dev/null 2>&1; then
    docker run --detach --name senproxi-mysql --restart unless-stopped \
        --network senproxi --network-alias mysql --memory=1g --cpus=1.5 \
        --env-file /opt/sen-proxi-teranga/mysql.env \
        --mount source=senproxi-mysql,target=/var/lib/mysql \
        --health-cmd='MYSQL_PWD="$MYSQL_PASSWORD" mysql --protocol=TCP -h 127.0.0.1 -u "$MYSQL_USER" "$MYSQL_DATABASE" -e "SELECT 1"' \
        --health-interval=5s --health-timeout=5s --health-retries=60 \
        --health-start-period=30s \
        --log-opt max-size=10m --log-opt max-file=3 mysql:8.4 >/dev/null
else
    docker start senproxi-mysql >/dev/null
fi
database_ready=false
for attempt in $(seq 1 90); do
    if [[ "$(docker inspect --format '{{.State.Health.Status}}' senproxi-mysql)" == healthy ]]; then
        database_ready=true
        break
    fi
    sleep 3
done
if ! "$database_ready"; then
    echo "MySQL did not become ready. Existing backend container was not replaced." >&2
    exit 1
fi
docker volume create senproxi-uploads >/dev/null
name=senproxi-backend
previous="$name-previous"
docker rm -f "$previous" >/dev/null 2>&1 || true
had_previous=false
if docker container inspect "$name" >/dev/null 2>&1; then
    docker stop "$name" >/dev/null
    docker rename "$name" "$previous"
    had_previous=true
fi
rollback() {
    docker logs --tail 60 "$name" >&2 || true
    docker rm -f "$name" >/dev/null 2>&1 || true
    if "$had_previous"; then
        docker rename "$previous" "$name"
        docker start "$name" >/dev/null
    fi
    echo "Deployment failed; previous container restored when available." >&2
    exit 1
}
docker run --detach --name "$name" --restart unless-stopped \
    --network senproxi --network-alias backend --memory=1g --cpus=2 \
    --publish 127.0.0.1:18080:8080 \
    --env-file /opt/sen-proxi-teranga/backend.env \
    --mount source=senproxi-uploads,target=/app/uploads \
    --log-opt max-size=10m --log-opt max-file=3 "$image" || rollback
ready=false
for attempt in $(seq 1 60); do
    if curl --fail --silent --max-time 5 http://127.0.0.1:18080/api/categories >/dev/null; then
        ready=true
        break
    fi
    sleep 3
done
"$ready" || rollback
docker rm "$previous" >/dev/null 2>&1 || true
echo "Deployed $image successfully."
# Container rollback does not revert database changes.
