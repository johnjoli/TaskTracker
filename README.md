# Task Tracker

Учебный backend-проект на `Spring Boot 3`, в котором мы постепенно пройдём путь от простого CRUD до более production-подобного сервиса.

## Стек

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Flyway
- H2 для быстрых тестов и старта
- PostgreSQL для локальной разработки

## Запуск

Запуск тестов:

```bash
mvn test
```

Запуск приложения на H2:

```bash
mvn spring-boot:run
```

Запуск PostgreSQL:

```bash
docker compose up -d
```

Запуск приложения с локальным профилем:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Тестовый admin-пользователь создаётся автоматически:

- `username: admin`
- `password: admin12345`

## API

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/tasks`
- `GET /api/tasks?page=0&size=10&status=TODO&priority=HIGH&q=tracker&createdBy=alice&assignee=bob`
- `GET /api/tasks/{id}`
- `PUT /api/tasks/{id}`
- `PATCH /api/tasks/{id}`
- `DELETE /api/tasks/{id}`
