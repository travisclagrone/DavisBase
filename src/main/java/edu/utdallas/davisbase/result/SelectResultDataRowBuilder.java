package edu.utdallas.davisbase.result;

/**
 * A mutable builder of one {@link SelectResultDataRow} instance.
 *
 * Intended to enforce type safety of {@code SelectResultDataRow} member values. This is necessary
 * because the most derived common base class of the set of Java classes corresponding to the
 * DavisBase {@link edu.utdallas.davisbase.DataType DataType}s is {@code Object}, and so any build
 * process that relies on the invocation of a single method signature (such as a public constructor)
 * is unavoidably relegated to dynamic run-time type checking rather than compile-time type safety.
 * {@link SelectResultDataRowBuilder} achieves compile-time type safety by presenting a complete set
 * of strongly-typed incremental build methods, and <i>no</i> method that accepts arbitrary-typed
 * {@code Object}s.
 */
public class SelectResultDataRowBuilder {

  // TODO Implement SelectResultDataRowBuilder
}
