# Provision

Заготовка сервиса управления приборами учёта электроэнергии.

## Структура

- `backend` — Spring Boot REST API.
- `frontend` — Vue 3 UI для работы с API.
- `compose.yaml` — локальная инфраструктура: PostgreSQL и Mailpit.

## Запуск

Запустите локальную инфраструктуру:

```bash
docker compose up -d
```

Затем запустите backend локально:

```bash
cd backend
./gradlew bootRun
```

И frontend в отдельном терминале (Node.js 20.19+):

```bash
cd frontend
npm install
npm run dev
```

После запуска:

- API: `http://localhost:8080`
- Frontend: `http://localhost:5173`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Actuator health: `http://localhost:8080/actuator/health`
- Mailpit UI: `http://localhost:8025`

Переменные окружения перечислены в `.env.example`. Не добавляйте рабочий `.env` в Git.
