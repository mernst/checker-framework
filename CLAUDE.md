Chapter 36 of the Checker Framework Manual (https://checkerframework.org/manual/#creating-a-checker) explains how to create a new pluggable type-checker.

Create a new checker named the "Modifiable Checker".  Its aim is to warn, at compile time, if a program might throw `UnsupportedOperationException` at run time due to calling a mutating method on an unmodifable list.
You will create the checker step-by-step according to my instructions.  Ask for approval after each step.

An "unmodifiable collection" is one that throws `UnsupportedOperationException` if a methods like `add()` or `remove()` is called.  These methods, and others, return an unmodifiable collection:

* List.of()
* List.copyOf()
* Set.of()
* Set.copyOf()
* Map.of()
* Map.copyOf()
* Collections.emptyList()
* Collections.emptySet()
* Collections.emptyMap()
* Collections.unmodifiableList()
* Collections.unmodifiableSet()
* Collections.unmodifiableMap()

A "modifiable collection" is one on which methods like `add()` and `remove()` can be called without an `UnsupportedOperationException`.

The type qualifiers of the Modifiable Checker are:

* `@Modifiable`, top qualifier, and the default
* `@Unmodifiable`: bottom qualifier

The qualifiers apply to collections.

Step 1: Write the user manual.  The manual explains the type system, what it guarantees, how to use it, etc., from the point of view of a user. Writing the manual will help you flesh out your goals and the concepts, which are easier to understand and change in text than in an implementation. Manual Section 36.13 (https://checkerframework.org/manual/#creating-documenting-a-checker) gives a suggested structure for the manual chapter, which will help you avoid omitting any parts.

Perform step 1 and ask for my approval.

Step 2: Implement the type qualifiers and hierarchy, according to manual Section 36.5 (https://checkerframework.org/manual/#creating-typequals).

Step 3: Write the checker class itself, following Section 36.6 of the manual (https://checkerframework.org/manual/#creating-compiler-interface).

Step 4: Integrate the checker with the Checker Framework’s Gradle targets for testing, following Section 36.11 of the manual:  https://checkerframework.org/manual/#creating-testing-framework.
