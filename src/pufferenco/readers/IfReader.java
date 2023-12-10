package pufferenco.readers;

import pufferenco.*;
import pufferenco.variables.StackElement;

import static pufferenco.Main.tokenizeAndRun;

public class IfReader {
    static void read(TokenStream stream, AssemblyBuilder builder) {
        builder.append_call("sub_block_enter");

        header(stream, builder);

        Token if_block = stream.read();
        if (if_block.type != Token.TokenTypes.CURLY_BRACKETS) {
            builder.error("no code block following if statement");
        }

        String end_id = "if_end_" + Main.getId();
        String next_end = "if_next_" + Main.getId();

        if(!WhileReader.While_depth.isEmpty()) {
            int i = WhileReader.While_depth.pop();
            WhileReader.While_depth.push(i + 1);
        }
        if(!Function.Function_depth_stack.isEmpty()) {
            int i = Function.Function_depth_stack.pop();
            Function.Function_depth_stack.push(i + 1);
        }

        builder.append_pop("AF");
        builder.append_cp("A", "%11111111");
        builder.append_jp("NZ", next_end);


        tokenizeAndRun(if_block.content, builder);

        builder.append_jp(end_id);
        builder.append_tag(next_end);

        while (stream.isNotEmpty()) {
            Token next_statement = stream.read();

            if (next_statement.type == Token.TokenTypes.ELSE) {
                Token else_block = stream.read();
                if (else_block.type != Token.TokenTypes.CURLY_BRACKETS) {
                    builder.error("no code block following else statement");
                }


                tokenizeAndRun(else_block.content, builder);
                break;
            }
            if (next_statement.type == Token.TokenTypes.ELIF) {
                header(stream, builder);
                if_block = stream.read();
                if (if_block.type != Token.TokenTypes.CURLY_BRACKETS) {
                    builder.error("no code block following else statement");
                }

                next_end = "elif_next_" + Main.getId();
                builder.append_pop("AF");
                builder.append_cp("A", "%11111111");
                builder.append_jp("NZ", next_end);

                tokenizeAndRun(if_block.content, builder);

                builder.append_jp(end_id);
                builder.append_tag(next_end);
                continue;
            }
            break;
        }
        builder.append_tag(end_id);
        builder.append_call("sub_block_leave");
        if(!WhileReader.While_depth.isEmpty()) {
            int i = WhileReader.While_depth.pop();
            WhileReader.While_depth.push(i - 1);
        }
        if(!Function.Function_depth_stack.isEmpty()) {
            int i = Function.Function_depth_stack.pop();
            Function.Function_depth_stack.push(i - 1);
        }
}

    private static void header(TokenStream stream, AssemblyBuilder builder) {
        Token logic_expression = stream.read();
        if (logic_expression.type != Token.TokenTypes.ROUND_BRACKETS) {
            builder.error("if statement requires a logic expression");
        }

        StackElement logic_value = ExpressionReader.evalExpression(
                new TokenStream(Token.tokenize(logic_expression.content), builder), builder, false);

        if (logic_value.type != DataType.BOOL) {
            builder.error("if statement requires a boolean");
        }
    }


}
