package edu.utdallas.davisbase.compiler;

import edu.utdallas.davisbase.NotImplementedException;
import edu.utdallas.davisbase.command.Command;
import edu.utdallas.davisbase.representation.CommandRepresentation;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A compiler of {@link edu.utdallas.davisbase.representation.CommandRepresentation CommandRepresentation} to
 * {@link edu.utdallas.davisbase.command.Command Command}.
 */
public class Compiler {

  protected final CompilerConfiguration configuration;

  public Compiler(CompilerConfiguration configuration) {
    checkNotNull(configuration);
    this.configuration = configuration;
  }

  public Command compile(CommandRepresentation command) throws CompileException {
    // TODO Implement Compiler.compile(CommandRepresentation)
    throw new NotImplementedException();
  }
}
