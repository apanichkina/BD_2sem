package frontend;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import main.APIErrors;
import main.Main;
import org.jetbrains.annotations.NotNull;
import user.UserDetailsServlet;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

import java.util.*;


/**
 * Created by anna on 11.10.15.
 */
public class UserListFollowers extends HttpServlet {

    private String field_name = "";
    private String query = "";
    public UserListFollowers(String param) {

        if (param.equals("followers")) {
            field_name = "followerID";
            query = "SELECT followerID, email FROM Follow LEFT JOIN User ON Follow.followerID = User.id WHERE followeeID= ?";
        }
        if (param.equals("following")) {
            field_name = "followeeID";
            query = "SELECT followeeID, email FROM Follow LEFT JOIN User ON Follow.followeeID = User.id WHERE followerID= ?";
        }
    }

    @Override
    public void doGet(@NotNull HttpServletRequest request,
                      @NotNull HttpServletResponse response) throws ServletException, IOException {

        JsonObject result = new JsonObject();
        JsonObject responseJSON = new JsonObject();
        result.addProperty("code", 0);
        JsonArray list = new JsonArray();
        result.add("response", list);

        String query_since = "";
        String query_order = "desc";
        String query_limit = "";



        try(Connection con = Main.mainConnection.getConnection();) {
            ResultSet rs = null;
            PreparedStatement stmt = null;
            String curr_email = request.getParameter("user");
            if (curr_email != null){
//            int curr_id = UserDetailsServlet.GetID(curr_email, "email", "User", con);
            int curr_id = UserDetailsServlet.GetUserID(curr_email, con);
            if (curr_id != -1) {

            String input_since_id = request.getParameter("since_id");
            if (input_since_id != null) {
                query_since = " and "+field_name+" >= " + input_since_id;
            }
            String input_order = request.getParameter("order");
            if (input_order != null) {
                query_order = input_order;
            }
            String input_limit = request.getParameter("limit");
            if (input_limit != null) {
                query_limit = " limit " + input_limit;
            }

            stmt = con.prepareStatement(query+ query_since + " order by name "+query_order + query_limit);
            stmt.setInt(1, curr_id);
            rs = stmt.executeQuery();

            while (rs.next()) {
                JsonObject responceJS = new JsonObject();
                UserDetailsServlet.UsDet(rs.getInt(field_name), responceJS, con);
                list.add(responceJS);
            }

        } else APIErrors.ErrorMessager(1,result);
            } else APIErrors.ErrorMessager(3,result);

        }
        catch (com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException icvEx) {
            APIErrors.ErrorMessager(1,result);
        }
        catch (java.lang.NullPointerException npEx) {
            APIErrors.ErrorMessager(3,result);
        }
        catch (SQLException sqlEx) {
            APIErrors.ErrorMessager(4,result);
            sqlEx.printStackTrace();
        }
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().println(result);
    }

}
