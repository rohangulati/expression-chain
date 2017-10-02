package com.rohangulati.expressionchain;

import com.rohangulati.expressionchain.util.Preconditions;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by rohan
 * <p>
 * An implementation of expression tree
 */
public final class ExpressionChain<T> {

  private static final ChainBehaviour OR = new OrBehaviour();

  private static final ChainBehaviour AND = new AndBehaviour();

  public static <T> ExpressionChain<T> andOperator(Collection<T> values) {
    Preconditions.checkNotEmpty(values);
    Iterator<T> iterator = values.iterator();
    ExpressionChain<T> chain = new ExpressionChain<T>(iterator.next());
    chain.operator = AND;
    while (iterator.hasNext()) {
      chain.and(iterator.next());
    }
    return chain;
  }

  @SafeVarargs
  public static <T> ExpressionChain<T> andOperator(T... values) {
    Preconditions.checkNotEmpty(values);
    Preconditions.checkNotNull(values);
    ExpressionChain<T> chain = new ExpressionChain<T>(values[0]);
    chain.operator = AND;
    for (int i = 1; i < values.length; i++) {
      chain.and(values[i]);
    }
    return chain;
  }

  @SafeVarargs
  public static <T> ExpressionChain<T> andChains(ExpressionChain<T>... values) {
    Preconditions.checkNotEmpty(values);
    // no need for a parent operator
    if (values.length == 1) {
      return values[0];
    }
    ExpressionChain<T> chain = new ExpressionChain<T>(Arrays.asList(values));
    chain.operator = AND;
    return chain;
  }

  public static <T> ExpressionChain<T> andChains(Collection<ExpressionChain<T>> values) {
    Preconditions.checkNotEmpty(values);
    // no need for a parent operator
    if (values.size() == 1) {
      return values.iterator().next();
    }
    ExpressionChain<T> and = new ExpressionChain<T>(values);
    and.operator = AND;
    return and;
  }

  public static <T> ExpressionChain<T> orOperator(Collection<T> values) {
    Preconditions.checkNotEmpty(values);
    Iterator<T> iterator = values.iterator();
    ExpressionChain<T> chain = new ExpressionChain<T>(iterator.next());
    chain.operator = OR;
    while (iterator.hasNext()) {
      chain.or(iterator.next());
    }
    return chain;
  }

  @SafeVarargs
  public static <T> ExpressionChain<T> orOperator(T... values) {
    Preconditions.checkNotEmpty(values);
    ExpressionChain<T> chain = new ExpressionChain<T>(values[0]);
    chain.operator = OR;
    for (int i = 1; i < values.length; i++) {
      chain.or(values[i]);
    }
    return chain;
  }

  public static <T> ExpressionChain<T> orChains(Collection<ExpressionChain<T>> values) {
    Preconditions.checkNotEmpty(values);
    // no need for a parent operator
    if (values.size() == 1) {
      return values.iterator().next();
    }
    ExpressionChain<T> or = new ExpressionChain<T>(values);
    or.operator = OR;
    return or;
  }

  @SafeVarargs
  public static <T> ExpressionChain<T> orChains(ExpressionChain<T>... values) {
    Preconditions.checkNotEmpty(values);
    // no need for a parent operator
    if (values.length == 1) {
      return values[0];
    }
    ExpressionChain<T> or = new ExpressionChain<T>(Arrays.asList(values));
    or.operator = OR;
    return or;
  }

  public static <T> ExpressionChain<T> of(T value) {
    Preconditions.checkNotNull(value);
    return new ExpressionChain<T>(value);
  }

  @Nullable
  private T value;

  private ChainBehaviour operator = AND;

  private List<ExpressionChain<T>> chain = new LinkedList<>();

  ExpressionChain(T value) {
    this.value = value;
  }

  ExpressionChain(Collection<ExpressionChain<T>> chain) {
    this.chain.addAll(chain);
  }

  ExpressionChain(ExpressionChain<T> other) {
    this.value = other.value;
    this.operator = other.operator;
    this.chain.addAll(other.chain);
  }

  public ExpressionChain<T> and(T criteria) {
    this.operator.and(this, criteria);
    return this;
  }

  public ExpressionChain<T> and(ExpressionChain<T> chain) {
    this.operator.and(this, chain);
    return this;
  }

  public ExpressionChain<T> and(Optional<T> optional) {
    Preconditions.checkNotNull(optional);
    optional.ifPresent(value -> this.operator.and(this, value));
    return this;
  }

  public ExpressionChain<T> or(T criteria) {
    this.operator.or(this, criteria);
    return this;
  }

  public ExpressionChain<T> or(ExpressionChain<T> chain) {
    this.operator.or(this, chain);
    return this;
  }

  public ExpressionChain<T> or(Optional<T> optional) {
    Preconditions.checkNotNull(optional);
    optional.ifPresent(value -> this.operator.or(this, value));
    return this;
  }

  public boolean isOperator() {
    return value == null;
  }

  public boolean isAnd() {
    return AND == this.operator;
  }

  public boolean isOr() {
    return AND == this.operator;
  }

  public Optional<T> getValue() {
    return Optional.ofNullable(value);
  }

  public List<ExpressionChain<T>> getChain() {
    return Collections.unmodifiableList(chain);
  }

  private interface ChainBehaviour {

    <T> void and(ExpressionChain<T> root, T value);

    <T> void and(ExpressionChain<T> root, ExpressionChain<T> value);

    <T> void or(ExpressionChain<T> root, T value);

    <T> void or(ExpressionChain<T> root, ExpressionChain<T> value);
  }

  private static final class AndBehaviour implements ChainBehaviour {

    @Override
    public <T> void and(ExpressionChain<T> root, T value) {
      Preconditions.checkNotNull(value);
      and(root, new ExpressionChain<T>(value));
    }

    @Override
    public <T> void and(ExpressionChain<T> root, ExpressionChain<T> node) {
      Preconditions.checkNotNull(root);
      Preconditions.checkArgument(AND == root.operator);
      Preconditions.checkNotNull(node);

      // root is already AND chain, so add the new node to its chain
      if (root.isOperator()) {
        root.chain.add(node);
        return;
      }

      ExpressionChain<T> clone = new ExpressionChain<>(root);

      // make current node operator and add the current condition and new value to chain
      root.value = null;
      root.chain.add(clone);
      root.chain.add(node);
    }

    @Override
    public <T> void or(ExpressionChain<T> root, T value) {
      Preconditions.checkNotNull(value);
      or(root, new ExpressionChain<T>(value));
    }

    @Override
    public <T> void or(ExpressionChain<T> root, ExpressionChain<T> node) {
      Preconditions.checkNotNull(root);
      Preconditions.checkArgument(AND == root.operator);
      Preconditions.checkNotNull(node);

      ExpressionChain<T> clone = new ExpressionChain<T>(root);

      // make current node or operator and add 2 children
      root.value = null;
      root.operator = OR;
      root.chain.clear();
      root.chain.add(clone);
      root.chain.add(node);
    }
  }

  private static final class OrBehaviour implements ChainBehaviour {

    @Override
    public <T> void and(ExpressionChain<T> root, T value) {
      Preconditions.checkNotNull(value);
      and(root, new ExpressionChain<T>(value));
    }

    @Override
    public <T> void and(ExpressionChain<T> root, ExpressionChain<T> node) {
      Preconditions.checkNotNull(root);
      Preconditions.checkArgument(OR == root.operator);
      Preconditions.checkNotNull(node);

      ExpressionChain<T> clone = new ExpressionChain<T>(root);

      // make root AND operator and add 2 children
      root.value = null;
      root.operator = AND;
      root.chain.clear();
      root.chain.add(clone);
      root.chain.add(node);
    }

    @Override
    public <T> void or(ExpressionChain<T> root, T value) {
      Preconditions.checkNotNull(value);
      or(root, new ExpressionChain<>(value));
    }

    @Override
    public <T> void or(ExpressionChain<T> root, ExpressionChain<T> node) {
      Preconditions.checkNotNull(root);
      Preconditions.checkArgument(OR == root.operator);
      Preconditions.checkNotNull(node);

      // root is already OR chain, so add the new node to its chain
      if (root.isOperator()) {
        root.chain.add(node);
        return;
      }

      ExpressionChain<T> clone = new ExpressionChain<>(root);

      // make current node operator and add the current condition and new value to chain
      root.value = null;
      root.chain.add(clone);
      root.chain.add(node);
    }
  }
}
