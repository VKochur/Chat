package kvv.console.colors;

/**
 * Коды цветного вывода в консоль
 */
public enum ConsoleColors {
    DEFAULT(0),
    TEXT_BLACK(30),
    TEXT_RED (31),
    TEXT_GREEN(32),
    TEXT_YELLOW(33),
    TEXT_BLUE(34),
    TEXT_MAGENTA(35),
    TEXT_CYAN(36),
    TEXT_WHITE(37),
    BG_BLACK (40),
    BG_RED (41),
    BG_GREEN(42),
    BG_YELLOW(43),
    BG_BLUE(44),
    BG_MAGENTA(45),
    BG_CYAN(46),
    BG_WHITE(47);

    int cod;

    ConsoleColors(int cod){
        this.cod = cod;
    }
}
