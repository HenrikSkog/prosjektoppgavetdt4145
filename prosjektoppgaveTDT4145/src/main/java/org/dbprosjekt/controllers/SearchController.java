package org.dbprosjekt.controllers;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.dbprosjekt.App;
import org.dbprosjekt.database.DatabaseQueryGenerator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

//Kontrollerer søk etter nøkkelord
public class SearchController {
    private static VBox root;
    private static DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
    //Finner alle threads som inneholder ett av søkeordene og displayer disse i guien
    public static void initialize(String input) throws SQLException {
        String[] keyWords = input.split(" ");
        HashSet<Integer> postIDs = new HashSet<>();
        ArrayList<Node> nodes = new ArrayList<>();
        for(String word:keyWords){
            String queryString = "select PostID from Post where Text like'%"+word+"%'";
            ResultSet rs = queryGenerator.query(queryString);
            while(rs.next()){
                int id = rs.getInt("PostID");
                while(true){
                    queryString = "select * from Post as P inner join Reply R on P.PostID=R.PostID where P.PostID='"+id+"'";
                    if(!queryGenerator.queryHasResultRows(queryString)){
                        if(!postIDs.contains(id)){
                            queryString = "select * from ThreadPost as TP inner join ThreadInFolder as TIF on TP.PostID=TIF.PostID inner join Post as P on P.PostID=TP.PostID inner join User on P.Author=User.Email and P.PostID='"+id+"'";
                            nodes.addAll(ProgramController.postsFromResultSet(queryGenerator.query(queryString)));
                        }
                        postIDs.add(id);
                        break;
                    }
                    ResultSet rs2 = queryGenerator.query(queryString);
                    rs2.next();
                    id = rs2.getInt("ReplyToID");
                }
            }
        }
        VBox vBox = new VBox();
        ScrollPane scrollPane = new ScrollPane(vBox);
        vBox.getChildren().addAll(ProgramController.nodeListToArray(nodes));
        Button back = new Button("Back");
        back.setOnAction(actionEvent -> {
            try {
                ProgramController.reload();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        Text title = new Text("Search results");
        title.setStyle("-fx-end-margin: 20");
        title.setStyle("-fx-font-size: 25");
        HBox top = new HBox(50);
        top.getChildren().addAll(title, back);
        root = new VBox(top, scrollPane);
        App.setRoot(root);
    }
}
