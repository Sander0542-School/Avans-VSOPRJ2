package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.models.annotations.Column;
import nl.avans.vsoprj2.wordcrex.models.annotations.PrimaryKey;

import java.sql.ResultSet;

public class Word extends DbModel {

    @PrimaryKey
    @Column("word")
    private String wordValue;
    @PrimaryKey
    @Column("letterset_code")
    private String letterset_code;
    @Column("state")
    private String state;
    @Column("username")
    private String username;

    public Word(ResultSet resultSet) {
        super(resultSet);
    }

    @Override
    public String getTable() {
        return "dictionary";
    }

    public String getWord() {
        return this.wordValue;
    }

    public String getLetterset_code() {
        return this.letterset_code;
    }

    public String getUsername() {
        return this.username;
    }

}
