package user;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by anna on 15.10.15.
 */
public class UserDetailsServlet extends HttpServlet {

    private Connection con = null;
    private String table_name = "";
    public UserDetailsServlet(Connection connect, String table) {
        con = connect;
        table_name = table;
    }
    public static PreparedStatement stmt = null;
    public static ResultSet rs = null;

    public static void UsDet(int curr_id, @Nullable JsonObject responseJSON , Connection con) throws IOException, SQLException {

        String query_userDetails = "SELECT * FROM User WHERE id=?";
        PreparedStatement stmt = con.prepareStatement(query_userDetails);
        stmt.setInt(1, curr_id);
        //TODO проверить везде сработал ли запрос посредством проверки возвращаемого значения
        ResultSet rs = stmt.executeQuery();
        //TODO проверить валидность данных
        while (rs.next()) {
            responseJSON.addProperty("id", rs.getInt("id"));
            responseJSON.addProperty("about", rs.getString("about"));
            responseJSON.addProperty("email", rs.getString("email"));
            responseJSON.addProperty("name", rs.getString("name"));
            responseJSON.addProperty("username", rs.getString("username"));
            responseJSON.addProperty("isAnonymous", rs.getBoolean("isAnonymous"));
        }
        //////3 qvery
        String query_subscriptions = "SELECT threadID\n" + //TODO threadID
                "FROM Subscription\n" +
                "WHERE userID= ?";
        stmt = con.prepareStatement(query_subscriptions);
        stmt.setInt(1, curr_id);
        rs = stmt.executeQuery();
        JsonArray subscriptions_list = new JsonArray();
        while (rs.next()) {
            subscriptions_list.add(rs.getInt("threadID"));
        }
        responseJSON.add("subscriptions", subscriptions_list);
        ////4 qvery
        String query_following = "SELECT email \n" +
                "FROM Follow \n" +
                "LEFT JOIN User\n" +
                "ON Follow.followeeID=User.id\n" +
                "WHERE followerID= ?";
        stmt = con.prepareStatement(query_following);
        stmt.setInt(1, curr_id);
        rs = stmt.executeQuery();
        JsonArray following_list = new JsonArray();
        while (rs.next()) {
            following_list.add(rs.getString("email"));
        }
        responseJSON.add("following", following_list);
        /////5 qvery
        String query_followers = "SELECT email \n" +
                "FROM Follow \n" +
                "LEFT JOIN User\n" +
                "ON Follow.followerID=User.id\n" +
                "WHERE followeeID= ?";
        stmt = con.prepareStatement(query_followers);
        stmt.setInt(1, curr_id);
        rs = stmt.executeQuery();
        JsonArray followers_list = new JsonArray();
        while (rs.next()) {
            followers_list.add(rs.getString("email"));
        }
        responseJSON.add("followers", followers_list);
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException se) {}
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException se) {}

    };

    public static int GetID (String row_value, String row_name, String table_name, Connection con) throws SQLException {
        int curr_id = -1;
        String query_getID = "SELECT id FROM "+ table_name+" WHERE "+row_name+"=?";
        PreparedStatement stmt = con.prepareStatement(query_getID);
        stmt.setString(1, row_value);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            curr_id = rs.getInt("id");
        }
        try{if (stmt != null){
            stmt.close();
        }
        } catch(SQLException se) {}
        try{if (rs != null){
            rs.close();
        }
        } catch(SQLException se) {}
        return curr_id;
    };

    @Override
    public void doGet(@NotNull HttpServletRequest request,
                      @NotNull HttpServletResponse response) throws ServletException, IOException {


        JsonObject result = new JsonObject();
        JsonObject responseJSON = new JsonObject();
        result.addProperty("code", 0);
        try {

            String curr_email = request.getParameter("user");
            if (curr_email == null) throw new NullPointerException();

            int curr_id = GetID(curr_email, "email", table_name, con);
            if (curr_id == -1) throw new com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException();

            UsDet(curr_id,responseJSON, con);
            result.add("response", responseJSON);


        }
        catch (com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException icvEx) {
            result.addProperty("code", 1);
            result.addProperty("response", "error1");
        }
        catch (java.lang.NullPointerException npEx) {
            result.addProperty("code", 3);
            result.addProperty("response", "er3");
        }
        catch (SQLException sqlEx) {
            result.addProperty("code", 4);
            result.addProperty("response", "error4");
            sqlEx.printStackTrace();
        } finally {

            try{if (stmt != null){
                stmt.close();
            }
            } catch(SQLException se) {}
            try{if (rs != null){
                rs.close();
            }
            } catch(SQLException se) {}


        }

        response.setContentType("application/json; charset=utf-8");
        response.getWriter().println(result);

    }




}