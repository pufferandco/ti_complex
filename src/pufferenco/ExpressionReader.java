package pufferenco;

import pufferenco.variables.Constant;
import pufferenco.variables.StackElement;
import pufferenco.variables.Variable;
import pufferenco.variables.types.ArrayType;
import pufferenco.variables.types.StringType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static pufferenco.ArrayUtil.inArray;
import static pufferenco.ArrayUtil.isInArray;
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
        if (!parameter.isEmpty())
            type_list.add(evalExpression(new TokenStream(parameter, builder), builder, false));
        return type_list;
    }

    public static StackElement evalExpression(TokenStream stream, AssemblyBuilder builder, boolean allow_constant) {
        if (stream.isEmpty())
            builder.error("empty stream");
        StackElement prev = null;

        while (stream.isNotEmpty()) {
            Token token = stream.read();

            prev = switch (token.type) {
                case Token.TokenTypes.QUOTE, Token.TokenTypes.DOUBLE_QUOTE ->
                        evalString(token, stream, builder, prev);
                case Token.TokenTypes.IDENTIFIER, Token.TokenTypes.ARITHMETIC, Token.TokenTypes.AND, Token.TokenTypes.OR, Token.TokenTypes.XOR ->
                        evalIdentifier(token, stream, builder, prev, allow_constant);
                case Token.TokenTypes.ROUND_BRACKETS ->
                        evalExpression(new TokenStream(Token.tokenize(token.content), builder), builder, allow_constant);
                case Token.TokenTypes.TRUE, Token.TokenTypes.FALSE ->
                        evalBool(token, builder, allow_constant);
                case Token.TokenTypes.SQUARE_BRACKETS ->
                        evalSquareBrackets(token, builder, prev);
                case Token.TokenTypes.NEW ->
                        newReader(stream, builder, prev);

                default -> {
                    builder.error("unknown parameter " + token.content);
                    yield null;
                }
            };
        }

        if (prev == null) {
            builder.error("empty parameter");
            throw new RuntimeException();
        }
        return prev;
    }

    private static StackElement evalMathematicsExpression(Token operator, TokenStream tokenStream, AssemblyBuilder builder, StackElement prev, boolean allow_constant) {

        LinkedList<Token> Tokens = new LinkedList<>();

        if(tokenStream.isEmpty() && operator.content.equals("++")) {
            DataType type = DataType.getInstance(prev.type);
            StackElement increment;
            if(type.getId() == DataType.POINTER)
                increment = type.callOperator("+", builder, new StackElement(1, DataType.BYTE, "1"));
            else
                increment = type.callOperator("+", builder, new StackElement(1, prev.type, "1"));
            type.setStackVariable(prev, increment, builder);
            type.getStackVariable(prev, builder);
            return increment;
        }

        while (tokenStream.isNotEmpty()) {
            Token current = tokenStream.read();

            if (current.type == Token.TokenTypes.ARITHMETIC || current.type == Token.TokenTypes.AND || current.type == Token.TokenTypes.OR || current.type == Token.TokenTypes.XOR) {
                prev = DataType.getInstance(prev.type)
                        .callOperator(
                                operator.content, builder,
                                evalExpression(new TokenStream(Tokens, builder), builder, true)
                        );
                operator = current;
                Tokens = new LinkedList<>();
            } else
                Tokens.add(current);
        }
        StackElement right = evalExpression(new TokenStream(Tokens, builder), builder, true);

        return DataType.getInstance(prev.type)
                .callOperator(
                        operator.content,
                        builder,
                        right
                );
    }

    private static StackElement evalString(Token token, TokenStream tokenStream, AssemblyBuilder builder, StackElement prev) {
        if (prev != null)
            builder.error("cannot have string as second parameter element");

        String id;

        if(StringType.strings.containsKey(token.content)){
            id = StringType.strings.get(token.content);
        }else {
            id = "string_" + getId();
            Main.Constants.append_tag(id);
            Main.Constants.append_db("\"" + token.content + "\", 0");
            StringType.strings.put(token.content, id);
        }

        builder.append_ld("HL", id);
        builder.append_push("HL");

        return new StackElement(id, DataType.STRING);
    }

    private static StackElement evalIdentifier(Token token, TokenStream tokenStream, AssemblyBuilder builder, StackElement prev, boolean allow_constant) {
        if (prev == null) {
            StackElement byte_type = evalByte(token, builder, allow_constant);
            if (byte_type != null)
                return byte_type;

            StackElement double_type = evalInt(token, builder, allow_constant);
            if (double_type != null)
                return double_type;

            StackElement pointer_type = evalPointer(token, builder, allow_constant);
            if (pointer_type!= null)
                return pointer_type;

            StackElement tag_type = evalTag(token, tokenStream, builder);
            if (tag_type!= null)
                return tag_type;

            StackElement type_type = evalType(token, tokenStream, builder, allow_constant);
            if (type_type!= null)
                return type_type;

            if (NativeFunction.exists(token.content))
                return NativeFunction.exec(token.content, readParameters(tokenStream, builder), builder, true);

            if (Function.exists(token.content)) {
                builder.append_call("sub_block_enter");
                return Function.exec(token.content, ExpressionReader.readParameters(tokenStream, builder), builder, true);
            }

            if (Variable.exists(token.content) || Variable.Globals.containsKey(token.content))
                return Variable.get(token.content, builder);

            if (Constant.constants.containsKey(token.content))
                return Constant.get(token.content, builder, allow_constant);

        } else {
            if (token.type == Token.TokenTypes.ARITHMETIC || token.type == Token.TokenTypes.AND || token.type == Token.TokenTypes.OR || token.type == Token.TokenTypes.XOR)
                return evalMathematicsExpression(token, tokenStream, builder, prev, allow_constant);
        }

        builder.error("unknown parameter: " + token.content);
        return null;
    }


    private static StackElement evalByte(Token token, AssemblyBuilder builder, boolean allow_constant) {
        try {

            if (token.content.charAt(token.content.length()-1) != 'b')
                return null;

            int number = Integer.parseInt(token.content.substring(0, token.content.length() - 1));

            if (number < 0 || number > 255)
                return null;

            if (allow_constant) {
                return new StackElement(number, DataType.BYTE, "byteC_" + Main.getId());
            } else {

                builder.append_ld("A", String.valueOf(number)).addComment(token.content);
                builder.append_push("AF");
                return new StackElement("byte_" + Main.getId(), DataType.BYTE);
            }
        } catch (NumberFormatException ignore) {
            return null;
        }
    }

    private static StackElement evalPointer(Token token, AssemblyBuilder builder, boolean allow_constant) {
        try {
            if (token.content.charAt(token.content.length()-1) != 'p')
                return null;

            int number = Integer.parseInt(token.content.substring(0, token.content.length() - 1));

            if (number < 0 || number > 16777216)
                return null;

            if (allow_constant) {
                return new StackElement(number, DataType.POINTER, "pointerC_" + Main.getId());
            } else {
                builder.append_ld("HL", String.valueOf(number));
                builder.append_push("HL");
                return new StackElement("pointer_" + Main.getId(), DataType.POINTER);
            }
        } catch (NumberFormatException ignore) {
            return null;
        }
    }

    private static StackElement evalTag(Token token, TokenStream stream, AssemblyBuilder builder) {
        try {
            if (!token.content.equals("&"))
                return null;

            String location = stream.read().content;

            builder.append_ld("HL", location);
            builder.append_push("HL");
            return new StackElement("pointer_" + Main.getId(), DataType.POINTER);

        } catch (NumberFormatException ignore) {
            return null;
        }
    }

    private static StackElement evalInt(Token token, AssemblyBuilder builder, boolean allow_constant) {
        try {
            if (token.content.charAt(token.content.length()-1) != 'i')
                return null;

            int number = Integer.parseInt(token.content.substring(0, token.content.length() - 1));

            if (number < 0 || number > 65535)
                return null;

            if (allow_constant) {
                return new StackElement(number, DataType.INT, "intC_" + Main.getId());
            } else {
                builder.append_ld("HL", String.valueOf(number));
                builder.append_push("HL");
                return new StackElement("int_" + Main.getId(), DataType.INT);
            }
        } catch (NumberFormatException ignore) {
            return null;
        }
    }

    private static StackElement evalBool(Token token, AssemblyBuilder builder, boolean allow_constant) {
        if (allow_constant) {
            return new StackElement(token.content, DataType.BOOL, "boolC_" + Main.getId());
        } else {
            if (token.type == Token.TokenTypes.TRUE)
                builder.append_ld("A", "%11111111");
            else
                builder.append_ld("A", "%00000000");
            builder.append_push("AF");
            return new StackElement("bool_" + Main.getId(), DataType.BOOL);
        }
    }

    private static StackElement evalType(Token token, TokenStream tokenStream, AssemblyBuilder builder, boolean allow_constant){
        if(!isInArray(token.content, DataType.NAMES))
            return null;

        if(tokenStream.isNotEmpty()) {
            Token next = tokenStream.read();
            if (next.type == Token.TokenTypes.ROUND_BRACKETS) {
                return DataType.getInstance(inArray(token.content, DataType.NAMES)).convertFrom(evalExpression(new TokenStream(next.content, builder), builder, false), builder, allow_constant);
            }
            tokenStream.backSpace();
        }

        if(!allow_constant)
            builder.error("type has to be constant");

        return new StackElement(inArray(token.content, DataType.NAMES), DataType.TYPE , "type_" + Main.getId());
    }

    private static StackElement evalSquareBrackets(Token brackets, AssemblyBuilder builder, StackElement prev) {
        if(prev == null)
            return null;

        if(prev.type == DataType.NULL)
            builder.error("tried to access sub-element of a null element");

        StackElement key = evalExpression(new TokenStream(brackets.content, builder), builder, true);
        return DataType.getInstance(prev.type).getSub(prev, key, builder);
    }

    private static StackElement newReader(TokenStream stream, AssemblyBuilder builder, StackElement prev){
        if(prev != null)
            return null;
        Token type = stream.read();
        if(type.type != Token.TokenTypes.IDENTIFIER || !isInArray(type.content, DataType.NAMES))
            builder.error("token after [new] required to be type");
        DataType data_type = DataType.getInstance(inArray(type.content, DataType.NAMES));

        Token next = stream.read();
        if(next.type == Token.TokenTypes.SQUARE_BRACKETS){
            StackElement size = evalExpression(new TokenStream(next.content, builder), builder, false);
            if(size.type != DataType.INT)
                builder.error("array size mst be defined by a integer");

            if(stream.read().type != Token.TokenTypes.AT)
                builder.error("not [at] after [new]");

            StackElement pointer = evalExpression(stream, builder, false);
            if(pointer.type != DataType.POINTER)
                builder.error("expression after [at] should result in a pointer");

            return ArrayType.createNew(data_type.getId(), size, pointer, builder);
        }
        //if(next.type == Token.TokenTypes.CURLY_BRACKETS) {
        //    ArrayList<Token> tokens = Token.tokenize(next.content);
        //    ArrayList<Token> current = new ArrayList<>();
        //    ArrayList<StackElement> elements = new ArrayList<>();
//
        //    for (Token token : tokens) {
        //        if(token.type == Token.TokenTypes.CONTROL && token.content.equals(",")){
        //            elements.add(evalExpression(new TokenStream(current, builder), builder, true));
        //            current = new ArrayList<>();
        //        }else
        //            current.add(token);
        //    }
//
        //    StringBuilder string_builder = new StringBuilder();
        //    int size = elements.size() * data_type.elementSize(builder);
        //    for (int i = 0; i < elements.size(); i++) {
        //        StackElement element = elements.get(i);
        //        if(element.is_constant && (data_type.getId() == DataType.BYTE || data_type.getId() == DataType.INT)){
        //            string_builder.append((int) element.Constant_value).append(", ");
        //        }else
        //            builder.error("type in array initialization must be a constant");
        //    }
        //
        //    String tag = "default_array_" + Main.getId();
        //    Main.Constants.append_tag(tag);
        //    Main.Constants.append_db(string_builder.toString());
        //
        //    builder.append_ld("HL", "tag");
        //
        //}


        builder.error("required array after new statement");
        throw new RuntimeException();
    }
}
