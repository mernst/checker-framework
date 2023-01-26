import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Copy of the AinferSibling1 annotation, to test how WPI handles annotations with the same simple
 * name.
 */
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface AinferSibling1 {}
