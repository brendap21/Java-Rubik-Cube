package main;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class PixelFont {
    private static final Map<Character, int[]> FONT = new HashMap<>();
    static {
        // digits
        FONT.put('0', new int[]{0b01110,0b10001,0b10011,0b10101,0b11001,0b10001,0b01110});
        FONT.put('1', new int[]{0b00100,0b01100,0b00100,0b00100,0b00100,0b00100,0b01110});
        FONT.put('2', new int[]{0b01110,0b10001,0b00001,0b00110,0b01000,0b10000,0b11111});
        FONT.put('3', new int[]{0b11110,0b00001,0b00001,0b01110,0b00001,0b00001,0b11110});
        FONT.put('4', new int[]{0b00010,0b00110,0b01010,0b10010,0b11111,0b00010,0b00010});
        FONT.put('5', new int[]{0b11111,0b10000,0b10000,0b11110,0b00001,0b00001,0b11110});
        FONT.put('6', new int[]{0b01110,0b10000,0b10000,0b11110,0b10001,0b10001,0b01110});
        FONT.put('7', new int[]{0b11111,0b00001,0b00010,0b00100,0b01000,0b01000,0b01000});
        FONT.put('8', new int[]{0b01110,0b10001,0b10001,0b01110,0b10001,0b10001,0b01110});
        FONT.put('9', new int[]{0b01110,0b10001,0b10001,0b01111,0b00001,0b00001,0b01110});
        // letters
        FONT.put('A', new int[]{0b01110,0b10001,0b10001,0b11111,0b10001,0b10001,0b10001});
        FONT.put('B', new int[]{0b11110,0b10001,0b10001,0b11110,0b10001,0b10001,0b11110});
        FONT.put('C', new int[]{0b01111,0b10000,0b10000,0b10000,0b10000,0b10000,0b01111});
        FONT.put('D', new int[]{0b11110,0b10001,0b10001,0b10001,0b10001,0b10001,0b11110});
        FONT.put('E', new int[]{0b11111,0b10000,0b10000,0b11110,0b10000,0b10000,0b11111});
        FONT.put('F', new int[]{0b11111,0b10000,0b10000,0b11110,0b10000,0b10000,0b10000});
        FONT.put('G', new int[]{0b01110,0b10001,0b10000,0b10011,0b10001,0b10001,0b01110});
        FONT.put('H', new int[]{0b10001,0b10001,0b10001,0b11111,0b10001,0b10001,0b10001});
        FONT.put('I', new int[]{0b11111,0b00100,0b00100,0b00100,0b00100,0b00100,0b11111});
        FONT.put('J', new int[]{0b00111,0b00010,0b00010,0b00010,0b10010,0b10010,0b01100});
        FONT.put('K', new int[]{0b10001,0b10010,0b10100,0b11000,0b10100,0b10010,0b10001});
        FONT.put('L', new int[]{0b10000,0b10000,0b10000,0b10000,0b10000,0b10000,0b11111});
        FONT.put('M', new int[]{0b10001,0b11011,0b10101,0b10101,0b10001,0b10001,0b10001});
        FONT.put('N', new int[]{0b10001,0b11001,0b10101,0b10011,0b10001,0b10001,0b10001});
        FONT.put('O', new int[]{0b01110,0b10001,0b10001,0b10001,0b10001,0b10001,0b01110});
        FONT.put('P', new int[]{0b11110,0b10001,0b10001,0b11110,0b10000,0b10000,0b10000});
        FONT.put('Q', new int[]{0b01110,0b10001,0b10001,0b10001,0b10101,0b10010,0b01101});
        FONT.put('R', new int[]{0b11110,0b10001,0b10001,0b11110,0b10100,0b10010,0b10001});
        FONT.put('S', new int[]{0b01111,0b10000,0b10000,0b01110,0b00001,0b00001,0b11110});
        FONT.put('T', new int[]{0b11111,0b00100,0b00100,0b00100,0b00100,0b00100,0b00100});
        FONT.put('U', new int[]{0b10001,0b10001,0b10001,0b10001,0b10001,0b10001,0b01110});
        FONT.put('V', new int[]{0b10001,0b10001,0b10001,0b10001,0b01010,0b01010,0b00100});
        FONT.put('W', new int[]{0b10001,0b10001,0b10001,0b10101,0b10101,0b10101,0b01010});
        FONT.put('X', new int[]{0b10001,0b10001,0b01010,0b00100,0b01010,0b10001,0b10001});
        FONT.put('Y', new int[]{0b10001,0b10001,0b01010,0b00100,0b00100,0b00100,0b00100});
        FONT.put('Z', new int[]{0b11111,0b00001,0b00010,0b00100,0b01000,0b10000,0b11111});
        FONT.put(' ', new int[]{0,0,0,0,0,0,0});
        FONT.put(':', new int[]{0,0b00100,0,0,0,0b00100,0});
        FONT.put('-', new int[]{0,0,0,0b11111,0,0,0});
    }

    public static void drawString(Graficos g, String text, int x, int y, int scale, Color color) {
        int cursorX = x;
        text = text.toUpperCase();
        for (char c : text.toCharArray()) {
            int[] pattern = FONT.getOrDefault(c, FONT.get(' '));
            drawCharPattern(g, pattern, cursorX, y, scale, color);
            cursorX += (5 + 1) * scale;
        }
    }

    private static void drawCharPattern(Graficos g, int[] pattern, int x, int y, int scale, Color color) {
        for (int row = 0; row < 7; row++) {
            int bits = pattern[row];
            for (int col = 0; col < 5; col++) {
                if ((bits & (1 << (4 - col))) != 0) {
                    int x0 = x + col * scale;
                    int y0 = y + row * scale;
                    g.fillRect(x0, y0, x0 + scale - 1, y0 + scale - 1, color);
                }
            }
        }
    }
}
