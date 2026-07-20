import java.util.ArrayList;
import java.util.List;
import org.checkerframework.dataflow.qual.SideEffectsOnly;

// A call to a method that is annotated `@SideEffectsOnly` side-effects what the annotation says
// (view-adapted to the call site), not the call's receiver and arguments.
public class AnnotatedCallee {

  List<Integer> field = new ArrayList<>();

  @SideEffectsOnly("this")
  void modifiesReceiver(List<Integer> arg1, List<Integer> arg2) {
    field.add(1);
  }

  @SideEffectsOnly("#2")
  void modifiesSecondArgument(List<Integer> arg1, List<Integer> arg2) {
    arg2.add(1);
  }

  @SideEffectsOnly("this")
  void callerModifiesThis(List<Integer> a, List<Integer> b) {
    // `modifiesReceiver` modifies only its receiver, which is `this`, so the arguments are not
    // modified.
    modifiesReceiver(a, b);
  }

  @SideEffectsOnly("#2")
  void callerModifiesSecondArgument(List<Integer> a, List<Integer> b) {
    // The annotation `@SideEffectsOnly("#2")` view-adapts to `b`, which is permitted.
    modifiesSecondArgument(a, b);
  }

  @SideEffectsOnly("#1")
  void callerModifiesFirstArgument(List<Integer> a, List<Integer> b) {
    // The annotation `@SideEffectsOnly("#2")` view-adapts to `b`, which is not permitted.
    // :: error: (purity.incorrect.sideeffectsonly)
    modifiesSecondArgument(a, b);
  }

  @SideEffectsOnly("#1")
  void callerDoesNotPermitReceiver(List<Integer> a, List<Integer> b) {
    // `modifiesReceiver` modifies `this`, which is not permitted.
    // :: error: (purity.incorrect.sideeffectsonly)
    modifiesReceiver(a, b);
  }
}
