package com.atguigu.gulimall.common;

import com.atguigu.common.CommonTestHelper;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.Accessors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonTestHelperTest {

    @Test
    void DFS() {
        CommonTestHelper.DepthFirstPreOrderIterator<TreeNode> dfi = new CommonTestHelper.DepthFirstPreOrderIterator<>(getTree(), TreeNode::getChildren);
        List<Integer> treeNodeIds = new ArrayList<>();
        while (dfi.hasNext()) {
            treeNodeIds.add(dfi.next().id);
        }
        final List<Integer> resultList = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        assertThat(treeNodeIds).hasSize(resultList.size()).containsSequence(resultList);
    }

    @Test
    void BFS() {
        CommonTestHelper.BreadthFirstIterator<TreeNode> bfi = new CommonTestHelper.BreadthFirstIterator<>(getTree(), TreeNode::getChildren);
        List<Integer> treeNodeIds = new ArrayList<>();
        while (bfi.hasNext()) {
            treeNodeIds.add(bfi.next().id);
        }
        final List<Integer> resultList = Arrays.asList(1, 2, 5, 7, 3, 4, 6);
        assertThat(treeNodeIds).hasSize(resultList.size()).containsSequence(resultList);
    }

    @Test
    void treeFlattenBFS() {
        List<Integer> treeNodeIds = CommonTestHelper.treeFlattenBFS(getTree(), TreeNode::getChildren).stream()
            .map(TreeNode::getId).collect(Collectors.toList());
        final List<Integer> resultList = Arrays.asList(1, 2, 5, 7, 3, 4, 6);
        assertThat(treeNodeIds).hasSize(resultList.size()).containsSequence(resultList);
    }

    private TreeNode getTree() {
        return TreeNode.builder().id(1)
            .child(TreeNode.builder().id(2)
                .child(TreeNode.builder().id(3).build())
                .child(TreeNode.builder().id(4).build()).build())
            .child(TreeNode.builder().id(5)
                .child(TreeNode.builder().id(6).build()).build())
            .child(TreeNode.builder().id(7).build())
            .build();
    }

    @Data
    @Accessors(chain = true)
    @Builder
    private static class TreeNode {
        private int id;
        @Singular
        private List<TreeNode> children;
    }
}
