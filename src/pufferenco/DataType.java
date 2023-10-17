package pufferenco;

import pufferenco.variables.DataStack;
import pufferenco.variables.StackElement;
import pufferenco.variables.types.BooleanType;
import pufferenco.variables.types.ByteType;
import pufferenco.variables.types.IntType;
import pufferenco.variables.types.StringType;

public interface DataType {
    String[] NAMES = {"byte", "int", "string", "null", "bool"};
    DataType[] TYPES = {new ByteType(), new IntType(), new StringType(), null, new BooleanType()};
    int BYTE = 0;
    int INT = 1;
    int STRING = 2;
    int NULL = 3;
    int BOOL = 4;

    static DataType getInstance(int type){
        return TYPES[type];
    }
    static void staticInit(){
        for (DataType dataType : TYPES) {
            if(dataType == null)
                continue;
            dataType.init();
        }
    }


    StackElement initStackVariable(StackElement value, DataStack stack, AssemblyBuilder builder);


    StackElement getStackVariable(StackElement element, AssemblyBuilder builder);

    StackElement convertFrom(StackElement old, AssemblyBuilder builder, boolean keep_constant);

    StackElement callOperator(String operator, AssemblyBuilder builder, StackElement right);
    void init();

    void setValue(StackElement variable,StackElement newValue, AssemblyBuilder builder);

}
