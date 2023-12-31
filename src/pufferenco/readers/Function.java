package pufferenco.readers;

import pufferenco.*;
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
                if (function.parameters.get(i).type.getId() != params.get(i).getType())
                    continue outer;
            }
            function.call(builder, return_value);
            if (return_value){
                if(function.return_type == null)
                    builder.error("function " + name + " does not return");
                return function.return_type.retrieve();
            }

            return null;
        }
        StringBuilder string_params = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            string_params.append(DataType.NAMES[params.get(i).getType()]);
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
            if (param.size() < 3)
                builder.error("invalid function parameter: should be <name>: <type>");

            Token param_name = param.get(0);
            if (!param.get(1).content.equals(":"))
                builder.error("invalid function parameter: should be <name>: <type>");

            Token param_type = param.get(2);
            int type = ArrayUtil.inArray(param_type.content, DataType.NAMES);
            if (type == -1)
                builder.error("invalid function parameter: should be <name>: <type>");
            if(param.size() == 4){

                if(!(param.get(3).type == Token.TokenTypes.SQUARE_BRACKETS && param.get(3).content.isEmpty()))
                    builder.error("invalid function parameter: should be <name>: <type>");
                parameters.add(Parameter.ArrayParameter(param_name.content, DataType.getInstance(type)));
            }else
                parameters.add(new Parameter(param_name.content, DataType.getInstance(type)));
        }

        DataStack stack = new DataStack("IX");
        Main.VariableStacks.push(stack);

        StackElement return_type = null;
        if (stream.read().content.equals("->")) {
            return_type = new StackElement("return_value", ArrayUtil.inArray(stream.read().content, DataType.NAMES));
            if (return_type.getType() == -1)
                builder.error("invalid return type");

            if(stream.read().type == Token.TokenTypes.SQUARE_BRACKETS){
                return_type.setArray_type(return_type.getType());
                return_type.setType(DataType.ARRAY);
            }else
                stream.backSpace();
        } else
            stream.backSpace();

        Token code_block = stream.read();
        if (code_block.type != Token.TokenTypes.CURLY_BRACKETS)
            builder.error("expected code block after function declaration");

        new Function(name, parameters, return_type, code_block);
    }


    ArrayList<Parameter> parameters;
    String name;
    StackElement return_type;
    String assembly_name;
    String end_tag;
    DataStack stack;

    Function(String name, ArrayList<Parameter> parameters, StackElement return_type, Token code_block) {
        this.name = name;
        this.parameters = parameters;
        this.return_type = return_type;
        this.assembly_name = name + "_" + Main.getId();
        this.end_tag = name + "_end_" + Main.getId();

        if (!Functions.containsKey(name))
            Functions.put(name, new ArrayList<>());
        Functions.get(name).add(this);

        FunctionBuilder.add_func(name);
        Function_depth_stack.push(0);
        FunctionBuilder.append_tag(assembly_name);
        Main.Function_stack.push(this);

        for (Parameter parameter : parameters) {
            StackElement stack_element;
            if(parameter.subtype != null){
                stack_element = Main.VariableStacks.peek().push(parameter.name, 1, DataType.ARRAY, FunctionBuilder);
                stack_element.setArray_type(parameter.subtype.getId());
            }else {
                stack_element = Main.VariableStacks.peek().push(parameter.name, 1, parameter.type.getId(), FunctionBuilder);
            }

            new Variable(parameter.name, stack_element);
        }

        FunctionBuilder.append_push("IX");
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
