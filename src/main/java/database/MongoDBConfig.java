package database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MongoDBConfig {

    private String host;
    private long port;
    private String dbName;
    private String collection;
    private String username;
    private String password;
    @JsonIgnore()
    private String uri;

    public void setUri() {

        this.uri = "mongodb://" + this.username + ":" + this.password + "@"
                + this.host + ":" + this.port + "/" + this.dbName;

    }
}
