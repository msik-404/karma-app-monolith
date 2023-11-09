# karma-app
karma-app monolith version. karma-app is http server which exposes rest endpoints, it uses PostgreSQL and Redis. 

There is also [microservices](https://github.com/msik-404/karma-app-gateway) version of this app which uses MongoDB instead of PostgreSQL.

# Technologies used
- Java 21
- Redis
- PostgreSQL
- Docker
- Java spring
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-data-redis
- spring-boot-starter-security
- spring-boot-starter-validation
- spring-boot-starter-hateoas
- spring-boot-starter-test
- [spring-boot-testcontainers](https://spring.io/blog/2023/06/23/improved-testcontainers-support-in-spring-boot-3-1)
- [jjwt](https://github.com/jwtk/jjwt#install-jdk-maven)
- junit-jupiter
- lombok
- OpenAPI
- Swagger

# Features

## REST API
The API documentation follows OpenAPI specification. API docs can be inspected:
- In raw form: [openapi.yaml](https://github.com/msik-404/karma-app/blob/main/openapi.yaml).
- In UI: [Swagger UI](https://petstore.swagger.io/?url=https%3A%2F%2Fraw.githubusercontent.com%2Fmsik-404%2Fkarma-app%2Fmain%2Fapi-docs.yaml#/)

### Note
Endpoints that have `/user`, `/mod`, `/admin` prefix require user to be authenticated.
User need to set `HTTP Authorization header` to `Berear JWT_STRING`. Token is acquired from
login endpoint.

JWT has two claims set:
- sub (subject) to logged-in user id which is SOME_24_CHAR_HEX_STRING.
- exp (expiration time) to one hour.

## Cache
Most of the endpoints use cache in some way but primarily cache is used for fetching any range of posts between [0 -
 [MAX_CACHED_POSTS](https://github.com/msik-404/karma-app/blob/main/src/main/java/com/msik404/karmaapp/post/cache/PostRedisCache.java#L36)]
as long as no filtering rules are applied (filter by username or visibility other than active). Each post state change 
which is persisted in database is being reflected to the cache. That is post score and visibility changes. Rating posts 
high enough might make them present in cache. Making post visibility hidden or deleted evicts it from cache if it was 
cached prior. Every post get cached if max cached posts count is not yet reached.

#### How is it implemented?

I use several redis structures for this:

- [Redis sorted sets](https://redis.io/docs/data-types/sorted-sets/) (ZSet) for preserving top posts rating (all cached posts). ZSet is set under the [KARMA_SCORE_ZSET_KEY](https://github.com/msik-404/karma-app/blob/main/src/main/java/com/msik404/karmaapp/post/cache/PostRedisCache.java#L27).
  ZSet contains Keys in [post_key](https://github.com/msik-404/karma-app/blob/main/src/main/java/com/msik404/karmaapp/post/cache/PostRedisCache.java#L43)
  format, each post_key has score which is post karmaScore. Score is being updated in real time, so that post score does not become stale.
  KARMA_SCORE_ZSET_KEY expires after [TIMEOUT](https://github.com/msik-404/karma-app/blob/main/src/main/java/com/msik404/karmaapp/post/cache/PostRedisCache.java#L31).

- [Redis hashes](https://redis.io/docs/data-types/hashes/) for storing all post non-image data. Each field is post_key
  and value is json serialized [PostDto.java](https://github.com/msik-404/karma-app/blob/main/src/main/java/com/msik404/karmaapp/post/dto/PostDto.java).
  There are as many fields as there are keys in ZSet.
  This hash is set under the [POST_HASH_KEY](https://github.com/msik-404/karma-app/blob/main/src/main/java/com/msik404/karmaapp/post/cache/PostRedisCache.java#L28),
  it expires after TIMEOUT.

- [Redis Strings](https://redis.io/docs/data-types/strings/) are used for storing image data. Each image binary data is found under the [post_image_key](https://github.com/msik-404/karma-app/blob/main/src/main/java/com/msik404/karmaapp/post/cache/PostRedisCache.java#L48).
  Each post_image_key
  has expiration time set to TIMEOUT which is one hour. post_image_key is set once the image is requested for the first time. Expiration time is reset each
  time the data is requested within TIMEOUT.

I used this [redis.conf](https://github.com/msik-404/karma-app/blob/main/redis.conf). The most important things
about it are that is uses: [AOF and RDB](https://redis.io/docs/management/persistence/).

My cache code uses [Redis pipelining](https://redis.io/docs/manual/pipelining/) when more than single operation needs to
be preformed. This improves efficiency, by reducing required number of request.

Because ZSet [ZRANGE](https://redis.io/commands/zrange/) cannot be trivially used for getting key-set paginated values I
had to come up with a solution. If reader is interested in details look inside [findNextNCached](https://github.com/msik-404/karma-app/blob/main/src/main/java/com/msik404/karmaapp/post/cache/PostRedisCache.java#L194)
method code and comments.

#### Note
Maximum amount of posts cached can exceed [MAX_CACHED_POSTS](https://github.com/msik-404/karma-app/blob/main/src/main/java/com/msik404/karmaapp/post/cache/PostRedisCache.java#L36)
this is because of the second rule for caching during rating posts positively:

```
Post will get cached if one of these two things take place at the time of rating the post:
- first: cache is not yet full.
- second: post karma score after rating is higher than the lowest score of a post in cache.
```
But this would be actually rare because all cached posts get expired after [TIMEOUT](https://github.com/msik-404/karma-app/blob/main/src/main/java/com/msik404/karmaapp/post/cache/PostRedisCache.java#L31).

# Environment variables
Backend requires five environment variables to be set:
- PSQL_HOSTNAME
- PSQL_USER
- PSQL_PASSWORD
- REDIS_HOSTNAME
- SECRET

for details see: [application.yaml](https://github.com/msik-404/karma-app/blob/main/src/main/resources/application.yaml)

Simply create .env and place it in the root of project.

For example:
```
PSQL_HOSTNAME=karma-app-psql
PSQL_USER=dev
PSQL_PASSWORD=dev
PSQL_NAME=karma-app-psql
REDIS_HOSTNAME=karma-app-redis
SECRET=BARDZO-POTĘŻNY-SEKRET-JAKI-DŁUGI
```
## Important notes
SECRET should have at least 32 bytes.

# Building the project
To get target folder and build the project with maven simply run:
```
./mvnw clean package -DskipTests
```

If one would like to build the project with running the tests, one must have docker installed on their machine and run:
```
./mvnw clean package
```

# Tests
Docker is required to run tests locally because I use [Testcontainers for Java](https://java.testcontainers.org/).

Code that is directly communicating with Redis and PostgreSQL is fully tested with integration tests.
There are also unit test for service layer, they use mocking from [Mockito](https://site.mockito.org/).
Test reside in [src/test](https://github.com/msik-404/karma-app/tree/main/src/test).

The rest of the code was tested manually using postman.

# Starting the microservice | deployment for testing

To start the app locally, docker compose is required.

In this repository one can find [docker-compose-yaml](https://github.com/msik-404/karma-app/blob/main/docker-compose.yaml).

To start the app one should run in the root of the project:
```
docker compose up
```
To stop all containers:
```
docker compose stop
```
To remove containers and their data:
```
docker compose down -v
```

# Further development
- Unfortunately this app does not have frontend yet. Maybe in the future I will create front for it. Because of the lack
  of front, CORS is not configured.
- Update post text or headline.
- Search posts by text option.
- Add comment section feature.
- Maybe some sort of subreddits feature.
