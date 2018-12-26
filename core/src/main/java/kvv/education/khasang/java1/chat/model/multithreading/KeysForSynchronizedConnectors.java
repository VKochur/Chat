package kvv.education.khasang.java1.chat.model.multithreading;

import kvv.education.khasang.java1.chat.model.StorageConnector;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Множество индентификаторов храниищ, к которым инициализированы коннекторы.
 * Элементы данного множества используются для синхронизации методов коннекторов к хранилищам.
 * <p>
 * Каждое хранилище имеет свой UUID
 * <p>
 * Для безопасного использования многопоточности методы реализуемые конкретными коннекторами должны быть синхронизированы.
 * В качестве ключа синхронизации должен выступать UUID хранилища.
 * <p>
 * Причины:
 * <p>
 * В общем случае может быть несколько классов реализующих коннектор для одного и того же хранилища.
 * Может быть несколько экземпляров одного или разных классов коннекторов подключенных к одному хранилищу, поэтому использовать synchronised целых методов не следует,
 * т.к. методы коннектора окажутся синхронизированы только в рамках определенного экзепляра коннектора, а поток работающий в методе другого экземпляра коннектора, но подключенного
 * к тому же хранилищу будет выполняться.
 * Использование статического ключа (статическое поле в определенном классе реализующем коннектор),
 * может привести к тому что это заблокирует поток выполнения операций над хранилищем, даже в том случае если по данному хранилищу никакой посторонний поток не выполняет действий (в случае если
 * будет еще один экзепляр такого же класса работать с другим хранилищем), но не заблокирует поток выполнения операций над хранилищем, в случае если коннектор будет экземпляром другого класса.
 * <p>
 * Использование единственного статического ключа для синхронизации всех методов всех классов реализующих коннектор
 * приведет к блокированию потоков для все хранилищ, независимо от того в каком хранилище и по какому коннектору поток осуществляют действия.
 */

public class KeysForSynchronizedConnectors {
    private static Set<UUID> keys = new HashSet<>();

    public static Set<UUID> getKeys() {
        return Collections.unmodifiableSet(keys);
    }

    public static void addKeyForSynchronized(UUID uuidStorage) {
        keys.add(uuidStorage);
    }

    public static UUID getKeyForSynchronized(StorageConnector storageConnector) {
        UUID uuidKey = null;
        UUID uuidStorage = null;
        try {
            uuidStorage = storageConnector.getStorageId();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (UUID key : keys) {
            if (key.equals(uuidStorage)) {
                uuidKey = key;
            }
        }
        if (uuidKey == null) {
            throw new IllegalStateException("Коннектор инстанцирован, а ключ синхронизации для соответствующего хранилища не найден!");
        }
        return uuidKey;
    }
}
