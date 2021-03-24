package org.dbprosjekt.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import org.dbprosjekt.database.DatabaseQueryGenerator;

import java.sql.SQLException;
import java.util.ArrayList;

//Kontrollerer fremvinsing og innhenting av statistikk.
public class StatsController {

	//TODO change email to username in stats

	@FXML
	TableView usertable;

	@FXML
	TableView threadtable;

	@FXML
	Text activeUsersText;

	@FXML
	//går tilbake til hovedvinduet
	public void back() throws SQLException {
		Program2Controller.reload();
	}

	@FXML
	//Innhenter aktuelle data og oppretter tabeller
	public void initialize() throws SQLException {
		//about users
		//how many posts users have created and viewed in total
		var queryGenerator = new DatabaseQueryGenerator();
		var stats = queryGenerator.getTotalUserStats();

		ObservableList<UserRow> data = FXCollections.observableArrayList();

		stats.forEach(user -> {
			data.add(new UserRow(user.get(0), user.get(1), user.get(2)));
		});


		//Creating columns
		TableColumn username = new TableColumn("Username");
		username.setCellValueFactory(new PropertyValueFactory<>("username"));
		TableColumn postsViewed = new TableColumn("Number of posts viewed");
		postsViewed.setCellValueFactory(new PropertyValueFactory("postsViewed"));
		TableColumn postsCreated = new TableColumn("Number of posts created");
		postsCreated.setCellValueFactory(new PropertyValueFactory("postsCreated"));

		usertable.setItems(data);
		usertable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		usertable.getColumns().addAll(username, postsViewed, postsCreated);


		//daily active users
		String activeUsers = queryGenerator.getDailyActiveUsers();
		activeUsersText.setText("Daily active users: " + activeUsers);

		//about threads
		//most active threads
		var mostViewedThreads = queryGenerator.getActiveThreads();
		var mostRepliedToThreads = queryGenerator.getMostRepliedToThreads();

		//list with [title, viewed, repliedto]
		var mostActiveThreads = new ArrayList<ArrayList<String>>();

		mostViewedThreads.forEach(thread -> {
			var curr = new ArrayList<String>();
			curr.add(thread.get(1));
			curr.add(thread.get(2));
			curr.add(Integer.toString(mostRepliedToThreads.get(Integer.parseInt(thread.get(0)))));
			mostActiveThreads.add(curr);
		});

		ObservableList<ThreadRow> threadData = FXCollections.observableArrayList();

		mostActiveThreads.forEach(thread -> {
			threadData.add(new ThreadRow(thread.get(0), thread.get(1), thread.get(2)));
		});


		//Creating columns
		TableColumn title = new TableColumn("Title");
		title.setCellValueFactory(new PropertyValueFactory<>("title"));
		TableColumn viewedTimes = new TableColumn("Times viewed");
		viewedTimes.setCellValueFactory(new PropertyValueFactory("viewedTimes"));
		TableColumn commentedTimes = new TableColumn("Times commented");
		commentedTimes.setCellValueFactory(new PropertyValueFactory("commentedTimes"));

		threadtable.setItems(threadData);
		threadtable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		threadtable.getColumns().addAll(title, viewedTimes, commentedTimes);

	}
	//representerer en rad i tabellen som viser data og brukere
	public class UserRow {
		SimpleStringProperty username;
		SimpleStringProperty postsViewed;
		SimpleStringProperty postsCreated;

		public UserRow(String username, String postsViewed, String postsCreated) {
			this.username = new SimpleStringProperty(username);
			this.postsViewed = new SimpleStringProperty(postsViewed);
			this.postsCreated = new SimpleStringProperty(postsCreated);
		}

		public String getUsername() {
			return username.get();
		}


		public String getPostsViewed() {
			return postsViewed.get();
		}


		public String getPostsCreated() {
			return postsCreated.get();
		}

		public void setUsername(String username) {
			this.username.set(username);
		}

		public void setPostsViewed(String postsViewed) {
			this.postsViewed.set(postsViewed);
		}

		public void setPostsCreated(String postsCreated) {
			this.postsCreated.set(postsCreated);
		}
	}

		//representerer en rad i tabellen som viser data om threads
		public class ThreadRow {
		SimpleStringProperty title;
		SimpleStringProperty viewedTimes;
		SimpleStringProperty commentedTimes;

		public ThreadRow(String username, String postsViewed, String postsCreated) {
			this.title = new SimpleStringProperty(username);
			this.viewedTimes = new SimpleStringProperty(postsViewed);
			this.commentedTimes = new SimpleStringProperty(postsCreated);
		}

		public String getTitle() {
			return title.get();
		}


		public String getViewedTimes() {
			return viewedTimes.get();
		}


		public String getCommentedTimes() {
			return commentedTimes.get();
		}

		public void setTitle(String title) {
			this.title.set(title);
		}

		public void setViewedTimes(String viewedTimes) {
			this.viewedTimes.set(viewedTimes);
		}

		public void setCommentedTimes(String commentedTimes) {
			this.commentedTimes.set(commentedTimes);
		}
	}



}
