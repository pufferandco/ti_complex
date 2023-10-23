package pufferenco.variables;

import pufferenco.*;

import java.util.ArrayList;
import java.util.HashMap;

import static pufferenco.Main.Call_stack;

public class Variable {
    public static ArrayList<HashMap<String, StackElement>> Variables = new ArrayList<>();
    public static void init(TokenStream stream, AssemblyBuilder builder){
        Token name = stream.read();
        if(name.type != Token.TokenTypes.IDENTIFIER)
            builder.error("token after [val] must be an identifier");

        if(!stream.read().content.equals(":"))
            builder.error("no required token [:] after name of value");

        Token typeIdentifier = stream.read();
        if(typeIdentifier.type != Token.TokenTypes.IDENTIFIER || !ArrayUtil.isInArray(typeIdentifier.content, DataType.NAMES)){
            builder.error("unknown token after [:]");
            return;
        }

        if(!stream.read().content.equals("="))
            builder.error("no required token [=] after type of value");

        int dataId = ArrayUtil.inArray(typeIdentifier.content, DataType.NAMES);
        if(dataId == -1)
            builder.error("no type exists with the name [" + typeIdentifier.content);



        DataType dataType = DataType.TYPES[dataId];
        Variables.get(Variables.size()-1).put(name.content, dataType.initStackVariable(
                ExpressionReader.evalExpression(stream, builder, false),
                Main.VariableStacks.peek(),
                builder
        ));
    }

    public static void increase_scope(AssemblyBuilder builder){
        Call_stack.push(new StackElement("stack_save_" + Main.getId(), DataType.POINTER), builder);
        Variables.add(new HashMap<>());
    }

    public static void decrease_scope(){
        Variables.remove(Variables.size()-1);
        Call_stack.pop();
    }

    public static boolean exists(String name){
        for (int i = Variables.size() - 1; i >= 0; i--) {
            HashMap<String, StackElement> current_scope = Variables.get(i);
            if(current_scope.containsKey(name)){
                return true;
            }
        }
        return false;
    }

    public static StackElement get(String name, AssemblyBuilder builder){
        for (int i = Variables.size() - 1; i >= 0; i--) {
            HashMap<String, StackElement> current_scope = Variables.get(i);
            if(current_scope.containsKey(name)){
                StackElement element = current_scope.get(name);
                return DataType.getInstance(element.type).getStackVariable(element,builder);
            }
        }
        builder.error("variable [" + name + "] does not exist");
        throw new RuntimeException("variable [" + name + "] does not exist");
    }

    public static void set(String name, AssemblyBuilder builder, TokenStream stream){

        for (int i = Variables.size() - 1; i >= 0; i--) {
            HashMap<String, StackElement> current_scope = Variables.get(i);
            if(current_scope.containsKey(name)){
                StackElement element = current_scope.get(name);
                if(!stream.read().content.equals("="))
                    builder.error("no required token [=] after global mention of variable");

                DataType.getInstance(element.type).setValue(
                        element,
                        ExpressionReader.evalExpression(stream, builder, false),
                        builder
                );
                return;
            }
        }
    }

    public Variable(String name, int size, int type){
        Variables.get(Variables.size()-1).put(name, new StackElement(name, size));
    }
}
