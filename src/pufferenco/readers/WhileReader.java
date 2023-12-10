package pufferenco.readers;

import pufferenco.AssemblyBuilder;
import pufferenco.Main;
import pufferenco.Token;
import pufferenco.TokenStream;
import pufferenco.variables.StackElement;

import java.util.Stack;

import static pufferenco.Main.tokenizeAndRun;

public class WhileReader {
    public static Stack<String> While_continue_stack = new Stack<>();
    public static Stack<Integer> While_depth = new Stack<>();
    public static Stack<String> While_break_stack = new Stack<>();
    static void read(TokenStream stream, AssemblyBuilder builder) {
        Token while_condition = stream.read();
        if (while_condition.type != Token.TokenTypes.ROUND_BRACKETS) {
            builder.error("no condition block following while statement");
        }

        Token while_block = stream.read();
        if (while_block.type != Token.TokenTypes.CURLY_BRACKETS) {
            builder.error("no code block following while statement");
        }
        String while_start = "while_start" + Main.getId();
        String while_end = "while_end" + Main.getId();

        While_continue_stack.push(while_start);
        While_break_stack.push(while_end);
        While_depth.push(1);
        if(!Function.Function_depth_stack.isEmpty()) {
            int i = Function.Function_depth_stack.pop();
            Function.Function_depth_stack.push(i + 1);
        }

        builder.append_tag(while_start);

        StackElement logic =
                ExpressionReader.evalExpression(new TokenStream(Token.tokenize(while_condition.content), builder), builder, true);
        if(logic.is_constant) {
            if(logic.Constant_value.equals(false)) {
                While_continue_stack.pop();
                While_break_stack.pop();
                return;
            }
        }else {
            builder.append_pop("AF");
            builder.append_cp("A", "%11111111");
            builder.append_jp("NZ", while_end);
        }

        builder.append_call("sub_block_enter");

        tokenizeAndRun(while_block.content, builder);

        builder.append_call("sub_block_leave");

        builder.append_jp(while_start);
        builder.append_tag(while_end);

        While_continue_stack.pop();
        While_break_stack.pop();
        While_depth.pop();
        if(!Function.Function_depth_stack.isEmpty()) {
            int i = Function.Function_depth_stack.pop();
            Function.Function_depth_stack.push(i - 1);
        }
    }
}
