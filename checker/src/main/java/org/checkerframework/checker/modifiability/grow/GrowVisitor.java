package org.checkerframework.checker.modifiability.grow;

import org.checkerframework.checker.modifiability.ModifiabilityVisitor;
import org.checkerframework.common.basetype.BaseTypeChecker;

/** Visitor for the {@link GrowChecker}. */
public class GrowVisitor extends ModifiabilityVisitor {

  /** Create a visitor for the Grow Checker. */
  public GrowVisitor(BaseTypeChecker checker) {
    super(checker);
  }
}
