package pufferenco;

import pufferenco.variables.StackElement;

import java.util.ArrayList;

public class MoveReader {
    public static void read(TokenStream stream, AssemblyBuilder builder) {
        ArrayList<Token> pre_tokens = new ArrayList<>();
        while (stream.isNotEmpty()) {
            Token next = stream.read();
            if(next.type == Token.TokenTypes.TO){
                break;
            }
            pre_tokens.add(next);
        }
        StackElement element = ExpressionReader.evalExpression(new TokenStream(pre_tokens, builder), builder, false);

        StackElement pointer = ExpressionReader.evalExpression(stream, builder, false);
        if(pointer.type != DataType.POINTER)
            builder.error("expected pointer in move statement");

        switch (element.type) {
            case DataType.INT -> {
                builder.append_pop("HL");
                builder.append_pop("DE");
                builder.append_ld("(HL)", "E");
                builder.append_inc("HL");
                builder.append_ld("(HL)", "D");
            }
            case DataType.BYTE, DataType.BOOL -> {
                builder.append_pop("HL");
                builder.append_pop("AF");
                builder.append_ld("(HL)", "A");
            }
            case DataType.ARRAY, DataType.POINTER, DataType.STRING  ->{
                builder.append_pop("HL");
                builder.append_pop("DE");
                builder.append_ld("(HL)", "DE");
            }
            case DataType.NULL -> builder.error("cannot move to pointer cause it is null");

            default -> builder.error("unexpected type in move statement");
        }
    }
}
