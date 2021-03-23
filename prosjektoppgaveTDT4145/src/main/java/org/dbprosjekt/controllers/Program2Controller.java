package org.dbprosjekt.controllers;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
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
        viewStats.setOnAction(event -> {
            try {
                if(Session.isAdmin())
                    App.setRoot("stats");
                else
                    errorMessage.setText("Unauthorized");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

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

        TextField searchInput = new TextField();
        searchInput.setPromptText("Enter keywords");
        Button search = new Button("Search");
        search.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(Session.getCourseID()==null){
                    errorMessage.setText("Please select a course");
                    return;
                }
                String input = searchInput.getText();
                try {
                    SearchController.initialize(input);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });

        ToolBar toolBar = new ToolBar(dropDown, newSubject, newCourse, manageFolders, manageUsers, newPost, viewStats, searchInput, search, logOut, errorMessage);
        leftVBox = new VBox();
        rightVBox = new VBox();
        ScrollPane leftScrollPane = new ScrollPane(leftVBox);
        ScrollPane rightScrollPane = new ScrollPane(rightVBox);
        SplitPane splitPane = new SplitPane(leftScrollPane, rightScrollPane);
        splitPane.setDividerPositions(0.2);
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
        Session.setFolderID(0);
        updateFolders();
        updatePots();
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
            Text filler = new Text(space(5*(depth)));
            nodes.add(new VBox(folder, new HBox(filler, sub)));
        }
        return nodes;
    }

    private static ArrayList<Node> fillPosts() throws SQLException {
        String queryString = "select * from ThreadPost as TP inner join ThreadInFolder as TIF on TP.PostID=TIF.PostID inner join Post as P on P.PostID=TP.PostID inner join User on P.Author=User.Email where TIF.FolderID='"+Session.getCurrentFolderID()+"'";
        ResultSet rs = queryGenerator.query(queryString);

        return postsFromResultSet(rs);
    }

    public static ArrayList<Node> postsFromResultSet(ResultSet rs) throws SQLException {
        String queryString;
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
            HBox top = new HBox(20);
            top.getChildren().addAll(dAndT, userName, tag1, pID);
            HBox bottom = new HBox(20);

            Text likes = new Text("Likes: "+queryGenerator.getLikes(postID));

            Button like = new Button("Like");
            queryString = "select * from User as U inner join UserLikedPost as ULP on U.Email=ULP.Email where U.Email='"+Session.getUserID()+"' and ULP.PostID='"+postID+"'";
            if(queryGenerator.queryHasResultRows(queryString)){
                like.setText("Liked");
                addLikeHandling(like, postID);
            }
            else{
                addUnlikeHandling(like, postID);
            }

            Button reply = new Button("Reply");
            addReplyHandling(reply, postID);

            bottom.getChildren().addAll(like, likes, reply);
            nodes.add(new VBox(text2, top, text1, bottom, new Text(" "), new HBox(new Text(space(10)),new VBox(nodeListToArray(replies(postID, 1))))));
        }
        return nodes;
    }

    private static ArrayList<Node> replies(int parentID, int depth) throws SQLException {
        String queryString = "select * from Reply as R inner join Post as P on P.PostID=R.PostID inner join User on P.Author=User.Email where R.ReplyToID='"+parentID+"'";
        ResultSet rs = queryGenerator.query(queryString);
        ArrayList<Node> nodes = new ArrayList<>();
        if(rs==null)
            return nodes;
        while(rs.next()){
            int postID = rs.getInt("P.PostID");
            String text = rs.getString("Text");
            String date = rs.getString("Date");
            String time = rs.getString("Time");
            boolean isAnonymous = rs.getBoolean("IsAnonymous");
            String authorEmail = rs.getString("Email");
            String authorUsername = rs.getString("Username");
            String authorType = rs.getString("Type");
            Text pID = new Text("ID: "+postID);
            Text text1 = new Text(text);
            Text dAndT = new Text("Posted: "+date+" "+time);
            Text userName = new Text("By: Anonymous user");
            if (!isAnonymous)
                userName.setText("By: "+authorUsername);
            Text type = new Text(authorType);
            HBox top = new HBox(20);
            top.getChildren().addAll(dAndT, userName, pID);
            HBox bottom = new HBox(20);

            Text likes = new Text("Likes: "+queryGenerator.getLikes(postID));

            Button like = new Button("Like");
            queryString = "select * from User as U inner join UserLikedPost as ULP on U.Email=ULP.Email where U.Email='"+Session.getUserID()+"' and ULP.PostID='"+postID+"'";
            if(queryGenerator.queryHasResultRows(queryString)){
                like.setText("Liked");
                addLikeHandling(like, postID);
            }
            else{
                addUnlikeHandling(like, postID);
            }

            Button reply = new Button("Reply");
            addReplyHandling(reply, postID);

            bottom.getChildren().addAll(like, likes, reply);
            nodes.add(new VBox(top, text1, bottom, new Text(" "), new HBox(new Text(space((depth)*10)), new VBox(nodeListToArray(replies(postID, depth+1))))));
        }
        return nodes;
    }

    public static Node[] nodeListToArray(ArrayList<Node> list){
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

    public static String space(int n){
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
        queryGenerator.insertThreadPostsViewedByUser(Integer.toString(folderID));
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
    private static void addLikeHandling(Button like, int postID){
        like.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                System.out.println("removing");
                try {
                    queryGenerator.removeLike(Session.getUserID(), postID);
                    updatePots();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });
    }
    private static void addUnlikeHandling(Button like, int postID){
        like.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                System.out.println("adding");
                try {
                    queryGenerator.insertLike(Session.getUserID(), postID);
                    updatePots();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });
    }
    private static void addReplyHandling(Button reply, int postID){
        reply.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Session.setReplyingToID(postID);
                try {
                    App.setRoot("reply");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

}
