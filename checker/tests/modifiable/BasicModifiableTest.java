import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.modifiable.qual.Unmodifiable;

public class BasicModifiableTest {

  void testBasicModifiable() {
    // Modifiable collections should allow mutation (default)
    List<String> modifiableList = new ArrayList<>();
    modifiableList.add("test"); // OK

    // Unmodifiable collections should not allow mutation
    @Unmodifiable List<String> unmodifiableList = List.of("test1", "test2");
    // :: error: (method.invocation)
    unmodifiableList.add("test3"); // Should error
  }

  void testUnmodifiableFactoryMethods() {
    // These should be inferred as @Unmodifiable
    List<String> list1 = List.of("a", "b");
    // :: error: (method.invocation)
    list1.add("c"); // Should error

    List<String> list2 = List.copyOf(new ArrayList<>());
    // :: error: (method.invocation)
    list2.remove(0); // Should error
  }

  void testModifiableFactoryMethods() {
    // This should be inferred as @Modifiable
    List<String> modifiableList = new ArrayList<>();
    modifiableList.add("test"); // Should be OK
    modifiableList.remove(0); // Should be OK
  }
}
