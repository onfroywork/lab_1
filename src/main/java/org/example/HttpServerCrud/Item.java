package org.example.HttpServerCrud;

record Item(int id, String data) {
    public String toJson() {
        return String.format("{\"id\":%d,\"data\":\"%s\"}", id, data);
    }
}
