package pufferenco;

import pufferenco.optimization.AssemblyCollapse;
import pufferenco.variables.DataStack;

import java.util.List;

import static pufferenco.AssemblyLine.customAssemblyLine;

public class Main {

    public static AssemblyBuilder Constants = new AssemblyBuilder();
    public static DataStack Data_stack = new DataStack();
    final static int OPTIMIZE_LEVEL = 1;
    public static void main(String[] args) {
        AssemblyBuilder builder = new AssemblyBuilder();
        init();


        builder.append(customAssemblyLine("#include \"bin/asm/include.inc\""));
        builder.append(customAssemblyLine(".assume\tADL=1"));
        builder.append(customAssemblyLine(DataStack.StackStart + " .equ saveSScreen+768"));
        builder.append(customAssemblyLine(".org\tuserMem-2"));
        builder.append_db("tExtTok,tAsm84CeCmp");
        builder.append((new AssemblyLine("")));
        builder.append_ld("(StackSave)", "SP");
        builder.append_ld("SP", DataStack.StackStart);
        builder.append_call("init");
        builder.append_call("_homeup");
        builder.append_call("_ClrScrnFull");
        builder.append((new AssemblyLine("")));
        builder.append((new AssemblyLine("")));

        String code = IOUtil.readTxt("root/main.TIC");
        List<List<Token>> tokenLines = Token.tokenizeLines(code);
        for (List<Token> tokens : tokenLines) {
            builder.tIC_line++;
            System.out.println(tokens);
            globalReader.read(tokens, builder);
            builder.append((new AssemblyLine("")));
        }

        builder.append_tag("ProgramExit");
        builder.append_call("_GetKey");
        builder.append_call("_ClrScrnFull");
        builder.append_res("donePrgm","(iy+doneFlags)");
        builder.append_ld("SP", "(StackSave)");
        builder.append_ret();
        builder.append(customAssemblyLine("#include \"bin/asm/api.asm\""));
        builder.append_tag("StackSave");
        builder.append_db("0,0,0");

        IOUtil.writeTxt("bin/asm/main.asm", AssemblyCollapse.optimize(builder, OPTIMIZE_LEVEL).getAssembly() + "\n" + Constants.getAssembly());
    }

    private static void init(){
        NativeFunction.init();
        DataType.staticInit();
    }

    private static long id = 0;
    public static String getId(){
        return String.valueOf(id++);
    }
}