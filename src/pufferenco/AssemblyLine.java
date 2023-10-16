package pufferenco;

public class AssemblyLine {
    public String Opcode;
    public String Param1;
    public String Param2;
    public String Custom;
    public AssemblyLine() {
    }
    public AssemblyLine(String opcode) {
        this.Opcode = opcode;
    }

    public AssemblyLine(String opcode, String param1) {
        this.Opcode = opcode;
        this.Param1 = param1;
    }


    public AssemblyLine(String opcode, String param1, String param2) {
        this.Opcode = opcode;
        this.Param1 = param1;
        this.Param2 = param2;
    }
    public static AssemblyLine customAssemblyLine(String custom){
        AssemblyLine assemblyLine = new AssemblyLine();
        assemblyLine.Custom = custom;
        return assemblyLine;
    }

    @Override
    public String toString() {
        if(Custom != null)
            return Custom;
        return "\t" + Opcode + ' ' + " ".repeat(12- Opcode.length()) + ((Param1 !=null)? Param1 :"") + ((Param2 !=null)?","+ Param2 :"");
    }
}
