# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

MediTrack is a clinic appointment & billing REST API built with Spring Boot. **This is an intentionally-messy course starter** (`README.md`): it compiles and runs, but deliberately violates the conventions the assignment teaches. The `// SMELL:` comments scattered through the code mark the anti-patterns on purpose — they are the assignment backlog, not bugs to leave alone. When refactoring, expect to *remove* these smells and their comments.

Known intentional smells (from README + inline comments): money as `double` instead of `BigDecimal`; `@Data` on JPA entities; entities returned straight from controllers (no DTOs); all business logic in controllers (no service layer); field injection with `@Autowired`; no exception handling (missing rows return `null`, generic `RuntimeException` → HTTP 500); free-text `status` with no transition rules; `cancel` doesn't restore the doctor's slot or refund payment; `ddl-auto=update` and a plaintext DB password; no validation, no tests, no API docs.

## Commands

No Maven wrapper is bundled (adding one via `mvn -N wrapper:wrapper` is an intended first task). Use a system Maven:

- Build: `mvn compile` / package: `mvn package`
- Run: `mvn spring-boot:run` (starts on `http://localhost:8080`)
- Tests: `mvn test` — **there is no test starter dependency and no tests yet.** Adding `spring-boot-starter-test` to `pom.xml` is part of the assignment before any test can run. Run a single test once tests exist: `mvn test -Dtest=ClassName#methodName`.

## Database setup

The project targets **PostgreSQL** end-to-end: `db/meditrack_schema.sql` (Postgres syntax), the `org.postgresql` driver in `pom.xml`, and `application.properties` (`jdbc:postgresql://localhost:5432/meditrack_db`, user/pass `postgres`/`postgres`).

Run `db/meditrack_schema.sql` in psql or pgAdmin to create `meditrack_db` with tables and seed data **before** starting the app. `ddl-auto=validate` means Hibernate owns nothing: it only checks the entity mappings against the existing schema at startup and refuses to boot if they don't match (it will not create or migrate tables). So the SQL file is mandatory, and any entity change must be mirrored in `meditrack_schema.sql`.

## Architecture

Single-module Maven project, package root `com.bharath.meditrack`. Standard flat Spring layering, currently missing its middle:

- `controller/` — `AppointmentController` (book/cancel/list), `CatalogController` (specialties + doctors, two resources in one class), `PatientController`. **These hold all business logic today**; the intended refactor introduces a `service/` layer and moves logic there.
- `model/` — JPA entities *and* one confusingly-named entity: `AppointmentService` is the **appointment line-item entity** (`appointment_services` table), NOT a Spring service. Don't create a service class with a colliding name. Entities: `Specialty` → `Doctor` (ManyToOne) → `Appointment` (ManyToOne patient+doctor) → `AppointmentService` line items (OneToMany) + `Payment` (OneToOne). All associations are `FetchType.EAGER`.
- `repo/` — Spring Data JPA repositories. `AppointmentRepository.countByDoctorAndScheduledDate` backs the per-doctor daily-capacity check in `book`.

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
