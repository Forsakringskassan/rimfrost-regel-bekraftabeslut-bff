# rimfrost-regel-bekraftabeslut-bff

Backend-for-frontend for the bekräfta beslut rule. Proxies and transforms decision data from the bekraftabeslut backend service.

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_** Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:9003/q/dev/>.

The application runs on port **9003** by default.

To run the full build locally (mirrors CI, skips Docker):

```shell script
./mvnw verify -Dquarkus.container-image.build=false
```

## Environment variables

| Variable | Default | Description |
|---|---|---|
| `BE_BEKRAFTABESLUT_URL` | `http://localhost:8891` | Base URL for the bekraftabeslut backend |
| `CORS_ORIGINS` | _(dev: localhost:3000, localhost:3030)_ | Allowed CORS origins — set in production via this env var |

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

## Packaging and running as Docker

Build a Docker image _rimfrost/rimfrost-regel-bekraftabeslut-bff:latest_ (requires Docker to be running locally):

```shell script
./mvnw package -Dquarkus.container-image.build=true
```

Launch container:

```shell script
docker run -p 9003:9003 \
  -e BE_BEKRAFTABESLUT_URL=http://host.docker.internal:8891 \
  rimfrost/rimfrost-regel-bekraftabeslut-bff
```

## REST endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/regel/bekraftabeslut/{path}` | Referensdata — `path` is one of `avslutstyp`, `beslutstyp`, `beslutsutfallstyp`, `yrkandestatus` |
| `GET` | `/api/regel/bekraftabeslut/handlaggning/{handlaggningId}` | Fetch and transform decision data |
| `PATCH` | `/api/regel/bekraftabeslut/{handlaggningId}` | Confirm decision |
| `GET` | `/api/uppgiftsbeskrivning` | Extended task description |
| `POST` | `/api/regel/bekraftabeslut/done` | Mark task as done — forwards `Authorization` header to backend |

Health (readiness + backend connectivity check): <http://localhost:9003/q/health>
