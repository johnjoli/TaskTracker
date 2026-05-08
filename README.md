# Task Tracker API

Backend-проект на `Spring Boot 3` для управления задачами, пользователями и комментариями.

Проект покрывает типичные backend-задачи:
- JWT-аутентификацию и роли
- CRUD для задач
- фильтрацию, поиск и пагинацию
- action endpoints для задач
- комментарии к задачам
- unit/service tests

## Stack

- Java 17
- Spring Boot 3.3
- Spring Web
- Spring Data JPA
- Spring Security
- Flyway
- H2
- PostgreSQL
- JUnit 5
- Mockito
- Testcontainers

## Features

### Auth and users

- регистрация пользователя
- логин с выдачей JWT
- получение текущего пользователя
- просмотр пользователей
- смена роли пользователя
- автоматический bootstrap admin-пользователь

### Tasks

- создание, чтение, обновление и удаление задач
- фильтры по:
  - `status`
  - `priority`
  - `dueDateFrom`
  - `dueDateTo`
  - `createdBy`
  - `assignee`
  - `onlyMine`
- полнотекстовый поиск по `title` и `description`
- пагинация и сортировка
- отдельный endpoint `GET /api/tasks/my`

### Task actions

- `complete`
- `assign-to-me`
- `unassign`

### Comments

- добавить комментарий к задаче
- получить список комментариев задачи
- редактировать комментарий
- удалить комментарий

## Project Structure

```text
com.tasktracker
├── auth
│   ├── api
│   ├── config
│   ├── domain
│   ├── repository
│   ├── service
│   └── validation
├── common
│   ├── api
│   └── exception
└── task
    ├── api
    ├── entity
    ├── repository
    └── service
```

## Run

### 1. Run with default profile

По умолчанию приложение стартует на H2:

```bash
mvn spring-boot:run
```

H2 datasource настроен в [application.yml](/Users/hulioiglesias/Documents/TaskTracker/src/main/resources/application.yml).

### 2. Run with PostgreSQL

Подними БД:

```bash
docker compose up -d
```

Запусти приложение с локальным профилем:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

PostgreSQL настройки лежат в [application-local.yml](/Users/hulioiglesias/Documents/TaskTracker/src/main/resources/application-local.yml).

## Default admin

Админ создается автоматически при старте приложения:

- username: `admin`
- password: `admin12345`

## Database migrations

Схема управляется через Flyway.

Текущие миграции:
- `V1__create_tasks_table.sql`
- `V2__create_users_and_link_tasks.sql`
- `V3__add_due_date_to_tasks.sql`
- `V4__create_task_comments_table.sql`

## Testing

### Unit and service tests

Можно запускать локально без Docker:

```bash
mvn -Dtest=TaskServiceTest test
```

или весь тестовый набор без интеграционных ограничений среды:

```bash
mvn test
```

### Integration tests

Интеграционные тесты используют `Testcontainers + PostgreSQL`.

Для них нужен:
- запущенный Docker Desktop
- корректный Docker environment для Testcontainers

Если Docker недоступен, integration tests могут быть пропущены.

## API Overview

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

### Users

- `GET /api/users/me`
- `GET /api/users?q=ali&role=USER&page=0&size=10`
- `GET /api/users/{id}`
- `PATCH /api/users/{id}/role`

### Tasks

- `POST /api/tasks`
- `GET /api/tasks`
- `GET /api/tasks/my`
- `GET /api/tasks/{id}`
- `PUT /api/tasks/{id}`
- `PATCH /api/tasks/{id}`
- `DELETE /api/tasks/{id}`

### Task actions

- `POST /api/tasks/{id}/complete`
- `POST /api/tasks/{id}/assign-to-me`
- `POST /api/tasks/{id}/unassign`

### Comments

- `POST /api/tasks/{id}/comments`
- `GET /api/tasks/{id}/comments`
- `PATCH /api/tasks/{taskId}/comments/{commentId}`
- `DELETE /api/tasks/{taskId}/comments/{commentId}`

## Example task filter request

```http
GET /api/tasks?status=TODO&priority=HIGH&dueDateFrom=2025-03-01T00:00:00&dueDateTo=2025-03-31T23:59:59&q=backend&onlyMine=true&page=0&size=10
```

## Password rules

Пароль должен:
- быть длиной от 8 до 100 символов
- содержать минимум одну заглавную букву
- содержать минимум одну строчную букву
- содержать минимум одну цифру
- содержать минимум один специальный символ
