package pufferenco.readers;

import pufferenco.AssemblyBuilder;
import pufferenco.Main;
import pufferenco.Token;
import pufferenco.TokenStream;
import pufferenco.variables.Constant;
import pufferenco.variables.Variable;

import java.util.List;

import static pufferenco.Token.TokenTypes;

public class globalReader {
    public static void read(List<Token> tokens, AssemblyBuilder builder) {
        TokenStream stream = new TokenStream(tokens, builder);
        Token token = stream.read();

        switch (token.type) {
            case TokenTypes.IDENTIFIER -> {
                if(NativeFunction.exists(token.content))
                    NativeFunction.exec(token.content, ExpressionReader.readParameters(stream, builder), builder, false);
                else if (Function.exists(token.content)) {
                    builder.append_call("sub_block_enter");
                    Function.exec(token.content, ExpressionReader.readParameters(stream, builder), builder, false);
                }
                else if(Variable.exists(token.content))
                    Variable.set(token.content, builder, stream);
                else
                    builder.error("unknown token: " + token.content);
            }
            case TokenTypes.IF -> {
                Variable.increase_scope(builder);
                IfReader.read(stream, builder);
                Variable.decrease_scope();
            }
            case TokenTypes.WHILE -> {
                Variable.increase_scope(builder);
                WhileReader.read(stream, builder);
                Variable.decrease_scope();
            }
            case TokenTypes.FUN -> {
                Variable.increase_scope(builder);
                Function.read(stream, builder);
                Variable.decrease_scope();
            }
            case TokenTypes.RETURN -> {
                if(stream.isNotEmpty()) {
                    ExpressionReader.evalExpression(stream, builder, false);
                    builder.append_pop("DE");
                }
                if(!Function.Function_depth_stack.isEmpty()) {
                    if(Function.Function_depth_stack.peek() != 0) {
                        builder.append_ld("B", String.valueOf(Function.Function_depth_stack.peek()));
                        builder.append_call("multi_block_leave");
                    }
                }else
                    builder.error("return statement outside of function");
                builder.append_jp(Main.Function_stack.peek().end_tag);
            }
            case TokenTypes.CONTINUE -> {
                if(WhileReader.While_continue_stack.isEmpty())
                    builder.error("tried to continue outside of a while loop");
                builder.append_ld("B", String.valueOf(WhileReader.While_depth.peek()));
                builder.append_call("multi_block_leave");
                builder.append_jp(WhileReader.While_continue_stack.peek());
            }case TokenTypes.BREAK -> {
                if(WhileReader.While_break_stack.isEmpty())
                    builder.error("tried to break outside of a while loop");
                builder.append_ld("B", String.valueOf(WhileReader.While_depth.peek()));
                builder.append_call("multi_block_leave");
                builder.append_jp(WhileReader.While_break_stack.peek());
            }
            case TokenTypes.MOVE -> MoveReader.read(stream, builder);
            case TokenTypes.IMPORT, TokenTypes.ASM -> ImportReader.read(stream, builder, token.type == TokenTypes.ASM);
            case TokenTypes.VAR -> Variable.init(stream, builder);
            case TokenTypes.CONST -> Constant.init(stream, builder);
            case TokenTypes.NAT -> NativeFunction.read(stream, builder);
            case TokenTypes.THROW -> ThrowReader.read(stream, builder);
            case TokenTypes.COPY -> CopyReader.read(stream, builder);

            default -> builder.error("unknown token: " + token.content);
        }
        if(stream.isNotEmpty()){
            builder.error("global statement is not finished (probably forgot a semicolon");
        }

    }
}
