package org.dbprosjekt.database;

import org.dbprosjekt.controllers.Program2Controller;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

public class DatabaseQueryGenerator extends DBConn {
    public DatabaseQueryGenerator() {
        super();
    }


    public ResultSet query(String queryString) {
        try {
            Statement statement = conn.createStatement();
            return statement.executeQuery(queryString);
        } catch (Exception e) {
            System.out.println(e.getCause());
            return null;
        }
    }

    public ArrayList<String> getSelectResult(ResultSet rs, String... properties) {
        var res = new ArrayList<String>();
        try {
            while (rs.next()) {
                Arrays.asList(properties).forEach(property -> {
                    try {
                        res.add(rs.getString(property));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    public boolean queryHasResultRows(String queryString) {
        var rs = query(queryString);
        if (rs==null)
            return false;
        try {
            if (rs.isBeforeFirst()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void insertUser(String email, String username, String password) {
        try {
            Statement statement = conn.createStatement();
            String queryString = "insert into User(Email, Username, Password, Type) values('" + email + "','" + username + "','" + password + "','student')";
            statement.execute(queryString);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public void insertSubject(String id, String name){
        try {
            Statement statement = conn.createStatement();
            String queryString = "insert into Subject(SubjectID, name ) values('" + id + "','" + name + "')";
            statement.execute(queryString);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public void insertCourse(String id, String term, boolean allowsAnonymous){
        try {
            Statement statement = conn.createStatement();
            String queryString = "insert into Course(SubjectID, Term, AllowsAnonymous) values('" + id + "','" + term + "',"+allowsAnonymous+")";
            statement.execute(queryString);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public void insertFolder(String name){
        String queryString = "";
        try {
            String term = Session.getTerm();
            String courseID = Session.getCourseID();
            Statement statement = conn.createStatement();
            if(Session.getCurrentFolderID()==0){
                queryString = "insert into Folder(FolderID,Name,ParentID,SubjectID,Term) values(null,'"+name+"',null,'"+courseID+"','"+term+"')";
            }
            else{
                queryString = "insert into Folder(FolderID,Name,ParentID,SubjectID,Term) values(null,'"+name+"','"+Session.getCurrentFolderID()+"',null,null)";
            }
            statement.execute(queryString);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
