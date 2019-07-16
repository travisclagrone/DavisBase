package edu.utdallas.davisbase.compiler;

import static com.google.common.base.Preconditions.checkNotNull;

import edu.utdallas.davisbase.NotImplementedException;
import edu.utdallas.davisbase.command.Command;
import edu.utdallas.davisbase.parser.Ast;

/**
 * A compiler of {@link edu.utdallas.davisbase.parser.Ast Ast} to
 * {@link edu.utdallas.davisbase.command.Command Command}.
 */
public class Compiler {

  protected final CompilerConfiguration configuration;

  public Compiler(CompilerConfiguration configuration) {
    checkNotNull(configuration);
    this.configuration = configuration;
  }

  public Command compile(Ast ast) throws CompileException {
    // TODO Implement Compiler.compile(Ast)
    throw new NotImplementedException();
  }
}
