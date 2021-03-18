package org.dbprosjekt.database;

import java.util.ArrayList;

public abstract class FolderTraversal {
    private static ArrayList<String> folderPath;

    public static ArrayList<String> getFolderPath() {
        return folderPath;
    }

    public static void addToPath(String filename){
        folderPath.add(filename);
    }
}
