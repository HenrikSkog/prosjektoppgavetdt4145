package org.dbprosjekt.controllers;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.dbprosjekt.App;
import org.dbprosjekt.Course;
import org.dbprosjekt.database.DatabaseQueryGenerator;
import org.dbprosjekt.database.Session;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Program2Controller {
    private static VBox leftVBox;

    public static void initialize() throws SQLException {
        System.out.println("init");
        Text errorMessage = new Text();

        ComboBox<Course> dropDown = new ComboBox<>();
        fillDropDown(dropDown);
        dropDown.valueProperty().addListener((obs, oldVal, newVal) -> {
            try {
                selectCourse(newVal);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });

        Button newSubject = new Button();
        newSubject.setText("New Subject");
        newSubject.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                try {
                    App.setRoot("subject");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        Button newCourse = new Button();
        newCourse.setText("New Course");
        newCourse.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                try {
                    App.setRoot("course");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        Button newFolder = new Button();
        newFolder.setText("New Folder");
        newFolder.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                try {
                    if(Session.getCourseID()!=null)
                        App.setRoot("folder");
                    else
                        errorMessage.setText("Please select a course");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        Button manageUsers = new Button();
        manageUsers.setText("Manage Users");

        Button newPost = new Button();
        newPost.setText("New Post");
        newPost.setOnAction(event -> {
            try {
                if(Session.getCourseID() != null)
                    App.setRoot("newpost");
                else
                    errorMessage.setText("Please select a course");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        Button viewStats = new Button();
        viewStats.setText("View Statistics");

        ToolBar toolBar = new ToolBar(dropDown, newSubject, newCourse, newFolder, manageUsers, newPost, viewStats, errorMessage);
//        if(Session.getCourseID()!=null){
//            leftVBox = new VBox(nodeListToArray(fillFolders()));
//        }
//        else
        leftVBox = new VBox();
        VBox rightVBox = new VBox();
        ScrollPane leftScrollPane = new ScrollPane(leftVBox);
        ScrollPane rightScrollPane = new ScrollPane(rightVBox);
        SplitPane splitPane = new SplitPane(leftScrollPane, rightScrollPane);
        VBox vBox = new VBox(toolBar, splitPane);
        App.SetRoot(vBox);
    }
    private static void fillDropDown(ComboBox<Course> combo) throws SQLException {
        String queryString = "select SubjectID, Term, name from Course natural join Subject";
        DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
        ResultSet rs = queryGenerator.query(queryString);
        ArrayList<Course> courses = new ArrayList<Course>();
        while(rs.next()){
            String id = rs.getString("SubjectID");
            String term = rs.getString("Term");
            String name = rs.getString("name");
            courses.add(new Course(name, term, id));
        }
        combo.setItems(FXCollections.observableArrayList(courses));
    }
    private static void selectCourse(Course course) throws SQLException {
        Session.setCourseID(course.getId());
        Session.setTerm(course.getTerm());
        updateFolders();
    }
    private static ArrayList<Node> fillFolders() throws SQLException {
        String queryString = "select FolderID, Name from Folder where ParentID is null";
        DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
        ResultSet rs = queryGenerator.query(queryString);
        ArrayList<Node> nodes = new ArrayList<>();
        while(rs.next()){
            if (!Session.getFolderPath().contains(rs.getInt("FolderID"))){
                nodes.add(new Button(rs.getString("Name")+" "+rs.getString("FolderID")));
            }
            else{
                nodes.add(new VBox(nodeListToArray(subFolders(rs.getInt("FolderID"), 1))));
            }
        }
        return nodes;
    }
    private static ArrayList<Node> subFolders(int parentID, int depth) throws SQLException {
        String queryString = "select FolderID, Name from folder where ParentID="+parentID;
        DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
        ResultSet rs = queryGenerator.query(queryString);
        ArrayList<Node> nodes = new ArrayList<>();
        while(rs.next()){
            if (!Session.getFolderPath().contains(rs.getInt("FolderID"))){
                nodes.add(new Button(rs.getString("Name")+" "+rs.getString("FolderID")));
            }
            else{
                nodes.add(new VBox(nodeListToArray(subFolders(rs.getInt("FolderID"), depth + 1))));
            }
        }
        return nodes;
    }

    private static Node[] nodeListToArray(ArrayList<Node> list){
        Node[] nodes = new Node[list.size()];
        for(int i = 0; i<list.size(); i++){
            nodes[i] = list.get(i);
        }
        return nodes;
    }

    private static void updateFolders() throws SQLException {
        System.out.println("updateFolders");
        leftVBox.getChildren().clear();
        leftVBox.getChildren().addAll(nodeListToArray(fillFolders()));
    }
}
