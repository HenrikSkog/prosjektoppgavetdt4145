package org.dbprosjekt.database;

import java.util.ArrayList;

public abstract class Session {
    private static String userID;

    private static String courseID;

    private static String term;

    private static int folderID;

    private static boolean isAdmin;

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
        return folderID;
    }
    public static void setFolderID(int id){
        folderID = id;
    }
    public static boolean isAdmin(){
        return isAdmin;
    }
    public static void setAdmin(boolean b){
        isAdmin = b;
    }
}
