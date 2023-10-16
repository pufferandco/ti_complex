package pufferenco.variables;

public class HeapElement {
    public final int type;
    protected boolean is_modified = false;

    public HeapElement(int type){
        this.type = type;
    }
}
