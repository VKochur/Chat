package kvv.console.colors;

/**
 * Утилита вывода цветного текста в консоль
 */
public class Util {
    /**
     * Получает форматированную строку. Формат предполагает информацию о тексте и его оформлении
     * @param text изначальный текст
     * @param colors требуемое оформление
     * @return форматированную строку. Формат предполагает информацию о тексте и его оформлении
     */
    public static String colorConsolText(String text, ConsoleColors... colors) {
        if (colors.length < 1) {
            colors = new ConsoleColors[]{ConsoleColors.DEFAULT};
        }
        String param = String.valueOf(new char[]{(char) 27, '['});
        for (int i = 0; i < colors.length - 1; i++) {
            param = String.format("%s%s%c", param, String.valueOf(colors[i].cod), ';');
        }
        param = String.format("%s%s%c", param, String.valueOf(colors[colors.length - 1].cod), 'm');
        return String.format("%s%s", param, text);
    }

    /**
     * Выводит в консоль форматированный текст под указанное оформление текст
     * @param text
     * @param colors
     */
    public static void printConsolColorText(String text, ConsoleColors... colors) {
        String temp = colorConsolText(text, colors);
        System.out.print(temp);
        setDefaultConsolColorText();
    }

    /**
     * Возвращает настройки вывода текста в консоль на умолчательные
     */
    public static void setDefaultConsolColorText() {
        System.out.print(colorConsolText("", ConsoleColors.DEFAULT));
    }
}
