package org.dbprosjekt.controllers;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.dbprosjekt.App;
import org.dbprosjekt.database.DatabaseQueryGenerator;
import org.dbprosjekt.database.Session;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class SearchController {
    private static VBox root;
    private static DatabaseQueryGenerator queryGenerator = new DatabaseQueryGenerator();
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
                    System.out.println(queryString);
                    if(!queryGenerator.queryHasResultRows(queryString)){
                        if(!postIDs.contains(id)){
                            queryString = "select * from ThreadPost as TP inner join ThreadInFolder as TIF on TP.PostID=TIF.PostID inner join Post as P on P.PostID=TP.PostID inner join User on P.Author=User.Email and P.PostID='"+id+"'";
                            nodes.addAll(Program2Controller.postsFromResultSet(queryGenerator.query(queryString)));
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
        System.out.println(postIDs);
        System.out.println(nodes);
        System.out.println(Arrays.toString(Program2Controller.nodeListToArray(nodes)));
        VBox vBox = new VBox();
        ScrollPane scrollPane = new ScrollPane(vBox);
        vBox.getChildren().addAll(Program2Controller.nodeListToArray(nodes));
        root = new VBox(scrollPane);
        App.setRoot(root);
    }
}
