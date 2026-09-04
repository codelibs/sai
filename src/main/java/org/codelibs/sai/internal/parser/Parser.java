/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2026, CodeLibs Project and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.codelibs.sai.internal.parser;

import static org.codelibs.sai.internal.codegen.CompilerConstants.ANON_FUNCTION_PREFIX;
import static org.codelibs.sai.internal.codegen.CompilerConstants.EVAL;
import static org.codelibs.sai.internal.codegen.CompilerConstants.PROGRAM;
import static org.codelibs.sai.internal.parser.TokenType.ARROW;
import static org.codelibs.sai.internal.parser.TokenType.ASSIGN;
import static org.codelibs.sai.internal.parser.TokenType.CASE;
import static org.codelibs.sai.internal.parser.TokenType.CLASS;
import static org.codelibs.sai.internal.parser.TokenType.CATCH;
import static org.codelibs.sai.internal.parser.TokenType.COLON;
import static org.codelibs.sai.internal.parser.TokenType.COMMARIGHT;
import static org.codelibs.sai.internal.parser.TokenType.CONST;
import static org.codelibs.sai.internal.parser.TokenType.DECPOSTFIX;
import static org.codelibs.sai.internal.parser.TokenType.DECPREFIX;
import static org.codelibs.sai.internal.parser.TokenType.ELSE;
import static org.codelibs.sai.internal.parser.TokenType.EOF;
import static org.codelibs.sai.internal.parser.TokenType.EXTENDS;
import static org.codelibs.sai.internal.parser.TokenType.ELLIPSIS;
import static org.codelibs.sai.internal.parser.TokenType.EOL;
import static org.codelibs.sai.internal.parser.TokenType.FINALLY;
import static org.codelibs.sai.internal.parser.TokenType.FUNCTION;
import static org.codelibs.sai.internal.parser.TokenType.IDENT;
import static org.codelibs.sai.internal.parser.TokenType.IF;
import static org.codelibs.sai.internal.parser.TokenType.INCPOSTFIX;
import static org.codelibs.sai.internal.parser.TokenType.LBRACE;
import static org.codelibs.sai.internal.parser.TokenType.LBRACKET;
import static org.codelibs.sai.internal.parser.TokenType.LET;
import static org.codelibs.sai.internal.parser.TokenType.LPAREN;
import static org.codelibs.sai.internal.parser.TokenType.RBRACE;
import static org.codelibs.sai.internal.parser.TokenType.RBRACKET;
import static org.codelibs.sai.internal.parser.TokenType.RPAREN;
import static org.codelibs.sai.internal.parser.TokenType.SEMICOLON;
import static org.codelibs.sai.internal.parser.TokenType.SUPER;
import static org.codelibs.sai.internal.parser.TokenType.TEMPLATE;
import static org.codelibs.sai.internal.parser.TokenType.TEMPLATE_HEAD;
import static org.codelibs.sai.internal.parser.TokenType.TEMPLATE_MIDDLE;
import static org.codelibs.sai.internal.parser.TokenType.TEMPLATE_TAIL;
import static org.codelibs.sai.internal.parser.TokenType.TERNARY;
import static org.codelibs.sai.internal.parser.TokenType.WHILE;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codelibs.sai.internal.codegen.CompilerConstants;
import org.codelibs.sai.internal.codegen.Namespace;
import org.codelibs.sai.internal.dynalink.support.NameCodec;
import org.codelibs.sai.internal.ir.AccessNode;
import org.codelibs.sai.internal.ir.BaseNode;
import org.codelibs.sai.internal.ir.BinaryNode;
import org.codelibs.sai.internal.ir.Block;
import org.codelibs.sai.internal.ir.BlockLexicalContext;
import org.codelibs.sai.internal.ir.BlockStatement;
import org.codelibs.sai.internal.ir.BreakNode;
import org.codelibs.sai.internal.ir.BreakableNode;
import org.codelibs.sai.internal.ir.CallNode;
import org.codelibs.sai.internal.ir.CaseNode;
import org.codelibs.sai.internal.ir.CatchNode;
import org.codelibs.sai.internal.ir.ContinueNode;
import org.codelibs.sai.internal.ir.EmptyNode;
import org.codelibs.sai.internal.ir.Expression;
import org.codelibs.sai.internal.ir.ExpressionStatement;
import org.codelibs.sai.internal.ir.ForNode;
import org.codelibs.sai.internal.ir.FunctionNode;
import org.codelibs.sai.internal.ir.IdentNode;
import org.codelibs.sai.internal.ir.IfNode;
import org.codelibs.sai.internal.ir.IndexNode;
import org.codelibs.sai.internal.ir.JoinPredecessorExpression;
import org.codelibs.sai.internal.ir.LabelNode;
import org.codelibs.sai.internal.ir.LexicalContext;
import org.codelibs.sai.internal.ir.LiteralNode;
import org.codelibs.sai.internal.ir.LoopNode;
import org.codelibs.sai.internal.ir.Node;
import org.codelibs.sai.internal.ir.ObjectNode;
import org.codelibs.sai.internal.ir.PropertyKey;
import org.codelibs.sai.internal.ir.PropertyNode;
import org.codelibs.sai.internal.ir.ReturnNode;
import org.codelibs.sai.internal.ir.RuntimeNode;
import org.codelibs.sai.internal.ir.Statement;
import org.codelibs.sai.internal.ir.SwitchNode;
import org.codelibs.sai.internal.ir.TernaryNode;
import org.codelibs.sai.internal.ir.ThrowNode;
import org.codelibs.sai.internal.ir.TryNode;
import org.codelibs.sai.internal.ir.UnaryNode;
import org.codelibs.sai.internal.ir.VarNode;
import org.codelibs.sai.internal.ir.WhileNode;
import org.codelibs.sai.internal.ir.WithNode;
import org.codelibs.sai.internal.ir.debug.ASTWriter;
import org.codelibs.sai.internal.ir.debug.PrintVisitor;
import org.codelibs.sai.internal.runtime.Context;
import org.codelibs.sai.internal.runtime.ErrorManager;
import org.codelibs.sai.internal.runtime.JSErrorType;
import org.codelibs.sai.internal.runtime.ParserException;
import org.codelibs.sai.internal.runtime.RecompilableScriptFunctionData;
import org.codelibs.sai.internal.runtime.ScriptEnvironment;
import org.codelibs.sai.internal.runtime.ScriptRuntime;
import org.codelibs.sai.internal.runtime.ScriptingFunctions;
import org.codelibs.sai.internal.runtime.Source;
import org.codelibs.sai.internal.runtime.Timing;
import org.codelibs.sai.internal.runtime.logging.DebugLogger;
import org.codelibs.sai.internal.runtime.logging.Loggable;
import org.codelibs.sai.internal.runtime.logging.Logger;

/**
 * Builds the IR.
 */
@Logger(name = "parser")
public class Parser extends AbstractParser implements Loggable {
    private static final String ARGUMENTS_NAME = CompilerConstants.ARGUMENTS_VAR.symbolName();

    /** Current env. */
    private final ScriptEnvironment env;

    /** Is scripting mode. */
    private final boolean scripting;

    private List<Statement> functionDeclarations;

    /**
     * Name of the binding that carries {@code this} into arrow functions. Internal
     * names start with a colon so that they cannot collide with a source identifier.
     */
    private static final String ARROW_THIS = ":arrowthis";

    /**
     * Prefix of the temporaries the parser introduces while desugaring. Internal
     * names start with a colon so that they cannot collide with a source identifier.
     */
    private static final String TEMPORARY_PREFIX = ":pt";

    /**
     * Prefix of the name given to a parameter that is written as a pattern. Unlike a
     * temporary it is a real parameter, so it is not declared in the body.
     */
    private static final String PATTERN_PARAMETER_PREFIX = ":pp";

    /**
     * Name of the binding holding the superclass of the class being parsed.
     *
     * It is a fixed name rather than a numbered temporary because a method that uses
     * super is re-parsed from its own source, where the class is not in sight: the name
     * it emits then has to be the one the class assigned, and a counter would not
     * survive. A class declaration puts the binding in a block of its own so that two
     * classes in the same scope cannot share it.
     */
    private static final String SUPERCLASS = ":superclass";

    /** Set while parsing a class with a superclass. */
    private boolean inSubclass;

    /** Set while super may actually be used, which a class expression cannot allow. */
    private boolean superUsable;

    /** Names of the temporaries the function currently being parsed has to declare. */
    private List<String> temporaries;

    /** Counter behind {@link #newTemporary()}, unique across one parse. */
    private int temporaryCount;

    private final BlockLexicalContext lc = new BlockLexicalContext();
    private final Deque<Object> defaultNames = new ArrayDeque<>();

    /** Namespace for function names where not explicitly given */
    private final Namespace namespace;

    private final DebugLogger log;

    /** to receive line information from Lexer when scanning multine literals. */
    protected final Lexer.LineInfoReceiver lineInfoReceiver;

    private RecompilableScriptFunctionData reparsedFunction;

    /** Template substitutions currently being parsed, @see #skipFunctionBody. */
    private int templateSubstitutions;

    /**
     * Constructor
     *
     * @param env     script environment
     * @param source  source to parse
     * @param errors  error manager
     */
    public Parser(final ScriptEnvironment env, final Source source, final ErrorManager errors) {
        this(env, source, errors, env._strict, null);
    }

    /**
     * Constructor
     *
     * @param env     script environment
     * @param source  source to parse
     * @param errors  error manager
     * @param strict  strict
     * @param log debug logger if one is needed
     */
    public Parser(final ScriptEnvironment env, final Source source, final ErrorManager errors, final boolean strict, final DebugLogger log) {
        this(env, source, errors, strict, 0, log);
    }

    /**
     * Construct a parser.
     *
     * @param env     script environment
     * @param source  source to parse
     * @param errors  error manager
     * @param strict  parser created with strict mode enabled.
     * @param lineOffset line offset to start counting lines from
     * @param log debug logger if one is needed
     */
    public Parser(final ScriptEnvironment env, final Source source, final ErrorManager errors, final boolean strict, final int lineOffset,
            final DebugLogger log) {
        super(source, errors, strict, lineOffset);
        this.env = env;
        this.namespace = new Namespace(env.getNamespace());
        this.scripting = env._scripting;
        if (this.scripting) {
            this.lineInfoReceiver = new Lexer.LineInfoReceiver() {
                @Override
                public void lineInfo(final int receiverLine, final int receiverLinePosition) {
                    // update the parser maintained line information
                    Parser.this.line = receiverLine;
                    Parser.this.linePosition = receiverLinePosition;
                }
            };
        } else {
            // non-scripting mode script can't have multi-line literals
            this.lineInfoReceiver = null;
        }

        this.log = log == null ? DebugLogger.DISABLED_LOGGER : log;
    }

    @Override
    public DebugLogger getLogger() {
        return log;
    }

    @Override
    public DebugLogger initLogger(final Context context) {
        return context.getLogger(this.getClass());
    }

    /**
     * Sets the name for the first function. This is only used when reparsing anonymous functions to ensure they can
     * preserve their already assigned name, as that name doesn't appear in their source text.
     * @param name the name for the first parsed function.
     */
    public void setFunctionName(final String name) {
        defaultNames.push(createIdentNode(0, 0, name));
    }

    /**
     * Sets the {@link RecompilableScriptFunctionData} representing the function being reparsed (when this
     * parser instance is used to reparse a previously parsed function, as part of its on-demand compilation).
     * This will trigger various special behaviors, such as skipping nested function bodies.
     * @param reparsedFunction the function being reparsed.
     */
    public void setReparsedFunction(final RecompilableScriptFunctionData reparsedFunction) {
        this.reparsedFunction = reparsedFunction;
    }

    /**
     * Execute parse and return the resulting function node.
     * Errors will be thrown and the error manager will contain information
     * if parsing should fail
     *
     * This is the default parse call, which will name the function node
     * {code :program} {@link CompilerConstants#PROGRAM}
     *
     * @return function node resulting from successful parse
     */
    public FunctionNode parse() {
        return parse(PROGRAM.symbolName(), 0, source.getLength(), ProgramKind.NORMAL);
    }

    /**
     * What a parsed range is expected to hold.
     *
     * A function is re-parsed from the source range it was written in, and neither a
     * property accessor nor a method definition is a program in its own right, so the
     * parser has to be told which one to expect.
     */
    public enum ProgramKind {
        /** An ordinary script. */
        NORMAL,
        /** A single property getter or setter, {@code get x() {}}. */
        PROPERTY_ACCESSOR,
        /** A single method definition, {@code m() {}}. */
        METHOD,
        /**
         * The method definition a class wrote out as its constructor. Its range is an
         * ordinary method's, but it has to be told apart from one so that the check
         * rejecting a call without {@code new} is put back on re-parsing.
         */
        CONSTRUCTOR_METHOD,
        /**
         * A whole class, of which only the constructor it did not write out is wanted.
         * That constructor has no source of its own, so the class it belongs to is its
         * range and the rest of the class is parsed and thrown away.
         */
        CLASS_CONSTRUCTOR
    }

    /**
     * Execute parse and return the resulting function node.
     * Errors will be thrown and the error manager will contain information
     * if parsing should fail
     *
     * This should be used to create one and only one function node
     *
     * @param scriptName name for the script, given to the parsed FunctionNode
     * @param startPos start position in source
     * @param len length of parse
     * @param programKind what the range is expected to hold. A property accessor or a method definition is not a
     * program in its own right, so re-parsing one has to say which it is.
     *
     * @return function node resulting from successful parse
     */
    public FunctionNode parse(final String scriptName, final int startPos, final int len, final ProgramKind programKind) {
        final boolean isTimingEnabled = env.isTimingEnabled();
        final long t0 = isTimingEnabled ? System.nanoTime() : 0L;
        log.info(this, " begin for '", scriptName, "'");

        try {
            stream = new TokenStream();
            lexer = new Lexer(source, startPos, len, stream, scripting && !env._no_syntax_extensions, reparsedFunction != null,
                    env._es6);
            lexer.line = lexer.pendingLine = lineOffset + 1;
            line = lineOffset;

            // Set up first token (skips opening EOL.)
            k = -1;
            next();

            // A function being re-parsed sits inside a class it cannot see from its own
            // source. The original parse already checked that super was allowed here, and
            // the binding it emits resolves through the scope the function closes over,
            // so allow it again. An arrow is not a method or a constructor and so has no
            // program kind of its own, but it is re-parsed the same way and borrows the
            // super of the method around it, so being re-parsed at all is what counts.
            superUsable = programKind != ProgramKind.NORMAL || reparsedFunction != null;

            // Begin parse.
            return program(scriptName, programKind);
        } catch (final Exception e) {
            handleParseException(e);

            return null;
        } finally {
            final String end = this + " end '" + scriptName + "'";
            if (isTimingEnabled) {
                env._timing.accumulateTime(toString(), System.nanoTime() - t0);
                log.info(end, "' in ", Timing.toMillisPrint(System.nanoTime() - t0), " ms");
            } else {
                log.info(end);
            }
        }
    }

    /**
     * Parse and return the list of function parameter list. A comma
     * separated list of function parameter identifiers is expected to be parsed.
     * Errors will be thrown and the error manager will contain information
     * if parsing should fail. This method is used to check if parameter Strings
     * passed to "Function" constructor is a valid or not.
     *
     * @return the list of IdentNodes representing the formal parameter list
     */
    public List<IdentNode> parseFormalParameterList() {
        try {
            stream = new TokenStream();
            lexer = new Lexer(source, stream, scripting && !env._no_syntax_extensions, env._es6);
            final int functionLine = line;

            // Set up first token (skips opening EOL.)
            k = -1;
            next();

            // A default value, a pattern and a rest binding are all read only when
            // formalParameterList is given somewhere to put the setup code the body would
            // run, since that is what applies them. There is no body to check here and
            // the setups are discarded, but withholding the collector would reject the
            // very syntax this is meant to accept - the function itself is then built by
            // evaluating a function expression, which has always understood these forms.
            //
            // Reading them needs a function on the stack: they are expressions, and the
            // expression parser asks the lexical context which function it is in. Open
            // the same throwaway program node parseFunctionBody opens.
            final long functionToken = Token.toDesc(FUNCTION, 0, source.getLength());
            FunctionNode function = newFunctionNode(functionToken,
                    new IdentNode(functionToken, Token.descPosition(functionToken), PROGRAM.symbolName()),
                    new ArrayList<IdentNode>(), FunctionNode.Kind.NORMAL, functionLine);

            final Parameters parameters = new Parameters();
            formalParameterList(TokenType.EOF, parameters);
            expect(TokenType.EOF);

            function.setFinish(source.getLength() - 1);
            restoreFunctionNode(function, token);

            return parameters.list;
        } catch (final Exception e) {
            handleParseException(e);
            return null;
        }
    }

    /**
     * Execute parse and return the resulting function node.
     * Errors will be thrown and the error manager will contain information
     * if parsing should fail. This method is used to check if code String
     * passed to "Function" constructor is a valid function body or not.
     *
     * @return function node resulting from successful parse
     */
    public FunctionNode parseFunctionBody() {
        try {
            stream = new TokenStream();
            lexer = new Lexer(source, stream, scripting && !env._no_syntax_extensions, env._es6);
            final int functionLine = line;

            // Set up first token (skips opening EOL.)
            k = -1;
            next();

            // Make a fake token for the function.
            final long functionToken = Token.toDesc(FUNCTION, 0, source.getLength());
            // Set up the function to append elements.

            FunctionNode function =
                    newFunctionNode(functionToken, new IdentNode(functionToken, Token.descPosition(functionToken), PROGRAM.symbolName()),
                            new ArrayList<IdentNode>(), FunctionNode.Kind.NORMAL, functionLine);

            functionDeclarations = new ArrayList<>();
            sourceElements(ProgramKind.NORMAL);
            addFunctionDeclarations(function);
            functionDeclarations = null;

            expect(EOF);

            function.setFinish(source.getLength() - 1);
            function = restoreFunctionNode(function, token); //commit code
            function = function.setBody(lc, function.getBody().setNeedsScope(lc));

            printAST(function);
            return function;
        } catch (final Exception e) {
            handleParseException(e);
            return null;
        }
    }

    private void handleParseException(final Exception e) {
        // Extract message from exception.  The message will be in error
        // message format.
        String message = e.getMessage();

        // If empty message.
        if (message == null) {
            message = e.toString();
        }

        // Issue message.
        if (e instanceof ParserException) {
            errors.error((ParserException) e);
        } else {
            errors.error(message);
        }

        if (env._dump_on_error) {
            e.printStackTrace(env.getErr());
        }
    }

    /**
     * Skip to a good parsing recovery point.
     */
    private void recover(final Exception e) {
        if (e != null) {
            // Extract message from exception.  The message will be in error
            // message format.
            String message = e.getMessage();

            // If empty message.
            if (message == null) {
                message = e.toString();
            }

            // Issue message.
            if (e instanceof ParserException) {
                errors.error((ParserException) e);
            } else {
                errors.error(message);
            }

            if (env._dump_on_error) {
                e.printStackTrace(env.getErr());
            }
        }

        // Skip to a recovery point.
        loop: while (true) {
            switch (type) {
            case EOF:
                // Can not go any further.
                break loop;
            case EOL:
            case SEMICOLON:
            case RBRACE:
                // Good recovery points.
                next();
                break loop;
            default:
                // So we can recover after EOL.
                nextOrEOL();
                break;
            }
        }
    }

    /**
     * Set up a new block.
     *
     * @return New block.
     */
    private Block newBlock() {
        return lc.push(new Block(token, Token.descPosition(token)));
    }

    /**
     * Set up a new function block.
     *
     * @param ident Name of function.
     * @return New block.
     */
    private FunctionNode newFunctionNode(final long startToken, final IdentNode ident, final List<IdentNode> parameters,
            final FunctionNode.Kind kind, final int functionLine) {
        return newFunctionNode(token, startToken, ident, parameters, kind, functionLine);
    }

    /**
     * @param functionToken token the function is identified by. A function is
     *                      identified by where it starts in the source, so a synthetic
     *                      one has to be given a position inside the range it will be
     *                      re-parsed from rather than wherever the parser happens to be.
     */
    private FunctionNode newFunctionNode(final long functionToken, final long startToken, final IdentNode ident,
            final List<IdentNode> parameters, final FunctionNode.Kind kind, final int functionLine) {
        // Build function name.
        final StringBuilder sb = new StringBuilder();

        final FunctionNode parentFunction = lc.getCurrentFunction();
        if (parentFunction != null && !parentFunction.isProgram()) {
            sb.append(parentFunction.getName()).append(CompilerConstants.NESTED_FUNCTION_SEPARATOR.symbolName());
        }

        assert ident.getName() != null;
        sb.append(ident.getName());

        final String name = namespace.uniqueName(sb.toString());
        assert parentFunction != null || name.equals(PROGRAM.symbolName())
                || name.startsWith(RecompilableScriptFunctionData.RECOMPILATION_PREFIX) : "name = " + name;

        int flags = 0;
        if (isStrictMode) {
            flags |= FunctionNode.IS_STRICT;
        }
        if (parentFunction == null) {
            flags |= FunctionNode.IS_PROGRAM;
        }

        // Start new block.
        final FunctionNode functionNode =
                new FunctionNode(source, functionLine, functionToken, Token.descPosition(functionToken), startToken, namespace, ident,
                        name, parameters, kind, flags);

        lc.push(functionNode);
        // Create new block, and just put it on the context stack, restoreFunctionNode() will associate it with the
        // FunctionNode.
        newBlock();

        return functionNode;
    }

    /**
     * Restore the current block.
     */
    private Block restoreBlock(final Block block) {
        return lc.pop(block);
    }

    private FunctionNode restoreFunctionNode(final FunctionNode functionNode, final long lastToken) {
        final Block newBody = restoreBlock(lc.getFunctionBody(functionNode));

        return lc.pop(functionNode).setBody(lc, newBody).setLastToken(lc, lastToken);
    }

    /**
     * Get the statements in a block.
     * @return Block statements.
     */
    private Block getBlock(final boolean needsBraces) {
        // Set up new block. Captures LBRACE.
        Block newBlock = newBlock();
        try {
            // Block opening brace.
            if (needsBraces) {
                expect(LBRACE);
            }
            // Accumulate block statements.
            statementList();

        } finally {
            newBlock = restoreBlock(newBlock);
        }

        final int possibleEnd = Token.descPosition(token) + Token.descLength(token);

        // Block closing brace.
        if (needsBraces) {
            expect(RBRACE);
        }

        newBlock.setFinish(possibleEnd);

        return newBlock;
    }

    /**
     * Get all the statements generated by a single statement.
     * @return Statements.
     */
    private Block getStatement() {
        if (type == LBRACE) {
            return getBlock(true);
        }
        // Set up new block. Captures first token.
        Block newBlock = newBlock();
        try {
            statement(false, ProgramKind.NORMAL, true);
        } finally {
            newBlock = restoreBlock(newBlock);
        }
        return newBlock;
    }

    /**
     * Detect calls to special functions.
     * @param ident Called function.
     */
    private void detectSpecialFunction(final IdentNode ident) {
        final String name = ident.getName();

        if (EVAL.symbolName().equals(name)) {
            markEval(lc);
        }
    }

    /**
     * Detect use of special properties.
     * @param ident Referenced property.
     */
    private void detectSpecialProperty(final IdentNode ident) {
        if (isArguments(ident)) {
            if (lc.getCurrentFunction().getKind() == FunctionNode.Kind.ARROW) {
                throw error(AbstractParser.message("no.arguments.in.arrow"), ident.getToken());
            }
            lc.setFlag(lc.getCurrentFunction(), FunctionNode.USES_ARGUMENTS);
        }
    }

    private boolean useBlockScope() {
        return isES6();
    }

    private boolean isES6() {
        return env._es6;
    }

    private static boolean isArguments(final String name) {
        return ARGUMENTS_NAME.equals(name);
    }

    private static boolean isArguments(final IdentNode ident) {
        return isArguments(ident.getName());
    }

    /**
     * Tells whether a IdentNode can be used as L-value of an assignment
     *
     * @param ident IdentNode to be checked
     * @return whether the ident can be used as L-value
     */
    private static boolean checkIdentLValue(final IdentNode ident) {
        return ident.tokenType().getKind() != TokenKind.KEYWORD;
    }

    /**
     * Verify an assignment expression.
     * @param op  Operation token.
     * @param lhs Left hand side expression.
     * @param rhs Right hand side expression.
     * @return Verified expression.
     */
    private Expression verifyAssignment(final long op, final Expression lhs, final Expression rhs) {
        final TokenType opType = Token.descType(op);

        switch (opType) {
        case ASSIGN:
        case ASSIGN_ADD:
        case ASSIGN_BIT_AND:
        case ASSIGN_BIT_OR:
        case ASSIGN_BIT_XOR:
        case ASSIGN_DIV:
        case ASSIGN_MOD:
        case ASSIGN_MUL:
        case ASSIGN_SAR:
        case ASSIGN_SHL:
        case ASSIGN_SHR:
        case ASSIGN_SUB:
            if (!(lhs instanceof AccessNode || lhs instanceof IndexNode || lhs instanceof IdentNode)) {
                return referenceError(lhs, rhs, env._early_lvalue_error);
            }

            if (lhs instanceof IdentNode) {
                if (!checkIdentLValue((IdentNode) lhs)) {
                    return referenceError(lhs, rhs, false);
                }
                verifyStrictIdent((IdentNode) lhs, "assignment");
            }
            break;

        default:
            break;
        }

        // Build up node.
        if (BinaryNode.isLogical(opType)) {
            return new BinaryNode(op, new JoinPredecessorExpression(lhs), new JoinPredecessorExpression(rhs));
        }
        return new BinaryNode(op, lhs, rhs);
    }

    /**
     * Reduce increment/decrement to simpler operations.
     * @param firstToken First token.
     * @param tokenType  Operation token (INCPREFIX/DEC.)
     * @param expression Left hand side expression.
     * @param isPostfix  Prefix or postfix.
     * @return           Reduced expression.
     */
    private static UnaryNode incDecExpression(final long firstToken, final TokenType tokenType, final Expression expression,
            final boolean isPostfix) {
        if (isPostfix) {
            return new UnaryNode(Token.recast(firstToken, tokenType == DECPREFIX ? DECPOSTFIX : INCPOSTFIX), expression.getStart(),
                    Token.descPosition(firstToken) + Token.descLength(firstToken), expression);
        }

        return new UnaryNode(firstToken, expression);
    }

    /**
     * -----------------------------------------------------------------------
     *
     * Grammar based on
     *
     *      ECMAScript Language Specification
     *      ECMA-262 5th Edition / December 2009
     *
     * -----------------------------------------------------------------------
     */

    /**
     * Program :
     *      SourceElements?
     *
     * See 14
     *
     * Parse the top level script.
     */
    private FunctionNode program(final String scriptName, final ProgramKind programKind) {
        // Make a pseudo-token for the script holding its start and length.
        final long functionToken = Token.toDesc(FUNCTION, Token.descPosition(Token.withDelimiter(token)), source.getLength());
        final int functionLine = line;
        // Set up the script to append elements.

        FunctionNode script =
                newFunctionNode(functionToken, new IdentNode(functionToken, Token.descPosition(functionToken), scriptName),
                        new ArrayList<IdentNode>(), FunctionNode.Kind.SCRIPT, functionLine);

        restoreArrowThis(script);

        functionDeclarations = new ArrayList<>();
        temporaries = new ArrayList<>();
        sourceElements(programKind);
        addFunctionDeclarations(script);
        declareArrowThis(script);
        declareTemporaries(script);
        functionDeclarations = null;

        expect(EOF);

        script.setFinish(source.getLength() - 1);

        script = restoreFunctionNode(script, token); //commit code
        script = script.setBody(lc, script.getBody().setNeedsScope(lc));

        return script;
    }

    /**
     * Directive value or null if statement is not a directive.
     *
     * @param stmt Statement to be checked
     * @return Directive value if the given statement is a directive
     */
    private String getDirective(final Node stmt) {
        if (stmt instanceof ExpressionStatement) {
            final Node expr = ((ExpressionStatement) stmt).getExpression();
            if (expr instanceof LiteralNode) {
                final LiteralNode<?> lit = (LiteralNode<?>) expr;
                final long litToken = lit.getToken();
                final TokenType tt = Token.descType(litToken);
                // A directive is either a string or an escape string
                if (tt == TokenType.STRING || tt == TokenType.ESCSTRING) {
                    // Make sure that we don't unescape anything. Return as seen in source!
                    return source.getString(lit.getStart(), Token.descLength(litToken));
                }
            }
        }

        return null;
    }

    /**
     * SourceElements :
     *      SourceElement
     *      SourceElements SourceElement
     *
     * See 14
     *
     * Parse the elements of the script or function.
     */
    private void sourceElements(final ProgramKind shouldAllowPropertyFunction) {
        List<Node> directiveStmts = null;
        boolean checkDirective = true;
        ProgramKind programKind = shouldAllowPropertyFunction;
        final boolean oldStrictMode = isStrictMode;

        try {
            // If is a script, then process until the end of the script.
            while (type != EOF) {
                // Break if the end of a code block.
                if (type == RBRACE) {
                    break;
                }

                try {
                    // Get the next element.
                    statement(true, programKind, false);
                    programKind = ProgramKind.NORMAL;

                    // check for directive prologues
                    if (checkDirective) {
                        // skip any debug statement like line number to get actual first line
                        final Node lastStatement = lc.getLastStatement();

                        // get directive prologue, if any
                        final String directive = getDirective(lastStatement);

                        // If we have seen first non-directive statement,
                        // no more directive statements!!
                        checkDirective = directive != null;

                        if (checkDirective) {
                            if (!oldStrictMode) {
                                if (directiveStmts == null) {
                                    directiveStmts = new ArrayList<>();
                                }
                                directiveStmts.add(lastStatement);
                            }

                            // handle use strict directive
                            if ("use strict".equals(directive)) {
                                isStrictMode = true;
                                final FunctionNode function = lc.getCurrentFunction();
                                lc.setFlag(lc.getCurrentFunction(), FunctionNode.IS_STRICT);

                                // We don't need to check these, if lexical environment is already strict
                                if (!oldStrictMode && directiveStmts != null) {
                                    // check that directives preceding this one do not violate strictness
                                    for (final Node statement : directiveStmts) {
                                        // the get value will force unescape of preceding
                                        // escaped string directives
                                        getValue(statement.getToken());
                                    }

                                    // verify that function name as well as parameter names
                                    // satisfy strict mode restrictions.
                                    verifyStrictIdent(function.getIdent(), "function name");
                                    for (final IdentNode param : function.getParameters()) {
                                        verifyStrictIdent(param, "function parameter");
                                    }
                                }
                            } else if (Context.DEBUG) {
                                final int flag = FunctionNode.getDirectiveFlag(directive);
                                if (flag != 0) {
                                    final FunctionNode function = lc.getCurrentFunction();
                                    lc.setFlag(function, flag);
                                }
                            }
                        }
                    }
                } catch (final Exception e) {
                    //recover parsing
                    recover(e);
                }

                // No backtracking from here on.
                stream.commit(k);
            }
        } finally {
            isStrictMode = oldStrictMode;
        }
    }

    /**
     * Statement :
     *      Block
     *      VariableStatement
     *      EmptyStatement
     *      ExpressionStatement
     *      IfStatement
     *      IterationStatement
     *      ContinueStatement
     *      BreakStatement
     *      ReturnStatement
     *      WithStatement
     *      LabelledStatement
     *      SwitchStatement
     *      ThrowStatement
     *      TryStatement
     *      DebuggerStatement
     *
     * see 12
     *
     * Parse any of the basic statement types.
     */
    private void statement() {
        statement(false, ProgramKind.NORMAL, false);
    }

    /**
     * @param topLevel does this statement occur at the "top level" of a script or a function?
     * @param allowPropertyFunction allow property "get" and "set" functions?
     * @param singleStatement are we in a single statement context?
     */
    private void statement(final boolean topLevel, final ProgramKind programKind, final boolean singleStatement) {
        if (type == FUNCTION) {
            // As per spec (ECMA section 12), function declarations as arbitrary statement
            // is not "portable". Implementation can issue a warning or disallow the same.
            functionExpression(true, topLevel);
            return;
        }

        if (programKind == ProgramKind.METHOD || programKind == ProgramKind.CONSTRUCTOR_METHOD) {
            // A method definition on its own, which is how one is re-parsed. It is the
            // whole of the range, so it is read before the statement forms rather than
            // among them: a key that is a string, a number or an expression starts a
            // statement of its own otherwise.
            methodReparse(programKind == ProgramKind.CONSTRUCTOR_METHOD);
            return;
        }

        switch (type) {
        case LBRACE:
            block();
            break;
        case VAR:
            variableStatement(type, true);
            break;
        case SEMICOLON:
            emptyStatement();
            break;
        case IF:
            ifStatement();
            break;
        case FOR:
            forStatement();
            break;
        case WHILE:
            whileStatement();
            break;
        case DO:
            doStatement();
            break;
        case CONTINUE:
            continueStatement();
            break;
        case BREAK:
            breakStatement();
            break;
        case RETURN:
            returnStatement();
            break;
        case YIELD:
            yieldStatement();
            break;
        case WITH:
            withStatement();
            break;
        case SWITCH:
            switchStatement();
            break;
        case THROW:
            throwStatement();
            break;
        case TRY:
            tryStatement();
            break;
        case DEBUGGER:
            debuggerStatement();
            break;
        case CLASS:
            if (!isES6()) {
                // Nothing has been consumed, so falling out of the switch here would
                // leave the statement loop spinning on the same token.
                throw error(AbstractParser.message("expected.operand", type.getNameOrType()), token);
            }
            if (programKind == ProgramKind.CLASS_CONSTRUCTOR) {
                // Only the constructor the class did not write out is wanted; the rest
                // of the class is parsed and thrown away.
                final FunctionNode constructor = (FunctionNode) classTail(ProgramKind.CLASS_CONSTRUCTOR, true);
                addPropertyFunctionStatement(new PropertyFunction(constructor.getIdent(), constructor));

                return;
            }
            classDeclaration();

            return;
        case RPAREN:
        case RBRACKET:
        case EOF:
            expect(SEMICOLON);
            break;
        default:
            if (useBlockScope() && (type == LET || type == CONST)) {
                if (singleStatement) {
                    throw error(AbstractParser.message("expected.stmt", type.getName() + " declaration"), token);
                }
                variableStatement(type, true);
                break;
            }
            if (env._const_as_var && type == CONST) {
                variableStatement(TokenType.VAR, true);
                break;
            }

            if (type == IDENT || isNonStrictModeIdent()) {
                if (T(k + 1) == COLON) {
                    labelStatement();
                    return;
                }
                if (programKind == ProgramKind.PROPERTY_ACCESSOR) {
                    final String ident = (String) getValue();
                    final long propertyToken = token;
                    final int propertyLine = line;
                    if ("get".equals(ident)) {
                        next();
                        addPropertyFunctionStatement(propertyGetterFunction(propertyToken, propertyLine));
                        return;
                    } else if ("set".equals(ident)) {
                        next();
                        addPropertyFunctionStatement(propertySetterFunction(propertyToken, propertyLine));
                        return;
                    }
                }
            }

            expressionStatement();
            break;
        }
    }

    /**
     * Re-parse a method definition from its own source range, which holds the key as
     * well as the function so that the range starts where the method starts.
     *
     * A computed key is read and thrown away. Only the function is wanted, and the
     * runtime, which counts the functions it gets back and expects exactly one, walks
     * the statements it is given: a key that holds a function of its own is not among
     * them, and a function inside it starts later in the source than the method does,
     * so it can never be mistaken for the method either.
     */
    private void methodReparse(final boolean classConstructor) {
        final long methodToken = methodStartToken(token);
        final int methodLine = line;
        final IdentNode methodName;

        if (type == LBRACKET) {
            next();
            assignmentExpression(false);
            expect(RBRACKET);
            methodName = syntheticMethodName(methodToken, methodLine, null);
        } else if (isPropertyNameStart(type)) {
            methodName = syntheticMethodName(methodToken, methodLine, propertyName().getPropertyName());
        } else {
            methodName = getIdentifierName();
        }

        addPropertyFunctionStatement(new PropertyFunction(methodName,
                methodDefinition(methodToken, methodLine, methodName, classConstructor)));
    }

    /**
     * A class declaration binds the class expression to its name. ES6 makes the binding
     * block scoped, so it is a let where block scoping is available.
     */
    private void classDeclaration() {
        final int classLine = line;
        final long classToken = token;
        final IdentNode name = className();
        final int varFlags = useBlockScope() ? VarNode.IS_LET : 0;

        // The class binding is declared first and assigned inside a block of its own, so
        // that the superclass binding the block also holds belongs to this class alone.
        appendStatement(new VarNode(classLine, classToken, finish, name.setIsDeclaredHere(), null, varFlags));

        Block classBlock = newBlock();
        try {
            appendStatement(new VarNode(classLine, classToken, finish,
                    createIdentNode(Token.recast(classToken, IDENT), finish, SUPERCLASS).setIsDeclaredHere(), null, varFlags));
            appendStatement(new ExpressionStatement(classLine, classToken, finish,
                    new BinaryNode(Token.recast(classToken, TokenType.ASSIGN), referenceTo(name),
                            classTail(ProgramKind.NORMAL, true))));
        } finally {
            classBlock = restoreBlock(classBlock);
        }

        appendStatement(new BlockStatement(classLine, classBlock));
    }

    /** Read the name of the class starting at the current token, without consuming it. */
    private IdentNode className() {
        final int nameIndex = k + 1;

        if (T(nameIndex) != IDENT) {
            final long nameToken = getToken(nameIndex);
            throw error(AbstractParser.message("expected", IDENT.getNameOrType(), Token.toString(source, nameToken)),
                    nameToken);
        }

        return createIdentNode(getToken(nameIndex), finish, (String) getValue(getToken(nameIndex)));
    }

    private void addPropertyFunctionStatement(final PropertyFunction propertyFunction) {
        final FunctionNode fn = propertyFunction.functionNode;
        functionDeclarations.add(new ExpressionStatement(fn.getLineNumber(), fn.getToken(), finish, fn));
    }

    /**
     * block :
     *      { StatementList? }
     *
     * see 12.1
     *
     * Parse a statement block.
     */
    private void block() {
        appendStatement(new BlockStatement(line, getBlock(true)));
    }

    /**
     * StatementList :
     *      Statement
     *      StatementList Statement
     *
     * See 12.1
     *
     * Parse a list of statements.
     */
    private void statementList() {
        // Accumulate statements until end of list. */
        loop: while (type != EOF) {
            switch (type) {
            case EOF:
            case CASE:
            case DEFAULT:
            case RBRACE:
                break loop;
            default:
                break;
            }

            // Get next statement.
            statement();
        }
    }

    /**
     * Make sure that in strict mode, the identifier name used is allowed.
     *
     * @param ident         Identifier that is verified
     * @param contextString String used in error message to give context to the user
     */
    private void verifyStrictIdent(final IdentNode ident, final String contextString) {
        if (isStrictMode) {
            switch (ident.getName()) {
            case "eval":
            case "arguments":
                throw error(AbstractParser.message("strict.name", ident.getName(), contextString), ident.getToken());
            default:
                break;
            }

            if (ident.isFutureStrictName()) {
                throw error(AbstractParser.message("strict.name", ident.getName(), contextString), ident.getToken());
            }
        }
    }

    /**
     * VariableStatement :
     *      var VariableDeclarationList ;
     *
     * VariableDeclarationList :
     *      VariableDeclaration
     *      VariableDeclarationList , VariableDeclaration
     *
     * VariableDeclaration :
     *      Identifier Initializer?
     *
     * Initializer :
     *      = AssignmentExpression
     *
     * See 12.2
     *
     * Parse a VAR statement.
     * @param isStatement True if a statement (not used in a FOR.)
     */
    private List<VarNode> variableStatement(final TokenType varType, final boolean isStatement) {
        // VAR tested in caller.
        next();

        final List<VarNode> vars = new ArrayList<>();
        int varFlags = 0;
        if (varType == LET) {
            varFlags |= VarNode.IS_LET;
        } else if (varType == CONST) {
            varFlags |= VarNode.IS_CONST;
        }

        while (true) {
            // Get starting token.
            final int varLine = line;
            final long varToken = token;

            if (isES6() && (type == LBRACKET || type == LBRACE)) {
                destructuringDeclaration(varLine, varToken, varFlags, isStatement, vars);

                if (type != COMMARIGHT) {
                    break;
                }
                next();
                continue;
            }

            // Get name of var.
            final IdentNode name = getIdent();
            verifyStrictIdent(name, "variable name");

            // Assume no init.
            Expression init = null;

            // Look for initializer assignment.
            if (type == ASSIGN) {
                next();

                // Get initializer expression. Suppress IN if not statement.
                defaultNames.push(name);
                try {
                    init = assignmentExpression(!isStatement);
                } finally {
                    defaultNames.pop();
                }
            } else if (varType == CONST) {
                throw error(AbstractParser.message("missing.const.assignment", name.getName()));
            }

            // Allocate var node.
            final VarNode var = new VarNode(varLine, varToken, finish, name.setIsDeclaredHere(), init, varFlags);
            vars.add(var);
            appendStatement(var);

            if (type != COMMARIGHT) {
                break;
            }
            next();
        }

        // If is a statement then handle end of line.
        if (isStatement) {
            final boolean semicolon = type == SEMICOLON;
            endOfLine();
            if (semicolon) {
                lc.getCurrentBlock().setFinish(finish);
            }
        }

        return vars;
    }

    /**
     * EmptyStatement :
     *      ;
     *
     * See 12.3
     *
     * Parse an empty statement.
     */
    private void emptyStatement() {
        if (env._empty_statements) {
            appendStatement(new EmptyNode(line, token, Token.descPosition(token) + Token.descLength(token)));
        }

        // SEMICOLON checked in caller.
        next();
    }

    /**
     * ExpressionStatement :
     *      Expression ; // [lookahead ~({ or  function )]
     *
     * See 12.4
     *
     * Parse an expression used in a statement block.
     */
    private void expressionStatement() {
        // Lookahead checked in caller.
        final int expressionLine = line;
        final long expressionToken = token;

        // Get expression and add as statement.
        final Expression expression = expression();

        ExpressionStatement expressionStatement = null;
        if (expression != null) {
            expressionStatement = new ExpressionStatement(expressionLine, expressionToken, finish, expression);
            appendStatement(expressionStatement);
        } else {
            expect(null);
        }

        endOfLine();

        if (expressionStatement != null) {
            expressionStatement.setFinish(finish);
            lc.getCurrentBlock().setFinish(finish);
        }
    }

    /**
     * IfStatement :
     *      if ( Expression ) Statement else Statement
     *      if ( Expression ) Statement
     *
     * See 12.5
     *
     * Parse an IF statement.
     */
    private void ifStatement() {
        // Capture IF token.
        final int ifLine = line;
        final long ifToken = token;
        // IF tested in caller.
        next();

        expect(LPAREN);
        final Expression test = expression();
        expect(RPAREN);
        final Block pass = getStatement();

        Block fail = null;
        if (type == ELSE) {
            next();
            fail = getStatement();
        }

        appendStatement(new IfNode(ifLine, ifToken, fail != null ? fail.getFinish() : pass.getFinish(), test, pass, fail));
    }

    /**
     * ... IterationStatement:
     *           ...
     *           for ( Expression[NoIn]?; Expression? ; Expression? ) Statement
     *           for ( var VariableDeclarationList[NoIn]; Expression? ; Expression? ) Statement
     *           for ( LeftHandSideExpression in Expression ) Statement
     *           for ( var VariableDeclaration[NoIn] in Expression ) Statement
     *
     * See 12.6
     *
     * Parse a FOR statement.
     */
    private void forStatement() {
        // When ES6 for-let is enabled we create a container block to capture the LET.
        final int startLine = start;
        Block outer = useBlockScope() ? newBlock() : null;

        // Create FOR node, capturing FOR token.
        ForNode forNode = new ForNode(line, token, Token.descPosition(token), null, 0);
        lc.push(forNode);

        try {
            // FOR tested in caller.
            next();

            // Sai extension: for each expression.
            // iterate property values rather than property names.
            if (!env._no_syntax_extensions && type == IDENT && "each".equals(getValue())) {
                forNode = forNode.setIsForEach(lc);
                next();
            }

            expect(LPAREN);

            if (isES6() && isForOf()) {
                forNode = forOf(forNode, startLine);
                appendStatement(forNode);

                return;
            }

            if (isES6() && isForInLowered()) {
                forNode = forIn(forNode, startLine);
                appendStatement(forNode);

                return;
            }

            List<VarNode> vars = null;

            switch (type) {
            case VAR:
                // Var declaration captured in for outer block.
                vars = variableStatement(type, false);
                break;
            case SEMICOLON:
                break;
            default:
                if (useBlockScope() && (type == LET || type == CONST)) {
                    if (type == LET) {
                        forNode = forNode.setPerIterationScope(lc);
                    }
                    // LET/CONST declaration captured in container block created above.
                    vars = variableStatement(type, false);
                    break;
                }
                if (env._const_as_var && type == CONST) {
                    // Var declaration captured in for outer block.
                    vars = variableStatement(TokenType.VAR, false);
                    break;
                }

                final Expression expression = expression(unaryExpression(), COMMARIGHT.getPrecedence(), true);
                forNode = forNode.setInit(lc, expression);
                break;
            }

            switch (type) {
            case SEMICOLON:
                // for (init; test; modify)

                // for each (init; test; modify) is invalid
                if (forNode.isForEach()) {
                    throw error(AbstractParser.message("for.each.without.in"), token);
                }

                expect(SEMICOLON);
                if (type != SEMICOLON) {
                    forNode = forNode.setTest(lc, joinPredecessorExpression());
                }
                expect(SEMICOLON);
                if (type != RPAREN) {
                    forNode = forNode.setModify(lc, joinPredecessorExpression());
                }
                break;

            case IN:
                forNode = forNode.setIsForIn(lc).setTest(lc, new JoinPredecessorExpression());
                if (vars != null) {
                    // for (var i in obj)
                    if (vars.size() == 1) {
                        forNode = forNode.setInit(lc, new IdentNode(vars.get(0).getName()));
                    } else {
                        // for (var i, j in obj) is invalid
                        throw error(AbstractParser.message("many.vars.in.for.in.loop"), vars.get(1).getToken());
                    }

                } else {
                    // for (expr in obj)
                    final Node init = forNode.getInit();
                    assert init != null : "for..in init expression can not be null here";

                    // check if initial expression is a valid L-value
                    if (!(init instanceof AccessNode || init instanceof IndexNode || init instanceof IdentNode)) {
                        throw error(AbstractParser.message("not.lvalue.for.in.loop"), init.getToken());
                    }

                    if (init instanceof IdentNode) {
                        if (!checkIdentLValue((IdentNode) init)) {
                            throw error(AbstractParser.message("not.lvalue.for.in.loop"), init.getToken());
                        }
                        verifyStrictIdent((IdentNode) init, "for-in iterator");
                    }
                }

                next();

                // Get the collection expression.
                forNode = forNode.setModify(lc, joinPredecessorExpression());
                break;

            default:
                expect(SEMICOLON);
                break;
            }

            expect(RPAREN);

            // Set the for body.
            final Block body = getStatement();
            forNode = forNode.setBody(lc, body);
            forNode.setFinish(body.getFinish());

            appendStatement(forNode);
        } finally {
            lc.pop(forNode);

            // Restored here rather than after the try, so that the for-of path, which
            // is complete once its body is parsed and returns early, still closes it.
            if (outer != null) {
                outer.setFinish(forNode.getFinish());
                outer = restoreBlock(outer);
                appendStatement(new BlockStatement(startLine, outer));
            }
        }
    }

    /**
     * Look ahead for the {@code of} of a for-of loop. This has to be known before the
     * loop variable is parsed, because a let binding is declared inside the body so
     * that every iteration gets its own, rather than around the loop.
     *
     * @return true if a for-of head starts at the current token
     */
    private boolean isForOf() {
        return lookahead(this::isForOfAhead);
    }

    private boolean isForOfAhead() {
        final int i = skipForVariable();

        return i >= 0 && T(i) == IDENT && "of".equals(getValue(getToken(i)));
    }

    /**
     * Look ahead for the {@code in} of a for-in loop whose variable is a destructuring
     * pattern. A plain name is left to the ordinary for-in path; only a pattern needs
     * the loop rewriting.
     *
     * @return true if a for-in head with a pattern starts at the current token
     */
    private boolean isForInPattern() {
        return lookahead(this::isForInPatternAhead);
    }

    private boolean isForInPatternAhead() {
        int i = k;

        if (T(i) == TokenType.VAR || T(i) == LET || T(i) == CONST) {
            i++;
        }

        if (T(i) != LBRACKET && T(i) != LBRACE) {
            return false;
        }

        i = skipForVariable();

        return i >= 0 && T(i) == TokenType.IN;
    }

    /**
     * Skip the loop variable at the head of a for loop, which may be a destructuring
     * pattern, without parsing it.
     *
     * @return the index of the token after the variable, or -1 if what is there cannot
     *         be a loop variable at all
     */
    private int skipForVariable() {
        int i = k;

        if (T(i) == TokenType.VAR || T(i) == LET || T(i) == CONST) {
            i++;
        }

        if (T(i) == LBRACKET || T(i) == LBRACE) {
            // A destructuring pattern. Find the bracket that closes it; what is in
            // between only has to balance here, it is parsed properly once the head is
            // known to be a for-of or a for-in.
            final TokenType open = T(i);
            final TokenType close = open == LBRACKET ? RBRACKET : RBRACE;
            int depth = 0;

            for (;; i++) {
                final TokenType tokenType = T(i);

                if (tokenType == open) {
                    depth++;
                } else if (tokenType == close) {
                    if (--depth == 0) {
                        i++;
                        break;
                    }
                } else if (tokenType == EOF) {
                    return -1;
                }
            }
        } else if (T(i) == IDENT) {
            i++;
        } else {
            return -1;
        }

        return i;
    }

    /**
     * Parse a for-of head and body, lowering the loop to a counted loop that reads the
     * source by index:
     *
     * <pre>
     * for (var v of src) body;
     * </pre>
     * <pre>
     * :pt0 = src; :pt1 = 0;
     * for (; :pt1 &lt; :pt0.length; :pt1 = :pt1 + 1) { var v = :pt0[:pt1]; body; }
     * </pre>
     *
     * Reading by index rather than through an iterator is what makes a string and any
     * array-like object work, and an object that is only iterable not.
     *
     * The loop variable is declared inside the body rather than around the loop, so a
     * let binding is fresh on every iteration and a closure made in the body captures
     * that iteration's value.
     *
     * @param forNode the loop being built
     * @param forLine line the loop starts on
     * @return the completed loop
     */
    private ForNode forOf(final ForNode forNodeArg, final int forLine) {
        ForNode forNode = forNodeArg;

        TokenType declarationType = null;
        long declarationToken = 0L;

        if (type == TokenType.VAR || type == LET || type == CONST) {
            declarationType = type;
            declarationToken = token;
            next();
        }

        final List<Binding> pattern;
        final IdentNode name;

        if (type == LBRACKET || type == LBRACE) {
            // The leaves of a pattern with no declaration are assignment targets rather
            // than names to declare, the same distinction destructuringAssignment makes.
            pattern = destructuringPattern(declarationType == null);
            name = null;
        } else {
            pattern = null;
            name = getIdent();
            verifyStrictIdent(name, "for-of iterator");
        }

        // "of" is a plain identifier, not a keyword.
        final long ofToken = token;
        next();

        final Expression source = expression();

        final String sourceName = newTemporary();
        final String indexName = newTemporary();

        // Evaluate the source once, and start the index, before the loop.
        appendStatement(assignTemporary(forLine, ofToken, sourceName, source));
        appendStatement(assignTemporary(forLine, ofToken, indexName,
                LiteralNode.newInstance(ofToken, finish, Integer.valueOf(0))));

        forNode = forNode.setTest(lc, new JoinPredecessorExpression(new BinaryNode(Token.recast(ofToken, TokenType.LT),
                identifierFor(ofToken, indexName), new AccessNode(Token.recast(ofToken, TokenType.PERIOD), finish,
                        identifierFor(ofToken, sourceName), "length"))));

        forNode = forNode.setModify(lc, new JoinPredecessorExpression(new BinaryNode(
                Token.recast(ofToken, TokenType.ASSIGN), identifierFor(ofToken, indexName),
                new BinaryNode(Token.recast(ofToken, TokenType.ADD), identifierFor(ofToken, indexName),
                        LiteralNode.newInstance(ofToken, finish, Integer.valueOf(1))))));

        expect(RPAREN);

        final Expression element = readFrom(ofToken, sourceName, identifierFor(ofToken, indexName));

        Block body = newBlock();
        try {
            final int varFlags = declarationType == LET ? VarNode.IS_LET
                    : declarationType == CONST ? VarNode.IS_CONST : 0;

            if (pattern != null) {
                // Bind the element to a temporary and take the pattern apart from there,
                // so the element expression is evaluated once per iteration.
                final String elementName = newTemporary();

                if (declarationType == null) {
                    final List<Expression> steps = new ArrayList<>();
                    steps.add(new BinaryNode(Token.recast(ofToken, TokenType.ASSIGN),
                            identifierFor(ofToken, elementName), element));
                    assignBindings(elementName, pattern, steps);

                    Expression result = steps.get(0);

                    for (int i = 1; i < steps.size(); i++) {
                        result = new BinaryNode(Token.recast(ofToken, TokenType.COMMARIGHT), result, steps.get(i));
                    }

                    appendStatement(new ExpressionStatement(forLine, ofToken, finish, result));
                } else {
                    appendStatement(assignTemporary(forLine, ofToken, elementName, element));

                    final List<Statement> statements = new ArrayList<>();
                    declareBindings(elementName, pattern, forLine, varFlags, new ArrayList<VarNode>(), statements);

                    for (final Statement statement : statements) {
                        appendStatement(statement);
                    }
                }
            } else if (declarationType == null) {
                appendStatement(new ExpressionStatement(forLine, ofToken, finish,
                        new BinaryNode(Token.recast(ofToken, TokenType.ASSIGN), name, element)));
            } else {
                appendStatement(new VarNode(forLine, declarationToken, finish, name.setIsDeclaredHere(), element, varFlags));
            }

            appendStatement(new BlockStatement(forLine, getStatement()));
        } finally {
            body = restoreBlock(body);
        }

        forNode = forNode.setBody(lc, body);
        forNode.setFinish(body.getFinish());

        return forNode;
    }

    /**
     * Look ahead for a for-in head that has to be rewritten: one whose loop variable is
     * a destructuring pattern, and one that declares a plain name with let or const.
     * As with for-of this has to be known before the head is parsed, because either way
     * the variable is declared inside the body rather than around the loop.
     *
     * A var or a bare assignment target needs none of that and is left to the ordinary
     * for-in path.
     *
     * @return true if a for-in head that needs rewriting starts at the current token
     */
    private boolean isForInLowered() {
        return lookahead(this::isForInLoweredAhead);
    }

    private boolean isForInLoweredAhead() {
        final TokenType declaration = T(k);
        final int variable = declaration == TokenType.VAR || declaration == LET || declaration == CONST ? k + 1 : k;

        if (T(variable) != LBRACKET && T(variable) != LBRACE && declaration != LET && declaration != CONST) {
            return false;
        }

        final int i = skipForVariable();

        return i >= 0 && T(i) == TokenType.IN;
    }

    /**
     * Parse the head and body of a for-in loop whose variable is a destructuring pattern
     * or a let or const declaration:
     *
     * <pre>
     * for (let k in obj) body;
     * for (var [i, j] in obj) body;
     * </pre>
     * <pre>
     * for (:pt0 in obj) { let k = :pt0; { body } }
     * for (:pt0 in obj) { var i = :pt0[0]; var j = :pt0[1]; { body } }
     * </pre>
     *
     * The loop itself iterates a temporary and the variable is bound from it at the top
     * of the body. Binding inside the body rather than around the loop is what gives a
     * let or a const one binding per iteration: the body block gets a fresh scope every
     * time round, so a closure made there captures that iteration's value, and a const
     * is initialised rather than assigned to - which is also why the head of such a loop
     * needs no initializer for a const.
     *
     * The key a for-in hands over is always a string, so an array pattern reads it out
     * character by character, which follows from patterns reading by index.
     *
     * @param forNodeArg the loop being built
     * @param forLine    line the loop starts on
     * @return the completed loop
     */
    private ForNode forIn(final ForNode forNodeArg, final int forLine) {
        ForNode forNode = forNodeArg;
        final long headToken = token;

        TokenType declarationType = null;
        long declarationToken = 0L;

        if (type == TokenType.VAR || type == LET || type == CONST) {
            declarationType = type;
            declarationToken = token;
            next();
        }

        final List<Binding> pattern;
        final IdentNode name;

        if (type == LBRACKET || type == LBRACE) {
            if (forNode.isForEach()) {
                throw error(AbstractParser.message("no.pattern.in.for.each"), headToken);
            }

            // The leaves of a pattern with no declaration are assignment targets rather
            // than names to declare, the same distinction destructuringAssignment makes.
            pattern = destructuringPattern(declarationType == null);
            name = null;
        } else {
            pattern = null;
            name = getIdent();
            verifyStrictIdent(name, "for-in iterator");
        }

        final long inToken = token;
        expect(TokenType.IN);

        final String keyName = newTemporary();

        forNode = forNode.setIsForIn(lc).setTest(lc, new JoinPredecessorExpression());
        forNode = forNode.setInit(lc, identifierFor(inToken, keyName));

        // Get the collection expression.
        forNode = forNode.setModify(lc, joinPredecessorExpression());

        expect(RPAREN);

        final int varFlags = declarationType == LET ? VarNode.IS_LET : declarationType == CONST ? VarNode.IS_CONST : 0;

        Block body = newBlock();
        try {
            if (pattern == null) {
                // Only a let or a const reaches here with a plain name; a var keeps its
                // single loop-wide binding on the ordinary path.
                appendStatement(new VarNode(forLine, declarationToken, finish, name.setIsDeclaredHere(),
                        identifierFor(inToken, keyName), varFlags));
            } else if (declarationType == null) {
                final List<Expression> steps = new ArrayList<>();
                assignBindings(keyName, pattern, steps);

                if (!steps.isEmpty()) {
                    Expression result = steps.get(0);

                    for (int i = 1; i < steps.size(); i++) {
                        result = new BinaryNode(Token.recast(inToken, TokenType.COMMARIGHT), result, steps.get(i));
                    }

                    appendStatement(new ExpressionStatement(forLine, inToken, finish, result));
                }
            } else {
                final List<Statement> statements = new ArrayList<>();
                declareBindings(keyName, pattern, forLine, varFlags, new ArrayList<VarNode>(), statements);

                for (final Statement statement : statements) {
                    appendStatement(statement);
                }
            }

            appendStatement(new BlockStatement(forLine, getStatement()));
        } finally {
            body = restoreBlock(body);
        }

        forNode = forNode.setBody(lc, body);
        forNode.setFinish(body.getFinish());

        return forNode;
    }

    /**
     * ... IterationStatement :
     *           ...
     *           Expression[NoIn]?; Expression? ; Expression?
     *           var VariableDeclarationList[NoIn]; Expression? ; Expression?
     *           LeftHandSideExpression in Expression
     *           var VariableDeclaration[NoIn] in Expression
     *
     * See 12.6
     *
     * Parse the control section of a FOR statement.  Also used for
     * comprehensions.
     * @param forNode Owning FOR.
     */

    /**
     * ...IterationStatement :
     *           ...
     *           while ( Expression ) Statement
     *           ...
     *
     * See 12.6
     *
     * Parse while statement.
     */
    private void whileStatement() {
        // Capture WHILE token.
        final long whileToken = token;
        // WHILE tested in caller.
        next();

        // Construct WHILE node.
        WhileNode whileNode = new WhileNode(line, whileToken, Token.descPosition(whileToken), false);
        lc.push(whileNode);

        try {
            expect(LPAREN);
            final int whileLine = line;
            final JoinPredecessorExpression test = joinPredecessorExpression();
            expect(RPAREN);
            final Block body = getStatement();
            appendStatement(whileNode = new WhileNode(whileLine, whileToken, finish, false).setTest(lc, test).setBody(lc, body));
        } finally {
            lc.pop(whileNode);
        }
    }

    /**
     * ...IterationStatement :
     *           ...
     *           do Statement while( Expression ) ;
     *           ...
     *
     * See 12.6
     *
     * Parse DO WHILE statement.
     */
    private void doStatement() {
        // Capture DO token.
        final long doToken = token;
        // DO tested in the caller.
        next();

        WhileNode doWhileNode = new WhileNode(-1, doToken, Token.descPosition(doToken), true);
        lc.push(doWhileNode);

        try {
            // Get DO body.
            final Block body = getStatement();

            expect(WHILE);
            expect(LPAREN);
            final int doLine = line;
            final JoinPredecessorExpression test = joinPredecessorExpression();
            expect(RPAREN);

            if (type == SEMICOLON) {
                endOfLine();
            }
            doWhileNode.setFinish(finish);

            //line number is last
            appendStatement(doWhileNode = new WhileNode(doLine, doToken, finish, true).setBody(lc, body).setTest(lc, test));
        } finally {
            lc.pop(doWhileNode);
        }
    }

    /**
     * ContinueStatement :
     *      continue Identifier? ; // [no LineTerminator here]
     *
     * See 12.7
     *
     * Parse CONTINUE statement.
     */
    private void continueStatement() {
        // Capture CONTINUE token.
        final int continueLine = line;
        final long continueToken = token;
        // CONTINUE tested in caller.
        nextOrEOL();

        LabelNode labelNode = null;

        // SEMICOLON or label.
        switch (type) {
        case RBRACE:
        case SEMICOLON:
        case EOL:
        case EOF:
            break;

        default:
            final IdentNode ident = getIdent();
            labelNode = lc.findLabel(ident.getName());

            if (labelNode == null) {
                throw error(AbstractParser.message("undefined.label", ident.getName()), ident.getToken());
            }

            break;
        }

        final String labelName = labelNode == null ? null : labelNode.getLabelName();
        final LoopNode targetNode = lc.getContinueTo(labelName);

        if (targetNode == null) {
            throw error(AbstractParser.message("illegal.continue.stmt"), continueToken);
        }

        endOfLine();

        // Construct and add CONTINUE node.
        appendStatement(new ContinueNode(continueLine, continueToken, finish, labelName));
    }

    /**
     * BreakStatement :
     *      break Identifier? ; // [no LineTerminator here]
     *
     * See 12.8
     *
     */
    private void breakStatement() {
        // Capture BREAK token.
        final int breakLine = line;
        final long breakToken = token;
        // BREAK tested in caller.
        nextOrEOL();

        LabelNode labelNode = null;

        // SEMICOLON or label.
        switch (type) {
        case RBRACE:
        case SEMICOLON:
        case EOL:
        case EOF:
            break;

        default:
            final IdentNode ident = getIdent();
            labelNode = lc.findLabel(ident.getName());

            if (labelNode == null) {
                throw error(AbstractParser.message("undefined.label", ident.getName()), ident.getToken());
            }

            break;
        }

        //either an explicit label - then get its node or just a "break" - get first breakable
        //targetNode is what we are breaking out from.
        final String labelName = labelNode == null ? null : labelNode.getLabelName();
        final BreakableNode targetNode = lc.getBreakable(labelName);
        if (targetNode == null) {
            throw error(AbstractParser.message("illegal.break.stmt"), breakToken);
        }

        endOfLine();

        // Construct and add BREAK node.
        appendStatement(new BreakNode(breakLine, breakToken, finish, labelName));
    }

    /**
     * ReturnStatement :
     *      return Expression? ; // [no LineTerminator here]
     *
     * See 12.9
     *
     * Parse RETURN statement.
     */
    private void returnStatement() {
        // check for return outside function
        if (lc.getCurrentFunction().getKind() == FunctionNode.Kind.SCRIPT) {
            throw error(AbstractParser.message("invalid.return"));
        }

        // Capture RETURN token.
        final int returnLine = line;
        final long returnToken = token;
        // RETURN tested in caller.
        nextOrEOL();

        Expression expression = null;

        // SEMICOLON or expression.
        switch (type) {
        case RBRACE:
        case SEMICOLON:
        case EOL:
        case EOF:
            break;

        default:
            expression = expression();
            break;
        }

        endOfLine();

        // Construct and add RETURN node.
        appendStatement(new ReturnNode(returnLine, returnToken, finish, expression));
    }

    /**
     * YieldStatement :
     *      yield Expression? ; // [no LineTerminator here]
     *
     * JavaScript 1.8
     *
     * Parse YIELD statement.
     */
    private void yieldStatement() {
        // Capture YIELD token.
        final int yieldLine = line;
        final long yieldToken = token;
        // YIELD tested in caller.
        nextOrEOL();

        Expression expression = null;

        // SEMICOLON or expression.
        switch (type) {
        case RBRACE:
        case SEMICOLON:
        case EOL:
        case EOF:
            break;

        default:
            expression = expression();
            break;
        }

        endOfLine();

        // Construct and add YIELD node.
        appendStatement(new ReturnNode(yieldLine, yieldToken, finish, expression));
    }

    /**
     * WithStatement :
     *      with ( Expression ) Statement
     *
     * See 12.10
     *
     * Parse WITH statement.
     */
    private void withStatement() {
        // Capture WITH token.
        final int withLine = line;
        final long withToken = token;
        // WITH tested in caller.
        next();

        // ECMA 12.10.1 strict mode restrictions
        if (isStrictMode) {
            throw error(AbstractParser.message("strict.no.with"), withToken);
        }

        // Get WITH expression.
        WithNode withNode = new WithNode(withLine, withToken, finish);

        try {
            lc.push(withNode);
            expect(LPAREN);
            withNode = withNode.setExpression(lc, expression());
            expect(RPAREN);
            withNode = withNode.setBody(lc, getStatement());
        } finally {
            lc.pop(withNode);
        }

        appendStatement(withNode);
    }

    /**
     * SwitchStatement :
     *      switch ( Expression ) CaseBlock
     *
     * CaseBlock :
     *      { CaseClauses? }
     *      { CaseClauses? DefaultClause CaseClauses }
     *
     * CaseClauses :
     *      CaseClause
     *      CaseClauses CaseClause
     *
     * CaseClause :
     *      case Expression : StatementList?
     *
     * DefaultClause :
     *      default : StatementList?
     *
     * See 12.11
     *
     * Parse SWITCH statement.
     */
    private void switchStatement() {
        final int switchLine = line;
        final long switchToken = token;
        // SWITCH tested in caller.
        next();

        // Create and add switch statement.
        SwitchNode switchNode =
                new SwitchNode(switchLine, switchToken, Token.descPosition(switchToken), null, new ArrayList<CaseNode>(), null);
        lc.push(switchNode);

        try {
            expect(LPAREN);
            switchNode = switchNode.setExpression(lc, expression());
            expect(RPAREN);

            expect(LBRACE);

            // Prepare to accumulate cases.
            final List<CaseNode> cases = new ArrayList<>();
            CaseNode defaultCase = null;

            while (type != RBRACE) {
                // Prepare for next case.
                Expression caseExpression = null;
                final long caseToken = token;

                switch (type) {
                case CASE:
                    next();
                    caseExpression = expression();
                    break;

                case DEFAULT:
                    if (defaultCase != null) {
                        throw error(AbstractParser.message("duplicate.default.in.switch"));
                    }
                    next();
                    break;

                default:
                    // Force an error.
                    expect(CASE);
                    break;
                }

                expect(COLON);

                // Get CASE body.
                final Block statements = getBlock(false);
                final CaseNode caseNode = new CaseNode(caseToken, finish, caseExpression, statements);
                statements.setFinish(finish);

                if (caseExpression == null) {
                    defaultCase = caseNode;
                }

                cases.add(caseNode);
            }

            switchNode = switchNode.setCases(lc, cases, defaultCase);
            next();
            switchNode.setFinish(finish);

            appendStatement(switchNode);
        } finally {
            lc.pop(switchNode);
        }
    }

    /**
     * LabelledStatement :
     *      Identifier : Statement
     *
     * See 12.12
     *
     * Parse label statement.
     */
    private void labelStatement() {
        // Capture label token.
        final long labelToken = token;
        // Get label ident.
        final IdentNode ident = getIdent();

        expect(COLON);

        if (lc.findLabel(ident.getName()) != null) {
            throw error(AbstractParser.message("duplicate.label", ident.getName()), labelToken);
        }

        LabelNode labelNode = new LabelNode(line, labelToken, finish, ident.getName(), null);
        try {
            lc.push(labelNode);
            labelNode = labelNode.setBody(lc, getStatement());
            labelNode.setFinish(finish);
            appendStatement(labelNode);
        } finally {
            assert lc.peek() instanceof LabelNode;
            lc.pop(labelNode);
        }
    }

    /**
     * ThrowStatement :
     *      throw Expression ; // [no LineTerminator here]
     *
     * See 12.13
     *
     * Parse throw statement.
     */
    private void throwStatement() {
        // Capture THROW token.
        final int throwLine = line;
        final long throwToken = token;
        // THROW tested in caller.
        nextOrEOL();

        Expression expression = null;

        // SEMICOLON or expression.
        switch (type) {
        case RBRACE:
        case SEMICOLON:
        case EOL:
            break;

        default:
            expression = expression();
            break;
        }

        if (expression == null) {
            throw error(AbstractParser.message("expected.operand", type.getNameOrType()));
        }

        endOfLine();

        appendStatement(new ThrowNode(throwLine, throwToken, finish, expression, false));
    }

    /**
     * TryStatement :
     *      try Block Catch
     *      try Block Finally
     *      try Block Catch Finally
     *
     * Catch :
     *      catch( Identifier if Expression ) Block
     *      catch( Identifier ) Block
     *
     * Finally :
     *      finally Block
     *
     * See 12.14
     *
     * Parse TRY statement.
     */
    private void tryStatement() {
        // Capture TRY token.
        final int tryLine = line;
        final long tryToken = token;
        // TRY tested in caller.
        next();

        // Container block needed to act as target for labeled break statements
        final int startLine = line;
        Block outer = newBlock();

        // Create try.

        try {
            final Block tryBody = getBlock(true);
            final List<Block> catchBlocks = new ArrayList<>();

            while (type == CATCH) {
                final int catchLine = line;
                final long catchToken = token;
                next();
                expect(LPAREN);

                final List<Binding> pattern;
                final IdentNode exception;

                if (isES6() && (type == LBRACKET || type == LBRACE)) {
                    // The exception is caught by a parameter of its own and taken apart
                    // at the top of the body, the same shape the other pattern positions
                    // are lowered to.
                    final long patternToken = token;
                    pattern = destructuringPattern();
                    exception = createIdentNode(Token.recast(patternToken, IDENT), finish, newPatternParameter());
                } else {
                    pattern = null;
                    exception = getIdent();

                    // ECMA 12.4.1 strict mode restrictions
                    verifyStrictIdent(exception, "catch argument");
                }

                // Sai extension: catch clause can have optional
                // condition. So, a single try can have more than one
                // catch clause each with it's own condition.
                final Expression ifExpression;
                if (!env._no_syntax_extensions && type == IF) {
                    if (pattern != null) {
                        // The condition decides whether the body runs, so it is evaluated
                        // before the pattern has been taken apart and cannot see any of
                        // the names in it.
                        throw error(AbstractParser.message("no.pattern.in.conditional.catch"), token);
                    }

                    next();
                    // Get the exception condition.
                    ifExpression = expression();
                } else {
                    ifExpression = null;
                }

                expect(RPAREN);

                Block catchBlock = newBlock();
                try {
                    // Get CATCH body.
                    final Block catchBody = pattern == null ? getBlock(true) : catchPatternBody(catchLine, exception, pattern);
                    final CatchNode catchNode = new CatchNode(catchLine, catchToken, finish, exception, ifExpression, catchBody, false);
                    appendStatement(catchNode);
                } finally {
                    catchBlock = restoreBlock(catchBlock);
                    catchBlocks.add(catchBlock);
                }

                // If unconditional catch then should to be the end.
                if (ifExpression == null) {
                    break;
                }
            }

            // Prepare to capture finally statement.
            Block finallyStatements = null;

            if (type == FINALLY) {
                next();
                finallyStatements = getBlock(true);
            }

            // Need at least one catch or a finally.
            if (catchBlocks.isEmpty() && finallyStatements == null) {
                throw error(AbstractParser.message("missing.catch.or.finally"), tryToken);
            }

            final TryNode tryNode = new TryNode(tryLine, tryToken, Token.descPosition(tryToken), tryBody, catchBlocks, finallyStatements);
            // Add try.
            assert lc.peek() == outer;
            appendStatement(tryNode);

            tryNode.setFinish(finish);
            outer.setFinish(finish);

        } finally {
            outer = restoreBlock(outer);
        }

        appendStatement(new BlockStatement(startLine, outer));
    }

    /**
     * Build the body of a catch clause whose parameter was written as a pattern:
     *
     * <pre>
     * catch ([i, j]) { body }
     * </pre>
     * <pre>
     * catch (:pp0) { let i = :pp0[0]; let j = :pp0[1]; { body } }
     * </pre>
     *
     * The bindings are let rather than var so that they belong to the catch clause the
     * way the plain exception name does, and the written body keeps a block of its own
     * so that a declaration in it can still shadow one of them.
     *
     * @param catchLine line the catch clause starts on
     * @param exception the parameter the exception is caught by
     * @param pattern   the pattern to take it apart with
     * @return the body to give the catch clause
     */
    private Block catchPatternBody(final int catchLine, final IdentNode exception, final List<Binding> pattern) {
        Block wrapper = newBlock();
        try {
            final List<Statement> statements = new ArrayList<>();
            declareBindings(exception.getName(), pattern, catchLine, VarNode.IS_LET, new ArrayList<VarNode>(), statements);

            for (final Statement statement : statements) {
                appendStatement(statement);
            }

            appendStatement(new BlockStatement(catchLine, getBlock(true)));
        } finally {
            wrapper = restoreBlock(wrapper);
        }

        wrapper.setFinish(finish);

        return wrapper;
    }

    /**
     * DebuggerStatement :
     *      debugger ;
     *
     * See 12.15
     *
     * Parse debugger statement.
     */
    private void debuggerStatement() {
        // Capture DEBUGGER token.
        final int debuggerLine = line;
        final long debuggerToken = token;
        // DEBUGGER tested in caller.
        next();
        endOfLine();
        appendStatement(new ExpressionStatement(debuggerLine, debuggerToken, finish, new RuntimeNode(debuggerToken, finish,
                RuntimeNode.Request.DEBUGGER, new ArrayList<Expression>())));
    }

    /**
     * PrimaryExpression :
     *      this
     *      Identifier
     *      Literal
     *      ArrayLiteral
     *      ObjectLiteral
     *      ( Expression )
     *
     *  See 11.1
     *
     * Parse primary expression.
     * @return Expression node.
     */
    @SuppressWarnings("fallthrough")
    private Expression primaryExpression() {
        // Capture first token.
        final int primaryLine = line;
        final long primaryToken = token;

        switch (type) {
        case THIS:
            next();
            return thisFor(primaryToken);
        case IDENT:
            final IdentNode ident = getIdent();
            if (ident == null) {
                break;
            }
            detectSpecialProperty(ident);
            return ident;
        case TEMPLATE:
        case TEMPLATE_HEAD:
            return templateLiteral();
        case CLASS:
            if (isES6()) {
                return classTail(ProgramKind.NORMAL, false);
            }
            break;
        case OCTAL_LEGACY:
            if (isStrictMode) {
                throw error(AbstractParser.message("strict.no.octal"), token);
            }
        case STRING:
        case ESCSTRING:
        case DECIMAL:
        case HEXADECIMAL:
        case OCTAL:
        case BINARY_NUMBER:
        case FLOATING:
        case REGEX:
        case XML:
            return getLiteral();
        case EXECSTRING:
            return execString(primaryLine, primaryToken);
        case FALSE:
            next();
            return LiteralNode.newInstance(primaryToken, finish, false);
        case TRUE:
            next();
            return LiteralNode.newInstance(primaryToken, finish, true);
        case NULL:
            next();
            return LiteralNode.newInstance(primaryToken, finish);
        case LBRACKET:
            return arrayLiteral();
        case LBRACE:
            return objectLiteral();
        case LPAREN:
            next();

            final Expression expression = expression();

            expect(RPAREN);

            return expression;

        default:
            // In this context some operator tokens mark the start of a literal.
            if (lexer.scanLiteral(primaryToken, type, lineInfoReceiver)) {
                next();
                return getLiteral();
            }
            if (isNonStrictModeIdent()) {
                return getIdent();
            }
            break;
        }

        return null;
    }

    /**
     * Convert execString to a call to $EXEC.
     *
     * @param primaryToken Original string token.
     * @return callNode to $EXEC.
     */
    CallNode execString(final int primaryLine, final long primaryToken) {
        // Synthesize an ident to call $EXEC.
        final IdentNode execIdent = new IdentNode(primaryToken, finish, ScriptingFunctions.EXEC_NAME);
        // Skip over EXECSTRING.
        next();
        // Set up argument list for call.
        // Skip beginning of edit string expression.
        expect(LBRACE);
        // Add the following expression to arguments.
        final List<Expression> arguments = Collections.singletonList(expression());
        // Skip ending of edit string expression.
        expect(RBRACE);

        return new CallNode(primaryLine, primaryToken, finish, execIdent, arguments, false);
    }

    /**
     * ArrayLiteral :
     *      [ Elision? ]
     *      [ ElementList ]
     *      [ ElementList , Elision? ]
     *      [ expression for (LeftHandExpression in expression) ( (if ( Expression ) )? ]
     *
     * ElementList : Elision? AssignmentExpression
     *      ElementList , Elision? AssignmentExpression
     *
     * Elision :
     *      ,
     *      Elision ,
     *
     * See 12.1.4
     * JavaScript 1.8
     *
     * Parse array literal.
     * @return Expression node.
     */
    private Expression arrayLiteral() {
        // Capture LBRACKET token.
        final int arrayLine = line;
        final long arrayToken = token;
        // LBRACKET tested in caller.
        next();

        // Prepare to accummulating elements.
        final List<Expression> elements = new ArrayList<>();
        // Track elisions.
        boolean elision = true;
        loop: while (true) {
            switch (type) {
            case RBRACKET:
                next();

                break loop;

            case COMMARIGHT:
                next();

                // If no prior expression
                if (elision) {
                    elements.add(null);
                }

                elision = true;

                break;

            default:
                if (!elision) {
                    throw error(AbstractParser.message("expected.comma", type.getNameOrType()));
                }
                // Add expression element.
                final Expression expression = isES6() && type == ELLIPSIS ? spreadElement() : assignmentExpression(false);

                if (expression != null) {
                    elements.add(expression);
                } else {
                    expect(RBRACKET);
                }

                elision = false;
                break;
            }
        }

        if (hasSpread(elements)) {
            return spreadToArray(arrayLine, arrayToken, elements);
        }

        return LiteralNode.newInstance(arrayToken, finish, elements);
    }

    /**
     * ObjectLiteral :
     *      { }
     *      { PropertyNameAndValueList } { PropertyNameAndValueList , }
     *
     * PropertyNameAndValueList :
     *      PropertyAssignment
     *      PropertyNameAndValueList , PropertyAssignment
     *
     * See 11.1.5
     *
     * Parse an object literal.
     * @return Expression node.
     */
    private Expression objectLiteral() {
        // Capture LBRACE token.
        final long objectToken = token;
        // LBRACE tested in caller.
        next();

        // Object context.
        // Prepare to accumulate elements.
        final List<PropertyNode> elements = new ArrayList<>();
        final Map<String, Integer> map = new HashMap<>();

        // Properties from the first computed key onwards. They are applied to the
        // finished object one by one so that source order is kept.
        final List<DeferredProperty> deferred = new ArrayList<>();

        // Create a block for the object literal.
        boolean commaSeen = true;
        loop: while (true) {
            switch (type) {
            case RBRACE:
                next();
                break loop;

            case COMMARIGHT:
                if (commaSeen) {
                    throw error(AbstractParser.message("expected.property.id", type.getNameOrType()));
                }
                next();
                commaSeen = true;
                break;

            default:
                if (!commaSeen) {
                    throw error(AbstractParser.message("expected.comma", type.getNameOrType()));
                }

                commaSeen = false;

                if (isES6() && type == LBRACKET) {
                    // ES6 computed property name.
                    deferred.add(computedProperty());
                    break;
                }

                // Get and add the next property.
                final PropertyNode property = propertyAssignment(deferred);

                if (property == null) {
                    // An accessor with a computed name, which propertyAssignment has
                    // already deferred: there is no PropertyNode that can hold one.
                    break;
                }

                if (!deferred.isEmpty()) {
                    // A property after a computed key has to be applied after it.
                    final boolean getterFirst = property.getGetter() != null;

                    deferred.add(new DeferredProperty(property.getToken(), line,
                            LiteralNode.newInstance(property.getToken(), property.getFinish(), property.getKeyName()),
                            property.getValue(), getterFirst ? property.getGetter() : property.getSetter(),
                            getterFirst));
                    break;
                }

                final String key = property.getKeyName();
                final Integer existing = map.get(key);

                if (existing == null) {
                    map.put(key, elements.size());
                    elements.add(property);
                    break;
                }

                final PropertyNode existingProperty = elements.get(existing);

                // ECMA section 11.1.5 Object Initialiser
                // point # 4 on property assignment production
                final Expression value = property.getValue();
                final FunctionNode getter = property.getGetter();
                final FunctionNode setter = property.getSetter();

                final Expression prevValue = existingProperty.getValue();
                final FunctionNode prevGetter = existingProperty.getGetter();
                final FunctionNode prevSetter = existingProperty.getSetter();

                // ECMA 11.1.5 strict mode restrictions
                if (isStrictMode && value != null && prevValue != null) {
                    throw error(AbstractParser.message("property.redefinition", key), property.getToken());
                }

                final boolean isPrevAccessor = prevGetter != null || prevSetter != null;
                final boolean isAccessor = getter != null || setter != null;

                // data property redefined as accessor property
                if (prevValue != null && isAccessor) {
                    throw error(AbstractParser.message("property.redefinition", key), property.getToken());
                }

                // accessor property redefined as data
                if (isPrevAccessor && value != null) {
                    throw error(AbstractParser.message("property.redefinition", key), property.getToken());
                }

                if (isAccessor && isPrevAccessor) {
                    if (getter != null && prevGetter != null || setter != null && prevSetter != null) {
                        throw error(AbstractParser.message("property.redefinition", key), property.getToken());
                    }
                }

                if (value != null) {
                    elements.add(property);
                } else if (getter != null) {
                    elements.set(existing, existingProperty.setGetter(getter));
                } else if (setter != null) {
                    elements.set(existing, existingProperty.setSetter(setter));
                }
                break;
            }
        }

        final ObjectNode objectNode = new ObjectNode(objectToken, finish, elements);

        if (deferred.isEmpty()) {
            return objectNode;
        }

        return applyDeferredProperties(objectToken, objectNode, deferred);
    }

    /**
     * A property of an object literal that cannot go into the literal itself, either
     * because its key is computed or because it comes after one that is. It is applied
     * to the finished object instead.
     */
    private static final class DeferredProperty {
        private final long token;
        private final int line;
        private final Expression key;
        /** The value, for a data property or a shorthand method. */
        private final Expression value;
        /** The function, when this is an accessor rather than a data property. */
        private final FunctionNode accessor;
        private final boolean getter;

        DeferredProperty(final long token, final int line, final Expression key, final Expression value,
                final FunctionNode accessor, final boolean getter) {
            this.token = token;
            this.line = line;
            this.key = key;
            this.value = value;
            this.accessor = accessor;
            this.getter = getter;
        }
    }

    /**
     * Parse a property of an object literal whose name is computed, which may be a data
     * property or a shorthand method:
     *
     * <pre>
     * { [k]: v }   { [k]() {} }
     * </pre>
     *
     * An accessor with a computed name starts with its {@code get} or {@code set}, so
     * propertyAssignment reads that one.
     *
     * @return the property, to be applied to the finished object
     */
    private DeferredProperty computedProperty() {
        final long keyToken = methodStartToken(token);
        final int keyLine = line;
        // LBRACKET tested in caller.
        next();
        final Expression key = assignmentExpression(false);
        expect(RBRACKET);

        if (type == LPAREN) {
            return new DeferredProperty(keyToken, keyLine, key,
                    methodDefinition(keyToken, keyLine, syntheticMethodName(keyToken, keyLine, null)), null, false);
        }

        expect(COLON);

        return new DeferredProperty(keyToken, keyLine, key, assignmentExpression(false), null, false);
    }

    /**
     * Lower an object literal that has a computed property name. Everything up to the
     * first computed key stays an ObjectNode; the rest is applied to a temporary
     * holding it, so that source order survives:
     *
     * <pre>
     * { a: 1, [k]: 2, b: 3 }   becomes   (:pt = { a: 1 }, :pt[k] = 2, :pt["b"] = 3, :pt)
     * </pre>
     *
     * An immediately invoked function would be the obvious alternative, but every
     * FunctionNode has to stay re-parseable from its own source range, and a synthetic
     * one is not. A comma expression over a temporary introduces no function at all.
     *
     * @param objectToken token of the literal
     * @param objectNode the properties up to the first computed key
     * @param deferred the properties to apply afterwards, in source order
     * @return the lowered expression
     */
    private Expression applyDeferredProperties(final long objectToken, final ObjectNode objectNode,
            final List<DeferredProperty> deferred) {
        final String temporary = newTemporary();
        final int objectFinish = objectNode.getFinish();

        // A synthetic node has to carry the token type its own kind is checked against:
        // Lower.leaveIndexNode asserts that an IndexNode really is one.
        final long identToken = Token.recast(objectToken, TokenType.IDENT);
        final long indexToken = Token.recast(objectToken, TokenType.LBRACKET);

        Expression result = new BinaryNode(Token.recast(objectToken, TokenType.ASSIGN),
                createIdentNode(identToken, objectFinish, temporary), objectNode);

        for (final DeferredProperty property : deferred) {
            final Expression step;

            if (property.accessor == null) {
                final Expression target =
                        new IndexNode(indexToken, objectFinish, createIdentNode(identToken, objectFinish, temporary),
                                property.key);
                step = new BinaryNode(Token.recast(objectToken, TokenType.ASSIGN), target, property.value);
            } else {
                // An accessor is not expressible as an assignment. A property of an
                // object literal is enumerable, unlike a class member.
                step = defineAccessor(property.token, property.line,
                        createIdentNode(identToken, objectFinish, temporary), property.key, property.accessor,
                        property.getter, true);
            }

            result = new BinaryNode(Token.recast(objectToken, TokenType.COMMARIGHT), result, step);
        }

        return new BinaryNode(Token.recast(objectToken, TokenType.COMMARIGHT), result,
                createIdentNode(identToken, objectFinish, temporary));
    }

    /**
     * PropertyName :
     *      IdentifierName
     *      StringLiteral
     *      NumericLiteral
     *
     * See 11.1.5
     *
     * @return PropertyName node
     */
    @SuppressWarnings("fallthrough")
    private PropertyKey propertyName() {
        switch (type) {
        case IDENT:
            return getIdent().setIsPropertyName();
        case OCTAL_LEGACY:
            if (isStrictMode) {
                throw error(AbstractParser.message("strict.no.octal"), token);
            }
        case STRING:
        case ESCSTRING:
        case DECIMAL:
        case HEXADECIMAL:
        case OCTAL:
        case BINARY_NUMBER:
        case FLOATING:
            return getLiteral();
        default:
            return getIdentifierName().setIsPropertyName();
        }
    }

    /**
     * Whether a property name written as a literal rather than as a name starts here.
     * Only a name can be an access node's property or a function's own name, so this is
     * what decides that a member has to be defined by index instead.
     */
    private static boolean isPropertyNameStart(final TokenType type) {
        switch (type) {
        case STRING:
        case ESCSTRING:
        case DECIMAL:
        case HEXADECIMAL:
        case OCTAL:
        case OCTAL_LEGACY:
        case BINARY_NUMBER:
        case FLOATING:
            return true;
        default:
            return false;
        }
    }

    /**
     * PropertyAssignment :
     *      PropertyName : AssignmentExpression
     *      get PropertyName ( ) { FunctionBody }
     *      set PropertyName ( PropertySetParameterList ) { FunctionBody }
     *
     * PropertySetParameterList :
     *      Identifier
     *
     * PropertyName :
     *      IdentifierName
     *      StringLiteral
     *      NumericLiteral
     *
     * See 11.1.5
     *
     * Parse an object literal property.
     *
     * @param deferred where to put an accessor whose name is computed, which no
     *                 PropertyNode can hold
     * @return Property or reference node, or null when the property went into deferred
     */
    private PropertyNode propertyAssignment(final List<DeferredProperty> deferred) {
        // Capture firstToken.
        final long propertyToken = token;
        final int functionLine = line;

        PropertyKey propertyName;

        if (type == IDENT) {
            // Get IDENT.
            final String ident = (String) expectValue(IDENT);

            if (isES6() && type == LPAREN) {
                // ES6 method definition: { m() {} } is { m: function m() {} }, except
                // that the function knows it is a method so that it can be re-parsed.
                final IdentNode methodName = createIdentNode(propertyToken, finish, ident);

                return new PropertyNode(propertyToken, finish, createIdentNode(propertyToken, finish, ident)
                        .setIsPropertyName(), methodDefinition(propertyToken, functionLine, methodName), null, null);
            }

            if (isES6() && (type == COMMARIGHT || type == RBRACE)) {
                // ES6 shorthand: { x } is { x: x }. This is checked before the get and
                // set handling below, so that { get, set } is a pair of shorthands
                // rather than a malformed accessor.
                final IdentNode value = createIdentNode(propertyToken, finish, ident);
                detectSpecialProperty(value);

                return new PropertyNode(propertyToken, finish, createIdentNode(propertyToken, finish, ident)
                        .setIsPropertyName(), value, null, null);
            }

            if (type != COLON) {
                final long getSetToken = propertyToken;

                switch (ident) {
                case "get":
                    final PropertyFunction getter = propertyGetterFunction(getSetToken, functionLine);

                    if (getter.computedKey != null) {
                        deferred.add(new DeferredProperty(propertyToken, functionLine, getter.computedKey, null,
                                getter.functionNode, true));

                        return null;
                    }

                    return new PropertyNode(propertyToken, finish, getter.ident, null, getter.functionNode, null);

                case "set":
                    final PropertyFunction setter = propertySetterFunction(getSetToken, functionLine);

                    if (setter.computedKey != null) {
                        deferred.add(new DeferredProperty(propertyToken, functionLine, setter.computedKey, null,
                                setter.functionNode, false));

                        return null;
                    }

                    return new PropertyNode(propertyToken, finish, setter.ident, null, null, setter.functionNode);
                default:
                    break;
                }
            }

            propertyName = createIdentNode(propertyToken, finish, ident).setIsPropertyName();
        } else {
            propertyName = propertyName();

            if (isES6() && type == LPAREN) {
                // ES6 method definition with a key a name cannot be made of:
                // { "foo bar"() {} } is { "foo bar": function () {} }, except that the
                // function knows it is a method so that it can be re-parsed.
                final long methodToken = methodStartToken(propertyToken);

                return new PropertyNode(propertyToken, finish, propertyName,
                        methodDefinition(methodToken, functionLine,
                                syntheticMethodName(methodToken, functionLine, propertyName.getPropertyName())),
                        null, null);
            }
        }

        expect(COLON);

        defaultNames.push(propertyName);
        try {
            return new PropertyNode(propertyToken, finish, propertyName, assignmentExpression(false), null, null);
        } finally {
            defaultNames.pop();
        }
    }

    /**
     * SuperCall / SuperProperty.
     *
     * A class has no super binding of its own here, so super reads the temporary
     * holding the superclass and the receiver is passed explicitly:
     *
     * <pre>
     * super(x)     becomes   :pt0.call(this, x)
     * super.m(x)   becomes   :pt0.prototype.m.call(this, x)
     * super.m      becomes   :pt0.prototype.m
     * </pre>
     *
     * The last one loses the receiver, as it does wherever a method is read rather
     * than called.
     *
     * @return the lowered expression
     */
    private Expression superExpression() {
        final long superToken = token;
        final int superLine = line;

        if (!superUsable) {
            throw error(AbstractParser.message(inSubclass ? "super.in.class.expression" : "super.outside.class"), superToken);
        }

        next();

        markThisUse();

        if (type == LPAREN) {
            return superCall(superLine, superToken, identifierFor(superToken, SUPERCLASS));
        }

        final Expression prototype = new AccessNode(Token.recast(superToken, TokenType.PERIOD), finish,
                identifierFor(superToken, SUPERCLASS), "prototype");
        final Expression member;

        if (type == LBRACKET) {
            next();
            final Expression property = expression();
            expect(RBRACKET);
            member = new IndexNode(Token.recast(superToken, LBRACKET), finish, prototype, property);
        } else {
            expect(TokenType.PERIOD);
            member = new AccessNode(Token.recast(superToken, TokenType.PERIOD), finish, prototype,
                    getIdentifierName().getName());
        }

        return type == LPAREN ? superCall(superLine, superToken, member) : member;
    }

    /** {@code callee.call(this, args...)}, with the argument list still to be read. */
    private Expression superCall(final int superLine, final long superToken, final Expression callee) {
        final List<Expression> arguments = new ArrayList<>();
        arguments.add(thisFor(Token.recast(superToken, TokenType.THIS)));
        arguments.addAll(argumentList());

        return new CallNode(superLine, superToken, finish, new AccessNode(Token.recast(superToken, TokenType.PERIOD), finish,
                callee, "call"), arguments, false);
    }

    /**
     * Whether an accessor starts at the current {@code get} or {@code set} token.
     *
     * A member named "get" is an ordinary method, so what follows decides: an accessor
     * has a property name after the keyword, a method has its parameter list.
     *
     * @return true if this is a getter or setter definition.
     */
    private boolean isClassAccessor() {
        final Object value = getValue(token);

        if (!"get".equals(value) && !"set".equals(value)) {
            return false;
        }

        final TokenType next = T(k + 1);

        return next != LPAREN && next != RBRACE && next != SEMICOLON && next != ASSIGN;
    }

    /** Where a class member is defined: the class itself when static, its prototype otherwise. */
    private Expression memberTarget(final long memberToken, final String classTemporary, final boolean isStatic) {
        final Expression classRef = identifierFor(memberToken, classTemporary);

        return isStatic ? classRef
                : new AccessNode(Token.recast(memberToken, TokenType.PERIOD), finish, classRef, "prototype");
    }

    /**
     * Define a member of a class body on the class or on its prototype.
     *
     * A class member is not enumerable, which a plain assignment cannot express, and an
     * accessor is not expressible as an assignment at all - the class body has no object
     * literal to carry one the way an object literal member would. A call to
     * {@code Object.defineProperty} would express both, but it would read {@code Object}
     * out of whatever scope the class is written in, so a local of that name would break
     * every class below it. A runtime request names nothing.
     *
     * @param memberToken token the synthetic node is attributed to
     * @param target the object to define the member on
     * @param key the member name, which may be an expression
     * @param value the member, a method or one half of an accessor pair
     * @param getter TRUE or FALSE for an accessor, null for a method
     * @return the definition
     */
    private Expression defineMember(final long memberToken, final Expression target, final Expression key,
            final Expression value, final Boolean getter) {
        final long token = Token.recast(memberToken, IDENT);

        return getter == null ? new RuntimeNode(token, finish, RuntimeNode.Request.DEFINE_METHOD, target, key, value)
                : new RuntimeNode(token, finish, RuntimeNode.Request.DEFINE_ACCESSOR, target, key, value,
                        LiteralNode.newInstance(token, finish, getter.booleanValue()));
    }

    /**
     * {@code Object.defineProperty(target, key, {get: fn, configurable: true, enumerable: ..})},
     * for an accessor of an object literal whose key is an expression. An accessor is
     * not expressible as an assignment, and an object literal cannot carry a computed
     * key, so the property is defined after the literal is built.
     *
     * @param memberToken token the synthetic nodes are attributed to
     * @param memberLine line the synthetic nodes are attributed to
     * @param target the object to define the accessor on
     * @param key the property name, which may be an expression
     * @param accessor the parsed accessor
     * @param getter true for a getter, false for a setter
     * @param enumerable whether the property is enumerable
     * @return the defineProperty call
     */
    private Expression defineAccessor(final long memberToken, final int memberLine, final Expression target,
            final Expression key, final FunctionNode accessor, final boolean getter, final boolean enumerable) {
        final long token = Token.recast(memberToken, IDENT);
        final List<PropertyNode> descriptor = new ArrayList<>();

        descriptor.add(new PropertyNode(token, finish, identifierFor(token, getter ? "get" : "set").setIsPropertyName(),
                accessor, null, null));
        descriptor.add(new PropertyNode(token, finish, identifierFor(token, "configurable").setIsPropertyName(),
                LiteralNode.newInstance(token, finish, true), null, null));
        descriptor.add(new PropertyNode(token, finish, identifierFor(token, "enumerable").setIsPropertyName(),
                LiteralNode.newInstance(token, finish, enumerable), null, null));

        final List<Expression> arguments = new ArrayList<>();
        arguments.add(target);
        arguments.add(key);
        arguments.add(new ObjectNode(token, finish, descriptor));

        return new CallNode(memberLine, token, finish,
                new AccessNode(Token.recast(memberToken, TokenType.PERIOD), finish, identifierFor(token, "Object"),
                        "defineProperty"),
                arguments, false);
    }

    /**
     * ClassDeclaration / ClassExpression :
     *      class BindingIdentifier? ClassTail
     *
     * A class lowers to an ES5 constructor with its prototype chains wired by hand:
     *
     * <pre>
     * class Foo extends Bar { constructor(x) {..} m() {..} static s() {..} }
     * </pre>
     * <pre>
     * (:pt0 = Bar,
     *  :pt1 = function Foo(x) {..},
     *  :pt1.prototype.__proto__ = :pt0.prototype,
     *  :pt1.__proto__ = :pt0,
     *  :pt1.prototype.m = function m() {..},
     *  :pt1.s = function s() {..},
     *  :pt1)
     * </pre>
     *
     * An immediately invoked function would be the obvious shape, but every
     * FunctionNode has to stay re-parseable from its own source range and a synthetic
     * one is not - hence the comma expression over temporaries, which introduces no
     * function at all. It also serves both forms: a declaration is this expression
     * assigned to a var.
     *
     * @param wanted what the caller wants back, so that re-parsing the constructor a
     *               class did not write out can ask for just that
     * @return the class expression, or the synthesized constructor
     */
    private Expression classTail(final ProgramKind wanted, final boolean declaration) {
        final long classToken = token;
        final int classLine = line;
        // CLASS tested in caller.
        next();

        final boolean prevStrictMode = isStrictMode;
        final boolean prevInSubclass = inSubclass;
        final boolean prevSuperUsable = superUsable;
        // Everything from the class keyword to the closing brace is strict code, and
        // newFunctionNode reads this flag, so every method the body defines is strict
        // too. Re-parsing one lazily keeps that: RecompilableScriptFunctionData hands
        // its own strictness to the parser it opens, so a method reparsed from a source
        // range with no class keyword in it is still strict.
        isStrictMode = true;
        try {
            IdentNode className = null;

            if (type == IDENT || isNonStrictModeIdent()) {
                className = getIdent();
                verifyStrictIdent(className, "class name");
            }

            Expression superClass = null;

            if (type == EXTENDS) {
                next();
                superClass = leftHandSideExpression();
            }

            inSubclass = superClass != null;
            // A declaration gives the binding a block of its own; an expression has
            // nowhere to put one, so super inside it would read whichever class in the
            // scope assigned it last.
            superUsable = inSubclass && declaration;

            final String classTemporary = newTemporary();
            final IdentNode constructorName = className != null ? className
                    : createIdentNode(Token.recast(classToken, IDENT), finish,
                            getDefaultValidFunctionName(classLine, false));

            // A function is identified by where it starts, and the program itself starts
            // at zero, so a synthesized constructor cannot be identified by the class
            // keyword: a class at the very start of a file would collide with it. The
            // brace that opens the class body is inside the class, is never zero and is
            // reached identically when the class is re-parsed.
            final long classBodyToken = token;
            expect(LBRACE);

            FunctionNode constructor = null;
            final List<Expression> members = new ArrayList<>();

            while (type != RBRACE) {
                if (type == SEMICOLON) {
                    // A stray semicolon between members is allowed.
                    next();
                    continue;
                }

                boolean isStatic = false;

                if (type == TokenType.STATIC && T(k + 1) != LPAREN) {
                    // "static" is a reserved word, so it is its own token type. A
                    // parameter list right after it makes it an ordinary method name.
                    isStatic = true;
                    next();
                }

                final long memberToken = token;
                final int memberLine = line;

                if ((type == IDENT || isNonStrictModeIdent()) && isClassAccessor()) {
                    // get x() {} / set x(v) {}, read exactly as an object literal reads
                    // them, but defined on the target rather than collected into a
                    // literal - there is no object literal here to hold an accessor.
                    final boolean getter = "get".equals((String) getValue(token));
                    next();

                    final PropertyFunction accessor = getter ? propertyGetterFunction(memberToken, memberLine)
                            : propertySetterFunction(memberToken, memberLine);
                    final Expression accessorKey = accessor.computedKey != null ? accessor.computedKey
                            : LiteralNode.newInstance(memberToken, finish, accessor.ident.getPropertyName());

                    members.add(defineMember(memberToken, memberTarget(memberToken, classTemporary, isStatic),
                            accessorKey, accessor.functionNode, Boolean.valueOf(getter)));

                    continue;
                }

                // A key that is not a plain name is kept as an expression: a member is
                // then defined by index rather than by an access node, whose name would
                // otherwise end up inside a call site name.
                final long methodToken = methodStartToken(memberToken);
                final Expression memberKey;
                final IdentNode memberName;

                if (type == LBRACKET) {
                    next();
                    memberKey = assignmentExpression(false);
                    expect(RBRACKET);
                    memberName = syntheticMethodName(methodToken, memberLine, null);
                } else if (isPropertyNameStart(type)) {
                    final PropertyKey key = propertyName();
                    memberKey = LiteralNode.newInstance(memberToken, finish, key.getPropertyName());
                    memberName = syntheticMethodName(methodToken, memberLine, key.getPropertyName());
                } else {
                    memberKey = null;
                    memberName = getIdentifierName();
                }

                if (memberKey == null && !isStatic && "constructor".equals(memberName.getName())) {
                    if (constructor != null) {
                        throw error(AbstractParser.message("duplicate.constructor"), memberToken);
                    }
                    constructor = methodDefinition(methodToken, memberLine, constructorName, true);

                    continue;
                }

                final FunctionNode method = methodDefinition(methodToken, memberLine, memberName);
                final Expression methodKey = memberKey == null
                        ? LiteralNode.newInstance(memberToken, finish, memberName.getName())
                        : memberKey;

                members.add(defineMember(memberToken, memberTarget(memberToken, classTemporary, isStatic), methodKey,
                        method, null));
            }

            expect(RBRACE);

            if (constructor == null) {
                constructor = implicitConstructor(classToken, classBodyToken, classLine, constructorName);
            }

            if (wanted == ProgramKind.CLASS_CONSTRUCTOR) {
                return constructor;
            }

            final List<Expression> steps = new ArrayList<>();

            if (superClass != null) {
                steps.add(new BinaryNode(Token.recast(classToken, TokenType.ASSIGN),
                        identifierFor(classToken, SUPERCLASS), superClass));
            }

            steps.add(new BinaryNode(Token.recast(classToken, TokenType.ASSIGN),
                    identifierFor(classToken, classTemporary), constructor));

            if (superClass != null) {
                // Instances see the superclass prototype, and the class itself sees the
                // superclass, which is how a static member is inherited. Both have to ask
                // whether there is one first, because ES6 allows "extends null": that
                // ends the instances' prototype chain immediately and leaves the class
                // itself with Function.prototype. "extends undefined" stays a TypeError,
                // which reading the prototype of undefined below still produces.
                steps.add(protoAssignment(classToken, new AccessNode(Token.recast(classToken, TokenType.PERIOD), finish,
                        identifierFor(classToken, classTemporary), "prototype"),
                        new TernaryNode(Token.recast(classToken, TokenType.TERNARY), superclassIsNull(classToken),
                                new JoinPredecessorExpression(LiteralNode.newInstance(classToken, finish)),
                                new JoinPredecessorExpression(new AccessNode(Token.recast(classToken, TokenType.PERIOD),
                                        finish, identifierFor(classToken, SUPERCLASS), "prototype")))));
                steps.add(new BinaryNode(Token.recast(classToken, TokenType.OR),
                        new JoinPredecessorExpression(superclassIsNull(classToken)),
                        new JoinPredecessorExpression(protoAssignment(classToken,
                                identifierFor(classToken, classTemporary), identifierFor(classToken, SUPERCLASS)))));
            }

            steps.addAll(members);
            steps.add(identifierFor(classToken, classTemporary));

            Expression result = steps.get(0);

            for (int i = 1; i < steps.size(); i++) {
                result = new BinaryNode(Token.recast(classToken, TokenType.COMMARIGHT), result, steps.get(i));
            }

            return result;
        } finally {
            isStrictMode = prevStrictMode;
            inSubclass = prevInSubclass;
            superUsable = prevSuperUsable;
        }
    }

    /** {@code :superclass === null}, the test that tells "extends null" from a real base. */
    private Expression superclassIsNull(final long classToken) {
        return new BinaryNode(Token.recast(classToken, TokenType.EQ_STRICT), identifierFor(classToken, SUPERCLASS),
                LiteralNode.newInstance(classToken, finish));
    }

    private Expression protoAssignment(final long classToken, final Expression target, final Expression value) {
        return new BinaryNode(Token.recast(classToken, TokenType.ASSIGN),
                new AccessNode(Token.recast(classToken, TokenType.PERIOD), finish, target, "__proto__"), value);
    }

    /**
     * Build the constructor a class did not write out. There is no source for it, so it
     * is identified by the class token and re-parsed from the class as a whole.
     *
     * A base class gets an empty constructor; a derived one forwards everything it was
     * given to its superclass.
     */
    private FunctionNode implicitConstructor(final long classToken, final long classBodyToken, final int classLine,
            final IdentNode className) {
        final List<String> prevTemporaries = temporaries;
        temporaries = new ArrayList<>();

        FunctionNode constructor = null;
        try {
            constructor = newFunctionNode(classBodyToken, classToken, className, new ArrayList<IdentNode>(),
                    FunctionNode.Kind.CLASS_CONSTRUCTOR, classLine);

            // A function the re-parse is not aiming at is skipped, and the compiler
            // requires a skipped function to have an empty body. functionBody() decides
            // that the same way for the functions it parses.
            final boolean parseBody = reparsedFunction == null
                    || constructor.getId() <= reparsedFunction.getFunctionNodeId();

            if (parseBody) {
                if (inSubclass) {
                    lc.setFlag(constructor, FunctionNode.USES_ARGUMENTS | FunctionNode.USES_THIS);

                    final List<Expression> arguments = new ArrayList<>();
                    arguments.add(new IdentNode(Token.recast(classToken, TokenType.THIS), finish, TokenType.THIS.getName()));
                    arguments.add(identifierFor(classToken, ARGUMENTS_NAME));

                    appendStatement(new ExpressionStatement(classLine, classToken, finish, new CallNode(classLine, classToken,
                            finish, new AccessNode(Token.recast(classToken, TokenType.PERIOD), finish,
                                    identifierFor(classToken, SUPERCLASS), "apply"), arguments, false)));
                }

                requireNew(constructor, classToken);
            }

            constructor.setFinish(finish);
        } finally {
            temporaries = prevTemporaries;
            // The range has to stop at the brace that closes the class, which is the
            // token just consumed, not wherever the parser goes next.
            constructor = restoreFunctionNode(constructor, previousToken);
        }

        return constructor;
    }

    /**
     * MethodDefinition :
     *      PropertyName ( FormalParameterList ) { FunctionBody }
     *
     * Parse the parameter list and body of an ES6 method definition, with the name
     * already read. The result is an ordinary function of kind METHOD; the kind is
     * what tells the re-parser that this range is a method rather than a program.
     *
     * @param methodToken token the method starts at, so that its source range covers
     *                    the name as well
     * @param methodLine line the method starts on
     * @param name name of the method
     * @return the function
     */
    private FunctionNode methodDefinition(final long methodToken, final int methodLine, final IdentNode name) {
        return methodDefinition(methodToken, methodLine, name, false);
    }

    private FunctionNode methodDefinition(final long methodToken, final int methodLine, final IdentNode name,
            final boolean classConstructor) {
        expect(LPAREN);
        final Parameters parameters = new Parameters();
        formalParameterList(RPAREN, parameters);
        expect(RPAREN);

        return functionBody(methodToken, name, parameters, FunctionNode.Kind.METHOD, methodLine, classConstructor);
    }

    /**
     * The token a method definition starts at, given the token its key starts at.
     *
     * A string token reports its position past the opening quote, and the source range
     * of a method has to cover the whole key: the range is what the method is re-parsed
     * from, and the re-parse has to arrive at the same method. The token is recast
     * because what sits at that position is a property key, not a string to scan again.
     */
    private static long methodStartToken(final long keyToken) {
        return Token.recast(Token.withDelimiter(keyToken), IDENT);
    }

    /**
     * Name the function of a method whose key is not a plain identifier. A string, a
     * number or an expression is not usable as a name, so the function is named the way
     * an anonymous one written at that point would be. The name is built out of the key
     * and the line alone, so that re-parsing the method on its own arrives at the same
     * one.
     *
     * @param key the key as it was written, or null when it is computed
     */
    private IdentNode syntheticMethodName(final long methodToken, final int methodLine, final String key) {
        return createIdentNode(Token.recast(methodToken, IDENT), finish,
                isValidIdentifier(key) ? key : ANON_FUNCTION_PREFIX.symbolName() + methodLine);
    }

    private PropertyFunction propertyGetterFunction(final long getSetToken, final int functionLine) {
        if (isES6() && type == LBRACKET) {
            final long keyToken = token;
            next();
            final Expression key = assignmentExpression(false);
            expect(RBRACKET);
            expect(LPAREN);
            expect(RPAREN);

            return new PropertyFunction(null, functionBody(getSetToken, accessorName(keyToken, true), new Parameters(),
                    FunctionNode.Kind.GETTER, functionLine), key);
        }

        final PropertyKey getIdent = propertyName();
        final String getterName = getIdent.getPropertyName();
        final IdentNode getNameNode = createIdentNode(((Node) getIdent).getToken(), finish, NameCodec.encode("get " + getterName));
        expect(LPAREN);
        expect(RPAREN);
        final FunctionNode functionNode =
                functionBody(getSetToken, getNameNode, new Parameters(), FunctionNode.Kind.GETTER, functionLine);

        return new PropertyFunction(getIdent, functionNode);
    }

    /**
     * Name the function of an accessor whose key is computed. There is no name to build
     * one from, so it is the bare prefix an accessor's name carries: the runtime takes
     * the property name back out of it by cutting that prefix off, and here nothing is
     * left over.
     */
    private IdentNode accessorName(final long keyToken, final boolean getter) {
        return createIdentNode(Token.recast(keyToken, IDENT), finish, NameCodec.encode(getter ? "get " : "set "));
    }

    private PropertyFunction propertySetterFunction(final long getSetToken, final int functionLine) {
        if (isES6() && type == LBRACKET) {
            final long keyToken = token;
            next();
            final Expression key = assignmentExpression(false);
            expect(RBRACKET);

            return new PropertyFunction(null, setterBody(getSetToken, accessorName(keyToken, false), functionLine), key);
        }

        final PropertyKey setIdent = propertyName();
        final String setterName = setIdent.getPropertyName();
        final IdentNode setNameNode = createIdentNode(((Node) setIdent).getToken(), finish, NameCodec.encode("set " + setterName));

        return new PropertyFunction(setIdent, setterBody(getSetToken, setNameNode, functionLine));
    }

    /** The parameter list and body of a setter, with its name already read. */
    private FunctionNode setterBody(final long getSetToken, final IdentNode setNameNode, final int functionLine) {
        expect(LPAREN);
        // be sloppy and allow missing setter parameter even though
        // spec does not permit it!
        final IdentNode argIdent;
        if (type == IDENT || isNonStrictModeIdent()) {
            argIdent = getIdent();
            verifyStrictIdent(argIdent, "setter argument");
        } else {
            argIdent = null;
        }
        expect(RPAREN);
        final Parameters parameters = new Parameters();
        if (argIdent != null) {
            parameters.list.add(argIdent);
            parameters.setups.add(null);
        }

        return functionBody(getSetToken, setNameNode, parameters, FunctionNode.Kind.SETTER, functionLine);
    }

    private static class PropertyFunction {
        /** The key, when it was written as a name, a string or a number. */
        final PropertyKey ident;
        final FunctionNode functionNode;
        /** The key expression, when it was written as a computed name. */
        final Expression computedKey;

        PropertyFunction(final PropertyKey ident, final FunctionNode function) {
            this(ident, function, null);
        }

        PropertyFunction(final PropertyKey ident, final FunctionNode function, final Expression computedKey) {
            this.ident = ident;
            this.functionNode = function;
            this.computedKey = computedKey;
        }
    }

    /**
     * LeftHandSideExpression :
     *      NewExpression
     *      CallExpression
     *
     * CallExpression :
     *      MemberExpression Arguments
     *      CallExpression Arguments
     *      CallExpression [ Expression ]
     *      CallExpression . IdentifierName
     *
     * See 11.2
     *
     * Parse left hand side expression.
     * @return Expression node.
     */
    private Expression leftHandSideExpression() {
        int callLine = line;
        long callToken = token;

        Expression lhs = memberExpression();

        if (type == LPAREN) {
            final List<Expression> arguments = optimizeList(argumentList());

            // Catch special functions.
            if (lhs instanceof IdentNode) {
                detectSpecialFunction((IdentNode) lhs);
            }

            lhs = hasSpread(arguments) ? spreadCall(callLine, callToken, lhs, arguments)
                    : new CallNode(callLine, callToken, finish, lhs, arguments, false);
        }

        loop: while (true) {
            // Capture token.
            callLine = line;
            callToken = token;

            switch (type) {
            case LPAREN:
                // Get NEW or FUNCTION arguments.
                final List<Expression> arguments = optimizeList(argumentList());

                // Create call node.
                lhs = hasSpread(arguments) ? spreadCall(callLine, callToken, lhs, arguments)
                        : new CallNode(callLine, callToken, finish, lhs, arguments, false);

                break;

            case LBRACKET:
                next();

                // Get array index.
                final Expression rhs = expression();

                expect(RBRACKET);

                // Create indexing node.
                lhs = new IndexNode(callToken, finish, lhs, rhs);

                break;

            case PERIOD:
                next();

                final IdentNode property = getIdentifierName();

                // Create property access node.
                lhs = new AccessNode(callToken, finish, lhs, property.getName());

                break;

            case TEMPLATE:
            case TEMPLATE_HEAD:
                // A template directly after an expression is a tagged template,
                // which needs the literal parts as data rather than concatenated.
                lhs = taggedTemplate(callLine, callToken, lhs);

                break;

            default:
                break loop;
            }
        }

        return lhs;
    }

    /**
     * TemplateLiteral :
     *      NoSubstitutionTemplate
     *      TemplateHead Expression TemplateSpans
     *
     * Parse a template literal, lowering it to a string concatenation. The
     * concatenation starts from the head, which is a string literal even when it is
     * empty, so that every substitution is concatenated rather than added:
     * {@code `${1}${2}`} is "12", not 3.
     *
     * @return Expression node.
     */
    private Expression templateLiteral() {
        assert type == TEMPLATE || type == TEMPLATE_HEAD;

        if (type == TEMPLATE) {
            return getLiteral();
        }

        final long templateToken = token;
        Expression concat = getLiteral();

        templateSubstitutions++;

        try {
            while (true) {
                concat = new BinaryNode(Token.recast(templateToken, TokenType.ADD), concat, expression());

                if (type != TEMPLATE_MIDDLE && type != TEMPLATE_TAIL) {
                    throw error(AbstractParser.message("expected.literal", "template"), token);
                }

                final boolean last = type == TEMPLATE_TAIL;
                concat = new BinaryNode(Token.recast(templateToken, TokenType.ADD), concat, getLiteral());

                if (last) {
                    return concat;
                }
            }
        } finally {
            templateSubstitutions--;
        }
    }

    /**
     * MemberExpression TemplateLiteral
     *
     * Parse a tagged template, which hands the literal parts to the tag as data rather
     * than concatenating them:
     *
     * <pre>
     * tag`a${x}b`   becomes   tag(TEMPLATE_OBJECT("&lt;key&gt;", ["a", "b"], ["a", "b"]), x)
     * </pre>
     *
     * The strings are data, so the call site needs an identity of its own: the same site
     * has to produce the same frozen object every time it runs, including after a
     * recompilation. A compiled constant would not survive one, but a position in the
     * source does, so the key is the source and the position of the template.
     *
     * @param line line of the call
     * @param callToken token the tag expression starts at
     * @param tag the expression being tagged with the template
     * @return the call
     */
    private Expression taggedTemplate(final int line, final long callToken, final Expression tag) {
        assert type == TEMPLATE || type == TEMPLATE_HEAD;

        final long templateToken = token;
        final List<Expression> cooked = new ArrayList<>();
        final List<Expression> raw = new ArrayList<>();
        final List<Expression> substitutions = new ArrayList<>();

        boolean last = type == TEMPLATE;
        addTemplatePart(cooked, raw);

        if (!last) {
            templateSubstitutions++;

            try {
                while (true) {
                    substitutions.add(expression());

                    if (type != TEMPLATE_MIDDLE && type != TEMPLATE_TAIL) {
                        throw error(AbstractParser.message("expected.literal", "template"), token);
                    }

                    last = type == TEMPLATE_TAIL;
                    addTemplatePart(cooked, raw);

                    if (last) {
                        break;
                    }
                }
            } finally {
                templateSubstitutions--;
            }
        }

        final String key = source.getDigest() + "#" + Token.descPosition(templateToken);

        final List<Expression> arguments = new ArrayList<>();
        arguments.add(new RuntimeNode(templateToken, finish, RuntimeNode.Request.TEMPLATE_OBJECT,
                LiteralNode.newInstance(templateToken, finish, key), arrayOf(templateToken, cooked),
                arrayOf(templateToken, raw)));
        arguments.addAll(substitutions);

        // A method call keeps its receiver by itself - CallNode reads it back out of the
        // access node - so unlike spreadCall there is no temporary to introduce here.
        return new CallNode(line, Token.recast(callToken, LPAREN), finish, tag, arguments, false);
    }

    /**
     * Take one literal part of a template as data: the cooked value an ordinary template
     * would concatenate, and the raw text exactly as it was written.
     */
    private void addTemplatePart(final List<Expression> cooked, final List<Expression> raw) {
        final long partToken = token;
        final String rawText = source.getString(Token.descPosition(partToken), Token.descLength(partToken));
        // An escape that is invalid everywhere else is allowed in a tagged template; only
        // the cooked value of the part it is in goes missing.
        final String cookedText = lexer.valueOfTemplateCooked(partToken, isStrictMode);

        next();

        raw.add(LiteralNode.newInstance(partToken, finish, Lexer.normalizeEOL(rawText)));
        cooked.add(cookedText == null ? LiteralNode.newInstance(partToken, finish, ScriptRuntime.UNDEFINED)
                : LiteralNode.newInstance(partToken, finish, cookedText));
    }

    /**
     * NewExpression :
     *      MemberExpression
     *      new NewExpression
     *
     * See 11.2
     *
     * Parse new expression.
     * @return Expression node.
     */
    private Expression newExpression() {
        final long newToken = token;
        // NEW is tested in caller.
        next();

        // Get function base.
        final int callLine = line;
        final Expression constructor = memberExpression();
        if (constructor == null) {
            return null;
        }
        // Get arguments.
        ArrayList<Expression> arguments;

        // Allow for missing arguments.
        if (type == LPAREN) {
            arguments = argumentList();

            if (hasSpread(arguments)) {
                // Spreading into a call goes through apply, which cannot construct.
                throw error(AbstractParser.message("no.spread.in.new"), constructor.getToken());
            }
        } else {
            arguments = new ArrayList<>();
        }

        // Sai extension: This is to support the following interface implementation
        // syntax:
        //
        //     var r = new java.lang.Runnable() {
        //         run: function() { println("run"); }
        //     };
        //
        // The object literal following the "new Constructor()" expression
        // is passed as an additional (last) argument to the constructor.
        if (!env._no_syntax_extensions && type == LBRACE) {
            arguments.add(objectLiteral());
        }

        final CallNode callNode = new CallNode(callLine, constructor.getToken(), finish, constructor, optimizeList(arguments), true);

        return new UnaryNode(newToken, callNode);
    }

    /**
     * MemberExpression :
     *      PrimaryExpression
     *      FunctionExpression
     *      MemberExpression [ Expression ]
     *      MemberExpression . IdentifierName
     *      new MemberExpression Arguments
     *
     * See 11.2
     *
     * Parse member expression.
     * @return Expression node.
     */
    private Expression memberExpression() {
        // Prepare to build operation.
        Expression lhs;

        switch (type) {
        case NEW:
            // Get new expression.
            lhs = newExpression();
            break;

        case FUNCTION:
            // Get function expression.
            lhs = functionExpression(false, false);
            break;

        case SUPER:
            if (isES6()) {
                lhs = superExpression();
                break;
            }
            lhs = primaryExpression();
            break;

        default:
            // Get primary expression.
            lhs = primaryExpression();
            break;
        }

        loop: while (true) {
            // Capture token.
            final long callToken = token;

            switch (type) {
            case LBRACKET:
                next();

                // Get array index.
                final Expression index = expression();

                expect(RBRACKET);

                // Create indexing node.
                lhs = new IndexNode(callToken, finish, lhs, index);

                break;

            case PERIOD:
                if (lhs == null) {
                    throw error(AbstractParser.message("expected.operand", type.getNameOrType()));
                }

                next();

                final IdentNode property = getIdentifierName();

                // Create property access node.
                lhs = new AccessNode(callToken, finish, lhs, property.getName());

                break;

            case TEMPLATE:
            case TEMPLATE_HEAD:
                // A template binds to the member expression before it, which is what
                // makes "new tag`x`" construct what the tag returns rather than tag
                // itself. A template token is only ever produced in ES6 mode.
                lhs = taggedTemplate(line, callToken, lhs);

                break;

            default:
                break loop;
            }
        }

        return lhs;
    }

    /**
     * Arguments :
     *      ( )
     *      ( ArgumentList )
     *
     * ArgumentList :
     *      AssignmentExpression
     *      ArgumentList , AssignmentExpression
     *
     * See 11.2
     *
     * Parse function call arguments.
     * @return Argument list.
     */
    private ArrayList<Expression> argumentList() {
        // Prepare to accumulate list of arguments.
        final ArrayList<Expression> nodeList = new ArrayList<>();
        // LPAREN tested in caller.
        next();

        // Track commas.
        boolean first = true;

        while (type != RPAREN) {
            // Comma prior to every argument except the first.
            if (!first) {
                expect(COMMARIGHT);
            } else {
                first = false;
            }

            // Get argument expression.
            nodeList.add(isES6() && type == ELLIPSIS ? spreadElement() : assignmentExpression(false));
        }

        expect(RPAREN);
        return nodeList;
    }

    private static <T> List<T> optimizeList(final ArrayList<T> list) {
        switch (list.size()) {
        case 0: {
            return Collections.emptyList();
        }
        case 1: {
            return Collections.singletonList(list.get(0));
        }
        default: {
            list.trimToSize();
            return list;
        }
        }
    }

    /**
     * FunctionDeclaration :
     *      function Identifier ( FormalParameterList? ) { FunctionBody }
     *
     * FunctionExpression :
     *      function Identifier? ( FormalParameterList? ) { FunctionBody }
     *
     * See 13
     *
     * Parse function declaration.
     * @param isStatement True if for is a statement.
     *
     * @return Expression node.
     */
    private Expression functionExpression(final boolean isStatement, final boolean topLevel) {
        final long functionToken = token;
        final int functionLine = line;
        // FUNCTION is tested in caller.
        next();

        IdentNode name = null;

        if (type == IDENT || isNonStrictModeIdent()) {
            name = getIdent();
            verifyStrictIdent(name, "function name");
        } else if (isStatement) {
            // Sai extension: anonymous function statements.
            // Do not allow anonymous function statement if extensions
            // are now allowed. But if we are reparsing then anon function
            // statement is possible - because it was used as function
            // expression in surrounding code.
            if (env._no_syntax_extensions && reparsedFunction == null) {
                expect(IDENT);
            }
        }

        // name is null, generate anonymous name
        boolean isAnonymous = false;
        if (name == null) {
            final String tmpName = getDefaultValidFunctionName(functionLine, isStatement);
            name = new IdentNode(functionToken, Token.descPosition(functionToken), tmpName);
            isAnonymous = true;
        }

        expect(LPAREN);
        final Parameters allParameters = new Parameters();
        formalParameterList(RPAREN, allParameters);
        final List<IdentNode> parameters = allParameters.list;
        expect(RPAREN);

        FunctionNode functionNode;
        // Hide the current default name across function boundaries. E.g. "x3 = function x1() { function() {}}"
        // If we didn't hide the current default name, then the innermost anonymous function would receive "x3".
        hideDefaultName();
        try {
            functionNode = functionBody(functionToken, name, allParameters, FunctionNode.Kind.NORMAL, functionLine);
        } finally {
            defaultNames.pop();
        }

        if (isStatement) {
            if (topLevel || useBlockScope()) {
                functionNode = functionNode.setFlag(lc, FunctionNode.IS_DECLARED);
            } else if (isStrictMode) {
                throw error(JSErrorType.SYNTAX_ERROR, AbstractParser.message("strict.no.func.decl.here"), functionToken);
            } else if (env._function_statement == ScriptEnvironment.FunctionStatementBehavior.ERROR) {
                throw error(JSErrorType.SYNTAX_ERROR, AbstractParser.message("no.func.decl.here"), functionToken);
            } else if (env._function_statement == ScriptEnvironment.FunctionStatementBehavior.WARNING) {
                warning(JSErrorType.SYNTAX_ERROR, AbstractParser.message("no.func.decl.here.warn"), functionToken);
            }
            if (isArguments(name)) {
                lc.setFlag(lc.getCurrentFunction(), FunctionNode.DEFINES_ARGUMENTS);
            }
        }

        if (isAnonymous) {
            functionNode = functionNode.setFlag(lc, FunctionNode.IS_ANONYMOUS);
        }

        final int arity = parameters.size();

        final boolean strict = functionNode.isStrict();
        if (arity > 1) {
            final HashSet<String> parametersSet = new HashSet<>(arity);

            for (int i = arity - 1; i >= 0; i--) {
                final IdentNode parameter = parameters.get(i);
                String parameterName = parameter.getName();

                if (isArguments(parameterName)) {
                    functionNode = functionNode.setFlag(lc, FunctionNode.DEFINES_ARGUMENTS);
                }

                if (parametersSet.contains(parameterName)) {
                    // redefinition of parameter name
                    if (strict) {
                        throw error(AbstractParser.message("strict.param.redefinition", parameterName), parameter.getToken());
                    }
                    // rename in non-strict mode
                    parameterName = functionNode.uniqueName(parameterName);
                    final long parameterToken = parameter.getToken();
                    parameters.set(i,
                            new IdentNode(parameterToken, Token.descPosition(parameterToken), functionNode.uniqueName(parameterName)));
                }

                parametersSet.add(parameterName);
            }
        } else if (arity == 1) {
            if (isArguments(parameters.get(0))) {
                functionNode = functionNode.setFlag(lc, FunctionNode.DEFINES_ARGUMENTS);
            }
        }

        if (isStatement) {
            if (isAnonymous) {
                appendStatement(new ExpressionStatement(functionLine, functionToken, finish, functionNode));
                return functionNode;
            }

            final int varFlags = (topLevel || !useBlockScope()) ? 0 : VarNode.IS_LET;
            final VarNode varNode = new VarNode(functionLine, functionToken, finish, name, functionNode, varFlags);
            if (topLevel) {
                functionDeclarations.add(varNode);
            } else if (useBlockScope()) {
                prependStatement(varNode); // Hoist to beginning of current block
            } else {
                appendStatement(varNode);
            }
        }

        return functionNode;
    }

    /**
     * Look ahead for the arrow of an arrow function: an identifier, or a
     * parenthesised list, followed by {@code =>}. Only token types are read; nothing
     * is consumed, and the lookahead buffer grows as needed.
     *
     * A line terminator is not allowed before the arrow, and EOL is a token of its
     * own, so no EOL skipping happens here.
     *
     * @return true if an arrow function starts at the current token.
     */
    private boolean isArrowFunction() {
        return lookahead(this::isArrowFunctionAhead);
    }

    private boolean isArrowFunctionAhead() {
        if (type == IDENT || isNonStrictModeIdent()) {
            return T(k + 1) == ARROW;
        }

        if (type != LPAREN) {
            return false;
        }

        // Find the parenthesis that closes this one. What is in between only has to
        // balance here; it is parsed properly once this is known to be an arrow.
        int depth = 0;

        for (int i = k;; i++) {
            final TokenType tokenType = T(i);

            if (tokenType == LPAREN) {
                depth++;
            } else if (tokenType == RPAREN) {
                if (--depth == 0) {
                    return T(i + 1) == ARROW;
                }
            } else if (tokenType == EOF) {
                return false;
            }
        }
    }

    /**
     * ArrowFunction :
     *      ArrowParameters => ConciseBody
     *
     * Parse an arrow function. The result is an ordinary FunctionNode of kind ARROW,
     * spanning the whole arrow expression, so that it stays re-parseable from its own
     * source range.
     *
     * @return Expression node.
     */
    private Expression arrowFunction() {
        final long arrowToken = token;
        final int arrowLine = line;

        final Parameters parameters = new Parameters();

        if (type == LPAREN) {
            next();
            formalParameterList(RPAREN, parameters);
            expect(RPAREN);
        } else {
            final IdentNode parameter = getIdent();
            verifyStrictIdent(parameter, "function parameter");
            parameters.list.add(parameter);
            parameters.setups.add(null);
        }

        expect(ARROW);

        final IdentNode name =
                new IdentNode(arrowToken, Token.descPosition(arrowToken), getDefaultValidFunctionName(arrowLine, false));

        FunctionNode functionNode;
        hideDefaultName();
        try {
            functionNode = functionBody(arrowToken, name, parameters, FunctionNode.Kind.ARROW, arrowLine);
        } finally {
            defaultNames.pop();
        }

        return functionNode.setFlag(lc, FunctionNode.IS_ANONYMOUS);
    }

    private String getDefaultValidFunctionName(final int functionLine, final boolean isStatement) {
        final String defaultFunctionName = getDefaultFunctionName();
        if (isValidIdentifier(defaultFunctionName)) {
            if (isStatement) {
                // The name will be used as the LHS of a symbol assignment. We add the anonymous function
                // prefix to ensure that it can't clash with another variable.
                return ANON_FUNCTION_PREFIX.symbolName() + defaultFunctionName;
            }
            return defaultFunctionName;
        }
        return ANON_FUNCTION_PREFIX.symbolName() + functionLine;
    }

    private static boolean isValidIdentifier(final String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(name.charAt(0))) {
            return false;
        }
        for (int i = 1; i < name.length(); ++i) {
            if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String getDefaultFunctionName() {
        if (!defaultNames.isEmpty()) {
            final Object nameExpr = defaultNames.peek();
            if (nameExpr instanceof PropertyKey) {
                markDefaultNameUsed();
                return ((PropertyKey) nameExpr).getPropertyName();
            } else if (nameExpr instanceof AccessNode) {
                markDefaultNameUsed();
                return ((AccessNode) nameExpr).getProperty();
            }
        }
        return null;
    }

    private void markDefaultNameUsed() {
        defaultNames.pop();
        hideDefaultName();
    }

    private void hideDefaultName() {
        // Can be any value as long as getDefaultFunctionName doesn't recognize it as something it can extract a value
        // from. Can't be null
        defaultNames.push("");
    }

    /**
     * FormalParameterList :
     *      Identifier
     *      FormalParameterList , Identifier
     *
     * See 13
     *
     * Parse function parameter list.
     * @return List of parameter nodes.
     */
    private List<IdentNode> formalParameterList() {
        return formalParameterList(RPAREN, null);
    }

    /**
     * Same as the other method of the same name - except that the end
     * token type expected is passed as argument to this method.
     *
     * FormalParameterList :
     *      Identifier
     *      FormalParameterList , Identifier
     *
     * See 13
     *
     * Parse function parameter list.
     * @return List of parameter nodes.
     */
    private List<IdentNode> formalParameterList(final TokenType endType, final Parameters out) {
        // Prepare to gather parameters. ES6 defaults, patterns and rest are only
        // accepted where the body that has to apply them is parsed as well.
        final List<IdentNode> parameters = out == null ? new ArrayList<IdentNode>() : out.list;
        final List<ParameterSetup> setups = out == null ? null : out.setups;
        // Track commas.
        boolean first = true;

        while (type != endType) {
            // Comma prior to every argument except the first.
            if (!first) {
                expect(COMMARIGHT);
            } else {
                first = false;
            }

            if (out != null && isES6() && type == ELLIPSIS) {
                // A rest binding ends the list, and is declared in the body rather than
                // taking a parameter slot.
                next();
                out.rest = getIdent();
                verifyStrictIdent(out.rest, "function parameter");

                break;
            }

            if (setups != null && isES6() && (type == LBRACKET || type == LBRACE)) {
                // ES6 pattern parameter. It becomes an ordinary parameter under a name of
                // its own, and the bindings it stands for are declared in the body.
                final long patternToken = token;
                final List<Binding> pattern = destructuringPattern();

                parameters.add(createIdentNode(Token.recast(patternToken, IDENT), finish, newPatternParameter()));
                setups.add(new ParameterSetup(defaultValue(), pattern));

                continue;
            }

            // Get and add parameter.
            final IdentNode ident = getIdent();

            // ECMA 13.1 strict mode restrictions
            verifyStrictIdent(ident, "function parameter");

            parameters.add(ident);

            if (setups != null) {
                final Expression defaultValue = isES6() ? defaultValue() : null;
                setups.add(defaultValue == null ? null : new ParameterSetup(defaultValue, null));
            }
        }

        return parameters;
    }

    /**
     * FunctionBody :
     *      SourceElements?
     *
     * See 13
     *
     * Parse function body.
     * @return function node (body.)
     */
    private FunctionNode functionBody(final long firstToken, final IdentNode ident, final Parameters parameters,
            final FunctionNode.Kind kind, final int functionLine) {
        return functionBody(firstToken, ident, parameters, kind, functionLine, false);
    }

    private FunctionNode functionBody(final long firstToken, final IdentNode ident, final Parameters parameters,
            final FunctionNode.Kind kind, final int functionLine, final boolean classConstructor) {
        FunctionNode functionNode = null;
        long lastToken = 0L;

        final boolean parseBody;
        Object endParserState = null;
        final List<String> prevTemporaries = temporaries;
        temporaries = new ArrayList<>();
        try {
            // Create a new function block.
            functionNode = newFunctionNode(firstToken, ident, parameters.list, kind, functionLine);
            assert functionNode != null;
            final int functionId = functionNode.getId();
            restoreArrowThis(functionNode);
            parseBody = reparsedFunction == null || functionId <= reparsedFunction.getFunctionNodeId();
            // Sai extension: expression closures. An arrow function's concise body has
            // the same shape but is not an extension, so it is always allowed.
            if ((kind == FunctionNode.Kind.ARROW || !env._no_syntax_extensions) && type != LBRACE) {
                /*
                 * Example:
                 *
                 * function square(x) x * x;
                 * print(square(3));
                 */

                // just expression as function body
                final Expression expr = assignmentExpression(true);
                lastToken = previousToken;
                assert lc.getCurrentBlock() == lc.getFunctionBody(functionNode);
                // EOL uses length field to store the line number
                final int lastFinish = Token.descPosition(lastToken) + (Token.descType(lastToken) == EOL ? 0 : Token.descLength(lastToken));
                // Only create the return node if we aren't skipping nested functions. Note that we aren't
                // skipping parsing of these extended functions; they're considered to be small anyway. Also,
                // they don't end with a single well known token, so it'd be very hard to get correctly (see
                // the note below for reasoning on skipping happening before instead of after RBRACE for
                // details).
                if (parseBody) {
                    final ReturnNode returnNode = new ReturnNode(functionNode.getLineNumber(), expr.getToken(), lastFinish, expr);
                    appendStatement(returnNode);
                }
                functionNode.setFinish(lastFinish);
            } else {
                expectDontAdvance(LBRACE);
                if (parseBody || !skipFunctionBody(functionNode)) {
                    next();
                    // Gather the function elements.
                    final List<Statement> prevFunctionDecls = functionDeclarations;
                    functionDeclarations = new ArrayList<>();
                    try {
                        sourceElements(ProgramKind.NORMAL);
                        addFunctionDeclarations(functionNode);
                    } finally {
                        functionDeclarations = prevFunctionDecls;
                    }

                    lastToken = token;
                    if (parseBody) {
                        // Since the lexer can read ahead and lexify some number of tokens in advance and have
                        // them buffered in the TokenStream, we need to produce a lexer state as it was just
                        // before it lexified RBRACE, and not whatever is its current (quite possibly well read
                        // ahead) state.
                        endParserState = new ParserState(Token.descPosition(token), line, linePosition);

                        // NOTE: you might wonder why do we capture/restore parser state before RBRACE instead of
                        // after RBRACE; after all, we could skip the below "expect(RBRACE);" if we captured the
                        // state after it. The reason is that RBRACE is a well-known token that we can expect and
                        // will never involve us getting into a weird lexer state, and as such is a great reparse
                        // point. Typical example of a weird lexer state after RBRACE would be:
                        //     function this_is_skipped() { ... } "use strict";
                        // because lexer is doing weird off-by-one maneuvers around string literal quotes. Instead
                        // of compensating for the possibility of a string literal (or similar) after RBRACE,
                        // we'll rather just restart parsing from this well-known, friendly token instead.
                    }
                }
                expect(RBRACE);
                functionNode.setFinish(finish);
            }

            if (classConstructor) {
                lc.setFlag(functionNode, FunctionNode.IS_CLASS_CONSTRUCTOR);
                requireNew(functionNode, firstToken);
            }

            declareRestParameter(functionNode, parameters);
            applyParameterSetups(functionNode, parameters.list, parameters.setups);
            declareArrowThis(functionNode);
            declareTemporaries(functionNode);
        } finally {
            temporaries = prevTemporaries;
            functionNode = restoreFunctionNode(functionNode, lastToken);
        }

        // NOTE: we can only do alterations to the function node after restoreFunctionNode.

        if (parseBody) {
            functionNode = functionNode.setEndParserState(lc, endParserState);
        } else if (functionNode.getBody().getStatementCount() > 0) {
            // This is to ensure the body is empty when !parseBody but we couldn't skip parsing it (see
            // skipFunctionBody() for possible reasons). While it is not strictly necessary for correctness to
            // enforce empty bodies in nested functions that were supposed to be skipped, we do assert it as
            // an invariant in few places in the compiler pipeline, so for consistency's sake we'll throw away
            // nested bodies early if we were supposed to skip 'em.
            functionNode = functionNode.setBody(null, functionNode.getBody().setStatements(null, Collections.<Statement> emptyList()));
        }

        if (reparsedFunction != null) {
            // We restore the flags stored in the function's ScriptFunctionData that we got when we first
            // eagerly parsed the code. We're doing it because some flags would be set based on the
            // content of the function, or even content of its nested functions, most of which are normally
            // skipped during an on-demand compilation.
            final RecompilableScriptFunctionData data = reparsedFunction.getScriptFunctionData(functionNode.getId());
            if (data != null) {
                // Data can be null if when we originally parsed the file, we removed the function declaration
                // as it was dead code.
                functionNode = functionNode.setFlags(lc, data.getFunctionFlags());
                // This compensates for missing markEval() in case the function contains an inner function
                // that contains eval(), that now we didn't discover since we skipped the inner function.
                if (functionNode.hasNestedEval()) {
                    assert functionNode.hasScopeBlock();
                    functionNode = functionNode.setBody(lc, functionNode.getBody().setNeedsScope(null));
                }
            }
        }
        printAST(functionNode);
        return functionNode;
    }

    private boolean skipFunctionBody(final FunctionNode functionNode) {
        if (reparsedFunction == null) {
            // Not reparsing, so don't skip any function body.
            return false;
        }
        if (templateSubstitutions > 0) {
            // Inside a template substitution. Skipping restarts the lexer at the closing
            // brace of this body, and a lexer started there knows nothing of the template
            // around it. Declining is a supported outcome - the caller drops the body.
            return false;
        }
        // Skip to the RBRACE of this function, and continue parsing from there.
        final RecompilableScriptFunctionData data = reparsedFunction.getScriptFunctionData(functionNode.getId());
        if (data == null) {
            // Nested function is not known to the reparsed function. This can happen if the FunctionNode was
            // in dead code that was removed. Both FoldConstants and Lower prune dead code. In that case, the
            // FunctionNode was dropped before a RecompilableScriptFunctionData could've been created for it.
            return false;
        }
        final ParserState parserState = (ParserState) data.getEndParserState();
        assert parserState != null;

        stream.reset();
        lexer = parserState.createLexer(source, lexer, stream, scripting && !env._no_syntax_extensions, env._es6);
        line = parserState.line;
        linePosition = parserState.linePosition;
        // Doesn't really matter, but it's safe to treat it as if there were a semicolon before
        // the RBRACE.
        type = SEMICOLON;
        k = -1;
        next();

        return true;
    }

    /**
     * Encapsulates part of the state of the parser, enough to reconstruct the state of both parser and lexer
     * for resuming parsing after skipping a function body.
     */
    private static class ParserState implements Serializable {
        private final int position;
        private final int line;
        private final int linePosition;

        private static final long serialVersionUID = -2382565130754093694L;

        ParserState(final int position, final int line, final int linePosition) {
            this.position = position;
            this.line = line;
            this.linePosition = linePosition;
        }

        Lexer createLexer(final Source source, final Lexer lexer, final TokenStream stream, final boolean scripting,
                final boolean es6) {
            final Lexer newLexer = new Lexer(source, position, lexer.limit - position, stream, scripting, true, es6);
            newLexer.restoreState(new Lexer.State(position, Integer.MAX_VALUE, line, -1, linePosition, SEMICOLON));
            return newLexer;
        }
    }

    private void printAST(final FunctionNode functionNode) {
        if (functionNode.getFlag(FunctionNode.IS_PRINT_AST)) {
            env.getErr().println(new ASTWriter(functionNode));
        }

        if (functionNode.getFlag(FunctionNode.IS_PRINT_PARSE)) {
            env.getErr().println(new PrintVisitor(functionNode, true, false));
        }
    }

    private void addFunctionDeclarations(final FunctionNode functionNode) {
        VarNode lastDecl = null;
        for (int i = functionDeclarations.size() - 1; i >= 0; i--) {
            Statement decl = functionDeclarations.get(i);
            if (lastDecl == null && decl instanceof VarNode) {
                decl = lastDecl = ((VarNode) decl).setFlag(VarNode.IS_LAST_FUNCTION_DECLARATION);
                lc.setFlag(functionNode, FunctionNode.HAS_FUNCTION_DECLARATIONS);
            }
            prependStatement(decl);
        }
    }

    private RuntimeNode referenceError(final Expression lhs, final Expression rhs, final boolean earlyError) {
        if (earlyError) {
            throw error(JSErrorType.REFERENCE_ERROR, AbstractParser.message("invalid.lvalue"), lhs.getToken());
        }
        final ArrayList<Expression> args = new ArrayList<>();
        args.add(lhs);
        if (rhs == null) {
            args.add(LiteralNode.newInstance(lhs.getToken(), lhs.getFinish()));
        } else {
            args.add(rhs);
        }
        args.add(LiteralNode.newInstance(lhs.getToken(), lhs.getFinish(), lhs.toString()));
        return new RuntimeNode(lhs.getToken(), lhs.getFinish(), RuntimeNode.Request.REFERENCE_ERROR, args);
    }

    /*
     * parse LHS [a, b, ..., c].
     *
     * JavaScript 1.8.
     */
    //private Node destructureExpression() {
    //    return null;
    //}

    /**
     * PostfixExpression :
     *      LeftHandSideExpression
     *      LeftHandSideExpression ++ // [no LineTerminator here]
     *      LeftHandSideExpression -- // [no LineTerminator here]
     *
     * See 11.3
     *
     * UnaryExpression :
     *      PostfixExpression
     *      delete UnaryExpression
     *      Node UnaryExpression
     *      typeof UnaryExpression
     *      ++ UnaryExpression
     *      -- UnaryExpression
     *      + UnaryExpression
     *      - UnaryExpression
     *      ~ UnaryExpression
     *      ! UnaryExpression
     *
     * See 11.4
     *
     * Parse unary expression.
     * @return Expression node.
     */
    private Expression unaryExpression() {
        if (isES6() && isArrowFunction()) {
            return arrowFunction();
        }

        if (isES6() && isDestructuringAssignment()) {
            return destructuringAssignment();
        }

        final int unaryLine = line;
        final long unaryToken = token;

        switch (type) {
        case DELETE: {
            next();
            final Expression expr = unaryExpression();
            if (expr instanceof BaseNode || expr instanceof IdentNode) {
                return new UnaryNode(unaryToken, expr);
            }
            appendStatement(new ExpressionStatement(unaryLine, unaryToken, finish, expr));
            return LiteralNode.newInstance(unaryToken, finish, true);
        }
        case VOID:
        case TYPEOF:
        case ADD:
        case SUB:
        case BIT_NOT:
        case NOT:
            next();
            final Expression expr = unaryExpression();
            return new UnaryNode(unaryToken, expr);

        case INCPREFIX:
        case DECPREFIX:
            final TokenType opType = type;
            next();

            final Expression lhs = leftHandSideExpression();
            // ++, -- without operand..
            if (lhs == null) {
                throw error(AbstractParser.message("expected.lvalue", type.getNameOrType()));
            }

            if (!(lhs instanceof AccessNode || lhs instanceof IndexNode || lhs instanceof IdentNode)) {
                return referenceError(lhs, null, env._early_lvalue_error);
            }

            if (lhs instanceof IdentNode) {
                if (!checkIdentLValue((IdentNode) lhs)) {
                    return referenceError(lhs, null, false);
                }
                verifyStrictIdent((IdentNode) lhs, "operand for " + opType.getName() + " operator");
            }

            return incDecExpression(unaryToken, opType, lhs, false);

        default:
            break;
        }

        Expression expression = leftHandSideExpression();

        if (last != EOL) {
            switch (type) {
            case INCPREFIX:
            case DECPREFIX:
                final TokenType opType = type;
                final Expression lhs = expression;
                // ++, -- without operand..
                if (lhs == null) {
                    throw error(AbstractParser.message("expected.lvalue", type.getNameOrType()));
                }

                if (!(lhs instanceof AccessNode || lhs instanceof IndexNode || lhs instanceof IdentNode)) {
                    next();
                    return referenceError(lhs, null, env._early_lvalue_error);
                }
                if (lhs instanceof IdentNode) {
                    if (!checkIdentLValue((IdentNode) lhs)) {
                        next();
                        return referenceError(lhs, null, false);
                    }
                    verifyStrictIdent((IdentNode) lhs, "operand for " + opType.getName() + " operator");
                }
                expression = incDecExpression(token, type, expression, true);
                next();
                break;
            default:
                break;
            }
        }

        if (expression == null) {
            throw error(AbstractParser.message("expected.operand", type.getNameOrType()));
        }

        return expression;
    }

    /**
     * MultiplicativeExpression :
     *      UnaryExpression
     *      MultiplicativeExpression * UnaryExpression
     *      MultiplicativeExpression / UnaryExpression
     *      MultiplicativeExpression % UnaryExpression
     *
     * See 11.5
     *
     * AdditiveExpression :
     *      MultiplicativeExpression
     *      AdditiveExpression + MultiplicativeExpression
     *      AdditiveExpression - MultiplicativeExpression
     *
     * See 11.6
     *
     * ShiftExpression :
     *      AdditiveExpression
     *      ShiftExpression << AdditiveExpression
     *      ShiftExpression >> AdditiveExpression
     *      ShiftExpression >>> AdditiveExpression
     *
     * See 11.7
     *
     * RelationalExpression :
     *      ShiftExpression
     *      RelationalExpression < ShiftExpression
     *      RelationalExpression > ShiftExpression
     *      RelationalExpression <= ShiftExpression
     *      RelationalExpression >= ShiftExpression
     *      RelationalExpression instanceof ShiftExpression
     *      RelationalExpression in ShiftExpression // if !noIf
     *
     * See 11.8
     *
     *      RelationalExpression
     *      EqualityExpression == RelationalExpression
     *      EqualityExpression != RelationalExpression
     *      EqualityExpression === RelationalExpression
     *      EqualityExpression !== RelationalExpression
     *
     * See 11.9
     *
     * BitwiseANDExpression :
     *      EqualityExpression
     *      BitwiseANDExpression & EqualityExpression
     *
     * BitwiseXORExpression :
     *      BitwiseANDExpression
     *      BitwiseXORExpression ^ BitwiseANDExpression
     *
     * BitwiseORExpression :
     *      BitwiseXORExpression
     *      BitwiseORExpression | BitwiseXORExpression
     *
     * See 11.10
     *
     * LogicalANDExpression :
     *      BitwiseORExpression
     *      LogicalANDExpression && BitwiseORExpression
     *
     * LogicalORExpression :
     *      LogicalANDExpression
     *      LogicalORExpression || LogicalANDExpression
     *
     * See 11.11
     *
     * ConditionalExpression :
     *      LogicalORExpression
     *      LogicalORExpression ? AssignmentExpression : AssignmentExpression
     *
     * See 11.12
     *
     * AssignmentExpression :
     *      ConditionalExpression
     *      LeftHandSideExpression AssignmentOperator AssignmentExpression
     *
     * AssignmentOperator :
     *      = *= /= %= += -= <<= >>= >>>= &= ^= |=
     *
     * See 11.13
     *
     * Expression :
     *      AssignmentExpression
     *      Expression , AssignmentExpression
     *
     * See 11.14
     *
     * Parse expression.
     * @return Expression node.
     */
    private Expression expression() {
        // TODO - Destructuring array.
        // Include commas in expression parsing.
        return expression(unaryExpression(), COMMARIGHT.getPrecedence(), false);
    }

    private JoinPredecessorExpression joinPredecessorExpression() {
        return new JoinPredecessorExpression(expression());
    }

    private Expression expression(final Expression exprLhs, final int minPrecedence, final boolean noIn) {
        // Get the precedence of the next operator.
        int precedence = type.getPrecedence();
        Expression lhs = exprLhs;

        // While greater precedence.
        while (type.isOperator(noIn) && precedence >= minPrecedence) {
            // Capture the operator token.
            final long op = token;

            if (type == TERNARY) {
                // Skip operator.
                next();

                // Pass expression. Middle expression of a conditional expression can be a "in"
                // expression - even in the contexts where "in" is not permitted.
                final Expression trueExpr = expression(unaryExpression(), ASSIGN.getPrecedence(), false);

                expect(COLON);

                // Fail expression.
                final Expression falseExpr = expression(unaryExpression(), ASSIGN.getPrecedence(), noIn);

                // Build up node.
                lhs = new TernaryNode(op, lhs, new JoinPredecessorExpression(trueExpr), new JoinPredecessorExpression(falseExpr));
            } else {
                // Skip operator.
                next();

                // Get the next primary expression.
                Expression rhs;
                final boolean isAssign = Token.descType(op) == ASSIGN;
                if (isAssign) {
                    defaultNames.push(lhs);
                }
                try {
                    rhs = unaryExpression();
                    // Get precedence of next operator.
                    int nextPrecedence = type.getPrecedence();

                    // Subtask greater precedence.
                    while (type.isOperator(noIn)
                            && (nextPrecedence > precedence || nextPrecedence == precedence && !type.isLeftAssociative())) {
                        rhs = expression(rhs, nextPrecedence, noIn);
                        nextPrecedence = type.getPrecedence();
                    }
                } finally {
                    if (isAssign) {
                        defaultNames.pop();
                    }
                }
                lhs = verifyAssignment(op, lhs, rhs);
            }

            precedence = type.getPrecedence();
        }

        return lhs;
    }

    private Expression assignmentExpression(final boolean noIn) {
        // TODO - Handle decompose.
        // Exclude commas in expression parsing.
        return expression(unaryExpression(), ASSIGN.getPrecedence(), noIn);
    }

    /**
     * Parse an end of line.
     */
    private void endOfLine() {
        switch (type) {
        case SEMICOLON:
        case EOL:
            next();
            break;
        case RPAREN:
        case RBRACKET:
        case RBRACE:
        case EOF:
            break;
        default:
            if (last != EOL) {
                expect(SEMICOLON);
            }
            break;
        }
    }

    @Override
    public String toString() {
        return "'JavaScript Parsing'";
    }

    /**
     * Record that the nearest enclosing function that is not an arrow has to declare
     * the binding arrow functions read {@code this} from. Arrows nested inside arrows
     * all reach the same function, so one binding serves all of them. The program is
     * of kind SCRIPT, so the walk always terminates.
     */
    private void markArrowThis() {
        final Iterator<FunctionNode> functions = lc.getFunctions();

        while (functions.hasNext()) {
            final FunctionNode function = functions.next();

            if (function.getKind() != FunctionNode.Kind.ARROW) {
                lc.setFlag(function, FunctionNode.USES_THIS | FunctionNode.USES_ARROW_THIS);
                return;
            }
        }
    }

    /**
     * Record that an expression at this point reads {@code this}, against whichever
     * function actually owns that binding. Inside an arrow that is not the arrow.
     */
    private void markThisUse() {
        if (lc.getCurrentFunction().getKind() == FunctionNode.Kind.ARROW) {
            markArrowThis();
        } else {
            lc.setFlag(lc.getCurrentFunction(), FunctionNode.USES_THIS);
        }
    }

    /**
     * The {@code this} an expression at this point reads. Every {@code this} the parser
     * emits has to come from here rather than be built by hand, because inside an arrow
     * the name is not "this" at all - it is the binding the nearest enclosing non-arrow
     * function was made to declare.
     *
     * @param thisToken token to give the identifier.
     * @return the identifier to read the receiver through.
     */
    private IdentNode thisFor(final long thisToken) {
        final boolean arrow = lc.getCurrentFunction().getKind() == FunctionNode.Kind.ARROW;

        markThisUse();

        return new IdentNode(thisToken, finish, arrow ? ARROW_THIS : TokenType.THIS.getName());
    }

    /**
     * Prepend {@code var :arrowthis = this;} to the body of the function being
     * parsed, if an arrow function inside it asked for the binding. From here on it
     * is an ordinary variable, so the closure machinery gives the arrows access to it.
     *
     * @param functionNode the function whose body is currently open
     */
    /**
     * Restore {@link FunctionNode#USES_ARROW_THIS} from the data recorded when the
     * function was first parsed eagerly.
     *
     * {@link #markArrowThis()} sets the flag on the enclosing function, and only runs
     * when an arrow's {@code this} is actually lexed. An on-demand re-parse skips the
     * bodies of nested functions, so an arrow nested in the function being re-parsed
     * never sets it. The general flag restore for that runs after
     * {@code restoreFunctionNode}, which is after {@link #declareArrowThis} has already
     * decided not to emit the binding - leaving the restored flags claiming a binding
     * that is not there. This is the same hazard the surrounding code compensates for
     * with markEval, so the one flag declareArrowThis reads is restored up front.
     *
     * @param functionNode the function being parsed.
     */
    private void restoreArrowThis(final FunctionNode functionNode) {
        if (reparsedFunction == null) {
            return;
        }

        final RecompilableScriptFunctionData data = reparsedFunction.getScriptFunctionData(functionNode.getId());

        if (data != null && (data.getFunctionFlags() & FunctionNode.USES_ARROW_THIS) != 0) {
            lc.setFlag(functionNode, FunctionNode.USES_ARROW_THIS);
        }
    }

    private void declareArrowThis(final FunctionNode functionNode) {
        if ((lc.getFlags(functionNode) & FunctionNode.USES_ARROW_THIS) == 0) {
            return;
        }

        final long firstToken = functionNode.getFirstToken();
        final int start = Token.descPosition(firstToken);
        final IdentNode binding = new IdentNode(firstToken, start, ARROW_THIS);
        final IdentNode thisNode = new IdentNode(firstToken, start, TokenType.THIS.getName());

        prependStatement(new VarNode(functionNode.getLineNumber(), Token.recast(firstToken, TokenType.VAR), start, binding,
                thisNode));
    }

    /**
     * Prepend the default value handling for the parameters of the function being
     * parsed. A default becomes {@code if (p === undefined) { p = <default>; }} at the
     * top of the body, so a default may read a parameter declared before it and is
     * re-evaluated on every call that needs it.
     *
     * The statements are prepended back to front so that they end up in declaration
     * order.
     *
     * @param functionNode the function whose body is currently open
     * @param parameters the parameters
     * @param parameterDefaults one entry per parameter, null where there is no
     *                          default; null altogether where defaults are not
     *                          accepted
     */
    /**
     * Declare the rest binding of the function being parsed, as an array of the
     * arguments past the last formal parameter:
     *
     * <pre>
     * function f(a, ...r)   gets   var r = TO_ARRAY(arguments, 1);
     * </pre>
     *
     * @param functionNode the function whose body is currently open
     * @param parameters the parameters, carrying the rest binding if there is one
     */
    /**
     * Prepend the check that rejects a call of a class constructor that did not come
     * from {@code new}, to the body of the constructor being parsed.
     *
     * A class body is strict code, so a call with no receiver leaves {@code this}
     * undefined, and that is what the check looks at. A super call is
     * {@code :superclass.call(this, ..)} and passes the instance, so it goes through.
     *
     * The check is a runtime request rather than a throw of a TypeError read out of
     * the scope, for the same reason the members of a class body are defined by one: a
     * local named TypeError would otherwise break every class below it.
     *
     * @param functionNode the constructor whose body is currently open
     * @param token token the synthetic nodes are attributed to
     */
    private void requireNew(final FunctionNode functionNode, final long token) {
        prependStatement(new ExpressionStatement(functionNode.getLineNumber(), token, finish,
                new RuntimeNode(token, finish, RuntimeNode.Request.REQUIRE_NEW,
                        thisFor(Token.recast(token, TokenType.THIS)))));
    }

    private void declareRestParameter(final FunctionNode functionNode, final Parameters parameters) {
        if (parameters.rest == null) {
            return;
        }

        lc.setFlag(functionNode, FunctionNode.USES_ARGUMENTS);

        final long restToken = parameters.rest.getToken();
        final Expression value = new RuntimeNode(restToken, finish, RuntimeNode.Request.TO_ARRAY,
                identifierFor(restToken, ARGUMENTS_NAME),
                LiteralNode.newInstance(restToken, finish, Integer.valueOf(parameters.list.size())));

        prependStatement(new VarNode(functionNode.getLineNumber(), restToken, finish,
                parameters.rest.setIsDeclaredHere(), value));
    }

    private void applyParameterSetups(final FunctionNode functionNode, final List<IdentNode> parameters,
            final List<ParameterSetup> parameterSetups) {
        if (parameterSetups == null) {
            return;
        }

        assert parameterSetups.size() == parameters.size();

        final int lineNumber = functionNode.getLineNumber();

        for (int i = parameterSetups.size() - 1; i >= 0; i--) {
            final ParameterSetup setup = parameterSetups.get(i);

            if (setup == null) {
                continue;
            }

            final IdentNode parameter = parameters.get(i);

            // Prepended back to front, so the pattern goes in before the default that
            // has to run ahead of it.
            if (setup.pattern != null) {
                final List<Statement> statements = new ArrayList<>();
                declareBindings(parameter.getName(), setup.pattern, lineNumber, 0, new ArrayList<VarNode>(), statements);

                for (int j = statements.size() - 1; j >= 0; j--) {
                    prependStatement(statements.get(j));
                }
            }

            if (setup.defaultValue != null) {
                prependStatement(newUndefinedGuard(parameter, setup.defaultValue, lineNumber));
            }
        }
    }

    /**
     * Build {@code if (target === undefined) { target = defaultValue; }}, the shape
     * both a default parameter and a default in a destructuring pattern lower to.
     *
     * @param target the binding to fill in
     * @param defaultValue the value to fill it with
     * @param lineNumber line to attribute the synthetic statement to
     * @return the statement
     */
    private static Statement newUndefinedGuard(final IdentNode target, final Expression defaultValue, final int lineNumber) {
        final long token = defaultValue.getToken();
        final int finish = defaultValue.getFinish();

        final Expression test = new BinaryNode(Token.recast(token, TokenType.EQ_STRICT), referenceTo(target),
                LiteralNode.newInstance(token, finish, ScriptRuntime.UNDEFINED));
        final Expression assignment = new BinaryNode(Token.recast(token, TokenType.ASSIGN), referenceTo(target), defaultValue);
        final Block pass = new Block(token, finish, new ExpressionStatement(lineNumber, token, finish, assignment));

        return new IfNode(lineNumber, token, finish, test, pass, null);
    }

    /**
     * A fresh IdentNode naming the same thing. Nodes are not shared between places in
     * the tree, since each one is given its own symbol.
     */
    private static IdentNode referenceTo(final IdentNode ident) {
        return new IdentNode(ident.getToken(), ident.getFinish(), ident.getName());
    }

    /**
     * Look ahead for the {@code =} that turns a bracketed or braced list into a
     * destructuring assignment. Without this the list would parse as an array or
     * object literal, which is a different shape entirely.
     *
     * @return true if a destructuring assignment starts at the current token
     */
    private boolean isDestructuringAssignment() {
        return lookahead(this::isDestructuringAssignmentAhead);
    }

    private boolean isDestructuringAssignmentAhead() {
        if (type != LBRACKET && type != LBRACE) {
            return false;
        }

        final TokenType open = type;
        final TokenType close = open == LBRACKET ? RBRACKET : RBRACE;
        int depth = 0;

        for (int i = k;; i++) {
            final TokenType tokenType = T(i);

            if (tokenType == open) {
                depth++;
            } else if (tokenType == close) {
                if (--depth == 0) {
                    return T(i + 1) == ASSIGN;
                }
            } else if (tokenType == EOF) {
                return false;
            }
        }
    }

    /**
     * Parse a destructuring assignment, which unlike a destructuring declaration is an
     * expression and has to produce a value. The right hand side goes into a temporary,
     * every target is assigned from it, and the temporary is the result:
     *
     * <pre>
     * [a, b] = rhs   becomes   (:pt0 = rhs, a = :pt0[0], b = :pt0[1], :pt0)
     * </pre>
     *
     * Reading the whole right hand side before assigning anything is what makes
     * {@code [a, b] = [b, a]} a swap.
     *
     * @return the lowered expression
     */
    private Expression destructuringAssignment() {
        final long patternToken = token;
        final List<Binding> pattern = destructuringPattern(true);

        expect(ASSIGN);
        final Expression rhs = assignmentExpression(false);

        final String source = newTemporary();
        final List<Expression> steps = new ArrayList<>();
        steps.add(new BinaryNode(Token.recast(patternToken, TokenType.ASSIGN), identifierFor(patternToken, source), rhs));
        assignBindings(source, pattern, steps);
        steps.add(identifierFor(patternToken, source));

        Expression result = steps.get(0);

        for (int i = 1; i < steps.size(); i++) {
            result = new BinaryNode(Token.recast(patternToken, TokenType.COMMARIGHT), result, steps.get(i));
        }

        return result;
    }

    /**
     * Collect the assignments a parsed pattern stands for, reading each target's value
     * out of the given source. A nested pattern gets a temporary of its own.
     *
     * A default becomes a conditional rather than the statement a declaration uses,
     * since this all has to stay one expression. The element is read twice, which is
     * free: the source is a temporary and the key a literal.
     */
    private void assignBindings(final String source, final List<Binding> bindings, final List<Expression> steps) {
        for (final Binding binding : bindings) {
            final Expression key = bindingKey(binding, steps);
            Expression value = valueOfBinding(source, binding, key);

            if (binding.defaultValue != null) {
                final Expression test = new BinaryNode(Token.recast(binding.token, TokenType.EQ_STRICT), value,
                        LiteralNode.newInstance(binding.token, finish, ScriptRuntime.UNDEFINED));
                value = new TernaryNode(Token.recast(binding.token, TokenType.TERNARY), test,
                        new JoinPredecessorExpression(binding.defaultValue),
                        new JoinPredecessorExpression(readFrom(binding.token, source, key)));
            }

            if (binding.target != null) {
                steps.add(new BinaryNode(Token.recast(binding.token, TokenType.ASSIGN), binding.target, value));

                continue;
            }

            final String nested = newTemporary();
            steps.add(new BinaryNode(Token.recast(binding.token, TokenType.ASSIGN), identifierFor(binding.token, nested),
                    value));
            assignBindings(nested, binding.nested, steps);
        }
    }

    /**
     * Parse one destructuring declarator and emit the declarations it stands for.
     *
     * The pattern is read into a small tree first, because the right hand side has to
     * be evaluated before any binding is read out of it, and it comes second in the
     * source. It is evaluated once, into a temporary:
     *
     * <pre>
     * var { t: [u, v] } = o;
     * </pre>
     * <pre>
     * :pt0 = o; :pt1 = :pt0["t"]; var u = :pt1[0]; var v = :pt1[1];
     * </pre>
     *
     * Array patterns read by index rather than through an iterator, which is what
     * makes a string work as a source and an iterable object not.
     *
     * @param varLine line of the declaration
     * @param varToken token the pattern starts at
     * @param varFlags VarNode flags, carrying let or const
     * @param isStatement false when this is the init of a for loop
     * @param vars collects the declarations, as the plain path does
     */
    private void destructuringDeclaration(final int varLine, final long varToken, final int varFlags, final boolean isStatement,
            final List<VarNode> vars) {
        final List<Binding> pattern = destructuringPattern();

        expect(ASSIGN);
        final Expression init = assignmentExpression(!isStatement);

        final String source = newTemporary();
        appendStatement(assignTemporary(varLine, varToken, source, init));

        final List<Statement> statements = new ArrayList<>();
        declareBindings(source, pattern, varLine, varFlags, vars, statements);

        for (final Statement statement : statements) {
            appendStatement(statement);
        }
    }

    private List<Binding> destructuringPattern() {
        return destructuringPattern(false);
    }

    /**
     * @param assignment true when the pattern is the left hand side of an assignment,
     *                   where each leaf is an assignment target rather than a name to
     *                   declare
     */
    private List<Binding> destructuringPattern(final boolean assignment) {
        return type == LBRACKET ? arrayPattern(assignment) : objectPattern(assignment);
    }

    private List<Binding> arrayPattern(final boolean assignment) {
        expect(LBRACKET);

        final List<Binding> bindings = new ArrayList<>();
        int index = 0;

        while (type != RBRACKET) {
            if (type == COMMARIGHT) {
                // An elision leaves the element at this position unbound.
                next();
                index++;
                continue;
            }

            if (isES6() && type == ELLIPSIS) {
                // A rest element takes everything from here on, so it ends the pattern.
                final long restToken = token;
                next();
                final Expression restKey = LiteralNode.newInstance(restToken, finish, Integer.valueOf(index));

                // What the tail lands in is a target like any other, so it can be a
                // pattern too. Without this the target is parsed as an expression, and an
                // array literal in an assignment ends up standing where a store has to
                // go, which is not something bytecode generation can express.
                if (type == LBRACKET || type == LBRACE) {
                    bindings.add(new Binding(restToken, restKey, null, destructuringPattern(assignment), null, true));
                } else {
                    bindings.add(new Binding(restToken, restKey, patternTarget(assignment), null, null, true));
                }

                if (type == COMMARIGHT) {
                    throw error(AbstractParser.message("rest.not.last.in.pattern"), token);
                }

                break;
            }

            bindings.add(patternElement(LiteralNode.newInstance(token, finish, Integer.valueOf(index)), assignment));
            index++;

            if (type != COMMARIGHT) {
                break;
            }
            next();
        }

        expect(RBRACKET);

        return bindings;
    }

    private List<Binding> objectPattern(final boolean assignment) {
        expect(LBRACE);

        final List<Binding> bindings = new ArrayList<>();

        while (type != RBRACE) {
            final long keyToken = token;

            if (type == LBRACKET) {
                // A computed key, the pattern counterpart of { [k]: v } in a literal.
                next();
                final Expression computed = assignmentExpression(false);
                expect(RBRACKET);
                expect(COLON);
                bindings.add(patternElement(computed, assignment));

                if (type != COMMARIGHT) {
                    break;
                }
                next();

                continue;
            }

            final String keyName;
            IdentNode shorthand = null;

            if (type == IDENT || isNonStrictModeIdent()) {
                final IdentNode ident = getIdent();
                keyName = ident.getName();

                if (type != COLON) {
                    shorthand = ident;
                }
            } else {
                keyName = propertyName().getPropertyName();
            }

            final Expression key = LiteralNode.newInstance(keyToken, finish, keyName);

            if (shorthand != null) {
                verifyStrictIdent(shorthand, "variable name");
                bindings.add(new Binding(keyToken, key, shorthand, null, defaultValue()));
            } else {
                expect(COLON);
                bindings.add(patternElement(key, assignment));
            }

            if (type != COMMARIGHT) {
                break;
            }
            next();
        }

        expect(RBRACE);

        return bindings;
    }

    /** One element of a pattern: a nested pattern, or somewhere to put the value. */
    private Binding patternElement(final Expression key, final boolean assignment) {
        final long elementToken = token;

        if (type == LBRACKET || type == LBRACE) {
            return new Binding(elementToken, key, null, destructuringPattern(assignment), defaultValue());
        }

        return new Binding(elementToken, key, patternTarget(assignment), null, defaultValue());
    }

    /** Where one value of a pattern ends up: a name to declare, or any assignment target. */
    private Expression patternTarget(final boolean assignment) {
        if (assignment) {
            return leftHandSideExpression();
        }

        final IdentNode name = getIdent();
        verifyStrictIdent(name, "variable name");

        return name;
    }

    private Expression defaultValue() {
        if (type != ASSIGN) {
            return null;
        }
        next();

        return assignmentExpression(false);
    }

    /**
     * Emit the declarations a parsed pattern stands for, reading each binding out of
     * the given source. A nested pattern gets a temporary of its own.
     */
    private void declareBindings(final String source, final List<Binding> bindings, final int varLine, final int varFlags,
            final List<VarNode> vars, final List<Statement> out) {
        for (final Binding binding : bindings) {
            final Expression key = bindingKey(binding, out, varLine);
            final Expression value = valueOfBinding(source, binding, key);

            if (binding.target != null) {
                final IdentNode name = (IdentNode) binding.target;
                Expression initializer = value;

                if (binding.defaultValue != null) {
                    // The default has to go into the initializer: a const binding cannot
                    // be assigned to a second time, so filling it in afterwards failed
                    // with "Assignment to constant" whenever the default was taken.
                    // The value is read once into a temporary first, since testing the
                    // read expression and then using it would read it twice.
                    final String read = newTemporary();
                    out.add(assignTemporary(varLine, binding.token, read, value));

                    final Expression test = new BinaryNode(Token.recast(binding.token, TokenType.EQ_STRICT),
                            identifierFor(binding.token, read),
                            LiteralNode.newInstance(binding.token, finish, ScriptRuntime.UNDEFINED));
                    initializer = new TernaryNode(Token.recast(binding.token, TokenType.TERNARY), test,
                            new JoinPredecessorExpression(binding.defaultValue),
                            new JoinPredecessorExpression(identifierFor(binding.token, read)));
                }

                final VarNode var = new VarNode(varLine, binding.token, finish, name.setIsDeclaredHere(), initializer,
                        varFlags);
                vars.add(var);
                out.add(var);

                continue;
            }

            final String nested = newTemporary();
            out.add(assignTemporary(varLine, binding.token, nested, value));

            if (binding.defaultValue != null) {
                out.add(newUndefinedGuard(identifierFor(binding.token, nested), binding.defaultValue, varLine));
            }

            declareBindings(nested, binding.nested, varLine, varFlags, vars, out);
        }
    }

    private ExpressionStatement assignTemporary(final int varLine, final long token, final String temporary,
            final Expression value) {
        return new ExpressionStatement(varLine, token, finish,
                new BinaryNode(Token.recast(token, TokenType.ASSIGN), identifierFor(token, temporary), value));
    }

    /**
     * Parse {@code ... AssignmentExpression}, wrapping it so that the list it belongs
     * to can tell a spread element from an ordinary one. The marker never reaches code
     * generation: whoever built the list replaces it.
     *
     * @return the marked element
     */
    private Expression spreadElement() {
        final long spreadToken = token;
        next();

        return new UnaryNode(spreadToken, assignmentExpression(false));
    }

    private static boolean isSpread(final Expression element) {
        return element instanceof UnaryNode && element.isTokenType(ELLIPSIS);
    }

    private static boolean hasSpread(final List<Expression> elements) {
        for (final Expression element : elements) {
            if (isSpread(element)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Build the array a list containing a spread stands for. Runs of ordinary elements
     * become array literals, a spread becomes TO_ARRAY, and the pieces are joined with
     * concat, which flattens the arrays it is handed:
     *
     * <pre>
     * [a, ...b, c]   becomes   [a].concat(TO_ARRAY(b, 0), [c])
     * </pre>
     *
     * @param line line to attribute the synthetic nodes to
     * @param token token of the list
     * @param elements the elements, some of them marked by spreadElement()
     * @return an expression evaluating to the array
     */
    private Expression spreadToArray(final int line, final long token, final List<Expression> elements) {
        final List<Expression> segments = new ArrayList<>();
        List<Expression> plain = new ArrayList<>();

        for (final Expression element : elements) {
            if (!isSpread(element)) {
                plain.add(element);
                continue;
            }

            if (!plain.isEmpty()) {
                segments.add(arrayOf(token, plain));
                plain = new ArrayList<>();
            }

            segments.add(new RuntimeNode(token, finish, RuntimeNode.Request.TO_ARRAY,
                    ((UnaryNode) element).getExpression(), LiteralNode.newInstance(token, finish, Integer.valueOf(0))));
        }

        if (!plain.isEmpty()) {
            segments.add(arrayOf(token, plain));
        }

        final Expression first = segments.get(0);

        if (segments.size() == 1) {
            // A lone spread already produced a fresh array, and a lone run of ordinary
            // elements is just the literal.
            return first;
        }

        return new CallNode(line, token, finish, new AccessNode(Token.recast(token, TokenType.PERIOD), finish, first, "concat"),
                segments.subList(1, segments.size()), false);
    }

    private Expression arrayOf(final long token, final List<Expression> elements) {
        return LiteralNode.newInstance(Token.recast(token, LBRACKET), finish,
                elements.toArray(new Expression[elements.size()]));
    }

    /**
     * Build the call an argument list containing a spread stands for. The arguments
     * become one array and the call goes through apply, which needs the receiver
     * passing explicitly - and evaluating once, hence the temporary:
     *
     * <pre>
     * o.m(...a)   becomes   (:pt0 = o, :pt0.m.apply(:pt0, TO_ARRAY(a, 0)))
     * </pre>
     *
     * @param line line of the call
     * @param callToken token of the call
     * @param function the function being called
     * @param arguments the arguments, some of them marked by spreadElement()
     * @return the lowered call
     */
    private Expression spreadCall(final int line, final long callToken, final Expression function,
            final List<Expression> arguments) {
        final Expression argumentArray = spreadToArray(line, callToken, arguments);

        Expression receiver = LiteralNode.newInstance(callToken, finish);
        Expression callee = function;
        Expression prologue = null;

        if (function instanceof BaseNode) {
            // A method call has to keep its receiver, and the base is an expression that
            // may only be evaluated once.
            final BaseNode access = (BaseNode) function;
            final String temporary = newTemporary();

            prologue = new BinaryNode(Token.recast(callToken, TokenType.ASSIGN), identifierFor(callToken, temporary),
                    access.getBase());
            receiver = identifierFor(callToken, temporary);
            callee = access instanceof AccessNode
                    ? new AccessNode(access.getToken(), access.getFinish(), identifierFor(callToken, temporary),
                            ((AccessNode) access).getProperty())
                    : new IndexNode(access.getToken(), access.getFinish(), identifierFor(callToken, temporary),
                            ((IndexNode) access).getIndex());
        }

        final List<Expression> applyArguments = new ArrayList<>();
        applyArguments.add(receiver);
        applyArguments.add(argumentArray);

        final Expression call = new CallNode(line, callToken, finish,
                new AccessNode(Token.recast(callToken, TokenType.PERIOD), finish, callee, "apply"), applyArguments, false);

        return prologue == null ? call
                : new BinaryNode(Token.recast(callToken, TokenType.COMMARIGHT), prologue, call);
    }

    /**
     * What one binding reads out of the source: an element by key, or, for a rest
     * element, everything from its index onwards as an array.
     */
    private Expression valueOfBinding(final String source, final Binding binding, final Expression key) {
        if (binding.rest) {
            return new RuntimeNode(binding.token, finish, RuntimeNode.Request.TO_ARRAY,
                    identifierFor(binding.token, source), key);
        }

        return readFrom(binding.token, source, key);
    }

    /**
     * The key to read a binding by, evaluated at most once.
     *
     * An array index and a plain property name are literals and can be used as they
     * are. A computed key is an arbitrary expression that has to run exactly once and
     * in source order, so it goes into a temporary first.
     *
     * @param binding the binding
     * @param out receives the assignment, when one is needed
     * @param line line to attribute a synthetic assignment to
     * @return the key expression to use
     */
    private Expression bindingKey(final Binding binding, final List<Statement> out, final int line) {
        if (binding.key instanceof LiteralNode) {
            return binding.key;
        }

        final String key = newTemporary();
        out.add(assignTemporary(line, binding.token, key, binding.key));

        return identifierFor(binding.token, key);
    }

    /** {@link #bindingKey(Binding, List, int)} for the comma-expression form. */
    private Expression bindingKey(final Binding binding, final List<Expression> steps) {
        if (binding.key instanceof LiteralNode) {
            return binding.key;
        }

        final String key = newTemporary();
        steps.add(new BinaryNode(Token.recast(binding.token, TokenType.ASSIGN), identifierFor(binding.token, key),
                binding.key));

        return identifierFor(binding.token, key);
    }

    /** {@code source[key]}, with the token types the IR checks for. */
    private Expression readFrom(final long token, final String source, final Expression key) {
        return new IndexNode(Token.recast(token, LBRACKET), finish, identifierFor(token, source), key);
    }

    private IdentNode identifierFor(final long token, final String name) {
        return createIdentNode(Token.recast(token, IDENT), finish, name);
    }

    /**
     * The parameters of a function: the formal parameter list, what each of them needs
     * doing to it at the top of the body, and the rest binding if there is one.
     *
     * A rest binding is deliberately not a formal parameter. It is declared in the
     * body, so that it does not count towards the function's length and so that
     * assigning to it cannot write through the mapped arguments object.
     */
    private static final class Parameters {
        private final List<IdentNode> list = new ArrayList<>();
        private final List<ParameterSetup> setups = new ArrayList<>();
        private IdentNode rest;
    }

    /**
     * What a parameter needs doing to it at the top of the body: a default value to
     * fill in when the argument is missing, a pattern to take apart, or both.
     */
    private static final class ParameterSetup {
        private final Expression defaultValue;
        private final List<Binding> pattern;

        ParameterSetup(final Expression defaultValue, final List<Binding> pattern) {
            this.defaultValue = defaultValue;
            this.pattern = pattern;
        }
    }

    /**
     * One binding of a destructuring pattern, parsed before the source is known. The
     * target is an IdentNode to declare in a declaration, and any assignment target in
     * a destructuring assignment.
     */
    private static final class Binding {
        private final long token;
        private final Expression key;
        private final Expression target;
        private final List<Binding> nested;
        private final Expression defaultValue;
        /** True for a rest element, where the key is the index to start collecting at. */
        private final boolean rest;

        Binding(final long token, final Expression key, final Expression target, final List<Binding> nested,
                final Expression defaultValue) {
            this(token, key, target, nested, defaultValue, false);
        }

        Binding(final long token, final Expression key, final Expression target, final List<Binding> nested,
                final Expression defaultValue, final boolean rest) {
            this.token = token;
            this.key = key;
            this.target = target;
            this.nested = nested;
            this.defaultValue = defaultValue;
            this.rest = rest;
        }
    }

    /**
     * Reserve a temporary for the function currently being parsed. The name is unique
     * across the parse, so a temporary can never be confused with one belonging to
     * another function even though they are all declared with var.
     *
     * @return the name of the temporary
     */
    private String newTemporary() {
        final String name = TEMPORARY_PREFIX + temporaryCount++;
        temporaries.add(name);

        return name;
    }

    /**
     * Name a parameter that was written as a pattern. It shares the temporary counter
     * so that the names cannot collide, but it is a parameter rather than a local and
     * so is not declared in the body.
     *
     * @return the name of the parameter
     */
    private String newPatternParameter() {
        return PATTERN_PARAMETER_PREFIX + temporaryCount++;
    }

    /**
     * Declare the temporaries reserved while parsing the body of the function being
     * parsed. They carry no initializer, so where the declaration lands among the
     * other statements does not matter; the desugaring that reserved one always
     * assigns it before reading it.
     *
     * @param functionNode the function whose body is currently open
     */
    private void declareTemporaries(final FunctionNode functionNode) {
        final long firstToken = functionNode.getFirstToken();
        final int start = Token.descPosition(firstToken);

        for (int i = temporaries.size() - 1; i >= 0; i--) {
            final IdentNode name = new IdentNode(firstToken, start, temporaries.get(i));
            prependStatement(new VarNode(functionNode.getLineNumber(), Token.recast(firstToken, TokenType.VAR), start, name,
                    null));
        }
    }

    private static void markEval(final LexicalContext lc) {
        final Iterator<FunctionNode> iter = lc.getFunctions();
        boolean flaggedCurrentFn = false;
        while (iter.hasNext()) {
            final FunctionNode fn = iter.next();
            if (!flaggedCurrentFn) {
                lc.setFlag(fn, FunctionNode.HAS_EVAL);
                flaggedCurrentFn = true;
            } else {
                lc.setFlag(fn, FunctionNode.HAS_NESTED_EVAL);
            }
            // NOTE: it is crucial to mark the body of the outer function as needing scope even when we skip
            // parsing a nested function. functionBody() contains code to compensate for the lack of invoking
            // this method when the parser skips a nested function.
            lc.setBlockNeedsScope(lc.getFunctionBody(fn));
        }
    }

    private void prependStatement(final Statement statement) {
        lc.prependStatement(statement);
    }

    private void appendStatement(final Statement statement) {
        lc.appendStatement(statement);
    }
}
