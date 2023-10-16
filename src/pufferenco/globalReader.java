package pufferenco;

import pufferenco.variables.Variable;

import java.util.List;

import static pufferenco.Token.TokenTypes;
class globalReader {
    static void read(List<Token> tokens, AssemblyBuilder builder){

        TokenStream stream = new TokenStream(tokens, builder);
        while(stream.isNotEmpty()) {
            Token token = stream.read();
            switch (token.type) {
                case TokenTypes.IDENTIFIER -> {
                    if (NativeFunction.exists(token.content))
                        NativeFunction.exec(token.content, ExpressionReader.readParameters(stream, builder), builder, false);
                    else if(Variable.exists(token.content))
                        Variable.set(token.content, builder, stream);
                    else
                        throw new RuntimeException("unknown function: " + token.content);
                }
                case TokenTypes.NEW_LINE -> builder.tIC_line++;
                case TokenTypes.VAR -> Variable.init(stream, builder);
                default -> throw new RuntimeException("unknown global token: " + token.content);
            }
        }
    }
}
