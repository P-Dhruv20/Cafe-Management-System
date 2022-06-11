/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Cafe {

   private static String authorisedUser = null;

   private static String createdAt = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
         new InputStreamReader(System.in));

   /**
    * Creates a new instance of Cafe
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Cafe(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try {
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      } catch (Exception e) {
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      } // end catch
   }// end Cafe

   /**
    * Method to execute an update SQL statement. Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate(String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the update instruction
      stmt.executeUpdate(sql);

      // close the instruction
      stmt.close();
   }// end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT). This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult(String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery(query);

      /*
       ** obtains the metadata object for the returned result set. The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData();
      int numCol = rsmd.getColumnCount();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()) {
         if (outputHeader) {
            for (int i = 1; i <= numCol; i++) {
               System.out.print(rsmd.getColumnName(i) + "\t");
            }
            System.out.println();
            outputHeader = false;
         }
         for (int i = 1; i <= numCol; ++i)
            System.out.print(rs.getString(i) + "\t");
         System.out.println();
         ++rowCount;
      } // end while
      stmt.close();
      return rowCount;
   }// end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT). This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult(String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery(query);

      /*
       ** obtains the metadata object for the returned result set. The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData();
      int numCol = rsmd.getColumnCount();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result = new ArrayList<List<String>>();
      while (rs.next()) {
         List<String> record = new ArrayList<String>();
         for (int i = 1; i <= numCol; ++i)
            record.add(rs.getString(i));
         result.add(record);
      } // end while
      stmt.close();
      return result;
   }// end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT). This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery(String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery(query);

      int rowCount = 0;

      // iterates through the result set and count nuber of results.
      while (rs.next()) {
         rowCount++;
      } // end while
      stmt.close();
      return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
      Statement stmt = this._connection.createStatement();

      ResultSet rs = stmt.executeQuery(String.format("Select currval('%s')", sequence));
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup() {
      try {
         if (this._connection != null) {
            this._connection.close();
         } // end if
      } catch (SQLException e) {
         // ignored.
      } // end try
   }// end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login
    *             file>
    */

   public static void main(String[] args) {
      if (args.length != 3) {
         System.err.println(
               "Usage: " +
                     "java [-classpath <classpath>] " +
                     Cafe.class.getName() +
                     " <dbname> <port> <user>");
         return;
      }

      Greeting();
      Cafe esql = null;
      try {
         Class.forName("org.postgresql.Driver").newInstance();
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Cafe(dbname, dbport, user, "");

         boolean keepon = true;
         while (keepon) {
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            authorisedUser = null;
            switch (readChoice()) {
               case 1:
                  CreateUser(esql);
                  break;
               case 2:
                  authorisedUser = LogIn(esql);
                  break;
               case 9:
                  keepon = false;
                  break;
               default:
                  System.out.println("Unrecognized choice!");
                  break;
            }
            if (authorisedUser != null) {
               boolean usermenu = true;
               String usertype = UserType(esql);
               switch (usertype) {
                  case "Customer":
                     while (usermenu) {
                        System.out.println("MAIN MENU for Customer");
                        System.out.println("---------");
                        System.out.println("0. View menu");
                        System.out.println("1. Search Menu by Item Name");
                        System.out.println("2. Search Menu by Item Type");
                        System.out.println("3. Place an order");
                        System.out.println("4. Update order");
                        System.out.println("5. View order history");
                        System.out.println("6. View order status");
                        System.out.println("7. Update user information");
                        System.out.println(".........................");
                        System.out.println("9. Log out");
                        switch (readChoice()) {
                           case 0:
                              Menu(esql);
                              break;
                           case 1:
                              SearchMenuByName(esql);
                              break;
                           case 2:
                              SearchMenuByType(esql);
                              break;
                           case 3:
                              PlaceOrder(esql);
                              break;
                           case 4:
                              UpdateOrder(esql);
                              break;
			   case 5:
				ViewOrderHistory(esql);
				break;
			   case 6:
				ViewOrderStatus(esql);
				break;
                           case 9:
                              usermenu = false;
                              break;
                           default:
                              System.out.println("Unrecognized choice!");
                              break;
                        }
                     }
                     break;
                  case "Employee":
                     while (usermenu) {
                        System.out.println("MAIN MENU for employee");
                        System.out.println("---------");
                        System.out.println("0. View menu");
                        System.out.println("1. Search Menu by Item Name");
                        System.out.println("2. Search Menu by Item Type");
                        System.out.println("3. Place an order");
                        System.out.println("4. Update order");
                        System.out.println("5. View Current Orders");
                        System.out.println("6. View Order Status");
                        System.out.println("7. Update user information");
                        System.out.println(".........................");
                        System.out.println("9. Log out");
                        switch (readChoice()) {
                           case 0:
                              Menu(esql);
                              break;
                           case 1:
                              SearchMenuByName(esql);
                              break;
                           case 2:
                              SearchMenuByType(esql);
                              break;
                           case 3:
                              PlaceOrder(esql);
                              break;
			  case 6:
				ViewOrderStatus(esql);
				break;
                           case 9:
                              usermenu = false;
                              break;
                           default:
                              System.out.println("Unrecognized choice!");
                              break;
                        }
                     }
                     break;
                  case "Manager ":
                     while (usermenu) {
                        System.out.println("MAIN MENU for manager");
                        System.out.println("---------");
                        System.out.println("0. View menu");
                        System.out.println("1. Search Menu by Item Name");
                        System.out.println("2. Search Menu by Item Type");
                        System.out.println("3. Place an order");
                        System.out.println("4. Update order");
                        System.out.println("5. View Current Orders");
                        System.out.println("6. View Order Status");
                        System.out.println("7. Update user information");
                        System.out.println("8. Update menu");
                        System.out.println(".........................");
                        System.out.println("9. Log out");
                        switch (readChoice()) {
                           case 0:
                              Menu(esql);
                              break;
                           case 1:
                              SearchMenuByName(esql);
                              break;
                           case 2:
                              SearchMenuByType(esql);
                              break;
                           case 3:
                              PlaceOrder(esql);
                              break;
			   case 6:
				ViewOrderStatus(esql);
				break;
                           case 9:
                              usermenu = false;
                              break;
                           default:
                              System.out.println("Unrecognized choice!");
                              break;
                        }
                     }
                     break;
               }
            }
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      } finally {
         try {
            if (esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup();
               System.out.println("Done\n\nBye !");
            }
         } catch (Exception e) {
         }
      }
   }

    public static void Greeting() {
      System.out.println(
            "\n\n*******************************************************\n" +
                  "              User Interface      	               \n" +
                  "*******************************************************\n");
   }// end Greeting

   /*
    * Reads the users choice given from the keyboard
    * 
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         } catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         } // end try
      } while (true);
      return input;
   }// end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    **/
   public static void CreateUser(Cafe esql) {
      try {
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();

         String type = "Customer";
         String favItems = "";

         String query = String.format(
               "INSERT INTO USERS (phoneNum, login, password, favItems, type) VALUES ('%s','%s','%s','%s','%s')", phone,
               login, password, favItems, type);

         esql.executeUpdate(query);
         System.out.println("User successfully created!");
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }// end CreateUser

   /*
    * Check log in credentials for an existing user
    * 
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Cafe esql) {
      try {
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
         if (userNum > 0)
            return login;
         return null;
      } catch (Exception e) {
         System.err.println(e.getMessage());
         return null;
      }
   }// end

   // Rest of the functions definition go in here

   public static String UserType(Cafe esql) {
      String type;
      try {
         String query = String.format("SELECT type FROM Users WHERE login = '%s';", authorisedUser);
         List<List<String>> result = esql.executeQueryAndReturnResult(query);
         if (result.size() > 0) {
            type = result.get(0).get(0);
         } else {
            System.err.println("Error: User not found");
            return null;
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
         return null;
      }
      return type;
   }

   public static void Menu(Cafe esql) {
      try {
         String query = "SELECT * FROM Menu";

         int rowCount = esql.executeQueryAndPrintResult(query);

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void SearchMenuByName(Cafe esql) {
      try {
         String query = "SELECT * FROM Menu WHERE itemName= ";
         System.out.print("\tEnter itemName: ");
         String input = in.readLine();
         input = "'" + input + "';";
         query += input;

         int rowCount = esql.executeQueryAndPrintResult(query);
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void SearchMenuByType(Cafe esql) {
      try {
         String query = "SELECT * FROM Menu WHERE type= ";
         System.out.print("\tEnter type: ");
         String input = in.readLine();
         input = "'" + input + "';";
         query += input;

         int rowCount = esql.executeQueryAndPrintResult(query);
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void UpdateProfile(Cafe esql) {
   }

   /*
    * while isOrdering = true first create entry in Order table(only once)
    * // While not case 9, (loop)
    * // Run a query on Order table to fetch the current OrderID, store this in
    * java
    * // USE getCurrSeqVal() somehow to get the current value of the OrderID
    * // Prompt which items they would like to order
    * // store in temp string var
    * // Run SQL query to check if item is valid
    * // If true ask how many of that item they would like
    * // store in temp int var
    * // push item name and quantity into vector of vector
    * // repeat process until user indicates they're done ordering
    * // end input loop
    * // USER IS DONE PLACING ORDER
    * // Pop entries from vector, insert into ItemStatus table
    * // don't forget to add lastupdated time for each entry
    * // insert timeStampRecieved in Order table
    * // calculate total
    */

  public static Integer PlaceOrder(Cafe esql) {
      /* declare variables */
      boolean isOrdering = true;
      boolean orderPlaced = false;
      Integer orderid = 0;
      int querySize;
      String query;
      String item;
      float price;
      float OrderTotal = 0;

      try {
         while (isOrdering) {
            System.out.println("\nPLACE AN ORDER");
            System.out.println("----------------");
            System.out.println("0. View Menu");
            System.out.println("1. Place order");
            System.out.println("2. Add an item to created order (if exists already)");
            System.out.println("---------------------------------");
            System.out.println("9. Finish Ordering");

            switch (readChoice()) {
               case 0:
                  Menu(esql);
                  break;
               case 1:
                  System.out.println("Please enter the ITEM NAME you would like to add to your order");
                  System.out.println(
                        "Note: item names are case sensitive and must be spelled correctly, refer to menu if needed");
                  System.out.print("Enter item name: ");
                  item = in.readLine();
                  System.out.print("\n");
                  if (item.length() == 0) {
                     System.out.println("ERROR: no input detected.");
                     break;
                  }
                  query = "SELECT * FROM Menu WHERE itemName=";
                  query += "'" + item + "';";
                  querySize = esql.executeQuery(query);
                  if (querySize > 0) {
                     query = "SELECT price FROM Menu WHERE itemName=";
                     query += "'" + item + "';";
                     List<List<String>> result = esql.executeQueryAndReturnResult(query);
                     if (result.size() > 0) {
                        String temp = result.get(0).get(0);
                        price = Float.parseFloat(temp);
                     } else {
                        System.out.println("ERROR: item not found");
                        break;
                     }
                     System.out.println(createdAt);
                     query = String.format(
                           "INSERT INTO Orders (login, paid, timeStampRecieved, total) VALUES ('%s', 'false', '%s', '%s')",
                           authorisedUser, createdAt, price);
                     esql.executeUpdate(query);

                     String sequence = "Orders_orderid_seq";
                     orderid = esql.getCurrSeqVal(sequence);

                     query = String.format(
                           "INSERT INTO ItemStatus (orderid, itemName, lastUpdated, status) VALUES ('%s', '%s', '%s', 'Hasn''t started')",
                           orderid, item, createdAt);

                     esql.executeUpdate(query);
                     System.out.printf(
                           "Success! Item %s has been added to orderID %s at %s.\n", item, orderid, createdAt);
                     OrderTotal += price;
                     System.out.printf("Your current total is: $%.2f\n", OrderTotal);
                     orderPlaced = true;
                     break;
                  } else {
                     System.out.println("ERROR: item not found");
                     break;
                  }
               case 2:
                  if (orderPlaced) {
                     System.out.println("Please enter the ITEM NAME you would like to add to your order");
                     System.out.println(
                           "Note: item names are case sensitive and must be spelled correctly, refer to menu if needed");
                     System.out.print("Enter item name: ");
                     item = in.readLine();
                     System.out.print("\n");
                     if (item.length() == 0) {
                        System.out.println("ERROR: no input detected.");
                        break;
                     }
                     query = String.format("SELECT * FROM Menu WHERE itemName='%s'", item);
                     querySize = esql.executeQuery(query);
                     if (querySize > 0) {
                        String sequence = "Orders_orderid_seq";
                        orderid = esql.getCurrSeqVal(sequence);
                        query = String.format(
                              "INSERT INTO ItemStatus (orderid, itemName, lastUpdated, status) VALUES ('%s', '%s', '%s', 'Hasn''t Started')",
                              orderid, item, createdAt);
                        esql.executeUpdate(query);

                        query = String.format("SELECT price FROM Menu WHERE itemName='%s'", item);
                        List<List<String>> result = esql.executeQueryAndReturnResult(query);
                        if (result.size() > 0) {
                           String temp = result.get(0).get(0);
                           price = Float.parseFloat(temp);
                        } else {
                           System.out.println("ERROR: item not found");
                           break;
                        }

                        query = String.format("SELECT total FROM Orders WHERE orderid='%s'", orderid);
                        result = esql.executeQueryAndReturnResult(query);
                        if (result.size() > 0) {
                           String temp = result.get(0).get(0);
                           OrderTotal = Float.parseFloat(temp);
                        } else {
                           System.out.println("ERROR: Could not update total");
                           break;
                        }
                        OrderTotal = OrderTotal + price;
                        query = String.format("UPDATE Orders SET total='%s' WHERE orderid='%s'", OrderTotal, orderid);
                        esql.executeUpdate(query);
                        System.out.printf(
                              "Success! Item %s has been added to orderID %s at %s.\n", item, orderid, createdAt);
                        System.out.printf("Your current total is: $%.2f\n", OrderTotal);
                        break;
                     } else {
                        System.out.println("ERROR: item not found");
                        break;
                     }
                  } else {
                     System.out.println("ERROR: No order has been placed yet");
                     break;
                  }
               case 9:
                  if (orderPlaced) {
                     System.out.printf("Grand total: $%.2f\n", OrderTotal);
                  }
                  isOrdering = false;
                  break;
            }
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
         return null;
      }
      return orderid;
   }
   

   public static void UpdateOrder(Cafe esql) {
      boolean isMenuOpen = true;
      try {
         while (isMenuOpen) {
            System.out.println("UPDATE ORDER");
            System.out.println("--------------");
            System.out.println("0. Update Order");
            System.out.println("...............");
            System.out.println("9. Done updating");
            switch (readChoice()) {
               case 0:
                  int inputOrderID;
                  System.out.print("Enter your orderID:");
                  try {
                     inputOrderID = Integer.parseInt(in.readLine());
                  } catch (Exception e) {
                     System.out.println("ERROR: invalid input");
                     break;
                  }
                  String query = String.format(
                        "SELECT * FROM Orders WHERE login = '%s' AND orderid = '%s' AND paid='false'", authorisedUser,
                        inputOrderID);
                  int check = esql.executeQuery(query);
                  if (check <= 0) {
                     System.out.println("You did not place this order or the order has already been paid.");
                     break;
                  } else {
                     query = String.format(
                           "Select itemName,comments FROM ItemStatus WHERE orderid = '%s' AND status='Hasn''t Started'",
                           inputOrderID);
                     System.out.println("THIS IS YOUR CURRENT ORDER");
                     System.out.println("NOTE: Only items that can be updated are displayed");
                     System.out.println("-------------------------------------------------------");
                     int temp1 = esql.executeQueryAndPrintResult(query);
                     if (!(temp1 > 0)) {
                        System.out.println("No items to update");
                        break;
                     }
                     System.out.println(".......................................................");
                  }
                  boolean ismodding = true;
                  while (ismodding) {
                     System.out.println("EDIT ORDER");
                     System.out.println("-----------");
                     System.out.println("0. Add comment");
                     System.out.println("...............");
                     System.out.println("9. Done updating");
                     switch (readChoice()) {
                        case 0:
                           System.out.print("Please enter the item name that you would like to update: ");
                           String item = in.readLine();
                           query = String.format("SELECT * FROM ItemStatus WHERE orderid='%s' AND itemName='%s'",
                                 inputOrderID, item);
                           int check2 = esql.executeQuery(query);
                           if (!(check2 > 0)) {
                              System.out.println("ERROR: item does not exist/cannot be updated");
                              break;
                           }
                           System.out
                                 .print("Please enter the new comment: ");
                           String userInput = in.readLine();
                           query = String.format(
                                 "UPDATE ItemStatus SET comments='%s' AND lastUpdated ='%s' WHERE orderid = '%s' AND itemName='%s'",
                                 userInput, createdAt, inputOrderID, item);
                           esql.executeUpdate(query);
                           break;
                        case 9:
                           ismodding = false;
                           break;
                     }
                  }
                  break;
               case 9:
                  isMenuOpen = false;
                  break;
            }
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void ViewOrderHistory(Cafe esql){
      try{
         String query = String.format("SELECT * FROM Orders WHERE login = '%s' ORDER BY orderid desc LIMIT 5", authorisedUser);
         int rowCount = esql.executeQueryAndPrintResult(query);
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }

   public static void ViewOrderStatus(Cafe esql) {
      try {
         String type = UserType(esql);
         if (type.equals("Customer")) {
            String query = String.format(
                  "SELECT I.orderid, I.itemName, I.status FROM ItemStatus I, Orders O WHERE I.orderid=O.orderid AND O.login = '%s' AND O.orderid= ",
                  authorisedUser);
            System.out.print("Please enter your orderid: ");
            String input = in.readLine();
            query += input;
            int rowCount = esql.executeQueryAndPrintResult(query);
            if (rowCount == 0) {
               System.out.println("ERROR: order not found or you have not placed that orders");
            }
         } else {
            String query = "SELECT itemName, status FROM ItemStatus WHERE orderid= ";
            System.out.print("Please enter your orderid: ");
            String input = in.readLine();
            query += input;
            int rowCount = esql.executeQueryAndPrintResult(query);
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

}// end Cafe
