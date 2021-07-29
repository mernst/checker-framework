package org.checkerframework.common.util;

import javax.lang.model.element.Element;

/**
 * This interface contains a method {@link #isNonNull} that indicates whether an XXX is non-null.
 *
 * <p>This interface enables a checker defined in the framework project to query the Nullness
 * Checker, which is defined in the checker project. The checker project is not visible in the
 * framework project. This interface is implemented by NullnessAnnotatedTypeFactory.
 */
public interface IIsNonNull {

  /**
   * Returns true if the given element is non-null.
   *
   * @param element the element to test
   * @return true if the given element is non-null
   */
  boolean isNonNull(Element element);
}
