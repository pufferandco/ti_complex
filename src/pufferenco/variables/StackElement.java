package pufferenco.variables;

public class StackElement{
    public String name;
    public final int size;
    public final int type;
    public int location;
    public boolean is_constant = false;
    public Object Constant_value;
    public HeapElement Heap_element;

    StackElement(String name, int size, int type, int location) {
        this.name = name;
        this.size = size;
        this.type = type;
        this.location = location;
    }

    public StackElement(String name, int size, int type) {
        this.name = name;
        this.size = size;
        this.type = type;
    }

    public StackElement(String name, int type) {
        this.name = name;
        this.size = 1;
        this.type = type;
    }

    public StackElement(String name, int type, Object constant_value) {
        this.name = name;
        this.size = 1;
        this.type = type;
        this.is_constant = true;
        this.Constant_value = constant_value;
    }
}