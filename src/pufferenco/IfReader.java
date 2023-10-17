package pufferenco;

import pufferenco.variables.StackElement;

import static pufferenco.Main.tokenizeAndRun;

class IfReader {
    static void read(TokenStream stream, AssemblyBuilder builder, String end_id) {
        header(stream, builder);

        Token if_block = stream.read();
        if (if_block.type != Token.TokenTypes.CURLY_BRACKETS) {
            builder.error("no code block following if statement");
        }
        if (!stream.isNotEmpty()) {
            builder.append_pop("AF");
            builder.append_cp("A", "%11111111");
            builder.append_jp("NZ", end_id);
            tokenizeAndRun(if_block.content, builder);
            return;
        }
        Token following_statement = stream.read();
        if (following_statement.type == Token.TokenTypes.ELSE) {
            Token else_block = stream.read();
            if (else_block.type != Token.TokenTypes.CURLY_BRACKETS) {
                builder.error("no code block following else statement");
            }
            String else_start = "true_end_" + Main.getId();
            builder.append_pop("AF");
            builder.append_cp("A", "%11111111");
            builder.append_jp("NZ", else_start);
            tokenizeAndRun(if_block.content, builder);
            builder.append_jp(end_id);
            builder.append_tag(else_start);
            tokenizeAndRun(else_block.content, builder);
            return;
        }
        if (following_statement.type == Token.TokenTypes.ELIF) {
            String next_end = "elif_next_" + Main.getId();
            builder.append_pop("AF");
            builder.append_cp("A", "%11111111");
            builder.append_jp("NZ", next_end);
            tokenizeAndRun(if_block.content, builder);
            builder.append_jp(end_id);
            builder.append_tag(next_end);

            stream.backSpace();
            while(stream.isNotEmpty()) {
                Token next_statement = stream.read();

                if (next_statement.type == Token.TokenTypes.ELSE) {
                    Token else_block = stream.read();
                    if (else_block.type!= Token.TokenTypes.CURLY_BRACKETS) {
                        builder.error("no code block following else statement");
                    }

                    tokenizeAndRun(else_block.content, builder);
                    return;
                }
                if (next_statement.type == Token.TokenTypes.ELIF) {

                    header(stream, builder);
                    if_block = stream.read();
                    if (if_block.type!= Token.TokenTypes.CURLY_BRACKETS) {
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
                builder.error("if statement is not closed");
            }
            return;
        }





    }

    private static void header(TokenStream stream, AssemblyBuilder builder) {
        Token logic_expression = stream.read();
        if (logic_expression.type != Token.TokenTypes.ROUND_BRACKETS) {
            builder.error("if statement requires a logic expression");
        }

        StackElement logic_value = ExpressionReader.evalExpression(
                new TokenStream(Token.tokenize(logic_expression.content), builder), builder, false);

        if (logic_value.type!= DataType.BOOL) {
            builder.error("if statement requires a boolean");
        }
    }


}
