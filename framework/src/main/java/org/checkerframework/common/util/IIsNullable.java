/**
 * This interface enables a checker defined in the framework project to query the Nullness Checker,
 * which is defined in the checker project. The checker project is not visible in the framework
 * project. The interface is implemented by NullnessAnnotatedTypeFactory.
 */
public interface IIsNullable {

  /**
   * Returns true if the given XXX may be null, or false if it is definitely non-null.
   *
   * @param XXX
   * @return true if the given XXX may be null
   */
  boolean isNullable(XXX xxx);
}
