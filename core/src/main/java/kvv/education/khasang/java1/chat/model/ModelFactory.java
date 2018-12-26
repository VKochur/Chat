package kvv.education.khasang.java1.chat.model;

import kvv.education.khasang.java1.chat.model.storage.omd.StorageOMD;
import kvv.education.khasang.java1.chat.model.storage.file.ConnectorFileStorage;

import java.io.IOException;

/**
 * Фабрика моделей чатов
 */
public class ModelFactory {
    /**
     * Создает модель чата
     *
     * @param storageType тип хранилища данных
     * @param arg         в случае если тип хранилища storageType = FILE, в arg[0] должен быть путь к рабочей директории
     *                    если по указанному пути нет ничего, то будет создано новое файловое хранилище, если есть, то использовано существующее
     *                    файловое хранилище
     *                    <p>
     *                    если тип хранилища OMD, возвращает модель чата с хранением информации о данных чата в ОЗУ,
     *                    Если при этом указан аргумент arg, то arg[0], должен быть хранилищем StorageOMD, к которому необходимо подключиться
     *                    Если в arg ничего не указано, создается новое хранилище StorageOMD
     * @return
     * @throws IOException
     */
    public static ModelChat getInstance(StorageType storageType, Object... arg) throws IOException {
        StorageConnector storageConnector;
        switch (storageType) {
            case FILE:
                //в данном случае подразумеваем что в переданных параметрах путь к рабочей директории
                String workPath = (String) arg[0];
                //подключаемся к хранилищу по указанной директории
                if (ConnectorFileStorage.canCreateStorage(workPath)) {
                    //создаем хранилище, если по указанному пути нет каталога
                    storageConnector = ConnectorFactory.getInstanceForNewStorage(StorageType.FILE, workPath);
                } else {
                    //если каталог существует, подключаемся к хранилищу
                    storageConnector = ConnectorFactory.getInstanceForExsistsStorage(StorageType.FILE, workPath);
                }
                break;
            case OMD:
                if (arg.length > 0) {
                    StorageOMD storageOMD;
                    storageOMD = (StorageOMD) arg[0];
                    storageConnector = ConnectorFactory.getInstanceForExsistsStorage(StorageType.OMD, storageOMD);
                } else {
                    storageConnector = ConnectorFactory.getInstanceForNewStorage(StorageType.OMD);
                }
                break;

            default: {
                throw new IllegalArgumentException("Некорректный тип хранилища");
            }
        }

        ModelChat modelChat = new ModelChat();
        modelChat.setStorageConnector(storageConnector);
        return modelChat;
    }


    /**
     * Инстанцирует модель в зависимости от настроек подключения указанных указанных в Parser
     * <p>
     * Если в настройках подключения указан файловый тип хранилища, то в настройках должна быть информация о пути к файловому хранилищу
     * Если по указанному пути есть хранилище, то оно используется, если нет, то создается по указанному пути новое файловое хранилище
     * Пример настроек с указанием файлового хранилища:
     * TYPE_STORAGE=FILE
     * FILE_STORAGE_PATH_FILE=StorageFile/Demo
     * <p>
     * Если в настройках указано хранилище OMD, то может быть указан в настройках путь к файлу, дессериализацией из которого нужно взять
     * хранилище StorageOMD, а может и не указан (в этом случае, будет создано новое хранилище StorageOMD)
     * Пример настроек с указанием хранилища
     * TYPE_STORAGE=OMD
     * SERIALIZATION_OMD_PATH_FILE=StorageOMD/DemoStorageOMD
     * <p>
     * или
     * <p>
     * TYPE_STORAGE=OMD
     *
     * @param parser
     * @return
     */
    public static ModelChat getInstance(Parser parser) throws IllegalFormatParserException, IOException {
        StorageType storageType = parser.getStorageType();
        switch (storageType) {
            case OMD:
                String pathFile = parser.getFileSerializationOmdPath();
                StorageOMD storageOMD;
                try {
                    //если указан путь к файлу в котором хранится хранилище OMD, дессериализуем его, иначе новое хранилище
                    storageOMD = (pathFile == null) ? new StorageOMD() : StorageOMD.getInstance(pathFile);
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("Не удалось воостановить OMD хранилище. Файл дессериализации: " + pathFile);
                }
                return getInstance(StorageType.OMD, storageOMD);
            case FILE:
                String pathStorage = parser.getFileStoragePath();
                return getInstance(StorageType.FILE, pathStorage);
            default: {
                throw new IllegalArgumentException("Не известен порядок отработки парсером файла. Тип хранилища: " + storageType);
            }
        }
    }
}
