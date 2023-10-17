package pufferenco;

import pufferenco.variables.RegisterManager;

import java.util.LinkedList;
import java.util.List;

import static pufferenco.AssemblyLine.customAssemblyLine;

public class AssemblyBuilder {
    public LinkedList<AssemblyLine> lines = new LinkedList<>();
    private boolean locked = false;

    public int tIC_line = 0;

    public void append(AssemblyLine line){
        if(locked)
            error("attempted to write to locked builder");

        lines.add(line);
    }

    public void appendList(List<AssemblyLine> list){
        if(locked)
            error("attempted to write to locked builder");

        lines.addAll(list);
    }

    public String getAssembly(){
        locked = true;
        StringBuilder builder = new StringBuilder();
        for (AssemblyLine line : lines) {
            builder.append(line).append('\n');
        }
        return builder.toString();
    }

    public void error(String error_code){
        throw new RuntimeException("error at line " + tIC_line + ": " + error_code);
    }

    //public void append_ld(String left, String right, String name){
    //    append(new AssemblyLine("ld", left, right));
    //    if(RegisterManager.isRegister(left))
    //        RegisterManager.setValue(left, name);
    //}
    public void append_ld(String left, String right){
        append(new AssemblyLine("ld", left, right));
        if(RegisterManager.isRegister(left))
            RegisterManager.clearValue(left);
    }
    public void append_db(String content){
        append(new AssemblyLine(".db", content));
    }
    public void append_tag(String name){
        append(customAssemblyLine(name+":"));
    }
    public void append_push(String register){append(new AssemblyLine("push", register));}
    public void append_pop(String register){
        append(new AssemblyLine("pop", register));
    }
    //public void append_pop(String register, String name){
    //    append(new AssemblyLine("pop", register));
    //}
    public void append_call(String pointer){append(new AssemblyLine("call", pointer));}
    public void append_ret(){append(new AssemblyLine("ret"));}
    public void append_res(String left, String right){append(new AssemblyLine("res", left, right));}
    public void append_add(String left, String right){append(new AssemblyLine("add", left, right));}
    public void append_sub(String left, String right){append(new AssemblyLine("sub", left, right));}
    public void append_sbc(String left, String right){append(new AssemblyLine("sbc", left, right));}
    public void append_ex(String left, String right){append(new AssemblyLine("ex", left, right));}


    public void append_mlt(String rr){append(new AssemblyLine("mlt", rr));}
    public void append_and(String operand1, String operand2){append(new AssemblyLine("and", operand1, operand2));}
    public void append_or(String operand1, String operand2){append(new AssemblyLine("or", operand1, operand2));}
    public void append_xor(String operand1, String operand2){append(new AssemblyLine("xor", operand1, operand2));}

    public void append_jp(String statement, String pointer){append(new AssemblyLine("jp", statement, pointer));}
    public void append_jp(String pointer){append(new AssemblyLine("jp", pointer));}
    public void append_cp(String left, String right){append(new AssemblyLine("cp", left, right));}


}
