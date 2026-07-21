#!/usr/bin/env python3
"""Collect the deterministic facts a build-and-test readiness review needs.

This exists so the review spends its thinking on judgement calls rather than on
re-deriving the same greps every time. It only *reports*; deciding what is a
blocker is the reviewer's job.

Usage: python scan.py [project_root]   (default: current directory)
"""
import os
import re
import sys
from pathlib import Path

root = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
pom = root / "pom.xml"
main_src = root / "src" / "main" / "java"
test_src = root / "src" / "test" / "java"


def out(section):
    print(f"\n=== {section} ===")


def read(p):
    try:
        return p.read_text(encoding="utf-8", errors="replace")
    except OSError:
        return ""


# ---------------------------------------------------------------- build setup
out("BUILD SETUP")
print(f"project_root: {root}")
wrapper = [
    p for p in ["mvnw", "mvnw.cmd", ".mvn/wrapper/maven-wrapper.properties"]
    if (root / p).exists()
]
print(f"maven_wrapper_files: {wrapper if wrapper else 'NONE'}")
print(f"gitignored_target: {'target/' in read(root / '.gitignore')}")

pom_text = read(pom)
if not pom_text:
    print("pom.xml: NOT FOUND")
else:
    parent = re.search(
        r"<parent>.*?<artifactId>([^<]+)</artifactId>.*?<version>([^<]+)</version>",
        pom_text, re.S)
    if parent:
        print(f"parent: {parent.group(1)} {parent.group(2)}")
    for prop in re.findall(r"<(java\.version|maven\.compiler\.\w+)>([^<]+)<", pom_text):
        print(f"property: {prop[0]} = {prop[1]}")
    gid = re.search(r"</parent>.*?<groupId>([^<]+)</groupId>", pom_text, re.S)
    if gid:
        print(f"pom_groupId: {gid.group(1)}")

    # Dependencies carrying their own <version> are outside the Boot BOM's
    # managed set, so they will not move when the parent is upgraded.
    print("\npinned_dependencies (explicit <version>, not BOM-managed):")
    found = False
    for dep in re.findall(r"<dependency>(.*?)</dependency>", pom_text, re.S):
        ver = re.search(r"<version>([^<]+)</version>", dep)
        if ver:
            art = re.search(r"<artifactId>([^<]+)</artifactId>", dep)
            print(f"  - {art.group(1) if art else '?'} = {ver.group(1)}")
            found = True
    if not found:
        print("  (none)")

    snaps = re.findall(r"<version>([^<]*SNAPSHOT)</version>", pom_text)
    print(f"snapshot_dependency_versions: {snaps if snaps else 'none'}")
    for flag in ["skipTests", "maven.test.skip", "testFailureIgnore"]:
        if flag in pom_text:
            print(f"WARNING: pom contains '{flag}'")
    for plugin in ["maven-surefire-plugin", "maven-failsafe-plugin", "jacoco-maven-plugin"]:
        print(f"plugin_{plugin}: {'present' if plugin in pom_text else 'absent'}")

    # Comments that contradict the file around them are a maintenance hazard:
    # they mislead the next reader about what the build actually does.
    for c in re.findall(r"<!--(.*?)-->", pom_text, re.S):
        print(f"pom_comment: {' '.join(c.split())[:120]}")

# ------------------------------------------------------- production inventory
out("PRODUCTION CLASSES BY ROLE")
roles = {"@RestController": [], "@Service": [], "@Repository": [], "@Entity": []}
main_files = sorted(main_src.rglob("*.java")) if main_src.exists() else []
for f in main_files:
    body = read(f)
    for ann in roles:
        if re.search(rf"^\s*{re.escape(ann)}\b", body, re.M):
            roles[ann].append(f.stem)
for ann, names in roles.items():
    print(f"{ann} ({len(names)}): {', '.join(sorted(names)) or 'none'}")

# ------------------------------------------------------------- test inventory
out("TEST COVERAGE BY CLASS")
test_files = sorted(test_src.rglob("*.java")) if test_src.exists() else []
tested = {f.stem[:-4] for f in test_files if f.stem.endswith("Test")}
print(f"test_classes ({len(test_files)}): {', '.join(f.stem for f in test_files) or 'none'}")

# Controllers, services and repositories are where behaviour lives, so an
# untested one is the gap worth naming. Entities and DTOs are mostly Lombok.
for ann in ["@RestController", "@Service"]:
    missing = [n for n in roles[ann] if n not in tested]
    print(f"untested_{ann.strip('@')}: {', '.join(sorted(missing)) or 'NONE — all covered'}")

out("TEST QUALITY SIGNALS")
total_tests = 0
bad_names = []
for f in test_files:
    body = read(f)
    methods = re.findall(r"@Test\s+(?:public\s+)?\w[\w<>\[\], ]*\s+(\w+)\s*\(", body)
    total_tests += len(methods)
    bad_names += [f"{f.stem}#{m}" for m in methods if not re.match(r"should_.+_when_.+", m)]
    if f.stem != f.stem.rstrip() or not f.stem.endswith("Test"):
        print(f"naming: test file not named *Test.java -> {f.name}")
print(f"total_@Test_methods: {total_tests}")
print(f"methods_not_matching_should_X_when_Y: {bad_names or 'none'}")

patterns = {
    "junit4_assertEquals/assertTrue (project requires AssertJ assertThat)":
        r"\bassert(Equals|True|False|NotNull|Null)\s*\(",
    "@Disabled / @Ignore (silently skipped test)": r"@(Disabled|Ignore)\b",
    "@MockBean (removed in Spring Boot 4)": r"@MockBean\b",
    "empty or trivially-passing assertion (assertThat(true))": r"assertThat\s*\(\s*true\s*\)",
}
def is_comment(line):
    """A finding that turns out to be a code comment destroys the report's
    credibility, so never match inside one."""
    s = line.strip()
    return s.startswith(("//", "*", "/*"))


for label, pat in patterns.items():
    hits = []
    for f in test_files:
        for i, line in enumerate(read(f).splitlines(), 1):
            if not is_comment(line) and re.search(pat, line):
                hits.append(f"{f.name}:{i}")
    print(f"{label}: {hits or 'none'}")

out("NEXT STEP")
print("Run the build yourself and quote its real output — these greps do not")
print("prove the project compiles or that the tests pass.")
