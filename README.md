# Banking Application

## Описание проекта

Банковское приложение с функциями авторизации, денежных переводов, поиска пользователей и управления счетами. Приложение предоставляет безопасный и надежный функционал для выполнения основных банковских операций.

## Технологии

- **Java 17**
- **Spring Boot 3.x**
- **Spring Security** - для авторизации и аутентификации
- **Spring Data JPA** - для работы с базой данных
- **PostgreSQL** - основная база данных для хранения информации
- **Redis** - для кэширования и ускорения поиска
- **JWT** - для безопасной аутентификации
- **JUnit 5 & Mockito** - для тестирования
- **Hibernate** - ORM для работы с базой данных
- **Lombok** - для уменьшения шаблонного кода

## Настройка проекта

### Предварительные требования

- JDK 17+
- Maven 3.8+
- Docker и Docker Compose
- PostgreSQL (или использовать Docker)
- Redis (или использовать Docker)

### Установка

1. Клонировать репозиторий:
   ```bash
   git clone https://github.com/yourusername/bank-app.git
   cd bank-app
   ```

2. Запустить базы данных с помощью Docker (если не установлены локально):
   ```bash
   docker-compose up -d postgres redis
   ```

3. Скомпилировать проект:
   ```bash
   mvn clean package -DskipTests
   ```

## Запуск приложения

### Через Maven

```bash
mvn spring-boot:run
```

### Через Docker

```bash
docker-compose up -d
```

### Запуск вручную через JAR

```bash
java -jar target/bank-0.0.1-SNAPSHOT.jar
```

## Конфигурация

Основные настройки находятся в файле `application.properties`. Для разработки можно создать файл `application-dev.properties` с настройками локальной среды.

Пример основных настроек:

```properties
# Подключение к базе данных
spring.datasource.url=jdbc:postgresql://localhost:5432/bank-app
spring.datasource.username=postgres
spring.datasource.password=yourpassword

# Настройки Redis
spring.redis.host=localhost
spring.redis.port=6379

# JWT настройки
jwt.secret=yourJwtSecretKey
jwt.access-token-expiration=3600000
jwt.refresh-token-expiration=86400000
```

## Основные функции

### Аутентификация и авторизация
- Регистрация новых пользователей
- Вход по email или телефону
- JWT токены для аутентификации
- Обновление токенов

### Денежные переводы
- Перевод средств между пользователями
- Проверка остатка средств
- Валидация лимитов перевода
- Атомарные транзакции с блокировками

### Поиск пользователей
- Поиск по имени с использованием Redis
- Фильтрация по email, телефону, дате рождения
- Постраничная навигация и сортировка

### Управление счетами
- Создание счета
- Начисление процентов
- Просмотр истории транзакций

## Тестирование

### Запуск тестов

```bash
mvn test
```

### Запуск интеграционных тестов

```bash
mvn verify
```

## API Документация

После запуска приложения документация Swagger доступна по адресу:
```
http://localhost:8080/swagger-ui.html
```

## Примеры запросов

### Регистрация пользователя
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Иван Иванов",
    "emails": [{"email": "ivan@example.com"}],
    "phones": [{"phone": "79001234567"}],
    "password": "password123",
    "dateOfBirth": "01.01.1990"
  }'
```

### Вход в систему
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "login": "ivan@example.com",
    "password": "password123"
  }'
```

### Выполнение перевода
```bash
curl -X POST http://localhost:8080/api/transfers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "recipientId": 2,
    "amount": 100.00
  }'
```

## Структура проекта

```
src/
├── main/
│   ├── java/com/example/bank/
│   │   ├── config/          # Конфигурация Spring
│   │   ├── controller/      # REST контроллеры
│   │   ├── dto/             # Объекты передачи данных
│   │   ├── event/           # События
│   │   ├── exception/       # Обработка исключений
│   │   ├── filter/          # Фильтры безопасности
│   │   ├── handler/         # Обработчики событий
│   │   ├── listener/        # Слушатели событий
│   │   ├── mapper/          # Маперы DTO <-> Entity
│   │   ├── model/           # Модели данных
│   │   ├── repository/      # Репозитории для работы с БД
│   │   └── service/         # Бизнес-логика
│   └── resources/           # Конфигурационные файлы
└── test/                    # Тесты
```

## Лицензия

MIT 