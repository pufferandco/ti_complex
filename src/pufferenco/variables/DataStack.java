package pufferenco.variables;

import pufferenco.AssemblyBuilder;
import pufferenco.DataType;

import java.util.LinkedList;

public class DataStack {
    private final LinkedList<StackElement> stack = new LinkedList<>();
    private int size = 0;
    public static final String StackStart = "stackStart";

    public StackElement push(String name, int size, int type, AssemblyBuilder builder){
        return push(new StackElement(name, size, type, this.size), builder);
    }
    public StackElement push(String name, int type, AssemblyBuilder builder){
        return push(name, 1, type, builder);
    }
    public StackElement push(StackElement element, AssemblyBuilder builder){
        if(element.is_constant)
            element = DataType.getInstance(element.type).convertFrom(element, builder, false);
        element.location = size;
        stack.add(element);
        size += element.size * 3;
        return element;
    }

    public void rename(String old_name, String new_name){
        stack.forEach(stackElement -> {
            if(stackElement.name.equals(old_name))
                stackElement.name = new_name;
        });
    }

    public StackElement pop(){
        StackElement popped = stack.pop();
        size -= popped.size;
        return popped;
    }

    public StackElement get(String name){
        for (StackElement stackElement : stack) {
            if(name.equals(stackElement.name))
                return stackElement;
        }
        throw new RuntimeException("no variable exists with the name: " + name);
    }
}