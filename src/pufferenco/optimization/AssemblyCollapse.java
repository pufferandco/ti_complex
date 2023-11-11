package pufferenco.optimization;

import pufferenco.AssemblyBuilder;
import pufferenco.AssemblyLine;

import java.util.Collections;

public class AssemblyCollapse {
    public static AssemblyBuilder optimize(AssemblyBuilder builder, int level) {
        if (level > 0) {
            //same_double_stack(builder);
        }
        return builder;
    }

    private static void same_double_stack(AssemblyBuilder builder) {
        for (int i = 1; i < builder.lines.size(); i++) {
            AssemblyLine last_line = builder.lines.get(i - 1);
            AssemblyLine current_line = builder.lines.get(i);

            if (last_line == null || current_line == null)
                continue;

            if (!("push".equals(last_line.Opcode) && "pop".equals(current_line.Opcode) &&
                    last_line.Param1.equals(current_line.Param1)))
                continue;

            builder.lines.set(i, null);
            builder.lines.set(i - 1, null);
        }
        builder.lines.removeAll(Collections.singleton(null));
    }
}
