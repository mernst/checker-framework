import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.modifiability.qual.GrowReplace;
import org.checkerframework.checker.modifiability.qual.GrowShrink;
import org.checkerframework.checker.modifiability.qual.Growable;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.Replaceable;
import org.checkerframework.checker.modifiability.qual.ShrinkReplace;
import org.checkerframework.checker.modifiability.qual.Shrinkable;
import org.checkerframework.checker.modifiability.qual.UnknownModifiability;

class SetTest {
  // ==========================================================
  // Set: No Replace methods. Implicitly @Replaceable.
  // ==========================================================

  void testSetAssignment(
      @UnknownModifiability Set<String> unknown,
      @Replaceable Set<String> replaceable,
      @Shrinkable Set<String> shrinkable,
      @Modifiable Set<String> modifiable,
      @Growable Set<String> growable,
      @GrowReplace Set<String> growReplace,
      @ShrinkReplace Set<String> shrinkReplace,
      @GrowShrink Set<String> growShrink) {

    // Unknown <-> Replaceable
    @Replaceable Set<String> s1 = unknown; // Valid: Unknown -> Replaceable (Implicit R)
    @UnknownModifiability Set<String> s2 = replaceable; // Valid: Replaceable <: Unknown

    // Shrinkable <-> ShrinkReplace
    @ShrinkReplace Set<String> s3 = shrinkable; // Valid: Shrinkable -> ShrinkReplace (Implicit R)
    @Shrinkable Set<String> s4 = shrinkReplace; // Valid: ShrinkReplace <: Shrinkable

    // GrowShrink <-> Modifiable
    @Modifiable Set<String> s5 = growShrink; // Valid: GrowShrink -> Modifiable (Implicit R)
    @GrowShrink Set<String> s6 = modifiable; // Valid: Modifiable <: GrowShrink

    // Growable <-> GrowReplace
    @GrowReplace Set<String> s7 = growable; // Valid: Growable -> GrowReplace (Implicit R)
    @Growable Set<String> s8 = growReplace; // Valid: GrowReplace <: Growable
  }

  // ==========================================================
  // Map.Entry: Drops Grow (100) and Shrink (010) capabilities.
  // Effectively supports only Replace (001).
  // ==========================================================

  void testEntryAssignment(
      // Group 1: No Replace Bit (Result: @UnknownModifiability)
      Map.@UnknownModifiability Entry<String, String> unknown,
      Map.@Growable Entry<String, String> growable,
      Map.@Shrinkable Entry<String, String> shrinkable,
      Map.@GrowShrink Entry<String, String> growShrink,

      // Group 2: Has Replace Bit (Result: @Replaceable)
      Map.@Replaceable Entry<String, String> replaceable,
      Map.@GrowReplace Entry<String, String> growReplace,
      Map.@ShrinkReplace Entry<String, String> shrinkReplace,
      Map.@Modifiable Entry<String, String> modifiable) {

    // Group 1: Unknown = Growable = Shrinkable = GrowShrink ->
    // @UnknownModifiability
    // These types lose G and S capabilities, degrading to Unknown (000).

    Map.@UnknownModifiability Entry<String, String> u1 = growable;
    Map.@UnknownModifiability Entry<String, String> u2 = shrinkable;
    Map.@UnknownModifiability Entry<String, String> u3 = growShrink;

    // Since they are all @Unknown (000), they can be assigned to each other
    Map.@Growable Entry<String, String> g1 = unknown;
    Map.@Shrinkable Entry<String, String> s1 = unknown;
    Map.@GrowShrink Entry<String, String> gs1 = unknown;
    Map.@Growable Entry<String, String> g2 = growShrink;

    // Group 2: Replaceable = GrowReplace = ShrinkReplace = Modifiable ->
    // @Replaceable
    // These types retain R (001) but lose G and S, degrading to Replaceable.

    Map.@Replaceable Entry<String, String> r1 = growReplace;
    Map.@Replaceable Entry<String, String> r2 = shrinkReplace;
    Map.@Replaceable Entry<String, String> r3 = modifiable;

    // Since they are all @Replaceable (001), they can be assigned to each other
    Map.@GrowReplace Entry<String, String> gr1 = replaceable;
    Map.@ShrinkReplace Entry<String, String> sr1 = replaceable;
    Map.@Modifiable Entry<String, String> m1 = replaceable;

    Map.@ShrinkReplace Entry<String, String> sr2 = growReplace;
    Map.@GrowReplace Entry<String, String> gr2 = modifiable;
  }

  // ==========================================================
  // Iterator: No Grow/Replace methods. Implicitly @GrowReplace.
  // ==========================================================

  void testIteratorAssignment(
      @UnknownModifiability Iterator<String> unknown,
      @Growable Iterator<String> growable,
      @Replaceable Iterator<String> replaceable,
      @GrowReplace Iterator<String> growReplace,
      @Shrinkable Iterator<String> shrinkable,
      @GrowShrink Iterator<String> growShrink,
      @ShrinkReplace Iterator<String> shrinkReplace,
      @Modifiable Iterator<String> modifiable) {

    // Group 1: Unknown = Growable = Replaceable = GrowReplace
    // All these imply @GrowReplace (1?1), so they are effectively equal and
    // assignable.

    @GrowReplace Iterator<String> gr1 = unknown;
    @GrowReplace Iterator<String> gr2 = growable;
    @GrowReplace Iterator<String> gr3 = replaceable;
    @GrowReplace Iterator<String> gr4 = growReplace;

    @UnknownModifiability Iterator<String> u1 = growReplace;
    @Growable Iterator<String> g1 = growReplace;
    @Replaceable Iterator<String> r1 = growReplace;

    @Growable Iterator<String> g2 = unknown;
    @Replaceable Iterator<String> r2 = growable;

    // Group 2: Shrinkable = GrowShrink = ShrinkReplace = Modifiable
    // All these imply @Modifiable (111) because they have the S bit,
    // and Iterator adds G and R bits. (010 + 101 = 111).

    @Modifiable Iterator<String> m1 = shrinkable;
    @Modifiable Iterator<String> m2 = growShrink;
    @Modifiable Iterator<String> m3 = shrinkReplace;
    @Modifiable Iterator<String> m4 = modifiable;

    @Shrinkable Iterator<String> s1 = modifiable;
    @GrowShrink Iterator<String> gs1 = modifiable;
    @ShrinkReplace Iterator<String> sr1 = modifiable;

    @GrowShrink Iterator<String> gs2 = shrinkable;
    @ShrinkReplace Iterator<String> sr2 = growShrink;
  }
}
