# Scheduler-backend-2

## Описание проекта

**Scheduler-backend-2** — это backend-сервис на Kotlin и Spring Boot для управления пользовательским расписанием. Сервис предоставляет REST API для создания повторяющихся шаблонов событий, разовых событий и заметок к ним, а также автоматически генерирует актуальное расписание на основе шаблонов.

### Возможности

- **Управление пользователями** — создание и получение списка пользователей.
- **Шаблоны событий** — создание еженедельных шаблонов на конкретный день недели (понедельник–воскресенье) с указанием времени начала/окончания и описания.
- **Разовые события** — создание, обновление, удаление и получение событий на конкретную дату.
- **Автоматическая генерация событий** — по шаблонам автоматически создаются события на текущую и будущие недели.
- **Заметки** — прикрепление текстовых заметок с флагами к событиям.
- **Плановое обновление расписания** — каждое воскресенье в 03:00 сервис удаляет устаревшие события (старше 5 недель) и добавляет новые на 5 недель вперёд.
- **Ручное управление расписанием** — force-эндпоинты для принудительного обновления/создания/удаления будущих событий.
- **Наблюдаемость** — отправка логов в Loki и визуализация через Grafana.
- **Резервное копирование БД** — ежедневные бэкапы PostgreSQL через cron.

### Технологический стек

| Компонент | Технология |
|-----------|------------|
| Язык | Kotlin 1.9.24 |
| Фреймворк | Spring Boot 3.3.0 |
| ORM / БД | Spring Data JPA, Hibernate, PostgreSQL |
| Миграции БД | Flyway |
| Сборка | Gradle |
| Контейнеризация | Docker, Docker Compose |
| Логирование | Logback + Loki4j |
| Визуализация логов | Grafana |
| JDK | Amazon Corretto 17 |

### Основные сущности

- `profile` — пользователи.
- `events_template` — шаблоны повторяющихся событий (день недели, время, описание).
- `events_current` — конкретные события на определённую дату, связанные с пользователем и/или шаблоном.
- `note` — заметки к событиям.

## Развёртывание проекта

### Требования

- JDK 17+
- Gradle (или Gradle Wrapper `./gradlew`)
- Docker и Docker Compose
- PostgreSQL 15+ (если запуск без Docker)

### Локальный запуск для разработки

1. Убедитесь, что `application.properties` заполнен корректными параметрами подключения к PostgreSQL:

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/scheduler_db
   spring.datasource.username=your_user
   spring.datasource.password=your_password
   ```

2. Запустите PostgreSQL локально или через Docker.

3. Соберите и запустите приложение:

   ```bash
   ./gradlew bootRun
   ```

   Или соберите JAR и запустите его:

   ```bash
   ./gradlew bootJar
   java -jar build/libs/Scheduler-backend-2-0.0.1-SNAPSHOT.jar
   ```

4. API будет доступно по адресу: `http://localhost:8080/api/...`

### Запуск через Docker Compose (рекомендуется для stage/prod)

Для развёртывания подготовлены окружения:

- `env/prod/` — production-окружение.
- `env/stage/` — stage-окружение.
- `env/common/` — общие файлы для сборки БД, скрипты инициализации и резервного копирования.

#### Шаги развёртывания

1. **Подготовьте файл переменных окружения.**

   В директории `env/prod/` или `env/stage/` создайте/отредактируйте файл `.env` (не включается в репозиторий). В нём должны быть заданы переменные, используемые в `docker-compose.yml`:

   ```dotenv
   APP_PORT=8080
   DB_PORT=5432
   LOKI_PORT=3100
   GRAFANA_PORT=3000
   POSTGRES_DB=scheduler_db
   POSTGRES_USER=your_user
   POSTGRES_PASSWORD=your_password
   ```

2. **Соберите JAR-файл приложения.**

   ```bash
   ./gradlew bootJar
   ```

3. **Запустите окружение.**

   Для production:

   ```bash
   cd env/prod
   docker compose up --build -d
   ```

   Для stage:

   ```bash
   cd env/stage
   docker compose up --build -d
   ```

4. **Проверьте работу сервисов.**

   | Сервис | URL |
   |--------|-----|
   | Приложение | `http://localhost:${APP_PORT}` |
   | Grafana | `http://localhost:${GRAFANA_PORT}` |
   | Loki | `http://localhost:${LOKI_PORT}` |

### Что включает Docker Compose

- **`app`** — контейнер с backend-приложением на Amazon Corretto 17.
- **`db`** — PostgreSQL 15 с настроенным cron для ежедневных бэкапов в `/backups`.
- **`loki`** — сборка и хранение логов.
- **`grafana`** — визуализация логов с автоматически provisioned источником данных Loki.

### Бэкапы базы данных

В образ PostgreSQL добавлен cron-джоб, который каждый день в 03:00 создаёт дамп базы в `/backups/db_backup_YYYY-MM-DD.sql`.

При первом запуске, если в `/backups` нет дампов, БД инициализируется из файла `env/common/initial_data.sql`.

### Основные REST-эндпоинты

#### Пользователи

- `GET /api/users` — список всех пользователей.
- `POST /api/users` — создать пользователя.

#### Расписание

- `GET /api/schedule?userId={id}` — получить все события пользователя.
- `POST /api/schedule?userId={id}` — создать/обновить шаблон события.
- `DELETE /api/schedule?templateId={id}` — удалить шаблон.

#### Разовые события

- `POST /api/schedule/single?userId={id}` — создать/обновить разовое событие.
- `DELETE /api/schedule/single?eventId={id}` — удалить разовое событие.

#### Заметки

- `POST /api/schedule/note?eventId={id}` — создать/обновить заметку.
- `DELETE /api/schedule/note?noteId={id}` — удалить заметку.

#### Служебные force-операции

- `POST /api/schedule/force/update` — принудительно запустить еженедельное обновление.
- `POST /api/schedule/force/create_new` — создать события по шаблонам на 4 недели вперёд.
- `POST /api/schedule/force/create_new_n?weeks={n}` — создать события по шаблонам на `n` недель вперёд.
- `DELETE /api/schedule/force/delete_future` — удалить все будущие события.