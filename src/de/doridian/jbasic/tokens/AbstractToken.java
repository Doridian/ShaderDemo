package de.doridian.jbasic.tokens;

import de.doridian.jbasic.BasicProgram;
import de.doridian.jbasic.tokens.flow.*;
import de.doridian.jbasic.tokens.flow.do_loop_loop.DOToken;
import de.doridian.jbasic.tokens.flow.do_loop_loop.LOOPToken;
import de.doridian.jbasic.tokens.flow.for_next_loop.FORToken;
import de.doridian.jbasic.tokens.flow.for_next_loop.NEXTToken;
import de.doridian.jbasic.tokens.flow.gosub_return.GOSUBToken;
import de.doridian.jbasic.tokens.flow.gosub_return.RETURNToken;
import de.doridian.jbasic.tokens.flow.repeat_until_loop.REPEATToken;
import de.doridian.jbasic.tokens.flow.repeat_until_loop.UNTILToken;
import de.doridian.jbasic.tokens.flow.while_wend_loop.WENDToken;
import de.doridian.jbasic.tokens.flow.while_wend_loop.WHILEToken;
import de.doridian.jbasic.tokens.fs.FCLOSEToken;
import de.doridian.jbasic.tokens.fs.FOPENToken;
import de.doridian.jbasic.tokens.fs.FREADLNToken;
import de.doridian.jbasic.tokens.fs.FWRITELNToken;
import de.doridian.jbasic.tokens.misc.ENDToken;
import de.doridian.jbasic.tokens.misc.LETToken;
import de.doridian.jbasic.tokens.misc.NOOPToken;
import de.doridian.jbasic.tokens.misc.SLEEPToken;
import de.doridian.jbasic.parameters.*;
import de.doridian.jbasic.tokens.io.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractToken {
    public int line;
    protected GroupedParameter[] parametersSplitDetailed;
    protected String parametersRaw;
    protected BasicProgram program;
    private String name;

    private static final Pattern linePatternWithNumber = Pattern.compile("^([0-9]+) +([A-Z]+)(.*)$");
    private static final Pattern linePatternWithoutNumber = Pattern.compile("^([A-Z]+)(.*)$");

    private static HashMap<String, Constructor<? extends AbstractToken>> tokens;

    static {
        tokens = new HashMap<>();

        // FS
        addToken(FCLOSEToken.class);
        addToken(FOPENToken.class);
        addToken(FREADLNToken.class);
        addToken(FWRITELNToken.class);

        // I/O
        addToken(INPUTToken.class);
        addToken(PRINTToken.class);
        addToken(IPRINTToken.class);
        addToken(GETCHARToken.class);
        addToken(ATToken.class);
        addToken(CLLToken.class);
        addToken(CLSToken.class);

        // Flow control
        addToken(GOSUBToken.class);
        addToken(RETURNToken.class);

        addToken(BREAKToken.class);
        addToken(CONTINUEToken.class);

        addToken(GOTOToken.class);

        addToken(IFToken.class);

        addToken(FORToken.class);
        addToken(NEXTToken.class);

        addToken(WHILEToken.class);
        addToken(WENDToken.class);

        addToken(REPEATToken.class);
        addToken(UNTILToken.class);

        addToken(DOToken.class);
        addToken(LOOPToken.class);

        // Misc
        addToken(NOOPToken.class);
        addToken(ENDToken.class);
        addToken(LETToken.class);
        addToken(SLEEPToken.class);
        addToken(EXECToken.class);
    }

    private static void addToken(Class<? extends AbstractToken> tokenClass) {
        try {
            Constructor<? extends AbstractToken> tokenCtor = tokenClass.getConstructor();
            for(String name : tokenClass.getAnnotation(TokenName.class).value())
                tokens.put(name.toUpperCase(), tokenCtor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) public @interface TokenName {
        String[] value();
        boolean ignoreParameters() default false;
        boolean noGroups() default false;
    }

    public static class CompilerException extends RuntimeException {
        public CompilerException(String message) {
            super(message);
        }
    }

    public static class SyntaxException extends CompilerException {
        public SyntaxException(String message) {
            super("SYNTAX ERROR: " + message);
        }

        public SyntaxException() {
            super("SYNTAX ERROR");
        }
    }

    public static AbstractToken parseLine(BasicProgram program, String lineStr) {
        AbstractToken token = _parseLine(program, lineStr, -1);
        if(token == null)
            return null;
        return token;
    }

    public Class<? extends AbstractToken> getEndingToken() {
        return null;
    }

    protected AbstractToken endingToken = null;

    public void setEndingToken(AbstractToken token) {
        endingToken = token;
    }

    public String getName() {
        return name;
    }

    protected AbstractToken parseLine(String lineStr) {
        return _parseLine(program, lineStr, -2);
    }

    private static AbstractToken _parseLine(BasicProgram program, String lineStr, int lineNumber) {
        if(lineStr.isEmpty())
            return null;
        Matcher matcher = ((lineNumber == -1) ? linePatternWithNumber : linePatternWithoutNumber).matcher(lineStr);
        if(!matcher.find())
            throw new SyntaxException("INVALID LINE " + lineStr);

        int tokenNameOffset = (lineNumber == -1) ? 2 : 1;

        int line;
        if(lineNumber == -1)
            line = Integer.parseInt(matcher.group(1));
        else
            line = lineNumber;

        String tokenName = matcher.group(tokenNameOffset);

        AbstractToken token;
        Constructor<? extends AbstractToken> tokenCtor = tokens.get(tokenName.toUpperCase());
        if(tokenCtor == null)
            throw new SyntaxException("INVALID INSTRUCTION " + tokenName.toUpperCase());

        try {
            token = tokenCtor.newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        token.setData(program, line, matcher.group(tokenNameOffset + 1).trim());
        token.name = tokenName;
        return token;
    }

    public static GroupedParameter[] parseParameters(String parameters, boolean noGroups) {
        ArrayList<AbstractParameter> parsedParametersCurrentGroup = new ArrayList<>();
        ArrayList<GroupedParameter> parsedParameters = new ArrayList<>();

        boolean inQuotes = false, inBackslash = false; int openBrackets = 0;
        StringBuilder preBracket = new StringBuilder();
        StringBuilder tmpStr = new StringBuilder();
        for (int pos = 0; pos < parameters.length(); pos++) {
            char c = parameters.charAt(pos);

            if (inQuotes) {
                if(inBackslash) {
                    inBackslash = false;
                    tmpStr.append('\\');
                    tmpStr.append(c);
                } else if(c == '\\') {
                    inBackslash = true;
                } else if (c == '"') {
                    inQuotes = false;
                    parsedParametersCurrentGroup.add(new StringLiteralParameter(tmpStr.toString()));
                    tmpStr.setLength(0);
                } else {
                    tmpStr.append(c);
                }
            } else {
                if(c == '(') {
                    if(openBrackets++ == 0) {
                        preBracket = tmpStr;
                        tmpStr = new StringBuilder();
                    } else
                        tmpStr.append('(');
                } else if(c == ')') {
                    if(openBrackets <= 0)
                        throw new SyntaxException("CANNOT CLOSE NON-OPEN BRACKET");
                    if(--openBrackets == 0) {
                        parsedParametersCurrentGroup.add(new BracketedParameter(preBracket.toString(), tmpStr.toString()));
                        tmpStr.setLength(0);
                        preBracket.setLength(0);
                    } else
                        tmpStr.append(')');
                } else if (c == '"' && openBrackets == 0) {
                    if (tmpStr.length() > 0)
                        throw new SyntaxException("MISSING COMMA IN PARAMETER STRING");
                    inQuotes = true;
                } else if ((c == ' ' || (c == ',' && !noGroups)) && openBrackets == 0) {
                    if (tmpStr.length() >= 1) {
                        String realTmpStr = tmpStr.toString();
                        try {
                            parsedParametersCurrentGroup.add(new NumberParameter(Integer.parseInt(realTmpStr)));
                        } catch (Exception e) {
                            parsedParametersCurrentGroup.add(new RawStringParameter(tmpStr.toString()));
                        }

                        tmpStr.setLength(0);
                    }

                    if(c == ',') {
                        parsedParameters.add(new GroupedParameter(parsedParametersCurrentGroup.toArray(new AbstractParameter[parsedParametersCurrentGroup.size()])));
                        parsedParametersCurrentGroup.clear();
                    }
                } else {
                    tmpStr.append(c);
                }
            }
        }

        if (inQuotes || inBackslash || openBrackets > 0)
            throw new SyntaxException("IN PARAMETER STRING");

        if(tmpStr.length() > 0) {
            String realTmpStr = tmpStr.toString();
            try {
                parsedParametersCurrentGroup.add(new NumberParameter(Integer.parseInt(realTmpStr)));
            } catch (Exception e) {
                parsedParametersCurrentGroup.add(new RawStringParameter(tmpStr.toString()));
            }
        }

        if(parsedParametersCurrentGroup.size() > 0) {
            parsedParameters.add(new GroupedParameter(parsedParametersCurrentGroup.toArray(new AbstractParameter[parsedParametersCurrentGroup.size()])));
            parsedParametersCurrentGroup.clear();
        }

        for(AbstractParameter parameter : parsedParameters)
            validateParameter(parameter);

        return parsedParameters.toArray(new GroupedParameter[parsedParameters.size()]);
    }

    protected void setData(BasicProgram program, int line, String parameters) {
        this.program = program;
        this.line = line;

        TokenName tokenName = this.getClass().getAnnotation(TokenName.class);

        if(tokenName.ignoreParameters()) {
            this.parametersSplitDetailed = new GroupedParameter[0];
            this.parametersRaw = "";
        } else {
            this.parametersSplitDetailed = parseParameters(parameters, tokenName.noGroups());
            this.parametersRaw = parameters;
        }
    }

    public void insert()  {
        addLine(getCode());
    }

    public String getCode() {
        String code = getCode("\t\t");
        if(code == null || code.isEmpty())
            return null;
        return code;
    }

    public abstract String getCode(String prefix);

    protected void addLine(String lineStr) {
        if(line < 0 || lineStr == null || lineStr.isEmpty())
            return;
        program.addLine(line, lineStr);
    }

    public String getAsConditionalParameters(int start, boolean negate) {
        return getAsConditionalParameters(parametersSplitDetailed, start, parametersSplitDetailed.length, negate);
    }

    public String getAsConditionalParameters(int start, int end, boolean negate) {
        return getAsConditionalParameters(parametersSplitDetailed, start, end, negate);
    }

    public static String getAsConditionalParameters(AbstractParameter[] parametersSplitDetailed, int start, int end, boolean negate) {
        StringBuilder ret = new StringBuilder();

        outer_loop:
        for(int i = start; i < end; i++) {
            AbstractParameter parameter = fixConditionalParameter(parametersSplitDetailed[i]);
            if(i < end - 2) {
                AbstractParameter nextParam = fixConditionalParameter(parametersSplitDetailed[i + 1]);
                if(nextParam instanceof RawStringParameter) {
                    boolean needsDotEquals = false;
                    if (parameter instanceof StringLiteralParameter)
                        needsDotEquals = true;
                    else if (parameter instanceof BracketedParameter) {
                        BracketedParameter funcCall = (BracketedParameter) parameter;
                        if (funcCall.preBracket.charAt(funcCall.preBracket.length() - 1) == '$')
                            needsDotEquals = true;
                    }
                    if (needsDotEquals) {
                        AbstractParameter compareToParam;
                        switch (nextParam.getStringValue()) {
                            case "!=":
                                ret.append('!');
                            case "==":
                                compareToParam = fixConditionalParameter(parametersSplitDetailed[i + 2]);
                                if(i > start)
                                    ret.append(parameter.getSeparator());
                                ret.append(parameter);
                                ret.append(".equals(");
                                ret.append(compareToParam);
                                ret.append(")");
                                i += 2;
                                continue outer_loop;
                        }
                    }
                }
            }
            if(i > start)
                ret.append(parameter.getSeparator());
            ret.append(parameter);
        }
        if(negate)
            return "!(" + ret.toString() + ")";
        return ret.toString();
    }

    public String getAsAssignmentParameters(int start) {
        return getAsAssignmentParameters(parametersSplitDetailed, start, parametersSplitDetailed.length);
    }

    public String getAsAssignmentParameters(int start, int end) {
        return getAsAssignmentParameters(parametersSplitDetailed, start, end);
    }

    public static String getAsAssignmentParameters(AbstractParameter[] parametersSplitDetailed, int start, int end) {
        StringBuilder ret = new StringBuilder();
        for(int i = start; i < end; i++) {
            AbstractParameter parameter = fixAssignmentParamater(parametersSplitDetailed[i]);
            if(i > start)
                ret.append(parameter.getSeparator());
            ret.append(parameter);
        }
        return ret.toString();
    }

    public static void validateVarFuncName(String name) {
        if(name.charAt(0) == '$')
            throw new SyntaxException("$ NOT ALLOWED AT START OF FUNC OR VAR");
        if(name.indexOf('.') >= 0 || name.indexOf('{') >= 0 || name.indexOf('}') >= 0)
            throw new SyntaxException("./{/} NOT ALLOWED IN FUNC OR VAR");
    }

    public static void validateParameter(AbstractParameter parameter) {
        if(parameter instanceof BracketedParameter)
            validateVarFuncName(((BracketedParameter)parameter).preBracket);
        else if(parameter instanceof RawStringParameter)
            validateVarFuncName(parameter.getStringValue());
    }

    public static AbstractParameter fixAssignmentParamater(AbstractParameter parameter) {
        if(!(parameter instanceof RawStringParameter))
            return parameter;

        String param = parameter.getAsParameter();
        if(param.isEmpty())
            return parameter;

        switch (param) {
            case "NOT":
                param = "!";
                break;
            case "AND":
                param = "&";
                break;
            case "OR":
                param = "|";
                break;
            case "XOR":
                param = "^";
                break;
        }

        return new RawStringParameter(param);
    }

    public static AbstractParameter fixConditionalParameter(AbstractParameter parameter) {
        if(!(parameter instanceof RawStringParameter))
            return parameter;

        String param = parameter.getAsParameter();
        switch (param) {
            case "=":
                param = "==";
                break;
            case "<>":
                param = "!=";
                break;
            case "NOT":
                param = "!";
                break;
            case "AND":
                param = "&&";
                break;
            case "OR":
                param = "||";
                break;
        }

        return new RawStringParameter(param);
    }
}
