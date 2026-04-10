package org.checkerframework.checker.modifiability.replace;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.PolyModifiable;
import org.checkerframework.checker.modifiability.qual.PolyReplace;
import org.checkerframework.checker.modifiability.qual.Replaceable;
import org.checkerframework.checker.modifiability.qual.UnknownModifiability;
import org.checkerframework.checker.modifiability.qual.UnknownReplace;
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
public class ReplaceAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

  /** The erased {@code java.util.Set} type. */
  private final TypeMirror setErasure;

  /** The erased {@code java.util.Collection} type. */
  private final TypeMirror collectionErasure;

  /** The erased {@code java.util.Queue} type. */
  private final TypeMirror queueErasure;

  /** The erased {@code java.util.LinkedList} type. */
  private final TypeMirror linkedListErasure;

  /** The erased {@code java.util.Iterator} type. */
  private final TypeMirror iteratorErasure;

  /** The erased {@code java.util.ListIterator} type. */
  private final TypeMirror listIteratorErasure;

  // ── Hierarchy qualifiers ──────────

  /** The {@code @}{@link UnknownReplace} qualifier (top of Replace hierarchy). */
  private AnnotationMirror UNKNOWN_REPLACE;

  /** The {@code @}{@link Replaceable} qualifier (bottom of Replace hierarchy). */
  private AnnotationMirror REPLACEABLE;

  /** The {@code @}{@link PolyReplace} qualifier. */
  private AnnotationMirror POLY_REPLACE;

  @SuppressWarnings("this-escape")
  public ReplaceAnnotatedTypeFactory(BaseTypeChecker checker) {
    super(checker);
    // Cache type erasures.
    Types types = getProcessingEnv().getTypeUtils();
    this.setErasure = types.erasure(getElementUtils().getTypeElement("java.util.Set").asType());
    this.collectionErasure =
        types.erasure(getElementUtils().getTypeElement("java.util.Collection").asType());
    this.queueErasure = types.erasure(getElementUtils().getTypeElement("java.util.Queue").asType());
    this.linkedListErasure =
        types.erasure(getElementUtils().getTypeElement("java.util.LinkedList").asType());
    this.iteratorErasure =
        types.erasure(getElementUtils().getTypeElement("java.util.Iterator").asType());
    this.listIteratorErasure =
        types.erasure(getElementUtils().getTypeElement("java.util.ListIterator").asType());

    // Initialize annotation mirrors after the hierarchy is established.
    this.UNKNOWN_REPLACE = AnnotationBuilder.fromClass(getElementUtils(), UnknownReplace.class);
    this.REPLACEABLE = AnnotationBuilder.fromClass(getElementUtils(), Replaceable.class);
    this.POLY_REPLACE = AnnotationBuilder.fromClass(getElementUtils(), PolyReplace.class);

    addAliasedTypeAnnotation(Modifiable.class, REPLACEABLE);
    addAliasedTypeAnnotation(Unmodifiable.class, UNKNOWN_REPLACE);
    addAliasedTypeAnnotation(UnknownModifiability.class, UNKNOWN_REPLACE);
    addAliasedTypeAnnotation(PolyModifiable.class, POLY_REPLACE);
    postInit();
  }

  @Override
  protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
    return new LinkedHashSet<>(
        Arrays.asList(UnknownReplace.class, Replaceable.class, PolyReplace.class));
  }

  @Override
  protected TypeAnnotator createTypeAnnotator() {
    return new ListTypeAnnotator(new ReplaceTypeAnnotator(this), super.createTypeAnnotator());
  }

  /**
   * Removes capabilities that cannot be supported by structural constraints of the collection type:
   *
   * <ul>
   *   <li>Collection itself (not subtypes): remove Replace capability → set Replace to
   *       {@code @UnknownReplace}
   *   <li>Set or Queue (not LinkedList): remove Replace capability → set Replace to
   *       {@code @UnknownReplace}
   *   <li>Iterator: remove Replace capabilities
   * </ul>
   */
  private class ReplaceTypeAnnotator extends TypeAnnotator {
    public ReplaceTypeAnnotator(ReplaceAnnotatedTypeFactory factory) {
      super(factory);
    }

    @Override
    public Void visitDeclared(AnnotatedDeclaredType type, Void p) {
      super.visitDeclared(type, p);

      // Skip structural refinement for polymorphic types.
      if (type.hasPrimaryAnnotation(POLY_REPLACE)) {
        return null;
      }

      TypeMirror underlyingType = type.getUnderlyingType();
      TypeMirror erasedUnderlyingType = types.erasure(underlyingType);

      if (types.isSameType(erasedUnderlyingType, collectionErasure)) {
        // Collection itself: Drop R bit
        type.replaceAnnotation(UNKNOWN_REPLACE);
      } else if (TypesUtils.isErasedSubtype(underlyingType, setErasure, types)) {
        // Set: Drop R bit
        type.replaceAnnotation(UNKNOWN_REPLACE);
      } else if (TypesUtils.isErasedSubtype(underlyingType, queueErasure, types)
          && !TypesUtils.isErasedSubtype(underlyingType, linkedListErasure, types)) {
        // Queue (but not LinkedList): Drop R bit
        type.replaceAnnotation(UNKNOWN_REPLACE);
      } else if (TypesUtils.isErasedSubtype(underlyingType, iteratorErasure, types)
          && !TypesUtils.isErasedSubtype(underlyingType, listIteratorErasure, types)) {
        // Iterator: Drop R bits
        type.replaceAnnotation(UNKNOWN_REPLACE);
      }
      return null;
    }
  }

  @Override
  protected QualifierUpperBounds createQualifierUpperBounds() {
    return new QualifierUpperBounds(this) {
      private final AnnotationMirrorSet unknownReplace =
          AnnotationMirrorSet.singleton(UNKNOWN_REPLACE);

      @Override
      public AnnotationMirrorSet getBoundQualifiers(TypeMirror type) {
        TypeMirror erasedType = types.erasure(type);
        if (types.isSameType(erasedType, collectionErasure)) {
          // Elements of a raw Collection are treated as @UnknownReplace.
          return unknownReplace;
        } else if (TypesUtils.isErasedSubtype(type, setErasure, types)) {
          // Elements of a set can never be replaced, so treat them as @UnknownReplace. Even if
          // they are annotation @Modifiable in a stubfile.
          return unknownReplace;
        } else if (TypesUtils.isErasedSubtype(type, queueErasure, types)
            && !TypesUtils.isErasedSubtype(type, linkedListErasure, types)) {
          // Elements of a queue (but not LinkedList) can never be replaced, so treat them as
          // @UnknownReplace.
          return unknownReplace;
        } else if (TypesUtils.isErasedSubtype(type, iteratorErasure, types)
            && !TypesUtils.isErasedSubtype(type, listIteratorErasure, types)) {
          // Iterators cannot replace elements.
          return unknownReplace;
        }
        return super.getBoundQualifiers(type);
      }
    };
  }
}
