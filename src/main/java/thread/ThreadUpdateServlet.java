package thread;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import user.UserDetailsServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

/**
 * Created by anna on 17.10.15.
 */
public class ThreadUpdateServlet extends HttpServlet{
    private Connection con = null;


    public ThreadUpdateServlet(Connection connect) {
        con = connect;
    }

    public static PreparedStatement stmt = null;
    public static ResultSet rs = null;

    @Override
    public void doPost(@NotNull HttpServletRequest request,
                       @NotNull HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();

        JsonObject result = new JsonObject();
        JsonObject responseJSON = new JsonObject();
        result.addProperty("code", 0);
        result.add("response", responseJSON);
        try {
            JsonObject json = gson.fromJson(request.getReader(), JsonObject.class);
            String new_message = json.get("message").getAsString();
            String new_slug = json.get("slug").getAsString();
            int curr_id = json.get("thread").getAsInt();

            String query_update = "UPDATE `Thread` SET message = ?, slug = ? WHERE id= ?";
            stmt = con.prepareStatement(query_update);
            stmt.setString(1, new_message);
            stmt.setString(2, new_slug);
            stmt.setInt(3, curr_id);
            stmt.executeUpdate();

            ThreadDetailsServlet.ThreadDet(curr_id,responseJSON,con,new HashSet<String>());
            result.add("response", responseJSON);


        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se) {
            }
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException se) {
            }
        }
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().println(result);


    }
}
