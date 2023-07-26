#! /usr/bin/bash

docker rm karma-app-backend-1 && docker image rm karma-app-backend
docker rm karma-app-psql-1 && docker rm karma-app-redis-1
docker volume rm karma-app_psql-data && docker volume rm karma-app_redis-data
