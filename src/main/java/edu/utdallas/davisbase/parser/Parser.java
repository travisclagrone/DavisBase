package edu.utdallas.davisbase.parser;

import static com.google.common.base.Preconditions.checkNotNull;

import edu.utdallas.davisbase.NotImplementedException;

public class Parser {

  protected final ParserConfiguration configuration;

  public Parser(ParserConfiguration configuration) {
    checkNotNull(configuration);
    this.configuration = configuration;
  }

  /**
   * @param statement a single complete statement to parse
   * @return the {@link Ast} representation of <code>statement</code>
   * @throws ParseException if <code>statement</code> is not a single complete statement that is
   *                        both lexically and syntactically correct
   */
  public Ast parse(String statement) throws ParseException {
    // TODO Implement Parser.parse(String)
    throw new NotImplementedException();
  }

}
