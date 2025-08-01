package org.checkerframework.javacutil;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.signature.qual.BinaryName;
import org.checkerframework.checker.signature.qual.CanonicalName;
import org.plumelib.util.ArraySet;
import org.plumelib.util.CollectionsPlume;

/**
 * Utility methods for analyzing {@code Element}s. This complements {@link Elements}, providing
 * functionality that it does not.
 */
public class ElementUtils {

  // Class cannot be instantiated.
  private ElementUtils() {
    throw new AssertionError("Class ElementUtils cannot be instantiated.");
  }

  /** The value of Flags.COMPACT_RECORD_CONSTRUCTOR which does not exist in Java 9 or 11. */
  private static final long Flags_COMPACT_RECORD_CONSTRUCTOR = 1L << 51;

  /** The value of Flags.GENERATED_MEMBER which does not exist in Java 9 or 11. */
  private static final long Flags_GENERATED_MEMBER = 16777216;

  /**
   * Returns the innermost type element that is, or encloses, the given element.
   *
   * <p>Note that in this code:
   *
   * <pre>{@code
   * class Outer {
   *   static class Inner {  }
   * }
   * }</pre>
   *
   * {@code Inner} has no enclosing type, but this method returns {@code Outer}.
   *
   * @param elem the enclosed element of a class
   * @return the innermost type element (possibly the argument itself), or null if {@code elem} is
   *     not, and is not enclosed by, a type element
   */
  public static @Nullable TypeElement enclosingTypeElement(Element elem) {
    Element result = elem;
    while (result != null && !isTypeElement(result)) {
      result = result.getEnclosingElement();
    }
    return (TypeElement) result;
  }

  /**
   * Returns the innermost type element enclosing the given element, that is different from the
   * element itself. By contrast, {@link #enclosingTypeElement} returns its argument if the argument
   * is a type element.
   *
   * @param elem the enclosed element of a class
   * @return the innermost type element, or null if no type element encloses {@code elem}
   */
  public static @Nullable TypeElement strictEnclosingTypeElement(Element elem) {
    Element enclosingElement = elem.getEnclosingElement();
    if (enclosingElement == null) {
      return null;
    }

    return enclosingTypeElement(enclosingElement);
  }

  /**
   * Returns the top-level type element that contains {@code element}.
   *
   * @param element the element whose enclosing tye element to find
   * @return a type element containing {@code element} that isn't contained in another class
   */
  public static TypeElement toplevelEnclosingTypeElement(Element element) {
    TypeElement result = enclosingTypeElement(element);
    if (result == null) {
      return (TypeElement) element;
    }

    TypeElement enclosing = strictEnclosingTypeElement(result);
    while (enclosing != null) {
      result = enclosing;
      enclosing = strictEnclosingTypeElement(enclosing);
    }

    return result;
  }

  /**
   * Returns the binary name of the class enclosing {@code executableElement}.
   *
   * @param executableElement the ExecutableElement
   * @return the binary name of the class enclosing {@code executableElement}
   */
  public static @BinaryName String getEnclosingClassName(ExecutableElement executableElement) {
    return getBinaryName(((MethodSymbol) executableElement).enclClass());
  }

  /**
   * Returns the binary name of the class enclosing {@code variableElement}.
   *
   * @param variableElement the VariableElement
   * @return the binary name of the class enclosing {@code variableElement}
   */
  public static @BinaryName String getEnclosingClassName(VariableElement variableElement) {
    TypeElement enclosingType = enclosingTypeElement(variableElement);
    if (enclosingType == null) {
      throw new BugInCF("enclosingTypeElement(%s) is null", variableElement);
    }
    return getBinaryName(enclosingType);
  }

  /**
   * Returns the innermost package element enclosing the given element. The same effect as {@link
   * javax.lang.model.util.Elements#getPackageOf(Element)}. Returns the element itself if it is a
   * package.
   *
   * @param elem the enclosed element of a package
   * @return the innermost package element
   */
  public static PackageElement enclosingPackage(Element elem) {
    Element result = elem;
    while (result != null && result.getKind() != ElementKind.PACKAGE) {
      Element encl = result.getEnclosingElement();
      result = encl;
    }
    return (PackageElement) result;
  }

  /**
   * Returns the "parent" package element for the given package element. For package "A.B" it gives
   * "A". For package "A" it gives the default package. For the default package it returns null.
   *
   * <p>Note that packages are not enclosed within each other, we have to manually climb the
   * namespaces. Calling "enclosingPackage" on a package element returns the package element itself
   * again.
   *
   * @param elem the package to start from
   * @param elements the element
   * @return the parent package element or {@code null}
   */
  public static @Nullable PackageElement parentPackage(PackageElement elem, Elements elements) {
    // The following might do the same thing:
    //   ((Symbol) elt).owner;
    // TODO: verify and see whether the change is worth it.
    String fqnstart = elem.getQualifiedName().toString();
    String fqn = fqnstart;
    if (fqn != null && !fqn.isEmpty()) {
      int dotPos = fqn.lastIndexOf('.');
      if (dotPos != -1) {
        return elements.getPackageElement(fqn.substring(0, dotPos));
      }
    }
    return null;
  }

  /**
   * Returns true if the element is a static element: whether it is a static field, static method,
   * or static class.
   *
   * @return true if element is static
   */
  public static boolean isStatic(Element element) {
    return element.getModifiers().contains(Modifier.STATIC);
  }

  /**
   * Returns true if the element is a final element: a final field, method, or final class.
   *
   * @return true if the element is final
   */
  public static boolean isFinal(Element element) {
    return element.getModifiers().contains(Modifier.FINAL);
  }

  /**
   * Returns true if the element is a effectively final element.
   *
   * @return true if the element is effectively final
   */
  public static boolean isEffectivelyFinal(Element element) {
    Symbol sym = (Symbol) element;
    if (sym.getEnclosingElement().getKind() == ElementKind.METHOD
        && (sym.getEnclosingElement().flags() & Flags.ABSTRACT) != 0) {
      return true;
    }
    return (sym.flags() & (Flags.FINAL | Flags.EFFECTIVELY_FINAL)) != 0;
  }

  /**
   * Returns the {@code TypeMirror} for usage of Element as a value. It returns the return type of a
   * method element, the class type of a constructor, or simply the type mirror of the element
   * itself.
   *
   * @param element the element whose type to obtain
   * @return the type for the element used as a value
   */
  @SuppressWarnings("nullness:dereference.of.nullable") // a constructor has an enclosing class
  public static TypeMirror getType(Element element) {
    if (element.getKind() == ElementKind.METHOD) {
      return ((ExecutableElement) element).getReturnType();
    } else if (element.getKind() == ElementKind.CONSTRUCTOR) {
      return enclosingTypeElement(element).asType();
    } else {
      return element.asType();
    }
  }

  /**
   * Returns the qualified name of the innermost class enclosing the provided {@code Element}.
   *
   * @param element an element enclosed by a class, or a {@code TypeElement}
   * @return the qualified {@code Name} of the innermost class enclosing the element
   */
  public static @Nullable Name getQualifiedClassName(Element element) {
    if (element.getKind() == ElementKind.PACKAGE) {
      PackageElement elem = (PackageElement) element;
      return elem.getQualifiedName();
    }

    TypeElement elem = enclosingTypeElement(element);
    if (elem == null) {
      return null;
    }

    return elem.getQualifiedName();
  }

  /**
   * Returns a verbose name that identifies the element.
   *
   * @param elt the element whose name to obtain
   * @return the qualified name of the given element
   */
  public static String getQualifiedName(Element elt) {
    if (elt.getKind() == ElementKind.PACKAGE || isTypeElement(elt)) {
      Name n = getQualifiedClassName(elt);
      if (n == null) {
        return "Unexpected element: " + elt;
      }
      return n.toString();
    } else {
      return getQualifiedName(elt.getEnclosingElement()) + "." + elt;
    }
  }

  /**
   * Returns the binary name of the given type.
   *
   * @param te a type
   * @return the binary name of the type
   */
  @SuppressWarnings("signature:return") // string manipulation
  public static @BinaryName String getBinaryName(TypeElement te) {
    Element enclosing = te.getEnclosingElement();
    String simpleName = te.getSimpleName().toString();
    if (enclosing == null) { // is this possible?
      return simpleName;
    }
    if (ElementUtils.isTypeElement(enclosing)) {
      return getBinaryName((TypeElement) enclosing) + "$" + simpleName;
    } else if (enclosing.getKind() == ElementKind.PACKAGE) {
      PackageElement pe = (PackageElement) enclosing;
      if (pe.isUnnamed()) {
        return simpleName;
      } else {
        return pe.getQualifiedName() + "." + simpleName;
      }
    } else {
      // This case occurs for anonymous inner classes. Fall back to the flatname method.
      return ((ClassSymbol) te).flatName().toString();
    }
  }

  /**
   * Returns the canonical representation of the method declaration, which contains simple names of
   * the types only.
   *
   * @param element a method declaration
   * @return the simple name of the method, followed by the simple names of the formal parameter
   *     types
   */
  public static String getSimpleSignature(ExecutableElement element) {
    // note: constructor simple name is <init>
    StringJoiner sj = new StringJoiner(",", element.getSimpleName() + "(", ")");
    for (Iterator<? extends VariableElement> i = element.getParameters().iterator();
        i.hasNext(); ) {
      sj.add(TypesUtils.simpleTypeName(i.next().asType()));
    }
    return sj.toString();
  }

  /**
   * Returns a user-friendly name for the given method. Does not return {@code "<init>"} or {@code
   * "<clinit>"} as ExecutableElement.getSimpleName() does.
   *
   * @param element a method declaration
   * @return a user-friendly name for the method
   * @deprecated use {@link #getSimpleDescription}
   */
  @Deprecated // 2023-06-01
  public static CharSequence getSimpleNameOrDescription(ExecutableElement element) {
    Name result = element.getSimpleName();
    switch (result.toString()) {
      case "<init>":
        return element.getEnclosingElement().getSimpleName();
      case "<clinit>":
        return "class initializer";
      default:
        return result;
    }
  }

  /**
   * Returns a user-friendly name for the given method, which includes the name of the enclosing
   * type. Does not return {@code "<init>"} or {@code "<clinit>"} as
   * ExecutableElement.getSimpleName() does.
   *
   * @param element a method declaration
   * @return a user-friendly name for the method
   */
  public static CharSequence getSimpleDescription(ExecutableElement element) {
    String enclosingTypeName =
        ((TypeElement) element.getEnclosingElement()).getSimpleName().toString();
    Name methodName = element.getSimpleName();
    switch (methodName.toString()) {
      case "<init>":
        return enclosingTypeName + " constructor";
      case "<clinit>":
        return "class initializer for " + enclosingTypeName;
      default:
        return enclosingTypeName + "." + methodName;
    }
  }

  /**
   * Check if the element is an element for 'java.lang.Object'
   *
   * @param element the type element
   * @return true iff the element is java.lang.Object element
   */
  public static boolean isObject(TypeElement element) {
    return element.getQualifiedName().contentEquals("java.lang.Object");
  }

  /**
   * Check if the element is an element for 'java.lang.String'
   *
   * @param element the type element
   * @return true iff the element is java.lang.String element
   */
  public static boolean isString(TypeElement element) {
    return element.getQualifiedName().contentEquals("java.lang.String");
  }

  /**
   * Returns true if the element is a reference to a compile-time constant.
   *
   * @param elt an element
   * @return true if the element is a reference to a compile-time constant
   */
  public static boolean isCompileTimeConstant(@Nullable Element elt) {
    return elt != null
        && (elt.getKind() == ElementKind.FIELD || elt.getKind() == ElementKind.LOCAL_VARIABLE)
        && ((VariableElement) elt).getConstantValue() != null;
  }

  /**
   * Returns true if a given element came from a source file.
   *
   * <p>By contrast, {@link ElementUtils#isElementFromByteCode(Element)} returns true if there is a
   * classfile for the given element, even if there is also a source file.
   *
   * @param element the element to check, or null
   * @return true if a source file containing the element is being compiled
   */
  public static boolean isElementFromSourceCode(@Nullable Element element) {
    if (element == null) {
      return false;
    }
    TypeElement enclosingClass = enclosingTypeElement(element);
    if (enclosingClass == null) {
      throw new BugInCF("enclosingClass(%s) is null", element);
    }
    return isElementFromSourceCodeImpl((Symbol.ClassSymbol) enclosingClass);
  }

  /**
   * Returns true if a given ClassSymbol came from a source file.
   *
   * <p>By contrast, {@link ElementUtils#isElementFromByteCode(Element)} returns true if there is a
   * classfile for the given element, even if there is also a source file.
   *
   * @param symbol the class to check
   * @return true if a source file containing the class is being compiled
   */
  private static boolean isElementFromSourceCodeImpl(Symbol.ClassSymbol symbol) {
    // This is a bit of a hack to avoid treating JDK as source files. JDK files' toUri() method
    // returns just the name of the file (e.g. "Object.java"), but any file actually being
    // compiled returns a file URI to the source file.
    return symbol.sourcefile != null
        && symbol.sourcefile.getKind() == JavaFileObject.Kind.SOURCE
        && symbol.sourcefile.toUri().toString().startsWith("file:");
  }

  /**
   * Returns true if the element is declared in ByteCode. Always return false if elt is a package.
   *
   * @param elt some element
   * @return true if the element is declared in ByteCode
   */
  public static boolean isElementFromByteCode(@Nullable Element elt) {
    if (elt == null) {
      return false;
    }

    if (elt instanceof Symbol.ClassSymbol) {
      Symbol.ClassSymbol clss = (Symbol.ClassSymbol) elt;
      if (null != clss.classfile) {
        // The class file could be a .java file
        return clss.classfile.getKind() == JavaFileObject.Kind.CLASS;
      } else {
        return elt.asType().getKind().isPrimitive();
      }
    }
    return isElementFromByteCode(elt.getEnclosingElement());
  }

  /**
   * Returns the path to the source file containing {@code element}, which must be from source code.
   *
   * @param element the type element to look at
   * @return path to the source file containing {@code element}
   */
  public static String getSourceFilePath(TypeElement element) {
    return ((ClassSymbol) element).sourcefile.toUri().getPath();
  }

  /**
   * Returns the field of the class or {@code null} if not found.
   *
   * @param type the TypeElement to search
   * @param name name of a field
   * @return the VariableElement for the field if it was found, null otherwise
   */
  public static @Nullable VariableElement findFieldInType(TypeElement type, String name) {
    for (VariableElement field : ElementFilter.fieldsIn(type.getEnclosedElements())) {
      if (field.getSimpleName().contentEquals(name)) {
        return field;
      }
    }
    return null;
  }

  /**
   * Returns the elements of the fields whose simple names are in {@code names} and are declared in
   * {@code type}.
   *
   * <p>If a field isn't declared in {@code type}, its element isn't included in the returned set.
   * If none of the fields is declared in {@code type}, the empty set is returned.
   *
   * @param type where to look for fields
   * @param names simple names of fields that might be declared in {@code type}
   * @return the elements of the fields whose simple names are {@code names} and are declared in
   *     {@code type}
   */
  public static Set<VariableElement> findFieldsInType(TypeElement type, Collection<String> names) {
    Set<VariableElement> results = ArraySet.newArraySetOrHashSet(names.size());
    for (VariableElement field : ElementFilter.fieldsIn(type.getEnclosedElements())) {
      if (names.contains(field.getSimpleName().toString())) {
        results.add(field);
      }
    }
    return results;
  }

  /**
   * Returns non-private field elements, and side-effects {@code names} to remove them. For every
   * field name in {@code names} that is declared in {@code type} or a supertype, add its element to
   * the returned set and remove it from {@code names}.
   *
   * <p>When this routine returns, the combination of the return value and {@code names} has the
   * same cardinality, and represents the same fields, as {@code names} did when the method was
   * called.
   *
   * @param type where to look for fields
   * @param names simple names of fields that might be declared in {@code type} or a supertype.
   *     Names that are found are removed from this list.
   * @return the {@code VariableElement}s for non-private fields that are declared in {@code type}
   *     whose simple names were in {@code names} when the method was called
   */
  public static Set<VariableElement> findFieldsInTypeOrSuperType(
      TypeMirror type, Collection<String> names) {
    int origCardinality = names.size();
    Set<VariableElement> elements = ArraySet.newArraySetOrHashSet(origCardinality);
    findFieldsInTypeOrSuperType(type, names, elements);
    // Since `names` may contain duplicates, I don't trust the claim in the documentation about
    // cardinality.  (Does any code depend on the invariant, though?)
    if (origCardinality != names.size() + elements.size()) {
      throw new BugInCF(
          "Bad sizes: %d != %d + %d ; names=%s  elements=%s",
          origCardinality, names.size(), elements.size(), names, elements);
    }
    return elements;
  }

  /**
   * Side-effects both {@code foundFields} (which starts empty) and {@code notFound}, conceptually
   * moving elements from {@code notFound} to {@code foundFields}.
   */
  private static void findFieldsInTypeOrSuperType(
      TypeMirror type, Collection<String> notFound, Set<VariableElement> foundFields) {
    if (TypesUtils.isObject(type)) {
      return;
    }
    TypeElement elt = TypesUtils.getTypeElement(type);
    assert elt != null : "@AssumeAssertion(nullness): assumption";
    Set<VariableElement> fieldElts = findFieldsInType(elt, notFound);
    // Use a new list to avoid a ConcurrentModificationException.
    for (VariableElement field : new ArrayList<>(fieldElts)) {
      if (!field.getModifiers().contains(Modifier.PRIVATE)) {
        notFound.remove(field.getSimpleName().toString());
      } else {
        fieldElts.remove(field);
      }
    }
    foundFields.addAll(fieldElts);

    if (!notFound.isEmpty()) {
      findFieldsInTypeOrSuperType(elt.getSuperclass(), notFound, foundFields);
    }
  }

  /**
   * Returns true if {@code element} is "com.sun.tools.javac.comp.Resolve$SymbolNotFoundError".
   *
   * @param element the element to test
   * @return true if {@code element} is "com.sun.tools.javac.comp.Resolve$SymbolNotFoundError"
   */
  public static boolean isError(Element element) {
    return element.getClass().getName()
        == "com.sun.tools.javac.comp.Resolve$SymbolNotFoundError"; // interned
  }

  /**
   * Does the given element need a receiver for accesses? For example, an access to a local variable
   * does not require a receiver.
   *
   * @param element the element to test
   * @return true if the element requires a receiver for accesses
   */
  public static boolean hasReceiver(Element element) {
    if (element.getKind() == ElementKind.CONSTRUCTOR) {
      // The enclosing element of a constructor is the class it creates.
      // A constructor can only have a receiver if the class it creates has an outer type.
      TypeMirror t = element.getEnclosingElement().asType();
      return TypesUtils.hasEnclosingType(t);
    } else if (element.getKind() == ElementKind.FIELD) {
      if (ElementUtils.isStatic(element)
          // Artificial fields in interfaces are not marked as static, so check that
          // the field is not declared in an interface.
          || element.getEnclosingElement().getKind().isInterface()) {
        return false;
      } else {
        // In constructors, the element for "this" is a non-static field, but that field
        // does not have a receiver.
        return !element.getSimpleName().contentEquals("this");
      }
    }
    return element.getKind() == ElementKind.METHOD && !ElementUtils.isStatic(element);
  }

  /**
   * Returns a type's superclass, or null if it does not have a superclass (it is object or an
   * interface, or the superclass is not on the classpath).
   *
   * @param typeElt a type element
   * @return the superclass of {@code typeElt}
   */
  public static @Nullable TypeElement getSuperClass(TypeElement typeElt) {
    TypeMirror superTypeMirror;
    try {
      superTypeMirror = typeElt.getSuperclass();
    } catch (com.sun.tools.javac.code.Symbol.CompletionFailure cf) {
      // Looking up a supertype failed. This sometimes happens when transitive dependencies
      // are not on the classpath.  As javac didn't complain, let's also not complain.
      return null;
    }

    if (superTypeMirror == null || superTypeMirror.getKind() == TypeKind.NONE) {
      return null;
    } else {
      return (TypeElement) ((DeclaredType) superTypeMirror).asElement();
    }
  }

  /**
   * Determine all type elements for the supertypes of the given type element. This is the
   * transitive closure of the extends and implements clauses.
   *
   * <p>TODO: can we learn from the implementation of
   * com.sun.tools.javac.model.JavacElements.getAllMembers(TypeElement)?
   *
   * @param type the type whose supertypes to return
   * @param elements the Element utilities
   * @return supertypes of {@code type}
   */
  public static List<TypeElement> getSuperTypes(TypeElement type, Elements elements) {

    if (type == null) {
      return Collections.emptyList();
    }

    List<TypeElement> superElems = new ArrayList<>();

    // Set up a stack containing `type`, which is our starting point.
    Deque<TypeElement> stack = new ArrayDeque<>();
    stack.push(type);

    while (!stack.isEmpty()) {
      TypeElement current = stack.pop();

      // For each direct supertype of the current type element, if it
      // hasn't already been visited, push it onto the stack and
      // add it to our superElems set.
      TypeElement supercls = ElementUtils.getSuperClass(current);
      if (supercls != null) {
        if (!superElems.contains(supercls)) {
          stack.push(supercls);
          superElems.add(supercls);
        }
      }

      for (TypeMirror supertypeitf : current.getInterfaces()) {
        TypeElement superitf = (TypeElement) ((DeclaredType) supertypeitf).asElement();
        if (!superElems.contains(superitf)) {
          stack.push(superitf);
          superElems.add(superitf);
        }
      }
    }

    // Include java.lang.Object as implicit superclass for all classes and interfaces.
    TypeElement jlobject = elements.getTypeElement("java.lang.Object");
    if (!superElems.contains(jlobject)) {
      superElems.add(jlobject);
    }

    return Collections.unmodifiableList(superElems);
  }

  /**
   * Determine all type elements for the direct supertypes of the given type element. This is the
   * union of the extends and implements clauses.
   *
   * @param type the type whose supertypes to return
   * @param elements the Element utilities
   * @return direct supertypes of {@code type}
   */
  public static List<TypeElement> getDirectSuperTypeElements(TypeElement type, Elements elements) {
    TypeMirror superclass = type.getSuperclass();
    List<? extends TypeMirror> interfaces = type.getInterfaces();
    List<TypeElement> result = new ArrayList<TypeElement>(interfaces.size() + 1);
    if (superclass.getKind() != TypeKind.NONE) {
      @SuppressWarnings("nullness:assignment") // Not null because the TypeKind is not NONE.
      @NonNull TypeElement superclassElement = TypesUtils.getTypeElement(superclass);
      result.add(superclassElement);
    }
    for (TypeMirror interfac : interfaces) {
      @SuppressWarnings("nullness:assignment") // every interface is a type
      @NonNull TypeElement interfaceElt = TypesUtils.getTypeElement(interfac);
      result.add(interfaceElt);
    }
    return result;
  }

  /**
   * Returns all fields declared in the given type or any superclass/interface.
   *
   * <p>TODO: should this use javax.lang.model.util.Elements.getAllMembers(TypeElement) instead of
   * our own getSuperTypes?
   *
   * @param type the type whose fields to return
   * @param elements the Element utilities
   * @return fields of {@code type}
   */
  public static List<VariableElement> getAllFieldsIn(TypeElement type, Elements elements) {
    // ElementFilter.fieldsIn returns a new list
    List<VariableElement> fields = ElementFilter.fieldsIn(type.getEnclosedElements());
    List<TypeElement> alltypes = getSuperTypes(type, elements);
    for (TypeElement atype : alltypes) {
      fields.addAll(ElementFilter.fieldsIn(atype.getEnclosedElements()));
    }
    return Collections.unmodifiableList(fields);
  }

  /**
   * Returns all enum constants declared in the given enumeration.
   *
   * @param type an Enum type
   * @return all enum constants declared in the given enumeration
   */
  public static List<VariableElement> getEnumConstants(TypeElement type) {
    List<? extends Element> enclosedElements = type.getEnclosedElements();
    List<VariableElement> enumConstants = new ArrayList<>(enclosedElements.size());
    for (Element e : enclosedElements) {
      if (e.getKind() == ElementKind.ENUM_CONSTANT) {
        enumConstants.add((VariableElement) e);
      }
    }
    return enumConstants;
  }

  /**
   * Returns all methods declared in the given type or any superclass/interface. Note that no
   * constructors will be returned.
   *
   * <p>TODO: should this use javax.lang.model.util.Elements.getAllMembers(TypeElement) instead of
   * our own getSuperTypes?
   *
   * @param type the type whose methods to return
   * @param elements the Element utilities
   * @return methods of {@code type}
   */
  public static List<ExecutableElement> getAllMethodsIn(TypeElement type, Elements elements) {
    // ElementFilter.fieldsIn returns a new list
    List<ExecutableElement> meths = ElementFilter.methodsIn(type.getEnclosedElements());

    List<TypeElement> alltypes = getSuperTypes(type, elements);
    for (TypeElement atype : alltypes) {
      meths.addAll(ElementFilter.methodsIn(atype.getEnclosedElements()));
    }
    return Collections.unmodifiableList(meths);
  }

  /**
   * Returns all nested/inner classes/interfaces declared in the given type.
   *
   * @param type a type
   * @return all nested/inner classes/interfaces declared in {@code type}
   */
  public static List<TypeElement> getAllTypeElementsIn(TypeElement type) {
    return ElementFilter.typesIn(type.getEnclosedElements());
  }

  /** The set of kinds that represent types. */
  private static final Set<ElementKind> typeElementKinds;

  static {
    typeElementKinds = EnumSet.noneOf(ElementKind.class);
    for (ElementKind kind : ElementKind.values()) {
      if (kind.isClass() || kind.isInterface()) {
        typeElementKinds.add(kind);
      }
    }
  }

  /**
   * Returns the set of kinds that represent classes.
   *
   * @return the set of kinds that represent classes
   */
  public static Set<ElementKind> typeElementKinds() {
    return typeElementKinds;
  }

  /**
   * Is the given element kind a type, i.e., a class, enum, interface, or annotation type.
   *
   * @param element the element to test
   * @return true, iff the given kind is a class kind
   * @deprecated use {@link #isTypeElement}
   */
  @Deprecated // 2020-12-11
  public static boolean isClassElement(Element element) {
    return isTypeElement(element);
  }

  /**
   * Is the given element kind a type, i.e., a class, enum, interface, or annotation type.
   *
   * @param element the element to test
   * @return true, iff the given kind is a class kind
   */
  public static boolean isTypeElement(Element element) {
    return typeElementKinds().contains(element.getKind());
  }

  /**
   * Returns true if the element is a type declaration.
   *
   * @param elt the element to test
   * @return true if the argument is a type declaration
   */
  public static boolean isTypeDeclaration(Element elt) {
    return isClassElement(elt) || elt.getKind() == ElementKind.TYPE_PARAMETER;
  }

  /** The set of kinds that represent local variables. */
  private static final Set<ElementKind> localVariableElementKinds =
      EnumSet.of(
          ElementKind.LOCAL_VARIABLE,
          ElementKind.RESOURCE_VARIABLE,
          ElementKind.EXCEPTION_PARAMETER);

  /**
   * Returns true if the element is a local variable.
   *
   * @param elt the element to test
   * @return true if the argument is a local variable
   */
  public static boolean isLocalVariable(Element elt) {
    return localVariableElementKinds.contains(elt.getKind());
  }

  /**
   * Returns true if the element is a binding variable.
   *
   * <p>This implementation compiles and runs under JDK 8 and 11 as well as versions that contain
   * {@code ElementKind.BINDING_VARIABLE}.
   *
   * @param element the element to test
   * @return true if the element is a binding variable
   */
  public static boolean isBindingVariable(Element element) {
    return SystemUtil.jreVersion >= 16 && "BINDING_VARIABLE".equals(element.getKind().name());
  }

  /**
   * Returns true if the element is a record accessor method.
   *
   * @param methodElement a method element
   * @return true if the element is a record accessor method
   */
  public static boolean isRecordAccessor(ExecutableElement methodElement) {
    // Can only be a record accessor if it has no parameters.
    if (!methodElement.getParameters().isEmpty()) {
      return false;
    }

    TypeElement enclosing = (TypeElement) methodElement.getEnclosingElement();
    if (enclosing.getKind().toString().equals("RECORD")) {
      String methodName = methodElement.getSimpleName().toString();
      List<? extends Element> encloseds = enclosing.getEnclosedElements();
      for (Element enclosed : encloseds) {
        if (enclosed.getKind().toString().equals("RECORD_COMPONENT")
            && enclosed.getSimpleName().toString().equals(methodName)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns true if the given {@link Element} is part of a record that has been automatically
   * generated by the compiler. This can be a field that is derived from the record's header field
   * list, or an automatically generated canonical constructor.
   *
   * @param e the {@link Element} for a member of a record
   * @return true if the given element is generated by the compiler
   */
  public static boolean isAutoGeneratedRecordMember(Element e) {
    if (!(e instanceof Symbol)) {
      return false;
    }
    // Generated constructors seem to get GENERATEDCONSTR even though the documentation
    // seems to imply they would get GENERATED_MEMBER like the fields do.
    return (((Symbol) e).flags() & (Flags_GENERATED_MEMBER | Flags.GENERATEDCONSTR)) != 0;
  }

  /**
   * Check that a method Element matches a signature.
   *
   * <p>Note: Matching the receiver type must be done elsewhere as the Element receiver type is only
   * populated when annotated.
   *
   * @param method the method Element to be tested
   * @param methodName the goal method name
   * @param parameters the goal formal parameter Classes
   * @return true if the method matches the methodName and parameters
   */
  public static boolean matchesElement(
      ExecutableElement method, String methodName, Class<?>... parameters) {

    if (!method.getSimpleName().contentEquals(methodName)) {
      return false;
    }

    if (method.getParameters().size() != parameters.length) {
      return false;
    } else {
      for (int i = 0; i < method.getParameters().size(); i++) {
        if (!method.getParameters().get(i).asType().toString().equals(parameters[i].getName())) {

          return false;
        }
      }
    }

    return true;
  }

  /**
   * Returns true if the given element is, or overrides, {@code method}.
   *
   * @param questioned an element that might override {@code method}
   * @param method a method that might be overridden
   * @param env the processing environment
   * @return true if {@code questioned} is, or overrides, {@code method}
   */
  public static boolean isMethod(
      ExecutableElement questioned, @Nullable ExecutableElement method, ProcessingEnvironment env) {
    if (method == null) {
      return false;
    }
    return questioned.equals(method)
        || env.getElementUtils()
            .overrides(questioned, method, (TypeElement) questioned.getEnclosingElement());
  }

  /**
   * Given an annotation name, return true if the element has the annotation of that name.
   *
   * <p>It is more efficient to use {@code Element#getAnnotation(Class)}, but note that both methods
   * ignore types from annotation files, such as stub or ajava files.
   *
   * <p>To include types from annotation files, use {@code AnnotatedTypeFactory#fromElement} or
   * {@code AnnotatedTypeFactory#getDeclAnnotations}.
   *
   * @param element the element
   * @param annotName name of the annotation
   * @return true if the element has the annotation of that name
   */
  public static boolean hasAnnotation(Element element, String annotName) {
    for (AnnotationMirror anm : element.getAnnotationMirrors()) {
      if (AnnotationUtils.areSameByName(anm, annotName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the TypeElement for the given class.
   *
   * @param processingEnv the processing environment
   * @param clazz a class
   * @return the TypeElement for the class
   */
  public static TypeElement getTypeElement(ProcessingEnvironment processingEnv, Class<?> clazz) {
    @CanonicalName String className = clazz.getCanonicalName();
    if (className == null) {
      throw new BugInCF("Anonymous class " + clazz + " has no canonical name");
    }
    return processingEnv.getElementUtils().getTypeElement(className);
  }

  /**
   * Returns all the supertypes of a given type, including the type itself. The result includes both
   * superclasses and implemented interfaces.
   *
   * @param type a type
   * @param env the processing environment
   * @return list including the type and all its supertypes, with a guarantee that direct supertypes
   *     (i.e. those that appear in extends or implements clauses) appear before indirect supertypes
   */
  public static List<TypeElement> getAllSupertypes(TypeElement type, ProcessingEnvironment env) {
    Context ctx = ((JavacProcessingEnvironment) env).getContext();
    com.sun.tools.javac.code.Types javacTypes = com.sun.tools.javac.code.Types.instance(ctx);
    return CollectionsPlume.<Type, TypeElement>mapList(
        t -> (TypeElement) t.tsym, javacTypes.closure(((Symbol) type).type));
  }

  /**
   * Returns the methods that are overridden or implemented by a given method.
   *
   * @param m a method
   * @param types the type utilities
   * @return the methods that {@code m} overrides or implements
   */
  public static Set<? extends ExecutableElement> getOverriddenMethods(
      ExecutableElement m, Types types) {
    JavacTypes t = (JavacTypes) types;
    return t.getOverriddenMethods(m);
  }

  /**
   * Returns true if the two elements are in the same class. The two elements should be class
   * members, such as methods or fields.
   *
   * @param e1 an element
   * @param e2 an element
   * @return true if the two elements are in the same class
   */
  public static boolean inSameClass(Element e1, Element e2) {
    return e1.getEnclosingElement().equals(e2.getEnclosingElement());
  }

  /**
   * Calls getKind() on the given Element, but returns CLASS if the ElementKind is RECORD. This is
   * needed because the Checker Framework runs on JDKs before the RECORD item was added, so RECORD
   * can't be used in case statements, and usually we want to treat them the same as classes.
   *
   * @param elt the element to get the kind for
   * @return the kind of the element, but CLASS if the kind was RECORD
   */
  public static ElementKind getKindRecordAsClass(Element elt) {
    ElementKind kind = elt.getKind();
    if (kind.name().equals("RECORD")) {
      kind = ElementKind.CLASS;
    }
    return kind;
  }

  /** The {@code TypeElement.getRecordComponents()} method. */
  private static final @Nullable Method getRecordComponentsMethod;

  static {
    if (SystemUtil.jreVersion >= 16) {
      try {
        getRecordComponentsMethod = TypeElement.class.getMethod("getRecordComponents");
      } catch (NoSuchMethodException e) {
        throw new BugInCF("Cannot access TypeElement.getRecordComponents()", e);
      }
    } else {
      getRecordComponentsMethod = null;
    }
  }

  /**
   * Calls getRecordComponents on the given TypeElement. Uses reflection because this method is not
   * available before JDK 16. On earlier JDKs, which don't support records anyway, an exception is
   * thrown.
   *
   * @param element the type element to call getRecordComponents on
   * @return the return value of calling getRecordComponents, or empty list if the method is not
   *     available
   */
  @SuppressWarnings({"unchecked", "nullness"}) // because of cast from reflection
  public static List<? extends Element> getRecordComponents(TypeElement element) {
    try {
      return (@NonNull List<? extends Element>) getRecordComponentsMethod.invoke(element);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new Error("Cannot call TypeElement.getRecordComponents()", e);
    }
  }

  /**
   * Check if the given element is a compact canonical record constructor.
   *
   * @param elt the element to check
   * @return true if the element is a compact canonical constructor of a record
   */
  public static boolean isCompactCanonicalRecordConstructor(Element elt) {
    return elt.getKind() == ElementKind.CONSTRUCTOR
        && (((Symbol) elt).flags() & Flags_COMPACT_RECORD_CONSTRUCTOR) != 0;
  }

  /**
   * Returns true iff the given element is a resource variable.
   *
   * @param elt an element; may be null, in which case this method always returns false
   * @return true iff the given element represents a resource variable
   */
  public static boolean isResourceVariable(@Nullable Element elt) {
    return elt != null && elt.getKind() == ElementKind.RESOURCE_VARIABLE;
  }

  /**
   * Returns true if the given element is a getter method. A getter method is an instance method
   * with no formal parameters, whose name starts with "get", "is", "not", or "has" followed by an
   * upper-case letter.
   *
   * @param methodElt a method
   * @return true if the given element is a getter method
   */
  public static boolean isGetter(@Nullable ExecutableElement methodElt) {
    if (methodElt == null) {
      return false;
    }
    if (isStatic(methodElt)) {
      return false;
    }
    if (!methodElt.getParameters().isEmpty()) {
      return false;
    }

    // I could check that the method has a non-void return type,
    // and that methods with prefix "is", "has", and "not" return boolean.

    // Constructors and initializers don't have a name starting with a character.
    String name = methodElt.getSimpleName().toString();
    // I expect this code is more efficient than use of a regular expression.
    boolean nameOk =
        nameStartsWith(name, "get")
            || nameStartsWith(name, "is")
            || nameStartsWith(name, "not")
            || nameStartsWith(name, "has");

    if (!nameOk) {
      return false;
    }

    return true;
  }

  /**
   * Returns true if the name starts with the given prefix, followed by an upper-case letter.
   *
   * @param name a name
   * @param prefix a prefix
   * @return true if the name starts with the given prefix, followed by an upper-case letter
   */
  private static boolean nameStartsWith(String name, String prefix) {
    return name.startsWith(prefix)
        && name.length() > prefix.length()
        && Character.isUpperCase(name.charAt(prefix.length()));
  }
}
