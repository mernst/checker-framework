import org.checkerframework.checker.modifiability.qual.GrowReplace;
import org.checkerframework.checker.modifiability.qual.GrowShrink;
import org.checkerframework.checker.modifiability.qual.Growable;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.Replaceable;
import org.checkerframework.checker.modifiability.qual.ShrinkReplace;
import org.checkerframework.checker.modifiability.qual.Shrinkable;
import org.checkerframework.checker.modifiability.qual.UnknownModifiability;
import org.checkerframework.checker.modifiability.qual.Unmodifiable;

class HierarchyTest {
  void test(
      @Modifiable Object mod,
      @GrowShrink Object gs,
      @GrowReplace Object gr,
      @ShrinkReplace Object sr,
      @Growable Object g,
      @Shrinkable Object s,
      @Replaceable Object r,
      @UnknownModifiability Object unknown,
      @Unmodifiable Object unmod) {
    // ============================================================
    // Assignments to @UnknownModifiability (Top)
    // ============================================================
    @UnknownModifiability Object u1 = mod;
    @UnknownModifiability Object u2 = gs;
    @UnknownModifiability Object u3 = gr;
    @UnknownModifiability Object u4 = sr;
    @UnknownModifiability Object u5 = g;
    @UnknownModifiability Object u6 = s;
    @UnknownModifiability Object u7 = r;
    @UnknownModifiability Object u8 = unknown;
    @UnknownModifiability Object u9 = unmod;

    // ============================================================
    // Assignments to @Unmodifiable (Alias to Top)
    // ============================================================
    @Unmodifiable Object um1 = mod;
    @Unmodifiable Object um2 = gs;
    @Unmodifiable Object um3 = gr;
    @Unmodifiable Object um4 = sr;
    @Unmodifiable Object um5 = g;
    @Unmodifiable Object um6 = s;
    @Unmodifiable Object um7 = r;
    @Unmodifiable Object um8 = unknown;
    @Unmodifiable Object um9 = unmod;

    // ============================================================
    // Assignments to @Growable (G)
    // ============================================================
    @Growable Object g1 = mod;
    @Growable Object g2 = gs;
    @Growable Object g3 = gr;
    // :: error: (assignment)
    @Growable Object g4 = sr;
    @Growable Object g5 = g;
    // :: error: (assignment)
    @Growable Object g6 = s;
    // :: error: (assignment)
    @Growable Object g7 = r;
    // :: error: (assignment)
    @Growable Object g8 = unknown;
    // :: error: (assignment)
    @Growable Object g9 = unmod;

    // ============================================================
    // Assignments to @Shrinkable (S)
    // ============================================================
    @Shrinkable Object s1 = mod;
    @Shrinkable Object s2 = gs;
    // :: error: (assignment)
    @Shrinkable Object s3 = gr;
    @Shrinkable Object s4 = sr;
    // :: error: (assignment)
    @Shrinkable Object s5 = g;
    @Shrinkable Object s6 = s;
    // :: error: (assignment)
    @Shrinkable Object s7 = r;
    // :: error: (assignment)
    @Shrinkable Object s8 = unknown;
    // :: error: (assignment)
    @Shrinkable Object s9 = unmod;

    // ============================================================
    // Assignments to @Replaceable (R)
    // ============================================================
    @Replaceable Object r1 = mod;
    // :: error: (assignment)
    @Replaceable Object r2 = gs;
    @Replaceable Object r3 = gr;
    @Replaceable Object r4 = sr;
    // :: error: (assignment)
    @Replaceable Object r5 = g;
    // :: error: (assignment)
    @Replaceable Object r6 = s;
    @Replaceable Object r7 = r;
    // :: error: (assignment)
    @Replaceable Object r8 = unknown;
    // :: error: (assignment)
    @Replaceable Object r9 = unmod;

    // ============================================================
    // Assignments to @GrowShrink (G, S)
    // ============================================================
    @GrowShrink Object gs1 = mod;
    @GrowShrink Object gs2 = gs;
    // :: error: (assignment)
    @GrowShrink Object gs3 = gr;
    // :: error: (assignment)
    @GrowShrink Object gs4 = sr;
    // :: error: (assignment)
    @GrowShrink Object gs5 = g;
    // :: error: (assignment)
    @GrowShrink Object gs6 = s;
    // :: error: (assignment)
    @GrowShrink Object gs7 = r;
    // :: error: (assignment)
    @GrowShrink Object gs8 = unknown;

    // ============================================================
    // Assignments to @GrowReplace (G, R)
    // ============================================================
    @GrowReplace Object gr1 = mod;
    // :: error: (assignment)
    @GrowReplace Object gr2 = gs;
    @GrowReplace Object gr3 = gr;
    // :: error: (assignment)
    @GrowReplace Object gr4 = sr;
    // :: error: (assignment)
    @GrowReplace Object gr5 = g;
    // :: error: (assignment)
    @GrowReplace Object gr6 = s;
    // :: error: (assignment)
    @GrowReplace Object gr7 = r;
    // :: error: (assignment)
    @GrowReplace Object gr8 = unknown;

    // ============================================================
    // Assignments to @ShrinkReplace (S, R)
    // ============================================================
    @ShrinkReplace Object sr1 = mod;
    // :: error: (assignment)
    @ShrinkReplace Object sr2 = gs;
    // :: error: (assignment)
    @ShrinkReplace Object sr3 = gr;
    @ShrinkReplace Object sr4 = sr;
    // :: error: (assignment)
    @ShrinkReplace Object sr5 = g;
    // :: error: (assignment)
    @ShrinkReplace Object sr6 = s;
    // :: error: (assignment)
    @ShrinkReplace Object sr7 = r;
    // :: error: (assignment)
    @ShrinkReplace Object sr8 = unknown;

    // ============================================================
    // Assignments to @Modifiable (Bottom)
    // ============================================================
    @Modifiable Object m1 = mod;
    // :: error: (assignment)
    @Modifiable Object m2 = gs;
    // :: error: (assignment)
    @Modifiable Object m3 = gr;
    // :: error: (assignment)
    @Modifiable Object m4 = sr;
    // :: error: (assignment)
    @Modifiable Object m5 = g;
    // :: error: (assignment)
    @Modifiable Object m6 = s;
    // :: error: (assignment)
    @Modifiable Object m7 = r;
    // :: error: (assignment)
    @Modifiable Object m8 = unknown;
    // :: error: (assignment)
    @Modifiable Object m9 = unmod;
  }
}
