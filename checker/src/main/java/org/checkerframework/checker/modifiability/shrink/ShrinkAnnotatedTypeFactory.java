package org.checkerframework.checker.modifiability.shrink;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.PolyModifiable;
import org.checkerframework.checker.modifiability.qual.PolyShrink;
import org.checkerframework.checker.modifiability.qual.Shrinkable;
import org.checkerframework.checker.modifiability.qual.UnknownModifiability;
import org.checkerframework.checker.modifiability.qual.UnknownShrink;
import org.checkerframework.checker.modifiability.qual.Unmodifiable;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.QualifierUpperBounds;
import org.checkerframework.framework.type.typeannotator.ListTypeAnnotator;
import org.checkerframework.framework.type.typeannotator.TypeAnnotator;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationMirrorSet;
import org.checkerframework.javacutil.TypesUtils;

/** The type factory for the Modifiability Checker. */
public class ShrinkAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

  /** The erased {@code java.util.Map.Entry} type. */
  private final TypeMirror mapEntryErasure;

  // ── Hierarchy qualifiers ──────────

  /** The {@code @}{@link UnknownShrink} qualifier (top of Shrink hierarchy). */
  private AnnotationMirror UNKNOWN_SHRINK;

  /** The {@code @}{@link Shrinkable} qualifier (bottom of Shrink hierarchy). */
  private AnnotationMirror SHRINKABLE;

  /** The {@code @}{@link PolyShrink} qualifier. */
  private AnnotationMirror POLY_SHRINK;

  @SuppressWarnings("this-escape")
  public ShrinkAnnotatedTypeFactory(BaseTypeChecker checker) {
    super(checker);
    // Cache type erasures.
    Types types = getProcessingEnv().getTypeUtils();
    this.mapEntryErasure =
        types.erasure(getElementUtils().getTypeElement("java.util.Map.Entry").asType());

    // Initialize annotation mirrors after the hierarchy is established.
    this.UNKNOWN_SHRINK = AnnotationBuilder.fromClass(getElementUtils(), UnknownShrink.class);
    this.SHRINKABLE = AnnotationBuilder.fromClass(getElementUtils(), Shrinkable.class);
    this.POLY_SHRINK = AnnotationBuilder.fromClass(getElementUtils(), PolyShrink.class);

    addAliasedTypeAnnotation(Modifiable.class, SHRINKABLE);
    addAliasedTypeAnnotation(Unmodifiable.class, UNKNOWN_SHRINK);
    addAliasedTypeAnnotation(UnknownModifiability.class, UNKNOWN_SHRINK);
    addAliasedTypeAnnotation(PolyModifiable.class, POLY_SHRINK);
    postInit();
  }

  @Override
  protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
    return new LinkedHashSet<>(
        Arrays.asList(UnknownShrink.class, Shrinkable.class, PolyShrink.class));
  }

  @Override
  protected TypeAnnotator createTypeAnnotator() {
    return new ListTypeAnnotator(new ShrinkTypeAnnotator(this), super.createTypeAnnotator());
  }

  /**
   * Removes capabilities that cannot be supported by structural constraints of the collection type:
   *
   * <ul>
   *   <li>Map.Entry: remove Shrink capabilities
   *   <li>Iterator: remove Shrink capabilities
   * </ul>
   */
  private class ShrinkTypeAnnotator extends TypeAnnotator {
    public ShrinkTypeAnnotator(ShrinkAnnotatedTypeFactory factory) {
      super(factory);
    }

    @Override
    public Void visitDeclared(AnnotatedDeclaredType type, Void p) {
      super.visitDeclared(type, p);

      // Skip structural refinement for polymorphic types.
      if (type.hasPrimaryAnnotation(POLY_SHRINK)) {
        return null;
      }

      TypeMirror underlyingType = type.getUnderlyingType();

      if (TypesUtils.isErasedSubtype(underlyingType, mapEntryErasure, types)) {
        // Map.Entry: Drop G and S bits
        type.replaceAnnotation(UNKNOWN_SHRINK);
      }

      return null;
    }
  }

  @Override
  protected QualifierUpperBounds createQualifierUpperBounds() {
    return new QualifierUpperBounds(this) {
      private final AnnotationMirrorSet unknownShrink =
          AnnotationMirrorSet.singleton(UNKNOWN_SHRINK);

      @Override
      public AnnotationMirrorSet getBoundQualifiers(TypeMirror type) {
        if (TypesUtils.isErasedSubtype(type, mapEntryErasure, types)) {
          // Elements of a map entry can never be shrunk, so treat them as @UnknownShrink. Even if
          // they are annotation @Growable in a stubfile.
          return unknownShrink;
        }
        return super.getBoundQualifiers(type);
      }
    };
  }
}
