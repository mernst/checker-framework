// The alias analysis that checks `@SideEffectsOnly` is a may-alias approximation: it is
// flow-insensitive and it never splits an alias set.  Its errors are therefore false negatives:
// some side effects that are not permitted by the annotation are accepted.

import java.util.List;
import org.checkerframework.dataflow.qual.SideEffectsOnly;

public class AliasingLimitation {

  // Correct behavior: mutating an expression that may be aliased to a listed one is permitted.
  @SideEffectsOnly("#1")
  void aliasIsPermitted(List<String> a) {
    List<String> t = a;
    t.add("x");
  }

  // Known limitation: `t` is aliased to `a` and later reassigned to `b`, but the alias sets are
  // merged rather than replaced, so `t` remains in `a`'s alias set.  Mutating `t` after the
  // reassignment mutates `b`, which the annotation does not permit, but no error is issued.
  @SideEffectsOnly("#1")
  void reassignmentIsNotTracked(List<String> a, List<String> b) {
    List<String> t = a;
    t = b;
    // The following line should be an error, but the checker does not report it.
    t.add("x");
  }

  // The merging is transitive, so it also makes `b` itself appear to be permitted.
  @SideEffectsOnly("#1")
  void mergingIsTransitive(List<String> a, List<String> b) {
    List<String> t = a;
    t = b;
    // The following line should be an error, but the checker does not report it.
    b.add("x");
  }

  // Without an assignment relating them, two expressions are not treated as aliases, even though
  // they might be aliased at run time.
  @SideEffectsOnly("#1")
  void unrelatedExpressionsAreNotAliases(List<String> a, List<String> b) {
    // :: error: (purity.incorrect.sideeffectsonly)
    b.add("x");
  }
}
