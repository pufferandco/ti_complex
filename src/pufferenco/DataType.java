package pufferenco;

import pufferenco.variables.DataStack;
import pufferenco.variables.StackElement;
import pufferenco.variables.types.*;

public interface DataType {
    String[] NAMES = {"byte", "int", "string", "null", "bool", "pointer", "array", "type"};
    DataType[] TYPES = {new ByteType(), new IntType(), new StringType(), null, new BooleanType(), new PointerType(), new ArrayType(), null};
    int BYTE = 0;
    int INT = 1;
    int STRING = 2;
    int NULL = 3;
    int BOOL = 4;
    int POINTER = 5;
    int ARRAY = 6;
    int TYPE = 7;

    static DataType getInstance(int type) {
        if(type < 0 || type >= TYPES.length)
            return TYPES[NULL];
        return TYPES[type];
    }

    static String getName(int type) {
        if(type < 0 || type >= TYPES.length)
            return NAMES[NULL];
        return NAMES[type];
    }

    static void staticInit() {
        for (DataType dataType : TYPES) {
            if (dataType == null)
                continue;
            dataType.init();
        }
    }


    StackElement initVariable(StackElement value, DataStack stack, AssemblyBuilder builder);


    StackElement getStackVariable(StackElement element, AssemblyBuilder builder);

    StackElement convertFrom(StackElement old, AssemblyBuilder builder, boolean keep_constant);

    StackElement callOperator(String operator, AssemblyBuilder builder, StackElement right);

    void init();

    void setStackVariable(StackElement variable, StackElement newValue, AssemblyBuilder builder);

    int getId();
    StackElement getSub(StackElement element, StackElement key, AssemblyBuilder builder);
    void setSub(StackElement element, StackElement key, StackElement new_value, AssemblyBuilder builder);

    int elementSize(AssemblyBuilder builder);

    StackElement getAt(StackElement pointer, AssemblyBuilder builder);

    void setAt(StackElement pointer, StackElement value, AssemblyBuilder builder);

    StackElement initGlobal(StackElement element, AssemblyBuilder builder);
    StackElement getStatic(AssemblyBuilder builder, String location);

    void setStatic(String name, StackElement value, AssemblyBuilder builder);

}
