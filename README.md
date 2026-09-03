# Provision

Заготовка сервиса управления приборами учёта электроэнергии.

## Структура

- `backend` — Spring Boot REST API.
- `frontend` — Vue 3 UI для работы с API.
- `compose.yaml` — backend, PostgreSQL и Mailpit.

## Запуск

Для запуска нужны Docker с Compose и Node.js 20.19+ для frontend.
Java и Gradle локально устанавливать не нужно: backend собирается внутри Docker.

Из корня проекта запустите backend и инфраструктуру:

```bash
docker compose up -d --build
```

Затем запустите frontend в отдельном терминале:

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

## Настройки

Compose работает с настройками по умолчанию. Чтобы изменить их, скопируйте шаблон:

```bash
cp .env.example .env
```

Отредактируйте `.env` и повторите `docker compose up -d --build`.
Compose автоматически читает этот файл; `.env.example` служит только примером.
Не добавляйте рабочий `.env` с паролями в Git.

Максимальный размер загружаемого CSV по умолчанию — 50 MB. Его можно изменить через
`MAX_UPLOAD_FILE_SIZE`; `MAX_UPLOAD_REQUEST_SIZE` должен быть немного больше, чтобы учесть multipart-заголовки.

## Обновление и остановка

После изменения backend пересоберите и запустите его:

```bash
docker compose up -d --build backend
```

Остановить контейнеры:

```bash
docker compose down
```

Данные PostgreSQL сохраняются в volume `postgres-data`. Не используйте `docker compose down -v`,
если хотите сохранить базу: флаг `-v` удаляет volume вместе с данными.
