import java.util.List;
import org.checkerframework.checker.modifiable.qual.AnyModifiable;
import org.checkerframework.checker.modifiable.qual.BottomModifiable;
import org.checkerframework.checker.modifiable.qual.Modifiable;
import org.checkerframework.checker.modifiable.qual.Unmodifiable;

class TestHierarchy {
  void testHierarchy(
      @AnyModifiable List<String> anymod,
      @Unmodifiable List<String> unmod,
      @Modifiable List<String> mod,
      @BottomModifiable List<String> botmod) {
    @AnyModifiable List<String> a;
    a = anymod;
    a = unmod;
    a = mod;
    a = botmod;

    @Unmodifiable List<String> b;
    // :: error: (assignment)
    b = anymod;
    b = unmod;
    // :: error: (assignment)
    b = mod;
    b = botmod;

    @Modifiable List<String> c;
    // :: error: (assignment)
    c = anymod;
    // :: error: (assignment)
    c = unmod;
    c = mod;
    c = botmod;

    @BottomModifiable List<String> d;
    // :: error: (assignment)
    d = anymod;
    // :: error: (assignment)
    d = unmod;
    // :: error: (assignment)
    d = mod;
    d = botmod;
  }
}
