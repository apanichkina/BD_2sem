package thread;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Created by anna on 15.10.15.
 */
public class ThreadSubscribeServlet extends HttpServlet{

    private String query = "";
    private Boolean isSubscribe = null;
    public ThreadSubscribeServlet(String param) {
        if (param.equals("subscribe")) {
            query = "INSERT INTO Subscription (userID,threadID) VALUES (?,?)";
            isSubscribe = true;
        }
        if (param.equals("unsubscribe")) {
            query = "DELETE FROM Subscription WHERE userID=? AND threadID=?";
            isSubscribe = false;
        }
    }

    @Override
    public void doPost(@NotNull HttpServletRequest request,
                       @NotNull HttpServletResponse response) throws ServletException, IOException {
        JsonObject result = new JsonObject();
        JsonObject responseJSON = new JsonObject();
        result.addProperty("code", 0);
        result.add("response", responseJSON);
        Gson gson = new Gson();
        try (Connection con = Main.mainConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(query);) {
            JsonObject json = gson.fromJson(request.getReader(), JsonObject.class);
            String user = json.get("user").getAsString();
//            int userID = UserDetailsServlet.GetID(user, "email", "User", con);
            int userID = UserDetailsServlet.GetUserID(user, con);
            if (userID == -1) APIErrors.ErrorMessager(3, result);
            else {
                int threadID = json.get("thread").getAsInt();
                stmt.setInt(1, userID);
                stmt.setInt(2, threadID);

                if (stmt.executeUpdate() != 1) {
                    if (isSubscribe) APIErrors.ErrorMessager(4, result);
                    else APIErrors.ErrorMessager(3, result);
                } else {
                    responseJSON.addProperty("thread", threadID);
                    responseJSON.addProperty("user", user);
                }
            }
        }
        catch (com.google.gson.JsonSyntaxException jsEx) {
            APIErrors.ErrorMessager(2, result);
        }
        catch (java.lang.NullPointerException npEx) {
            APIErrors.ErrorMessager(3, result);
        }
        catch (com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException icvEx) {
            APIErrors.ErrorMessager(3, result);
        }
        catch (SQLException sqlEx) {
            APIErrors.ErrorMessager(4, result);
            sqlEx.printStackTrace();
        }
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().println(result);
    }
}
