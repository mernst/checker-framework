package org.checkerframework.checker.modifiability.shrink;

import org.checkerframework.checker.modifiability.ModifiabilityVisitor;
import org.checkerframework.common.basetype.BaseTypeChecker;

/** Visitor for the {@link ShrinkChecker}. */
public class ShrinkVisitor extends ModifiabilityVisitor {

  /** Create a visitor for the Shrink Checker. */
  public ShrinkVisitor(BaseTypeChecker checker) {
    super(checker);
  }
}
