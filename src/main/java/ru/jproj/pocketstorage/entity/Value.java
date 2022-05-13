package ru.jproj.pocketstorage.entity;

public class Value {

    private Long id;
    private Long keyId;
    private String value;

    public Value() {
    }

    public Value(Long id, Long keyId, String value) {
        this.id = id;
        this.keyId = keyId;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Value{" +
                "id=" + id +
                ", keyId=" + keyId +
                ", value='" + value + '\'' +
                '}';
    }
}
