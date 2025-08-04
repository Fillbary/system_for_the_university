# University Course Registration System

Система для управления регистрацией студентов на университетские курсы с поддержкой конкурентного доступа.

## 📌 Основные функции
- Регистрация студентов на курсы
- Контроль лимитов мест (не более N студентов на курс)
- Потокобезопасная обработка параллельных запросов
- REST API для интеграции

## 🚀 Быстрый запуск

### Требования
- JDK 17+
- Maven 3.9+
- PostgreSQL или H2

```bash
# Клонировать репозиторий
git clone https://github.com/Fillbary/system_for_the_university.git

# Сборка и запуск
cd system_for_the_university
mvn spring-boot:run
```
Приложение доступно по адресу:
http://localhost:8080

## 🔍 API Endpoints

Основные эндпоинты:
``` bash
POST /api/registrations - регистрация студента

GET /api/courses/all - список всех курсов

GET /api/courses/id - данные по конкретному курсу

POST /api/courses - создание курса

GET /api/courses/available - доступные для регистрации курсы
```
## 🧪 Тестирование

```bash
# Запуск тестов
mvn test
```
## 🛠 Технологии

Backend: Java 17, Spring Boot 3.1
База данных: PostgreSQL (prod)
Тестирование: JUnit 5, Mockito
