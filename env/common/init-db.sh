#!/bin/bash

# Ожидание запуска PostgreSQL
until pg_isready -U $POSTGRES_USER -d $POSTGRES_DB; do
  echo 'Waiting for PostgreSQL to start...'
  sleep 1
done

# Проверка наличия дампов
latest_backup=$(ls -t /backups/db_backup_*.sql 2>/dev/null | head -n 1)

# Если дампов нет, выполнить начальную инициализацию из SQL файла
if [ -z "$latest_backup" ]; then
  echo "No backups found. Initializing database from initial_data.sql"
  psql -U $POSTGRES_USER -d $POSTGRES_DB -f /docker-entrypoint-initdb.d/initial_data.sql
else
  echo "Restoring database from $latest_backup"
  psql -U $POSTGRES_USER -d $POSTGRES_DB -f "$latest_backup"
fi
