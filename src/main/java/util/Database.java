package util;

import java.sql.*;

public class Database{
  private String dbURL=null;
  private String username=null;
  private String password=null;
  private Connection conn=null;

  public boolean connect(String sURL, String username, String password){
    try{  
	  this.username = username;
	  this.password = password;
	  this.dbURL = sURL;
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      conn = DriverManager.getConnection(sURL, username, password);
	  return true;
	}catch(Exception e){
	  Out.error("SQL", e.toString());
	  return false;
    }
  }
  
  public Statement getStatement() throws SQLException{
    return conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
  }
  public PreparedStatement getPreparedStatement(String sql) throws SQLException{
    return conn.prepareStatement(sql);
  }
  
  public void close(){
    try{
      if(conn != null && !conn.isClosed()) conn.close();
	}catch(Exception e){
	  Out.error("SQL", "Failed to close connection: " + e.toString());
	}
  }
  
  public String prepareSQL(String command){
    StringBuffer s = new StringBuffer();
	if(command == null) command = "";
    command = command.replaceAll("\\*", "%");
    for(int x = 0; x < command.length(); x++){
      if(command.charAt(x) == '\'') s.append("\\");
      s.append(command.charAt(x));
    }
    return s.toString();
  }
  
}