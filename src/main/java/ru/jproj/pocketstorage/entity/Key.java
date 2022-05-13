package ru.jproj.pocketstorage.entity;

public class Key {

    private Long id;
    private Long userId;
    private String keyHash;

    public Key() {
    }

    public Key(Long id, Long userId, String keyHash) {
        this.id = id;
        this.userId = userId;
        this.keyHash = keyHash;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getKeyHash() {
        return keyHash;
    }

    public void setKeyHash(String keyHash) {
        this.keyHash = keyHash;
    }

    @Override
    public String toString() {
        return "Key{" +
                "id=" + id +
                ", user=" + userId +
                ", keyHash='" + keyHash + '\'' +
                '}';
    }
}
