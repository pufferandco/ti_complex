package pufferenco.variables.types;

import pufferenco.AssemblyBuilder;
import pufferenco.DataType;
import pufferenco.Main;
import pufferenco.variables.DataStack;
import pufferenco.variables.StackElement;

import java.util.HashMap;


public class StringType implements DataType {
    private final static int Data_type_id = 2;
    public static HashMap<String, String> strings = new HashMap<String, String>();

    @Override
    public StackElement initVariable(StackElement pointer, DataStack stack, AssemblyBuilder builder) {
        pointer.name = "var_string_" + Main.getId();
        return stack.push(convertFrom(pointer, builder, false), builder);
    }

    @Override
    public StackElement getStackVariable(StackElement element, AssemblyBuilder builder) {
        builder.append_ld("HL", "(" + element.getLocation() + ")");
        builder.append_push("HL");
        return new StackElement("var_read_string_" + Main.getId(), Data_type_id);
    }

    @Override
    public StackElement convertFrom(StackElement old, AssemblyBuilder builder, boolean keep_constant) {
        if(old.getType() == DataType.POINTER)
            return new StackElement("converted_pointer", getId());
        if (old.getType() != Data_type_id) {
            builder.error("cannot convert value to string");
            throw new RuntimeException();
        }
        return old;
    }

    @Override
    public StackElement callOperator(String operator, AssemblyBuilder builder, StackElement right) {
        builder.error("operator " + operator + " is not supported by ");
        return null;
    }

    @Override
    public void init() {
    }

    @Override
    public void setStackVariable(StackElement variable, StackElement newValue, AssemblyBuilder builder) {
        newValue = convertFrom(newValue, builder, false);

        builder.append_pop("HL");
        builder.append_ld("(" + variable.getLocation() + ")", "HL");

        newValue.setLocation(variable.getLocation());
    }

    @Override
    public int getId() {
        return 2;
    }

    @Override
    public StackElement getSub(StackElement element, StackElement key, AssemblyBuilder builder) {
        key = TYPES[BYTE].convertFrom(key, builder, false);

        builder.append_pop("HL");
        builder.append_pop("AF");
        builder.append_ld("DE", "0");
        builder.append_ld("E", "A");
        builder.append_add("HL", "DE");

        return DataType.getInstance(BYTE).getAt(new StackElement("index_pointer", DataType.POINTER), builder);
    }

    @Override
    public void setSub(StackElement element, StackElement key, StackElement new_value, AssemblyBuilder builder) {
        key = TYPES[BYTE].convertFrom(key, builder, false);
        new_value = TYPES[BYTE].convertFrom(new_value, builder, false);

        builder.append_pop("HL");
        builder.append_pop("AF");
        builder.append_ld("DE", "0");
        builder.append_ld("E", "A");
        builder.append_add("HL", "DE");

        DataType.getInstance(BYTE).setAt(new StackElement("index_pointer", DataType.POINTER), new_value, builder);
    }

    @Override
    public int elementSize(AssemblyBuilder builder) {
        return 3;
    }

    @Override
    public StackElement getAt(StackElement pointer, AssemblyBuilder builder) {
        builder.append_pop("HL");
        builder.append_ld("HL", "(HL)");
        builder.append_push("HL");
        return new StackElement("var_read_str_" + Main.getId(), getId());
    }

    @Override
    public void setAt(StackElement pointer, StackElement value, AssemblyBuilder builder) {
        builder.append_pop("HL");
        builder.append_pop("DE");
        builder.append_ld("(HL)", "DE");
    }

    @Override
    public StackElement initGlobal(StackElement element, AssemblyBuilder builder) {
        return null;
    }

    @Override
    public StackElement getStatic(AssemblyBuilder builder, String location) {
        return null;
    }

    @Override
    public void setStatic(String name, StackElement value, AssemblyBuilder builder) {

    }

}
