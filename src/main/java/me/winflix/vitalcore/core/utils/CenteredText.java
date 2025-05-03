package me.winflix.vitalcore.core.utils;

import org.bukkit.ChatColor;

import me.winflix.vitalcore.general.utils.Utils;

public class CenteredText {

    private static final int CENTER_PX = 160;

    public static String getCenteredText(String message) {
        if (message == null || message.isEmpty())
            return "";

        // Primero aplicamos los colores usando tu utilidad
        message = Utils.useColors(message);

        int messagePxSize = 0;
        boolean isBold = false;

        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);

            if (c == ChatColor.COLOR_CHAR && i + 1 < message.length()) {
                char next = message.charAt(i + 1);
                ChatColor color = ChatColor.getByChar(next);
                if (color != null && color == ChatColor.BOLD) {
                    isBold = true;
                } else if (color != null && color != ChatColor.RESET) {
                    isBold = false;
                }
                i++; // saltar el carácter de formato
                continue;
            }

            FontInfo dFI = FontInfo.getDefaultFontInfo(c);
            messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
            messagePxSize++; // espacio por carácter
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = FontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();

        while (compensated + spaceLength <= toCompensate - 4) {
            sb.append(" ");
            compensated += spaceLength;
        }

        return sb + message;
    }

    private enum FontInfo {
        A('A', 5), a('a', 5), B('B', 5), b('b', 5), C('C', 5), c('c', 5),
        D('D', 5), d('d', 5), E('E', 5), e('e', 5), F('F', 5), f('f', 4),
        G('G', 5), g('g', 5), H('H', 5), h('h', 5), I('I', 3), i('i', 1),
        J('J', 5), j('j', 5), K('K', 5), k('k', 5), L('L', 5), l('l', 2),
        M('M', 5), m('m', 5), N('N', 5), n('n', 5), O('O', 5), o('o', 5),
        P('P', 5), p('p', 5), Q('Q', 5), q('q', 5), R('R', 5), r('r', 5),
        S('S', 5), s('s', 5), T('T', 5), t('t', 4), U('U', 5), u('u', 5),
        V('V', 5), v('v', 5), W('W', 5), w('w', 5), X('X', 5), x('x', 5),
        Y('Y', 5), y('y', 5), Z('Z', 5), z('z', 5),
        NUM_1('1', 5), NUM_2('2', 5), NUM_3('3', 5), NUM_4('4', 5), NUM_5('5', 5),
        NUM_6('6', 5), NUM_7('7', 5), NUM_8('8', 5), NUM_9('9', 5), NUM_0('0', 5),
        EXCLAMATION_POINT('!', 1), AT_SYMBOL('@', 6), NUM_SIGN('#', 5),
        DOLLAR_SIGN('$', 5), PERCENT('%', 5), UP_ARROW('^', 5), AMPERSAND('&', 5),
        ASTERISK('*', 5), LEFT_PARENTHESIS('(', 4), RIGHT_PARENTHESIS(')', 4),
        MINUS('-', 5), UNDERSCORE('_', 5), PLUS('+', 5), EQUALS('=', 5),
        LEFT_CURL_BRACE('{', 4), RIGHT_CURL_BRACE('}', 4), LEFT_BRACKET('[', 3),
        RIGHT_BRACKET(']', 3), COLON(':', 1), SEMI_COLON(';', 1),
        DOUBLE_QUOTE('"', 3), SINGLE_QUOTE('\'', 1), LEFT_ARROW('<', 4),
        RIGHT_ARROW('>', 4), QUESTION_MARK('?', 5), SLASH('/', 5),
        BACKSLASH('\\', 5), PIPE('|', 5), TILDE('~', 5), BACKTICK('`', 2),
        PERIOD('.', 1), COMMA(',', 1), SPACE(' ', 3), DEFAULT('a', 4);

        private final char character;
        private final int length;

        FontInfo(char character, int length) {
            this.character = character;
            this.length = length;
        }

        public int getLength() {
            return length;
        }

        public int getBoldLength() {
            return this == SPACE ? getLength() : length + 1;
        }

        public static FontInfo getDefaultFontInfo(char c) {
            for (FontInfo dFI : FontInfo.values()) {
                if (dFI.character == c)
                    return dFI;
            }
            return DEFAULT;
        }
    }
}
