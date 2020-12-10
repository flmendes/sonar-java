package org.sonar.java.checks;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.Arguments;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.LambdaExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree;

@Rule(key = "S6104")
public class NullReturnedOnComputeIfPresentOrAbsentCheck extends IssuableSubscriptionVisitor {
  public static final int REMEDIATION_COST_IN_MINUTES = 10;
  public static final String MESSAGE = "Use \"Map.containsKey(key)\" followed by \"Map.put(key, null)\" to add null values.";

  @Override
  public List<Tree.Kind> nodesToVisit() {
    return Collections.singletonList(Tree.Kind.METHOD_INVOCATION);
  }

  @Override
  public void visitNode(Tree tree) {
    MethodInvocationTree methodInvocation = (MethodInvocationTree) tree;
    if (!isTargetMethod(methodInvocation)) {
      return;
    }
    Arguments arguments = methodInvocation.arguments();
    if (arguments.size() < 2) {
      return;
    }
    ExpressionTree operator = arguments.get(1);
    if (!isABinaryOperator(operator)) {
      return;
    }
    LambdaExpressionTree lambda = (LambdaExpressionTree) operator;
    if (!returnsNullExplicitly(lambda)) {
      return;
    }
    reportIssue(methodInvocation, MESSAGE, Collections.emptyList(), REMEDIATION_COST_IN_MINUTES);
  }

  public static boolean isTargetMethod(MethodInvocationTree invocation) {
    Symbol symbol = invocation.symbol();
    Symbol targetObject = symbol.owner();
    if (targetObject == null || targetObject.type() == null) {
      return false;
    }
    if (!targetObject.type().is(Map.class.getCanonicalName())) {
      return false;
    }
    return symbol.name().equals("computeIfPresent");
  }

  public static boolean isABinaryOperator(ExpressionTree expression) {
    return expression.is(Tree.Kind.LAMBDA_EXPRESSION) || expression.symbolType().name().equals("BiFunction");
  }

  public static boolean returnsNullExplicitly(LambdaExpressionTree lambda) {
    return lambda.body().is(Tree.Kind.NULL_LITERAL);
  }
}
