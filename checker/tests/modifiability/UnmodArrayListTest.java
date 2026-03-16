import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.checkerframework.checker.modifiability.qual.Unmodifiable;

/**
 * Demonstrates a known soundness hole: casting an @Unmodifiable list to a concrete @Modifiable
 * class (ArrayList) bypasses the checker's protection.
 *
 * <p>The checker sees ArrayList as @Modifiable by default, so the downcast is treated as "safe" and
 * the subsequent add() call raises no warning — even though the underlying object is an
 * unmodifiable wrapper that will throw UnsupportedOperationException at runtime.
 */
public class UnmodArrayListTest {

  void testUnmodifiableCastEscape() {
    ArrayList<String> myList = new ArrayList<>();
    myList.add("hello");

    // Checker correctly infers @Unmodifiable here.
    @Unmodifiable List<String> view = Collections.unmodifiableList(myList);

    // Because ArrayList is treated as @Modifiable by the checker, this downcast is considered
    // "safe" — no cast.unsafe warning is issued, even though the underlying object is an
    // unmodifiable wrapper and will throw ClassCastException at runtime.
    ArrayList<String> backToMod = (ArrayList<String>) view;

    // The checker allows this because backToMod is typed as @Modifiable ArrayList.
    // :: error: [method.invocation]
    backToMod.add("Boom");
  }
}
