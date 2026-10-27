/* ******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Max Oesterle - initial API and implementation
 *******************************************************************************/

package tools.vitruv.dsls.vitruvocl.vitruv;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.vitruv.dsls.vitruvocl.common.CompileError;
import tools.vitruv.dsls.vitruvocl.pipeline.FileError;
import tools.vitruv.dsls.vitruvocl.pipeline.Violation;
import tools.vitruv.dsls.vitruvocl.pipeline.VitruvOCL;
import tools.vitruv.dsls.vitruvocl.pipeline.VsumWrapper;
import tools.vitruv.dsls.vitruvocl.vitruv.OclInvariantParser.ParsedInvariant;
import tools.vitruv.framework.vsum.constraints.ConstraintCheckContext;
import tools.vitruv.framework.vsum.constraints.ConstraintChecker;
import tools.vitruv.framework.vsum.constraints.ConstraintDescriptor;
import tools.vitruv.framework.vsum.constraints.ConstraintKind;
import tools.vitruv.framework.vsum.constraints.ConstraintResult;
import tools.vitruv.framework.vsum.constraints.ConstraintViolation;

/**
 * A Vitruv {@link ConstraintChecker} for VitruvOCL constraints. Register it at a virtual model via
 * {@link
 * tools.vitruv.framework.vsum.VirtualModelBuilder#withConstraintChecker(ConstraintChecker)}.
 *
 * <p>Each invariant is a constraint. Its id is the context type and the invariant name, e.g.
 * {@code shop::Item::nonNegativePrice}, so invariant names only need to be unique per context, as
 * in OCL. Unnamed invariants are numbered per context in declaration order ({@code
 * shop::Item#1}); name invariants to get ids that do not change when constraints are added.
 *
 * <p>Unlike {@link VitruvOCL#registerVSUM}, the checker does not register a VSUM globally.
 * Metamodels are still resolved by the name of their package, both from the global {@link
 * org.eclipse.emf.ecore.EPackage.Registry} and from the models of the checked virtual model.
 */
public final class VitruvOclConstraintChecker implements ConstraintChecker {

  /** The language identifier of VitruvOCL constraints. */
  public static final String LANGUAGE = "vitruvocl";

  private final Map<String, ConstraintDescriptor> constraints = new LinkedHashMap<>();

  private VitruvOclConstraintChecker(List<ParsedInvariant> invariants) {
    Map<String, Integer> unnamedPerContext = new LinkedHashMap<>();
    for (ParsedInvariant invariant : invariants) {
      String id;
      String name;
      if (invariant.name() == null) {
        int index = unnamedPerContext.merge(invariant.contextType(), 1, Integer::sum);
        id = invariant.contextType() + "#" + index;
        name = id;
      } else {
        id = invariant.contextType() + "::" + invariant.name();
        name = invariant.name();
      }
      if (constraints.containsKey(id)) {
        throw new IllegalArgumentException(
            "Duplicate OCL invariant " + name + " in context " + invariant.contextType());
      }
      constraints.put(
          id,
          new ConstraintDescriptor(
              id,
              name,
              invariant.contextType(),
              ConstraintKind.INVARIANT,
              invariant.severity(),
              LANGUAGE,
              invariant.source()));
    }
  }

  /**
   * Creates a checker for the given constraints.
   *
   * @param content the content of a {@code .ocl} file
   * @return the checker
   * @throws IllegalArgumentException if the content contains syntax errors or duplicate names
   */
  public static VitruvOclConstraintChecker fromString(String content) {
    return new VitruvOclConstraintChecker(OclInvariantParser.parse(content));
  }

  /**
   * Creates a checker for the constraints of the given files.
   *
   * @param files the {@code .ocl} files
   * @return the checker
   * @throws UncheckedIOException if a file cannot be read
   * @throws IllegalArgumentException if a file contains syntax errors or names are duplicated
   */
  public static VitruvOclConstraintChecker fromFiles(Path... files) {
    List<ParsedInvariant> invariants = new ArrayList<>();
    for (Path file : files) {
      try {
        invariants.addAll(OclInvariantParser.parse(Files.readString(file)));
      } catch (IOException e) {
        throw new UncheckedIOException("Could not read OCL constraints from " + file, e);
      }
    }
    return new VitruvOclConstraintChecker(invariants);
  }

  /**
   * Creates a checker for the constraints of the given classpath resources, loaded with the
   * context class loader of the current thread.
   *
   * @param resourceNames the names of the {@code .ocl} resources, e.g. {@code constraints.ocl}
   * @return the checker
   * @throws IllegalArgumentException if a resource does not exist, contains syntax errors or names
   *     are duplicated
   */
  public static VitruvOclConstraintChecker fromClasspath(String... resourceNames) {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if (classLoader == null) {
      classLoader = VitruvOclConstraintChecker.class.getClassLoader();
    }
    List<ParsedInvariant> invariants = new ArrayList<>();
    for (String resourceName : resourceNames) {
      try (InputStream stream = classLoader.getResourceAsStream(resourceName)) {
        if (stream == null) {
          throw new IllegalArgumentException(
              "OCL constraints not found on classpath: " + resourceName);
        }
        invariants.addAll(
            OclInvariantParser.parse(new String(stream.readAllBytes(), StandardCharsets.UTF_8)));
      } catch (IOException e) {
        throw new UncheckedIOException("Could not read OCL constraints " + resourceName, e);
      }
    }
    return new VitruvOclConstraintChecker(invariants);
  }

  @Override
  public List<ConstraintDescriptor> getConstraints() {
    return List.copyOf(constraints.values());
  }

  /**
   * {@inheritDoc}
   *
   * <p>Unknown ids are ignored. A failure while evaluating one constraint is reported as an error
   * of that constraint only.
   */
  @Override
  public List<ConstraintResult> check(
      ConstraintCheckContext context, Collection<String> constraintIds) {
    // a fresh wrapper per check sees all metamodels registered up to now
    VsumWrapper wrapper = new VsumWrapper(context);
    List<ConstraintResult> results = new ArrayList<>();
    for (String id : constraintIds) {
      ConstraintDescriptor descriptor = constraints.get(id);
      if (descriptor != null) {
        results.add(evaluate(descriptor, wrapper));
      }
    }
    return results;
  }

  private static ConstraintResult evaluate(ConstraintDescriptor descriptor, VsumWrapper wrapper) {
    try {
      return toVitruvResult(
          descriptor, VitruvOCL.evaluateConstraint(descriptor.source(), wrapper));
    } catch (RuntimeException | StackOverflowError e) {
      String reason = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
      return ConstraintResult.error(descriptor, List.of("Evaluation failed: " + reason));
    }
  }

  private static ConstraintResult toVitruvResult(
      ConstraintDescriptor descriptor,
      tools.vitruv.dsls.vitruvocl.pipeline.ConstraintResult oclResult) {
    if (!oclResult.isSuccess()) {
      List<String> errors = new ArrayList<>();
      for (CompileError error : oclResult.getCompilerErrors()) {
        errors.add(error.getLine() + ":" + error.getColumn() + " " + error.getMessage());
      }
      for (FileError error : oclResult.getFileErrors()) {
        errors.add(error.toString());
      }
      return ConstraintResult.error(descriptor, errors);
    }
    List<ConstraintViolation> violations = new ArrayList<>();
    for (Violation violation : oclResult.getViolations()) {
      violations.add(
          new ConstraintViolation(
              violation.instance(),
              OclInvariantParser.toSeverity(violation.severity()),
              violation.message()));
    }
    if (!oclResult.isSatisfied() && violations.isEmpty()) {
      return ConstraintResult.violated(descriptor, List.of());
    }
    return ConstraintResult.of(descriptor, violations);
  }
}
