# Log Ingestion Service

The Log Ingestion Service is responsible for receiving log entries from various sources and publishing them to RabbitMQ for processing.

## Features

- ✅ REST API for log ingestion
- ✅ Request validation with detailed error messages
- ✅ RabbitMQ integration for async processing
- ✅ Automatic log enrichment (timestamps, metadata)
- ✅ Swagger/OpenAPI documentation
- ✅ Health check endpoint
- ✅ Comprehensive error handling
- ✅ Unit and integration tests

## API Endpoints

### POST /api/v1/logs
Ingest a log entry.

**Request Body:**
```json
{
  "timestamp": "2024-01-09T14:00:00.000Z",
  "level": "ERROR",
  "message": "Database connection failed",
  "service": "user-service",
  "host": "prod-server-01",
  "environment": "production",
  "metadata": {
    "errorCode": "DB_CONN_001",
    "retryCount": 3
  },
  "traceId": "trace-123-456",
  "spanId": "span-abc-def"
}
```

**Required Fields:**
- `level`: One of ERROR, WARN, INFO, DEBUG, TRACE
- `message`: Log message (max 10,000 characters)
- `service`: Service name (max 100 characters)

**Optional Fields:**
- `timestamp`: ISO 8601 timestamp (auto-generated if not provided)
- `host`: Host name
- `environment`: Environment name (defaults to "unknown")
- `metadata`: Additional key-value pairs
- `traceId`: Distributed tracing ID
- `spanId`: Span ID for tracing

**Response (202 Accepted):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "accepted",
  "timestamp": "2024-01-09T14:00:01.000Z",
  "message": "Log entry accepted for processing"
}
```

**Error Response (400 Bad Request):**
```json
{
  "timestamp": "2024-01-09T14:00:01.000Z",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "level": "Log level must be one of: ERROR, WARN, INFO, DEBUG, TRACE",
    "message": "Message is required"
  }
}
```

### GET /api/v1/logs/health
Health check endpoint.

**Response (200 OK):**
```json
{
  "status": "UP",
  "service": "log-ingestion"
}
```

## Running the Service

### Prerequisites
- Java 17+
- Maven 3.9+
- Docker (for RabbitMQ and PostgreSQL)

### Start Dependencies
```bash
# From project root
docker-compose up -d
```

### Run the Service
```bash
cd backend/log-ingestion
./mvnw spring-boot:run
```

The service will start on port **8081**.

### Run Tests
```bash
# Unit tests
./mvnw test

# Integration tests
./mvnw verify

# With coverage report
./mvnw clean test jacoco:report
```

### Test the API
```bash
# From project root
bash scripts/test-log-ingestion.sh

# Or manually with curl
curl -X POST http://localhost:8081/api/v1/logs \
  -H "Content-Type: application/json" \
  -d '{
    "level": "INFO",
    "message": "Test log message",
    "service": "test-service"
  }'
```

## Configuration

Configuration is in `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: log-ingestion-service
  
  datasource:
    url: jdbc:postgresql://localhost:5432/ai_monitoring
    username: admin
    password: admin123
  
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: admin123

server:
  port: 8081
```

### Environment-Specific Configuration
Create `application-{profile}.yml` for different environments:
- `application-local.yml` - Local development
- `application-dev.yml` - Development environment
- `application-prod.yml` - Production environment

Run with specific profile:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Architecture

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ HTTP POST
       ▼
┌─────────────────┐
│ LogController   │
│  - Validation   │
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│LogIngestionSvc  │
│  - Enrichment   │
│  - Publishing   │
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│   RabbitMQ      │
│  logs.exchange  │
│  logs.raw queue │
└─────────────────┘
```

## RabbitMQ Configuration

- **Exchange**: `logs.exchange` (topic)
- **Queue**: `logs.raw` (durable)
- **Routing Key**: `logs.raw`
- **Dead Letter Queue**: `logs.dlq`

## Monitoring

### Actuator Endpoints
Available at `/actuator`:
- `/actuator/health` - Health status
- `/actuator/info` - Application info
- `/actuator/metrics` - Metrics
- `/actuator/prometheus` - Prometheus metrics

### Swagger UI
Access API documentation at:
- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI JSON: http://localhost:8081/v3/api-docs

## Development

### Project Structure
```
src/
├── main/
│   ├── java/com/ibm/aimonitoring/ingestion/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── exception/       # Exception handlers
│   │   ├── model/           # Domain models
│   │   └── service/         # Business logic
│   └── resources/
│       └── application.yml  # Configuration
└── test/
    └── java/com/ibm/aimonitoring/ingestion/
        ├── controller/      # Controller tests
        └── service/         # Service tests
```

### Adding New Features
1. Create DTOs in `dto/` package
2. Add business logic in `service/` package
3. Create REST endpoints in `controller/` package
4. Write tests in `test/` directory
5. Update this README

## Troubleshooting

### Port Already in Use
```bash
# Find process using port 8081
lsof -i :8081  # macOS/Linux
netstat -ano | findstr :8081  # Windows

# Kill the process
kill -9 <PID>
```

### RabbitMQ Connection Failed
```bash
# Check if RabbitMQ is running
docker ps | grep rabbitmq

# Restart RabbitMQ
docker-compose restart rabbitmq

# Check logs
docker-compose logs rabbitmq
```

### Database Connection Failed
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Test connection
docker exec -it ai-monitoring-postgres psql -U admin -d ai_monitoring

# Check logs
docker-compose logs postgres
```

## Contributing

1. Create a feature branch
2. Make your changes
3. Write/update tests
4. Ensure all tests pass: `./mvnw test`
5. Update documentation
6. Submit a pull request

## License

MIT License - See LICENSE file for details