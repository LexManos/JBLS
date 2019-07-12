package util;

import java.sql.*;
import java.util.ArrayList;

public class Statistics{
  private Database db = null;
  private ArrayList<String> queue = new ArrayList<String>();
  
  public void connect(){
	if(Constants.StatsDatabase.length() < 1 || Constants.StatsUsername.length() < 1){
	  Out.info("Stats", "Could not connect: Username/Database not set");
	  return;
	}
	if(db != null){
	  db.close();
	  db = null;
	}
    db = new Database();
	if(db.connect("jdbc:mysql://" + Constants.StatsServer + "/" + Constants.StatsDatabase, Constants.StatsUsername, Constants.StatsPassword)){
	  Out.info("Stats", "Connected to SQL server");
    }else{
	  Out.info("Stats", "Failed to connect to Database " + Constants.StatsDatabase + " on " + Constants.StatsServer + " as " + Constants.StatsUsername);
	}
	checkStruct();
  }
  private void checkStruct(){
    if(!Constants.StatsCheckSchema) return;
    if(db == null) return;
	try{
	  boolean crev = false, logins = false, connections = false;
	  Statement smnt = db.getStatement();
	  ResultSet r = smnt.executeQuery("SELECT `TABLE_NAME` FROM INFORMATION_SCHEMA.TABLES WHERE `TABLE_SCHEMA` = '" + Constants.StatsDatabase + "'");
	  while(r.next()){
		String name = r.getString("TABLE_NAME");
		if(name.equals("checkrevisions")) crev = true;
		else if(name.equals("botlogins")) logins = true;
		else if(name.equals("connections")) connections = true;
      }
	  r.close();
	  if(!crev){
	    Out.info("Stats", "Checkrevision table does not exist, Creating...");
		smnt.executeUpdate("CREATE TABLE `checkrevisions` (`game` INT NOT NULL, `version` VARCHAR( 255 ) NOT NULL, `time` TIMESTAMP NOT NULL, `ip` VARCHAR( 40 ) NOT NULL, `botID` VARCHAR( 255 ) NOT NULL) ENGINE = MYISAM;");
	  }
	  if(!logins){
	    Out.info("Stats", "Login table does not exist, Creating...");
		smnt.executeUpdate("CREATE TABLE `botlogins` (`time` TIMESTAMP NOT NULL, `ip` VARCHAR( 40 ) NOT NULL, `botID` VARCHAR( 255 ) NOT NULL) ENGINE = MYISAM;");
	  }
	  if(!connections){
	    Out.info("Stats", "Connection table does not exist, Creating...");
		smnt.executeUpdate("CREATE TABLE `connections` (`time` TIMESTAMP NOT NULL, `ip` VARCHAR( 40 ) NOT NULL) ENGINE = MYISAM;");
	  }
	  smnt.close();
	  r = null;
	  smnt = null;
	}catch(SQLException e){
	  Out.error("Stats", "Failed to check Scema: " + e.toString());
	}
  }
  
  public void onBotLogin(String botID, String IP){
    if(!Constants.StatsLogBotIDs) return;
    StringBuffer query = new StringBuffer();
	query.append("INSERT INTO `").append(Constants.StatsDatabase).append("`.`botlogins` ");
	query.append("(`time`,`ip`,`botID`)VALUES(");
	query.append("'").append((new Timestamp(System.currentTimeMillis())).toString()).append("', ");
	query.append("'").append((Constants.StatsLogIps ? db.prepareSQL(IP) : "")).append("', ");
	query.append("'").append(db.prepareSQL(botID)).append("');");
	pushStatement(query.toString());
  }
  
  public void onCheckRevision(String botID, String IP, String archive, int game){
    if(!Constants.StatsLogCRevs) return;
    StringBuffer query = new StringBuffer();
	query.append("INSERT INTO `").append(Constants.StatsDatabase).append("`.`checkrevisions` ");
	query.append("(`game`,`version`,`time`,`ip`,`botID`)VALUES(");
	query.append("'").append(game).append("', ");
	query.append("'").append(db.prepareSQL(archive)).append("', ");
	query.append("'").append((new Timestamp(System.currentTimeMillis())).toString()).append("', ");
	query.append("'").append(db.prepareSQL(IP)).append("', ");
	query.append("'").append(db.prepareSQL(botID)).append("');");
	pushStatement(query.toString());
  }
  public void onConnection(String IP){
    if(!Constants.StatsLogConns) return;
    StringBuffer query = new StringBuffer();
	query.append("INSERT INTO `").append(Constants.StatsDatabase).append("`.`connections` ");
	query.append("(`time`,`ip`)VALUES(");
	query.append("'").append((new Timestamp(System.currentTimeMillis())).toString()).append("', ");
	query.append("'").append(db.prepareSQL(IP)).append("');");
	pushStatement(query.toString());
  }
  
  private void pushStatement(String sql){
	queue.add(sql);
	if(queue.size() >= Constants.StatsQueue){
	  try{
	    Statement smnt = db.getStatement();
	    while(queue.size() > 0){
	      String query = queue.remove(0);
	      try{
		    Out.debug("Stats", "Executing Update");
		    smnt.executeUpdate(query);
	      }catch(SQLException e){
		    Out.error("Stats", "SQL Error: " + e.toString());
            Out.error("Stats", "Failed to execute query: \n" + query);
	      }
	    }
	    smnt.close();
	    smnt = null;
	  }catch(SQLException e){
	    Out.error("Stats", "Failed to create Statement for inserts");
	  }
	}
  }
}