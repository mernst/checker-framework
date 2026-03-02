import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.checkerframework.checker.modifiability.qual.Growable;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.Replaceable;
import org.checkerframework.checker.modifiability.qual.Shrinkable;
import org.checkerframework.checker.modifiability.qual.UnknownGrow;
import org.checkerframework.checker.modifiability.qual.UnknownReplace;
import org.checkerframework.checker.modifiability.qual.UnknownShrink;

/**
 * Tests structural capability removal for Set, Queue, Map.Entry, and Iterator.
 *
 * <p>Set / Queue: TypeAnnotator removes the Replace capability (sets Replace to @UnknownReplace).
 * Map.Entry: TypeAnnotator removes Grow and Shrink capabilities. Iterator: TypeAnnotator removes
 * Grow and Replace capabilities.
 */
class SetTest {

  // ==========================================================
  // Set: Replace capability is always removed by the TypeAnnotator.
  // Effective types have Replace = @UnknownReplace.
  // ==========================================================

  void testSetReplaceRemoved(
      @Replaceable Set<String> replaceable,
      @Growable @Replaceable Set<String> growReplace,
      @Shrinkable @Replaceable Set<String> shrinkReplace,
      @Growable @Shrinkable @Replaceable Set<String> modifiable) {

    // After TypeAnnotator, all Set Replace qualifiers become @UnknownReplace.
    // So @Replaceable Set, @Growable @Replaceable Set, etc. lose their Replaceable.

    // @Replaceable Set → Grow=Unknown, Shrink=Unknown, Replace=Unknown (R removed)
    // @Growable @Replaceable Set → Grow=Growable, Shrink=Unknown, Replace=Unknown (R removed)
    @Growable Set<String> g1 = growReplace; // OK: Grow still intact
    @Replaceable Set<String> r1 =
        growReplace; // OK: @replaceable set becomes @UnknownMod, GrowReplace set becomes @Growable.

    // @Growable @Shrinkable @Replaceable Set → Grow=G, Shrink=S, Replace=Unknown (R removed)
    // effectively the same as @Growable @Shrinkable Set
    @Growable @Shrinkable Set<String> gs1 = modifiable; // OK
    @Replaceable Set<String> r2 =
        modifiable; // OK: replaceale becomes unknown, modifiable becomes @Growable @Shrinkable
  }

  void testSetGrowShrinkPreserved(
      @UnknownGrow @UnknownShrink @UnknownReplace Set<String> unknown,
      @Growable Set<String> growable,
      @Shrinkable Set<String> shrinkable,
      @Growable @Shrinkable Set<String> gs) {

    // Top in all hierarchies: accepts everything
    @UnknownGrow @UnknownShrink @UnknownReplace Set<String> u1 = growable;
    @UnknownGrow @UnknownShrink @UnknownReplace Set<String> u2 = shrinkable;
    @UnknownGrow @UnknownShrink @UnknownReplace Set<String> u3 = gs;

    // @Growable Set: Grow=G, Shrink=Unknown, Replace=Unknown
    @Growable Set<String> g1 = gs; // OK: G+S <: G
    // :: error: [assignment]
    @Growable Set<String> g2 = shrinkable; // Error: S only, no G

    // @Shrinkable Set: Grow=Unknown, Shrink=S, Replace=Unknown
    @Shrinkable Set<String> s1 = gs; // OK: G+S <: S
    // :: error: [assignment]
    @Shrinkable Set<String> s2 = growable; // Error: G only, no S
  }

  void testQueueLikeSet(
      @Modifiable Queue<String> modifiable, @Growable @Shrinkable Queue<String> gs) {
    // Queue also has Replace removed (same rule as Set).
    @Growable @Shrinkable Queue<String> q1 = modifiable; // OK: modifiable <: gs (R removed from both)
    @Growable @Shrinkable @Replaceable Queue<String> q2 = gs; // OK: same after TypeAnnotator
  }

  // ==========================================================
  // Map.Entry: Grow and Shrink capabilities are both removed.
  // Effective types only retain the Replace hierarchy annotation.
  // ==========================================================

  void testEntryAssignment(
      // No Replace bit: effective Replace = @UnknownReplace after G+S removal
      Map.@UnknownGrow @UnknownShrink @UnknownReplace Entry<String, String> unknown,
      Map.@Growable Entry<String, String> growable,
      Map.@Shrinkable Entry<String, String> shrinkable,
      Map.@Growable @Shrinkable Entry<String, String> gs,
      // Has Replace bit: retains @Replaceable after G+S removal
      Map.@Replaceable Entry<String, String> replaceable,
      Map.@Growable @Replaceable Entry<String, String> growReplace,
      Map.@Shrinkable @Replaceable Entry<String, String> shrinkReplace,
      Map.@Growable @Shrinkable @Replaceable Entry<String, String> modifiable) {

    // Group 1: entries without Replace bit become @UnknownGrow @UnknownShrink @UnknownReplace
    // (G and S are removed → both become Unknown; R was already Unknown)
    Map.@UnknownGrow @UnknownShrink @UnknownReplace Entry<String, String> u1 = growable;
    Map.@UnknownGrow @UnknownShrink @UnknownReplace Entry<String, String> u2 = shrinkable;
    Map.@UnknownGrow @UnknownShrink @UnknownReplace Entry<String, String> u3 = gs;
    // After TypeAnnotator they are all effectively @UnknownGrow @UnknownShrink @UnknownReplace,
    // so assignments between them are valid.
    Map.@Growable Entry<String, String> g1 = unknown; // OK: same effective type
    Map.@Shrinkable Entry<String, String> s1 = unknown;
    Map.@Growable Entry<String, String> g2 = gs; // OK

    // Group 2: entries with Replace bit become @UnknownGrow @UnknownShrink @Replaceable
    // (G and S are removed; R=Replaceable is preserved)
    Map.@UnknownGrow @UnknownShrink @Replaceable Entry<String, String> r1 = growReplace;
    Map.@UnknownGrow @UnknownShrink @Replaceable Entry<String, String> r2 = shrinkReplace;
    Map.@UnknownGrow @UnknownShrink @Replaceable Entry<String, String> r3 = modifiable;
    // After TypeAnnotator they are all effectively @UnknownGrow @UnknownShrink @Replaceable.
    Map.@Growable @Replaceable Entry<String, String> gr1 = replaceable; // OK: same effective type
    Map.@Shrinkable @Replaceable Entry<String, String> sr1 = replaceable;
    Map.@Growable @Shrinkable @Replaceable Entry<String, String> m1 = replaceable;
    Map.@Shrinkable @Replaceable Entry<String, String> sr2 = growReplace;
    Map.@Growable @Replaceable Entry<String, String> gr2 = modifiable;

    // Cross-group: Replace !<: UnknownReplace (wrong direction) is NOT an error —
    // but UnknownReplace !<: Replaceable IS an error:
    // :: error: [assignment]
    Map.@Replaceable Entry<String, String> bad1 = growable; // Error: R=Unknown !<: Replaceable
  }

  // ==========================================================
  // Iterator: Grow and Replace capabilities are both removed.
  // Effective types only retain the Shrink hierarchy annotation.
  // ==========================================================

  void testIteratorAssignment(
      // No Shrink bit: effective after G+R removal → @UnknownGrow @UnknownShrink @UnknownReplace
      @UnknownGrow @UnknownShrink @UnknownReplace Iterator<String> unknown,
      @Growable Iterator<String> growable,
      @Replaceable Iterator<String> replaceable,
      @Growable @Replaceable Iterator<String> growReplace,
      // Has Shrink bit: effective after G+R removal → @UnknownGrow @Shrinkable @UnknownReplace
      @Shrinkable Iterator<String> shrinkable,
      @Growable @Shrinkable Iterator<String> growShrink,
      @Shrinkable @Replaceable Iterator<String> shrinkReplace,
      @Growable @Shrinkable @Replaceable Iterator<String> modifiable) {

    // Group 1: No Shrink bit → effective @UnknownGrow @UnknownShrink @UnknownReplace
    @UnknownGrow @UnknownShrink @UnknownReplace Iterator<String> u1 = growable;
    @UnknownGrow @UnknownShrink @UnknownReplace Iterator<String> u2 = replaceable;
    @UnknownGrow @UnknownShrink @UnknownReplace Iterator<String> u3 = growReplace;
    // They are all effectively the same type after TypeAnnotator:
    @Growable Iterator<String> g1 = unknown;
    @Replaceable Iterator<String> r1 = growReplace;
    @Growable @Replaceable Iterator<String> gr1 = replaceable;

    // Group 2: Has Shrink bit → effective @UnknownGrow @Shrinkable @UnknownReplace
    @UnknownGrow @Shrinkable @UnknownReplace Iterator<String> s1 = shrinkable;
    @UnknownGrow @Shrinkable @UnknownReplace Iterator<String> s2 = growShrink;
    @UnknownGrow @Shrinkable @UnknownReplace Iterator<String> s3 = shrinkReplace;
    @UnknownGrow @Shrinkable @UnknownReplace Iterator<String> s4 = modifiable;
    // Cross-assignments within group 2:
    @Shrinkable Iterator<String> sh1 = modifiable;
    @Growable @Shrinkable Iterator<String> gs1 = modifiable;
    @Shrinkable @Replaceable Iterator<String> sr1 = modifiable;
    @Growable @Shrinkable Iterator<String> gs2 = shrinkable;
    @Shrinkable @Replaceable Iterator<String> sr2 = growShrink;

    // :: error: [assignment]
    @Shrinkable Iterator<String> bad1 = growable; // Error: Shrink=Unknown !<: Shrinkable
  }
}
