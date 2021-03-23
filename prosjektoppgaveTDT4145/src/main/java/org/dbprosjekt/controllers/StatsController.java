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

public class StatsController {

	@FXML
	TableView usertable;

	@FXML
	TableView threadtable;

	@FXML
	Text activeUsersText;

	@FXML
	public void back() throws SQLException {
		Program2Controller.reload();
	}

	@FXML
	public void initialize() throws SQLException {
		//how many posts created and viewed in total
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

		//most active threads
		var mostViewedThreads = queryGenerator.getActiveThreads();

		ObservableList<ThreadRow> threadData = FXCollections.observableArrayList();

		mostViewedThreads.forEach(thread -> {
			threadData.add(new ThreadRow(thread.get(1), thread.get(2), "halvor"));
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
