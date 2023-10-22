package pufferenco;

import pufferenco.variables.DataStack;

import static pufferenco.Main.tokenizeAndRun;

public class WhileReader {
    static void read(TokenStream stream, AssemblyBuilder builder) {
        Token while_condition = stream.read();
        if(while_condition.type != Token.TokenTypes.ROUND_BRACKETS) {
            builder.error("no condition block following while statement");
        }
        ExpressionReader.evalExpression(new TokenStream(Token.tokenize(while_condition.content), builder), builder, false);

        Token while_block = stream.read();
        if (while_block.type!= Token.TokenTypes.CURLY_BRACKETS) {
            builder.error("no code block following while statement");
        }
        String while_start = "while_start" + Main.getId();
        String while_end = "while_end" + Main.getId();

        builder.append_tag(while_start);

        builder.append_ld("HL", "0");
        builder.append_add("HL", "SP");
        builder.append_ld("SP", "(" + DataStack.CallStack + ")");
        builder.append_push("HL");
        builder.append_ld("(" + DataStack.CallStack + ")", "SP");
        builder.append_ld("SP", "HL");// 25 cycles

        builder.append_pop("AF");
        builder.append_cp("A", "%11111111");
        builder.append_jp("NZ", while_end);

        tokenizeAndRun(while_block.content, builder);

        builder.append_ld("SP", "(" + DataStack.CallStack + ")");
        builder.append_pop("HL");
        builder.append_ld("(" + DataStack.CallStack + ")", "SP");
        builder.append_ld("SP", "HL");

        builder.append_jp(while_start);
        builder.append_tag(while_end);

    }
}
