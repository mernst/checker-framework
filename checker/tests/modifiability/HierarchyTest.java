import org.checkerframework.checker.modifiability.qual.Growable;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.Replaceable;
import org.checkerframework.checker.modifiability.qual.Shrinkable;
import org.checkerframework.checker.modifiability.qual.UnknownGrow;
import org.checkerframework.checker.modifiability.qual.UnknownModifiability;
import org.checkerframework.checker.modifiability.qual.UnknownReplace;
import org.checkerframework.checker.modifiability.qual.UnknownShrink;
import org.checkerframework.checker.modifiability.qual.Unmodifiable;

/**
 * Tests the three independent 2-element lattices of the Modifiability Checker.
 *
 * <p>Grow hierarchy: UnknownGrow (top/default) ← Growable (bottom) Shrink hierarchy: UnknownShrink
 * (top/default) ← Shrinkable (bottom) Replace hierarchy: UnknownReplace (top/default) ← Replaceable
 * (bottom)
 *
 * <p>An annotated type carries one qualifier per hierarchy. Assignment is valid if and only if the
 * RHS is a subtype in every hierarchy simultaneously.
 */
class HierarchyTest {

  // ============================================================
  // Grow hierarchy
  // ============================================================
  void testGrow(@Growable Object g, @UnknownGrow Object u) {
    // Growable <: UnknownGrow (bottom to top is always OK)
    @UnknownGrow Object u1 = g;
    // :: error: [assignment]
    @Growable Object g1 = u; // UnknownGrow !<: Growable
  }

  // ============================================================
  // Shrink hierarchy
  // ============================================================
  void testShrink(@Shrinkable Object s, @UnknownShrink Object u) {
    @UnknownShrink Object u1 = s;
    // :: error: [assignment]
    @Shrinkable Object s1 = u;
  }

  // ============================================================
  // Replace hierarchy
  // ============================================================
  void testReplace(@Replaceable Object r, @UnknownReplace Object u) {
    @UnknownReplace Object u1 = r;
    // :: error: [assignment]
    @Replaceable Object r1 = u;
  }

  // ============================================================
  // Multi-annotation combinations (one qualifier per hierarchy)
  // ============================================================
  void testCombinations(
      @Growable @Shrinkable @Replaceable Object gsr, // full capabilities
      @Growable @Shrinkable Object gs, // Grow + Shrink only (Replace = UnknownReplace default)
      @Growable @Replaceable Object gr, // Grow + Replace only (Shrink = UnknownShrink default)
      @Shrinkable @Replaceable Object sr, // Shrink + Replace only (Grow = UnknownGrow default)
      @Growable Object g, // Grow only
      @Shrinkable Object s, // Shrink only
      @Replaceable Object r, // Replace only
      @UnknownGrow @UnknownShrink @UnknownReplace Object none) { // no capabilities

    // ── Assignments to @Growable ──────────────────────────────
    @Growable Object gv1 = gsr; // G+S+R <: G
    @Growable Object gv2 = gs; // G+S <: G
    @Growable Object gv3 = gr; // G+R <: G
    // :: error: [assignment]
    @Growable Object gv4 = sr; // S+R: no G bit
    @Growable Object gv5 = g;
    // :: error: [assignment]
    @Growable Object gv6 = s;
    // :: error: [assignment]
    @Growable Object gv7 = r;
    // :: error: [assignment]
    @Growable Object gv8 = none;

    // ── Assignments to @Shrinkable ────────────────────────────
    @Shrinkable Object sv1 = gsr;
    @Shrinkable Object sv2 = gs;
    // :: error: [assignment]
    @Shrinkable Object sv3 = gr; // G+R: no S bit
    @Shrinkable Object sv4 = sr;
    // :: error: [assignment]
    @Shrinkable Object sv5 = g;
    @Shrinkable Object sv6 = s;
    // :: error: [assignment]
    @Shrinkable Object sv7 = r;
    // :: error: [assignment]
    @Shrinkable Object sv8 = none;

    // ── Assignments to @Replaceable ───────────────────────────
    @Replaceable Object rv1 = gsr;
    // :: error: [assignment]
    @Replaceable Object rv2 = gs; // G+S: no R bit
    @Replaceable Object rv3 = gr;
    @Replaceable Object rv4 = sr;
    // :: error: [assignment]
    @Replaceable Object rv5 = g;
    // :: error: [assignment]
    @Replaceable Object rv6 = s;
    @Replaceable Object rv7 = r;
    // :: error: [assignment]
    @Replaceable Object rv8 = none;

    // ── Assignments to full @UnknownGrow @UnknownShrink @UnknownReplace (top in all) ──
    @UnknownGrow @UnknownShrink @UnknownReplace Object tv1 = gsr;
    @UnknownGrow @UnknownShrink @UnknownReplace Object tv2 = gs;
    @UnknownGrow @UnknownShrink @UnknownReplace Object tv3 = gr;
    @UnknownGrow @UnknownShrink @UnknownReplace Object tv4 = sr;
    @UnknownGrow @UnknownShrink @UnknownReplace Object tv5 = g;
    @UnknownGrow @UnknownShrink @UnknownReplace Object tv6 = s;
    @UnknownGrow @UnknownShrink @UnknownReplace Object tv7 = r;
    @UnknownGrow @UnknownShrink @UnknownReplace Object tv8 = none;
  }

  // ============================================================
  // Alias annotations
  // ============================================================
  void testAliases(
      @Modifiable Object mod, @Unmodifiable Object unmod, @UnknownModifiability Object unknown) {

    // @Modifiable expands to @Growable @Shrinkable @Replaceable (full capabilities)
    @Growable Object g1 = mod;
    @Shrinkable Object s1 = mod;
    @Replaceable Object r1 = mod;

    // @Unmodifiable / @UnknownModifiability expand to @UnknownGrow @UnknownShrink @UnknownReplace
    @UnknownGrow Object ug1 = unmod;
    @UnknownShrink Object us1 = unmod;
    @UnknownReplace Object ur1 = unmod;
    @UnknownGrow Object ug2 = unknown;

    // @Modifiable <: @Unmodifiable in every hierarchy
    @Unmodifiable Object unmod1 = mod;
    @UnknownModifiability Object unknown1 = mod;

    // @Unmodifiable !<: @Growable (top !<: bottom in Grow hierarchy)
    // :: error: [assignment]
    @Growable Object g2 = unmod;

    // @Unmodifiable !<: @Modifiable
    // :: error: [assignment]
    @Modifiable Object mod1 = unmod;
  }
}
