package pufferenco;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static pufferenco.ArrayUtil.inArray;
import static pufferenco.ArrayUtil.isInArray;

public class Token {
    public interface TokenTypes {
        int ARITHMETIC = 5;
        int CONTROL = 6;
        int IDENTIFIER = 7;
        int NEW_LINE = 8;
        int ROUND_BRACKETS = 0;
        int CURLY_BRACKETS = 1;
        int SQUARE_BRACKETS = 2;
        int DOUBLE_QUOTE = 3;
        int QUOTE = 4;
        int FUN = 10;
        int WHILE = 11;
        int VAR = 12;
        int VAL = 13;
        int AND = 14;
        int OR = 15;
        int XOR = 16;
        int TRUE = 17;
        int FALSE = 18;
        int IF = 19;
        int ELSE = 20;
        int ELIF = 21;
    }

    private static final Pattern IDENTIFIER_TOKENS = Pattern.compile("[a-zA-Z0-9_]");
    private static final Character[] ARITHMETIC_TOKENS = {'+', '*', '-', '/', '=', '^', '<', '>', '%'};
    private static final Character[] CONTROL_TOKENS = {'!', ':', ';', '&', ',', '.'};
    private static final Character[] SUB_ENTER_TOKENS = {'(', '{', '[', '"', '\''};
    private static final Character[] SUB_LEAVE_TOKENS = {')', '}', ']', '"', '\''};

    private static final String[] KEYWORDS = {"fun", "while", "var", "val", "and", "or", "xor", "true", "false", "if", "else", "elif"};


    static ArrayList<Token> tokenize(String content) {
        char[] chars = content.toCharArray();
        int state = -1;

        StringBuilder token_builder = new StringBuilder();
        LinkedList<Token> tokens = new LinkedList<>();
        int nests = 0;
        char in_string = ' ';


        for (Character current_char : chars) {
            if (state >= 0 && state <= 4) {
                if ((state != 3 && state != 4) && (current_char == '\'' || current_char == '"')) {
                    switch (in_string) {
                        case ' ' -> in_string = current_char;
                        case '\'' -> {
                            if (current_char == '\'') in_string = ' ';
                        }
                        case '"' -> {
                            if (current_char == '"') in_string = ' ';
                        }
                    }
                }
                if (current_char == SUB_LEAVE_TOKENS[state] && in_string == ' ') {
                    nests--;
                    if (nests <= 0) {
                        tokens.add(new Token(token_builder.toString(), state));
                        token_builder = new StringBuilder();
                        state = -1;
                    }
                } else
                    token_builder.append(current_char);
                continue;
            } else {
                if (isInArray(current_char, SUB_ENTER_TOKENS) && in_string == ' ') {
                    if (!token_builder.isEmpty()) {
                        tokens.add(new Token(token_builder.toString(), state));
                        token_builder = new StringBuilder();
                    }
                    nests++;
                    state = inArray(current_char, SUB_ENTER_TOKENS);
                    continue;
                }
            }

            int char_type = -1;
            if (isInArray(current_char, ARITHMETIC_TOKENS)) {
                char_type = 5;
            } else if (isInArray(current_char, CONTROL_TOKENS)) {
                char_type = 6;
            } else if (IDENTIFIER_TOKENS.matcher(current_char.toString()).matches()) {
                char_type = 7;
            } else if (current_char == '\n') {
                char_type = 8;
            }
            if (char_type != -1) {
                if (state == char_type) {
                    token_builder.append(current_char);
                } else {
                    if (!token_builder.isEmpty()) {
                        tokens.add(new Token(token_builder.toString(), state));
                        token_builder = new StringBuilder();
                    }
                    token_builder.append(current_char);
                    state = char_type;
                }
                continue;
            }

            if (current_char == ' ' && !token_builder.isEmpty()) {
                tokens.add(new Token(token_builder.toString(), state));
                token_builder = new StringBuilder();
                state = -1;
            }
        }

        if (!token_builder.isEmpty()) {
            tokens.add(new Token(token_builder.toString(), state));
        }

        return ArrayUtil.linkedToArrayList(tokens);
    }

    static List<List<Token>> tokenizeLines(String content) {
        List<Token> tokens = tokenize(content);
        List<List<Token>> lines = new LinkedList<>();
        List<Token> current_line = new LinkedList<>();

        for (Token token : tokens) {
            if (token.type == TokenTypes.CONTROL && Objects.equals(token.content, ";")) {
                lines.add(current_line);
                current_line = new LinkedList<>();
                continue;
            }
            if (token.type != TokenTypes.NEW_LINE)
                current_line.add(token);
        }

        if (!current_line.isEmpty())
            throw new RuntimeException("file is not closed with semicolon");

        return lines;
    }


    public String content;
    public int type;

    private Token(String content, int type) {
        if (type == TokenTypes.IDENTIFIER && isInArray(content.toLowerCase(), KEYWORDS)) {
            this.content = content.toLowerCase();
            this.type = inArray(content.toLowerCase(), KEYWORDS) + 10;
        } else {
            this.content = content;
            this.type = type;
        }
    }

    @Override
    public String toString() {
        return type + ":" + content.replaceAll("\\r|\\n", "");
    }
}
