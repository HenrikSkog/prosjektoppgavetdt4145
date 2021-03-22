package org.dbprosjekt.database;

import java.util.ArrayList;

public abstract class Session {
    private static ArrayList<Integer> folderPath = new ArrayList<>();

    private static String userID;

    private static String courseID;

    private static String term;

    public static ArrayList<Integer> getFolderPath() {
        return folderPath;
    }

    public static void addToPath(int folderID){
        folderPath.add(folderID);
    }

    public static void setUserID(String id){
        userID = id;
    }
    public static String getUserID(){
        return userID;
    }
    public static void setCourseID(String id){
        courseID = id;
    }
    public static String getCourseID(){
        return courseID;
    }
    public static void setTerm(String t){
        term = t;
    }
    public static String getTerm(){
        return term;
    }
    public static int getCurrentFolderID(){
        return folderPath.get(folderPath.size()-1);
    }

}
