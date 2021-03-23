package org.dbprosjekt.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.dbprosjekt.database.DatabaseQueryGenerator;

import java.sql.SQLException;

public class StatsController {

	@FXML
	TableView table;

	@FXML
	public void back() throws SQLException {
		Program2Controller.reload();
	}

	@FXML
	public void initialize() {
		var queryGenerator = new DatabaseQueryGenerator();
		var stats = queryGenerator.getStats();

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

		table.setItems(data);
		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		table.getColumns().addAll(username, postsViewed, postsCreated);


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



}
