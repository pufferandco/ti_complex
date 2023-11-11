package pufferenco;

import pufferenco.variables.DataStack;
import pufferenco.variables.StackElement;
import pufferenco.variables.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class Function {
    private static final HashMap<String, ArrayList<Function>> Functions = new HashMap<>();
    public static final Stack<Integer> Function_depth_stack = new Stack<>();
    public static AssemblyBuilder FunctionBuilder = new AssemblyBuilder();

    public static boolean exists(String name) {
        return Functions.containsKey(name);
    }

    public static StackElement exec(String name, List<StackElement> params, AssemblyBuilder builder, boolean return_value) {
        ArrayList<Function> functions = Functions.get(name);
        outer:
        for (Function function : functions) {
            if (function.parameters.size() != params.size())
                continue;
            for (int i = 0; i < function.parameters.size(); i++) {
                if (function.parameters.get(i).type.getId() != params.get(i).type)
                    continue outer;
            }
            function.call(builder, return_value);
            if (return_value)
                return new StackElement("name_return_value_" + Main.getId(), function.return_type);
            return null;
        }
        StringBuilder string_params = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            string_params.append(DataType.NAMES[params.get(i).type]);
            if (i!= params.size() - 1)
                string_params.append(", ");
        }
        builder.error("function not found: " + name + "(" + string_params + ")");
        throw new IllegalStateException();
    }

    public static void read(TokenStream stream, AssemblyBuilder builder) {
        Token name_token = stream.read();
        if (name_token.type != Token.TokenTypes.IDENTIFIER)
            builder.error("expected function name after fun keyword");
        String name = name_token.content;

        Token params_token = stream.read();
        if (params_token.type != Token.TokenTypes.ROUND_BRACKETS)
            builder.error("expected round brackets after fun name");


        ArrayList<ArrayList<Token>> param_tokens = new ArrayList<>();
        ArrayList<Token> current_param_token = new ArrayList<>();
        TokenStream param_stream = new TokenStream(Token.tokenize(params_token.content), builder);
        while (param_stream.isNotEmpty()) {
            Token current_token = param_stream.read();
            if (current_token.type == Token.TokenTypes.CONTROL && current_token.content.equals(",")) {
                param_tokens.add(current_param_token);
                current_param_token = new ArrayList<>();
                continue;
            }
            current_param_token.add(current_token);
        }
        if(!current_param_token.isEmpty())
            param_tokens.add(current_param_token);

        ArrayList<Parameter> parameters = new ArrayList<>();

        for (ArrayList<Token> param : param_tokens) {
            if (param.size() != 3)
                builder.error("invalid function parameter: should be <name>: <type>");

            Token param_name = param.get(0);
            if (!param.get(1).content.equals(":"))
                builder.error("invalid function parameter: should be <name>: <type>");

            Token param_type = param.get(2);
            int type = ArrayUtil.inArray(param_type.content, DataType.NAMES);
            if (type == -1)
                builder.error("invalid function parameter: should be <name>: <type>");
            parameters.add(new Parameter(param_name.content, DataType.getInstance(type)));
        }

        DataStack stack = new DataStack("IX");
        Main.VariableStacks.push(stack);

        int return_type = DataType.NULL;
        if (stream.read().content.equals("->")) {
            return_type = ArrayUtil.inArray(stream.read().content, DataType.NAMES);
            if (return_type == -1)
                builder.error("invalid return type");
        } else
            stream.backSpace();

        Token code_block = stream.read();
        if (code_block.type != Token.TokenTypes.CURLY_BRACKETS)
            builder.error("expected code block after function declaration");

        Function function = new Function(name, parameters, return_type, code_block);
        if (!Functions.containsKey(name))
            Functions.put(name, new ArrayList<>());
        Functions.get(name).add(function);

    }


    ArrayList<Parameter> parameters;
    String name;
    int return_type;
    String assembly_name;
    String end_tag;
    DataStack stack;

    Function(String name, ArrayList<Parameter> parameters, int return_type, Token code_block) {
        this.name = name;
        this.parameters = parameters;
        this.return_type = return_type;
        this.assembly_name = name + "_" + Main.getId();
        this.end_tag = name + "_end_" + Main.getId();


        FunctionBuilder.add_func(name);
        Function_depth_stack.push(0);
        FunctionBuilder.append_tag(assembly_name);
        Main.Function_stack.push(this);

        for (Parameter parameter : parameters) {
            StackElement stack_element = Main.VariableStacks.peek().push(parameter.name, 1, parameter.type.getId(), FunctionBuilder);

            new Variable(parameter.name, stack_element);
        }

        FunctionBuilder.append_push("IX").addComment("push stack_start");
        FunctionBuilder.append_ld("(" + DataStack.CallStack + ")", "SP");
        FunctionBuilder.append_ld("SP", "HL");
        FunctionBuilder.append_ld("IX", String.valueOf(parameters.size() * 3));
        FunctionBuilder.append_add("IX", "SP");

        Main.tokenizeAndRun(code_block.content, FunctionBuilder);

        FunctionBuilder.append_ld("DE", "0");

        FunctionBuilder.append_tag(end_tag);

        FunctionBuilder.append_ld("SP", "(" + DataStack.CallStack + ")");
        FunctionBuilder.append_pop("IX");
        FunctionBuilder.append_ret();

        Main.Function_stack.pop();
        Main.VariableStacks.pop();
        FunctionBuilder.remove_func();
        Function_depth_stack.pop();
    }

    void call(AssemblyBuilder builder, boolean return_value) {
        builder.append_ld("HL", "0");
        builder.append_add("HL", "SP");
        builder.append_ld("SP", "(" + DataStack.CallStack + ")");
        builder.append_call(assembly_name);

        builder.append_pop("HL");
        builder.append_ld("(" + DataStack.CallStack + ")", "SP");
        builder.append_ld("SP", "HL");
        if (return_value) {
            builder.append_push("DE");
        }
    }

    public String toString() {
        StringBuilder string_params = new StringBuilder();
        for (int i = 0; i < parameters.size(); i++) {
            string_params.append(DataType.NAMES[parameters.get(i).type.getId()]);
            if (i!= parameters.size() - 1)
                string_params.append(", ");
        }

        return name + "(" + string_params + ")";
    }
}
