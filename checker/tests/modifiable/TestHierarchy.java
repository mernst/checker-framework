import org.checkerframework.checker.modifiable.qual.Modifiable;
import org.checkerframework.checker.modifiable.qual.Unmodifiable;

class TestHierarchy {
  void testHierarchy(@Unmodifiable int unmod, @Modifiable int mod) {
    @Unmodifiable int a = unmod;
    @Unmodifiable int b = mod;
    // :: error: (assignment)
    @Modifiable int c = unmod; // expected error on this line
    @Modifiable int d = mod;
  }
}
