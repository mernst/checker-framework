# Instructions for Claude Code to create a pluggable type-checker

Chapter 36 of the Checker Framework Manual (<https://checkerframework.org/manual/#creating-a-checker>) explains how to create a new pluggable type-checker.

Create a new checker named the "Modifiable Checker".  Its aim is to warn, at compile time, if a program might throw `UnsupportedOperationException` at run time due to calling a mutating method on an unmodifable list.
You will create the checker step-by-step according to my instructions.  Ask for approval after each step.

An "unmodifiable collection" is one that throws `UnsupportedOperationException` if a mutating method like `add()` or `remove()` is called.
Many methods in the JDK and in user code take a collection as an argument.  Some of those methods likewise fail with `UnsupportedOperationException` if a caller provides an unmodifiable collection.

These methods in the JDK (and others in the JDK and in user code) return an unmodifiable collection:

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
These are examples of methods that may be called on any collection, not only modifiable ones:

* List.contains(Object o)
* List.containsAll(Collection<?> c)
* List.copyOf(Collection<? extends E> coll)
* List.equals(Object o)
* List.get(int index)
* List.getFirst()
* List.getLast()
* List.hashCode()
* List.indexOf(Object o)
* List.isEmpty()
* List.toArray()

The type qualifiers of the Modifiable Checker are:

* `@AnyModifiable`, the top qualifier
* `@Modifiable`: the default, a subtype of `@AnyModifiable`
* `@Unmodifiable`: a subtype of `@AnyModifiable`
* `@BottomModifiable`: the bottom qualifier, a subtype of `@Modifiable` and `@UnModifiable`

The qualifiers apply to collections.  Iterators are considered to be collections.

Step 1: Write the user manual.  The manual explains the type system, what it guarantees, how to use it, etc., from the point of view of a user. Writing the manual will help you flesh out your goals and the concepts, which are easier to understand and change in text than in an implementation. Manual Section 36.13 (<https://checkerframework.org/manual/#creating-documenting-a-checker>) gives a suggested structure for the manual chapter, which will help you avoid omitting any parts.

Perform step 1 and ask for my approval.

Step 2: Implement the type qualifiers and hierarchy, according to manual Section 36.5 (<https://checkerframework.org/manual/#creating-typequals>).

Step 3: Write the checker class itself, following Section 36.6 of the manual (<https://checkerframework.org/manual/#creating-compiler-interface>).

Step 4: Integrate the checker with the Checker Framework’s Gradle targets for testing, following Section 36.11 of the manual:  <https://checkerframework.org/manual/#creating-testing-framework>.

Step 5: Annotate the JDK as appropriate.
A non-exhaustive list of changes is:

* Add `@Unmodifiable` to result type of factory methods like those listed above as returning an unmodifiable collection.
* Add `@AnyModifiable` to the formal parameter type of functions that take a collection as input and do not change it.
* Add `@AnyModifiable` to the type of the receiver parameter (`this`) of collection methods that do not modify the receiver, like those listed above that may be called on any collection.
* Add `@PolyModifiable` to the return type and receiver type of menthods `iterator()`, `listIterator()`, etc.
