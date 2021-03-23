package org.dbprosjekt.controllers;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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
    private static VBox root;
    private static VBox leftVBox;
    private static VBox rightVBox;
    private static Button lastFolder;
    private static Text errorMessage;
    private static Text path = new Text();
    private static DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
    public static void initialize() throws SQLException {
        System.out.println("init");
        errorMessage = new Text();

        ComboBox<Course> dropDown = new ComboBox<>();
        dropDown.setPromptText("Choose Course");
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
                    if(!Session.isAdmin()){
                        errorMessage.setText("This action requires admin rights");
                        return;
                    }
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
                    if(!Session.isAdmin()){
                        errorMessage.setText("This action requires admin rights");
                        return;
                    }
                    App.setRoot("course");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        Button manageFolders = new Button();
        manageFolders.setText("Manage Folders");
        manageFolders.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                try {
                    if(!Session.isAdmin()){
                        errorMessage.setText("This action requires admin rights");
                        return;
                    }
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
        manageUsers.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    if(!Session.isAdmin()){
                        errorMessage.setText("This action requires admin rights");
                        return;
                    }
                    if(Session.getCourseID()!=null)
                        App.setRoot("manageUsers");
                    else
                        errorMessage.setText("Please select a course");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        Button newPost = new Button();
        newPost.setText("New Post");
        newPost.setOnAction(event -> {
            try {
                if(Session.getCourseID() != null && Session.getCurrentFolderID() != 0)
                    App.setRoot("newpost2");
                else
                    errorMessage.setText("Please select a course");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        Button viewStats = new Button();
        viewStats.setText("View Statistics");

        Button logOut = new Button();
        logOut.setText("Log Out");
        logOut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    handleLogOut();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        ToolBar toolBar = new ToolBar(dropDown, newSubject, newCourse, manageFolders, manageUsers, newPost, viewStats, logOut, errorMessage);
        leftVBox = new VBox();
        rightVBox = new VBox();
        ScrollPane leftScrollPane = new ScrollPane(leftVBox);
        ScrollPane rightScrollPane = new ScrollPane(rightVBox);
        SplitPane splitPane = new SplitPane(leftScrollPane, rightScrollPane);
        VBox vBox = new VBox(path, toolBar, splitPane);
        App.setRoot(vBox);
        root = vBox;
        updatePath();
    }
    private static void fillDropDown(ComboBox<Course> combo) throws SQLException {
        String queryString = "select Subject.SubjectID, Course.Term, Subject.name from Course inner join Subject on Course.SubjectID = Subject.SubjectID inner join InCourse on Subject.SubjectID = InCourse.SubjectID and Course.Term = InCourse.Term inner join User on InCourse.Email = User.Email where User.Email='"+Session.getUserID()+"'";
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
        String queryString = "select FolderID, Name from Folder where ParentID is null and SubjectID='"+Session.getCourseID()+"' and Term='"+Session.getTerm()+"'";
        ResultSet rs = queryGenerator.query(queryString);
        ArrayList<Node> nodes = new ArrayList<>();
        if(rs==null)
            return nodes;
        while(rs.next()){
            int folderID = rs.getInt("FolderID");
            Button folder = new Button(rs.getString("Name")+"  "+"ID: "+folderID);
            if(folderID!=0 && folderID==Session.getCurrentFolderID())
                goToFolder(folderID, folder);
            folder.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    try {
                        goToFolder(folderID, folder);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            });
            VBox sub = new VBox(nodeListToArray(subFolders(rs.getInt("FolderID"), 1)));
            Text filler = new Text(space(5));
            nodes.add(new VBox(folder, new HBox(filler, sub)));
        }
        return nodes;
    }
    private static ArrayList<Node> subFolders(int parentID, int depth) throws SQLException {
        String queryString = "select FolderID, Name from Folder where ParentID="+parentID;
        ResultSet rs = queryGenerator.query(queryString);
        ArrayList<Node> nodes = new ArrayList<>();
        if(rs==null){
            return nodes;
        }
        while(rs.next()){
            int folderID = rs.getInt("FolderID");
            Button folder = new Button(rs.getString("Name")+"  "+"ID: "+folderID);
            if(folderID!=0 && folderID==Session.getCurrentFolderID())
                goToFolder(folderID, folder);
            folder.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    try {
                        goToFolder(folderID, folder);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            });
            VBox sub = new VBox(nodeListToArray(subFolders(folderID, 1)));
            Text filler = new Text(space(5*(depth+1)));
            nodes.add(new VBox(folder, new HBox(filler, sub)));
        }
        return nodes;
    }

    private static ArrayList<Node> fillPosts() throws SQLException {
        String queryString = "select * from ThreadPost as TP inner join ThreadInFolder as TIF on TP.PostID=TIF.PostID inner join Post as P on P.PostID=TP.PostID inner join User on P.Author=User.Email where TIF.FolderID='"+Session.getCurrentFolderID()+"'";
        ResultSet rs = queryGenerator.query(queryString);
        ArrayList<Node> nodes = new ArrayList<>();
        if(rs==null)
            return nodes;
        while(rs.next()){
            int postID = rs.getInt("P.PostID");
            String tag = rs.getString("Tag");
            String text = rs.getString("Text");
            String title = rs.getString("Title");
            String date = rs.getString("Date");
            String time = rs.getString("Time");
            boolean isAnonymous = rs.getBoolean("IsAnonymous");
            String authorEmail = rs.getString("Email");
            String authorUsername = rs.getString("Username");
            String authorType = rs.getString("Type");
            System.out.println(postID);
            System.out.println(tag);
            System.out.println(text);
            System.out.println(date);
            System.out.println(time);
            System.out.println(isAnonymous);
            System.out.println(authorEmail);
            System.out.println(authorUsername);
            System.out.println(authorType);
            Text text1 = new Text(text);
            Text text2 = new Text(title);
            text2.setStyle("-fx-font-size: 20");
            Text tag1 = new Text("#"+tag);
            Text pID = new Text("ID: "+postID);
            Text dAndT = new Text("Posted: "+date+" "+time);
            Text userName = new Text("By: Anonymous user");
            if (!isAnonymous)
                userName.setText("By: "+authorUsername);
            Text type = new Text(authorType);
            HBox top = new HBox(10);
            top.getChildren().addAll(dAndT, userName, tag1, pID);
            nodes.add(new VBox(text2, top, text1));
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
        updatePath();
    }

    private static String space(int n){
        return " ".repeat(Math.max(0, n));
    }

    private static void goToFolder(int folderID, Button b) throws SQLException {
        if (lastFolder!=null)
            lastFolder.setStyle(null);
        Session.setFolderID(folderID);
        b.setStyle("-fx-background-color: #80cdb8;");
        lastFolder = b;
        updatePath();
        updatePots();
    }

    public static void reload() throws SQLException {
        App.setRoot(root);
        updateFolders();
        updatePath();
    }

    private static void handleLogOut() throws IOException {
        Session.setUserID(null);
        Session.setFolderID(0);
        Session.setTerm(null);
        Session.setCourseID(null);
        App.setRoot("login");
    }

    public static void updatePath(){
        path.setText("  "+Session.ToString());
    }

    public static void setErrorMessage(String message){
        errorMessage.setText(message);
    }

    private static void updatePots() throws SQLException {
        System.out.println("updatePosts");
        rightVBox.getChildren().clear();
        rightVBox.getChildren().addAll(nodeListToArray(fillPosts()));
    }

}
