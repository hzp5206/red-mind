package com.redmind.modules.moderation.service;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AcAutomatonTest {

    @Test
    void shouldMatchSensitiveWords() {
        AcAutomaton automaton = new AcAutomaton();
        automaton.addWord("最好");
        automaton.addWord("第一");
        automaton.build();

        List<String> result = automaton.search("这个方案不是最好，但第一眼很抓人");
        Assertions.assertTrue(result.contains("最好"));
        Assertions.assertTrue(result.contains("第一"));
    }
}
