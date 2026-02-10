import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class WillThrowUOETest {

  void testSortedSet(SortedSet<String> s) {
    // :: error: (usage.will.throw.uoe)
    s.addFirst("foo");

    // :: error: (usage.will.throw.uoe)
    s.addLast("bar");
  }

  void testImplementation(TreeSet<String> ts) {
    // Since TreeSet implements SortedSet, it inherits the methods.
    // We verify that calling them on a concrete implementation also triggers the
    // warning.

    // :: error: (usage.will.throw.uoe)
    ts.addFirst("foo");

    // :: error: (usage.will.throw.uoe)
    ts.addLast("bar");
  }

  void testSortedMap(SortedMap<String, String> m) {
    // :: error: (usage.will.throw.uoe)
    m.putFirst("foo", "bar");

    // :: error: (usage.will.throw.uoe)
    m.putLast("baz", "qux");
  }

  void testImplementation(TreeMap<String, String> tm) {
    // :: error: (usage.will.throw.uoe)
    tm.putFirst("foo", "bar");

    // :: error: (usage.will.throw.uoe)
    tm.putLast("baz", "qux");
  }

  void testNavigableMap(NavigableMap<String, String> m) {
    // :: error: (usage.will.throw.uoe)
    m.putFirst("foo", "bar");

    // :: error: (usage.will.throw.uoe)
    m.putLast("baz", "qux");
  }

  void testConcurrentNavigableMap(ConcurrentNavigableMap<String, String> m) {
    // :: error: (usage.will.throw.uoe)
    m.putFirst("foo", "bar");

    // :: error: (usage.will.throw.uoe)
    m.putLast("baz", "qux");
  }

  void testConcurrentSkipListMap(ConcurrentSkipListMap<String, String> m) {
    // :: error: (usage.will.throw.uoe)
    m.putFirst("foo", "bar");

    // :: error: (usage.will.throw.uoe)
    m.putLast("baz", "qux");
  }
}
