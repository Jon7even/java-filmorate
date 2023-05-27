package ru.yandex.practicum.filmorate.model;

public enum UserRelationStatus {
    REQUEST("REQUEST"),
    APPROVED("APPROVED"),
    BLACK_LIST("BLACK LIST"),
    NO_RELATION("RELATION EMPTY");

    private final String value;

    UserRelationStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
