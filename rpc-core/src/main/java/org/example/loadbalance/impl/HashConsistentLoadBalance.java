package org.example.loadbalance.impl;

import org.example.loadbalance.LoadBalance;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class HashConsistentLoadBalance implements LoadBalance {
    private static final int VIRTUAL_NODE_NUM = 100;
    private static final SortedMap<Integer, String> virtualNodes = new TreeMap<>();

    private static String getVirtualNodeName(String realName, int num) {
        return realName + "&&VN" + num;
    }

    private static String getRealNodeName(String virtualName) {
        return virtualName.split("&&")[0];
    }

    private static int getHash(String key) {
        final int FNV_32_PRIME = 0x01000193;
        int hash = 0x811c9dc5;
        for (int i = 0; i < key.length(); i++) {
            hash ^= key.charAt(i);
            hash *= FNV_32_PRIME;
        }
        return hash;
    }

    @Override
    public String select(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        if (virtualNodes.isEmpty() || virtualNodes.size() / VIRTUAL_NODE_NUM != list.size()) {
            for (String s : list) {
                for (int j = 0; j < VIRTUAL_NODE_NUM; j++) {
                    virtualNodes.put(getHash(getVirtualNodeName(s, j)), s);
                }
            }
        }
        String widgetKey = String.valueOf(System.currentTimeMillis() + (int) (Math.random() * 1000));
        int hash = getHash(widgetKey);
        SortedMap<Integer, String> subMap = virtualNodes.tailMap(hash);
        String virtualNodeName;
        if (subMap.isEmpty()) {
            virtualNodeName = virtualNodes.get(virtualNodes.firstKey());
        } else {
            virtualNodeName = subMap.get(subMap.firstKey());
        }
        return getRealNodeName(virtualNodeName);
    }
}
