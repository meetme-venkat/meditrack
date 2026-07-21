---
name: production-readiness
description: Runs a build-and-test production readiness review of this Spring Boot project and returns findings grouped by severity with a GO / NO-GO verdict. Use this whenever the user asks whether the project is ready to ship, deploy, release, tag, merge to main, or hand off — and also for softer phrasings like "is this good enough?", "anything blocking?", "can we cut a release?", "review before I push", "what's left before prod?", or "sanity check the build". Prefer this over ad-hoc inspection whenever the question is about overall shippability rather than one specific file, since the point is a consistent, evidence-backed verdict rather than a tour of the code.
---

# Production readiness review

Answer one question: **would shipping this right now hurt?** Everything else is
supporting evidence.

The reader is about to decide whether to deploy. They need a verdict they can act
on in thirty seconds, and evidence they can verify in five minutes. A long list of
observations with no verdict is worse than useless — it pushes the judgement call
back onto them, which is the thing they asked you to do.

This review covers the **build and test gate** plus this project's own
conventions. It deliberately does not audit runtime ops or infrastructure.

## How to run it

### 1. Gather the deterministic facts

```bash
python .claude/skills/production-readiness/scripts/scan.py .
```

This reports the build setup, which classes exist by role, which of them have
tests, and known test-quality smells. It is grep, not proof — it tells you where
to look, and it can be wrong. Confirm anything you plan to report as a blocker by
opening the file.

### 2. Actually run the build

```bash
mvn -q clean verify
```

This is the part that cannot be faked, and it is the single most valuable thing
in the report. A review that says "tests look fine" without running them is
guessing. Capture the real Surefire summary line and the exit status.

If the build cannot run at all (no Maven on PATH, no network for dependency
resolution, no database), **say so explicitly in the report** rather than quietly
skipping it — an unverified build is itself a reason not to ship. Note what you
could not verify and let the reader weigh it.

Some tests need a live PostgreSQL. If the failures are environmental rather than
defects, distinguish the two clearly; misreporting a missing database as a broken
test wastes the reader's afternoon.

### 3. Check this project's conventions

The repo already encodes its rules in three places. Read them and apply them
rather than inventing your own standard:

- `.claude/CLAUDE.md` — the non-negotiable conventions (constructor injection,
  `BigDecimal` money, DTOs at the controller boundary, thin controllers,
  `/api/v1` routing, centralised error handling, `ddl-auto=validate`).
- `.claude/rules/testing.md` — test naming, AAA comments, AssertJ over JUnit
  asserts.
- `.claude/commands/` — three focused checks (`verify-dto-coverage`,
  `check-transactions`, `find-n-plus-one`). Their logic is short; apply it
  directly instead of re-deriving it.

A convention violation is only worth reporting if you can name the file and line.

### 4. Judge severity by consequence

Sort findings by what actually happens if the project ships unchanged. Resist
sorting by how easy something is to spot — a missing `@Transactional` is quieter
than a naming inconsistency and far more dangerous.

- **Blocker** — the deploy fails, or it succeeds and then corrupts data, loses
  money, or exposes something. Compilation failure, a failing test, a
  multi-write service method with no transaction, an entity mapping that will not
  pass `ddl-auto=validate` against the real schema.
- **Warning** — it ships and works today, but the next change is likely to break
  it and nothing will catch that. An untested service, a build that is not
  reproducible across machines, a dependency outside the managed BOM.
- **Nice-to-have** — genuine improvements with no shipping consequence. Say so
  plainly so nobody delays a release over them.

When a finding sits between two levels, place it in the lower one and explain the
condition that would raise it. Inflating severity to seem thorough trains the
reader to ignore your blockers, which is the one failure mode this report cannot
survive.

### 5. Decide GO or NO-GO

The rule is simple, so state it and follow it: **any blocker means NO-GO.** Zero
blockers means GO, warnings and all — otherwise nothing ever ships.

Commit to one or the other. "GO with reservations" is not a verdict; if the
reservations matter they are blockers, and if they don't, it's a GO.

## Report format

Use this structure. Keep it tight — a reader who has to scroll to find the
verdict has already lost the thing they came for.

```markdown
# Production readiness: <project> (<branch> @ <short-sha>)

## Verdict: GO | NO-GO
<One or two sentences. If NO-GO, name the blockers. If GO, name the biggest
remaining risk so the reader knows what they are accepting.>

| Check | Result |
|---|---|
| `mvn clean verify` | PASS / FAIL / not run (reason) |
| Tests | 22 run, 0 failed, 0 skipped |
| Untested services / controllers | 5 of 10 |
| Convention violations | 2 |

## Blockers
### 1. <What is wrong, stated as the problem not the fix>
**Where:** `path/to/File.java:42`
**Evidence:** <build output, or the actual line>
**Why it matters in production:** <the concrete failure — who sees what, when>
**Fix:** <the specific change>

## Warnings
<Same shape, briefer.>

## Nice-to-have
<One line each. No ceremony.>

## Not verified
<Anything you could not check, and why. Omit the section if there is nothing.>
```

## What makes this report good

**Every finding carries evidence.** A `file:line`, a build log line, a command
output. If you cannot point at something, you are speculating, and speculation in
a readiness report is how a team learns to skip reading them.

**"Why it matters in production" is the load-bearing sentence.** Not "violates
the DTO convention" but "returns the JPA entity, so a new column on `Patient` is
silently published to every API client and the lazy `doctor` association
serialises the whole graph". The reader is deciding whether to spend an hour
before a deploy; they need the consequence, not the category.

**Missing things count.** The absence of a test for `PatientService` will never
show up as a failure — it shows up as a green build that proves less than it
appears to. Scan for what should exist and doesn't: untested behaviour, an
unpinned dependency, a build that only works on the author's machine.

**Say what you did not check.** Silence reads as "verified and fine". If the
database was unreachable and four tests could not run, that belongs in the
report, not in a footnote you left out.

**Ten real findings beat forty padded ones.** Volume is not thoroughness. If a
check comes back clean, one line saying so is the right amount of space.
