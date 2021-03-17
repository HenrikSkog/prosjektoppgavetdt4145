package org.dbprosjekt.database;

import java.sql.ResultSet;
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
    try {
      if (rs.isBeforeFirst()) {
        return true;
      }
      return false;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
