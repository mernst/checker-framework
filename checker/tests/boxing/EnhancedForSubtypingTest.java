import java.util.Collection;
import org.checkerframework.checker.boxing.qual.CollectionRepBottom;
import org.checkerframework.checker.boxing.qual.PrimCollection;
import org.checkerframework.checker.boxing.qual.RefCollection;
import org.checkerframework.checker.boxing.qual.UnknownRepCollection;

public class EnhancedForSubtypingTest {

  void testHierarchy(
      @UnknownRepCollection Collection<String> u,
      @RefCollection Collection<String> o,
      @PrimCollection Collection<String> f,
      @CollectionRepBottom Collection<String> b) {

    @UnknownRepCollection Collection<String> u1 = u;
    @UnknownRepCollection Collection<String> u2 = o;
    @UnknownRepCollection Collection<String> u3 = f;
    @UnknownRepCollection Collection<String> u4 = b;

    // :: error: (assignment)
    @RefCollection Collection<String> o1 = u;
    @RefCollection Collection<String> o2 = o;
    // :: error: (assignment)
    @RefCollection Collection<String> o3 = f;
    @RefCollection Collection<String> o4 = b;

    // :: error: (assignment)
    @PrimCollection Collection<String> f1 = u;
    // :: error: (assignment)
    @PrimCollection Collection<String> f2 = o;
    @PrimCollection Collection<String> f3 = f;
    @PrimCollection Collection<String> f4 = b;

    // :: error: (assignment)
    @CollectionRepBottom Collection<String> b1 = u;
    // :: error: (assignment)
    @CollectionRepBottom Collection<String> b2 = o;
    // :: error: (assignment)
    @CollectionRepBottom Collection<String> b3 = f;
    @CollectionRepBottom Collection<String> b4 = b;
  }
}
