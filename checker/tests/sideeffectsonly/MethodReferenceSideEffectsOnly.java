// A method reference must respect the `@SideEffectsOnly` annotation on the method of the
// functional interface, just as an overriding method must.  The error key is `purity.methodref`
// rather than `purity.overriding`.

import java.util.Collection;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.SideEffectsOnly;

public class MethodReferenceSideEffectsOnly {

  interface Mutator {
    @SideEffectsOnly("#1")
    void mutate(Collection<Integer> a, Collection<Integer> b);
  }

  @SideEffectsOnly("#1")
  static void mutatesFirst(Collection<Integer> a, Collection<Integer> b) {
    a.add(1);
  }

  @SideEffectsOnly({"#1", "#2"})
  static void mutatesBoth(Collection<Integer> a, Collection<Integer> b) {
    a.add(1);
    b.add(1);
  }

  @SideEffectFree
  static void mutatesNothing(Collection<Integer> a, Collection<Integer> b) {}

  static void unannotated(Collection<Integer> a, Collection<Integer> b) {}

  void m() {
    // The referenced method side-effects the same expression as `mutate` does.
    Mutator ok = MethodReferenceSideEffectsOnly::mutatesFirst;

    // `@SideEffectFree` is a stronger guarantee than any `@SideEffectsOnly`.
    Mutator stronger = MethodReferenceSideEffectsOnly::mutatesNothing;

    // The referenced method side-effects more expressions than `mutate` permits.
    // :: error: (purity.methodref)
    Mutator tooMany = MethodReferenceSideEffectsOnly::mutatesBoth;

    // Unlike an overriding method, a referenced method does not inherit `@SideEffectsOnly`, so an
    // unannotated method may not be used here.
    // :: error: (purity.methodref)
    Mutator none = MethodReferenceSideEffectsOnly::unannotated;
  }
}
