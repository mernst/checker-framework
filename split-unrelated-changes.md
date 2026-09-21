# Plan: move unrelated changes out of the `canonical-annotation` branch

The `canonical-annotation` branch splits
`AnnotatedTypeFactory.canonicalAnnotation` into two hooks:
`canonicalAnnotation`, which the framework calls when an annotation is added to
an `AnnotatedTypeMirror`, and `canonicalAnnotationForComparison`, which the
`QualifierHierarchy` methods that take a `TypeMirror` call.

The branch also carries five changes that have nothing to do with that split.
This document lists those five changes and gives a procedure for moving them
into a separate pull request.

## The unrelated changes

Each entry gives the change, the files it touches, and the commit on the
`canonical-annotation` branch that introduced it.

### 1. `DoubleJavacVisitor.assertSameKind` dereferences a null tree

`framework/src/main/java/org/checkerframework/framework/ajava/DoubleJavacVisitor.java`,
in commit `32222adba4` ("Code improvements").

The error message for "one tree is null" called `tree1.getKind()` and
`tree2.getKind()` unconditionally, so building the message threw a
`NullPointerException` in exactly the situation the message describes.  The
change guards both calls.

This is a bug fix that stands on its own.

### 2. `@DoesNotUnrefineReceiver("allcheckers")` had no effect

`framework/src/main/java/org/checkerframework/framework/type/AnnotatedTypeFactory.java`
(`hasDoesNotUnrefineReceiver`) and the test
`checker/tests/tainting/TestDoesNotUnrefine.java`, in commit `32222adba4`
("Code improvements").

`DoesNotUnrefineReceiver`'s Javadoc has said 'Use "allcheckers" to affect all
checkers' since the annotation was introduced in version 4.1.0, but
`hasDoesNotUnrefineReceiver` compared the annotation's value only against the
checker's own `SuppressWarnings` prefixes.  The change makes the implementation
match the documentation, and adds a test.

This is a bug fix in a released feature.  Per `docs/CHANGELOG.md` conventions,
an ordinary bug fix needs no changelog entry.

### 3. Two pairs of jtreg tests shared a class name

`checker/jtreg/index/UnneededSuppressionsTest.{java,goal}` renamed to
`UnneededSuppressionsIndexTest.{java,goal}`, and
`checker/jtreg/nullness/UnneededSuppressionsTest.{java,goal}` renamed to
`UnneededSuppressionsNullnessTest.{java,goal}`.  Commit `06f592b5bb` ("Give
different files different names") did the rename, and commit `70f6ae6769`
renamed the goal files from `.out` to `.goal`.

Only the class name, the file names, and the `@compile/ref=` directive change.

### 4. Grammar fix in three test files' comments

"This class should not issues any errors" to "should not issue", in
`checker/tests/index/MethodOverrides.java`,
`checker/tests/value-index-interaction/MethodOverrides.java`, and
`checker/tests/value-index-interaction/MethodOverrides3.java`.  Commit
`8378471055` ("Grammar fix").

### 5. Inlined a return variable in `ValueQualifierHierarchy.widenedRange`

`framework/src/main/java/org/checkerframework/common/value/ValueQualifierHierarchy.java`:
`Range result = Range.create(min, max); return result;` became `return
Range.create(min, max);`.  Commit `4bbd6c48be` ("Add nullness annotations").

## Procedure

Do not rewrite the history of the `canonical-annotation` branch: do not rebase
it, do not amend its commits, and do not force-push it.  The steps below create
the new pull request from a fresh working copy, then remove the unrelated hunks
from `canonical-annotation` with an ordinary new commit.

Commit `32222adba4` mixes change 1 and change 2 with nothing else, but changes
3, 4, and 5 are interleaved with related work in their commits, so cherry-picking
is not enough by itself.  Apply the hunks by hand instead.

### Step 1: create the new branch in a new working copy

Because the repository at
`/home/mernst/research/types/checker-framework-fork-mernst-branch-canonical-annotation`
has `canonical-annotation` checked out, and an existing working copy's branch
must not be switched, make a new clone or a new `git worktree` for the new
branch.  For example:

```
git clone git@github.com:mernst/checker-framework.git \
    /home/mernst/research/types/checker-framework-branch-unrelated-cleanups
cd /home/mernst/research/types/checker-framework-branch-unrelated-cleanups
git switch -c unrelated-cleanups origin/master
```

### Step 2: apply the five changes to the new branch

Changes 1 and 2 are the whole of commit `32222adba4`, so cherry-pick it:

```
git cherry-pick 32222adba4
```

(If the commit is not reachable from the new clone, fetch the
`canonical-annotation` branch first, or copy the three files' hunks by hand.)

Change 4 is the whole of commit `8378471055`, so cherry-pick it too.

For changes 3 and 5, copy the hunks by hand from the `canonical-annotation`
working copy; `git diff origin/master...canonical-annotation -- <path>` prints
each one.  Change 3 is a rename plus the corresponding edit of the class name
and the `@compile/ref=` directive in each of the two `.java` files.

Split the result into one commit per change, so that each is reviewable on its
own:

* "Do not dereference a null tree when building an error message"
* "`@DoesNotUnrefineReceiver(\"allcheckers\")` affects all checkers"
* "Give the two `UnneededSuppressionsTest` jtreg tests different names"
* "Grammar fixes in test file comments"
* "Inline a return variable"

### Step 3: verify the new branch

```
./gradlew :framework:test :checker:test
./gradlew :checker:jtregTests
```

The renamed jtreg tests must still produce their `.goal` files' contents.

### Step 4: remove the changes from `canonical-annotation`

In the `canonical-annotation` working copy, revert the five changes with a new
commit (not by rewriting history):

```
cd /home/mernst/research/types/checker-framework-fork-mernst-branch-canonical-annotation
# Undo each of the five changes in the working tree, then:
git commit -m "Move unrelated changes to the unrelated-cleanups branch"
```

Afterwards, `git diff origin/master...canonical-annotation --stat` should list
only these files:

* `checker-qual/src/main/java/org/checkerframework/common/value/qual/IntRangeFrom*.java`
* `checker/src/main/java/org/checkerframework/checker/nullness/NullnessAnnotatedTypeFactory.java`
* `checker/src/main/java/org/checkerframework/checker/units/UnitsAnnotatedTypeFactory.java`
* `checker/src/main/java/org/checkerframework/checker/units/UnitsAnnotationClassLoader.java`
* `docs/CHANGELOG.md`
* `framework/src/main/java/org/checkerframework/common/basetype/BaseTypeVisitor.java`
* `framework/src/main/java/org/checkerframework/common/value/ValueAnnotatedTypeFactory.java`
* `framework/src/main/java/org/checkerframework/common/value/ValueQualifierHierarchy.java`
* `framework/src/main/java/org/checkerframework/common/wholeprograminference/WholeProgramInferenceScenesStorage.java`
* `framework/src/main/java/org/checkerframework/framework/flow/CFAbstractAnalysis.java`
* `framework/src/main/java/org/checkerframework/framework/flow/CFAbstractValue.java`
* `framework/src/main/java/org/checkerframework/framework/type/AnnotatedTypeFactory.java`
* `framework/src/main/java/org/checkerframework/framework/type/AnnotatedTypeMirror.java`
* `framework/src/main/java/org/checkerframework/framework/type/DefaultTypeHierarchy.java`
* `framework/src/main/java/org/checkerframework/framework/type/QualifierHierarchy.java`
* `framework/src/main/java/org/checkerframework/framework/util/Contract.java`
* `framework/src/main/java/org/checkerframework/framework/util/ContractsFromMethod.java`
* `framework/src/main/java/org/checkerframework/framework/util/defaults/QualifierDefaults.java`
* `framework/src/main/java/org/checkerframework/framework/util/element/TypeParamElementAnnotationApplier.java`
* `framework/src/test/java/org/checkerframework/common/wholeprograminference/WholeProgramInferenceScenesStorageTest.java`
* `framework/tests/value/MinLenCanonicalization.java`

### Step 5: order the two pull requests

The two branches touch `AnnotatedTypeFactory.java`, so merge the
`unrelated-cleanups` pull request first, then merge `master` into
`canonical-annotation` and resolve the (small) conflict there.
