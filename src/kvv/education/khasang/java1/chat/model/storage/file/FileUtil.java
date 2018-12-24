package kvv.education.khasang.java1.chat.model.storage.file;

import java.io.*;
import java.util.*;

public class FileUtil {

    /**
     * Создание файла по указанной директории
     *
     * @param pathFile
     * @throws IOException
     */
    public static void createFile(String pathFile) throws IOException {
        File file = new File(pathFile);
        new File(file.getParent()).mkdirs();
        file.createNewFile();
    }

    /**
     * Получение множества ключ-значение из файла формата
     * ключ1=значение1
     * ключ2=значение2
     *
     * @param pathFile
     * @return
     * @throws FileNotFoundException
     */
    public static Map<String, String> pullSettings(String pathFile) throws FileNotFoundException {
        Map<String, String> settings = new HashMap<>();
        try (Scanner scanner = new Scanner(new FileInputStream(pathFile))) {
            while (scanner.hasNext()) {
                String temp = scanner.nextLine();
                int index = temp.indexOf('=');
                settings.put(temp.substring(0, index), temp.substring(index + 1, temp.length()));
            }
        }
        return settings;
    }

    /**
     * Получить значение по ключу из файла соответсвий.
     * Вид файла
     * ключ1=значение1
     * ключ2=значение2
     *
     * @param pathFile путь к файлу
     * @param key
     * @return null если не найдено
     */
    public static String pullValue(String pathFile, String key) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(new BufferedInputStream(new FileInputStream(pathFile)))) {
            while (scanner.hasNext()) {
                String currentLine = scanner.nextLine();
                int index = currentLine.indexOf('=');
                String tag = currentLine.substring(0, index);
                if (tag.equals(key)) {
                    return currentLine.substring(index + 1, currentLine.length());
                }
            }
        }
        return null;
    }

    /**
     * Сохранить значение соответствующее ключу в файле соответствий
     * Если есть в файле соответсвующий ключ, то его значение меняется, если нет, то добавляется ключ-значение в конец файла
     *
     * @param pathFile
     * @param key
     * @param newValue
     */
    public static void saveValue(String pathFile, String key, String newValue) throws IOException {
        Map<String, String> map = FileUtil.pullSettings(pathFile);
        if (map.containsKey(key)) {
            map.replace(key, newValue);
        } else {
            map.put(key, newValue);
        }
        FileUtil.saveSettings(pathFile, map);
    }

    /**
     * Сохранение множества ключ-значение в файл формата
     * ключ1=значение1
     * ключ2=значение2
     *
     * @param pathFile
     * @param applicationsSettings
     * @throws IOException
     */
    public static void saveSettings(String pathFile, Map<String, String> applicationsSettings) throws IOException {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(pathFile)))) {
            Set<Map.Entry<String, String>> temp = applicationsSettings.entrySet();
            for (Map.Entry<String, String> tagValueEntry : temp) {
                out.printf("%s=%s%n", tagValueEntry.getKey(), tagValueEntry.getValue());
            }
        }
    }

}
