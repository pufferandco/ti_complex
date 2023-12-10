package pufferenco.readers;

import pufferenco.AssemblyBuilder;
import pufferenco.DataType;
import pufferenco.Token;
import pufferenco.TokenStream;
import pufferenco.variables.StackElement;

import java.util.ArrayList;

public class CopyReader {
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
            builder.error("expected pointer in copy statement");

        switch (element.type) {
            case DataType.INT, DataType.BYTE, DataType.BOOL, DataType.POINTER ->
                    builder.error("cannot copy" + DataType.getName(element.type)+ "(use move instead)");
            case DataType.ARRAY  -> builder.append_call("copy_array");
            case DataType.NULL -> builder.error("cannot move to pointer cause it is null");

            default -> builder.error("unexpected type in move statement");
        }
        builder.append_pop("HL").addComment("void return value");
    }
}
