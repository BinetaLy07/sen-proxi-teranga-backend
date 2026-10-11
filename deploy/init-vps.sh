#!/usr/bin/env bash
set -Eeuo pipefail
command -v docker >/dev/null
command -v openssl >/dev/null
docker info >/dev/null
directory=/opt/sen-proxi-teranga
umask 077
mkdir -p "$directory"
if [[ -e "$directory/backend.env" || -e "$directory/mysql.env" ]]; then
    echo "Configuration already exists. No file was changed; review it manually." >&2
    exit 1
fi
database_password="$(openssl rand -hex 32)"
root_password="$(openssl rand -hex 32)"
admin_password="$(openssl rand -hex 24)"
cat > "$directory/mysql.env" <<EOF
MYSQL_DATABASE=sen_proxi_teranga
MYSQL_USER=senproxi
MYSQL_PASSWORD=$database_password
MYSQL_ROOT_PASSWORD=$root_password
EOF
cat > "$directory/backend.env" <<EOF
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/sen_proxi_teranga?serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false
SPRING_DATASOURCE_USERNAME=senproxi
SPRING_DATASOURCE_PASSWORD=$database_password
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false
SERVER_FORWARD_HEADERS_STRATEGY=framework
APP_UPLOADS_DOSSIER=/app/uploads
ADMIN_EMAIL=admin@senproxiteranga.sn
ADMIN_PASSWORD=$admin_password
EOF
chmod 600 "$directory/mysql.env" "$directory/backend.env"
echo "Configuration created in $directory with random passwords (not printed)."
echo "Run the backend deployment to initialize MySQL and the application tables."
echo "After first successful deployment, change ddl-auto from update to validate in backend.env."
