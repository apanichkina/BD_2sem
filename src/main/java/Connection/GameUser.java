package Connection;

/**
 * Created by iHelos on 20.09.2015.
 */
public class GameUser {
        private String login;
        private String password;
        private String email;

        public GameUser(String login, String password, String email) {
            this.login = login;
            this.password = password;
            this.email = email;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }

        public String getEmail() {
            return email;
        }
}
