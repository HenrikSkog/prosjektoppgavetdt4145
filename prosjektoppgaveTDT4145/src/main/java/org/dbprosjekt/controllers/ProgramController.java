package org.dbprosjekt.controllers;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.dbprosjekt.App;
import org.dbprosjekt.Course;
import org.dbprosjekt.database.DatabaseQueryGenerator;
import org.dbprosjekt.database.Session;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

//Kontrollerer all funksjonalitet i hovedvinduet
public class ProgramController {
    private static VBox root;
    private static VBox leftVBox;
    private static VBox rightVBox;
    private static Button lastFolder;
    private static Text errorMessage;
    private static Text path = new Text();
    private static DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
    //Oppretter guien i hovedvinduet og legger til funksjonalitet på elementer
    public static void initialize() throws SQLException {
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
        newSubject.setOnAction(e -> {
            try {
                if(!Session.isAdmin()){
                    setErrorMessage("This action requires admin rights");
                    return;
                }
                App.setRoot("subject");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        Button newCourse = new Button();
        newCourse.setText("New Course");
        newCourse.setOnAction(e -> {
            try {
                if(!Session.isAdmin()){
                    setErrorMessage("This action requires admin rights");
                    return;
                }
                App.setRoot("course");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        Button manageFolders = new Button();
        manageFolders.setText("Manage Folders");
        manageFolders.setOnAction(e -> {
            try {
                if(!Session.isAdmin()){
                    setErrorMessage("This action requires admin rights");
                    return;
                }
                if(Session.getCourseID()!=null)
                    App.setRoot("folder");
                else
                    setErrorMessage("Please select a course");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        Button manageUsers = new Button();
        manageUsers.setText("Manage Users");
        manageUsers.setOnAction(actionEvent -> {
            try {
                if(!Session.isAdmin()){
                    setErrorMessage("This action requires admin rights");
                    return;
                }
                if(Session.getCourseID()!=null)
                    App.setRoot("manageUsers");
                else
                    setErrorMessage("Please select a course");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        Button newPost = new Button();
        newPost.setText("New Post");
        newPost.setOnAction(event -> {
            try {
                if(Session.getCourseID() != null && Session.getCurrentFolderID() != 0)
                    App.setRoot("newpost");
                else
                    setErrorMessage("Please select a course");
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
                    setErrorMessage("Unauthorized");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        Button logOut = new Button();
        logOut.setText("Log Out");
        logOut.setOnAction(event -> {
                try {
                    handleLogOut();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        });

        TextField searchInput = new TextField();
        searchInput.setPromptText("Enter keywords");
        Button search = new Button("Search");
        search.setOnAction(event -> {
                if(Session.getCourseID()==null){
                    setErrorMessage("Please select a course");
                    return;
                }
                String input = searchInput.getText();
                try {
                    SearchController.initialize(input);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
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
    //Fyller dropdown-menyen med courses den påloggede brukeren er med i
    private static void fillDropDown(ComboBox<Course> combo) throws SQLException {
        String queryString = "select Subject.SubjectID, Course.Term, Subject.name from Course inner join Subject on Course.SubjectID = Subject.SubjectID inner join InCourse on Subject.SubjectID = InCourse.SubjectID and Course.Term = InCourse.Term inner join User on InCourse.Email = User.Email where User.Email='"+Session.getUserID()+"'";
        ResultSet rs = queryGenerator.query(queryString);
        ArrayList<Course> courses = new ArrayList<>();
        while(rs.next()){
            String id = rs.getString("SubjectID");
            String term = rs.getString("Term");
            String name = rs.getString("name");
            courses.add(new Course(name, term, id));
        }
        combo.setItems(FXCollections.observableArrayList(courses));
    }
    //Utløses når bruker velger et course. Oppdaterer Session, og laster inn folders og posts
    private static void selectCourse(Course course) throws SQLException {
        Session.setCourseID(course.getId());
        Session.setTerm(course.getTerm());
        Session.setFolderID(0);
        updateFolders();
        updatePots();
    }
    //Returnerer en liste med JavaFX-elementer som inneholder alle folders i courset bruker befinner seg i.
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
            folder.setOnAction(actionEvent -> {
                try {
                    goToFolder(folderID, folder);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            });
            VBox sub = new VBox(nodeListToArray(subFolders(rs.getInt("FolderID"), 1)));
            Text filler = new Text(space(5));
            nodes.add(new VBox(folder, new HBox(filler, sub)));
        }
        return nodes;
    }
    //Calles fra funksjonen over og returnerer rekursivt lister med folders i form av JavaFX-elementer
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
            folder.setOnAction(actionEvent -> {
                try {
                    goToFolder(folderID, folder);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            });
            VBox sub = new VBox(nodeListToArray(subFolders(folderID, 1)));
            Text filler = new Text(space(5*(depth)));
            nodes.add(new VBox(folder, new HBox(filler, sub)));
        }
        return nodes;
    }

    //Returnerer en liste med posts i form av JavaFX-elementer for staten til programmet
    private static ArrayList<Node> fillPosts() throws SQLException {
        String queryString = "select * from ThreadPost as TP inner join ThreadInFolder as TIF on TP.PostID=TIF.PostID inner join Post as P on P.PostID=TP.PostID inner join User on P.Author=User.Email where TIF.FolderID='"+Session.getCurrentFolderID()+"'";
        ResultSet rs = queryGenerator.query(queryString);

        return postsFromResultSet(rs);
    }

    //Returner en liste med posts basert på et resultset med posts
    public static ArrayList<Node> postsFromResultSet(ResultSet rs) throws SQLException {
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
            String authorUsername = rs.getString("Username");
            Text text1 = new Text(text);
            Text text2 = new Text(title);
            text1.setWrappingWidth(400);
            text2.setWrappingWidth(400);

            text2.setStyle("-fx-font-size: 20");
            Text tag1 = new Text("#"+tag);
            Text pID = new Text("ID: "+postID);
            Text dAndT = new Text("Posted: "+date+" "+time);
            Text userName = new Text("By: Anonymous user");
            if (!isAnonymous)
                userName.setText("By: "+authorUsername);
            HBox top = new HBox(20);
            top.getChildren().addAll(dAndT, userName, tag1, pID);
            HBox bottom = createBottomPostPart(postID);
            nodes.add(new VBox(text2, top, text1, bottom, new Text(" "), new HBox(new Text(space(10)),new VBox(nodeListToArray(replies(postID, 1))))));
        }
        return nodes;
    }

    // tar inn en PostID og returnerer en HBox med nodes som skal bli vist i den nedre delen av en Post
    private static HBox createBottomPostPart(int postID) throws SQLException {
        HBox bottom = new HBox(20);

        Text likes = new Text("Likes: "+queryGenerator.getLikes(postID));

        Button like = new Button("Like");
        String queryString = "select * from User as U inner join UserLikedPost as ULP on U.Email=ULP.Email where U.Email='"+ Session.getUserID()+"' and ULP.PostID='"+postID+"'";
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
        return bottom;
    }

    //Returner en liste med replies i form av JavaFX-elementer rekursivt
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
            String authorUsername = rs.getString("Username");
            Text pID = new Text("ID: "+postID);
            Text text1 = new Text(text);
            text1.setWrappingWidth(400);
            Text dAndT = new Text("Posted: "+date+" "+time);
            Text userName = new Text("By: Anonymous user");
            if (!isAnonymous)
                userName.setText("By: "+authorUsername);
            HBox top = new HBox(20);
            top.getChildren().addAll(dAndT, userName, pID);
            HBox bottom = createBottomPostPart(postID);

            var linkedPostID = queryGenerator.getLinkedPost(Integer.toString(postID));

            var postVBox = new VBox();

            if(linkedPostID == null) {
                postVBox.getChildren().addAll(top, text1, bottom, new Text(" "), new HBox(new Text(space((depth)*10)), new VBox(nodeListToArray(replies(postID, depth+1)))));
            } else {
                Text linkText = new Text("This reply has a link to thread with id: " + linkedPostID);
                linkText.setStyle("-fx-font-style: italic");

                Button linkBtn = new Button("Go to linked thread");
                addLinkHandling(linkBtn, Integer.parseInt(linkedPostID));
                VBox box = new VBox(linkText, linkBtn, bottom);

                postVBox.getChildren().addAll(top, text1, box, new Text(" "), new HBox(new Text(space((depth)*10)), new VBox(nodeListToArray(replies(postID, depth+1)))));

            }
                nodes.add(postVBox);


        }
        return nodes;
    }

    //tar inn en list med nodes og returnerer en array
    public static Node[] nodeListToArray(ArrayList<Node> list){
        Node[] nodes = new Node[list.size()];
        for(int i = 0; i<list.size(); i++){
            nodes[i] = list.get(i);
        }
        return nodes;
    }

    //Oppdaterer guien med folders for courset programmet befinner seg i.
    private static void updateFolders() throws SQLException {
        leftVBox.getChildren().clear();
        leftVBox.getChildren().addAll(nodeListToArray(fillFolders()));
        updatePath();
    }

    //Returnerer en string med n mellomrom
    public static String space(int n){
        return " ".repeat(Math.max(0, n));
    }

    //Oppdaterer Session og gui når en mappe trykkes på
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

    // Åpner et modal-vindu med en thread, brukes i forbindelse med linker i replies til threads
    private static void openLinkedThreadInModal(int linkedPostID, ActionEvent event) throws SQLException {
        String queryString = "select * from ThreadPost as TP inner join ThreadInFolder as TIF on TP.PostID=TIF.PostID inner join Post as P on P.PostID=TP.PostID inner join User on P.Author=User.Email where TP.PostID="+linkedPostID;
        ResultSet rs = queryGenerator.query(queryString);

        var nodes = postsFromResultSet(rs);
        var nodesArray = nodeListToArray(nodes);

        var vbox = new VBox(nodesArray);
        var scrollPane = new ScrollPane(vbox);

        var modal = new Stage();
        modal.setMaximized(true);
        modal.setScene(new Scene(scrollPane));

        modal.setTitle("Linked thread");
        modal.initModality(Modality.WINDOW_MODAL);
        modal.initOwner(((Node)event.getSource()).getScene().getWindow());
        modal.show();

    }

    //Oppdaterer folders og posts og setter scenen til hovedvinduet
    public static void reload() throws SQLException {
        App.setRoot(root);
        updateFolders();
        updatePath();
    }

    //Nullstiller Session og oppdaterer scene til login når bruker trykker på log out
    private static void handleLogOut() throws IOException {
        Session.setUserID(null);
        Session.setFolderID(0);
        Session.setTerm(null);
        Session.setCourseID(null);
        App.setRoot("login");
    }

    //Setter teksten med state på toppen av skjermen
    public static void updatePath(){
        path.setText("  "+Session.ToString());
    }

    //Setter feilmeldingen i hovedvinduet
    public static void setErrorMessage(String message){
        errorMessage.setText(message);
    }

    //Oppdaterer guien med posts
    private static void updatePots() throws SQLException {
        rightVBox.getChildren().clear();
        rightVBox.getChildren().addAll(nodeListToArray(fillPosts()));
    }
    //Legger til funksjonalitet på like-knappene
    private static void addLikeHandling(Button like, int postID){
        like.setOnAction(actionEvent -> {
            try {
                queryGenerator.removeLike(Session.getUserID(), postID);
                updatePots();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }
    //Legger til funksjonalitet på liked-knappene
    private static void addUnlikeHandling(Button like, int postID){
        like.setOnAction(actionEvent -> {
            try {
                queryGenerator.insertLike(Session.getUserID(), postID);
                updatePots();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }
    //Legger til funksjonalitet på reply-knappene
    private static void addReplyHandling(Button reply, int postID){
        reply.setOnAction(actionEvent -> {
            Session.setReplyingToID(postID);
            try {
                App.setRoot("reply");
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }
    //Legger til funksjonalitet på link-knapper
    private static void addLinkHandling(Button btn, int linkedPostID) {
        btn.setOnAction(event -> {
                try {
                    openLinkedThreadInModal(linkedPostID, event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        });
    }
}
