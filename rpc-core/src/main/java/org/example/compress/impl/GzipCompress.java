package org.example.compress.impl;

import cn.hutool.core.util.ZipUtil;
import org.example.compress.Compress;

import java.util.Objects;

/**
 * @Author Mike
 * @Date 2025/4/1
 **/
public class GzipCompress implements Compress {
    @Override
    public byte[] compress(byte[] data) {
        if (Objects.isNull(data) || data.length == 0) {
            return data;
        }

        return ZipUtil.gzip(data);
    }

    @Override
    public byte[] decompress(byte[] data) {
        if (Objects.isNull(data) || data.length == 0) {
            return data;
        }

        return ZipUtil.unGzip(data);
    }
}
