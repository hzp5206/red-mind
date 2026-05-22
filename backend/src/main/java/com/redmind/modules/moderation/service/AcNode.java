package com.redmind.modules.moderation.service;

import java.util.HashMap;
import java.util.Map;

public class AcNode {

    private final Map<Character, AcNode> children = new HashMap<>();
    private AcNode fail;
    private boolean wordEnd;
    private String word;

    public Map<Character, AcNode> getChildren() {
        return children;
    }

    public AcNode getFail() {
        return fail;
    }

    public void setFail(AcNode fail) {
        this.fail = fail;
    }

    public boolean isWordEnd() {
        return wordEnd;
    }

    public void setWordEnd(boolean wordEnd) {
        this.wordEnd = wordEnd;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
