package org.dbprosjekt.database;

import org.dbprosjekt.controllers.Program2Controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

//Denne klassen inneholder metoder relatert til querying
public class DatabaseQueryGenerator extends DBConn {
	//Connecter til databasen
	public DatabaseQueryGenerator() {
		super();
	}


	//Tar inn en query, sender denne til databasen og returnerer resultatet
	public ResultSet query(String queryString) {
		try {
			Statement statement = conn.createStatement();
			return statement.executeQuery(queryString);
		} catch (Exception e) {
			System.out.println("exception in query method with string "+queryString);
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
		return res;
	}

	//Returner true hvis en query gir resultater, ellers false
	public boolean queryHasResultRows(String queryString) {
		var rs = query(queryString);
		if (rs==null)
			return false;
		try {
			if (rs.isBeforeFirst()) {
				return true;
			}
			return false;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	//Queryer databasen og finner antall brukerere som har vært aktive i dag
	public String getDailyActiveUsers() {
		String queryString = "select count(*) as num from UserViewedThread u where date(u.date) = date(curdate())";
		var rs = query(queryString);
		var data = getSelectResult(rs, "num");

		if(data.get(0).size() == 0) {
			System.out.println("Excepton in getDailyActiveUsers, no data");
			return "0";
		}
		return data.get(0).get(0);
	}

	//Returnerer en tabell med email, antall posts brukeren med denne emailen har sett og antallet vedkommende har opprettet
	public ArrayList<ArrayList<String>> getTotalUserStats() {
		String queryString = "SELECT q1.Email, viewedPosts, createdPosts FROM (select User.email as Email, count(U.Email) as viewedPosts from User left outer join UserViewedThread U on User.Email = U.Email group by User.Email) q1 left outer join (select Author as Email, count(*) as createdPosts from Post group by Author) q2 on q1.Email = q2.Email order by viewedPosts desc;";

		var rs = query(queryString);
		var data = getSelectResult(rs, "Email", "viewedPosts", "createdPosts");

//		removing nulls from result
		for (int i = 0; i < data.size(); i++) {
			for (int j = 0; j < data.get(i).size(); j++) {
				if(data.get(i).get(j) == null)
					data.get(i).set(j, "0");
			}
		}

		return data;
	}

	//Returnerer en tabell med id tittel antall views og antall replies for hver thread
	public ArrayList<ArrayList<String>> getActiveThreads() throws SQLException {
		String queryString = "select TP.PostID, TP.Title, count(*) as num from ThreadPost TP join UserViewedThread UVT on TP.PostID = UVT.PostID group by TP.PostID;";

		var rs = query(queryString);

		queryString = "select * from ThreadPost as TP inner join Post as P on TP.PostID = P.PostID";
		ResultSet rs2 = query(queryString);
		HashMap<Integer, Integer> repliesToPost = new HashMap<>();
		while (rs2.next()){
			int replies = getThreadSize(rs2.getInt("P.PostID"),1)-1;
			System.out.println(rs2.getInt("P.PostID")+", "+replies);
			repliesToPost.put(rs2.getInt("P.PostID"), replies);
		}
		var data = getSelectResult(rs, "PostID", "Title", "num");

		return data;
	}

	//Returnerer antall posts i en gitt thread
	private int getThreadSize(int postID, int depth) throws SQLException {
		String queryString = "select * from Reply where ReplyToID='"+postID+"'";
		if(!queryHasResultRows(queryString))
			return 1;
		int sum = 0;
		ResultSet rs = query(queryString);
		while(rs.next()){
			sum+=getThreadSize(rs.getInt("PostID"),depth+1);
		}
		return sum+1;
	}

	//Returnerer IDen til  alle threadposts i en gitt folder
	private ArrayList<String> getPostIDsInFolder(String FolderID) {
		try {
		var rs = query("select ThreadPost.PostID from ThreadPost join ThreadInFolder TIF on ThreadPost.PostID = TIF.PostID where TIF.FolderID=" + FolderID);
		var data = getSelectResult(rs, "PostID");

		var onlyIds = (ArrayList<String>) data.stream().map(row -> row.get(0)).collect(Collectors.toList());

		return onlyIds;
		} catch(Exception e) {
			System.out.println("exception in getting threadpost ids from folder");
			System.out.println(e.getMessage());
			return null;
		}
	}

	//Inserter i UserViewedThread for alle threads i en gitt folder
	public void insertThreadPostsViewedByUser(String FolderID) {
		try {
			var ids = getPostIDsInFolder(FolderID);
			var userID = Session.getUserID();
			Statement statement = conn.createStatement();
			for (String id: ids) {
				String query = buildInsert("UserViewedThread", userID, "CURDATE()", "CURTIME()", id);
				statement.execute(query);
			}

		} catch(Exception e) {
			System.out.println("exception in inserting threadposts viewed by user");
			System.out.println(e.getMessage());
		}

	}

	//Returnerer true dersom courset programmet befinner seg i tillater anonyme posts, ellers false
	public boolean currentCourseAllowsAnonymous(){
		try {
			String queryString = "select * from Course where SubjectID='" + Session.getCourseID() + "' and Term='" + Session.getTerm() + "'";
			var rs = query(queryString);
			var data = getSelectResult(rs, "AllowsAnonymous");

			if(data.get(0).get(0).equals("1")) {
				return true;
			}

		} catch(Exception e) {
			System.out.println("exception in check for anonymous");
			System.out.println(e);
		}
		return false;
	}

	//Returnerer IDen til raden som sist ble innsatt i databasen, dette benyttes f.eks. ved oppretting av nye posts der flere tabeller i databasen må oppdateres
	public String getLastInsertedID() {
		try {
			String query = "select LAST_INSERT_ID() as id";
			var rs = query(query);
			var data = getSelectResult(rs, "id");

			if(data.size() == 0) {
				return null;
			}

			return data.get(0).get(0);
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	//Returnerer en insert-statement med gitte verdier
	public String buildInsert(String table, String... values) {
		String start = "insert into " + table + " values(";
		for(String val: Arrays.asList(values)) {
			if(val == null) {
				start += "null,";
			} else if(val == "CURDATE()" || val == "CURTIME()" || val.matches("-?\\d+(\\.\\d+)?")) {
				start += val + ",";
			} else {
				start += "'" + val + "',";
			}
		};
		start = start.substring(0, start.length()-1) + ")";
		return start;
	}

	//Inserter en bruker i databasen
	public void insertUser(String email, String username, String password) {
		try {
			Statement statement = conn.createStatement();
			String queryString = "insert into User(Email, Username, Password, Type) values('" + email + "','" + username + "','" + password + "','student')";
			statement.execute(queryString);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	//Inserter en Threadpost i databasen
	public void insertThreadPost(String title, String text, String tag, int isAnonymous) {
		try {
			var statement = conn.createStatement();

//            insert post row
			String insertPostQueryString = buildInsert("Post", null, text, "CURDATE()", "CURTIME()", Integer.toString(isAnonymous), Session.getUserID());
			statement.execute(insertPostQueryString);

//            get row id
			String id = getLastInsertedID();

//            insert thread post
			String insertThreadPostQueryString = buildInsert("ThreadPost", id, tag, title);
			statement.execute(insertThreadPostQueryString);


//            insert thread in folder
			String insertThreadInFolderQueryString = buildInsert("ThreadInFolder", Integer.toString(Session.getCurrentFolderID()), id);


			statement.execute(insertThreadInFolderQueryString);

		} catch (Exception e) {
			System.out.println("exception in post insert");
			System.out.println(e.getMessage());
		}
	}


	//Inserter et subject i databasen
	public void insertSubject(String id, String name){
		try {
			Statement statement = conn.createStatement();
			String queryString = "insert into Subject(SubjectID, name ) values('" + id + "','" + name + "')";
			statement.execute(queryString);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	//Inserter et nytt course
	public void insertCourse(String id, String term, boolean allowsAnonymous){
		try {
			Statement statement = conn.createStatement();
			String queryString = "insert into Course(SubjectID, Term, AllowsAnonymous) values('" + id + "','" + term + "',"+allowsAnonymous+")";
			statement.execute(queryString);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	//Inserter en ny folder
	public void insertFolder(String name){
		String queryString = "";
		try {
			String term = Session.getTerm();
			String courseID = Session.getCourseID();
			Statement statement = conn.createStatement();
			if(Session.getCurrentFolderID()==0){
				queryString = "insert into Folder(FolderID,Name,ParentID,SubjectID,Term) values(null,'"+name+"',null,'"+courseID+"','"+term+"')";
			}
			else{
				queryString = "insert into Folder(FolderID,Name,ParentID,SubjectID,Term) values(null,'"+name+"','"+Session.getCurrentFolderID()+"',null,null)";
			}
			statement.execute(queryString);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	//Inserter en ny relasjon mellom user og course
    public void insertInCourse(String email, String courseID, String term) throws SQLException {
        String queryString = "insert into InCourse (Email, SubjectID, Term) VALUES ('"+email+"','"+courseID+"','"+term+"')";
        Statement statement = conn.createStatement();
        statement.execute(queryString);
    }
    //Endrer type for en bruker fra student til instructor
    public void promoteUser(String email) throws SQLException {
        String queryString = "update User set Type='instructor' where Email='"+email+"'";
        Statement statement = conn.createStatement();
        statement.execute(queryString);
    }
    //Fjerner en relasjon mellom en bruker og et course
    public void removeInCourse(String email) throws SQLException {
        String queryString = "delete from InCourse where Email='"+email+"' and SubjectID='"+Session.getCourseID()+"' and Term='"+Session.getTerm()+"'";
        Statement statement = conn.createStatement();
        statement.execute(queryString);
    }
    //Sletter en mappe
    public void removeFolder(int folderID) throws SQLException {
        String queryString = "delete from Folder where FolderID='"+folderID+"'";
        Statement statement = conn.createStatement();
        statement.execute(queryString);
    }
    //Endrer navnet til en mappe
    public void renameFolder(int folderID, String name) throws SQLException {
        String queryString = "update Folder set Name='"+name+"' where FolderID='"+folderID+"'";
        Statement statement = conn.createStatement();
        statement.execute(queryString);
    }
    //Fjerner en Like-relasjon mellom en bruker og en post
    public void removeLike(String email, int postID) throws SQLException {
		String queryString = "delete from UserLikedPost as ULP where ULP.Email='"+email+"' and ULP.PostID='"+postID+"'";
		Statement statement = conn.createStatement();
		statement.execute(queryString);
	}
	//Inserter en like-relasjon mellom en bruker og en post
	public void insertLike(String email, int postID) throws SQLException {
		String queryString = "insert into UserLikedPost (Email, PostID, Date, Time) VALUES('"+email+"','"+postID+"',CURDATE(),CURTIME())";
		Statement statement = conn.createStatement();
		statement.execute(queryString);
	}
	//Returnerer antall likes på en post
	public int getLikes(int postID) throws SQLException {
		String queryString = "select count(distinct Email) as likes from UserLikedPost where PostID='"+postID+"'";
		ResultSet rs = query(queryString);
		rs.next();
		return rs.getInt("Likes");
	}
	//Inserter en ny reply
	public void insertReply(int postID, boolean anonymous, String text, String email) throws SQLException {
		String queryString = "insert into Post (PostID, Text, Date, Time, IsAnonymous, Author) VALUES (null,'"+text+"',CURDATE(),CURTIME(),"+anonymous+",'"+email+"')";
		conn.createStatement().execute(queryString);
		int replyID = Integer.parseInt(getLastInsertedID());
		queryString = "insert into Reply (PostID, ReplyToID) VALUES ("+replyID+","+postID+")";
		conn.createStatement().execute(queryString);
	}

	public static void main(String[] args) {
//		var test = new DatabaseQueryGenerator();
//		var test1 = test.getTotalUserStats();
//
	}
}

