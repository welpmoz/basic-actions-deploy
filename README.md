# Spring Quiz API

A REST API that serves **40 advanced Spring Framework interview questions** across 8 topics, with 5 questions per topic.

- **Live:** https://spring-quiz-nh0m.onrender.com/api/questions. This is the Render free plan, so the first request after the service has been idle takes about 30–60 s.
- **Stack:** Java 21 · Spring Boot 3.5 · Spring Batch (chunk processing) · Spring Data JPA/H2 · Micrometer/Prometheus · Grafana · Docker · GitHub Actions · Render

## Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/questions` | All questions. Optional filters: `?topic=Transactions&difficulty=EXPERT` |
| GET | `/api/questions/{id}` | One question. An unknown id returns a `404` ProblemDetail |
| GET | `/api/questions/random` | A random question |
| GET | `/api/questions/topics` | The list of topics |
| GET | `/api/questions/stats` | Counts: `total`, `byTopic`, `byDifficulty` |
| GET | `/actuator/health` · `/actuator/info` · `/actuator/prometheus` | Health, the deployed commit SHA, and metrics |

`difficulty` must be `ADVANCED` or `EXPERT`. Any other value returns `400`.

## How the data is loaded (Spring Batch chunk)

At startup, `importQuestionsJob` runs one chunk step that commits every 10 items:

```
questions.csv ──FlatFileItemReader──▶ QuestionItemProcessor (validate/trim) ──RepositoryItemWriter──▶ H2
```

- If a row is invalid (missing id, blank field or unknown difficulty), the job fails fast.
- To add or change questions, edit `src/main/resources/data/questions.csv` and redeploy.

## Run locally

```bash
./mvnw verify                      # TDD suite: unit, slice, batch and integration tests
./mvnw spring-boot:run             # http://localhost:8080

docker compose up -d --build       # app :8080 + Prometheus :9090 + Grafana :3000
APP_PORT=8081 docker compose up -d # if port 8080 is already taken
```

- In Grafana (http://localhost:3000, login `admin`/`admin`), open the **Spring Quiz → Spring Quiz API** dashboard.
  - It shows request rate and p95 latency, questions served by topic, JVM heap, and batch items.
- To also scrape the Render instance, uncomment the `spring-quiz-render` job in `monitoring/prometheus.yml`.

## CI/CD (`.github/workflows/ci-cd.yml`)

```
push to main
  ├─ test    ./mvnw verify
  ├─ docker  build → push ghcr.io/welpmoz/basic-actions-deploy:<sha> + :latest → smoke-test the container
  └─ deploy  POST Render deploy hook with imgURL=<sha image>
             → poll /actuator/info until version == <sha> → check health and that 40 questions are served
```

Pull requests run only the `test` job.

| Setting | Type | Value |
|---|---|---|
| `RENDER_DEPLOY_HOOK` | Repo secret | From the Render service → Settings → Deploy Hook |
| `RENDER_SERVICE_URL` | Repo variable | `https://spring-quiz-nh0m.onrender.com`. The deploy job is skipped if this isn't set |

The Render service is defined in `render.yaml`, an image-backed Blueprint. Render only pulls the image that CI has already tested; it never builds the code itself.
