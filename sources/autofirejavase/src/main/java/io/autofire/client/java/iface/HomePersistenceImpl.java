package io.autofire.client.java.iface;

import io.autofire.client.japi.iface.FileBatchPersistence;

public class HomePersistenceImpl extends FileBatchPersistence {
    private static final String HOME_PATH = System.getProperty("user.home");

    protected String rootDirectory(Object platformContext) {
        return HOME_PATH;
    }
}
