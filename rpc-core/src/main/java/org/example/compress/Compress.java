package org.example.compress;
/**
 * @Author Mike
 * @Date 2025/4/1
 **/
public interface Compress {
    byte[] compress(byte[] data);

    byte[] decompress(byte[] data);
}
