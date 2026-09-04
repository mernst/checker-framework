# Plan: a webpage for every Checker Framework message key

This plan is written for an LLM agent (or a person) working in this clone, on branch
`example-per-message-key`.  Do not switch branches or create branches in this directory.
Work incrementally; after each phase, run the verification steps listed for it.

<!-- Essential: -->
<!-- markdownlint-disable no-space-in-code -->

<!-- markdownlint-disable line-length -->
<!-- markdownlint-disable table-column-style -->
<!-- markdownlint-disable no-bare-urls -->

## 0. Overview

Goal: for every message key that the Checker Framework can issue (every key in every
`messages.properties` file shipped in `checker.jar`), publish a webpage at
`https://checkerframework.org/messages/<key>.html` that explains:

1. what the message means,
2. example code that provokes the message (and no other message), and what goes wrong at run
   time if the flagged code is allowed,
3. how to fix it, including a fixed version of the example that issues no message, and
4. when it is acceptable to suppress the warning instead, and what the user must verify first.

(Items 3 and 4 are omitted for the few messages that report a fact about the code rather than a
defect; see question 4 of Section 1.)

Plus an index page `https://checkerframework.org/messages/` that lists every key, grouped by
checker, in the style of <https://errorprone.info/bugpatterns> and
<https://rust-lang.github.io/rust-clippy/master/>.  Each checker chapter in the manual ends with
a section that lists the message keys specific to that checker, each linked to its webpage
(Phase 5).  In addition, every error and warning that a checker prints ends with the URL of the
key's page, so that a user can reach the explanation directly from the compiler output (as Error
Prone does with "see https://errorprone.info/bugpattern/X").  Notes, and output under
`-Anomsgtext` or `-Adetailedmsgtext`, are unchanged (Phase 4).

The source of truth for each page is a single Java file, `<CamelCaseKey>.java`, that lives in a
`messages/` subdirectory of a Checker Framework test directory.  The file is simultaneously a
test case (so the example is guaranteed to keep provoking exactly the documented message) and
a literate program whose `///` comments hold the prose of the webpage.  A script,
`checker/bin-devel/message-docs.py`, validates all such files and generates the HTML.

Deliverables, in the order they should be produced:

| # | Deliverable | Location |
|---|-------------|----------|
| 1 | File-format specification (Section 3) | `docs/developer/message-examples.md` |
| 2 | Validator/generator script (Section 4) | `checker/bin-devel/message-docs.py` |
| 3 | Pilot: 6 example files exercising every format feature (Phase 1) | `messages/` subdirectories |
| 4 | One example file per message key, 294 files in all including the 6 pilot files (fewer if Phase 0 step 3 finds dead keys), written in batches (Phase 2, Appendix A) | `checker/tests/<dir>/messages/` and `framework/tests/<dir>/messages/` |
| 5 | Build, CI, and website integration (Phase 3) | `build.gradle`, `release.gradle`, `checker/bin-devel/test-misc.sh`, release scripts |
| 6 | The page URL in every diagnostic (Phase 4) | `SourceChecker.java`, jtreg tests, `docs/examples/*/Expected.txt` |
| 7 | Documentation updates, including a per-checker key list at the end of each checker chapter of the manual (Phase 5) | `docs/manual/*.tex`, developer manual, main webpage, `CHANGELOG.md`, `checker/tests/README.md` |

Size of the job: 298 (key, properties-file) pairs, 294 distinct keys (4 keys are defined in two
properties files).  112 of the keys are framework-wide (`common/basetype/messages.properties`)
and are issued by every checker.  See Appendix A for the full inventory.

## 1. Open questions, and the assumption this plan makes for each

The task statement left the following points unspecified.  Items marked "Decided" were settled
by the user; the rest state the assumption the plan proceeds under.  If an assumption is wrong,
adjust the plan before starting Phase 1.

1. **File names for keys that contain dots.** `<MESSAGEKEY>.java` is not a legal Java file
   name for `dereference.of.nullable`.  Decided on 2026-09-03: the file name is the key in
   UpperCamelCase, one word per dot-separated segment, with no prefix (the `messages/`
   subdirectory already identifies the file as an example): `DereferenceOfNullable.java`,
   `PurityNotSideeffectfreeCall.java`.  The mapping is reversible (split before each capital
   letter), but the file still names its key explicitly (`/// @key dereference.of.nullable`)
   for readability, and the script checks that the name and the directive agree.
2. **Which checker demonstrates the 112 framework-wide (`basetype`) keys and the 4 keys in
   `framework/source/messages.properties`?** Assumption: the Nullness Checker
   (`checker/tests/nullness/messages/`) whenever it can issue the key, because it is the most
   widely used checker and its examples read naturally; otherwise the checker for which the key
   is most natural (e.g., the Tainting Checker for `@HasQualifierParameter` keys, the Regex
   Checker for `instanceof.unsafe`).  There is exactly one example file per key, so the page
   shows one checker's example and states that every checker can issue the key.
3. **Keys that cannot be provoked by a self-contained test file** (crashes such as
   `type.inference.failed`; environment-dependent messages such as `slow.typechecking`,
   `ambiguous.ajava`, `annotation.not.completed`, `invalid.annotation.location.bytecode`;
   the LSP message `lsp.type.information`).  Assumption: still create `<CamelCaseKey>.java`, with
   a `/// @no-example <reason>` directive and a `/// @skip-test` directive, so that the page
   exists and the index is complete.  Such a file may contain illustrative code, which appears
   on the page.  `@skip-test` keeps the *checker* from running on it (Section 2.2), but the code
   is still ordinary compilable Java, and Section 3.6 says how that is enforced.  Alternative:
   no page for such keys.
4. **Keys of the Report Checker** (`common/util/count/report/messages.properties`:
   `fieldreadwrite`, `usage`, ...).  These are developer-tool messages rather than type errors.
   Assumption: include them (cheap), listed under "Informational messages" on the index
   (Section 4.5).  Decided on 2026-09-03: such a file carries an `/// @informational`
   directive, and its page has only `Meaning` and `Example`, because "how to fix it" and "when
   to suppress it" have no sensible content for output that reports a fact about the code
   rather than a defect (Sections 3.3 and 3.4).  `lsp.type.information` is informational too.  The Compiler Messages Checker's only key, `type.incompatible`, is also
   defined in `basetype`, so question 12 governs it: its page is an ordinary framework-wide
   page, not an informational one.
5. **Where the generated HTML lives.** Assumption: `docs/messages/`, generated by a Gradle task
   `messageDocs` that the `manual` task depends on, **committed to git**, copied to the website
   by `copyToWebsite` in `release.gradle`, and included in the release zip.  The same task also
   writes the manual fragments `docs/manual/messages-<propdir>.tex` (question 13), which are
   likewise committed.  Because the output
   is committed, CI checks that it is up to date, the same way it checks
   `docs/manual/contributors.tex` (Phase 3).
6. **Script language and dependencies.** Assumption: Python 3.10+ (the repository's
   `pyproject.toml` requires >= 3.10), standard library only, conforming to `.ruff.toml` and
   `ty` (both run as prek hooks on every `.py` file).  The prose is Markdown; the script renders
   a documented Markdown subset itself rather than depending on a Markdown package.
   Alternatives: bash (poor fit), a Gradle/Java task (heavy), or `uv run` with the `markdown`
   package.
7. **Compiler output on the page.** Assumption: the author writes the exact message text on the
   `// :: error: [key] ...` line (the test harness ignores that text, see Section 2.2), and the
   page shows it as the compiler output.  The script's `--compiler-output` argument obtains the
   real output by running the checker, and either updates the Java files or (with `--check`)
   verifies them.
8. **Directory placement.** Example files live in a subdirectory named
   `messages/` of the checker's test directory, e.g. `checker/tests/nullness/messages/`.  Each
   subdirectory is a separate javac invocation (Section 2.2), so a checker's examples are
   compiled together, apart from its other tests, by the existing JUnit driver; drivers search
   subdirectories recursively, so no driver changes are needed.  Nothing but example files goes
   into a `messages/` subdirectory.
9. **New test drivers for option-gated messages.** Some keys are issued only under an option
    that no existing test directory passes (e.g., `cast.redundant` needs
    `-Alint=cast:redundant`; `purity.more.*` need `-AsuggestPureMethods`, which only a test
    checker's directory passes).  Use an existing test directory and
    driver whenever one passes the needed option (the usual case); add a test directory
    plus a small JUnit driver class (e.g., `checker/tests/nullness-suggestpuremethods/`
    with `NullnessSuggestPureMethodsTest.java`) only when none does.
10. **Commits.** No pushes unless asked.
11. **The page URL in each diagnostic.** Decided on 2026-09-03: in scope.  Every error and
    warning ends with a line
    `(Also see https://checkerframework.org/messages/<key>.html)`, except under `-Anomsgtext`
    or `-Adetailedmsgtext`, and except for `Diagnostic.Kind.NOTE` messages.  Details in
    Phase 4.  Translations remain out of scope.
12. **Which meaning gets the example, for a key defined in two properties files?** Four keys are
    defined twice (Section 2.1), and there is exactly one example file per key, so only one of
    the two meanings can have an example.  Decided on 2026-09-03: the example demonstrates the
    framework-wide meaning, which every checker can issue and users therefore see more often.
    The page also shows the checker-specific message text, with a sentence explaining when that
    checker issues it instead.  Consequences: all four examples live in a framework-wide
    directory such as `checker/tests/nullness/messages/`, and the Fenum, Lock, and Compiler
    Messages Checkers own fewer keys than their properties files list (Phase 2).
13. **Per-checker key lists in the manual: generated or hand-written?** Decided on 2026-09-03:
    generated, one fragment per `messages.properties` file rather than per checker, because the
    `CHECKERS` table is keyed by test directory and does not line up with the manual's chapters
    (Section 2.4).  `message-docs.py` writes `docs/manual/messages-<propdir>.tex`, and a chapter
    `\input`s as many fragments as it needs -- the Index Checker chapter `\input`s four
    (Phase 5).  The
    fragments are committed, like `docs/manual/contributors.tex`, and the `manual` task
    therefore depends on `messageDocs`.  The cost is that building the manual now needs Python;
    the benefit is that a list can never go stale.
14. **Suppression string shown for a framework-wide key.** Confirmed on 2026-09-03: the page
    shows `@SuppressWarnings("<key>")`, with no checker name, because any checker can issue the
    key.  A note adds that prefixing a checker name (for example `"nullness:assignment"`)
    suppresses the message for that checker only.  A checker-specific key's page keeps the
    prefixed form, `@SuppressWarnings("<prefix>:<key>")`.
15. **Error or warning, for a key that is issued both ways.**  Section 2.1 notes that the kind
    is chosen at each call site (`reportError` vs. `reportWarning`), so one key can appear as
    either.  Decided on 2026-09-03: the page reports the kind that its own example produces and
    makes no claim about other call sites, and the script does not scan the source for the
    other kinds.  A page that would mislead can say so in "Meaning".
16. **`propertykeychecker.filenotfound` is not in scope.** An earlier draft of this plan said
    that the Property File Checker issues this key without defining it.  Rechecked on
    2026-09-03: the only two occurrences, in `PropertyKeyAnnotatedTypeFactory` and
    `I18nFormatterAnnotatedTypeFactory`, are inside commented-out `// report(...)` lines, so no
    code issues it and no `messages.properties` defines it.  There is nothing to document, and
    the Property File Checker has no message key of its own, no `messages.properties`, and no
    test directory or JUnit driver.  No action; recorded here so that the next reader does not
    rediscover it.

## 2. Facts about the repository that the design relies on

Verify any of these that seem doubtful before depending on them; all were checked on
2026-09-02.

### 2.1 Message keys and message text

- Every checker's messages come from `messages.properties` files, one per package
  (`SourceChecker.MSGS_FILE`).  `SourceChecker.getMessagesProperties()` walks the checker's
  superclass chain from `SourceChecker` down to the concrete checker and `putAll`s each
  package's file, so a checker-specific file overrides framework-wide text.  Four keys are
  defined twice: `compound.assignment` (basetype, fenum), `contracts.precondition` (basetype,
  lock), `inconsistent.constructor.type` (basetype, lock), `type.incompatible` (basetype,
  compilermsgs).  The page for such a key must show each variant.
- `SourceChecker.fullMessageOf` looks up the key, then repeatedly strips the leading
  dot-separated segment until a key matches (so a message `foo.assignment` would use the text
  of `assignment`).  No shipped code currently relies on this except that an unknown key is
  printed as `[key]` with no text.
- The user-visible form is `[key] text`, e.g. `error: [dereference.of.nullable] dereference of
  possibly-null reference myList`.  With `-AshowPrefixInWarningMessages` the checker prefix is
  included: `[nullness:dereference.of.nullable]`.  That prefixed form is the most specific
  `@SuppressWarnings` string; see the manual section "Suppressing warnings"
  (`docs/manual/warnings.tex`, label `compiler-message-keys`).
- Some keys are built by string concatenation, so `grep '"<key>"'` does not find them:
  `purity.not.*` (in `BaseTypeVisitor`/`PurityChecker`), `contracts.*.override` and
  `contracts.*.methodref` (`BaseTypeVisitor` ~line 4326), `expression.parameter.name*`,
  `initialization.field.write.*`, and possibly others marked "verify" in Appendix A.
- Whether a key is an error or a warning is decided at the call site (`reportError` vs.
  `reportWarning`), not in the properties file.
- Message texts use `java.util.Formatter` syntax: `%s`, `%d`, `%n` (newline), and in the Report
  Checker `%2$s`.
- Messages with `%n` are multi-line, e.g. `assignment=incompatible types in assignment.%nfound
  : %s%nrequired: %s`.
- The text of a diagnostic is assembled in `SourceChecker.reportUnsuppressed`
  (`framework/src/main/java/org/checkerframework/framework/source/SourceChecker.java`, about
  lines 1536-1600): `[key] ` or `[prefix:key] `, then `String.format(template, args)`; with
  `-Anomsgtext` only `[key]`; with `-Adetailedmsgtext` a stylized, tool-readable format; then
  `-Aonelinemsg` replaces line separators by ` / `.  `Diagnostic.Kind.NOTE` messages take a
  separate path.  `fullMessageOf` (about line 1742) finds the template.  Options are declared
  in the `@SupportedOptions` annotation on the class (line 112; `nomsgtext`, `onelinemsg`,
  `detailedmsgtext`, `noPrintErrorStack` are nearby models for a new option).
- Tests that compare full diagnostic text, which a change to the text format affects: the
  jtreg tests under `checker/jtreg/` (41 tests use `@compile/ref=<name>.goal`; 21 of them
  pass `-Anomsgtext` and are unaffected; run them with `./gradlew nonJunitTests`); the example
  programs' `docs/examples/{units,fenum,subtyping}-extension/Expected.txt`, compared with
  `diff` by their Makefiles (`./gradlew :checker:exampleTests`); possibly `docs/tutorial`
  (`make -C docs/tutorial`).  The JUnit test directories are unaffected because they pass
  `-Anomsgtext`.  Sample output also appears in the manual (`docs/manual/warnings.tex`,
  `troubleshooting.tex`, and eight other chapters) and in `docs/tutorial/webpages/*-cmd.html`.

### 2.2 The test harness

- JUnit drivers live in `checker/src/test/java/org/checkerframework/checker/test/junit/` and
  `framework/src/test/java/org/checkerframework/framework/test/junit/`.  Each extends
  `CheckerFrameworkPerDirectoryTest` (or `PerFileTest`) and passes the checker class, the test
  directory, and extra options; e.g. `NullnessTest` runs `checker/tests/nullness/` and
  `checker/tests/initialization/` with `-AcheckPurityAnnotations -Xlint:deprecation
  -Alint=soundArrayCreationNullness,redundantNullComparison`.  Appendix C lists every driver's
  directory and options.
- A driver's `getTestDirs()` directories are searched recursively.  All test files in one
  directory are compiled together in one javac invocation, and each subdirectory is compiled
  separately (`TestUtilities.findJavaTestFilesInDirectory`).  Checked on 2026-09-03: the only
  `CheckerFrameworkPerFileTest` drivers are `NullnessJavacErrorsTest`, `AggregateTest`, and
  `FrameworkTest`, none of whose directories appears in Appendix C, so every example directory
  is compiled as a unit.  So
  `checker/tests/nullness/messages/` is compiled by `NullnessTest` as its own javac invocation,
  top-level class names must be unique within that subdirectory, and an example file must not
  depend on other files.
- Tests run with `-Anomsgtext`, so actual diagnostics are just `[key]`.  Expected diagnostics
  are written on the line before the flagged line as `// :: error: [key]` or
  `// :: warning: [key]`; several can be joined with `::`.  The text after `[key]` is parsed as
  the message but **ignored when comparing** (`TestDiagnostic.equals` compares file, line,
  kind, key only).  Therefore `// :: error: [assignment] incompatible types in assignment.` is
  a valid expected-diagnostic line.  Lines immediately following a `// ::` line that start with
  `// ` are treated as continuations of the diagnostic
  (`TestDiagnosticUtils.isJavaDiagnosticLineContinuation`), which allows multi-line messages.
  Consequently an ordinary `//` comment must not directly follow a `// ::` line.  The text
  must not contain `::`, which splits diagnostics.  Prose lines starting with `///` are neither
  diagnostics nor continuations (they do not start with `// `).
- Any line containing `@skip-test` (or a conditional variant such as
  `@below-java17-jdk-skip-test`) anywhere in the file removes the file from the test run
  entirely -- it is not even compiled -- even if the string is inside a comment
  (`TestUtilities.isJavaTestFile`).  A `@no-example` file exploits this: it carries
  a `/// @skip-test` directive so that the checker never runs on it (Section 3.3), which matters
  because some of these keys report a crash that would fail the test run.  Skipping affects only
  the JUnit harness: spotless still formats the file, so google-java-format still parses it.  No
  other file, and no prose, may contain the substring `skip-test`.
- `checker/tests/README.md` documents the harness; `docs/manual/creating-a-checker.tex`
  section "Testing framework" links to it.
- `checker/tests/all-systems` is a symlink to `framework/tests/all-systems`, compiled by every
  checker; never put examples there.
- `framework/tests/` runs without the annotated JDK (see `framework/tests/README.md`).
- To run one directory's tests: `./gradlew :checker:NullnessTest` (a few minutes).  To run the
  checker on one file quickly, after `./gradlew assembleForJavac`:
  `checker/bin/javac -processor org.checkerframework.checker.nullness.NullnessChecker
  -proc:only <options from the driver> checker/tests/nullness/messages/DereferenceOfNullable.java`.
  Omit `-Anomsgtext` to see the real message text, and add `-AshowPrefixInWarningMessages` to
  see the checker prefix for the suppression string.

### 2.3 Formatting and lint hooks that touch the new files

- Spotless (google-java-format 1.36.1, configured in `buildSrc/src/main/groovy/cf-spotless.gradle`)
  formats every `.java` file under `checker/tests/` and `framework/tests/` (except a few
  excluded directories); it runs as a prek pre-commit hook and in CI (`test-misc.sh`).
  Verified behaviour on `///` comment lines: lines are re-indented to the code indentation;
  a line longer than 100 columns is **wrapped, and the continuation line gets a `//` prefix,
  not `///`**, which silently turns prose into code.  Hence the 100-column rule in Section 3
  and the corresponding check in the script.  `// :: error: [key] text` lines are left alone
  when they fit in 100 columns.
- prek hooks (`prek.toml`): `ruff-check`/`ruff-format`/`ty`/`pyupgrade` on `.py`;
  `markdownlint-cli2` on `.md`; `shellcheck`/`shfmt` on shell scripts; `validate-html` on
  committed `.html` files; `end-of-file-fixer`, `trailing-whitespace`.
- Because the generated pages are committed (assumption 5), those hooks run on them:
  `validate-html` validates every committed `.html` except a handful of unrelated excludes, and
  `end-of-file-fixer` and `trailing-whitespace` will rewrite them.  Therefore the script must
  emit valid HTML5, end every file with exactly one newline, and never emit trailing whitespace;
  otherwise a commit hook silently edits generated files and the freshness check then fails.
  The same applies to the generated `docs/manual/messages-*.tex` fragments (Phase 5).  Spotless
  does not format them: its `misc` format targets `*.tex` only at each project's top level, not
  `docs/manual/*.tex`.
- `checker/bin-devel/test-misc.sh` (CI job "misc") runs spotlessCheck, javadoc, `./gradlew
  manual`, and other documentation checks.  This is where the new check belongs.
- Python is available in CI images (`Dockerfile-contents-ubuntu-base.m4` installs python3;
  `-plus` images install `uv`).  `pyproject.toml` dev dependencies include `html5validator`.

### 2.4 The website

- `docs/checker-framework-webpage.html` is the site's front page (section `id="documentation"`).
- The manual is built by `make -C docs/manual` (hevea) via the Gradle task `manual`
  (`build.gradle` ~line 786); `manual.html` is not committed.
- `release.gradle` task `copyToWebsite` copies the front page, `CHANGELOG.md`, the manual,
  tutorial, and Javadoc into the website directory; `docs/developer/release/release_build.py`
  calls it.  The release zip (`build.gradle` ~line 640) includes `docs/manual/manual.html`.
- Manual anchors are `https://checkerframework.org/manual/#<label>`, e.g. `#nullness-checker`,
  `#index-checker`, `#resource-leak-checker`, `#constant-value-checker`,
  `#suppressing-warnings`.  The full list of *chapter* anchors is obtained with
  `grep -h chapterAndLabel docs/manual/*.tex`.  Not every anchor that the pages link to is a
  chapter: `initialization-checker` is a `\sectionAndLabel` inside `nullness-checker.tex`
  (line 1388), and `compiler-message-keys` is a plain `\label` in `warnings.tex` (line 283).
  Both work as URL anchors, but neither is a chapter, so Phase 5's per-chapter key lists cannot
  be keyed on them.

### 2.5 The manual's structure

These facts support the per-chapter key lists that Phase 5 adds.

- `docs/manual/manual.tex` includes one file per chapter with `\input{<chapter>.tex}`; a checker
  chapter starts with `\chapterAndLabel{<title>}{<label>}` and its sections use
  `\sectionAndLabel` (plain `\section` is rejected by the `check-labels` target).
- Generated `.tex` is already `\input` from a chapter: `docs/manual/troubleshooting.tex` ends with
  `\input{contributors.tex}`, and `contributors.tex` is generated by a Makefile target and
  committed.  A generated per-chapter key list follows the same pattern.
- Not every checker in Appendix C has its own chapter (for example, the Report Checker has
  none), and one chapter can cover several message-key namespaces (the Index Checker chapter
  covers `lowerbound`, `upperbound`, `inequality`, and `growonly`).

## 3. File format specification

Write this section, revised as needed, to `docs/developer/message-examples.md`.  It is the
contract between example authors and `message-docs.py`.  Keep the two in sync: every rule here
that the test harness does not already enforce must be checked by `message-docs.py --check`
(Section 4.3 says which rules the harness covers), and every check must be documented here.

### 3.1 Purpose and location

A message example file documents one message key.  It is:

- a Checker Framework test case, compiled with the other files in its directory by the
  directory's JUnit driver, and
- the source of the page `https://checkerframework.org/messages/<key>.html`.

Location: the `messages/` subdirectory of the test directory of the checker whose
`messages.properties` defines the key (e.g., `checker/tests/nullness/messages/` for
`dereference.of.nullable`, `framework/tests/value/messages/` for `from.greater.than.to`).  For
keys defined in `common/basetype/messages.properties` or `framework/source/messages.properties`,
which every checker can issue, use `checker/tests/nullness/messages/` when the Nullness Checker
can issue the key; otherwise the most natural checker.  If the key is issued only when a
command-line option is given, use the `messages/` subdirectory of a test directory whose driver
passes that option (Appendix C), creating the test directory and its driver if none exists.
Never use `all-systems`.  A `messages/` subdirectory contains nothing but example files, and
all of them are compiled together in one javac invocation by the directory's JUnit driver.

Name: the key in UpperCamelCase (split at `.`, capitalize each segment's first letter, leave
the rest of each segment unchanged, concatenate), with no prefix or suffix:
`dereference.of.nullable` -> `DereferenceOfNullable.java`; `type.argument.hasqualparam` ->
`TypeArgumentHasqualparam.java`.  The script verifies the name against `@key`.  Checked on
2026-09-03 over all 294 keys of Appendix A: no two keys map to the same file name, every
mapping is reversible by splitting before each capital letter, and no key's file name is
another key's file name plus `Fixed`, `Suppressed`, or `Example`.

Exactly one example file exists for a given message key.  Four keys are defined in two
properties files (Section 2.1); for those, the example lives in the directory chosen by the
rule above, and the page shows both message texts (Section 4.5).  For those four keys the
example demonstrates the framework-wide meaning, so the example goes in a framework-wide
directory, not in the specific checker's directory; see question 12 of Section 1.

### 3.2 Line kinds

Every line of the file is exactly one of the following.

1. **Directive line**: after optional indentation, `///` followed by a space and an at-sign,
   e.g. `/// @key assignment`.  Directives are listed in Section 3.3.  A directive is never
   shown on the page.  To start a *prose* line with an at-sign, write it in backticks
   (`` `@Nullable` is ... ``), which is the normal way to write annotation names in prose
   anyway.
2. **Prose line**: after optional indentation, `///` followed by a space or end of line, whose
   text does not start with `@`.  The text after `/// ` is Markdown (Section 3.5).  A bare
   `///` is a blank prose line.  Consecutive prose lines form one prose block (a directive
   line between them does not end the block).  Prose is always shown on the page.
3. **Expected-diagnostic line**: `// :: error: [key] (col <n>) message text` or `// ::
   warning: [key] (col <n>) message text`, the test-harness syntax (Section 2.2) with the
   column token of Section 3.6, always immediately followed by the flagged code line or by
   continuation lines.  Continuation lines start with `// ` and hold the remaining lines of a
   multi-line message (one physical line per `%n` in the message template).
4. **Code line**: anything else, including ordinary `//` and `/* */` comments, blank lines,
   imports, and class declarations.  Code lines are shown on the page unless hidden by
   `@hide`.

A line whose first non-blank characters are `///` but that is neither a directive line nor a
prose line -- `///text`, with no space after the slashes, or `/// @` with nothing after the
at-sign -- is an error, not a code line.  Otherwise a typo would silently move prose into the
code.

### 3.3 Directives

| Directive | Required? | Meaning |
|-----------|-----------|---------|
| `@key <message-key>` | yes, exactly once, before any other directive or non-blank prose | The key this file documents.  Must exist in some `messages.properties` in `checker/src/main` or `framework/src/main`. |
| `@options <javac options>` | no | The options, other than `-processor`, *without which this message would not be issued*, e.g. `@options -AwarnRedundantAnnotations` -- not the directory's whole option list.  Each must be one that the directory's driver passes (the script checks this; a `-Alint=<name>` option matches if the driver passes `<name>` in any of its `-Alint=` lists, since a driver combines its lint names into one option).  An example that needs an option no driver passes needs a new test directory instead (assumption 9).  The page calls them out ("this message is issued only with ..."), and "Meaning" says so in prose (Section 6). |
| `@hide` | no | Code lines from here on are omitted from the page, until `@show`.  Prose is never hidden. |
| `@show` | no | Ends `@hide`.  The initial state is "shown". |
| `@no-example <reason>` | no | The message cannot be provoked by a self-contained test file; the reason is shown on the page.  The file then contains no expected-diagnostic line, and `@kind` and `@skip-test` are required.  The file may contain illustrative code, which is shown on the page; that code must compile (Section 3.6). |
| `@skip-test` | only with `@no-example` | Keeps the checker from running on the file (Section 2.2).  Write it immediately after `@no-example`. |
| `@kind error` or `@kind warning` | only with `@no-example` | Whether the message is an error or a warning.  Otherwise the kind is taken from the expected-diagnostic lines. |
| `@informational` | no | The message reports a fact about the code rather than a defect (the Report Checker's keys and `lsp.type.information`; question 4 of Section 1).  The file then has only the `Meaning` and `Example` headings, and the index lists the key under "Informational messages".  May be combined with `@no-example`. |

Like every directive, `/// @skip-test` is not shown on the page.

### 3.4 Required structure

The level-2 headings, in order, are `Meaning`, then one or more `Example`/`Fix` pairs, then
`Suppressing the warning`, then optionally `See also`.  A file with two pairs reads: Meaning,
Example, Fix, Example, Fix, Suppressing the warning.  No other level-2 heading is allowed, and
`See also` is the only heading that may be omitted.

1. `## Meaning` -- what the message means: which construct is checked and what property is
   violated.  One to three paragraphs.  Name the annotations involved.
2. `## Example` -- the code that provokes the message, optionally preceded by a sentence or two
   introducing the example, and followed by brief text saying what goes wrong at run time, or
   why the annotation is meaningless or misleading, if the flagged code were allowed.  The code
   in this section must contain at least one expected-diagnostic line, every expected-diagnostic
   line in the file must be in some `## Example` section, and every one must use the file's key
   (several occurrences of the same key are permitted, to show variants, but one is preferred).
3. `## Fix` -- how to change the code or annotations, then the fixed code.  Code in this section
   must contain no expected-diagnostic line.  (The test harness verifies that it compiles
   without any diagnostic.)  Several alternative fixes may be shown, each introduced by a
   sentence.  The fixed code should differ from the example as little as possible.
4. `## Suppressing the warning` -- when suppression is legitimate (typically: the checker is
   imprecise and the programmer has verified the property by other means), exactly what the
   programmer must verify before suppressing, and a `@SuppressWarnings` snippet with the most
   specific string, e.g. `@SuppressWarnings("nullness:dereference.of.nullable") // reason`.  A
   suppression's justification comment must start on the same line as the string.  If
   suppression is never appropriate (e.g., the annotation is ill-formed), say so.

An `@informational` file has only two headings, `Meaning` and one `Example`, because a message
that reports a fact about the code has nothing to fix and no reason to suppress.  Everything
else in this section applies to it unchanged.

A `@no-example` file has the same four required headings, and exactly one `Example`/`Fix` pair.
Its `## Example` section contains no expected-diagnostic line; the page shows the `@no-example`
reason there (Section 4.5), followed by the illustrative code if the file has any.  Its
`## Fix` section may be prose only.

The optional `## See also` section holds links to manual sections or related keys (relative
links such as `[assignment](assignment.html)` are checked for existence).

The script generates the page header from the directives and the properties files: the key,
error/warning, the message template, and the checker and options used by the example, with a
`javac` command line to reproduce it, and the recommended suppression string.  Do not repeat
those in prose.  The one exception is `@options`: the header says which options the example
needs, and "Meaning" says in prose *why* the message appears only under them (Section 6).

### 3.5 Markdown subset

Supported in prose (anything else is passed through to the HTML file as literal text):

- Paragraphs separated by blank prose lines.
- `## ` and `### ` headings.
- Unordered lists (`- item`), ordered lists (`1. item`), one level; an item continues on
  following lines indented by two or more spaces.
- Fenced code blocks (` ``` ` ... ` ``` `), rendered verbatim, e.g. to show compiler output or
  an annotation that is not part of the compiled example.
- Inline: `` `code` ``, `**strong**`, `*emphasis*`, `[text](url)`.
- Inside a code span or a fenced code block, `<`, `>`, and `&` are escaped, so prose can contain
  `List<@Nullable String>`.  Everywhere else they are passed through unchanged, so a page may
  contain raw HTML; an author who writes a bare `<` in prose produces invalid HTML, which
  `validate-html` catches at commit time.

Line length: every line of the file, prose or code, is at most 100 characters (google-java-format
rewraps longer comment lines and breaks the format; the script rejects longer lines).

### 3.6 Rules for the Java code

- The file must compile with no javac error and must produce exactly the expected diagnostics
  and no others (the test harness enforces this).  The bad example produces only the documented
  key; the fixed version produces nothing.
- A `@no-example` file must compile too.  The JUnit harness skips it, so nothing in the ordinary
  test run would catch a compile error in it; instead, `message-docs.py --compiler-output`
  compiles every `@no-example` file with `javac -proc:none` and `checker-qual` on the classpath,
  and reports a failure like any other error (Section 4.1).  Only the *checker* is skipped, and
  only because some of these keys report a crash.  Write the same realistic, minimal code as in
  any other example; do not write pseudo-code, elisions such as `...`, or references to
  undeclared names.
- Java 17 language level, no dependencies beyond the JDK and `checker-qual`.
- Realistic, minimal, idiomatic code: descriptive names from a plausible domain; no code that a
  reviewer would call bad style merely to provoke the message; no unused declarations; one
  message per example.  Put boilerplate (imports, wrapper class declarations, helper
  declarations that would distract) under `@hide`.  Show the whole method or field that matters.
- Top-level classes: the file may declare several package-private classes, but every
  top-level class name must start with the file's base name (`DereferenceOfNullable`,
  `DereferenceOfNullableFixed`), because all examples in the subdirectory are compiled
  together.  Name the wrapper class after the file, the fixed version `...Fixed`, and the
  version in `## Suppressing the warning` `...Suppressed`, and hide all three declarations; no
  class needs to be `public`.  In a file with more than one `Example`/`Fix` pair, the second
  and later pairs number their classes: `...2` and `...Fixed2`, and so on.  If the base name is the simple name of a
  `java.lang` class or of a commonly used JDK class (`Override`, `Annotation`, `Predicate`),
  name the classes `<Base>Example` and `<Base>ExampleFixed` instead, so that they do not shadow
  the JDK class for the other examples compiled with them.  Helper classes are nested classes,
  shown when they matter (e.g., superclass/subclass pairs for override messages).
- Expected-diagnostic lines carry the exact message text the checker prints for this example,
  and the column that the caret in that output points at, as
  `// :: error: [key] (col <n>) <message text>`.  The token may instead name a range of
  columns, `(col <m>-<n>)`, which underlines the whole offending expression rather than its
  first character; both bounds are inclusive, and `m` must not exceed `n`.  javac reports a
  single column, so `--compiler-output` writes the single-column form; widen it to a range by
  hand when underlining the whole expression reads better, and `--compiler-output` will leave
  that range alone as long as it still contains the column that javac reports (Section 4.1).
  The columns are stored as numbers, not as an aligned caret, because google-java-format
  re-indents comment lines and would destroy the alignment.  Columns are 1-based, counted in
  characters from the start of the flagged line as it appears in the file.  The page's code
  blocks have their common indentation removed (Section 4.4), so the script shifts each caret
  left by the number of characters it removed from that block.  The test harness
  ignores everything after `[key]`, and the script strips the token before showing the message
  on the page.  Multi-line messages use continuation lines.  The text must not contain `::`; if
  an argument would, abbreviate it.  `--compiler-output` reports an error rather than writing a
  message text that contains `::`; change the example so that no argument contains `::`.
- Do not put an ordinary `//` comment on the line after an expected-diagnostic line.
- Do not write the substring `skip-test` anywhere, except in the `@skip-test` directive of a
  `@no-example` file.

### 3.7 Complete example

```java
/// @key dereference.of.nullable
///
/// ## Meaning
///
/// An expression whose type is `@Nullable` is dereferenced: a method is called on it, a field
/// of it is accessed, etc.  Such an expression may be null at run time, and if it is, the
/// dereference throws a `NullPointerException`.
///
/// The type is `@Nullable` either because of an explicit annotation, or because the
/// expression is a call to a method (such as `Map.get`) that is annotated to possibly return
/// `null`, and no preceding check has established that the value is non-null.
///
/// ## Example
///
/// `Map.get` returns `null` when the key is absent, so the Nullness Checker gives `name` the
/// type `@Nullable String`.
/// @hide
import java.util.Map;

class DereferenceOfNullable {
  /// @show
  static String greeting(Map<String, String> config) {
    String name = config.get("user.name");
    // :: error: [dereference.of.nullable] (col 28) dereference of possibly-null reference name
    return "Hello, " + name.trim();
  }
  /// @hide
}

/// If no entry for `"user.name"` exists, `greeting` throws a `NullPointerException`.
///
/// ## Fix
///
/// Supply a default, so that `name` is non-null on every path:
class DereferenceOfNullableFixed {
  /// @show
  static String greeting(Map<String, String> config) {
    String name = config.getOrDefault("user.name", "stranger");
    return "Hello, " + name.trim();
  }
  /// @hide
}

/// Alternatively, test for null before dereferencing.
///
/// ## Suppressing the warning
///
/// Suppress the warning if you have verified that the value cannot be `null` here for a reason
/// the checker cannot see, and the reason cannot be captured by other annotations, such as
/// `@KeyFor`.  Write the suppression on the smallest declaration that encloses the dereference:
class DereferenceOfNullableSuppressed {
  /// @show
  @SuppressWarnings("nullness:dereference.of.nullable") // "user.name" is always in config
  static String greeting(Map<String, String> config) {
    String name = config.get("user.name");
    return "Hello, " + name.trim();
  }
  /// @hide
}
```

The `(col 28)` token is written by `message-docs.py --compiler-output`, not by hand; the page
uses it to place the caret under the dereference.  Writing `(col 24-34)` instead would underline
all of `name.trim()`.

## 4. The script: `checker/bin-devel/message-docs.py`

### 4.1 Interface

```text
message-docs.py [--check] [--compiler-output] [--output-dir DIR] [--root DIR]
                [--allow-missing] [--list] [--self-test] [-v]
```

- Default action: validate every example file and every properties file, then write the HTML
  pages and the index to `--output-dir` (default `<root>/docs/messages/`) and the manual
  fragments `messages-<propdir>.tex` to `<root>/docs/manual/` (Phase 5).  Exit status 1 if
  any validation error occurred, 0 otherwise; warnings (Section 4.3) do not affect the exit
  status.
- Output is all-or-nothing: write into a temporary directory and move both sets of files into
  place only if validation succeeded, so that a failing run never leaves a half-updated
  `docs/messages/` or a half-updated set of fragments to be committed.
- Output is deterministic: iterate over keys and files in sorted order, and never emit a
  timestamp, a version number, or an absolute path.  Otherwise the freshness check that CI runs
  (Phase 3) fails spuriously.
- `--check`: validate only; write nothing.
- `--compiler-output`: obtain the real compiler output by running the checker on each example,
  and rewrite each expected-diagnostic line's message text and column token (Section 3.6).
  With `--check`, report a difference instead of rewriting.  An existing `(col <m>-<n>)` range
  is preserved, not narrowed, when it contains the column that javac reports; otherwise it is
  replaced by the single column that javac reports.  After Phase 4 the checker's output ends
  each diagnostic with an `(Also see <URL>)` line; the script strips that line before comparing
  or writing message text.  This mode also compiles every
  `@no-example` file with `javac -proc:none`, since the JUnit harness skips those files
  (Section 3.6).  It is the slow mode: it forks a javac per example directory, and it requires
  `./gradlew assembleForJavac` to have been run.
- `--output-dir`: where the pages, the index, `style.css`, and `keys.txt` are written (default
  `<root>/docs/messages/`).  It does not affect the manual fragments, which always go to
  `<root>/docs/manual/`.
- `--root`: the Checker Framework root (default: computed from the script's location).
- `--allow-missing`: report keys without an example file as warnings instead of errors (for
  use while the examples are being written).  Such a key gets no page, and the index lists it
  with the text "not yet documented" and no link, so that the index's per-checker counts stay
  meaningful during Phase 2.
- `--list`: print one line per key: key, defining properties file, kind, example file or
  `MISSING`.  Useful for tracking progress.
- `--self-test`: run the unit tests embedded in the script (Section 4.6) and exit.
- `-v`: also print the name of each file as it is processed.
- Diagnostics are printed as `path:line: error: text` so editors can jump to them.
- The script must not need anything outside the Python 3.10 standard library, and must pass
  `ruff check`, `ruff format --check`, and `ty check` with the repository configuration (D
  rules: docstrings on every function and class; line length 100; `pathlib`, not `os.path`).

### 4.2 Inputs

1. Properties files: every `messages.properties` under `checker/src/main` and
   `framework/src/main` (not `checker/src/test`, which holds test checkers).  Parse
   `key=value` lines; support `#` comments, blank lines, `\` line continuations, and `%n`,
   `%s`, `%d`, `%<n>$s` in values.  Record, per key, every (file, text) pair.
2. Example files: every `.java` file in a `messages/` subdirectory of a test directory
   (`checker/tests/*/messages/*.java` and `framework/tests/*/messages/*.java`; do not follow
   the `all-systems` symlink).  Every such file must be an example file.
3. Two tables in the script.

   `CHECKERS` maps each test directory that may hold a `messages/` subdirectory to what is
   needed to run a checker on the examples in it: the fully qualified checker class and the
   extra javac options that the directory's JUnit driver passes (used by `--compiler-output`
   and by the page's "to reproduce" line).  Initial contents: Appendix C.  An example in a
   directory missing from the table is an error.  Keep the table in sync with the drivers by
   hand; a mismatch shows up as a `--compiler-output` failure.

   `NAMESPACES` maps each `messages.properties` directory, named by its simple name
   (`nullness`, `initialization`, `rlccalledmethods`, ...), to the checker that issues its
   keys: display name, manual anchor (empty for a checker with no manual chapter or section),
   and `@SuppressWarnings` prefix.  This table, not `CHECKERS`, determines how the index groups
   keys, the suppression string on a page (Section 4.5), and the manual fragments (Phase 5).
   The two tables must be separate because the example for a key need not live in the test
   directory of the checker that defines the key: an `initialization` key's example may live in
   `checker/tests/nullness/messages/`, and the four dual-defined keys' examples live in a
   framework-wide directory (question 12 of Section 1).

   The prefix is not derivable from the properties directory's name, so `NAMESPACES` states it
   explicitly, verified with `-AshowPrefixInWarningMessages`.  Checked on 2026-09-03: the keys
   of `initialization/messages.properties` are issued by `NullnessChecker` (the only concrete
   subclass of the abstract `InitializationChecker`), so their prefix is `nullness`, and
   `InitializationChecker` says in a comment that "initialization" is not a prefix at all; the
   keys of `index/growonly/messages.properties` are issued by `GrowOnlyChecker`, whose
   `@SuppressWarningsPrefix` is `{"index", "mutable"}`, so `growonly` is not a prefix; and the
   keys of `index/inequality/messages.properties` are issued by `LessThanChecker`, whose
   prefixes are `{"index", "lessthan"}`.  When a checker has several prefixes, `NAMESPACES`
   records the most specific one -- the one that names the checker that issues the key, such as
   `rlccalledmethods` rather than `calledmethods` or `resourceleak` for
   `RLCCalledMethodsChecker` -- and the page mentions the others.
   `basetype` and `source` map to "every checker" and have no prefix (question 14 of
   Section 1).

### 4.3 Validation (all are errors unless noted)

Do not repeat validation that the test harness already does.  The harness checks that the file
compiles, that every expected diagnostic occurs, and that no unexpected diagnostic occurs; the
script checks everything else.

Per example file:

- File is in a `messages/` subdirectory whose parent is in `CHECKERS`; name is
  `<UpperCamel(key)>.java`; `@key` present once, first; key exists in a properties file;
  every top-level `class`/`interface`/`enum`/`record` name starts with the file's base name.
- All lines <= 100 characters.  Warning: a `//` (not `///`) comment line immediately after a
  `///` line at the same indentation (likely a wrapped prose line).
- A line whose first non-blank characters are `///` that is neither a directive line nor a
  prose line (Section 3.2).
- An ordinary `//` comment line immediately after an expected-diagnostic line or its
  continuation lines, which the harness would misread as a continuation (Section 3.6).
- Unknown directive; `@hide` when already hidden, `@show` when already shown; `@kind` or
  `@skip-test` without `@no-example`; `@no-example` without `@kind` and `@skip-test`; an
  `@options` option that the directory's driver does not pass (per `CHECKERS`), where a
  `-Alint=<name>` option matches if the driver passes `<name>` in any of its `-Alint=` lists
  (Section 3.3).
- Headings: the level-2 headings match `Meaning`, (`Example`, `Fix`)+, `Suppressing the
  warning`, optionally `See also`, in that order, with no others; an `@informational` file's
  headings are `Meaning`, `Example`, optionally `See also` (Section 3.4).
- `@informational` on a key that the index does not list as informational, or its absence from
  a key that the index does list as informational (the two lists must agree; Section 4.5).
- Expected-diagnostic lines: only in an `## Example` section; at least one in each `## Example`
  section unless `@no-example`; all use the file's key; the message text is non-empty and
  contains no `::`;
  the column token is present, is either `(col <n>)` or `(col <m>-<n>)` with `m <= n`, and its
  columns are within the flagged line's length.
- The message text of every expected-diagnostic line matches the key's template, compared
  against the regular expression that `MessageTemplate.regex()` (Section 4.6) builds from the
  template by quoting the literal text and replacing each `%s`, `%d`, and `%<n>$s` by `.*` and
  each `%n` by a line break.  This catches a mistyped message and one that a later edit of
  `messages.properties` made stale, and unlike `--compiler-output` it needs no javac.  For a
  key that two properties files define, matching either template suffices.
- No `// ::` line in any `## Fix` or `## Suppressing the warning` section.
- Relative links in prose (`foo.html`) refer to existing keys.
- Every visible code block is non-empty after trimming.
- With `--compiler-output`, the message text of every expected-diagnostic line matches what the
  checker prints, and its column token contains the column that the checker reports (both are
  rewritten when `--check` is absent), and every
  `@no-example` file compiles under `javac -proc:none`.

Globally:

- Every key in every properties file has exactly one example file.  Zero is an error, or a
  warning with `--allow-missing`; two or more is always an error.
- Every `.java` file in a `messages/` subdirectory contains `@key`.  Error: a `/// @key`
  directive in a `.java` file outside a `messages/` subdirectory.

### 4.4 Processing model

1. Read the file into lines; classify each line (Section 3.2); track the hide state; join
   continuation lines onto their `// ::` line.
2. Build a sequence of blocks: prose blocks (list of Markdown lines) and code blocks (list of
   visible code lines, each carrying an optional attached diagnostic).  Adjacent visible code
   lines between prose blocks form one code block; leading and trailing blank lines are trimmed
   and the common indentation is removed.  Hidden lines vanish; a hidden region does not split a
   code block if visible lines follow (e.g., the body of a class whose header is hidden), but
   a prose block always does.
3. Split blocks into sections by heading, keeping the `Example`/`Fix` pairs in order.
4. Render (Section 4.5).

### 4.5 HTML output

One page per key, `docs/messages/<key>.html`, plus `docs/messages/index.html`, plus one
shared `docs/messages/style.css`.  The file name `<key>.html` is part of the URL that the
checkers print (Phase 4); never change it without changing `SourceChecker` too.  Plain HTML5,
`<meta charset="utf-8">`, favicon and logo as the manual uses
(`favicon-checkerframework.png` and `CFLogo.png`, referenced by relative name), no JavaScript,
no other external resources.  The script does not generate those two images; `copyToWebsite`
copies them from `docs/logo/` into the website's `messages/` directory (Phase 3), so a page in
the working tree shows a broken image where the logo goes.  Do not commit copies of the images
into `docs/messages/`.  Each page must be valid HTML5: the prek `validate-html` hook checks
every committed page (Section 2.3), and `uv run html5validator --root docs/messages` checks
them on demand.

Page layout:

1. An HTML comment: `<!-- Generated by checker/bin-devel/message-docs.py from <path>;
   do not edit this file. -->`
2. `<h1>` the key; a line "Error"/"Warning" and "issued by the Nullness Checker" (link to the
   manual chapter or section named by `NAMESPACES`) or "issued by every checker; this example
   uses the Nullness Checker".  For a key that two properties files define (Section 2.1), the
   line says both: that every checker can issue it, and that the named checker gives it a
   different meaning.  The kind is the kind of this page's own example; because a call site
   chooses `reportError` or `reportWarning` (Section 2.1), another call site may use the other
   kind, and the page makes no claim about that (question 15 of Section 1).
3. "Message:" the template(s) from `messages.properties`, `%n` rendered as line breaks, each
   labelled with the checker if there are several.
4. The sections with headings as in the file.  Code blocks are `<pre>`.  Each diagnostic is
   rendered immediately *below* its flagged line: first a caret line, built from the line's
   column token (Section 3.6) as a single `^` at column `n`, or as carets filling columns `m`
   through `n` for a range, shifted left by the indentation that step 2 of Section 4.4 removed
   from the block, then the message, visually
   distinct (e.g., red, prefixed with `error:` or `warning:` and the bracketed key), with
   multi-line messages laid out as in the file.  (javac prints the message before the source
   line and the caret after it; the page puts the message last so that a reader scanning the
   code sees the flagged line first.)  The caret carries the column information that a page
   without one would lose.
5. In each `## Example` section, a "to reproduce" line: `javac -processor <checker class>
   <options from CHECKERS and @options> <file>`.
6. For an `@informational` key, the page ends after the `Example` section (and `See also`, if
   present); items 5 and 6 do not apply, and the page says that the message reports a fact
   about the code rather than a defect.
7. In the `## Suppressing the warning` section, the generated canonical suppression string,
   followed by the authored prose and a link to the manual's "Suppressing warnings" chapter.
   For a checker-specific key the string is `@SuppressWarnings("<prefix>:<key>")`, where
   `<prefix>` comes from the `NAMESPACES` entry for the properties directory that defines the
   key (Section 4.2), not from the test directory that holds the example; if that checker
   accepts other prefixes, the page names them too.  For a
   framework-wide key it is `@SuppressWarnings("<key>")` with no checker name (question 14 of
   Section 1), plus a note that prefixing a checker name (for example `"nullness:assignment"`)
   suppresses the message for that checker only.
8. If `@no-example`, the Example section shows the reason, followed by the file's illustrative
   code, marked as code that compiles but that the checker is not run on.
9. Footer: link to the index.

Index page: title "Checker Framework messages"; a brief paragraph explaining what a message key is
and how it appears in output and in `@SuppressWarnings` (link to the manual); then one list
per checker (framework-wide messages first, titled "Messages issued by every checker", then the
other checkers alphabetically by display name, and within each list the keys sorted
alphabetically), each entry giving the key (linked to its page), "error" or "warning", and the
one-line message template; then "Informational messages" for the keys whose example file carries `@informational`
(the Report Checker's keys and `lsp.type.information`; question 4 of Section 1).  Each key appears in exactly one list; in particular
`lsp.type.information` appears under "Informational messages" and not under the framework-wide
list, even though `framework/source/messages.properties` defines it.
Also emit
`docs/messages/keys.txt`, one key per line, for scripting.

### 4.6 Structure of the script

Suggested top-level pieces, each a small function or dataclass with a docstring:
`parse_properties`, `MessageTemplate` (file, key, text, `regex()`), the `CHECKERS` and
`NAMESPACES` tables,
`ExampleFile` parser (`Line`, `Directive`, `Diagnostic`, `Block`, `Section` dataclasses),
`validate_example`, `validate_global`, `markdown_to_html` (line-based state machine for the
Section 3.5 subset, with an inline renderer applied to text runs), `render_page`,
`render_index`, `render_tex_fragment` (Phase 5), `main`.  Keep it in one file of roughly
900-1100 lines.  The
`--self-test` flag of Section 4.1 exercises `markdown_to_html` and the template regex without
reading any example file.

## 5. Work plan

### Phase 0: preliminaries (half a day)

1. Build once: `./gradlew assembleForJavac` (needed for `checker/bin/javac`).
2. Regenerate the inventory of Appendix A to confirm it is current:
   `find checker/src/main framework/src/main -name messages.properties` and grep each key in
   `checker/src/main`, `framework/src/main`, and the test directories.
3. Resolve the "verify" rows of Appendix A: for each, find the issuing code (search for the
   key's prefix, since it may be built by concatenation) and decide: issued (note how), dead
   (delete from the properties file, in one commit titled "Remove unused message keys"), or
   impossible to trigger from a test (`@no-example`).  Prefer deleting a dead key over writing a
   `@no-example` page for it.

   In particular, `receiver` (`basetype`) appears dead: the only two `"receiver"` string
   literals in the source are a graph label in `TypeVisualizer` and a comment in
   `WholeProgramInferenceImplementation`.  Before deleting it, check the `fullMessageOf`
   fallback (Section 2.1): a dynamically built key ending in `.receiver` would fall back to this
   text.  Decided on 2026-09-03: if it is dead, delete it, which leaves 293 keys and 293 pages,
   and drop it from batch 7.  Adjust every count in this plan (Section 0, the deliverables
   table, and the Phase 2 arithmetic) by the number of keys actually deleted.
4. Decide the test directory for every key (Appendix A, Appendix B hints, Appendix C) and note
   which need a new test directory and driver (assumption 9).  Create those directories and
   drivers now (copy `NullnessWarnRedundantAnnotationsTest.java` as a model).  The `messages/`
   subdirectories are created when their first example is written; git does not track empty
   directories.

### Phase 1: specification, script, pilot (2-3 days)

1. Write `docs/developer/message-examples.md` from Section 3.  Run `prek run --files
   docs/developer/message-examples.md` (markdownlint).
2. Write `checker/bin-devel/message-docs.py` per Section 4.  Run `ruff check`, `ruff format`,
   `ty check`, and `python3 checker/bin-devel/message-docs.py --self-test`.
3. Write the six pilot examples, chosen to exercise every feature:
   - `checker/tests/nullness/messages/DereferenceOfNullable.java` (checker-specific
     error; Section 3.7).
   - `checker/tests/nullness/messages/Assignment.java` (framework-wide key, multi-line
     message with continuation lines, shown via the Nullness Checker).
   - `checker/tests/nullness/messages/NulltestRedundant.java` (a warning that needs
     `@options -Alint=redundantNullComparison`, which `NullnessTest` passes).
   - `checker/tests/nullness-warnredundantannotations/messages/RedundantAnno.java` (a
     framework-wide warning gated by an option that only a variant directory passes).
   - `checker/tests/nullness/messages/ContractsPrecondition.java` (a key defined in two
     properties files with different text; one example file, demonstrating the framework-wide
     meaning with `@RequiresNonNull`, whose page also shows the Lock Checker's text).
   - `checker/tests/nullness/messages/TypeInferenceFailed.java` (`@no-example`, so it also
     exercises the `javac -proc:none` compile of Section 3.6).
4. Verify end to end:
   - `./gradlew :checker:NullnessTest :checker:NullnessWarnRedundantAnnotationsTest` pass.
     (All six pilot files live in `checker/tests/nullness/messages/` and
     `checker/tests/nullness-warnredundantannotations/messages/`; `ContractsPrecondition.java`
     documents the Lock Checker's variant of the message text but is not a Lock Checker test.)
   - `python3 checker/bin-devel/message-docs.py --allow-missing` writes `docs/messages/`;
     `prek run --files docs/messages/*.html` passes (it runs `validate-html`); open the pages in
     a browser and check the layout, the caret lines, the index.
   - `python3 checker/bin-devel/message-docs.py --compiler-output` changes nothing after the
     examples' diagnostics have been written by it once, and `--compiler-output --check`
     passes.
   - `./gradlew spotlessApply` changes nothing in the pilot files (proves the format survives
     the formatter).  Then deliberately add a 120-character prose line, run `spotlessApply`,
     and confirm the script's check catches the resulting `//` line; revert.
5. Stop and ask the user to review the spec, the script's output, and the pilot prose before
   mass production.  Adjust the spec and the script; re-run the pilot; repeat until the user
   approves.

### Phase 2: write the examples, in batches (the bulk of the work)

Batch order (do the easiest and highest-traffic checkers first; counts are keys).  A count is
the number of keys whose example file lives in that batch, so the four keys that two properties
files define are counted in the framework-wide batches, not in the Fenum, Lock, or Compiler
Messages batches (question 12 of Section 1).

| Batch | Keys | Test directory (the example goes in its `messages/` subdirectory) | Notes |
|-------|------|-----------|-------|
| 1 | Nullness Checker: 23 (Appendix A, `checker/nullness`) | `checker/tests/nullness/` | `clear.system.property` needs no option (the option *permits* it); `nulltest.redundant` needs `-Alint=redundantNullComparison` (already passed); `new.array` needs `-Alint=soundArrayCreationNullness` (already passed). |
| 2 | Initialization: 8 | `checker/tests/initialization/` (run by `NullnessTest`) | The issuing checker is `NullnessChecker`, so the suppression prefix is `nullness`, not `initialization` (Section 4.2) |
| 3 | Framework-wide, subtyping family (40): `assignment`, `argument`, `return`, `enhancedfor`, `array.initializer`, `conditional`, `switch.expression`, `type.argument`, `varargs`, `compound.assignment`, `unary.increment`, `unary.decrement`, `vector.copyinto`, `annotation`, `enum.declaration`, `bound`, `super.wildcard`, `type.incompatible`, `monotonic`, `lambda.param`, `methodref.*` (4), `override.*` (3), `type.arguments.not.inferred`, `cast.unsafe`, `invariant.cast.unsafe`, `cast.unsafe.constructor.invocation`, `instanceof.unsafe`, `instanceof.pattern.unsafe`, `exception.parameter`, `throw`, `method.invocation`, `constructor.invocation`, `super.invocation`, `this.invocation`, `inconsistent.constructor.type` | mostly `checker/tests/nullness/`; see Appendix B for the ones that need another checker | |
| 4 | Framework-wide, contracts and expressions (21): `contracts.*` (12, including `contracts.precondition`, whose page also covers the Lock Checker's variant), `flowexpr.*` (6; `flowexpr.parse.context.not.determined` is in batch 7), `expression.unparsable`, `expression.parameter.name`, `expression.parameter.name.shadows.field` | `checker/tests/nullness/` using `@RequiresNonNull`, `@EnsuresNonNull`, `@EnsuresNonNullIf`, `@KeyFor` | |
| 5 | Framework-wide, purity (20): `purity.*` | `checker/tests/nullness/` (`-AcheckPurityAnnotations` is passed); `purity.more.*` and `purity.effectively.pure` need `-AsuggestPureMethods` (new directory, assumption 9; Appendix A puts all four in `framework:purity-suggestions`); `purity.viewpoint.adaptation` in `checker/tests/sideeffectsonly/` | |
| 6 | Framework-wide, declarations and annotations (25): `type.anno.before.modifier`, `type.anno.before.decl.anno`, `type.invalid`, `conflicting.annos`, `too.few.annotations`, `annotations.on.use`, `explicit.annotation.ignored`, `anno.on.irrelevant`, `redundant.anno`, `cast.redundant`, `unallowed.access`, `invalid.polymorphic.qualifier`, `invalid.polymorphic.qualifier.use`, `missing.has.qual.param`, `conflicting.qual.param`, `invalid.qual.param`, `type.argument.hasqualparam`, `declaration.inconsistent.with.extends.clause`, `declaration.inconsistent.with.implements.clause`, `field.invariant.*` (6) | various; Appendix B | |
| 7 | Framework-wide, environment and crashes (10; all are `@no-example` candidates except `unneeded.suppression`): `type.inference.failed`, `type.argument.inference.crashed`, `slow.typechecking`, `invalid.annotation.location.bytecode`, `flowexpr.parse.context.not.determined`, `receiver` (see Phase 0 step 3: expected to be deleted rather than documented), plus `framework/source` (4): `unneeded.suppression` (example in `checker/tests/resourceleak/`, which passes `-AwarnUnneededSuppressions`), `annotation.not.completed`, `ambiguous.ajava`, `lsp.type.information` (also `@informational`) | | |
| 8 | Resource Leak family: `rlccalledmethods` 12, `mustcall` 3, `calledmethods` 4, `accumulation` 1 (`predicate`, via `@CalledMethodsPredicate` in `checker/tests/calledmethods/`) | `checker/tests/resourceleak/`, `mustcall/`, `calledmethods/` | |
| 9 | Lock Checker: 18 | `checker/tests/lock/` | Its other two keys, `contracts.precondition` and `inconsistent.constructor.type`, are in batches 4 and 3 |
| 10 | Index Checker: 12 (`lowerbound` 3, `upperbound` 7, `inequality` 1, `growonly` 1) | `checker/tests/index/` | |
| 11 | Signedness: 17 | `checker/tests/signedness/` | many are near-duplicates; keep prose short and cross-link |
| 12 | Constant Value Checker: 22 | `framework/tests/value/` (`-AreportEvalWarns` is passed) | several `*.failed` keys need reflective evaluation failures; see Appendix B |
| 13 | Optional 11, Interning 7, GUI Effect 9 | respective directories | |
| 14 | Format String 8, I18n Format String 7, Regex 2, Fenum 1 | respective directories | Fenum's other key, `compound.assignment`, is in batch 3.  `i18nformat.key.not.found` is issued only when `-Apropfiles` or `-Abundlenames` names a translation file; checked on 2026-09-03, `I18nFormatterTest` passes neither, so this key needs a new test directory and driver (assumption 9) |
| 15 | Aliasing 2, Reflection 3 (`classval`/`methodval`), Returns Receiver 1, Report Checker 7 | `framework/tests/aliasing/`, `framework/tests/classval/`, `framework/tests/methodval/`, `framework/tests/returnsreceiver/`, `framework/tests/report/` | The Compiler Messages Checker's only key is `type.incompatible`, which is in batch 3, so it needs no example; the Property File Checker contributes no keys (question 16 of Section 1); the Report Checker's 7 keys are `@informational` (question 4) |

Batches 3 through 7 partition the 112 framework-wide keys and the 4 keys of
`framework/source/messages.properties`: 40 + 21 + 20 + 25 + 10 = 116.  The other batches
partition the 178 checker-specific keys, so the 15 batches hold 294 keys, one per key.  Every
key appears in exactly one batch.  Phase 0 step 4 confirms the assignment and revises it if a
key turns out to be dead or to need a different checker.

Per key, the workflow is:

1. Find the code that issues the key (`grep -rn '"<key>"' checker/src/main framework/src/main`,
   or the key's prefix if it is built dynamically) and read the surrounding logic to learn the
   exact trigger condition and the meaning of each `%s` argument.  Read the relevant manual
   section (`docs/manual/<checker>.tex`) for the intended semantics and the recommended fix.
2. Look at existing tests that expect the key (Appendix A, column 3); use a cut-down version of
   one if it suits, or write fresh, realistic code per Section 3.6.
3. Write `<CamelCaseKey>.java` following Section 3.  Draft the prose sections.
4. Run `python3 checker/bin-devel/message-docs.py --compiler-output` to write the real message
   text and caret column onto each `// ::` line.  To inspect the output by hand, run
   `checker/bin/javac -processor <checker> <driver options except -Anomsgtext> -proc:only
   <file>`; confirm that the fixed class and the suppressed class produce nothing.
5. `python3 checker/bin-devel/message-docs.py --check --allow-missing`; fix complaints.
6. After each batch: `./gradlew spotlessApply`, re-run the check, run the batch's JUnit
   driver(s) (`./gradlew :checker:NullnessTest`, etc.), regenerate the HTML and skim the new
   pages, then commit (assumption 10) with a message such as "Add message examples for the
   Lock Checker".
7. Keep a running list (e.g., `message-docs.py --list | grep MISSING`) to track progress.

### Phase 3: integration (1 day)

The generated pages are committed (assumption 5), so the integration work is a generator task, a
freshness check, and the website/zip wiring.

1. `build.gradle`: register an `Exec` task `messageDocs` (group "Documentation") that runs
   `python3 checker/bin-devel/message-docs.py`, and `messageDocsCheck` that runs it with
   `--check`.  A third task, `messageDocsCompilerOutput`, runs `--compiler-output`; it depends
   on `assembleForJavac` and is not part of any aggregate task, because it forks a javac per
   example directory.  Decided on 2026-09-03: no CI job runs it.  The cheap template check of
   Section 4.3 catches a message text that drifted from `messages.properties` without running
   javac, and Phase 6 runs the slow mode by hand.  The accepted cost is that a compile error
   introduced into a `@no-example` file is caught only by that manual run.
2. Make the `manual` task depend on `messageDocs`, because the manual's per-chapter key lists
   are generated `.tex` fragments (Phase 5).  Make each subproject's `allTests` task depend on
   the root project's `messageDocsCheck` (`allTests` is registered per subproject, at
   `build.gradle` line 1207, so use `dependsOn(rootProject.tasks.named("messageDocsCheck"))`).
3. `checker/bin-devel/test-misc.sh`, in the "User documentation" section that already runs
   `./gradlew manual`: run `./gradlew messageDocs`, then check that the committed output is up
   to date, following the `docs/manual/contributors.tex` pattern already in that file:

   ```bash
   # `git diff` alone would miss a newly generated page, which is untracked; hence
   # `git status --porcelain`, which reports modified and untracked files alike.
   if [ -n "$(git status --porcelain -- docs/messages/ 'docs/manual/messages-*.tex')" ] ; then
     git status --porcelain -- docs/messages/ 'docs/manual/messages-*.tex'
     git diff -- docs/messages/ 'docs/manual/messages-*.tex'
     echo "Generated message documentation is not up to date;"
     echo "run: ./gradlew messageDocs"
     false
   fi
   ```

   (This differs from the `docs/manual/contributors.tex` check just above it in the file, which
   can use plain `git diff --exit-code` because that one file always exists and is tracked.)

   Also run `./gradlew messageDocsCheck` so that a broken example fails the "misc" job with a
   readable message.  The committed HTML is validated by the existing prek `validate-html` hook
   (Section 2.3), so `test-misc.sh` needs no separate HTML validation.
4. `release.gradle` `copyToWebsite`: add `from("docs/messages") { into("messages") }` and copy
   the favicon and logo into `messages/` as is done for `manual/`.  `build.gradle` zip task: add
   `docs/messages/**` so the offline distribution has the pages, and add the favicon and logo
   under `messages/` as well, or the zipped pages show broken images (the images are not
   committed under `docs/messages/`; see Section 4.5).
5. `docs/developer/release/README-release-process.html`: mention how the message pages are
   generated and that they are committed.
6. Run `./gradlew messageDocs` and `./gradlew copyToWebsite -PcfWebsite=/tmp/cfsite` locally and
   check `/tmp/cfsite/messages/index.html`.

### Phase 4: print the page URL in every diagnostic (1 day)

Design (all in `SourceChecker`):

1. Add `public static final String MESSAGE_URL_BASE = "https://checkerframework.org/messages/";`
   with a comment pointing at `checker/bin-devel/message-docs.py`, which names the pages.
2. In `reportUnsuppressed`, after `messageText` is built from the template and before the
   `-Aonelinemsg` handling, if the kind is not `NOTE` and neither `-Anomsgtext` nor
   `-Adetailedmsgtext` was given, append
   `System.lineSeparator() + "(Also see " + MESSAGE_URL_BASE + messageKey + ".html)"`.  Every key
   that a checker in this repository issues has a page, so no lookup is needed.  Use the key
   that `fullMessageOf` matched, not the key passed in: for a dynamically built key that
   matched only after the fallback stripped leading segments (Section 2.1), the page that
   exists is the one for the matched key.  With
   `-Aonelinemsg` the URL then appears as ` / (Also see https://...)` on the same line, without
   any extra code.  Resulting output:

   ```text
   MyFile.java:107: error: [dereference.of.nullable] dereference of possibly-null reference myList
   (Also see https://checkerframework.org/messages/dereference.of.nullable.html)
             myList.add(elt);
             ^
   ```

3. `-Adetailedmsgtext` output is deliberately unchanged, since tools parse it.

Known limitation, accepted to keep this task simple: a checker outside this repository that
defines its own message keys will print a `checkerframework.org` URL that does not resolve.
Supporting third-party checkers is out of scope.

Steps:

1. Implement the design; `./gradlew assembleForJavac`; run the checker on
   `checker/tests/nullness/messages/DereferenceOfNullable.java` without `-Anomsgtext` and
   confirm the `(Also see` line, then with `-Aonelinemsg`.
2. Add jtreg tests modeled on `checker/jtreg/showPrefix/`: a directory
   `checker/jtreg/messageUrls/` with one test file and two `@compile/fail/ref=` runs whose
   `.goal` files show the default output (with the `(Also see` line) and the `-Aonelinemsg`
   output (with ` / (Also see `).  Cover a checker-specific key and a framework-wide key.
3. `./gradlew nonJunitTests`: update the existing `.goal` files whose only difference is the
   new line (about 20 files).  `./gradlew :checker:exampleTests`: update the three
   `Expected.txt` files.  `make -C docs/tutorial`: update expected outputs if it compares any.
4. `./gradlew allTests` still passes (the JUnit directories pass `-Anomsgtext`).
5. Do not merge this phase before Phase 3, or the printed URLs will 404 until the next release
   publishes `docs/messages/`.

### Phase 5: documentation (half a day)

1. `docs/manual/warnings.tex`, near `\label{compiler-message-keys}`: add a sentence "Every
   message key has a webpage, with an example and advice on fixing or suppressing it, at
   \url{https://checkerframework.org/messages/}."  Also mention it in the introduction to the
   "Suppressing warnings" chapter and in the troubleshooting chapter ("Understanding the error
   message").
2. `docs/checker-framework-webpage.html`: add "Explanation of each error message" (link to
   `messages/`) in both documentation lists (the file has a "Keep this in sync" comment).
3. `docs/CHANGELOG.md`: under the next version's "Documentation" or "Implementation details",
   one line.
4. `checker/tests/README.md`: a short section "Message-key example files" pointing to
   `docs/developer/message-examples.md`, and a sentence in `docs/developer/developer-manual.html`
   (section "tests" or "Documentation") saying that adding a message key requires adding an
   example file, which `test-misc.sh` enforces.
5. `docs/manual/creating-a-checker.tex`, where `messages.properties` is described (~line 1050):
   mention the example-file convention for checkers in this repository.
6. A per-checker key list at the end of each checker chapter (a goal from Section 0, decided in
   question 13 of Section 1).  The lists are generated, so that they cannot go stale.

   Decided on 2026-09-03: there is **one fragment per properties file**, not one per `CHECKERS`
   entry.  `message-docs.py` writes `docs/manual/messages-<propdir>.tex` for each
   `messages.properties` under `checker/src/main` and `framework/src/main`, where `<propdir>` is
   the properties file's directory's simple name: `messages-nullness.tex`,
   `messages-initialization.tex`, `messages-lowerbound.tex`, `messages-upperbound.tex`,
   `messages-inequality.tex`, `messages-growonly.tex`, `messages-basetype.tex`, and so on --
   one for each of the 27 properties files of Appendix A.  Each fragment holds an `itemize` of
   that file's keys, each key an `\href` to its page.  This decouples the fragments from
   `CHECKERS`, whose rows are test directories rather than chapters: a fragment is generated
   from the `NAMESPACES` entry for the properties directory (Section 4.2).  This handles the
   three cases where a properties directory and a manual chapter do not line up:

   - The Index Checker chapter `\input`s four fragments (`lowerbound`, `upperbound`,
     `inequality`, `growonly`) under one heading.
   - The Initialization Checker is a `\sectionAndLabel` inside `nullness-checker.tex`
     (Section 2.4), not a chapter, so `messages-initialization.tex` is `\input` at the end of
     that section under a `\subsectionAndLabel` rather than a `\sectionAndLabel`.
   - `purity.*` keys live in `basetype`, so they are covered by the framework-wide sentence
     below rather than by a Purity Checker fragment, and the `purity-checker` anchor of the
     `checker/tests/sideeffectsonly` row of Appendix C is used only for the "issued by" link on
     a page, not for a fragment.

   A fragment contains no sectioning command: the manual's `check-labels`
   target runs `grep -n '^\\\(chapter\|\(sub\)*section\|paragraph\){' *.tex` (so it rejects
   `\chapter{`, `\paragraph{`, and `\section{` with any number of `sub` prefixes) and fails if it
   matches, and the fragments are globbed by that `*.tex`.  The `\sectionAndLabel` line below
   goes in the hand-written chapter file, not in the generated fragment.  Add to the end of each
   such chapter:

   ```latex
   \sectionAndLabel{Message keys of the <Name> Checker}{<chapter-label>-message-keys}
   \input{messages-<propdir>.tex}
   ```

   This follows the `contributors.tex` pattern (Section 2.5): the fragments are generated and
   committed, and `make -C docs/manual` consumes them.  `docs/manual/manual.pdf` already depends
   on `*.tex`, so no Makefile change is needed.  The framework-wide keys are not specific to
   any checker, so each list ends with a sentence pointing at
   \url{https://checkerframework.org/messages/} for the keys that every checker can issue.

   `message-docs.py` writes a fragment only for a properties file that some chapter `\input`s,
   so that no generated fragment is unused: it skips `basetype` and `source`, whose keys the
   framework-wide sentence covers, and it skips a properties file whose checker has no manual
   chapter (for example the Report Checker's).  The script holds the list of fragments to
   write, and errors if a `docs/manual/*.tex` file `\input`s a `messages-*.tex` that is not on
   the list, or if a fragment on the list is `\input` by no chapter.  Twenty-seven properties
   files thus yield about twenty fragments.

   A key is listed by the properties file that defines it, wherever its example lives: the Lock
   Checker's list includes `contracts.precondition` even though its example demonstrates the
   framework-wide meaning (question 12 of Section 1).  Such a key is also in `basetype`'s key
   set, but since no `messages-basetype.tex` is written, it is listed exactly once in the
   manual.
7. The URL line (Phase 4): add the
   `(Also see` line to the sample output in `docs/manual/warnings.tex` (section
   "`@SuppressWarnings` syntax") and `docs/manual/troubleshooting.tex`, and state once that
   other samples in the manual omit the line for brevity; update the sample output in
   `docs/tutorial/webpages/*-cmd.html`; one `CHANGELOG.md` line under user-visible changes:
   "Each error and warning now ends with the URL of a webpage that explains it."

### Phase 6: final verification

- `./gradlew allTests` (or at least every driver whose directory gained files; use
  `git status` to list them) passes; ignore `slow.typechecking` warnings.
- `python3 checker/bin-devel/message-docs.py --check` reports no missing key and no error.
- `python3 checker/bin-devel/message-docs.py --compiler-output --check` reports no difference.
  This is the only run that compiles the `@no-example` files and that compares each message
  text with what the checker actually prints, and no CI job runs it (Phase 3 step 1), so run it
  by hand here and whenever a `messages.properties` text changes.
- For ten diagnostics chosen at random from a run of the checkers over the example files
  without `-Anomsgtext`, the printed URL names a file that exists in `docs/messages/`.
- `./gradlew spotlessCheck` and `prek run --all-files` pass.
- `./gradlew messageDocs` leaves `git status` clean (the committed pages are up to date), and
  `docs/messages/` holds one page per key listed by `--list`, plus the index; `prek run
  --all-files` validates them; for a random sample of ten pages, the page's "to reproduce"
  command actually reproduces the diagnostic.
- Read the index page top to bottom once for consistency of tone and terminology.
- Summarize for the user: number of pages, keys documented with `@no-example` and why, dead
  keys removed, new test drivers added, and the open questions that were decided by assumption.
- Remove files `plan-for-plan.md` and `plan-example-per-message-key.md`.

## 6. Writing guidelines for prose

- Audience: a programmer who just saw the message, may be new to the Checker Framework, and
  wants to get on with their work.  Lead with the meaning, in plain language, then the
  details.  Two to five short paragraphs per section; no headings inside sections other than
  `###` for alternative fixes.
- Use the vocabulary of the manual: "qualifier", "type annotation", "subtype", "refine",
  "flow-sensitive", "declaration annotation".  Link the first mention of a checker to its manual
  chapter and the first mention of an annotation to its Javadoc
  (`https://checkerframework.org/api/org/checkerframework/checker/nullness/qual/Nullable.html`).
- Do not describe internal implementation ("the visitor", "the annotated type factory").
- "Example", after the code, optionally states the concrete run-time failure or logic error (a
  `NullPointerException`, a data race, an index out of bounds, a SQL injection) in the example's
  terms, not in the abstract.
- "Fix" prefers the fix that makes the code correct over the fix that merely
  satisfies the checker, and explains when each alternative applies.
- "Suppressing the warning" always contains the condition the programmer must have verified, and
  the `@SuppressWarnings` line with a justification comment on the same line.  Where the message
  is a hard error (such as one about an ill-formed annotation), say that suppression is not
  appropriate.
- For messages that only a command-line option enables, say so in "Meaning".
- For framework-wide keys, "Meaning" describes the general rule ("the right-hand side of the
  assignment is not a subtype of the left-hand side's declared type in the checker's type
  hierarchy") and then the example's checker-specific reading.
- Use the message's own wording where possible so that a search for the message text finds the
  page.
- No text that depends on the Checker Framework version number.

## Appendix A. Inventory of message keys

Generated on 2026-09-02 from the properties files in `checker/src/main` and
`framework/src/main` (`checker/src/test/.../disbaruse/messages.properties` belongs to a test
checker and is excluded).  "Issued by" is where the key appears as a string literal in
`checker/src/main`, `framework/src/main`, or `dataflow/src/main`; "(built dynamically, or
unused: verify)" means the literal was not found.  A listed class is not proof that the key is
issued: the string literal may be a label, a comment, or an unrelated identifier (`receiver` was
such a false positive).  Phase 0 step 3 confirms each row.  "Test directories" are top-level test
directories containing a file that expects the key (prefix `framework:` for
`framework/tests/`); use them as a starting point for the trigger condition.

### `framework/src/main/java/org/checkerframework/common/basetype/messages.properties` (112 keys)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `receiver` | (built dynamically, or unused: verify) -- the `"receiver"` occurrences in TypeVisualizer and WholeProgramInferenceImplementation are a graph label and a comment, not this key | none |
| `type.anno.before.modifier` | BaseTypeVisitor | signedness |
| `type.anno.before.decl.anno` | BaseTypeVisitor | signedness |
| `array.initializer` | BaseTypeVisitor | framework:framework, index, regex |
| `enum.declaration` | BaseTypeVisitor | nullness |
| `assignment` | BaseTypeVisitor, GuiEffectVisitor, I18nFormatterVisitor, MustCallConsistencyAnalyzer, NullnessVisitor | aggregate, ainfer-index, ainfer-nullness, ainfer-resourceleak, ... |
| `compound.assignment` | BaseTypeVisitor, UnitsVisitor | fenumswing, framework:typedeclbounds, framework:value, framework:value-non-null-strings-concatenation, ... |
| `unary.increment` | BaseTypeVisitor | framework:value, index |
| `unary.decrement` | BaseTypeVisitor | framework:value, framework:value-ignore-range-overflow, index |
| `enhancedfor` | BaseTypeVisitor | framework:flow, framework:framework, nullness |
| `vector.copyinto` | BaseTypeVisitor | nullness |
| `return` | BaseTypeVisitor, GuiEffectVisitor, ReturnNode | ainfer-nullness, ainfer-testchecker, calledmethods, fenumswing, ... |
| `annotation` | BaseTypeVisitor | framework:framework |
| `conditional` | BaseTypeVisitor | none |
| `switch.expression` | BaseTypeVisitor | nullness |
| `type.argument` | BaseTypeValidator, BaseTypeVisitor | fenum, framework:aliasing, framework:classval, framework:framework, ... |
| `super.wildcard` | BaseTypeValidator | nullness, nullness-warnredundantannotations, tainting |
| `argument` | BaseTypeVisitor, FormatterVisitor, GuiEffectVisitor, I18nFormatterVisitor, LockVisitor | aggregate, ainfer-testchecker, calledmethods-usevaluechecker, command-line, ... |
| `varargs` | AnnotatedTypeFactory, BaseTypeVisitor | framework:value |
| `type.incompatible` | FenumVisitor | fenum, framework:subtyping, nullness |
| `bound` | BaseTypeValidator | framework:h1h2checker, nullness |
| `monotonic` | BaseTypeVisitor | framework:flow, nullness |
| `type.invalid` | BaseTypeValidator | framework:h1h2checker, regex |
| `conflicting.annos` | BaseTypeValidator | nullness, tainting |
| `too.few.annotations` | BaseTypeValidator | none |
| `annotations.on.use` | BaseTypeValidator, NullnessAnnotatedTypeFactory, NullnessVisitor | framework:h1h2checker, framework:typedeclbounds, framework:typedecldefault, guieffect, ... |
| `cast.unsafe` | BaseTypeVisitor | fenumswing, framework:all-systems, framework:framework, framework:subtyping, ... |
| `invariant.cast.unsafe` | BaseTypeVisitor | tainting |
| `cast.unsafe.constructor.invocation` | BaseTypeVisitor, GrowOnlyVisitor | framework:flow, framework:framework, framework:h1h2checker, framework:javaexpression, ... |
| `exception.parameter` | BaseTypeVisitor | fenum, framework:aliasing, framework:h1h2checker |
| `throw` | BaseTypeVisitor | guieffect, lock |
| `expression.unparsable` | DependentTypesHelper, LockAnnotatedTypeFactory, LockVisitor | framework:javaexpression, lock, nullness |
| `explicit.annotation.ignored` | BaseTypeVisitor | framework:lubglb, nullness, tainting |
| `override.return` | BaseTypeVisitor | framework:framework, framework:returnsreceiver, index, interning, ... |
| `override.param` | BaseTypeVisitor | framework:framework, guieffect, interning, nullness, ... |
| `override.receiver` | BaseTypeVisitor, GuiEffectVisitor | framework:framework, framework:h1h2checker, framework:returnsreceiver, nonempty, ... |
| `methodref.return` | BaseTypeVisitor | nullness |
| `methodref.param` | BaseTypeVisitor | i18n, nullness |
| `methodref.receiver` | BaseTypeVisitor | nullness |
| `methodref.receiver.bound` | BaseTypeVisitor | framework:returnsreceiver, nullness |
| `lambda.param` | BaseTypeVisitor | framework:javaexpression, nullness, tainting |
| `expression.parameter.name` | (built dynamically, or unused: verify) | nullness |
| `expression.parameter.name.shadows.field` | BaseTypeVisitor | nullness |
| `inconsistent.constructor.type` | BaseTypeVisitor, LockVisitor, MustCallVisitor | framework:framework, framework:h1h2checker, framework:reflection, lock, ... |
| `super.invocation` | BaseTypeVisitor | framework:framework, framework:h1h2checker, framework:reflection, lock, ... |
| `this.invocation` | BaseTypeVisitor | none |
| `method.invocation` | BaseTypeVisitor, CalledMethodsVisitor | calledmethods, fenum, framework:accumulation, framework:accumulation-norr, ... |
| `constructor.invocation` | BaseTypeVisitor, LockVisitor | framework:javaexpression, lock |
| `type.arguments.not.inferred` | BaseTypeVisitor, Expression | fenum, framework:framework, framework:h1h2checker, framework:lubglb, ... |
| `type.argument.inference.crashed` | BaseTypeVisitor, BoundSet, ConstraintSet | none |
| `type.argument.hasqualparam` | BaseTypeVisitor | tainting |
| `declaration.inconsistent.with.extends.clause` | BaseTypeVisitor | mustcall, tainting |
| `declaration.inconsistent.with.implements.clause` | BaseTypeVisitor | tainting |
| `unallowed.access` | BaseTypeVisitor | none |
| `cast.redundant` | BaseTypeVisitor | none |
| `purity.deterministic.constructor` | BaseTypeVisitor | framework:flow, nullness |
| `purity.deterministic.void.method` | BaseTypeVisitor | framework:flow, nullness |
| `purity.methodref` | BaseTypeVisitor | ainfer-testchecker, framework:all-systems |
| `purity.overriding` | BaseTypeVisitor | none |
| `purity.not.deterministic.assign.array` | (built dynamically, or unused: verify) | framework:flow |
| `purity.not.deterministic.assign.field` | (built dynamically, or unused: verify) | framework:flow |
| `purity.not.deterministic.call` | (built dynamically, or unused: verify) | framework:flow, nullness |
| `purity.not.deterministic.catch` | (built dynamically, or unused: verify) | framework:flow |
| `purity.not.deterministic.object.creation` | (built dynamically, or unused: verify) | framework:flow |
| `purity.not.deterministic.not.sideeffectfree.assign.array` | (built dynamically, or unused: verify) | framework:flow |
| `purity.not.deterministic.not.sideeffectfree.assign.field` | (built dynamically, or unused: verify) | framework:flow |
| `purity.not.deterministic.not.sideeffectfree.call` | (built dynamically, or unused: verify) | framework:flow |
| `purity.not.sideeffectfree.assign.array` | (built dynamically, or unused: verify) | framework:flow |
| `purity.not.sideeffectfree.assign.field` | (built dynamically, or unused: verify) | framework:flow |
| `purity.not.sideeffectfree.call` | (built dynamically, or unused: verify) | framework:flow |
| `purity.more.deterministic` | BaseTypeVisitor | framework:purity-suggestions |
| `purity.more.pure` | BaseTypeVisitor | framework:purity-suggestions |
| `purity.more.sideeffectfree` | BaseTypeVisitor | framework:purity-suggestions |
| `purity.effectively.pure` | BaseTypeVisitor | framework:purity-suggestions |
| `purity.viewpoint.adaptation` | CFAbstractAnalysis | none |
| `flowexpr.parse.index.too.big` | ExpressionTreeToJavaExpressionVisitor | framework:flow |
| `flowexpr.parse.error` | BaseTypeVisitor, DependentTypesHelper, JavaExpressionParseException | framework:flow, index, nullness |
| `flowexpr.parse.error.postcondition` | CFAbstractTransfer, CalledMethodsTransfer | framework:flow, nullness |
| `flowexpr.parse.error.contract` | BaseTypeVisitor | framework:flow, lock, nullness, nullness-asserts |
| `flowexpr.parse.error.sideeffectsonly` | BaseTypeVisitor | none |
| `flowexpr.parse.context.not.determined` | (built dynamically, or unused: verify) | none |
| `flowexpr.parameter.not.final` | BaseTypeVisitor | framework:flow |
| `contracts.precondition` | BaseTypeVisitor, LockVisitor | calledmethods, framework:flow, lock, nonempty, ... |
| `contracts.postcondition` | BaseTypeVisitor | calledmethods, framework:flow, framework:initialized-fields, framework:initialized-fields-value, ... |
| `contracts.conditional.postcondition` | BaseTypeVisitor | calledmethods, framework:flow, framework:value, index, ... |
| `contracts.conditional.postcondition.returntype` | BaseTypeVisitor | framework:flow, nullness |
| `contracts.precondition.override` | (built dynamically, or unused: verify) | framework:flow, index, lock, nullness |
| `contracts.postcondition.override` | (built dynamically, or unused: verify) | framework:flow, nullness |
| `contracts.conditional.postcondition.true.override` | (built dynamically, or unused: verify) | framework:flow, index |
| `contracts.conditional.postcondition.false.override` | (built dynamically, or unused: verify) | none |
| `contracts.precondition.methodref` | (built dynamically, or unused: verify) | none |
| `contracts.postcondition.methodref` | (built dynamically, or unused: verify) | none |
| `contracts.conditional.postcondition.true.methodref` | (built dynamically, or unused: verify) | none |
| `contracts.conditional.postcondition.false.methodref` | (built dynamically, or unused: verify) | none |
| `invalid.polymorphic.qualifier` | BaseTypeVisitor | nullness, tainting |
| `invalid.polymorphic.qualifier.use` | BaseTypeVisitor | framework:returnsreceiver, tainting |
| `missing.has.qual.param` | BaseTypeVisitor | none |
| `conflicting.qual.param` | BaseTypeVisitor | tainting |
| `invalid.qual.param` | BaseTypeVisitor | tainting |
| `field.invariant.not.found` | BaseTypeVisitor | nullness |
| `field.invariant.not.final` | BaseTypeVisitor | nullness |
| `field.invariant.not.subtype` | BaseTypeVisitor | framework:value, index |
| `field.invariant.not.wellformed` | BaseTypeVisitor | nullness |
| `field.invariant.not.found.superclass` | FieldInvariants | framework:value, index |
| `field.invariant.not.subtype.superclass` | FieldInvariants | framework:value, index |
| `invalid.annotation.location.bytecode` | ElementAnnotationApplier | none |
| `instanceof.unsafe` | BaseTypeVisitor | tainting |
| `instanceof.pattern.unsafe` | BaseTypeVisitor | tainting |
| `anno.on.irrelevant` | BaseTypeVisitor, GenericAnnotatedTypeFactory | i18n-formatter, index, regex, signedness |
| `redundant.anno` | BaseTypeVisitor, NullnessVisitor, SourceChecker | nullness-warnredundantannotations |
| `type.inference.failed` | (built dynamically, or unused: verify) | none |
| `slow.typechecking` | BaseTypeVisitor | none |

### `framework/src/main/java/org/checkerframework/framework/source/messages.properties` (4 keys)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `annotation.not.completed` | AnnotatedTypeFactory | none |
| `unneeded.suppression` | NullnessVisitor, SourceChecker | resourceleak, resourceleak-nocreatesmustcallfor |
| `lsp.type.information` | TypeInformationPresenter | none |
| `ambiguous.ajava` | AnnotatedTypeFactory | none |

### `checker/src/main/java/org/checkerframework/checker/nullness/messages.properties` (23 keys)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `dereference.of.nullable` | NullnessVisitor | initialization, nullness, nullness-asserts, nullness-assumeassertions, ... |
| `iterating.over.nullable` | NullnessVisitor | nullness |
| `unboxing.of.nullable` | NullnessVisitor | nullness |
| `throwing.nullable` | NullnessVisitor | nullness |
| `locking.nullable` | NullnessVisitor | nullness |
| `accessing.nullable` | NullnessVisitor | nullness |
| `condition.nullable` | NullnessVisitor | nullness |
| `switching.nullable` | NullnessVisitor | nullness |
| `toarray.nullable.elements.not.newarray` | CollectionToArrayHeuristics | nullness |
| `toarray.nullable.elements.mismatched.size` | CollectionToArrayHeuristics | nullness |
| `clear.system.property` | NullnessVisitor | nullness |
| `nulltest.redundant` | NullnessVisitor | nullness, nullness-asserts |
| `instanceof.nullable` | NullnessVisitor | nullness, nullness-warnredundantannotations |
| `instanceof.nonnull.redundant` | NullnessVisitor | nullness |
| `new.array` | NullnessVisitor | nullness |
| `new.class` | NullnessVisitor | nullness |
| `nullness.on.constructor` | NullnessVisitor | initialization, nullness, nullness-warnredundantannotations |
| `nullness.on.enum` | NullnessVisitor | nullness, nullness-warnredundantannotations |
| `nullness.on.exception.parameter` | NullnessVisitor | nullness, nullness-warnredundantannotations |
| `nullness.on.outer` | NullnessVisitor | nullness |
| `nullness.on.primitive` | NullnessVisitor | nullness |
| `nullness.on.receiver` | NullnessVisitor | nullness, nullness-warnredundantannotations |
| `nullness.on.supertype` | NullnessVisitor | nullness |

### `checker/src/main/java/org/checkerframework/checker/initialization/messages.properties` (8 keys)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `initialization.field.write.initialized` | InitializationVisitor | initialization, nullness |
| `initialization.field.write.unknown` | InitializationVisitor | none |
| `initialization.field.write.in.constructor` | (built dynamically, or unused: verify) | nullness |
| `initialization.constructor.return.type` | InitializationVisitor | initialization |
| `initialization.field.type` | InitializationVisitor | initialization |
| `initialization.field.uninitialized` | InitializationVisitor | initialization, nullness |
| `initialization.fields.uninitialized` | InitializationVisitor | ainfer-nullness, initialization, nullness |
| `initialization.static.field.uninitialized` | InitializationVisitor | ainfer-nullness, nullness, nullness-records |

### `checker/src/main/java/org/checkerframework/checker/lock/messages.properties` (20 keys)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `contracts.precondition` | BaseTypeVisitor, LockVisitor | calledmethods, framework:flow, lock, nonempty, ... |
| `override.sideeffect` | LockVisitor | lock |
| `multiple.sideeffect.annotations` | LockAnnotatedTypeFactory | none |
| `multiple.lock.precondition.annotations` | LockVisitor | lock |
| `multiple.guardedby.annotations` | LockVisitor | lock |
| `method.guarantee.violated` | LockVisitor | lock, lock-records |
| `cannot.dereference` | (built dynamically, or unused: verify) | none |
| `immutable.type.guardedby` | LockVisitor | lock |
| `explicit.lock.synchronized` | LockVisitor | lock |
| `guardsatisfied.with.mayreleaselocks` | LockVisitor | lock |
| `guardsatisfied.parameters.must.match` | LockVisitor | lock |
| `guardsatisfied.return.must.have.index` | LockVisitor | lock |
| `guardsatisfied.assignment.disallowed` | LockVisitor | lock |
| `guardsatisfied.location.disallowed` | LockVisitor | lock |
| `lockingfree.synchronized.method` | LockVisitor | lock |
| `synchronized.block.in.lockingfree.method` | LockVisitor | lock |
| `lock.expression.not.final` | LockAnnotatedTypeFactory, LockVisitor | lock |
| `lock.expression.possibly.not.final` | LockVisitor | lock |
| `lock.not.held` | LockVisitor | lock, lock-safedefaults |
| `inconsistent.constructor.type` | BaseTypeVisitor, LockVisitor, MustCallVisitor | framework:framework, framework:h1h2checker, framework:reflection, lock, ... |

### `checker/src/main/java/org/checkerframework/checker/index/lowerbound/messages.properties` (3 keys)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `array.access.unsafe.low` | LowerBoundVisitor | index |
| `array.length.negative` | LowerBoundVisitor | index |
| `from.not.nonnegative` | LowerBoundVisitor | index |

### `checker/src/main/java/org/checkerframework/checker/index/upperbound/messages.properties` (7 keys)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `array.access.unsafe.high` | UpperBoundVisitor | index |
| `array.access.unsafe.high.constant` | UpperBoundVisitor | index |
| `array.access.unsafe.high.range` | UpperBoundVisitor | index |
| `different.length.sequences.offsets` | UpperBoundVisitor | index |
| `to.not.ltel` | UpperBoundVisitor | index |
| `which.subsequence` | UpperBoundVisitor | index |
| `not.final` | UpperBoundVisitor | index |

### `checker/src/main/java/org/checkerframework/checker/index/inequality/messages.properties` (1 key)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `from.gt.to` | LessThanVisitor | index |

### `checker/src/main/java/org/checkerframework/checker/index/growonly/messages.properties` (1 key)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `growonly.new.bottom` | GrowOnlyVisitor | none |

### `checker/src/main/java/org/checkerframework/checker/interning/messages.properties` (7 keys)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `not.interned` | InterningVisitor | interning |
| `unnecessary.equals` | InterningVisitor | interning |
| `overrides.equals` | InterningVisitor | interning |
| `superclass.notannotated` | InterningVisitor | interning |
| `superclass.annotated` | (built dynamically, or unused: verify) | none |
| `interned.object.creation` | InterningVisitor | interning |
| `invalid.method.annotation` | InterningVisitor, SignednessVisitor | interning |

### `checker/src/main/java/org/checkerframework/checker/signedness/messages.properties` (17 keys)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `operation.unsignedrhs` | SignednessVisitor | signedness |
| `operation.unsignedlhs` | SignednessVisitor | signedness |
| `operation.mixed.unsignedlhs` | SignednessVisitor | signedness |
| `operation.mixed.unsignedrhs` | SignednessVisitor | signedness |
| `shift.signed` | SignednessVisitor | signedness |
| `shift.unsigned` | SignednessVisitor | signedness |
| `comparison.unsignedlhs` | SignednessVisitor | signedness |
| `comparison.unsignedrhs` | SignednessVisitor | signedness |
| `comparison.mixed.unsignedlhs` | SignednessVisitor | signedness |
| `comparison.mixed.unsignedrhs` | SignednessVisitor | signedness |
| `compound.assignment.unsigned.variable` | SignednessVisitor | signedness |
| `compound.assignment.unsigned.expression` | SignednessVisitor | signedness |
| `compound.assignment.mixed.unsigned.variable` | SignednessVisitor | signedness |
| `compound.assignment.mixed.unsigned.expression` | SignednessVisitor | signedness |
| `compound.assignment.shift.signed` | SignednessVisitor | signedness |
| `compound.assignment.shift.unsigned` | SignednessVisitor | signedness |
| `unsigned.concat` | SignednessVisitor | signedness |

### `checker/src/main/java/org/checkerframework/checker/regex/messages.properties` (2 keys)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `group.count` | RegexVisitor | regex |
| `group.count.unknown` | RegexVisitor | regex |

### `checker/src/main/java/org/checkerframework/checker/fenum/messages.properties` (2 keys)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `binary` | FenumVisitor | fenum |
| `compound.assignment` | BaseTypeVisitor, UnitsVisitor | fenumswing, framework:typedeclbounds, framework:value, framework:value-non-null-strings-concatenation, ... |

### `checker/src/main/java/org/checkerframework/checker/formatter/messages.properties` (8 keys)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `format.method` | FormatterTreeUtil, FormatterVisitor | formatter |
| `format.string` | FormatterVisitor | formatter |
| `format.argument.unused` | FormatterVisitor | formatter |
| `format.specifier.null` | FormatterVisitor | formatter |
| `format.excess.arguments` | FormatterVisitor | formatter |
| `format.missing.arguments` | FormatterVisitor | formatter |
| `format.asformat.indirect.arguments` | FormatterTransfer | none |
| `format.indirect.arguments` | FormatterVisitor | formatter |

### `checker/src/main/java/org/checkerframework/checker/i18nformatter/messages.properties` (7 keys)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `i18nformat.formatfor` | I18nFormatterTreeUtil, I18nFormatterVisitor | i18n-formatter |
| `i18nformat.string` | I18nFormatterVisitor | i18n-formatter |
| `i18nformat.argument.unused` | I18nFormatterVisitor | i18n-formatter |
| `i18nformat.excess.arguments` | I18nFormatterVisitor | i18n-formatter |
| `i18nformat.missing.arguments` | I18nFormatterVisitor | i18n-formatter |
| `i18nformat.indirect.arguments` | I18nFormatterTransfer, I18nFormatterVisitor | i18n-formatter |
| `i18nformat.key.not.found` | I18nFormatterTransfer | none |

### `checker/src/main/java/org/checkerframework/checker/guieffect/messages.properties` (9 keys)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `call.ui` | GuiEffectVisitor | guieffect |
| `annotations.conflicts` | GuiEffectVisitor | guieffect |
| `override.effect` | GuiEffectTypeFactory | guieffect |
| `override.effect.polymorphic` | GuiEffectTypeFactory | none |
| `override.effect.nonui` | GuiEffectTypeFactory | guieffect |
| `override.effect.warning.inheritance` | GuiEffectTypeFactory | guieffect |
| `polymorphism` | GuiEffectVisitor | guieffect |
| `inheritance.polymorphic` | (built dynamically, or unused: verify) | none |
| `effects.redundant.uitype` | GuiEffectVisitor | none |

### `checker/src/main/java/org/checkerframework/checker/optional/messages.properties` (11 keys)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `prefer.map` | OptionalImplVisitor | optional |
| `prefer.map.and.orelse` | OptionalImplVisitor | optional |
| `prefer.ifpresent` | OptionalImplVisitor | optional |
| `introduce.eliminate` | OptionalImplVisitor | optional |
| `optional.as.element.type` | OptionalImplVisitor | optional |
| `optional.null.assignment` | OptionalImplVisitor | optional |
| `optional.null.comparison` | OptionalImplVisitor | optional |
| `optional.collection` | OptionalImplVisitor | optional |
| `optional.nesting` | OptionalImplVisitor | optional |
| `optional.field` | OptionalImplVisitor | optional |
| `optional.parameter` | OptionalImplVisitor | optional |

### `checker/src/main/java/org/checkerframework/checker/calledmethods/messages.properties` (4 keys)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `finalizer.invocation` | CalledMethodsVisitor | calledmethods, calledmethods-autovalue, calledmethods-disablereturnsreceiver, calledmethods-lombok, ... |
| `ensuresvarargs.unverified` | CalledMethodsVisitor | calledmethods |
| `ensuresvarargs.invalid` | CalledMethodsVisitor | calledmethods |
| `contracts.exceptional.postcondition` | CalledMethodsVisitor | calledmethods, resourceleak |

### `checker/src/main/java/org/checkerframework/checker/mustcall/messages.properties` (3 keys)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `inconsistent.mustcall.subtype` | MustCallVisitor | mustcall, resourceleak |
| `createsmustcallfor.target.unparsable` | CreatesMustCallForToJavaExpression | mustcall, resourceleak |
| `mustcall.not.inheritable` | MustCallVisitor | mustcall |

### `checker/src/main/java/org/checkerframework/checker/rlccalledmethods/messages.properties` (12 keys)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `required.method.not.called` | MustCallConsistencyAnalyzer, RLCCalledMethodsVisitor, ResourceLeakChecker | ainfer-resourceleak, resourceleak, resourceleak-customignoredexceptions, resourceleak-firstinitconstructor, ... |
| `missing.creates.mustcall.for` | MustCallConsistencyAnalyzer | resourceleak, resourceleak-firstinitconstructor |
| `incompatible.creates.mustcall.for` | MustCallConsistencyAnalyzer | resourceleak |
| `reset.not.owning` | MustCallConsistencyAnalyzer | resourceleak |
| `creates.mustcall.for.override.invalid` | RLCCalledMethodsVisitor | resourceleak |
| `creates.mustcall.for.invalid.target` | RLCCalledMethodsVisitor | resourceleak |
| `destructor.exceptional.postcondition` | (built dynamically, or unused: verify) | none |
| `mustcallalias.out.of.scope` | MustCallConsistencyAnalyzer | resourceleak |
| `mustcallalias.method.return.and.param` | RLCCalledMethodsVisitor | resourceleak |
| `owning.override.param` | RLCCalledMethodsVisitor | none |
| `owning.override.return` | RLCCalledMethodsVisitor | none |
| `required.method.not.known` | MustCallConsistencyAnalyzer | none |

### `checker/src/main/java/org/checkerframework/checker/compilermsgs/messages.properties` (1 key)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `type.incompatible` | FenumVisitor | fenum, framework:subtyping, nullness |

### `framework/src/main/java/org/checkerframework/common/value/messages.properties` (22 keys)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `method.find.failed.in.class` | ReflectiveEvaluator | none |
| `method.find.failed` | ReflectiveEvaluator | none |
| `method.evaluation.failed` | ReflectiveEvaluator | none |
| `method.evaluation.exception` | ReflectiveEvaluator | framework:value |
| `class.find.failed` | ReflectiveEvaluator | framework:value |
| `constructor.evaluation.failed` | ReflectiveEvaluator | none |
| `constructor.invocation.failed` | ReflectiveEvaluator | none |
| `operator.unary.evaluation.failed` | (built dynamically, or unused: verify) | none |
| `operator.binary.evaluation.failed` | (built dynamically, or unused: verify) | none |
| `field.access.failed` | ReflectiveEvaluator | none |
| `too.many.values.given` | ValueVisitor | framework:value |
| `too.many.values.given.int` | ValueVisitor | framework:value |
| `no.values.given` | ValueVisitor | framework:value |
| `from.greater.than.to` | ValueVisitor | framework:value |
| `negative.arraylen` | ValueVisitor | framework:value |
| `class.convert.failed` | (built dynamically, or unused: verify) | none |
| `annotation.intrange.on.noninteger` | ValueVisitor | framework:value |
| `statically.executable.not.pure` | ValueVisitor | framework:value |
| `statically.executable.nonconstant.parameter.type` | ValueVisitor | framework:value |
| `statically.executable.nonconstant.return.type` | ValueVisitor | framework:value |
| `invalid.matches.regex` | ValueVisitor | framework:value |
| `invalid.doesnotmatch.regex` | ValueVisitor | framework:value |

### `framework/src/main/java/org/checkerframework/common/reflection/messages.properties` (3 keys)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `illegal.classname` | ClassValVisitor | framework:classval |
| `illegal.methodname` | MethodValVisitor | framework:methodval |
| `invalid.methodval` | MethodValVisitor | framework:aggregate, framework:methodval |

### `framework/src/main/java/org/checkerframework/common/aliasing/messages.properties` (2 keys)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `unique.leaked` | AliasingVisitor | framework:aliasing, framework:annotationclassloader |
| `unique.location.forbidden` | AliasingVisitor | framework:aggregate, framework:aliasing, framework:compound-checker |

### `framework/src/main/java/org/checkerframework/common/accumulation/messages.properties` (1 key)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `predicate` | AccumulationAnnotatedTypeFactory, AccumulationVisitor | calledmethods, framework:accumulation |

### `framework/src/main/java/org/checkerframework/common/returnsreceiver/messages.properties` (1 key)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `this.location` | ReturnsReceiverVisitor | framework:returnsreceiver |

### `framework/src/main/java/org/checkerframework/common/util/count/report/messages.properties` (7 keys)

| Key | Issued by (class) | Test directories that already expect it |
|-----|-------------------|-----------------------------------------|
| `fieldreadwrite` | ReportVisitor | framework:report |
| `fieldwrite` | ReportVisitor | framework:report |
| `methodcall` | ReportVisitor | framework:report |
| `creation` | ReportVisitor | framework:report |
| `inherit` | ReportVisitor | framework:report |
| `override` | BaseTypeVisitor, ReportVisitor | framework:report |
| `usage` | ReportVisitor | framework:report |

## Appendix B. Hints for keys whose trigger is not obvious

These are starting points, not verified facts; confirm each against the issuing code.
Directories named below are test directories; the example itself goes in the directory's
`messages/` subdirectory.

In some cases, you can move an existing test case (augmenting it with extra text
per the file format specification) or create a new example file that is a subset
of an existing test case.

Framework-wide keys, suggested checker other than Nullness:

- `compound.assignment`: Tainting (`@Untainted String s; s += tainted;`) or Signedness; the
  Fenum Checker defines its own text, so also give a Fenum example if the page should show
  both variants.
- `unary.increment`, `unary.decrement`: Constant Value Checker (`@IntRange(from = 0, to = 9)
  int digit; digit++;`) in `framework/tests/value/`, or Index Checker (`@NonNegative int i;
  i--;`).
- `enum.declaration`: the Nullness Checker reports `nullness.on.enum` first; use Tainting or
  Fenum.
- `annotation`: Regex Checker, an annotation element of type `@Regex String` given a
  non-regex literal.
- `cast.unsafe`: issued as a warning when a cast's qualifier cannot be verified; Nullness
  `(@NonNull String) nullable` if the Nullness Checker does not special-case it, else Regex
  `(@Regex String) s`.  Lint option `cast:unsafe` is on by default.
- `cast.redundant`: needs `-Alint=cast:redundant` (`BaseTypeVisitor.checkTypecastRedundancy`);
  new test directory required.
- `invariant.cast.unsafe`: cast between types whose type arguments differ only in qualifiers
  (`(List<@NonNull String>) listOfNullable`); check `BaseTypeVisitor` for the invariant-array
  case too (`-AinvariantArrays`, directory `nullness-invariantarrays`).
- `cast.unsafe.constructor.invocation`: `new @Untainted Foo()` where the constructor's result
  is not `@Untainted` (Tainting); see `BaseTypeVisitor.checkConstructorInvocation`.
- `exception.parameter`, `throw`: the Nullness Checker has specific keys
  (`nullness.on.exception.parameter`, `throwing.nullable`); use a checker whose exception
  parameter lower bound / throw upper bound is not top, e.g. Tainting
  (`catch (@Untainted RuntimeException e)`), or the Initialization hierarchy.
- `inconsistent.constructor.type`: a constructor whose result type is annotated below top
  (`@Untainted Config() {}` in Tainting).  The Lock Checker overrides the text; give a Lock
  example too (`@GuardedBy` on a constructor result).
- `super.invocation`, `this.invocation`: constructor result annotation incompatible with the
  invoked constructor's; Tainting or Initialization (`@UnderInitialization`).
- `method.invocation`: the classic Initialization case: calling an instance method on `this`
  from a constructor before all fields are initialized (`found: @UnderInitialization`).
- `constructor.invocation`: creating an inner class instance through an outer receiver of the
  wrong qualifier (`outer.new Inner()`), Tainting.
- `methodref.receiver.bound`: `taintedObject::methodWithUntaintedReceiver` (Tainting); for the
  Nullness Checker a nullable bound receiver gives `dereference.of.nullable` instead.
- `override.receiver`: subclass method declares a receiver `this` with a stronger qualifier
  than the overridden method; Tainting or Interning (Nullness forbids receiver annotations).
- `type.argument.hasqualparam`, `invalid.polymorphic.qualifier.use`, `missing.has.qual.param`,
  `conflicting.qual.param`, `invalid.qual.param`: `@HasQualifierParameter(Tainted.class)` in
  the Tainting Checker (see `checker/tests/tainting/` for existing tests).
- `invalid.polymorphic.qualifier`: `@PolyNull` / `@PolyTainted` in a position where a
  polymorphic qualifier is meaningless (e.g., on a class declaration or a top-level field's
  type without qualifier parameter); read `BaseTypeVisitor`/`BaseTypeValidator`.
- `annotations.on.use`, `declaration.inconsistent.with.extends.clause`,
  `declaration.inconsistent.with.implements.clause`: type-declaration bounds
  (`@UpperBoundFor`/annotations on class declarations); Interning (`@Interned class Color`,
  then `@UnknownInterned Color`) or Tainting; see `framework/tests/typedeclbounds/`.
- `too.few.annotations`, `type.invalid`, `conflicting.annos`: `BaseTypeValidator`; Nullness
  `@NonNull @Nullable String` for conflicting annotations; `too.few.annotations` may be
  impossible with shipped checkers (candidate `@no-example`).
- `explicit.annotation.ignored`: search `AnnotatedTypeFactory`/`GenericAnnotatedTypeFactory`
  for the key; probably an explicit qualifier that a defaulting rule overrides.
- `unallowed.access`: field annotated `@Unused(when = SomeQualifier.class)` accessed through a
  receiver of that qualifier (`org.checkerframework.framework.qual.Unused`); check which
  shipped checker supports it (Nullness tests `checker/tests/nullness/Unused*.java` exist).
- `anno.on.irrelevant`: a checker with `@RelevantJavaTypes`, e.g. Signedness (`@Unsigned
  String`) or Format String (`@Format int`).
- `instanceof.unsafe`, `instanceof.pattern.unsafe`: Regex (`s instanceof @Regex String`, and
  the Java 16 pattern form `o instanceof @Regex String r`).
- `expression.unparsable`: dependent-type annotation with an unparsable expression, e.g.
  Index `@LTLengthOf("arr +") int i` or Nullness `@KeyFor("map(") String k`; contrast with
  `flowexpr.parse.error*`, which are for contracts (`@EnsuresNonNull("bad(")`).
- `flowexpr.parse.error.postcondition`, `flowexpr.parse.error.contract`,
  `flowexpr.parse.error.sideeffectsonly`: malformed expressions in `@EnsuresNonNull`,
  `@RequiresNonNull`, `@SideEffectsOnly` (the last in `checker/tests/sideeffectsonly/`).
- `flowexpr.parse.index.too.big`: `@EnsuresNonNull("#2")` on a one-parameter method.
- `flowexpr.parameter.not.final`: a contract mentions `#1`, and the method reassigns that
  parameter.
- `contracts.*.methodref`: a functional interface whose abstract method has a contract
  (`interface Init { @EnsuresNonNull("#1.name") void run(Person p); }`) implemented by a method
  reference to a method without it; `contracts.precondition.methodref` is the easy one
  (`Runnable r = this::methodWithRequiresNonNull`).
- `contracts.conditional.postcondition.returntype`: `@EnsuresNonNullIf` on a `void` method.
- `field.invariant.*`: `@FieldInvariant(qualifier = NonNull.class, field = "...")` on a
  subclass; each key corresponds to one malformed variant (unknown field, non-final field,
  qualifier not a subtype, mismatched counts, weaker than superclass).
- `purity.*`: `@Pure`/`@SideEffectFree`/`@Deterministic` methods whose bodies violate the
  claim; `purity.methodref` and `purity.overriding` compare purity annotations of overriding
  methods and method references; `purity.effectively.pure` is `@SideEffectFree @Deterministic`
  on one method; `purity.deterministic.constructor` and `.void.method` are self-explanatory.
- `unneeded.suppression`: a `@SuppressWarnings("resourceleak:required.method.not.called")`
  on code that has no leak, in `checker/tests/resourceleak/` (driver passes
  `-AwarnUnneededSuppressions`).
- `redundant.anno`: `@NonNull String` where the default is `@NonNull`, in
  `checker/tests/nullness-warnredundantannotations/`.
- `type.anno.before.modifier`: `@Nullable public String f;`; `type.anno.before.decl.anno`:
  `@Nullable @Deprecated String f;`.  Both are warnings from `BaseTypeVisitor.visitVariable`
  about annotation placement, issued by every checker.

Checker-specific keys worth a note:

- Nullness `toarray.nullable.elements.not.newarray`: `collection.toArray(existingArray)`
  where the argument is not `new T[...]`; `.mismatched.size`: `toArray(new String[10])`.
  `clear.system.property`: `System.clearProperty(name)` or `System.setProperties(null)`.
  `new.array`: `new @NonNull String[n]` under `-Alint=soundArrayCreationNullness`.
- Initialization `initialization.field.write.unknown`: assigning a possibly-uninitialized
  value to a field through a receiver of type `@UnknownInitialization`.
- Lock: `cannot.dereference` is likely dead (no code issues it); `multiple.sideeffect.annotations`
  is a method with two of `@LockingFree`, `@ReleasesNoLocks`, `@MayReleaseLocks`.
- Index `growonly.new.bottom`: `new @BottomGrowShrink ArrayList<>()`.  `which.subsequence`,
  `to.not.ltel`, `from.not.nonnegative`, `from.gt.to`, `not.final`: `@HasSubsequence`.
- Value `*.evaluation.failed`, `method.find.failed*`, `field.access.failed`,
  `class.convert.failed`: only with `-AreportEvalWarns` (passed in `framework/tests/value/`),
  when `@StaticallyExecutable` evaluation fails, e.g. `Integer.parseInt("abc")` with a
  constant argument (throws) or a method not found reflectively; those that no code path can
  reach are `@no-example`.
- Resource Leak `destructor.exceptional.postcondition`: `@EnsuresCalledMethods` on a
  destructor whose exceptional path does not close the owning field; verify whether the key is
  still issued (Appendix A marks it "verify").  `required.method.not.known`: an object of an
  unconstrained generic type with unknown must-call obligations.
- Called Methods `contracts.exceptional.postcondition`: `@EnsuresCalledMethodsOnException`.
- GUI Effect `polymorphism`, `effects.redundant.uitype`, `override.effect.polymorphic`: see
  `GuiEffectVisitor`; `inheritance.polymorphic` appears dead.
- Format String `format.asformat.indirect.arguments`: passing an array rather than varargs to
  `FormatUtil.asFormat`.  I18n `i18nformat.key.not.found`: a key missing from the properties
  file given by `-Apropfiles`; check the `I18nFormatterTest` driver options.
- `lsp.type.information`: emitted only for the language-server integration
  (`-AlspTypeInfo`?); `@no-example` unless an option makes it trivial, and `@informational`
  either way (question 4 of Section 1).
- `ambiguous.ajava`, `annotation.not.completed`, `invalid.annotation.location.bytecode`,
  `slow.typechecking`, `type.inference.failed`, `type.argument.inference.crashed`: `@no-example`.

## Appendix C. Test directories and their drivers (initial `CHECKERS` table)

Extracted from the `super(...)` calls of the JUnit drivers on 2026-09-02.  Options omit
`-Anomsgtext` (added by the harness) and `-encoding`/`-Xlint` details.  Examples go in
the `messages/` subdirectory of the listed directory.  Directories not listed here (e.g.,
`ainfer-*`, `nullness-skip*`) should not hold examples.

The first three columns are the `CHECKERS` table of Section 4.2.  The last two columns are
source material for `NAMESPACES`, which is keyed by properties directory rather than by test
directory; Phase 1 step 2 builds `NAMESPACES` from them, adds each checker's display name (e.g.
"Nullness Checker"), and verifies every prefix with `-AshowPrefixInWarningMessages`, because a
properties directory's name is often not a prefix (Section 4.2).  A `-nowarn` in the options column is recorded rather than omitted, because it
may suppress a warning that an example must show.  Checked on 2026-09-03: every key of
`calledmethods`, `accumulation`, and `returnsreceiver` is reported with `reportError`, so the
two `-nowarn` directories hold no warning example and the question does not arise there.
Re-check before putting a warning's example in a `-nowarn` directory.

| Test directory | Checker class (`org.checkerframework.` prefix omitted) | Extra options | Manual anchor | Suppression prefix |
|----------------|--------------------------------------------------------|---------------|---------------|--------------------|
| `checker/tests/nullness` | `checker.nullness.NullnessChecker` | `-AcheckPurityAnnotations -Alint=soundArrayCreationNullness,redundantNullComparison` | `nullness-checker` | `nullness` |
| `checker/tests/initialization` | `checker.nullness.NullnessChecker` | `-AcheckPurityAnnotations -Alint=soundArrayCreationNullness,redundantNullComparison` | `initialization-checker` | `nullness` (**not** `initialization`; see Section 4.2) |
| `checker/tests/nullness-warnredundantannotations` | `NullnessChecker` | `-AwarnRedundantAnnotations` | `nullness-checker` | `nullness` |
| `checker/tests/nullness-checkcastelementtype` | `NullnessChecker` | `-AcheckCastElementType` | | |
| `checker/tests/nullness-invariantarrays` | `NullnessChecker` | `-AinvariantArrays` | | |
| `checker/tests/nullness-records` | `NullnessChecker` | `-AcheckPurityAnnotations` | | |
| `checker/tests/interning` | `checker.interning.InterningChecker` | | `interning-checker` | `interning` |
| `checker/tests/interning-warnredundantannotations` | `InterningChecker` | `-AwarnRedundantAnnotations` | | |
| `checker/tests/lock` | `checker.lock.LockChecker` | | `lock-checker` | `lock` |
| `checker/tests/index` | `checker.index.IndexChecker` | | `index-checker` | `index` (subcheckers: `lowerbound`, `upperbound`, `samelen`, `searchindex`, `substringindex`, `lessthan`, and -- for the keys of the `growonly` properties directory -- `mutable`, **not** `growonly`; checked on 2026-09-03 against the `@SuppressWarningsPrefix` annotations) |
| `checker/tests/signedness` | `checker.signedness.SignednessChecker` | | `signedness-checker` | `signedness` |
| `checker/tests/regex` | `checker.regex.RegexChecker` | | `regex-checker` | `regex` |
| `checker/tests/fenum` | `checker.fenum.FenumChecker` | | `fenum-checker` | `fenum` |
| `checker/tests/formatter` | `checker.formatter.FormatterChecker` | | `formatter-checker` | `formatter` |
| `checker/tests/i18n-formatter` | `checker.i18nformatter.I18nFormatterChecker` | | `i18n-formatter-checker` | `i18nformatter` |
| `checker/tests/i18n` | `checker.i18n.I18nChecker` | | `i18n-checker` (a section of `propkey-checker.tex`) | `i18n`.  Checked on 2026-09-03: this checker has no `messages.properties` of its own, so it owns no key and normally holds no example. |
| `checker/tests/guieffect` | `checker.guieffect.GuiEffectChecker` | | `guieffect-checker` | `guieffect` |
| `checker/tests/optional` | `checker.optional.OptionalChecker` | `-AoptionalMapAssumeNonNull` | `optional-checker` | `optional` |
| `checker/tests/optional-side-effects` | `OptionalChecker` | `-AcheckPurityAnnotations` | | |
| `checker/tests/calledmethods` | `checker.calledmethods.CalledMethodsChecker` | `-nowarn` (all of this checker's keys are errors, so this is harmless) | `called-methods-checker` | `calledmethods` |
| `checker/tests/mustcall` | `checker.mustcall.MustCallChecker` | | `must-call-checker` | `mustcall` |
| `checker/tests/resourceleak` | `checker.resourceleak.ResourceLeakChecker` | `-AwarnUnneededSuppressions` | `resource-leak-checker` | `resourceleak`, `builder`; but its 12 keys come from the `rlccalledmethods` properties directory, whose issuing `RLCCalledMethodsChecker` declares `{"calledmethods", "rlccalledmethods", "resourceleak"}`, so their pages use `rlccalledmethods` (Section 4.2) |
| `checker/tests/tainting` | `checker.tainting.TaintingChecker` | | `tainting-checker` | `tainting` |
| `checker/tests/sideeffectsonly` | `TaintingChecker` | `-AcheckPurityAnnotations` | `purity-checker` | `tainting` |
| `checker/tests/signature` | `checker.signature.SignatureChecker` | | `signature-checker` | `signature` |
| `checker/tests/units` | `checker.units.UnitsChecker` | | `units-checker` | `units` |
| `checker/tests/sqlquotes` | `checker.sqlquotes.SqlQuotesChecker` | | `sql-quotes-checker` | `sqlquotes` |
| `checker/tests/nonempty` | `checker.nonempty.NonEmptyChecker` | | `non-empty-checker` | `nonempty` |
| `checker/tests/compilermsg` | `checker.compilermsgs.CompilerMessagesChecker` | `-Apropfiles=tests/compilermsg/compiler.properties` | `compilermsgs-checker` (a section of `propkey-checker.tex`, not a chapter) | `compilermsgs` |
| `framework/tests/value` | `common.value.ValueChecker` | `-AreportEvalWarns -Astubs=tests/value/minints-stub.astub:tests/value/lowercase.astub` | `constant-value-checker` | `value` |
| `framework/tests/aliasing` | `common.aliasing.AliasingChecker` | | `aliasing-checker` | `aliasing` |
| `framework/tests/classval` | `common.reflection.ClassValChecker` | | `reflection-resolution` | `classval` |
| `framework/tests/methodval` | `common.reflection.MethodValChecker` | | `reflection-resolution` | `methodval` |
| `framework/tests/returnsreceiver` | `common.returnsreceiver.ReturnsReceiverChecker` | `-Astubs=stubs/ -nowarn` | `returns-receiver-checker` | `returnsreceiver` |
| `framework/tests/report` | `common.util.count.report.ReportChecker` | `-Astubs=tests/report/reporttest.astub` | (none; developer tool) | `report` |
| `framework/tests/initialized-fields` | `common.initializedfields.InitializedFieldsChecker` | | `initialized-fields-checker` | `initializedfields` |
| `framework/tests/accumulation`, `flow`, `framework`, `subtyping`, `typedeclbounds`, ... | test checkers | | | avoid: pages should show shipped checkers |

Directories to create if needed (assumption 9): `checker/tests/nullness-suggestpuremethods`
(`-AsuggestPureMethods`, for `purity.more.*` and `purity.effectively.pure`),
`checker/tests/nullness-castredundant` (`-Alint=cast:redundant`, for `cast.redundant`), and
`checker/tests/i18n-formatter-propfiles` (`-Apropfiles=...`, for `i18nformat.key.not.found`;
checked on 2026-09-03, `I18nFormatterTest` passes neither `-Apropfiles` nor `-Abundlenames`).
