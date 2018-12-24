package kvv.education.khasang.java1.chat.model;

import kvv.education.khasang.java1.chat.model.storage.file.ConnectorFileStorage;
import kvv.education.khasang.java1.chat.model.storage.omd.ConnectorOMDStorage;
import kvv.education.khasang.java1.chat.model.storage.omd.StorageOMD;

import java.io.IOException;

/**
 * Фабрика коннекторов
 */
public class ConnectorFactory {
    /**
     * Создает коннектор к существующему хранилищу
     *
     * @param storageType
     * @param args
     * @return
     * @throws IOException
     */
    public static StorageConnector getInstanceForExsistsStorage(StorageType storageType, Object... args) throws IOException {
        StorageConnector connector;
        switch (storageType) {
            case FILE:
                //в переданных параметрах должен быть путь к рабочей директории
                connector = new ConnectorFileStorage((String) args[0], false);
                break;
            case OMD:
                //в переданных параметрах должно быть хранилище
                connector = new ConnectorOMDStorage((StorageOMD) args[0]);
                break;
            default:
                throw new IllegalArgumentException("Не известный тип коннектора: " + storageType);
        }
        return connector;
    }

    /**
     * Создает коннектор к новому созданному хранилищу
     *
     * @param storageType
     * @param args
     * @return
     * @throws IOException
     */
    public static StorageConnector getInstanceForNewStorage(StorageType storageType, Object... args) throws IOException {
        StorageConnector connector;
        switch (storageType) {
            case FILE:
                //в переданных параметрах должен быть путь к рабочей директории нового создаваемого хранилища
                String pathToNewFileStorage = (String) args[0];
                connector = new ConnectorFileStorage(pathToNewFileStorage, true);
                break;
            case OMD:
                //в переданных параметрах должно быть хранилище
                StorageOMD newStorageOMD = new StorageOMD();
                connector = new ConnectorOMDStorage(newStorageOMD);
                break;
            default:
                throw new IllegalArgumentException("Не известный тип коннектора: " + storageType);
        }
        return connector;
    }
}
