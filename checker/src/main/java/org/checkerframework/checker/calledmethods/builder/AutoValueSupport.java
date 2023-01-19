package org.checkerframework.checker.calledmethods.builder;

import com.sun.source.tree.NewClassTree;
import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.checker.calledmethods.CalledMethodsAnnotatedTypeFactory;
import org.checkerframework.checker.calledmethods.qual.CalledMethods;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.util.AnnotatedTypes;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.javacutil.TypesUtils;
import org.checkerframework.javacutil.UserError;
import org.plumelib.util.ArraysPlume;

/**
 * AutoValue support for the Called Methods Checker. This class adds {@code @}{@link CalledMethods}
 * annotations to the code generated by AutoValue.
 */
public class AutoValueSupport implements BuilderFrameworkSupport {

  /** The type factory. */
  private final CalledMethodsAnnotatedTypeFactory atypeFactory;

  /**
   * Create a new AutoValueSupport.
   *
   * @param atypeFactory the typechecker's type factory
   */
  public AutoValueSupport(CalledMethodsAnnotatedTypeFactory atypeFactory) {
    this.atypeFactory = atypeFactory;
  }

  /**
   * This method modifies the type of a copy constructor generated by AutoValue to match the type of
   * the AutoValue toBuilder method, and has no effect if {@code tree} is a call to any other
   * constructor.
   *
   * @param tree AST for a constructor call
   * @param type type of the call expression
   */
  @Override
  public void handleConstructor(NewClassTree tree, AnnotatedTypeMirror type) {
    ExecutableElement element = TreeUtils.elementFromUse(tree);
    TypeMirror superclass = ((TypeElement) element.getEnclosingElement()).getSuperclass();

    if (superclass.getKind() != TypeKind.NONE
        && ElementUtils.hasAnnotation(
            TypesUtils.getTypeElement(superclass), getAutoValuePackageName() + ".AutoValue.Builder")
        && element.getParameters().size() > 0) {
      handleToBuilderType(
          type,
          superclass,
          (TypeElement) TypesUtils.getTypeElement(superclass).getEnclosingElement());
    }
  }

  @Override
  public boolean isBuilderBuildMethod(ExecutableElement candidateBuildElement) {
    TypeElement builderElement = (TypeElement) candidateBuildElement.getEnclosingElement();
    if (ElementUtils.hasAnnotation(
        builderElement, getAutoValuePackageName() + ".AutoValue.Builder")) {
      Element classContainingBuilderElement = builderElement.getEnclosingElement();
      if (!ElementUtils.hasAnnotation(
          classContainingBuilderElement, getAutoValuePackageName() + ".AutoValue")) {
        throw new UserError(
            "class "
                + classContainingBuilderElement.getSimpleName()
                + " is missing @AutoValue annotation");
      }
      // it is a build method if it returns the type with the @AutoValue annotation
      if (TypesUtils.getTypeElement(candidateBuildElement.getReturnType())
          .equals(classContainingBuilderElement)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void handleBuilderBuildMethod(AnnotatedExecutableType builderBuildType) {

    ExecutableElement element = builderBuildType.getElement();
    TypeElement builderElement = (TypeElement) element.getEnclosingElement();
    TypeElement autoValueClassElement = (TypeElement) builderElement.getEnclosingElement();
    AnnotationMirror newCalledMethodsAnno =
        createCalledMethodsForAutoValueClass(builderElement, autoValueClassElement);
    // Only add the new @CalledMethods annotation if there is not already a @CalledMethods
    // annotation present.
    AnnotationMirror explicitCalledMethodsAnno =
        builderBuildType
            .getReceiverType()
            .getAnnotationInHierarchy(
                atypeFactory.getQualifierHierarchy().getTopAnnotation(newCalledMethodsAnno));
    if (explicitCalledMethodsAnno == null) {
      builderBuildType.getReceiverType().addAnnotation(newCalledMethodsAnno);
    }
  }

  @Override
  public boolean isToBuilderMethod(ExecutableElement candidateToBuilderElement) {
    if (!"toBuilder".equals(candidateToBuilderElement.getSimpleName().toString())) {
      return false;
    }

    TypeElement candidateClassContainingToBuilder =
        (TypeElement) candidateToBuilderElement.getEnclosingElement();
    boolean isAbstractAV =
        isAutoValueGenerated(candidateClassContainingToBuilder)
            && candidateToBuilderElement.getModifiers().contains(Modifier.ABSTRACT);
    TypeMirror superclassOfClassContainingToBuilder =
        candidateClassContainingToBuilder.getSuperclass();
    boolean superIsAV = false;
    if (superclassOfClassContainingToBuilder.getKind() != TypeKind.NONE) {
      superIsAV =
          isAutoValueGenerated(TypesUtils.getTypeElement(superclassOfClassContainingToBuilder));
    }
    return superIsAV || isAbstractAV;
  }

  @Override
  public void handleToBuilderMethod(AnnotatedExecutableType toBuilderType) {
    AnnotatedTypeMirror returnType = toBuilderType.getReturnType();
    ExecutableElement toBuilderElement = toBuilderType.getElement();
    TypeElement classContainingToBuilder = (TypeElement) toBuilderElement.getEnclosingElement();
    // Because of the way that the check in #isToBuilderMethod works, if the code reaches this
    // point and this condition is false, the other condition MUST be true (otherwise,
    // isToBuilderMethod would have returned false).
    if (isAutoValueGenerated(classContainingToBuilder)
        && toBuilderElement.getModifiers().contains(Modifier.ABSTRACT)) {
      handleToBuilderType(returnType, returnType.getUnderlyingType(), classContainingToBuilder);
    } else {
      TypeElement superElement =
          TypesUtils.getTypeElement(classContainingToBuilder.getSuperclass());
      handleToBuilderType(returnType, returnType.getUnderlyingType(), superElement);
    }
  }

  /**
   * Was the given element generated by AutoValue?
   *
   * @param element the element to check
   * @return true if the element was generated by AutoValue
   */
  private boolean isAutoValueGenerated(Element element) {
    return ElementUtils.hasAnnotation(element, getAutoValuePackageName() + ".AutoValue");
  }

  /**
   * Add, to {@code type}, a CalledMethods annotation with all required methods called. The type can
   * be the return type of toBuilder or of the corresponding generated "copy" constructor.
   *
   * @param type type to update
   * @param builderType type of abstract @AutoValue.Builder class
   * @param classElement AutoValue class corresponding to {@code type}
   */
  private void handleToBuilderType(
      AnnotatedTypeMirror type, TypeMirror builderType, TypeElement classElement) {
    TypeElement builderElement = TypesUtils.getTypeElement(builderType);
    AnnotationMirror calledMethodsAnno =
        createCalledMethodsForAutoValueClass(builderElement, classElement);
    type.replaceAnnotation(calledMethodsAnno);
  }

  /**
   * Create an @CalledMethods annotation for the given AutoValue class and builder. The returned
   * annotation contains all the required setters.
   *
   * @param builderElement the element for the Builder class
   * @param classElement the element for the AutoValue class (i.e. the class that is built by the
   *     builder)
   * @return an @CalledMethods annotation representing that all the required setters have been
   *     called
   */
  private AnnotationMirror createCalledMethodsForAutoValueClass(
      TypeElement builderElement, TypeElement classElement) {
    Set<String> avBuilderSetterNames = getAutoValueBuilderSetterMethodNames(builderElement);
    List<String> requiredProperties =
        getAutoValueRequiredProperties(classElement, avBuilderSetterNames);
    return createCalledMethodsForAutoValueProperties(requiredProperties, avBuilderSetterNames);
  }

  /**
   * Creates a @CalledMethods annotation for the given property names, converting the names to the
   * corresponding setter method name in the Builder.
   *
   * @param propertyNames the property names
   * @param avBuilderSetterNames names of all setters in the builder class
   * @return a @CalledMethods annotation that indicates all the given properties have been set
   */
  private AnnotationMirror createCalledMethodsForAutoValueProperties(
      final List<String> propertyNames, Set<String> avBuilderSetterNames) {
    List<String> calledMethodNames =
        propertyNames.stream()
            .map(prop -> autoValuePropToBuilderSetterName(prop, avBuilderSetterNames))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    return atypeFactory.createAccumulatorAnnotation(calledMethodNames);
  }

  /**
   * Converts the name of a property (i.e., a field) into the name of its setter.
   *
   * @param prop the property (i.e., field) name
   * @param builderSetterNames names of all methods in the builder class
   * @return the name of the setter for prop
   */
  private static String autoValuePropToBuilderSetterName(
      String prop, Set<String> builderSetterNames) {
    String[] possiblePropNames;
    if (prop.startsWith("get") && prop.length() > 3 && Character.isUpperCase(prop.charAt(3))) {
      possiblePropNames = new String[] {prop, Introspector.decapitalize(prop.substring(3))};
    } else if (prop.startsWith("is")
        && prop.length() > 2
        && Character.isUpperCase(prop.charAt(2))) {
      possiblePropNames = new String[] {prop, Introspector.decapitalize(prop.substring(2))};
    } else {
      possiblePropNames = new String[] {prop};
    }

    for (String propName : possiblePropNames) {
      // The setter may be the property name itself, or prefixed by 'set'.
      if (builderSetterNames.contains(propName)) {
        return propName;
      }
      String setterName = "set" + BuilderFrameworkSupportUtils.capitalize(propName);
      if (builderSetterNames.contains(setterName)) {
        return setterName;
      }
    }

    // Could not find a corresponding setter.  This is likely because an AutoValue Extension is
    // in use.  See https://github.com/kelloggm/object-construction-checker/issues/110 .
    // For now we return null, but once that bug is fixed, this should be changed to an
    // assertion failure.
    return null;
  }

  /**
   * Computes the required properties of an @AutoValue class.
   *
   * @param autoValueClassElement the @AutoValue class
   * @param avBuilderSetterNames names of all setters in the corresponding AutoValue builder class
   * @return a list of required property names
   */
  private List<String> getAutoValueRequiredProperties(
      final TypeElement autoValueClassElement, Set<String> avBuilderSetterNames) {
    return getAllAbstractMethods(autoValueClassElement).stream()
        .filter(member -> isAutoValueRequiredProperty(member, avBuilderSetterNames))
        .map(e -> e.getSimpleName().toString())
        .collect(Collectors.toList());
  }

  /** Method names for {@link #isAutoValueRequiredProperty} to ignore. */
  private final Set<String> isAutoValueRequiredPropertyIgnored =
      new HashSet<>(Arrays.asList("equals", "hashCode", "toString", "<init>", "toBuilder"));

  /**
   * Does member represent a required property of an AutoValue class?
   *
   * @param member a member of an AutoValue class or superclass
   * @param avBuilderSetterNames names of all setters in corresponding AutoValue builder class
   * @return true if {@code member} is required
   */
  private boolean isAutoValueRequiredProperty(
      ExecutableElement member, Set<String> avBuilderSetterNames) {
    String name = member.getSimpleName().toString();
    // Ignore java.lang.Object overrides, constructors, and toBuilder methods in AutoValue
    // classes.
    // Strictly speaking, this code should check return types, etc. to handle strange
    // overloads and other corner cases. They seem unlikely enough that we are skipping for now.
    if (isAutoValueRequiredPropertyIgnored.contains(name)) {
      return false;
    }
    TypeMirror returnType = member.getReturnType();
    if (returnType.getKind() == TypeKind.VOID) {
      return false;
    }
    // shouldn't have a nullable return
    boolean hasNullable =
        Stream.concat(
                atypeFactory.getElementUtils().getAllAnnotationMirrors(member).stream(),
                returnType.getAnnotationMirrors().stream())
            .anyMatch(anm -> AnnotationUtils.annotationName(anm).endsWith(".Nullable"));
    if (hasNullable) {
      return false;
    }
    // if return type of foo() is a Guava Immutable type, not required if there is a
    // builder method fooBuilder()
    if (BuilderFrameworkSupportUtils.isGuavaImmutableType(returnType)
        && avBuilderSetterNames.contains(name + "Builder")) {
      return false;
    }
    // if it's an Optional, the Builder will automatically initialize it
    if (isOptional(returnType)) {
      return false;
    }
    // it's required!
    return true;
  }

  /** Classes that AutoValue considers "optional". This list comes from AutoValue's source code. */
  private static String[] optionalClassNames =
      new String[] {
        "com.google.common.base.Optional",
        "java.util.Optional",
        "java.util.OptionalDouble",
        "java.util.OptionalInt",
        "java.util.OptionalLong"
      };

  /**
   * Returns whether AutoValue considers a type to be "optional". Optional types do not need to be
   * set before build is called on a builder. Adapted from AutoValue source code.
   *
   * @param type some type
   * @return true if type is an Optional type
   */
  private static boolean isOptional(TypeMirror type) {
    if (type.getKind() != TypeKind.DECLARED) {
      return false;
    }
    DeclaredType declaredType = (DeclaredType) type;
    TypeElement typeElement = (TypeElement) declaredType.asElement();
    return typeElement.getTypeParameters().size() == declaredType.getTypeArguments().size()
        && ArraysPlume.indexOf(optionalClassNames, typeElement.getQualifiedName().toString()) != -1;
  }

  /**
   * Returns names of all setter methods.
   *
   * @see #isAutoValueBuilderSetter
   * @param builderElement the element representing an AutoValue builder
   * @return the names of setter methods for the AutoValue builder
   */
  private Set<String> getAutoValueBuilderSetterMethodNames(TypeElement builderElement) {
    return getAllAbstractMethods(builderElement).stream()
        .filter(e -> isAutoValueBuilderSetter(e, builderElement))
        .map(e -> e.getSimpleName().toString())
        .collect(Collectors.toSet());
  }

  /**
   * Return true if the given method is a setter for an AutoValue builder; that is, its return type
   * is the builder itself or a Guava Immutable type.
   *
   * @param method a method of a builder or one of its supertypes
   * @param builderElement element for the AutoValue builder
   * @return true if {@code method} is a setter for the builder
   */
  private boolean isAutoValueBuilderSetter(ExecutableElement method, TypeElement builderElement) {
    TypeMirror retType = method.getReturnType();
    if (retType.getKind() == TypeKind.TYPEVAR) {
      // instantiate the type variable for the Builder class
      retType =
          AnnotatedTypes.asMemberOf(
                  atypeFactory.getChecker().getTypeUtils(),
                  atypeFactory,
                  atypeFactory.getAnnotatedType(builderElement),
                  method)
              .getReturnType()
              .getUnderlyingType();
    }
    // Either the return type should be the builder itself, or it should be a Guava immutable
    // type.
    return BuilderFrameworkSupportUtils.isGuavaImmutableType(retType)
        || builderElement.equals(TypesUtils.getTypeElement(retType));
  }

  /**
   * Get all the abstract methods for a class. This includes inherited abstract methods that are not
   * overridden by the class or a superclass. There is no guarantee that this method will work as
   * intended on code that implements an interface (which AutoValue classes are not supposed to do:
   * https://github.com/google/auto/blob/master/value/userguide/howto.md#inherit).
   *
   * @param classElement the class
   * @return list of all abstract methods
   */
  public List<ExecutableElement> getAllAbstractMethods(TypeElement classElement) {
    List<TypeElement> supertypes =
        ElementUtils.getAllSupertypes(classElement, atypeFactory.getProcessingEnv());
    List<ExecutableElement> abstractMethods = new ArrayList<>();
    Set<ExecutableElement> overriddenMethods = new HashSet<>();
    for (Element t : supertypes) {
      for (Element member : t.getEnclosedElements()) {
        if (member.getKind() != ElementKind.METHOD) {
          continue;
        }
        Set<Modifier> modifiers = member.getModifiers();
        if (modifiers.contains(Modifier.STATIC)) {
          continue;
        }
        if (modifiers.contains(Modifier.ABSTRACT)) {
          // Make sure it's not overridden. This only works because ElementUtils#closure
          // returns results in a particular order.
          if (!overriddenMethods.contains(member)) {
            abstractMethods.add((ExecutableElement) member);
          }
        } else {
          // Exclude any methods that this overrides.
          overriddenMethods.addAll(
              AnnotatedTypes.overriddenMethods(
                      atypeFactory.getElementUtils(), atypeFactory, (ExecutableElement) member)
                  .values());
        }
      }
    }
    return abstractMethods;
  }

  /**
   * Get the qualified name of the package containing AutoValue annotations. This method constructs
   * the String dynamically, to ensure it does not get rewritten due to relocation of the {@code
   * "com.google"} package during the build process.
   *
   * @return {@code "com.google.auto.value"}
   */
  private String getAutoValuePackageName() {
    String com = "com";
    return com + "." + "google.auto.value";
  }
}
