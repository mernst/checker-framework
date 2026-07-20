# Review of PR typetools/checker-framework#7818 — `@SideEffectsOnly` annotation

**Scale:** 84 files, +1989/−128. Author: mernst. Base: `master`. Head: `side-effects-only-2`.

## Overview

Adds a new declaration annotation `@SideEffectsOnly({"expr", ...})` naming an upper bound on the
expressions a method or constructor may modify. Three parts:

1. **The annotation** (`checker-qual`), registered as inherited in `AnnotatedTypeFactory.postInit`,
   and a new `PurityKind.SIDE_EFFECTS_ONLY`.
2. **Declaration-site checking** (new `DisallowedSideEffects`, a `TreePathScanner` driven from
   `BaseTypeVisitor.checkPurityAnnotations`), with a `UnionFind`-based syntactic alias
   approximation. Four new message keys.
3. **Use-site effect** — `CFAbstractStore.updateForMethodCall` now retains refinements for
   expressions that do not contain a listed expression; `CFAbstractAnalysis` parses and caches the
   view-adapted expressions per call site.

Plus manual/CHANGELOG updates, ~15 new test files, and a large batch of `@SideEffectsOnly("this")`
annotations on the Checker Framework's own collection classes.

The design is well documented — the Javadoc and manual are unusually explicit about the unsoundness
and the fail-closed choices. Most of what follows is about the edges.

---

## Correctness

### 1. Lock Checker: `@SideEffectsOnly` is treated as a locking guarantee it does not make (soundness) — `LockAnnotatedTypeFactory.java:419`

`SIDEEFFECTSONLY` is inserted into the `SideEffectAnnotation` ordering strictly *stronger* than
`LOCKINGFREE`. But `@SideEffectsOnly` says nothing about locks. `methodSideEffectAnnotation` returns
the weakest annotation *present*, so a method whose only annotation is `@SideEffectsOnly("this.f")`
yields `SIDEEFFECTSONLY`, and every `isWeakerThan(LOCKINGFREE)` guard in `LockVisitor` (lines 211,
825) then evaluates to false — the Lock Checker concludes the method neither acquires nor releases
locks. Concretely:

```java
@SideEffectsOnly("this.count")
void bump() { lock.unlock(); this.count++; }   // Lock Checker now treats bump() as @LockingFree
```

Since the two properties are orthogonal, `@SideEffectsOnly` should sort no stronger than the default
(`RELEASESNOLOCKS`), or — cleaner — should not be a member of `SideEffectAnnotation` at all. Note
also that `isSideEffectFree` (line 800) deliberately does *not* accept `SIDEEFFECTSONLY`, which is
inconsistent with placing it above `LOCKINGFREE` in the lattice.

### 2. `Iterator.remove()` annotated `@SideEffectsOnly("this")` is wrong — `LinkedHashKeyedSet.java:61`

`KeyedSetIterator.remove()` delegates to `itr.remove()`, which mutates the enclosing set's backing
map (`theValues`) — not reachable through the iterator's `this`. A caller that holds a refinement
about the backing collection keeps it across `it.remove()`. The same annotation on
`JavaDiagnosticReader.remove()` and `Insertions`'s `remove()` is harmless only because those throw
`UnsupportedOperationException`; the pattern will be copied. Either drop the annotation here or list
the enclosing collection.

### 3. Alias collection is order-dependent — `DisallowedSideEffects.java`

Aliases are unioned during the same traversal that reports errors, so an alias established *after* a
modification does not cover it, while the same two statements in the other order do:

```java
@SideEffectsOnly("#1")
void m(List<String> a, List<String> b) {
  b.add("x");        // reported
  List<String> t = a; t = b;
}
// vs. the manual's own example, where the assignments precede the call — accepted
```

The result is neither sound nor consistently conservative, just order-sensitive. A two-pass scan
(collect all `union`s, then check) would make it order-independent. Worth at least a `TODO`
documenting the asymmetry, since the manual presents the alias handling as flow-insensitive.

### 4. `@SideEffectsOnly` is enforced inside anonymous-class bodies — `DisallowedSideEffects.visitNewClass`

`super.visitNewClass` descends into `node.getClassBody()`, and `visitMethod` is not overridden, so
side effects in a *declared but not invoked* method are attributed to the enclosing method:

```java
@SideEffectsOnly("#1")
void m(List<String> c) {
  Runnable r = new Runnable() { public void run() { staticField = 1; } };  // false positive
}
```

Lambdas have the same issue. Consider overriding `visitMethod` to return without scanning, and
deciding explicitly what to do about lambda bodies.

### 5. `MethodInvocationNode` is a structural-equality cache key — `CFAbstractAnalysis.java`

`sideEffectsOnlyExpressionsCache` is a `HashMap<MethodInvocationNode, …>`, but
`MethodInvocationNode.equals` (line 163) compares target and arguments, not identity. Two distinct
call sites in one CFG with the same syntax collide. In practice the view-adapted result is the same,
so this appears benign today — but the correctness argument is non-obvious and unstated. Either use
an `IdentityHashMap` or add a comment explaining why structural collision is safe. The same
reasoning question applies to `checker.report(method, ...)` in `computeSideEffectsOnlyExpressions`:
on a collision the parse error is attributed to whichever call site got there first.

### 6. Use-site aliasing unsoundness is undocumented at the point it bites — `CFAbstractStore.isSideEffected`

The new `containsSyntacticEqualJavaExpression` test drops refinements only for expressions that
syntactically contain a listed one. A local aliasing a listed expression keeps its refinement:

```java
List<String> b = this.f;
m();                     // @SideEffectsOnly("this.f")
// refinement on `b` survives, although m() mutated the same object
```

The manual documents aliasing unsoundness for *declaration-site checking*; this is a distinct
use-site hole. Please state it in the `@SideEffectsOnly` Javadoc and in `isSideEffected`, since this
is the direction users will actually rely on.

---

## Build / infrastructure

### 7. Maven 4 install is very likely broken, and is unrelated scope — `Dockerfile-*`

```dockerfile
RUN ... && cd $HOME && wget .../apache-maven-4.0.0-rc-5-bin.tar.gz && tar xzf ...
ENV PATH="$HOME/apache-maven-4.0.0-rc-5/bin:$PATH"
```

Docker expands only variables declared via `ENV`/`ARG` *in the same Dockerfile*. `HOME` is not
declared, and the `ubuntu` base image does not set it in its config — so `$HOME` in the `ENV` line
expands to the empty string and `PATH` gains `/apache-maven-4.0.0-rc-5/bin`, while `tar` extracted
to `/root/…`. Since the `maven` apt package was simultaneously removed, any CI job invoking `mvn`
will now fail. Use a literal path (`/opt/apache-maven-4.0.0-rc-5/bin`).

Also in the same hunk: no checksum/signature verification of the downloaded tarball,
`dlcdn.apache.org` purges non-current releases (pinning an RC there is a time bomb — use
`archive.apache.org`), and the tarball is never deleted, so it stays in the layer.

Separately: switching the CI base image to a Maven 4 release candidate has nothing to do with
`@SideEffectsOnly`. In an 84-file PR this is worth splitting out so it can be reverted
independently.

---

## Style and conventions

- The code matches Checker Framework conventions well: full Javadoc on every new member, `@Nullable`
  discipline, message keys in `messages.properties`, `// :: error:` test annotations.
- `calleeSideEffectedExpressions` and `constructorSideEffectedExpressions` are near-duplicates (~20
  lines each) differing only in the `StringToJavaExpression` entry point, the `ThisReference`
  filter, and the diagnostic name. Worth factoring.
- `AnnotationMirrorSet.descendingSet()` gaining `@Pure` while its body is `throw new Error("Not yet
  implemented.")` is an odd drive-by; it reads as an assertion about a method that has no behavior.
- The `TypecheckResult.java` and `CompilationResult.java` changes are comment-only TODOs about a
  *pre-existing* test-harness gap (unexpected warnings not reported). Fine to include, but they hint
  the new warning-only paths are not fully verifiable by the test harness — see below.
- `containsAsReceiver`'s Javadoc correctly warns the relation is asymmetric, and
  `isCoveredByAnnotation` restates the argument-order requirement at the call. Good.

---

## Test coverage

Genuinely thorough for the declaration-site checker: constructors, method references (including the
`@Deterministic`-fails-but-`@SideEffectsOnly`-passes case), inheritance, conflicting annotations,
empty annotation, malformed expressions, stub-file-supplied annotations, aliasing with and without
nesting.

Gaps:

- **No test that `@SideEffectsOnly` actually preserves a refinement** across a call. Every
  `sideeffectsonly/` test I read exercises `DisallowedSideEffects`; the
  `CFAbstractStore.isSideEffected` change — the whole point of the feature for users — appears
  untested except indirectly via `optional-side-effects`. Add a Tainting or Nullness test where a
  refinement survives a `@SideEffectsOnly` call and is dropped by an unannotated one.
- **No Lock Checker test** for the new `SIDEEFFECTSONLY` lattice position (finding 1).
- **No test for the alias-ordering asymmetry** (finding 3) or the anonymous-class false positive
  (finding 4).
- `purity.unknown.sideeffectsonly` on a `new` expression is tested, but there is no test of the
  interaction that will dominate real use: a `@SideEffectsOnly` method calling *any* JDK method. The
  manual concedes this "often reports `purity.unknown.sideeffectsonly`". That is an error, not a
  warning — worth a deliberate decision, because it makes `-AcheckPurityAnnotations` +
  `@SideEffectsOnly` close to unusable on real code until the JDK stubs are annotated.

## Security

Nothing security-relevant beyond the unverified Maven download in finding 7.

---

## Summary

The feature is carefully designed and documented, and the test suite for the declaration-site
checker is strong. Before merge I would prioritize: **(1)** the Lock Checker lattice placement,
**(2)** the `Iterator.remove()` annotation, **(7)** the Dockerfile `$HOME` expansion. Findings 3–6
are worth at least documenting if not fixing, and the store-refinement behavior needs direct tests.

One logistical note: the PR depends on `UnionFind`, which exists in plume-util `master` but not in
any release — `libs.versions.toml` pins `1.15.0-SNAPSHOT`. Merging is gated on that release.
