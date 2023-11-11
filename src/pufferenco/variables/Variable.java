package pufferenco.variables;

import pufferenco.*;
import pufferenco.variables.types.ArrayType;

import java.util.ArrayList;
import java.util.HashMap;

import static pufferenco.Main.Call_stack;

public class Variable {
    public static ArrayList<HashMap<String, Variable>> Variables = new ArrayList<>();
    public static HashMap<String, Variable> Globals = new HashMap<>();
    public static int GlobalOffset = 0;

    public static void init(TokenStream stream, AssemblyBuilder builder) {
        Token name = stream.read();

        if (name.type != Token.TokenTypes.IDENTIFIER)
            builder.error("token after [val] must be an identifier");

        if (!stream.read().content.equals(":"))
            builder.error("no required token [:] after name of value");


        Token type_identifier = stream.read();
        boolean is_global = type_identifier.type == Token.TokenTypes.GLOBAL;
        if(is_global)
            type_identifier = stream.read();

        if (type_identifier.type != Token.TokenTypes.IDENTIFIER || !ArrayUtil.isInArray(type_identifier.content, DataType.NAMES)) {
            builder.error("unknown token after [:]");
            return;
        }

        int dataId = ArrayUtil.inArray(type_identifier.content, DataType.NAMES);
        if (dataId == -1)
            builder.error("no type exists with the name [" + type_identifier.content);

        DataType data_type = DataType.TYPES[dataId];

        Token is_array = stream.read();

        if (is_array.type == Token.TokenTypes.SQUARE_BRACKETS){
            if(!is_array.content.isBlank())
                builder.error("array must be empty");

            if (!stream.read().content.equals("="))
                builder.error("no required token [=] after type of value");

            StackElement expr = ExpressionReader.evalExpression(stream, builder, false);
            if(expr.array_type != dataId)
                builder.error("type of value is not the same as the type of the array");

            if(is_global)
                createGlobal(name.content, expr, builder);
            else
                new Variable(name.content, new ArrayType().initVariable(
                        expr,
                        Main.VariableStacks.peek(),
                        builder
                ));

            return;
        }

        if (!is_array.content.equals("="))
            builder.error("no required token [=] after type of value");

        StackElement expr = ExpressionReader.evalExpression(stream, builder, false);

        if(is_global)
            createGlobal(name.content, expr, builder);
        else
            new Variable(name.content, data_type.initVariable(
                    expr,
                    Main.VariableStacks.peek(),
                    builder
            ));

    }


    public static void increase_scope(AssemblyBuilder builder) {
        Call_stack.push(new StackElement("stack_save_" + Main.getId(), DataType.POINTER), builder);
        Variables.add(new HashMap<>());
    }

    public static void decrease_scope() {
        Variables.remove(Variables.size() - 1);
        Call_stack.pop();
    }

    public static boolean exists(String name) {
        for (int i = Variables.size() - 1; i >= 0; i--) {
            HashMap<String, Variable> current_scope = Variables.get(i);
            if (current_scope.containsKey(name)) {
                return true;
            }
        }
        return Globals.containsKey(name);
    }

    public static StackElement get(String name, AssemblyBuilder builder) {
        for (int i = Variables.size() - 1; i >= 0; i--) {
            HashMap<String, Variable> current_scope = Variables.get(i);
            if (current_scope.containsKey(name)) {
                StackElement element = current_scope.get(name).element;
                DataType.getInstance(element.type).getStackVariable(element, builder);
                return element;
            }
        }
        if(Globals.containsKey(name)) {
            StackElement element = Globals.get(name).element;

            return DataType.getInstance(element.type).get(builder, element.location);
        }

        builder.error("variable [" + name + "] does not exist");
        throw new RuntimeException("variable [" + name + "] does not exist");
    }

    public static void set(String name, AssemblyBuilder builder, TokenStream stream) {
        StackElement element = null;
        boolean is_global = false;
        for (int i = Variables.size() - 1; i >= 0; i--) {
            HashMap<String, Variable> current_scope = Variables.get(i);
            if (current_scope.containsKey(name)) {
                element = current_scope.get(name).element;
                break;
            }
        }
        if(element == null) {
            if(Globals.containsKey(name)) {
                is_global = true;
                element = Globals.get(name).element;
            }else {
                builder.error("no such variable " + name);
                throw new RuntimeException();
            }
        }

        Token next_token = stream.read();
        if(next_token.type == Token.TokenTypes.SQUARE_BRACKETS) {
            if (!stream.read().content.equals("="))
                builder.error("no required token [=] after global mention of variable sub_setter");

            StackElement value = ExpressionReader.evalExpression(stream, builder, false);
            StackElement key = ExpressionReader.evalExpression(new TokenStream(Token.tokenize(next_token.content), builder), builder, true);

            DataType.getInstance(element.type).setSub(
                    element,
                    key,
                    value,
                    builder
            );
            return;
        }
        if(is_global)
            DataType.getInstance(element.type).set(
                    element.location,
                    ExpressionReader.evalExpression(stream, builder, false),
                    builder
            );
        else
            DataType.getInstance(element.type).setStackVariable(
                    element,
                    ExpressionReader.evalExpression(stream, builder, false),
                    builder
            );

        return;

    }

    public StackElement element;
    public String name;
    public boolean is_global = false;

    public Variable(String name, StackElement element) {
        this.element = element;
        this.name = name;
        Variables.get(Variables.size() - 1).put(name, this);
    }
    public Variable() {}

    public static void createGlobal(String name, StackElement element, AssemblyBuilder builder) {
        Variable variable = new Variable();
        variable.name = name;
        variable.is_global = true;
        variable.element = DataType.getInstance(element.type).initGlobal(element, builder);

        Globals.put(name, variable);
    }
}
