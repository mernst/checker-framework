import java.util.ArrayList;
import java.util.List;

// Reproduces a dataflow refinement issue: calling set() on a field initialized with
// new ArrayList<>() invalidates the @Modifiable refinement because set() lacks
// @SideEffectsOnly("this"). After the set() call, the field reverts to its declared
// type (@UnknownModifiability), causing subsequent mutation calls to fail.
public class SetRefinementTest {

  private List<String> items = new ArrayList<>();

  /** Creates a new SetRefinementTest. */
  public SetRefinementTest() {}

  /**
   * Creates a new SetRefinementTest.
   *
   * @param other list to copy from
   */
  public SetRefinementTest(List<String> other) {
    this.items.addAll(other); // this should not be allowed but checker is not checking this
  }

  public SetRefinementTest(List<String> other, int dummy) {
    this.items.set(0, other.get(0)); // this should not be allowed but checker is not checking this
    // :: error: [method.invocation]
    this.items.addAll(other);
  }

  public SetRefinementTest(List<String> other, boolean dummy) {
    this.items.set(0, other.get(0)); // this should not be allowed but checker is not checking this
    // :: error: [method.invocation]
    this.items.set(0, other.get(0));
    // :: error: [method.invocation]
    this.items.addAll(other);
  }

  public void updateItemsLikeConstructor(List<String> other, int dummy) {
    // :: error: [method.invocation]
    this.items.set(0, other.get(0));
    // :: error: [method.invocation]
    this.items.addAll(other);
  }
}
