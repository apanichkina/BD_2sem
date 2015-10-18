package forum;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import user.UserDetailsServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

/**
 * Created by anna on 15.10.15.
 */
public class ForumCreateServlet extends HttpServlet {
    private Connection con = null;
    private String query = "INSERT INTO Forum (name,short_name,userID) VALUES (?,?,?)";



    public ForumCreateServlet(Connection connect) {
        con = connect;
    }

    public static PreparedStatement stmt = null;
    public static ResultSet rs = null;
    @Override
    public void doPost(@NotNull HttpServletRequest request,
                       @NotNull HttpServletResponse response) throws ServletException, IOException {
        JsonObject result = new JsonObject();
        JsonObject responseJSON = new JsonObject();
        result.addProperty("code", 0);
        result.add("response", responseJSON);

        Gson gson = new Gson();


        try {

            JsonObject json = gson.fromJson(request.getReader(), JsonObject.class);
            String name = json.get("name").getAsString();
            String short_name = json.get("short_name").getAsString();
            String user = json.get("user").getAsString();
            int userID = UserDetailsServlet.GetID(user, "email", "User", con);


            stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, name);
            stmt.setString(2, short_name);
            stmt.setInt(3, userID);

            if (stmt.executeUpdate() != 1) throw new SQLException();
            rs = stmt.getGeneratedKeys();
            rs.next();

            responseJSON.addProperty("id", rs.getInt(1));
            responseJSON.addProperty("name", name);
            responseJSON.addProperty("short_name", short_name);
            responseJSON.addProperty("user", user);

        }
        catch (com.google.gson.JsonSyntaxException jsEx) {
            result.addProperty("code", 2);
            result.addProperty("response", "err2");
        }

        catch (java.lang.NullPointerException npEx) {
            result.addProperty("code", 3);
            result.addProperty("response", "err3");
        }

        catch (com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException icvEx) {
            result.addProperty("code", 3);
            result.addProperty("response", "error3");
        }

        catch (SQLException sqlEx) {
            result.addProperty("code", 4);
            result.addProperty("response", "err4");

            sqlEx.printStackTrace();
        } finally {
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
        }

        response.setContentType("application/json; charset=utf-8");
        response.getWriter().println(result);


    }
}