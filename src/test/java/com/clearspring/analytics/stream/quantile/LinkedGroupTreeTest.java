package com.clearspring.analytics.stream.quantile;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test with a GroupTree, specifically to test the linked list extensions of it.
 */
public class LinkedGroupTreeTest {

  /**
   * Run a small test with a tree.
   */
  @Test
  public void smallTreeTest() {
    GroupTree tree = new GroupTree();
    TDigest.Group group1 = new TDigest.Group(100);
    TDigest.Group group2 = new TDigest.Group(50);
    TDigest.Group group3 = new TDigest.Group(150);
    tree.add(group1);
    testTree(tree);
    tree.add(group2);
    testTree(tree);

    assertEquals(group1, group2.right);
    assertNull(group2.left);
    assertEquals(group2, group1.left);
    assertNull(group1.right);

    tree.add(group3);
    testTree(tree);
    assertEquals(group3, group1.right);
    assertEquals(group1, group3.left);
  }

  @Test
  public void randomTreeTest() {
    GroupTree tree = new GroupTree();
    int seed = new Random().nextInt();
    System.out.println("Test seed: " + seed);
    Random random = new Random(seed);
    List<TDigest.Group> allGroups = new ArrayList<>();
    for(int i = 0; i < 10000; i++) {
      TDigest.Group group = new TDigest.Group(random.nextDouble());
      allGroups.add(group);
      tree.add(group);// Add a random double
      if(i % 100 == 0) {
        testTree(tree);
      }
    }
    testTree(tree);

    // Perform some random removals
    Collections.shuffle(allGroups);
    for(int i = 0; i < 100; i++) {
      TDigest.Group removedGroup = allGroups.get(i);
      tree.remove(removedGroup);
      testTree(tree);
    }
  }


  /**
   * Test out a linked GroupTree, making sure the left to right iteration it correct and complete.
   * @param tree
   */
  private void testTree(GroupTree tree) {
    TDigest.Group group = tree.first();
    assertNull("First group shouldn't have a left neighbour", group.left);
    int processedGroups = 1;
    while(group.right != null) {
      processedGroups++;
      assertTrue("Have too many groups in the tree, based on left/right references", processedGroups <= tree.size());
      assertTrue("Right neighbour has a smaller value, should never happen: " + group.right.mean() + " vs " + group.mean(), group.right.mean() >= group.mean());
      assertEquals("Right neighbour doesn't have same group as left neighbour", group, group.right.left);
      group = group.right;
    }
    assertEquals("Left to right iteration doesn't result in full tree size", tree.size(), processedGroups);
  }

}
