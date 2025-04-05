package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enums.RpcRespStatus;

import java.io.Serializable;

/**
 * @Author Mike
 * @Date 2025/3/27
 **/

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RpcResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private String requestId;
    private Integer code;
    private String message;
    private T data;


    public static <T> RpcResponse<T> success(String reqId, T data) {
        RpcResponse<T> resp = new RpcResponse<T>();
        resp.setRequestId(reqId);
        resp.setCode(0);
        resp.setData(data);

        return resp;
    }

    public static <T> RpcResponse<T> fail(String reqId, RpcRespStatus status) {
        RpcResponse<T> resp = new RpcResponse<T>();
        resp.setRequestId(reqId);
        resp.setCode(status.getCode());
        resp.setMessage(status.getMsg());

        return resp;
    }

    public static <T> RpcResponse<T> fail(String reqId, String msg) {
        RpcResponse<T> resp = new RpcResponse<T>();
        resp.setRequestId(reqId);
        resp.setCode(RpcRespStatus.FAIL.getCode());
        resp.setMessage(msg);

        return resp;
    }
}
