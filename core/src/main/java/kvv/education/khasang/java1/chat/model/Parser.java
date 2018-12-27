package kvv.education.khasang.java1.chat.model;

import kvv.education.khasang.java1.chat.model.storage.file.FileUtil;

import java.io.FileNotFoundException;
import java.util.*;

/**
 * Набор информации для определения настроек подключения чата,
 * получаемый в результате анализа файла настроек
 *
 * Примеры файла настроек:
 *
 * ----1.txt----
 *
 * TYPE_STORAGE=FILE
 * FILE_STORAGE_PATH_FILE=sample/fileStorage
 *
 *
 * ----2.txt----
 *
 * TYPE_STORAGE=OMD
 */
public class Parser {

    public static final String TAG_TYPE_STORAGE = "TYPE_STORAGE";
    public static final String TAG_PATH_FILE_STORAGE = "FILE_STORAGE_PATH_FILE";
    public static final String TAG_PATH_SERIALIZATION_OMD = "SERIALIZATION_OMD_PATH_FILE";

    private String pathFile;
    Map<String, String> values;

    public Parser(Map<String, String> map) {
        values = map;
    }

    public Parser(String pathSettingsFile) throws FileNotFoundException {
        this.pathFile = pathSettingsFile;
        values = FileUtil.pullSettings(pathSettingsFile);
    }

    public String getPathFile() {
        return pathFile;
    }

    public StorageType getStorageType() throws IllegalFormatParserException {
        String type = getStringByTag(TAG_TYPE_STORAGE);
        StorageType storageType;
        if (type != null) {
            try {
                storageType = StorageType.valueOf(type);
            } catch (IllegalArgumentException e) {
                throw new IllegalFormatParserException("Указан не корректный тип хранилища: " + type + ". Допустимо: " + Arrays.asList(StorageType.values()).toString());
            }
            return storageType;
        } else {
            throw new IllegalFormatParserException("Не определен тип хранилища");
        }
    }

    public String getFileStoragePath() throws IllegalFormatParserException {
        String path = getStringByTag(TAG_PATH_FILE_STORAGE);
        if (path == null) {
            if (getStorageType() == StorageType.FILE) {
                throw new IllegalFormatParserException("В конфигурационном файле указан файловый тип хранилища, но не указан путь к файловому хранилищу");
            } else {
                return path;
            }
        } else {
            return path;
        }
    }

    public String getFileSerializationOmdPath() {
        return getStringByTag(TAG_PATH_SERIALIZATION_OMD);
    }

    public String getStringByTag(String tag) {
        String value = values.get(tag);
        return value;
    }
}
