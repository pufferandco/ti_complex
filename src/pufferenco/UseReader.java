package pufferenco;

import java.io.File;

import static pufferenco.Main.Constants;
import static pufferenco.Main.root_folder;

public class UseReader {

    static void read(TokenStream stream, AssemblyBuilder builder, boolean is_assembly) {
        Token file_token = stream.read();
        if(file_token.type != Token.TokenTypes.CURLY_BRACKETS)
            builder.error("no file name following use statement");

        String file_name = file_token.content.replace(".", "/") + (is_assembly? ".asm" : ".TIC");

        if(!new File(root_folder + file_name).exists())
            builder.error("file not found: " + file_name);

        if(is_assembly)
            Constants.append(AssemblyLine.customAssemblyLine("#include \"" + root_folder + file_name + "\""));
        else
            Main.get_and_run(file_name, builder);

    }

}
