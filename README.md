# Лабораторная работа по Микросервисам

Лаба состоит из двух микросервисов, единой точки входа (gateway), сервиса регистрации и обнаружения (Consul) и распределённой трассировки (Tempo + Grafana).

Все компоненты запускаются в контейнерах Docker одной командой `docker compose up -d`.

## Запуск и проверка работоспособности

### 1. Сборка JAR-файлов
Перед первым запуском необходимо собрать исполняемые JAR-файлы для каждого модуля. В корневой директории проекта выполните:

```bash
./gradlew clean bootJar
```

### 2. Запуск всех контейнеров

После успешной сборки JAR-файлов запустите все сервисы и вспомогательные инструменты:

```bash
docker-compose up -d
```

### 3. Проверка работы контейнеров

Для проверки, что все контейнеры работают:
```bash
docker ps
```

### 4. Запроса через gateway

Запрос на единую точку входа (порт 8888):
```bash
curl http://localhost:8888/api/service1
```

Ожидаемый ответ:
```json
{
  "service1": {
    "message": "Hello from service1",
    "timestamp": ...
  },
  "service2_data": {
    "service": "service2",
    "timestamp": "...",
    "data": ["Hello", "from", "service2"]
  }
}
```

### 5. Просмотр трассировки в Tempo (Grafana)
```json
http://localhost:3000
```

## Архитектура

| № | Требование | Реализация |
|---|------------|------------|
| 1 | Два микросервиса, один получает данные от второго | `service1` вызывает `service2`, объединяет результаты в `/api/service1` |
| 2 | Service discovery / registry | Используется Consul; все сервисы регистрируются и обращаются по логическим именам |
| 3 | Единая точка входа – gateway (порт 8888) | Gateway принимает запросы и перенаправляет в `service1`, пользователь не видит порты 8081/8082 |
| 4 | Запуск через `docker compose up -d` | Файл `docker-compose.yml` поднимает Consul, Tempo, Grafana, gateway, service1, service2 |
| 5 | Трассировка запроса в Tempo | В Grafana видны спаны `GET /api/service1` и `Call service2`, показывающие прохождение через оба сервиса |

### Компоненты

- **Gateway** – Spring Boot + WebFlux, проксирует запросы в `service1`.
- **service1** – основной сервис, генерирует ручные трейсы, вызывает `service2`.
- **service2** – вспомогательный сервис, возвращает свои данные.
- **Consul** – сервис регистрации и обнаружения.
- **Tempo** – хранилище трейсов (OTLP HTTP).
- **Grafana** – визуализация трейсов.

## Структура проекта

```plaintext
microservices-lab/
├── docker-compose.yml
├── settings.gradle.kts
├── config/
│   ├── tempo.yaml
│   └── grafana-datasources.yaml
├── service1/
│   ├── Dockerfile
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/example/service1/
│       ├── Service1Application.kt
│       ├── Service1Controller.kt
│       ├── TraceManual.kt
│       ├── AppConfig.kt
│       └── resources/application.yml
├── service2/
│   ├── Dockerfile
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/example/service2/
│       ├── Service2Application.kt
│       ├── Service2Controller.kt
│       └── resources/application.yml
└── gateway/
    ├── Dockerfile
    ├── build.gradle.kts
    └── src/main/kotlin/com/example/gateway/
        ├── GatewayApplication.kt
        ├── GatewayController.kt
        ├── AppConfig.kt
        └── resources/application.yml
```
