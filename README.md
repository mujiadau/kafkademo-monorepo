# pgbank – a playground banking microservices monorepo

A small event-driven banking playground built with Spring Boot and Kafka.

## Architecture

```
                 POST /api/transactions
                 { customerId, amount }
                          │
                          ▼
                ┌───────────────────┐
                │transaction-service│  (REST API + Kafka producer)
                └─────────┬─────────┘
                          │  topic: "transactions"
                          ▼
              ┌───────────┴───────────┐
              ▼                       ▼
   ┌───────────────────┐   ┌────────────────────┐
   │  account-service  │   │  customer-service  │
   │ (Kafka consumer)  │   │  (Kafka consumer)  │
   │ writes balance to │   │ "sends" an email:  │
   │ Postgres          │   │ "sent email: +20"  │
   └───────────────────┘   └────────────────────┘
```

### Modules

| Module                | Responsibility                                                                 |
|-----------------------|--------------------------------------------------------------------------------|
| `common`              | Shared `TransactionEvent` DTO published/consumed over Kafka.                   |
| `transaction-service` | Secured REST API. Accepts a transaction and produces a Kafka event. Port 8083. |
| `account-service`     | Consumes events and stores the new balance in Postgres. Port 8082.              |
| `customer-service`    | Consumes events and "sends an email" (prints a log line).                      |

## Security

`transaction-service` is an **OAuth 2.0 / OpenID Connect resource server**. Every
call to `POST /api/transactions` must present a valid **JWT Bearer token** issued
by [Keycloak](https://www.keycloak.org/) and containing the `transactions:write`
OAuth 2.0 scope (mapped by Spring Security to the `SCOPE_transactions:write`
authority). Tokens are validated against the realm's JWKS endpoint (signature,
expiry and issuer). The `/actuator/health` endpoint stays public for probes.

The identity provider is configured by the `keycloak/realm-export.json` file,
which is imported automatically on startup and provisions:

- realm `kafkademo`
- a public client `kafkademo-client` (with the *Direct Access Grants* / password flow enabled)
- a client scope `transactions:write`
- a demo user `alice` / password `alice`

### Getting a token

Because the token `iss` (issuer) claim must match what the service validates
against, add the following line to your `/etc/hosts` so both your host and the
containers reach Keycloak under the same name:

```
127.0.0.1 keycloak
```

Then request a token and call the API:

```bash
TOKEN=$(curl -s http://keycloak:8080/realms/kafkademo/protocol/openid-connect/token \
  -d grant_type=password \
  -d client_id=kafkademo-client \
  -d username=alice \
  -d password=alice \
  -d 'scope=openid transactions:write' | jq -r .access_token)

curl -X POST http://localhost:8083/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"customerId": 1, "amount": 20.00}'
```

Calling the endpoint without a token (or with an invalid one) returns `401`.

## Running locally

Everything (Kafka, Postgres, Keycloak and all three services) is wired up in
`docker-compose.yml`. Docker builds each service image from its `Dockerfile`.

```bash
# Build the service images and start the whole stack.
docker compose up --build -d
```

The Keycloak admin console is available at <http://localhost:8080>
(user `admin` / password `admin`).

### Running the services from source instead

If you prefer to run the services from Gradle, start only the infrastructure.
Each service is an independent Gradle build; run the commands below from the
repository root in separate terminals. Kafka is exposed on `localhost:29092`.

```bash
docker compose up -d kafka postgres keycloak

(cd transaction-service && ./gradlew bootRun)
(cd account-service && ./gradlew bootRun)
(cd customer-service && ./gradlew bootRun)
```

Then send a transaction (see [Getting a token](#getting-a-token) above):

```bash
curl -X POST http://localhost:8083/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"customerId": 1, "amount": 20.00}'
```

- `account-service` logs `Updated balance for customer 1 -> 20.00` and stores it in Postgres.
- `customer-service` logs `sent email: +20 CHF`.

Use a negative `amount` to withdraw money.

## Docker images

Each service has a multi-stage `Dockerfile` (Gradle build stage + slim JRE
runtime stage) at its module root. Build a single image from the repository root:

```bash
docker build -f transaction-service/Dockerfile -t kafkademo/transaction-service .
docker build -f account-service/Dockerfile     -t kafkademo/account-service .
docker build -f customer-service/Dockerfile     -t kafkademo/customer-service .
```

## Tests

Each service ships an integration test powered by [Testcontainers](https://testcontainers.com).
The `transaction-service` test additionally boots a real Keycloak container and
verifies that requests succeed with a valid JWT and are rejected (`401`) without
one. Docker must be running.

Run each service's tests from the repository root:

```bash
(cd transaction-service && ./gradlew test)
(cd account-service && ./gradlew test)
(cd customer-service && ./gradlew test)
```

