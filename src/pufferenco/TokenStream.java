package pufferenco;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class TokenStream {
    private final List<Token> tokens;
    AssemblyBuilder builder;
    int index = 0;

    TokenStream(List<Token> tokens, AssemblyBuilder builder){
        this.tokens = tokens;
        this.builder = builder;
    }

    public Token read(){
        if(index == tokens.size())
            builder.error("token stream reached end");
        Token token = tokens.get(index++);
        if(token.type == Token.TokenTypes.NEW_LINE)
            builder.tIC_line++;
        return token;
    }

    public boolean isNotEmpty(){
        return index < tokens.size();
    }

    public boolean isEmpty(){
        return index >= tokens.size();
    }

    public TokenStream split(Function<Token, Boolean> evaluator, boolean isInclusive){
        LinkedList<Token> list = new LinkedList<>();
        Token currentToken;
        while((currentToken = read()).type != Token.TokenTypes.NEW_LINE){
            if(isInclusive) {
                list.add(currentToken);
                if (evaluator.apply(currentToken))
                    return new TokenStream(list, builder);
            }else{
                if (evaluator.apply(currentToken))
                    return new TokenStream(list, builder);
                list.add(currentToken);
            }
        }
        return new TokenStream(list, builder);
    }

    public void backSpace(){
        index--;
    }

    public List<Token> toList(){
        LinkedList<Token> list = new LinkedList<>();
        for (int i = index; i < tokens.size(); i++) {
            list.add(tokens.get(i));
        }
        return list;
    }
}
