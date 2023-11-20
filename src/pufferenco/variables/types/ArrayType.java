package pufferenco.variables.types;

import pufferenco.ArrayUtil;
import pufferenco.AssemblyBuilder;
import pufferenco.DataType;
import pufferenco.Main;
import pufferenco.variables.DataStack;
import pufferenco.variables.StackElement;
import pufferenco.variables.Variable;

public class ArrayType implements DataType {
    private static final int Data_type_id = 6;
    @Override
    public StackElement initVariable(StackElement value, DataStack stack, AssemblyBuilder builder) {
        value.name = "var_array_" + Main.getId();
        return stack.push(convertFrom(value, builder, false), builder);
    }

    @Override
    public StackElement getStackVariable(StackElement element, AssemblyBuilder builder) {
        builder.append_ld("HL", "(" + element.location + ")");
        builder.append_push("HL");
        return StackElement.array("var_array_" + Main.getId(), element.array_type);
    }

    @Override
    public StackElement convertFrom(StackElement old, AssemblyBuilder builder, boolean keep_constant) {
        if(old.type == DataType.POINTER)
            return new StackElement("converted_array", getId());
        if(old.type != Data_type_id)
            builder.error("cannot convert from non-array to array");
        return old;
    }

    @Override
    public StackElement callOperator(String operator, AssemblyBuilder builder, StackElement right) {
        builder.error("cannot call operator [" + operator + "] on array");
        return null;
    }

    @Override
    public void init() {

    }

    @Override
    public void setStackVariable(StackElement variable, StackElement newValue, AssemblyBuilder builder) {
        if(newValue.type != Data_type_id)
            builder.error("cannot set value to non-array");
        if(variable.array_type != newValue.array_type)
            builder.error("cannot set value to different array type");

        builder.append_pop("HL");
        builder.append_ld("(" + variable.location + ")", "HL");
    }

    @Override
    public int getId() {
        return 6;
    }

    @Override
    public StackElement getSub(StackElement element, StackElement key, AssemblyBuilder builder) {
        DataType.getInstance(DataType.INT).convertFrom(key, builder, false);

        int element_size = DataType.getInstance(element.array_type).elementSize(builder);

        builder.append_pop("HL");           // move array_pointer(STACK -> HL)
        builder.append_pop("BC");           // move index(STACK -> BC)

        if(Main.safety_mode) {
            builder.append_ld("DE", "0");     // move size(MEMORY -> DE)
            builder.append_ld("D", "(HL)");   // elements_start(HL) = array_pointer + 2; void array_pointer
            builder.append_inc("HL");
            builder.append_ld("E", "(HL)");
            builder.append_inc("HL");

            builder.append_ex("DE", "HL");
            builder.append_or("A", "A");
            builder.append_sbc("HL", "BC");
            builder.append_add("HL", "BC");
            builder.append_jp("C", "index_out_of_bounds");
            builder.append_ex("DE", "HL");
        }else{
            builder.append_inc("HL");
            builder.append_inc("HL");
        }

        if(element_size != 1) {
            builder.append_push("HL");
            builder.append_push("BC");      // move index(BC -> HL)
            builder.append_pop("HL");

            builder.append_ld("BC", String.valueOf(DataType.getInstance(element.type).elementSize(builder)));
            builder.append_call("multiply_int");
            builder.append_ex("DE", "HL");
            builder.append_pop("HL");
            builder.append_add("HL", "DE");
        }else{
            builder.append_add("HL", "BC");
        }
        builder.append_push("HL");

        return DataType.getInstance(element.array_type).getAt(new StackElement("index_pointer", DataType.POINTER), builder);
    }

    @Override
    public void setSub(StackElement element, StackElement key, StackElement new_value, AssemblyBuilder builder) {
        int element_size = DataType.getInstance(element.array_type).elementSize(builder);
        DataType.getInstance(DataType.INT).convertFrom(key, builder, false);
        builder.append_ld("HL", "(" + element.location + ")");
        builder.append_pop("BC");           // move index(STACK -> BC)

        if(Main.safety_mode) {
            builder.append_ld("DE", "0");     // move size(MEMORY -> DE)
            builder.append_ld("D", "(HL)");   // elements_start(HL) = array_pointer + 2; void array_pointer
            builder.append_inc("HL");
            builder.append_ld("E", "(HL)");
            builder.append_inc("HL");

            builder.append_ex("DE", "HL");
            builder.append_or("A", "A");
            builder.append_sbc("HL", "BC");
            builder.append_add("HL", "BC");
            builder.append_jp("C", "index_out_of_bounds");
            builder.append_ex("DE", "HL");
        }else{
            builder.append_inc("HL");
            builder.append_inc("HL");
        }

        if(element_size != 1) {
            builder.append_push("HL");
            builder.append_push("BC");      // move index(BC -> HL)
            builder.append_pop("HL");

            builder.append_ld("BC", String.valueOf(DataType.getInstance(element.type).elementSize(builder)));
            builder.append_call("multiply_int");
            builder.append_ex("DE", "HL");
            builder.append_pop("HL");
            builder.append_add("HL", "DE");
        }else{
            builder.append_add("HL", "BC");
        }
        builder.append_push("HL");
        DataType.getInstance(element.array_type).setAt(new StackElement("index_pointer", DataType.POINTER), new_value, builder);
    }

    @Override
    public int elementSize(AssemblyBuilder builder) {
        builder.error("array element size is not supported");
        return 0;
    }

    @Override
    public StackElement getAt(StackElement pointer, AssemblyBuilder builder) {
        builder.error("array element is not supported");
        return null;
    }

    @Override
    public void setAt(StackElement pointer, StackElement value, AssemblyBuilder builder) {
        builder.error("array element is not supported");
    }

    @Override
    public StackElement initGlobal(StackElement element, AssemblyBuilder builder) {
        element = convertFrom(element, builder, false);
        builder.append_pop("HL");
        element.location = "globalVars+" + (Variable.GlobalOffset);
        builder.append_ld("("+element.location+")", "HL");
        Variable.GlobalOffset += 3;
        return element;
    }

    @Override
    public StackElement getStatic(AssemblyBuilder builder, String location) {
        builder.append_ld("HL", "("+location+")");
        builder.append_push("HL");
        return null;
    }

    @Override
    public void setStatic(String location, StackElement value, AssemblyBuilder builder) {
        convertFrom(value, builder, false);
        builder.append_pop("HL");
        builder.append_ld( "("+location+")", "HL");
    }

    public static StackElement createNew(int type, StackElement size, StackElement pointer, AssemblyBuilder builder){

        builder.append_pop("HL");
        builder.append_pop("DE");

        builder.append_ld("(HL)", "D");
        builder.append_inc("HL");
        builder.append_ld("(HL)", "E");

        builder.append_dec("HL");
        builder.append_push("HL");

        return StackElement.array("new array", type);
    }
}
