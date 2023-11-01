package com.fable.fablesiegeplugin.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetListFromMapKeyset {

    public static List<String> getListFromMapKeyset(Map<?, ?> map) {
        List<?> list = new ArrayList<>(map.keySet());
        ArrayList<String> listOfKeys = new ArrayList<>();
        for (Object key : list) {
            listOfKeys.add(key.toString());
        }
        return listOfKeys;
    }

}
