import java.util.Collection;
import org.checkerframework.checker.enhancedfor.qual.EnhancedForBottom;
import org.checkerframework.checker.enhancedfor.qual.EnhancedForForbidden;
import org.checkerframework.checker.enhancedfor.qual.EnhancedForOk;
import org.checkerframework.checker.enhancedfor.qual.EnhancedForUnknown;

public class EnhancedForSubtypingTest {

  void testHierarchy(
      @EnhancedForUnknown Collection<String> u,
      @EnhancedForOk Collection<String> o,
      @EnhancedForForbidden Collection<String> f,
      @EnhancedForBottom Collection<String> b) {

    @EnhancedForUnknown Collection<String> u1 = u;
    @EnhancedForUnknown Collection<String> u2 = o;
    @EnhancedForUnknown Collection<String> u3 = f;
    @EnhancedForUnknown Collection<String> u4 = b;

    // :: error: (assignment.type.incompatible)
    @EnhancedForOk Collection<String> o1 = u;
    @EnhancedForOk Collection<String> o2 = o;
    // :: error: (assignment.type.incompatible)
    @EnhancedForOk Collection<String> o3 = f;
    @EnhancedForOk Collection<String> o4 = b;

    // :: error: (assignment.type.incompatible)
    @EnhancedForForbidden Collection<String> f1 = u;
    // :: error: (assignment.type.incompatible)
    @EnhancedForForbidden Collection<String> f2 = o;
    @EnhancedForForbidden Collection<String> f3 = f;
    @EnhancedForForbidden Collection<String> f4 = b;

    // :: error: (assignment.type.incompatible)
    @EnhancedForBottom Collection<String> b1 = u;
    // :: error: (assignment.type.incompatible)
    @EnhancedForBottom Collection<String> b2 = o;
    // :: error: (assignment.type.incompatible)
    @EnhancedForBottom Collection<String> b3 = f;
    @EnhancedForBottom Collection<String> b4 = b;
  }
}
