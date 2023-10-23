package pufferenco.variables;

import java.util.HashMap;

public class RegisterManager {
    private static final HashMap<Character, Register> Registers = new HashMap<>();

    public static void init() {
        Registers.put('A', new Register('F', "AF"));
        Registers.put('F', new Register('A', "AF"));

        Registers.put('B', new Register('C', "BC"));
        Registers.put('C', new Register('B', "BC"));

        Registers.put('D', new Register('E', "DE"));
        Registers.put('E', new Register('D', "DE"));

        Registers.put('H', new Register('L', "HL"));
        Registers.put('L', new Register('H', "HL"));
    }

    public static void setValue(String register, String content) {
        char[] singulars = register.toCharArray();

        char reg_id1 = singulars[0];
        Register register1 = Registers.get(reg_id1);
        register1.Content = content;
        if (register1.Is_dual) {
            Registers.get(register1.Pair).Content = "";
            register1.Is_dual = false;
        }

        if (singulars.length == 1)
            return;

        char reg_id2 = singulars[0];
        Register register2 = Registers.get(reg_id2);
        register2.Content = content;

        register1.Is_dual = true;
        register2.Is_dual = true;
    }

    public static boolean isRegister(String register) {
        char[] singulars = register.toCharArray();

        if (singulars.length == 0 || singulars.length > 2)
            return false;

        return Registers.containsKey(singulars[0]) && Registers.containsKey(singulars[1]);
    }

    public static void clearValue(String... registers) {
        for (String register : registers) {
            char[] singulars = register.toCharArray();

            char reg_id1 = singulars[0];
            Register register1 = Registers.get(reg_id1);
            register1.Content = "";
            if (register1.Is_dual) {
                Registers.get(register1.Pair).Content = "";
                register1.Is_dual = false;
            }

            if (singulars.length == 1)
                return;

            char reg_id2 = singulars[0];
            Register register2 = Registers.get(reg_id2);
            register2.Content = "";

            register2.Is_dual = false;
        }
    }

    public static String getValue(String content) {
        String[] register_return = new String[1];

        Registers.forEach((character, register) -> {
            if (!register.Content.equals(content))
                return;

            if (register.Is_dual)
                register_return[0] = register.Paired;
            else
                register_return[0] = character.toString();
        });

        return register_return[0];
    }

    private static class Register {
        final String Paired;
        final char Pair;
        boolean Is_dual = false;
        String Content = "";

        Register(char pair, String paired) {
            this.Pair = pair;
            this.Paired = paired;
        }
    }
}
