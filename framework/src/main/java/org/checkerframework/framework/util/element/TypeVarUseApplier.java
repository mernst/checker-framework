package org.checkerframework.framework.util.element;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.TargetType;
import com.sun.tools.javac.code.TypeAnnotationPosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedArrayType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedTypeVariable;
import org.checkerframework.framework.type.ElementAnnotationApplier;
import org.checkerframework.framework.util.element.ElementAnnotationUtil.UnexpectedAnnotationLocationException;
import org.checkerframework.javacutil.BugInCF;

/** Apply annotations to the use of a type parameter declaration. */
public class TypeVarUseApplier {

  public static void apply(
      final AnnotatedTypeMirror type, Element element, AnnotatedTypeFactory typeFactory)
      throws UnexpectedAnnotationLocationException {
    new TypeVarUseApplier(type, element, typeFactory).extractAndApply();
  }

  /** The ElementKinds that are accepted by this. */
  private static final ElementKind[] acceptedKinds = {
    ElementKind.PARAMETER,
    ElementKind.FIELD,
    ElementKind.LOCAL_VARIABLE,
    ElementKind.RESOURCE_VARIABLE,
    ElementKind.METHOD
  };

  /**
   * Returns true if type is an AnnotatedTypeVariable, or an AnnotatedArrayType with a type variable
   * component, and the element is not a TYPE_PARAMETER.
   *
   * @return true if type is an AnnotatedTypeVariable, or an AnnotatedArrayType with a type variable
   *     component, and the element is not a TYPE_PARAMETER
   */
  public static boolean accepts(AnnotatedTypeMirror type, Element element) {
    return (type instanceof AnnotatedTypeVariable || isGenericArrayType(type))
        && ElementAnnotationUtil.contains(element.getKind(), acceptedKinds);
  }

  private static boolean isGenericArrayType(AnnotatedTypeMirror type) {
    return type instanceof AnnotatedArrayType
        && getNestedComponentType(type) instanceof AnnotatedTypeVariable;
  }

  private static AnnotatedTypeMirror getNestedComponentType(AnnotatedTypeMirror type) {

    AnnotatedTypeMirror componentType = type;
    while (componentType instanceof AnnotatedArrayType) {
      componentType = ((AnnotatedArrayType) componentType).getComponentType();
    }

    return componentType;
  }

  /** The generic array type, if any. */
  // In order to avoid sprinkling code for type parameter uses all over the various locations
  // uses can show up, we also handle generic array types.  T [] myTArr;
  private final @Nullable AnnotatedArrayType arrayType;

  /** The type variable. */
  private final AnnotatedTypeVariable typeVariable;

  /** The element for the declaration. */
  private final TypeParameterElement declarationElem;

  /** The element for the use. */
  private final Element useElem;

  /** The annotated type factory. */
  private final AnnotatedTypeFactory typeFactory;

  /**
   * Create a new TypeVarUseApplier.
   *
   * @param type the type of the variable use
   * @param element the element for the variable use
   * @param typeFactory the type factory
   */
  TypeVarUseApplier(AnnotatedTypeMirror type, Element element, AnnotatedTypeFactory typeFactory) {
    if (!accepts(type, element)) {
      throw new BugInCF(
          "TypeParamUseApplier does not accept type/element combination ("
              + " type ( "
              + type
              + " ) element ( "
              + element
              + " ) ");
    }

    if (isGenericArrayType(type)) {
      this.arrayType = (AnnotatedArrayType) type;
      this.typeVariable = (AnnotatedTypeVariable) getNestedComponentType(type);
      this.declarationElem = (TypeParameterElement) typeVariable.getUnderlyingType().asElement();
      this.useElem = element;
      this.typeFactory = typeFactory;

    } else {
      this.arrayType = null;
      this.typeVariable = (AnnotatedTypeVariable) type;
      this.declarationElem = (TypeParameterElement) typeVariable.getUnderlyingType().asElement();
      this.useElem = element;
      this.typeFactory = typeFactory;
    }
  }

  /**
   * Applies the bound annotations from the declaration of the type parameter and then applies the
   * explicit annotations written on the type variable.
   *
   * @throws UnexpectedAnnotationLocationException if invalid location for an annotation was found
   */
  public void extractAndApply() throws UnexpectedAnnotationLocationException {
    ElementAnnotationUtil.addDeclarationAnnotationsFromElement(
        typeVariable, useElem.getAnnotationMirrors());

    // apply declaration annotations
    ElementAnnotationApplier.apply(typeVariable, declarationElem, typeFactory);

    List<Attribute.TypeCompound> annotations = getAnnotations(useElem, declarationElem);

    List<Attribute.TypeCompound> typeVarAnnotations;
    if (arrayType != null) {
      // if the outer-most type is an array type then we want to ensure the outer annotations
      // are not applied as the type variables primary annotation
      typeVarAnnotations = removeComponentAnnotations(arrayType, annotations);
      ElementAnnotationUtil.annotateViaTypeAnnoPosition(arrayType, annotations);

    } else {
      typeVarAnnotations = annotations;
    }

    for (Attribute.TypeCompound annotation : typeVarAnnotations) {
      typeVariable.replaceAnnotation(annotation);
    }
  }

  private List<Attribute.TypeCompound> removeComponentAnnotations(
      AnnotatedArrayType arrayType, List<Attribute.TypeCompound> annotations) {

    List<Attribute.TypeCompound> componentAnnotations = new ArrayList<>();

    if (arrayType != null) {
      for (int i = 0; i < annotations.size(); ) {
        Attribute.TypeCompound anno = annotations.get(i);
        if (isBaseComponent(arrayType, anno)) {
          componentAnnotations.add(anno);
          annotations.remove(anno);
        } else {
          i++;
        }
      }
    }

    return componentAnnotations;
  }

  private boolean isBaseComponent(AnnotatedArrayType arrayType, Attribute.TypeCompound anno) {
    try {
      return ElementAnnotationUtil.getTypeAtLocation(arrayType, anno.getPosition().location)
              .getKind()
          == TypeKind.TYPEVAR;
    } catch (UnexpectedAnnotationLocationException ex) {
      return false;
    }
  }

  /**
   * Depending on what element type the annotations are stored on, the relevant annotations might be
   * stored with different annotation positions. getPrimaryAnnotations finds the correct annotations
   * by annotation position and element kind and returns them.
   */
  private static List<Attribute.TypeCompound> getAnnotations(
      Element useElem, Element declarationElem) {
    final List<Attribute.TypeCompound> annotations;
    switch (useElem.getKind()) {
      case METHOD:
        annotations = getReturnAnnos(useElem);
        break;

      case PARAMETER:
        annotations = getParameterAnnos(useElem);
        break;

      case FIELD:
      case LOCAL_VARIABLE:
      case RESOURCE_VARIABLE:
        annotations = getVariableAnnos(useElem);
        break;

      default:
        throw new BugInCF(
            "TypeVarUseApplier::extractAndApply : "
                + "Unhandled element kind "
                + useElem.getKind()
                + "useElem ( "
                + useElem
                + " ) "
                + "declarationElem ( "
                + declarationElem
                + " ) ");
    }

    return annotations;
  }

  /**
   * Returns annotations on an element that apply to variable declarations.
   *
   * @param variableElem the element whose annotations to check
   * @return annotations on an element that apply to variable declarations
   */
  private static List<Attribute.TypeCompound> getVariableAnnos(Element variableElem) {
    VarSymbol varSymbol = (VarSymbol) variableElem;
    List<Attribute.TypeCompound> annotations = new ArrayList<>();

    for (Attribute.TypeCompound anno : varSymbol.getRawTypeAttributes()) {

      TypeAnnotationPosition pos = anno.position;
      switch (pos.type) {
        case FIELD:
        case LOCAL_VARIABLE:
        case RESOURCE_VARIABLE:
        case EXCEPTION_PARAMETER:
          annotations.add(anno);
          break;

        default:
      }
    }

    return annotations;
  }

  /**
   * Currently, the metadata for storing annotations (i.e. the Attribute.TypeCompounds) is null for
   * binary-only parameters and type parameters. However, it is present on the method. So in order
   * to ensure that we correctly retrieve the annotations we need to index from the method and
   * retrieve the annotations from its metadata.
   *
   * @return a list of annotations that were found on METHOD_FORMAL_PARAMETERS that match the
   *     parameter index of the input element in the parent methods formal parameter list
   */
  private static List<Attribute.TypeCompound> getParameterAnnos(Element paramElem) {
    Element enclosingElement = paramElem.getEnclosingElement();
    if (!(enclosingElement instanceof ExecutableElement)) {
      throw new BugInCF(
          "Bad element passed to TypeFromElement.getTypeParameterAnnotationAttributes: "
              + "element: "
              + paramElem
              + " not found in enclosing executable: "
              + enclosingElement);
    }

    MethodSymbol enclosingMethod = (MethodSymbol) enclosingElement;

    if (enclosingMethod.getKind() != ElementKind.CONSTRUCTOR
        && enclosingMethod.getKind() != ElementKind.METHOD) {
      // Initializer blocks don't have parameters, so there is nothing to do.
      return Collections.emptyList();
    }

    // TODO: for the parameter in a lambda expression, the enclosingMethod isn't
    // the lambda expression. Does this read the correct annotations?

    int paramIndex = enclosingMethod.getParameters().indexOf(paramElem);
    List<Attribute.TypeCompound> annotations = enclosingMethod.getRawTypeAttributes();

    List<Attribute.TypeCompound> result = new ArrayList<>();
    for (Attribute.TypeCompound typeAnno : annotations) {
      if (typeAnno.position.type == TargetType.METHOD_FORMAL_PARAMETER) {
        if (typeAnno.position.parameter_index == paramIndex) {
          result.add(typeAnno);
        }
      }
    }

    return result;
  }

  /**
   * Returns the annotations on the return type of the input ExecutableElement.
   *
   * @param methodElem the method whose return type annotations to return
   * @return the annotations on the return type of the input ExecutableElement
   */
  private static List<Attribute.TypeCompound> getReturnAnnos(Element methodElem) {
    if (!(methodElem instanceof ExecutableElement)) {
      throw new BugInCF("Bad element passed to TypeVarUseApplier.getReturnAnnos:" + methodElem);
    }

    MethodSymbol enclosingMethod = (MethodSymbol) methodElem;

    List<Attribute.TypeCompound> annotations = enclosingMethod.getRawTypeAttributes();
    List<Attribute.TypeCompound> result = new ArrayList<>();
    for (Attribute.TypeCompound typeAnno : annotations) {
      if (typeAnno.position.type == TargetType.METHOD_RETURN) {
        result.add(typeAnno);
      }
    }

    return result;
  }
}
