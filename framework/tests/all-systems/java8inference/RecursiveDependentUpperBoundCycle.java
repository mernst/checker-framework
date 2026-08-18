// Test case for a recursive call to a method whose type parameters have dependent upper
// bounds, as in DependentUpperBoundCycle.  Because the call is within the method's own body,
// the type parameters are in scope at the call site, so resolution of the inference variables
// yields capture variables rather than an unsubstituted type variable.

public class RecursiveDependentUpperBoundCycle {

  static <T extends S, S extends Comparable<T>> T comparableCycle() {
    comparableCycle();
    throw new AssertionError();
  }
}
