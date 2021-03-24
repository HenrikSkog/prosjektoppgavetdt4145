package org.dbprosjekt.controllers;

import javafx.beans.property.SimpleIntegerProperty;
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
		ProgramController.reload();
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
			data.add(new UserRow(user.get(0), Integer.parseInt(user.get(1)), Integer.parseInt(user.get(2))));
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
		activeUsersText.setText("Users active today: " + activeUsers);

		//about threads
		//most active threads
		var mostViewedThreads = queryGenerator.getActiveThreads();
		var mostRepliedToThreads = queryGenerator.getMostRepliedToThreads();

		//list with [title, viewed, repliedto]
		var mostActiveThreads = new ArrayList<ArrayList<String>>();

		//combining mostViewed and mostReplied to one list to easier create threadData-objects
		mostViewedThreads.forEach(thread -> {
			var curr = new ArrayList<String>();
			curr.add(thread.get(1));
			curr.add(thread.get(2));
			curr.add(Integer.toString(mostRepliedToThreads.get(Integer.parseInt(thread.get(0)))));
			mostActiveThreads.add(curr);
		});

		ObservableList<ThreadRow> threadData = FXCollections.observableArrayList();

		mostActiveThreads.forEach(thread -> {
			threadData.add(new ThreadRow(thread.get(0), Integer.parseInt(thread.get(1)), Integer.parseInt(thread.get(2))));
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
		SimpleIntegerProperty postsViewed;
		SimpleIntegerProperty postsCreated;

		public UserRow(String username, int postsViewed, int postsCreated) {
			this.username = new SimpleStringProperty(username);
			this.postsViewed = new SimpleIntegerProperty(postsViewed);
			this.postsCreated = new SimpleIntegerProperty(postsCreated);
		}

		public String getUsername() {
			return username.get();
		}


		public int getPostsViewed() {
			return postsViewed.get();
		}


		public int getPostsCreated() {
			return postsCreated.get();
		}

		public void setUsername(String username) {
			this.username.set(username);
		}

		public void setPostsViewed(int postsViewed) {
			this.postsViewed.set(postsViewed);
		}

		public void setPostsCreated(int postsCreated) {
			this.postsCreated.set(postsCreated);
		}
	}

	//representerer en rad i tabellen som viser data om threads
	public class ThreadRow {
		SimpleStringProperty title;
		SimpleIntegerProperty viewedTimes;
		SimpleIntegerProperty commentedTimes;

		public ThreadRow(String username, int postsViewed, int postsCreated) {
			this.title = new SimpleStringProperty(username);
			this.viewedTimes = new SimpleIntegerProperty(postsViewed);
			this.commentedTimes = new SimpleIntegerProperty(postsCreated);
		}

		public String getTitle() {
			return title.get();
		}


		public int getViewedTimes() {
			return viewedTimes.get();
		}


		public int getCommentedTimes() {
			return commentedTimes.get();
		}

		public void setTitle(String title) {
			this.title.set(title);
		}

		public void setViewedTimes(int viewedTimes) {
			this.viewedTimes.set(viewedTimes);
		}

		public void setCommentedTimes(int commentedTimes) {
			this.commentedTimes.set(commentedTimes);
		}
	}


}
