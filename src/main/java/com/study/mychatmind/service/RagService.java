package com.study.mychatmind.service;

import java.util.List;

public interface RagService {

    float[] embed(String text);

    List<String> similaritySearch(String kbId, String query);
}
