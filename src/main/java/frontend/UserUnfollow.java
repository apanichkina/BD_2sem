package frontend;

import main.APIErrors;
import main.Main;
import org.jetbrains.annotations.NotNull;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.sql.*;

import com.google.gson.Gson;


import com.google.gson.JsonObject;
import user.UserDetailsServlet;

/**
 * Created by anna on 12.10.15.
 */
public class UserUnfollow extends HttpServlet {

    private String query = "";

    public UserUnfollow(String param) {
        if (param.equals("follow")) {
            query = "INSERT INTO Follow (followerID,followeeID) VALUES(?,?);";
        }
        if (param.equals("unfollow")) {
            query = "DELETE FROM Follow WHERE followerID= ? and followeeID= ?";
        }
    }

    @Override
    public void doPost(@NotNull HttpServletRequest request,
                       @NotNull HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject result = new JsonObject();
        JsonObject responseJSON = new JsonObject();
        result.addProperty("code", 0);
        result.add("response", responseJSON);

        try (Connection con = Main.mainConnection.getConnection();PreparedStatement stmt = con.prepareStatement(query)) {
            JsonObject json = gson.fromJson(request.getReader(), JsonObject.class);
            String follower = json.get("follower").getAsString();
            String followee = json.get("followee").getAsString();

//            int follower_id = UserDetailsServlet.GetID(follower, "email", "User", con);
            int follower_id = UserDetailsServlet.GetUserID(follower, con);
            if (follower_id == -1) throw new com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException();
//            int followee_id = UserDetailsServlet.GetID(followee, "email", "User", con);
            int followee_id = UserDetailsServlet.GetUserID(followee, con);
            if (followee_id == -1) throw new com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException();

            //stmt = con.prepareStatement(query);
            stmt.setInt(1, follower_id);
            stmt.setInt(2, followee_id);
            stmt.executeUpdate();

        } catch (com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException icvEx) {
            APIErrors.ErrorMessager(1, result);
        }
        catch (com.google.gson.JsonSyntaxException jsEx) {
            APIErrors.ErrorMessager(2, result);
        }
        catch (java.lang.NullPointerException npEx) {
            APIErrors.ErrorMessager(3,result);
        }
        catch (SQLException sqlEx) {
            APIErrors.ErrorMessager(4,result);
            sqlEx.printStackTrace();
        } finally {
            //close connection ,stmt and resultset here
            //try {if (con != null) {con.close();}} catch (SQLException se) { /*can't do anything */ }
//            try {
//                if (stmt != null) {
//                    stmt.close();
//                }
//            } catch (SQLException se) {}
//            try {
//                if (rs != null) {
//                    rs.close();
//                }
//            } catch (SQLException se) {}
        }
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().println(result);


    }
}
