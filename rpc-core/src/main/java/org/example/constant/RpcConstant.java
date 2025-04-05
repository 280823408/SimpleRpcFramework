package org.example.constant;

public interface RpcConstant {
    int SERVER_PORT = 8888;
    String SERVER_HOST = "127.0.0.1";
    String ZK_HOST = "192.168.23.138";
    int ZK_PORT = 2181;
    String ZK_RPC_ROOT_PATH = "/rpc";

    String NETTY_RPC_KEY = "RpcResponse";
    byte[] RPC_MAGIC_CODE = new byte[]{(byte) 'p', (byte) 'r', (byte) 'p', (byte) 'c'};
    int REQ_HEAD_LEN = 16;
    int REQ_MAX_LEN = 1024 * 1024 * 8;
}
