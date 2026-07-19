# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

MediTrack is a clinic appointment & billing REST API built with Spring Boot. **This is an intentionally-messy course starter** (`README.md`): it compiles and runs, but deliberately violates the conventions the assignment teaches. The `// SMELL:` comments scattered through the code mark the anti-patterns on purpose — they are the assignment backlog, not bugs to leave alone. When refactoring, expect to *remove* these smells and their comments.

The convention refactor is now largely done: constructor injection, `BigDecimal` money, DTOs in/out of controllers, a `service/` layer, `/api/v1` routing, a `GlobalExceptionHandler` returning `ErrorResponse`, bean validation, springdoc Swagger UI, PostgreSQL, and `ddl-auto=validate` are all in place, with unit + `@WebMvcTest` tests. Remaining intentional smells still open: free-text appointment `status` with no transition rules; `cancel` doesn't restore the doctor's slot or refund payment; plaintext DB password in `application.properties`. Entities keep `createdAt/updatedAt` set in code (no auditing).

## Commands

No Maven wrapper is bundled (adding one via `mvn -N wrapper:wrapper` is an intended first task). Use a system Maven:

- Build: `mvn compile` / package: `mvn package`
- Run: `mvn spring-boot:run` (starts on `http://localhost:8080`)
- Tests: `mvn test` — **there is no test starter dependency and no tests yet.** Adding `spring-boot-starter-test` to `pom.xml` is part of the assignment before any test can run. Run a single test once tests exist: `mvn test -Dtest=ClassName#methodName`.

## Database setup

The project targets **PostgreSQL** end-to-end: `db/meditrack_schema.sql` (Postgres syntax), the `org.postgresql` driver in `pom.xml`, and `application.properties` (`jdbc:postgresql://localhost:5432/meditrack_db`, user/pass `postgres`/`postgres`).

Run `db/meditrack_schema.sql` in psql or pgAdmin to create `meditrack_db` with tables and seed data **before** starting the app. `ddl-auto=validate` means Hibernate owns nothing: it only checks the entity mappings against the existing schema at startup and refuses to boot if they don't match (it will not create or migrate tables). So the SQL file is mandatory, and any entity change must be mirrored in `meditrack_schema.sql`.

## Architecture

Single-module Maven project, package root `com.venkat.meditrack`. Layered:

- `controller/` — thin: `SpecialtyController`, `DoctorController`, `PatientController`, `AppointmentController`. They only bind HTTP + `@Valid` DTOs and delegate to services. All under `/api/v1`; each carries a springdoc `@Tag` for its Swagger group.
- `service/` — business logic + entity↔DTO mapping: `SpecialtyService`, `DoctorService`, `PatientService`, and `BookingService` (booking/cancel; **named BookingService, not AppointmentService, because that name is the line-item entity**).
- `dto/` — request DTOs (`@Data`, bean-validation constraints) and response DTOs (`@Data`, with a static `from(entity)` mapper).
- `entity/` — JPA entities (`@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor`). `AppointmentService` here is the **line-item entity** (`appointment_services` table), NOT a Spring service. Graph: `Specialty` → `Doctor` (ManyToOne) → `Appointment` (ManyToOne patient+doctor) → `AppointmentService` line items (OneToMany) + `Payment` (OneToOne). All associations `FetchType.EAGER`.
- `repository/` — Spring Data JPA. `AppointmentRepository.countByDoctorAndScheduledDate` backs the daily-capacity rule; `existsBy*` finders back duplicate checks.
- `exception/` — `GlobalExceptionHandler` (`@RestControllerAdvice`) maps `ResourceNotFoundException`→404, `DuplicateResourceException`→409, `BusinessRuleException`→422, validation→400, else→500, all as `ErrorResponse` JSON.
- `config/` — `OpenApiConfig` (springdoc; UI at `/swagger-ui.html`, spec at `/v3/api-docs`).

Note `Appointment`'s PK column is `appointment_id` (not `id`); other entities use `id`.

## Conventions for changes

These are **required** for this project and are non-negotiable — new and refactored code must satisfy all of them:

- **Dependency injection:** constructor injection via `@RequiredArgsConstructor` + `final` fields. Never `@Autowired` / field injection.
- **Money:** `BigDecimal` for every money field and all money arithmetic. Never `double` or `float`.
- **DTOs:** controllers accept and return DTOs only. Never expose JPA entities across the controller boundary (no entity `@RequestBody`, no entity in a response). Map entity ↔ DTO in the service layer.
- **Thin controllers:** controllers only bind HTTP, validate input, and delegate. All business logic lives in a `service/` layer.
- **Routing:** every endpoint is under `/api/v1` (e.g. `/api/v1/appointments`, `/api/v1/doctors`).
- **Error handling:** centralised via `@RestControllerAdvice`. All errors return a consistent `ErrorResponse` JSON body (do not leak stack traces or generic 500s; map domain/not-found/validation failures to appropriate status codes).
- **Schema ownership:** `spring.jpa.hibernate.ddl-auto=validate`. Tables come from `db/meditrack_schema.sql`; Hibernate must not create or mutate schema. Entity mappings must match the SQL schema.

These align with the user's global conventions (`~/.claude/CLAUDE.md`): entities annotated field-by-field (`@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor`, never `@Data`); DTOs use `@Data @Builder @NoArgsConstructor @AllArgsConstructor`; tests in JUnit 5 + Mockito + AssertJ (`assertThat`, never `assertEquals`) + MockMvc. Show diffs rather than full rewrites, and ask before deleting files.

# meditrack-starter

## Stack
- Java 21
- Spring Boot 4.1.0
- Maven
- PostgreSQL

## Package Structure
Base package: `com.venkat.meditrack`

Sub-packages:
- `controller`
- `service`
- `repository`
- `entity`
- `dto`
- `exception`
- `config`

## Architecture Rules
- Always use DTOs in controllers — never expose entities directly.
- All business logic lives in the service layer — keep controllers thin.
- Centralised exception handling via `GlobalExceptionHandler`.
- All endpoints prefixed with `/api/v1/`.

## Lombok
- `@RequiredArgsConstructor` for injection everywhere (with `final` fields). No `@Autowired`.
- Entities: `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor`.
- DTOs: `@Data @Builder @NoArgsConstructor @AllArgsConstructor`.

## Exceptions
All handled in `GlobalExceptionHandler`, returning `ErrorResponse` JSON:

| Exception                    | HTTP Status |
|------------------------------|-------------|
| `ResourceNotFoundException`  | 404         |
| `BusinessRuleException`      | 422         |
| `DuplicateResourceException` | 409         |
| `AccessDeniedException`      | 403         |

## Database
- PostgreSQL, database `meditrack_db`.
- `ddl-auto=validate` — tables come from `db/meditrack_schema.sql`, not Hibernate.

## Commands
No Maven wrapper is bundled yet, so use system `mvn` (swap `mvn`→`./mvnw` once a wrapper is added):
- Run: `mvn spring-boot:run`
- Test: `mvn test`
- Compile check: `mvn clean compile`

