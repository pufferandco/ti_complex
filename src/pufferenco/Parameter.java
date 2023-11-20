package pufferenco;

public class Parameter {
    public String name;
    public DataType type;
    public DataType subtype;

    public Parameter(String name, DataType type) {
        this.name = name;
        this.type = type;
    }

    public static Parameter ArrayParameter(String name, DataType subtype) {
        Parameter parameter = new Parameter(name, DataType.getInstance(DataType.ARRAY));
        parameter.subtype = subtype;

        return parameter;
    }
}
