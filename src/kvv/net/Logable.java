package kvv.net;

import java.io.PrintStream;
import java.util.Date;

/**
 * Ведущий лог своих действий
 */
public interface Logable {

    PrintStream getLogWriter();

    void setLogWriter(PrintStream logWriter);

    boolean isNeedWriteLog();

    void setNeedWriteLog(boolean needWriteLog);

    /**
     * Отправляет сообщение в лог
     *
     * @param s
     */
    default void writeToLog(String s) {
        if (isNeedWriteLog()) {
            if (getLogWriter() == null) {
                throw new NullPointerException("Логер не инициализирован");
            }
            getLogWriter().println(formatString(s));
            getLogWriter().flush();
        }
    }

    default String formatString(String s) {
        return this.toString() + "| " + new Date() + " " + Thread.currentThread() + ": " + s;
    }
}
