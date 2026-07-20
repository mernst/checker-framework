// A `@SideEffectsOnly` expression that cannot be parsed at a call site is reported there.  This
// happens when the annotation is not on a method that is being compiled, so the declaration-site
// check never runs; here the annotation comes from the stub file unparseable-use-site.astub.

import java.util.StringJoiner;
import org.checkerframework.dataflow.qual.SideEffectsOnly;

public class UnparseableAtUseSite {

  @SideEffectsOnly("#1")
  void m(StringJoiner sj) {
    // :: error: (flowexpr.parse.error)
    sj.add("x");
  }
}
