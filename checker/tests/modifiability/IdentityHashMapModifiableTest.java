import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import org.checkerframework.checker.modifiability.qual.Modifiable;

public class IdentityHashMapModifiableTest {

  void testIdentityHashMap() {
    @Modifiable IdentityHashMap<String, String> identityMap = new IdentityHashMap<>();
    identityMap.put("key", "value1");
    identityMap.remove("key");

    // IdentityHashMap itself is mutable
    boolean removed = identityMap.remove("key", "value2");
  }

  // this type rule is not implemented yet
  // void testViews() {
  //     @Modifiable IdentityHashMap<String, String> identityMap = new IdentityHashMap<>();

  //     // Views (keySet, values, entrySet) usually throw UOE on add operations
  //     // :: error: (method.invocation)
  //     identityMap.keySet().add("newKey");

  //     // :: error: (method.invocation)
  //     identityMap.values().add("newValue");

  //     // :: error: (method.invocation)
  //     identityMap.entrySet().add(new AbstractMap.SimpleEntry<>("newKey", "newValue"));

  //     // However, removal is supported, so these should be allowed:
  //     identityMap.keySet().remove("key");
  //     identityMap.values().remove("value");
  //     identityMap.entrySet().remove(new AbstractMap.SimpleEntry<>("key", "value"));
  // }

  void testEntries() {
    @Modifiable IdentityHashMap<String, String> identityMap = new IdentityHashMap<>();
    identityMap.put("k", "v");

    // Iterator returns mutable entries
    Iterator<Map.Entry<String, String>> it = identityMap.entrySet().iterator();
    if (it.hasNext()) {
      Map.Entry<String, String> entry = it.next();
      // the following method is allowed and works at runtime, but the current type system throws
      // an error because we could only make entrySet.iterator() and entrySet.stream() return the
      // same mutability.
      // :: error: (method.invocation)
      entry.setValue("modified"); // OK
    }

    // Stream/Spliterator returns immutable entries
    if (!identityMap.isEmpty()) {
      Map.Entry<String, String> entry = identityMap.entrySet().stream().findFirst().get();
      // This throws UOE at runtime.
      // :: error: (method.invocation)
      entry.setValue("modified");
    }
  }
}
