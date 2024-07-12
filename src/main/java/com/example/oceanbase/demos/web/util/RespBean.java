package com.example.oceanbase.demos.web.util;

import lombok.Data;

@Data
public class RespBean {
    private Integer code;
    private String msg;
    private Object data;

    public static RespBean ok(String msg, Object data) {
        return new RespBean(200, msg, data);
    }


    public static RespBean ok(String msg) {
        return new RespBean(200, msg, null);
    }


    public static RespBean error(String msg, Object data) {
        return new RespBean(500, msg, data);
    }


    public static RespBean error(String msg) {
        return new RespBean(500, msg, null);
    }

    private RespBean() {
    }

    private RespBean(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

}

