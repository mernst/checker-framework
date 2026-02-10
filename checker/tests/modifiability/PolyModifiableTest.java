import java.util.List;
import org.checkerframework.checker.modifiability.qual.GrowReplace;
import org.checkerframework.checker.modifiability.qual.GrowShrink;
import org.checkerframework.checker.modifiability.qual.Growable;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.PolyModifiable;
import org.checkerframework.checker.modifiability.qual.Replaceable;
import org.checkerframework.checker.modifiability.qual.ShrinkReplace;
import org.checkerframework.checker.modifiability.qual.Shrinkable;
import org.checkerframework.checker.modifiability.qual.UnknownModifiability;
import org.checkerframework.checker.modifiability.qual.Unmodifiable;

public class PolyModifiableTest {
  // A simple polymorphic method
  @PolyModifiable
  List<String> identity(@PolyModifiable List<String> x) {
    return x;
  }

  void testPoly(
      @Modifiable List<String> mod,
      @GrowShrink List<String> gs,
      @GrowReplace List<String> gr,
      @ShrinkReplace List<String> sr,
      @Growable List<String> g,
      @Shrinkable List<String> s,
      @Replaceable List<String> r,
      @UnknownModifiability List<String> unknown,
      @Unmodifiable List<String> unmod) {

    // ============================================================
    // Identity on Modifiable (Bottom - 111)
    // ============================================================
    @Modifiable List<String> m1 = identity(mod); // OK
    @UnknownModifiability List<String> m2 = identity(mod); // OK

    // ============================================================
    // Identity on GrowShrink (110)
    // ============================================================
    @GrowShrink List<String> gs1 = identity(gs); // OK
    // :: error: (assignment)
    @Modifiable List<String> gs2 = identity(gs); // Error: GS (110) !<: Mod (111)
    @Growable List<String> gs3 = identity(gs); // OK: GS (110) <: G (100)
    @Shrinkable List<String> gs4 = identity(gs); // OK: GS (110) <: S (010)
    // :: error: (assignment)
    @Replaceable List<String> gs5 = identity(gs); // Error: GS (110) !<: R (001)

    // ============================================================
    // Identity on GrowReplace (101)
    // ============================================================
    @GrowReplace List<String> gr1 = identity(gr); // OK
    // :: error: (assignment)
    @Modifiable List<String> gr2 = identity(gr); // Error
    @Growable List<String> gr3 = identity(gr); // OK
    // :: error: (assignment)
    @Shrinkable List<String> gr4 = identity(gr); // Error
    @Replaceable List<String> gr5 = identity(gr); // OK

    // ============================================================
    // Identity on ShrinkReplace (011)
    // ============================================================
    @ShrinkReplace List<String> sr1 = identity(sr); // OK
    // :: error: (assignment)
    @Modifiable List<String> sr2 = identity(sr); // Error
    // :: error: (assignment)
    @Growable List<String> sr3 = identity(sr); // Error
    @Shrinkable List<String> sr4 = identity(sr); // OK
    @Replaceable List<String> sr5 = identity(sr); // OK

    // ============================================================
    // Identity on Growable (100)
    // ============================================================
    @Growable List<String> g1 = identity(g); // OK
    // :: error: (assignment)
    @GrowShrink List<String> g2 = identity(g); // Error
    // :: error: (assignment)
    @Modifiable List<String> g3 = identity(g); // Error

    // ============================================================
    // Identity on Shrinkable (010)
    // ============================================================
    @Shrinkable List<String> s1 = identity(s); // OK
    // :: error: (assignment)
    @GrowShrink List<String> s2 = identity(s); // Error
    // :: error: (assignment)
    @Modifiable List<String> s3 = identity(s); // Error
    // ============================================================
    // Identity on Replaceable (001)
    // ============================================================
    @Replaceable List<String> r1 = identity(r); // OK
    // :: error: (assignment)
    @GrowReplace List<String> r2 = identity(r); // Error
    // :: error: (assignment)
    @Modifiable List<String> r3 = identity(r); // Error
    // ============================================================
    // Identity on UnknownModifiability (000 / Top)
    // ============================================================
    @UnknownModifiability List<String> u1 = identity(unknown); // OK
    // :: error: (assignment)
    @Modifiable List<String> u2 = identity(unknown); // Error
    // :: error: (assignment)
    @Growable List<String> u3 = identity(unknown); // Error
    // ============================================================
    // Identity on Unmodifiable (000 / Top alias)
    // ============================================================
    @Unmodifiable List<String> unmod1 = identity(unmod); // OK
    // :: error: (assignment)
    @Modifiable List<String> unmod2 = identity(unmod); // Error
  }
}
