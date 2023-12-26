package pufferenco.variables;

import pufferenco.DataType;
import pufferenco.Main;

public class StackElement {
    public String name;
    public final int size;
    private int type;
    private String location = null;
    public boolean is_constant = false;
    public Object Constant_value;
    public DataStack stack;
    private int array_type = -1;

    public StackElement(String name, int size, int type, String location) {
        this.name = name;
        this.size = size;
        this.setType(type);
        this.setLocation(location);
        this.stack = Main.VariableStacks.peek();
    }

    public StackElement(String name, int size, int type) {
        this.name = name;
        this.size = size;
        this.setType(type);
        this.stack = Main.VariableStacks.peek();
    }

    public StackElement(String name, int type) {
        this.name = name;
        this.size = 1;
        this.setType(type);
        this.stack = Main.VariableStacks.peek();
    }

    public StackElement(Object constant_value, int type, String name) {
        this.name = name;
        this.size = 1;
        this.setType(type);
        this.is_constant = true;
        this.Constant_value = constant_value;
        this.stack = Main.VariableStacks.peek();
    }

    public static StackElement array(String name, int type){
        StackElement element = new StackElement(name, DataType.ARRAY);
        element.setArray_type(type);
        return element;

    }

    @Override
    public String toString() {
        if(getType() == DataType.ARRAY){
            return "StackElement{" +
                    "name='" + name + '\'' +
                    ", size=" + size +
                    ", type=array" +
                    ", location=" + getLocation() +
                    ", array_type=" + DataType.NAMES[getArray_type()] +
                    '}';
        }
        return "StackElement{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", type=" + DataType.NAMES[getType()] +
                ", location=" + getLocation() +
                '}';
    }

    @Deprecated
    public StackElement duplicate(){
        StackElement element = new StackElement(name, size, getType());
        element.setLocation(getLocation());
        element.is_constant = is_constant;
        element.Constant_value = Constant_value;
        element.stack = stack;
        element.setArray_type(getArray_type());
        return element;
    }

    public StackElement retrieve(){
        StackElement element = new StackElement(name, size, getType());
        element.setLocation(null);
        element.is_constant = is_constant;
        element.Constant_value = Constant_value;
        element.stack = stack;
        element.setArray_type(getArray_type());
        return element;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getArray_type() {
        return array_type;
    }

    public void setArray_type(int array_type) {
        this.array_type = array_type;
    }
}