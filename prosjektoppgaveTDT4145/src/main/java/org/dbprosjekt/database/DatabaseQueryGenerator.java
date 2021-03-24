package org.dbprosjekt.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

//Denne klassen inneholder metoder relatert til querying
public class DatabaseQueryGenerator extends DBConn {
	//Connecter til databasen
	public DatabaseQueryGenerator() {
		super();
	}


	//Tar inn en query, sender denne til databasen og returnerer resultatet
	public ResultSet query(String queryString) {
		try  {
			Statement statement = conn.createStatement();
			return statement.executeQuery(queryString);
		} catch (Exception e) {
			System.out.println("exception in query method with string " + queryString);
			System.out.println(e.getCause());
			return null;
		}
	}

	//Henter ut gitte attributter fra et ResultSet og returner en tabell i form av en 2d ArrayList
	public ArrayList<ArrayList<String>> getSelectResult(ResultSet rs, String... properties) {
		var res = new ArrayList<ArrayList<String>>();
		try {
			while (rs.next()) {
				var row = new ArrayList<String>();
				Arrays.asList(properties).forEach(property -> {
					try {
						row.add(rs.getString(property));
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				res.add(row);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		try {
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}

	//Returner true hvis en query gir resultater, ellers false
	public boolean queryHasResultRows(String queryString) {
		var rs = query(queryString);
		if (rs == null)
			return false;
		try {
			if (rs.isBeforeFirst()) {
				return true;
			}
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return false;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	//Queryer databasen og finner antall brukerere som har vært aktive i dag
	public String getDailyActiveUsers() {
		String queryString = "select count(distinct Email) as num from UserViewedThread u where date(u.date) = date(curdate())";
		var rs = query(queryString);
		var data = getSelectResult(rs, "num");

		if (data.get(0).size() == 0) {
			System.out.println("Excepton in getDailyActiveUsers, no data");
			return "0";
		}
		return data.get(0).get(0);
	}

	//Returnerer en tabell med username, antall posts brukeren med denne emailen har sett og antallet vedkommende har opprettet
	public ArrayList<ArrayList<String>> getTotalUserStats() throws SQLException {

		String queryString = "SELECT q1.Email, viewedPosts, createdPosts FROM (select User.email as Email, count(U.Email) as viewedPosts from User left outer join UserViewedThread U on User.Email = U.Email group by User.Email) q1 left outer join (select Author as Email, count(*) as createdPosts from Post group by Author) q2 on q1.Email = q2.Email order by viewedPosts desc;";

		var rs = query(queryString);
		var data = getSelectResult(rs, "Email", "viewedPosts", "createdPosts");

		//getting usernames
		var usersRS = query("select username, email from User");
		var users = new HashMap<String, String>();
		while (usersRS.next()) {
			String username = usersRS.getString("username");
			String email = usersRS.getString("email");
			users.put(email, username);
		}

		//removing nulls from result and switching emails with usernames
		for (int i = 0; i < data.size(); i++) {
			data.get(i).set(0, users.get(data.get(i).get(0)));
			for (int j = 0; j < data.get(i).size(); j++) {
				if (data.get(i).get(j) == null)
					data.get(i).set(j, "0");
			}
		}

		return data;
	}

	//Returnerer en tabell med id tittel antall views og antall replies for hver thread
	public ArrayList<ArrayList<String>> getActiveThreads() {
		//get views on posts
		String queryString = "select TP.PostID, TP.Title, count(*) as num from ThreadPost TP join UserViewedThread UVT on TP.PostID = UVT.PostID group by TP.PostID;";
		var rs = query(queryString);
		var data = getSelectResult(rs, "PostID", "Title", "num");
		return data;
	}

	//Returnerer et hashmap med hvor mange replies hver thread har
	public HashMap<Integer, Integer> getMostRepliedToThreads() throws SQLException {
		String queryString = "select * from ThreadPost as TP inner join Post as P on TP.PostID = P.PostID";
		ResultSet rs2 = query(queryString);
		HashMap<Integer, Integer> repliesToPost = new HashMap<>();
		while (rs2.next()) {
			int replies = getThreadSize(rs2.getInt("P.PostID"), 1) - 1;
			repliesToPost.put(rs2.getInt("P.PostID"), replies);
		}
		return repliesToPost;
	}

	//Returnerer antall posts i en gitt thread
	private int getThreadSize(int postID, int depth) throws SQLException {
		String queryString = "select * from Reply where ReplyToID='" + postID + "'";
		if (!queryHasResultRows(queryString))
			return 1;
		int sum = 0;
		ResultSet rs = query(queryString);
		while (rs.next()) {
			sum += getThreadSize(rs.getInt("PostID"), depth + 1);
		}
		return sum + 1;
	}

	//Returnerer IDen til  alle threadposts i en gitt folder
	private ArrayList<Integer> getPostIDsInFolder(String FolderID) {
		try {
			var rs = query("select ThreadPost.PostID from ThreadPost join ThreadInFolder TIF on ThreadPost.PostID = TIF.PostID where TIF.FolderID=" + FolderID);
			var data = getSelectResult(rs, "PostID");

			var onlyIds = (ArrayList<Integer>) data.stream().map(row -> Integer.parseInt(row.get(0))).collect(Collectors.toList());

			return onlyIds;
		} catch (Exception e) {
			System.out.println("exception in getting threadpost ids from folder");
			System.out.println(e.getMessage());
			return null;
		}
	}

	//Inserter i UserViewedThread for alle threads i en gitt folder
	public void insertThreadPostsViewedByUser(String FolderID) {
		try (PreparedStatement ps = conn.prepareStatement(buildInsertQuery("UserViewedThread", "Email", "CURDATE()", "CURTIME()", "PostID"))) {
			var ids = getPostIDsInFolder(FolderID);
			var email = Session.getUserID();

			for (int id : ids) {
				ps.setString(1, email);
				ps.setInt(2, id);
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			System.out.println("exception in inserting threadposts viewed by user");
			e.printStackTrace();
		}

	}

	//Returnerer true dersom courset programmet befinner seg i tillater anonyme posts, ellers false
	public boolean currentCourseAllowsAnonymous() {
		try {
			String queryString = "select * from Course where SubjectID='" + Session.getCourseID() + "' and Term='" + Session.getTerm() + "'";
			var rs = query(queryString);
			var data = getSelectResult(rs, "AllowsAnonymous");

			if (data.get(0).get(0).equals("1")) {
				return true;
			}

		} catch (Exception e) {
			System.out.println("exception in check for anonymous");
			System.out.println(e);
		}
		return false;
	}

	//Returnerer true dersom det finnes en threadpost med IDen
	public boolean threadPostExists(String postID) {
		try {
			var returnVal = queryHasResultRows("select PostID from ThreadPost where PostID=" + postID);
			return returnVal;
		} catch (Exception e) {
			System.out.println("Exception in thread post exists");
			System.out.println(e.getMessage());
		}
		return true;
	}

	//Returnerer IDen til raden som sist ble innsatt i databasen, dette benyttes f.eks. ved oppretting av nye posts der flere tabeller i databasen må oppdateres
	public String getLastInsertedID() {
		try {
			String query = "select LAST_INSERT_ID() as id";
			var rs = query(query);
			var data = getSelectResult(rs, "id");

			if (data.size() == 0) {
				return null;
			}

			return data.get(0).get(0);
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	//Returnerer en postID som er linket gjennom PostLink
	public String getLinkedPost(String PostID) {
		try {
			String queryString = "select LinkID from PostLink where PostID=" + PostID;
			var rs = query(queryString);
			var data = getSelectResult(rs, "LinkID");
			try {
				return data.get(0).get(0);
			} catch (IndexOutOfBoundsException e) {
				return null;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	//returnerer en prepared sql insert query
	private String buildInsertQuery(String table, String... values) {
		String start = "insert into " + table + " (";
		for (String val : Arrays.asList(values)) {
			if (val.equals("CURTIME()")) {
				start += "Time,";
			} else if (val.equals("CURDATE()")) {
				start += "Date,";
			} else {
				start += val + ",";
			}
		}
		start = start.substring(0, start.length() - 1) + ") VALUES(";
		for (int i = 0; i < values.length; i++) {
			if (values[i].equals("CURTIME()")) {
				start += "CURTIME(),";
			} else if (values[i].equals("CURDATE()")) {
				start += "CURDATE(),";
			} else {
				start += "?,";
			}
		}
		return start.substring(0, start.length() - 1) + ")";
	}

	//Inserter en bruker i databasen
	public void insertUser(String email, String username, String password) {
		try (PreparedStatement ps = conn.prepareStatement(buildInsertQuery("User", "Email", "Username", "Password", "Type"))) {
			ps.setString(1, email);
			ps.setString(2, username);
			ps.setString(3, password);
			ps.setString(4, "student");
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//Inserter en Threadpost i databasen
	public void insertThreadPost(String title, String text, String tag, boolean isAnonymous) {
		//insert post
		try (PreparedStatement ps = conn.prepareStatement(buildInsertQuery("Post", "Text", "CURDATE()", "CURTIME()", "isAnonymous", "Author"))) {
			ps.setString(1, text);
			ps.setBoolean(2, isAnonymous);
			ps.setString(3, Session.getUserID());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//get row id
		String id = getLastInsertedID();

		//insert thread post
		try (PreparedStatement ps = conn.prepareStatement(buildInsertQuery("ThreadPost", "PostID", "Tag", "Title"))) {
			ps.setString(1, id);
			ps.setString(2, tag);
			ps.setString(3, title);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		//insert thread in folder
		try (PreparedStatement ps = conn.prepareStatement(buildInsertQuery("ThreadInFolder", "FolderID", "PostID"))) {
			ps.setInt(1, Session.getCurrentFolderID());
			ps.setInt(2, Integer.parseInt(id));
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//Inserter en relasjon mellom to posts der den ene linker til den andre
	public void insertPostLink(int fromPostID, int toPostID) {
		try (PreparedStatement ps = conn.prepareStatement(buildInsertQuery("PostLink", "PostID", "LinkID"))) {
			ps.setInt(1, fromPostID);
			ps.setInt(2, toPostID);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	//Inserter et subject i databasen
	public void insertSubject(String id, String name) {
		try (PreparedStatement ps = conn.prepareStatement(buildInsertQuery("Subject", "SubjectID", "name"))) {
			ps.setString(1, id);
			ps.setString(2, name);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//Inserter et nytt course
	public void insertCourse(String id, String term, boolean allowsAnonymous) {
		try (PreparedStatement ps = conn.prepareStatement(buildInsertQuery("Course", "SubjectID", "Term", "AllowsAnonymous"))) {
			ps.setString(1, id);
			ps.setString(2, term);
			ps.setBoolean(3, allowsAnonymous);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//Inserter en ny folder
	public void insertFolder(String name) {
		try (PreparedStatement ps = conn.prepareStatement(buildInsertQuery("Folder", "Name", "ParentID", "SubjectID", "Term"))) {
			ps.setString(1, name);
			if (Session.getCurrentFolderID() == 0) {
				ps.setNull(2, Types.NULL);
				ps.setString(3, Session.getCourseID());
				ps.setString(4, Session.getTerm());
			} else {
				ps.setInt(2, Session.getCurrentFolderID());
				ps.setNull(3, Types.NULL);
				ps.setNull(4, Types.NULL);
			}
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//Inserter en ny relasjon mellom user og course
	public void insertInCourse(String email, String courseID, String term) {
		try (PreparedStatement ps = conn.prepareStatement(buildInsertQuery("InCourse", "Email", "SubjectID", "Term"))) {
			ps.setString(1, email);
			ps.setString(2, courseID);
			ps.setString(3, term);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//Endrer type for en bruker fra student til instructor
	public void promoteUser(String email) {
		try (PreparedStatement ps = conn.prepareStatement("update User set Type='instructor' where Email=?")) {
			ps.setString(1, email);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//Fjerner en relasjon mellom en bruker og et course
	public void removeInCourse(String email) {
		try (PreparedStatement ps = conn.prepareStatement("delete from InCourse where Email=? and SubjectID=? and Term=?")) {
			ps.setString(1, email);
			ps.setString(2, Session.getCourseID());
			ps.setString(3, Session.getTerm());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//Sletter en mappe
	public void removeFolder(int folderID) {
		try (PreparedStatement ps = conn.prepareStatement("delete from Folder where FolderID=?")) {
			ps.setInt(1, folderID);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//Endrer navnet til en mappe
	public void renameFolder(int folderID, String name) {
		try (PreparedStatement ps = conn.prepareStatement("update Folder set Name=? where FolderID=?")) {
			ps.setString(1, name);
			ps.setInt(2, folderID);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//Fjerner en Like-relasjon mellom en bruker og en post
	public void removeLike(String email, int postID) {
		try (PreparedStatement ps = conn.prepareStatement("delete from UserLikedPost as ULP where ULP.Email=? and ULP.PostID=?")) {
			ps.setString(1, email);
			ps.setInt(2, postID);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//Inserter en like-relasjon mellom en bruker og en post
	public void insertLike(String email, int postID) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement("insert into UserLikedPost(Email, PostID, Date, Time) VALUES (?, ?, CURDATE(), CURTIME())")) {
			ps.setString(1, email);
			ps.setInt(2, postID);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//Returnerer antall likes på en post
	public int getLikes(int postID) throws SQLException {
		String queryString = "select count(distinct Email) as likes from UserLikedPost where PostID='" + postID + "'";
		ResultSet rs = query(queryString);
		rs.next();
		return rs.getInt("Likes");
	}

	//Inserter en ny reply
	public void insertReply(int postID, boolean anonymous, String text, String email) {
		//insert post
		try (PreparedStatement ps = conn.prepareStatement("insert into Post (Text, Date, Time, IsAnonymous, Author) VALUES (?, CURDATE(), CURTIME(), ?, ?)")) {
			ps.setString(1, text);
			ps.setBoolean(2, anonymous);
			ps.setString(3, email);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		//get reply id
		int replyID = Integer.parseInt(getLastInsertedID());

		//insert reply
		try (PreparedStatement ps = conn.prepareStatement("insert into Reply (PostID, ReplyToID) VALUES (?, ?)")) {
			ps.setInt(1, replyID);
			ps.setInt(2, postID);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

