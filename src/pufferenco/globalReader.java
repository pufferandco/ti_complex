package pufferenco;

import pufferenco.variables.Constant;
import pufferenco.variables.Variable;

import java.util.List;

import static pufferenco.Token.TokenTypes;

class globalReader {
    static void read(List<Token> tokens, AssemblyBuilder builder) {
        TokenStream stream = new TokenStream(tokens, builder);
        while (stream.isNotEmpty()) {
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
                    ExpressionReader.evalExpression(stream, builder, false);
                    builder.append_pop("DE");
                    if(!WhileReader.While_continue_stack.isEmpty()) {
                        builder.append_ld("B", String.valueOf(Function.Function_depth_stack.peek()));
                        builder.append_call("multi_block_leave");
                    }
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
                case TokenTypes.USE, TokenTypes.ASM -> UseReader.read(stream, builder, token.type == TokenTypes.ASM);
                case TokenTypes.NEW_LINE -> builder.tIC_line++;
                case TokenTypes.VAR -> Variable.init(stream, builder);
                case TokenTypes.CONST -> Constant.init(stream, builder);
                case TokenTypes.NAT -> NativeFunction.read(stream, builder);


                default -> builder.error("unknown token: " + token.content);
            }
        }
    }
}
