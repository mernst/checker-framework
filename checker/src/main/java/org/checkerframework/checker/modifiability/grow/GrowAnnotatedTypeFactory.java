package org.checkerframework.checker.modifiability.grow;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.checkerframework.checker.modifiability.qual.Growable;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.PolyGrow;
import org.checkerframework.checker.modifiability.qual.PolyModifiable;
import org.checkerframework.checker.modifiability.qual.UnknownGrow;
import org.checkerframework.checker.modifiability.qual.UnknownModifiability;
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
public class GrowAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

  /** The erased {@code java.util.Map.Entry} type. */
  private final TypeMirror mapEntryErasure;

  /** The erased {@code java.util.Iterator} type. */
  private final TypeMirror iteratorErasure;

  /** The erased {@code java.util.ListIterator} type. */
  private final TypeMirror listIteratorErasure;

  // ── Hierarchy qualifiers ──────────

  /** The {@code @}{@link UnknownGrow} qualifier (top of Grow hierarchy). */
  private AnnotationMirror UNKNOWN_GROW;

  /** The {@code @}{@link Growable} qualifier (bottom of Grow hierarchy). */
  private AnnotationMirror GROWABLE;

  /** The {@code @}{@link PolyGrow} qualifier. */
  private AnnotationMirror POLY_GROW;

  @SuppressWarnings("this-escape")
  public GrowAnnotatedTypeFactory(BaseTypeChecker checker) {
    super(checker);
    // Cache type erasures.
    Types types = getProcessingEnv().getTypeUtils();
    this.mapEntryErasure =
        types.erasure(getElementUtils().getTypeElement("java.util.Map.Entry").asType());
    this.iteratorErasure =
        types.erasure(getElementUtils().getTypeElement("java.util.Iterator").asType());
    this.listIteratorErasure =
        types.erasure(getElementUtils().getTypeElement("java.util.ListIterator").asType());
    // Initialize annotation mirrors after the hierarchy is established.
    this.UNKNOWN_GROW = AnnotationBuilder.fromClass(getElementUtils(), UnknownGrow.class);
    this.GROWABLE = AnnotationBuilder.fromClass(getElementUtils(), Growable.class);
    this.POLY_GROW = AnnotationBuilder.fromClass(getElementUtils(), PolyGrow.class);

    addAliasedTypeAnnotation(Modifiable.class, GROWABLE);
    addAliasedTypeAnnotation(Unmodifiable.class, UNKNOWN_GROW);
    addAliasedTypeAnnotation(UnknownModifiability.class, UNKNOWN_GROW);
    addAliasedTypeAnnotation(PolyModifiable.class, POLY_GROW);
    postInit();
  }

  @Override
  protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
    return new LinkedHashSet<>(Arrays.asList(UnknownGrow.class, Growable.class, PolyGrow.class));
  }

  @Override
  protected TypeAnnotator createTypeAnnotator() {
    return new ListTypeAnnotator(new GrowTypeAnnotator(this), super.createTypeAnnotator());
  }

  /**
   * Removes capabilities that cannot be supported by structural constraints of the collection type:
   *
   * <ul>
   *   <li>Set or Queue (not LinkedList): remove Replace capability → set Replace to
   *       {@code @UnknownReplace}
   *   <li>Map.Entry: remove Grow and Shrink capabilities
   *   <li>Iterator: remove Grow and Replace capabilities
   * </ul>
   */
  private class GrowTypeAnnotator extends TypeAnnotator {
    public GrowTypeAnnotator(GrowAnnotatedTypeFactory factory) {
      super(factory);
    }

    @Override
    public Void visitDeclared(AnnotatedDeclaredType type, Void p) {
      super.visitDeclared(type, p);

      // Skip structural refinement for polymorphic types.
      if (type.hasPrimaryAnnotation(POLY_GROW)) {
        return null;
      }

      TypeMirror underlyingType = type.getUnderlyingType();

      if (TypesUtils.isErasedSubtype(underlyingType, mapEntryErasure, types)) {
        // Map.Entry: no grow.
        type.replaceAnnotation(UNKNOWN_GROW);
      } else if (TypesUtils.isErasedSubtype(underlyingType, iteratorErasure, types)
          && !TypesUtils.isErasedSubtype(underlyingType, listIteratorErasure, types)) {
        // Iterator: no grow.
        type.replaceAnnotation(UNKNOWN_GROW);
      }

      return null;
    }
  }

  @Override
  protected QualifierUpperBounds createQualifierUpperBounds() {
    return new QualifierUpperBounds(this) {
      private final AnnotationMirrorSet unknownGrow = AnnotationMirrorSet.singleton(UNKNOWN_GROW);

      @Override
      public AnnotationMirrorSet getBoundQualifiers(TypeMirror type) {
        if (TypesUtils.isErasedSubtype(type, mapEntryErasure, types)) {
          // Elements of a map entry can never be grown, so treat them as @UnknownGrow. Even if
          // they are annotation @Growable in a stubfile.
          return unknownGrow;
        } else if (TypesUtils.isErasedSubtype(type, iteratorErasure, types)
            && !TypesUtils.isErasedSubtype(type, listIteratorErasure, types)) {
          // It's a standard Iterator, but NOT a ListIterator: Drop G bit
          return unknownGrow;
        }
        return super.getBoundQualifiers(type);
      }
    };
  }
}
