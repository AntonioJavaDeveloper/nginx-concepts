package br.com.javadeveloper.dslist.dto;

public class ServerResponse<T> {
    private String server;
    private T data;

    public ServerResponse(String server, T data) {
        this.server = server;
        this.data = data;
    }

    public String getServer() {
        return server;
    }

    public T getData() {
        return data;
    }
}
