package pufferenco;

import pufferenco.variables.StackElement;
import pufferenco.variables.Variable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static pufferenco.Main.getId;

public class ExpressionReader {
    public static List<StackElement> readParameters(TokenStream outer_stream, AssemblyBuilder builder) {
        Token params = outer_stream.read();
        if (params.type != 0) {
            builder.error("function call is not followed by parameters");
            throw new RuntimeException();
        }

        ArrayList<StackElement> type_list = new ArrayList<>();
        ArrayList<Token> parameter = new ArrayList<>();
        TokenStream inner_stream = new TokenStream(Token.tokenize(params.content), builder);

        while (inner_stream.isNotEmpty()) {
            Token token = inner_stream.read();
            if (token.type == Token.TokenTypes.CONTROL && token.content.equals(",")) {
                if (parameter.isEmpty())
                    builder.error("trailing comma's");
                else {
                    type_list.add(evalExpression(new TokenStream(parameter, builder), builder, false));
                    parameter = new ArrayList<>();
                }
            } else {
                parameter.add(token);
            }
        }

        type_list.add(evalExpression(new TokenStream(parameter, builder), builder, false));
        return type_list;
    }

    public static StackElement evalExpression(TokenStream stream, AssemblyBuilder builder, boolean allow_constant) {
        StackElement type = null;

        while(stream.isNotEmpty()) {
            Token token = stream.read();
            type = switch (token.type) {
                case Token.TokenTypes.QUOTE, Token.TokenTypes.DOUBLE_QUOTE -> evalString(token, stream, builder, type);
                case Token.TokenTypes.IDENTIFIER, Token.TokenTypes.ARITHMETIC-> evalIdentifier(token, stream, builder, type, allow_constant);
                case Token.TokenTypes.ROUND_BRACKETS-> evalExpression(new TokenStream(Token.tokenize(token.content), builder), builder, allow_constant);
                default -> {
                    builder.error("unknown parameter" + token.content);
                    yield null;
                }
            };
        }

        if(type == null)
            builder.error("empty parameter");
        return type;
    }

    private static StackElement evalMathematicsExpression(Token operator, TokenStream tokenStream, AssemblyBuilder builder, StackElement prev, boolean allow_constant){
        LinkedList<Token> Tokens = new LinkedList<>();

        while(tokenStream.isNotEmpty()){
            Token current = tokenStream.read();

            if(current.type == Token.TokenTypes.ARITHMETIC || current.type == Token.TokenTypes.AND || current.type == Token.TokenTypes.OR){
                prev = DataType.getInstance(prev.type)
                        .callOperator(
                                operator.content, builder,
                                evalExpression(new TokenStream(Tokens, builder), builder, true)
                        );
                operator = current;
                Tokens = new LinkedList<>();
            }else
                Tokens.add(current);
        }

        return DataType.getInstance(prev.type)
                .callOperator(
                        operator.content, builder,
                        evalExpression(new TokenStream(Tokens, builder), builder, false)
                );
    }

    private static StackElement evalString(Token token, TokenStream tokenStream, AssemblyBuilder builder, StackElement prev) {
        if(prev != null)
            builder.error("cannot have string as second parameter element");

        String id = "string_" + getId();

        Main.Constants.append_tag(id);
        Main.Constants.append_db("\"" + token.content + "\", 0");

        builder.append_ld("HL", id);
        builder.append_push("HL");

        return new StackElement(id, DataType.STRING);
    }

    private static StackElement evalIdentifier(Token token, TokenStream tokenStream, AssemblyBuilder builder, StackElement prev, boolean allow_constant) {
        if(prev == null) {
            StackElement byte_type = evalByte(token, builder, allow_constant);
            if(byte_type != null)
                return byte_type;

            if (NativeFunction.exists(token.content))
                return NativeFunction.exec(token.content, readParameters(tokenStream, builder), builder, true);


            if (Variable.exists(token.content))
                return Variable.get(token.content, builder);

        }else{
            if(token.type == Token.TokenTypes.ARITHMETIC)
                return evalMathematicsExpression(token, tokenStream, builder, prev, allow_constant);
        }

        builder.error("unknown parameter: " + token.content);
        return null;
    }

    private static StackElement evalByte(Token token, AssemblyBuilder builder, boolean allow_constant){
        try {
            byte number = Byte.parseByte(token.content);

            if(allow_constant){
                return new StackElement("byteC_" + Main.getId(), DataType.BYTE, number);
            }else {
                builder.append_ld("H", String.valueOf(number));
                builder.append_push("HL");
                return new StackElement("byte_" + Main.getId(), DataType.BYTE);
            }
        } catch (NumberFormatException ignore) {
            return null;
        }
    }
}
