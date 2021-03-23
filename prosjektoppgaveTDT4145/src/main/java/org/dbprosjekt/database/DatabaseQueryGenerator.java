package org.dbprosjekt.database;

import org.dbprosjekt.controllers.Program2Controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

public class DatabaseQueryGenerator extends DBConn {
	public DatabaseQueryGenerator() {
		super();
	}


	public ResultSet query(String queryString) {
		try {
			Statement statement = conn.createStatement();
			return statement.executeQuery(queryString);
		} catch (Exception e) {
			System.out.println(e.getCause());
			return null;
		}
	}

	public ArrayList<String> getSelectResult(ResultSet rs, String... properties) {
		var res = new ArrayList<String>();
		try {
			while (rs.next()) {
				Arrays.asList(properties).forEach(property -> {
					try {
						res.add(rs.getString(property));
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return res;
	}

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

	public boolean currentCourseAllowsAnonymous(){
		try {
			String queryString = "select * from Course where SubjectID='" + Session.getCourseID() + "' and Term='" + Session.getTerm() + "'";
			var rs = query(queryString);
			var data = getSelectResult(rs, "AllowsAnonymous");

			if(data.get(0).equals("1")) {
				return true;
			}

		} catch(Exception e) {
			System.out.println("exception in check for anonymous");
			System.out.println(e);
		}
		return false;
	}

	public String getLastInsertedID() {
		try {
			String query = "select LAST_INSERT_ID() as id";
			var rs = query(query);
			var data = getSelectResult(rs, "id");

			if(data.size() == 0) {
				return null;
			}

			return data.get(0);
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

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

	public void insertUser(String email, String username, String password) {
		try {
			Statement statement = conn.createStatement();
			String queryString = "insert into User(Email, Username, Password, Type) values('" + email + "','" + username + "','" + password + "','student')";
			statement.execute(queryString);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

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


	public void insertSubject(String id, String name){
		try {
			Statement statement = conn.createStatement();
			String queryString = "insert into Subject(SubjectID, name ) values('" + id + "','" + name + "')";
			statement.execute(queryString);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	public void insertCourse(String id, String term, boolean allowsAnonymous){
		try {
			Statement statement = conn.createStatement();
			String queryString = "insert into Course(SubjectID, Term, AllowsAnonymous) values('" + id + "','" + term + "',"+allowsAnonymous+")";
			statement.execute(queryString);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
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
    public void insertInCourse(String email) throws SQLException {
        String queryString = "insert into InCourse (Email, SubjectID, Term) VALUES ('"+email+"','"+Session.getCourseID()+"','"+Session.getTerm()+"')";
        Statement statement = conn.createStatement();
        statement.execute(queryString);
    }
    public void promoteUser(String email) throws SQLException {
        String queryString = "update User set Type='instructor' where Email='"+email+"'";
        Statement statement = conn.createStatement();
        statement.execute(queryString);
    }
    public void removeInCourse(String email) throws SQLException {
        String queryString = "delete from InCourse where Email='"+email+"' and SubjectID='"+Session.getCourseID()+"' and Term='"+Session.getTerm()+"'";
        Statement statement = conn.createStatement();
        statement.execute(queryString);
    }
    public void removeFolder(int folderID) throws SQLException {
        String queryString = "delete from Folder where FolderID='"+folderID+"'";
        Statement statement = conn.createStatement();
        statement.execute(queryString);
    }
    public void renameFolder(int folderID, String name) throws SQLException {
        String queryString = "update Folder set Name='"+name+"' where FolderID='"+folderID+"'";
        Statement statement = conn.createStatement();
        statement.execute(queryString);
    }
    public void removeLike(String email, int postID) throws SQLException {
		String queryString = "delete from UserLikedPost as ULP where ULP.Email='"+email+"' and ULP.PostID='"+postID+"'";
		System.out.println(queryString);
		Statement statement = conn.createStatement();
		statement.execute(queryString);
	}
	public void insertLike(String email, int postID) throws SQLException {
		String queryString = "insert into UserLikedPost (Email, PostID, Date, Time) VALUES('"+email+"','"+postID+"',CURDATE(),CURTIME())";
		Statement statement = conn.createStatement();
		statement.execute(queryString);
	}
	public int getLikes(int postID) throws SQLException {
		String queryString = "select count(distinct Email) as likes from UserLikedPost where PostID='"+postID+"'";
		ResultSet rs = query(queryString);
		rs.next();
		return rs.getInt("Likes");
	}
	public void insertReply(int postID, boolean anonymous, String text, String email) throws SQLException {
		String queryString = "insert into Post (PostID, Text, Date, Time, IsAnonymous, Author) VALUES (null,'"+text+"',CURDATE(),CURTIME(),"+anonymous+",'"+email+"')";
		System.out.println(queryString);
		conn.createStatement().execute(queryString);
		int replyID = Integer.parseInt(getLastInsertedID());
		queryString = "insert into Reply (PostID, ReplyToID) VALUES ("+replyID+","+postID+")";
		System.out.println(queryString);
		conn.createStatement().execute(queryString);
	}
}
