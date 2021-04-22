import java.util.Collection;
import java.util.Iterator;
import org.checkerframework.checker.enhancedfor.qual.EnhancedForBottom;
import org.checkerframework.checker.enhancedfor.qual.EnhancedForForbidden;
import org.checkerframework.checker.enhancedfor.qual.EnhancedForOk;
import org.checkerframework.checker.enhancedfor.qual.EnhancedForUnknown;

public class EnhancedForDefaultTest {

  void testDefaults(Collection<String> c, Iterator<String> itor, Iterable<String> ible) {

    @EnhancedForUnknown Collection<String> uc = c;
    @EnhancedForOk Collection<String> oc = c;
    // :: error: (assignment.type.incompatible)
    @EnhancedForForbidden Collection<String> fc = c;
    // :: error: (assignment.type.incompatible)
    @EnhancedForBottom Collection<String> bc = c;

    @EnhancedForUnknown Iterator<String> uitor = itor;
    @EnhancedForOk Iterator<String> oitor = itor;
    // :: error: (assignment.type.incompatible)
    @EnhancedForForbidden Iterator<String> fitor = itor;
    // :: error: (assignment.type.incompatible)
    @EnhancedForBottom Iterator<String> bitor = itor;

    @EnhancedForUnknown Iterable<String> uible = ible;
    @EnhancedForOk Iterable<String> oible = ible;
    // :: error: (assignment.type.incompatible)
    @EnhancedForForbidden Iterable<String> fible = ible;
    // :: error: (assignment.type.incompatible)
    @EnhancedForBottom Iterable<String> bible = ible;

    // :: error: (anno.on.irrelevant)
    @EnhancedForUnknown String us;

    // :: error: (anno.on.irrelevant)
    @EnhancedForUnknown int ui;
  }
}
