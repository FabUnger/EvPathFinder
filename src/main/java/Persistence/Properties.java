package Persistence;

public class Properties {

    private String uri;
    private String username;
    private String password;

    public Properties(String uri, String username, String password) {
        this.uri = uri;
        this.username = username;
        this.password = password;
    }

    public String getUri() {
        return this.uri;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

}
