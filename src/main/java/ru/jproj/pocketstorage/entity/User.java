package ru.jproj.pocketstorage.entity;

public class User {

    private Long id;
    private String userHash;

    public User() {
    }

    public User(Long id, String userHash) {
        this.id = id;
        this.userHash = userHash;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserHash() {
        return userHash;
    }

    public void setUserHash(String userHash) {
        this.userHash = userHash;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userHash='" + userHash + '\'' +
                '}';
    }
}
