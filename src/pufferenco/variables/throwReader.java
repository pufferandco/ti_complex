package pufferenco.variables;

import pufferenco.*;

public class throwReader {
    public static void read(TokenStream stream, AssemblyBuilder builder){
        StackElement error = ExpressionReader.evalExpression(stream, builder, false);
        if(error.type != DataType.STRING)
            builder.error("expected string after throw");
        builder.append_jp("thrown_error");
    }
}
