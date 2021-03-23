package org.dbprosjekt.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public abstract class Session {
    private static final DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();

    private static String userID;

    private static String courseID;

    private static String term;

    private static int folderID;

    private static boolean isAdmin;

    private static int replyingToID;

    public static void setReplyingToID(int id){
        replyingToID = id;
    }
    public static int getReplyingToID(){
        return replyingToID;
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
    public static String getUsername() throws SQLException {
        String queryString = "select * from User where Email='"+userID+"'";
        ResultSet rs = queryGenerator.query(queryString);
        rs.next();
        return rs.getString("Username");
    }
    public static String getCourseName() throws SQLException {
        if(courseID==null)
            return "";
        String queryString = "select * from Subject where SubjectID='"+courseID+"'";
        ResultSet rs = queryGenerator.query(queryString);
        rs.next();
        return rs.getString("name");
    }
    private static String getFolderName(int folderID) throws SQLException {
        if(folderID==0)
            return "";
        String queryString = "select * from Folder where FolderID='"+folderID+"'";
        ResultSet rs = queryGenerator.query(queryString);
        rs.next();
        return rs.getString("Name");
    }
    public static String getFolderPath() throws SQLException {
        int currFolderID = folderID;
        if(currFolderID==0)
            return "";
        String path = getFolderName(currFolderID);
        while(true){
            String queryString = "select * from Folder where FolderID='"+currFolderID+"'";
            ResultSet rs = queryGenerator.query(queryString);
            rs.next();
            int parentID = rs.getInt("ParentID");
            if(parentID==0)
                return path;
            path = getFolderName(parentID)+"/"+path;
            currFolderID = parentID;
        }
    }

    public static String getTermString(){
        if(term==null)
            return "";
        return term;
    }
    public static String getCourseIDString(){
        if(courseID==null)
            return "";
        return courseID;
    }

    public static String ToString(){
        try {
            String s = getUsername();
            if (!getCourseIDString().equals(""))
                s+="/"+getCourseIDString()+"/"+getCourseName()+" - "+getTermString()+"/"+getFolderPath();
            if (isAdmin())
                s+="        Logged in as instructor";
            else
                s+="        Logged in as student";
            return s;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return "";
    }
}
