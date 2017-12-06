package io.autofire.client.japi.iface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static java.io.File.separator;

public abstract class FileBatchPersistence extends KVBatchPersistence {
    public static final String SEPARATOR = separator;

    protected abstract String rootDirectory(Object platformContext);

    public static String pathCombine(String path1, String path2) {
        return path1 + SEPARATOR + path2;
    }

    public static boolean directoryExists(File path) {
        return path.exists() && path.isDirectory();
    }

    public static boolean createDirectory(File path) {
        try {
            return path.exists() || path.mkdirs();
        } catch (Exception e) {
            return false;
        }
    }

    public static String readAllText(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            try {
                fis.read(data);
            } catch (IOException ioe) {
            } finally {
                try {
                    fis.close();
                } catch (IOException ioe) {
                }
            }
            try {
                return new String(data, "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                return "";
            }
        } catch (FileNotFoundException fnfe) {
            return "";
        }
    }

    public static boolean writeAllText(File file, String text) {
        boolean result;

        try {
            FileOutputStream fOut = new FileOutputStream(file);
            fOut.write(text.getBytes());
            fOut.close();
            result = true;
        } catch (IOException ioe) {
            result = false;
        }

        return result;
    }

    protected String chRootDirectory(Object platformContext) {
        return pathCombine(rootDirectory(platformContext), ".autofire");
    }

    protected String gameDirectory(Object platformContext) {
        return pathCombine(chRootDirectory(platformContext), BatchPersistence.gameId);
    }

    protected String getFileName(Object platformContext,
                                 String fname, boolean isAbsolute) {
        return isAbsolute ?
                pathCombine(chRootDirectory(platformContext), fname) :
                pathCombine(gameDirectory(platformContext), fname);
    }

    protected void checkDirectory(Object platformContext,
                                  boolean isAbsolute) {
        File chRoot = new File(chRootDirectory(platformContext));
        if (!directoryExists(chRoot))
            createDirectory(chRoot);
        if (!isAbsolute) {
            File game = new File(gameDirectory(platformContext));
            if (!directoryExists(game))
                createDirectory(game);
        }
    }

    protected String getString(Object platformContext,
                               String key,
                               boolean isAbsolute) {
        try {
            String fname = getFileName(platformContext, key, isAbsolute);
            File file = new File(fname);

            if (!file.exists())
                return DEFAULT_STRING;
            return readAllText(file);
        } catch (Exception e) {
            return DEFAULT_STRING;
        }
    }

    protected void setString(Object platformContext,
                             String key, String value,
                             boolean isAbsolute) {
        try {
            String fname = getFileName(platformContext, key, isAbsolute);
            File file = new File(fname);

            checkDirectory(platformContext, isAbsolute);
            writeAllText(file, value);
        } catch (Exception e) {
        }
    }

    protected int getInt(Object platformContext,
                         String key,
                         boolean isAbsolute) {
        try {
            String fname = getFileName(platformContext, key, isAbsolute);
            File file = new File(fname);

            if (!file.exists())
                return DEFAULT_INT;
            return Integer.parseInt(readAllText(file));
        } catch (Exception e) {
            return DEFAULT_INT;
        }
    }

    protected void setInt(Object platformContext,
                          String key, int value,
                          boolean isAbsolute) {
        try {
            String fname = getFileName(platformContext, key, isAbsolute);
            File file = new File(fname);

            checkDirectory(platformContext, isAbsolute);
            writeAllText(file, String.valueOf(value));
        } catch (Exception e) {
        }
    }

    public boolean isAvailable(Object platformContext) {
        try {
            checkDirectory(platformContext, false);
            String fname = pathCombine(gameDirectory(platformContext), "testAutofire");
            File file = new File(fname);
            writeAllText(file, "123");
            return file.delete();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean persistToDisk(Object platformContext) {
        return true;
    }
}
