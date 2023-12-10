package pufferenco.variables;

import pufferenco.DataType;
import pufferenco.Main;

public class StackElement {
    public String name;
    public final int size;
    public int type;
    public String location = null;
    public boolean is_constant = false;
    public Object Constant_value;
    public DataStack stack;
    public int array_type = -1;

    public StackElement(String name, int size, int type, String location) {
        this.name = name;
        this.size = size;
        this.type = type;
        this.location = location;
        this.stack = Main.VariableStacks.peek();
    }

    public StackElement(String name, int size, int type) {
        this.name = name;
        this.size = size;
        this.type = type;
        this.stack = Main.VariableStacks.peek();
    }

    public StackElement(String name, int type) {
        this.name = name;
        this.size = 1;
        this.type = type;
        this.stack = Main.VariableStacks.peek();
    }

    public StackElement(Object constant_value, int type, String name) {
        this.name = name;
        this.size = 1;
        this.type = type;
        this.is_constant = true;
        this.Constant_value = constant_value;
        this.stack = Main.VariableStacks.peek();
    }

    public static StackElement array(String name, int type){
        StackElement element = new StackElement(name, DataType.ARRAY);
        element.array_type = type;
        return element;

    }

    @Override
    public String toString() {
        if(type == DataType.ARRAY){
            return "StackElement{" +
                    "name='" + name + '\'' +
                    ", size=" + size +
                    ", type=array" +
                    ", location=" + location +
                    ", array_type=" + DataType.NAMES[array_type] +
                    '}';
        }
        return "StackElement{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", type=" + DataType.NAMES[type] +
                ", location=" + location +
                '}';
    }

    @Deprecated
    public StackElement duplicate(){
        StackElement element = new StackElement(name, size, type);
        element.location = location;
        element.is_constant = is_constant;
        element.Constant_value = Constant_value;
        element.stack = stack;
        element.array_type = array_type;
        return element;
    }

    public StackElement retrieve(){
        StackElement element = new StackElement(name, size, type);
        element.location = null;
        element.is_constant = is_constant;
        element.Constant_value = Constant_value;
        element.stack = stack;
        element.array_type = array_type;
        return element;
    }
}