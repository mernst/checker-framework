import java.util.Collection;
import java.util.Iterator;
import org.checkerframework.checker.boxing.qual.CollectionRepBottom;
import org.checkerframework.checker.boxing.qual.PrimCollection;
import org.checkerframework.checker.boxing.qual.RefCollection;
import org.checkerframework.checker.boxing.qual.UnknownRepCollection;

public class EnhancedForDefaultTest {

  void testDefaults(Collection<String> c, Iterator<String> itor, Iterable<String> ible) {

    @UnknownRepCollection Collection<String> uc = c;
    @RefCollection Collection<String> oc = c;
    // :: error: (assignment.type.incompatible)
    @PrimCollection Collection<String> fc = c;
    // :: error: (assignment.type.incompatible)
    @CollectionRepBottom Collection<String> bc = c;

    @UnknownRepCollection Iterator<String> uitor = itor;
    @RefCollection Iterator<String> oitor = itor;
    // :: error: (assignment.type.incompatible)
    @PrimCollection Iterator<String> fitor = itor;
    // :: error: (assignment.type.incompatible)
    @CollectionRepBottom Iterator<String> bitor = itor;

    @UnknownRepCollection Iterable<String> uible = ible;
    @RefCollection Iterable<String> oible = ible;
    // :: error: (assignment.type.incompatible)
    @PrimCollection Iterable<String> fible = ible;
    // :: error: (assignment.type.incompatible)
    @CollectionRepBottom Iterable<String> bible = ible;

    // :: error: (anno.on.irrelevant)
    @UnknownRepCollection String us;

    // :: error: (anno.on.irrelevant)
    @UnknownRepCollection int ui;
  }
}
