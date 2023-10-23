package pufferenco;

public class AssemblyLine {
    public static AssemblyLine customAssemblyLine(String custom) {
        AssemblyLine assemblyLine = new AssemblyLine();
        assemblyLine.Custom = custom;
        return assemblyLine;
    }



    public String Opcode;
    public String Param1;
    public String Param2;
    public String Custom;
    private String comment;



    public AssemblyLine() {}

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

    public void addComment(String comment) {
        this.comment = comment;
    }


    @Override
    public String toString() {
        if (Custom != null)
            return Custom;
        String to_return = "\t" + Opcode + ' ' + " ".repeat(12 - Opcode.length()) + ((Param1 != null) ? Param1 : "") + ((Param2 != null) ? "," + Param2 : "");
        if(comment != null) {
            int spacing = 40 - to_return.length();
            if(spacing < 0)
                spacing = 0;
            return to_return + " ".repeat(spacing) + "; " + comment;
        }
        return to_return;
    }
}
