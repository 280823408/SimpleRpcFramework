package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enums.CompressType;
import org.example.enums.MessageType;
import org.example.enums.SerializeType;
import org.example.enums.VersionType;

/**
 * @Author Mike
 * @Date 2025/4/1
 **/

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RpcMessage {
    private static final long serialVersionUID = 1L;

    private Integer reqId;
    private VersionType version;
    private MessageType msgType;
    private SerializeType serializeType;
    private CompressType compressType;
    private Object data;
}
