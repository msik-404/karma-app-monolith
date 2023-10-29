#! /usr/bin/bash

# stop containers
DIR=$(dirname $BASH_SOURCE)
source "$DIR/stop.sh"

# delete containers
docker rm karma-app-backend-1 && docker rm karma-app-psql-1 && docker rm karma-app-redis-1

# delete backend image
docker image rm karma-app-backend

# delete voulmes
docker volume rm karma-app_psql-data && docker volume rm karma-app_redis-data