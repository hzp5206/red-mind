package com.redmind.modules.moderation.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class AcAutomaton {

    private final AcNode root = new AcNode();

    public void addWord(String word) {
        AcNode current = root;
        for (char character : word.toCharArray()) {
            current = current.getChildren().computeIfAbsent(character, key -> new AcNode());
        }
        current.setWordEnd(true);
        current.setWord(word);
    }

    public void build() {
        Queue<AcNode> queue = new ArrayDeque<>();
        root.setFail(root);
        queue.offer(root);

        while (!queue.isEmpty()) {
            AcNode parent = queue.poll();
            for (Character character : parent.getChildren().keySet()) {
                AcNode child = parent.getChildren().get(character);
                if (parent == root) {
                    child.setFail(root);
                } else {
                    AcNode fail = parent.getFail();
                    while (fail != root && !fail.getChildren().containsKey(character)) {
                        fail = fail.getFail();
                    }
                    if (fail.getChildren().containsKey(character)) {
                        child.setFail(fail.getChildren().get(character));
                    } else {
                        child.setFail(root);
                    }
                }
                queue.offer(child);
            }
        }
    }

    public List<String> search(String text) {
        Set<String> results = new HashSet<>();
        AcNode current = root;
        for (char character : text.toCharArray()) {
            while (current != root && !current.getChildren().containsKey(character)) {
                current = current.getFail();
            }
            if (current.getChildren().containsKey(character)) {
                current = current.getChildren().get(character);
            }
            AcNode temp = current;
            while (temp != null && temp != root) {
                if (temp.isWordEnd()) {
                    results.add(temp.getWord());
                }
                temp = temp.getFail();
            }
        }
        return new ArrayList<>(results);
    }
}
