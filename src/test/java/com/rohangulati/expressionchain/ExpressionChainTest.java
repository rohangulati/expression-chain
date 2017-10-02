package com.rohangulati.expressionchain;

import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ExpressionChainTest {

  @Test
  public void testNoChain() throws Throwable {
    assertChain("A", ExpressionChain.of("A"));
    assertChain("A", ExpressionChain.orOperator("A"));
    assertChain("A", ExpressionChain.orOperator(Arrays.asList("A")));
    assertChain("A", ExpressionChain.orChains(ExpressionChain.of("A")));
    assertChain("A", ExpressionChain.orChains(Arrays.asList(ExpressionChain.of("A"))));
    assertChain("A", ExpressionChain.andOperator("A"));
    assertChain("A", ExpressionChain.andOperator(Arrays.asList("A")));
    assertChain("A", ExpressionChain.andChains(ExpressionChain.of("A")));
    assertChain("A", ExpressionChain.andChains(Arrays.asList(ExpressionChain.of("A"))));
  }

  @Test
  public void testSingleAndChain() throws Throwable {
    assertChain("(A && B)", ExpressionChain.of("A").and("B"));
    assertChain("(A && B)", ExpressionChain.of("A").and(ExpressionChain.of("B")));
    assertChain("(A && B)", ExpressionChain.andOperator("A", "B"));
    assertChain("(A && B)", ExpressionChain.andOperator(Arrays.asList("A", "B")));
    assertChain("(A && B)", ExpressionChain.andOperator("A").and("B"));
    assertChain("(A && B)", ExpressionChain.andOperator(Arrays.asList("A")).and("B"));
    assertChain("(A && B)", ExpressionChain.orOperator("A").and("B"));
    assertChain("(A && B)", ExpressionChain.orOperator(Arrays.asList("A")).and("B"));
    assertChain("(A && B)",
      ExpressionChain.andChains(ExpressionChain.of("A"), ExpressionChain.of("B")));
    assertChain("(A && B)",
      ExpressionChain.andChains(Arrays.asList(ExpressionChain.of("A"), ExpressionChain.of("B"))));
  }

  @Test
  public void testSingleOrChain() throws Throwable {
    assertChain("(A || B)", ExpressionChain.of("A").or("B"));
    assertChain("(A || B)", ExpressionChain.of("A").or(ExpressionChain.of("B")));
    assertChain("(A || B)", ExpressionChain.orOperator("A", "B"));
    assertChain("(A || B)", ExpressionChain.orOperator(Arrays.asList("A", "B")));
    assertChain("(A || B)", ExpressionChain.orOperator("A").or("B"));
    assertChain("(A || B)", ExpressionChain.orOperator(Arrays.asList("A")).or("B"));
    assertChain("(A || B)", ExpressionChain.andOperator("A").or("B"));
    assertChain("(A || B)", ExpressionChain.andOperator(Arrays.asList("A")).or("B"));
    assertChain("(A || B)",
      ExpressionChain.orChains(ExpressionChain.of("A"), ExpressionChain.of("B")));
    assertChain("(A || B)",
      ExpressionChain.orChains(Arrays.asList(ExpressionChain.of("A"), ExpressionChain.of("B"))));
  }

  @Test
  public void testTwoOperatorChain() throws Throwable {
    assertChain("(A || (B && C))",
      ExpressionChain.of("A").or(ExpressionChain.andOperator("B", "C")));
    assertChain("((A || B) && C)", ExpressionChain.of("A").or("B").and("C"));
    assertChain("(A && (B || C))",
      ExpressionChain.of("A").and(ExpressionChain.of("B").or("C")));
    assertChain("((A && B) || C)", ExpressionChain.andOperator("A", "B").or("C"));
    assertChain("(A && B && C)", ExpressionChain.of("A").and("B").and("C"));
    assertChain("(A && B && C)", ExpressionChain.andOperator("A", "B", "C"));
    assertChain("(A || B || C)", ExpressionChain.of("A").or("B").or("C"));
    assertChain("(A || B || C)", ExpressionChain.orOperator("A", "B", "C"));
  }

  @Test
  public void testThreeOperatorChain() throws Throwable {
    assertChain("((A && B) || (C && D))",
      ExpressionChain.of("A").and("B").or(ExpressionChain.of("C").and("D")));
    assertChain("((A && B) || (C && D))", ExpressionChain.orChains(
      ExpressionChain.of("A").and("B"), ExpressionChain.of("C").and("D")));
    ExpressionChain.of("A").and("B").or(ExpressionChain.of("C").and("D"));
    assertChain("((A && B) || (C || D))",
      ExpressionChain.of("A").and("B").or(ExpressionChain.of("C").or("D")));
    assertChain("((A || B) && (C || D))",
      ExpressionChain.of("A").or("B").and(ExpressionChain.of("C").or("D")));
    assertChain("((A || B) && (C && D))",
      ExpressionChain.andChains(ExpressionChain.orOperator("A", "B"),
        ExpressionChain.andOperator("C", "D")));
    assertChain("(A && B && (C || D))",
      ExpressionChain.of("A").and("B").and(ExpressionChain.of("C").or("D")));
    assertChain("(A || B || (C && D))",
      ExpressionChain.orChains(ExpressionChain.of("A"), ExpressionChain.of("B"),
        ExpressionChain.andChains(ExpressionChain.of("C"), ExpressionChain.of("D"))));
  }

  private <T> void assertChain(String expected, ExpressionChain<T> chain) {
    assertNotNull(chain);
    assertNotNull(expected);
    assertEquals(expected, buildCondition(chain));
  }

  private static <T> String buildCondition(ExpressionChain<T> chain) {
    if (chain == null) {
      return "";
    }

    if (chain.getChain().size() == 0) {
      return chain.getValue().toString();
    }

    Iterator<ExpressionChain<T>> it = chain.getChain().iterator();
    StringBuilder sb = new StringBuilder("(").append(buildCondition(it.next()));
    while (it.hasNext()) {
      String condition = buildCondition(it.next());
      if (condition != null && condition.length() > 0) {
        sb.append(" ").append(operator(chain)).append(" ").append(condition);
      }
    }
    return sb.append(")").toString();
  }

  private static String operator(ExpressionChain<?> chain) {
    if (chain.isAnd()) {
      return "&&";
    }

    return "||";
  }
}
