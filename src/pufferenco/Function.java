package pufferenco;

import pufferenco.variables.DataStack;
import pufferenco.variables.StackElement;
import pufferenco.variables.Variable;

import java.util.ArrayList;
import java.util.HashMap;

public class Function {
    private static final HashMap<String, ArrayList<Function>> Functions = new HashMap<>();
    public static AssemblyBuilder FunctionBuilder = new AssemblyBuilder();
    public static boolean exists(String name){
        return Functions.containsKey(name);
    }

    public static void read(TokenStream stream, AssemblyBuilder builder){
        Token name_token = stream.read();
        if(name_token.type != Token.TokenTypes.IDENTIFIER)
            builder.error("expected function name after fun keyword");
        String name = name_token.content;

        Token params_token = stream.read();
        if(params_token.type != Token.TokenTypes.ROUND_BRACKETS)
            builder.error("expected round brackets after fun name");

        ArrayList<ArrayList<Token>> param_tokens = new ArrayList<>();
        ArrayList<Token> current_param_token = new ArrayList<>();
        TokenStream param_stream = new TokenStream(Token.tokenize(params_token.content), builder);
        while(param_stream.isNotEmpty()){
            Token current_token = param_stream.read();
            if(current_token.type == Token.TokenTypes.CONTROL && current_token.content.equals(",")){
                param_tokens.add(current_param_token);
                current_param_token = new ArrayList<>();
                continue;
            }
            current_param_token.add(current_token);
        }
        param_tokens.add(current_param_token);

        ArrayList<Parameter> parameters = new ArrayList<>();
        for (ArrayList<Token> param : param_tokens) {
            System.out.println(param);
            if(param.size() != 3)
                builder.error("invalid function parameter: should be <name>: <type>");

            Token param_name = param.get(0);
            if(!param.get(1).content.equals(":"))
                builder.error("invalid function parameter: should be <name>: <type>");

            Token param_type = param.get(2);
            int type = ArrayUtil.inArray(param_type.content, DataType.NAMES);
            if(type == -1)
                builder.error("invalid function parameter: should be <name>: <type>");
            parameters.add(new Parameter(param_name.content, DataType.getInstance(type)));
        }

        int return_type = DataType.NULL;
        if(stream.read().content.equals("->")){
            return_type = ArrayUtil.inArray(stream.read(), DataType.NAMES);
            if(return_type == -1)
                builder.error("invalid return type");
        }else
            stream.backSpace();

        Token code_block = stream.read();
        if(code_block.type!= Token.TokenTypes.CURLY_BRACKETS)
            builder.error("expected code block after function declaration");

        Function function = new Function(name, parameters, return_type, code_block);
    }



    ArrayList<Parameter> parameters;
    String name;
    int return_type;
    String assembly_name;
    DataStack stack = new DataStack();

    Function(String name, ArrayList<Parameter> parameters, int return_type, Token code_block){
        this.name = name;
        this.parameters = parameters;
        this.return_type = return_type;
        this.assembly_name = name + "_" + Main.getId();

        FunctionBuilder.add_func(name);
        Main.VariableStacks.push(stack);
        FunctionBuilder.append_tag(assembly_name);

        for (Parameter parameter : parameters) {
            Variable.Variables.get(Variable.Variables.size()-1).put(parameter.name, new StackElement(parameter.name, parameter.type.getId()));
        }
        Main.tokenizeAndRun(code_block.content, FunctionBuilder);
        Main.VariableStacks.pop();

        FunctionBuilder.remove_func();
    }
}
