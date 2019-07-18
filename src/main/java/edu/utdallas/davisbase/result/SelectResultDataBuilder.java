package edu.utdallas.davisbase.result;

/**
 * A mutable builder of one {@link SelectResultData} instance.
 *
 * Intended to enable <i>incremental</i> building of a {@code SelectResultData} instance so that:
 * <ol>
 *   <li>The set of constituent {@link SelectResultDataRow}s--which may be large--need not</i> be
 *       wholly present in memory at the moment a {@code SelectResultData} is constructed.</li>
 *   <li>The execution of a {@link edu.utdallas.davisbase.command.SelectCommand SelectCommand}
 *       may proceed in a simple online fashion without the extraneous responsibility of managing
 *       the result set size.</li>
 * </ol>
 * Taken together, these features not only overcome runtime environment memory size limitations,
 * but also simplify the command execution logic by correctly separating the responsibility of
 * generating a result set through a {@code SelectCommand} from the responsibility of scalably
 * managing the internal data structure of the result set.
 */
public class SelectResultDataBuilder {

  // TODO Implement SelectResultDataBuilder
}
