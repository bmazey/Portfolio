// ===========================================================================
// This file has been generated by
// xtc Factory Factory, version 1.15.0,
// (C) 2004-2009 Robert Grimm,
// on Wednesday, February 23, 2011 at 2:21:22 PM.
// Edit at your own risk.
// ===========================================================================

package xtc.lang.jeannie;

import java.util.List;

import xtc.tree.Node;
import xtc.tree.GNode;

/**
 * Node factory <code>xtc.lang.jeannie.JeannieJavaFactory</code>.
 *
 * <p />This class has been generated by
 * the xtc Factory Factory, version 1.15.0,
 * (C) 2004-2009 Robert Grimm.
 */
public class JeannieJavaFactory {

  /** Create a new node factory. */
  public JeannieJavaFactory() {
    // Nothing to do.
  }

  /**
   * Create a block.
   *
   * @param statements The statements.
   * @return The generic node.
   */
  public Node block(List<Node> statements) {
    Node v$1 = GNode.create("Block", statements.size()).
      addAll(statements);
    return v$1;
  }

  /**
   * Create a call expression.
   *
   * @param nativeMethod The nativeMethod.
   * @return The generic node.
   */
  public Node cInJavaExpressionWithCEnv(String nativeMethod) {
    Node v$1 = GNode.create("ThisExpression", null);
    Node v$2 = GNode.create("PrimaryIdentifier", "cEnv");
    Node v$3 = GNode.create("Arguments", v$2);
    Node v$4 = GNode.create("CallExpression", v$1, null, nativeMethod, v$3);
    return v$4;
  }

  /**
   * Create a call expression.
   *
   * @param nativeMethod The nativeMethod.
   * @return The generic node.
   */
  public Node cInJavaExpressionWithoutCEnv(String nativeMethod) {
    Node v$1 = GNode.create("ThisExpression", null);
    Node v$2 = GNode.create("Arguments", false);
    Node v$3 = GNode.create("CallExpression", v$1, null, nativeMethod, v$2);
    return v$3;
  }

  /**
   * Create a block.
   *
   * @param nativeMethod The nativeMethod.
   * @param returnAbrupt The returnAbrupt.
   * @return The generic node.
   */
  public Node cInJavaStatementWithCEnv(String nativeMethod, 
                                       String returnAbrupt) {
    Node v$1 = GNode.create("ThisExpression", null);
    Node v$2 = GNode.create("PrimaryIdentifier", "cEnv");
    Node v$3 = GNode.create("Arguments", v$2);
    Node v$4 = GNode.create("CallExpression", v$1, null, nativeMethod, v$3);
    Node v$5 = GNode.create("ExpressionStatement", v$4);
    Node v$6 = GNode.create("ThisExpression", null);
    Node v$7 = GNode.create("SelectionExpression", v$6, returnAbrupt);
    Node v$8 = GNode.create("ReturnStatement", null);
    Node v$9 = GNode.create("ConditionalStatement", v$7, v$8, null);
    Node v$10 = GNode.create("Block", v$5, v$9);
    return v$10;
  }

  /**
   * Create a block.
   *
   * @param nativeMethod The nativeMethod.
   * @param returnAbrupt The returnAbrupt.
   * @return The generic node.
   */
  public Node cInJavaStatementWithoutCEnv(String nativeMethod, 
                                          String returnAbrupt) {
    Node v$1 = GNode.create("ThisExpression", null);
    Node v$2 = GNode.create("Arguments", false);
    Node v$3 = GNode.create("CallExpression", v$1, null, nativeMethod, v$2);
    Node v$4 = GNode.create("ExpressionStatement", v$3);
    Node v$5 = GNode.create("ThisExpression", null);
    Node v$6 = GNode.create("SelectionExpression", v$5, returnAbrupt);
    Node v$7 = GNode.create("ReturnStatement", null);
    Node v$8 = GNode.create("ConditionalStatement", v$6, v$7, null);
    Node v$9 = GNode.create("Block", v$4, v$8);
    return v$9;
  }

  /**
   * Create a method declaration.
   *
   * @param returnType The returnType.
   * @param name The name.
   * @param envType The envType.
   * @return The generic node.
   */
  public Node cInJavaCodeWithCEnv(Node returnType, String name, Node envType) {
    Node v$1 = GNode.create("Modifier", "private");
    Node v$2 = GNode.create("Modifier", "native");
    Node v$3 = GNode.create("Modifiers", v$1, v$2);
    Node v$4 = GNode.create("Type", returnType, null);
    Node v$5 = GNode.create("Modifier", "final");
    Node v$6 = GNode.create("Modifiers", v$5);
    Node v$7 = GNode.create("Type", envType, null);
    Node v$8 = GNode.create("FormalParameter", v$6, v$7, null, "cEnv", null);
    Node v$9 = GNode.create("FormalParameters", v$8);
    Node v$10 = GNode.create("MethodDeclaration", v$3, null, v$4, name, v$9, 
      null, null, null);
    return v$10;
  }

  /**
   * Create a method declaration.
   *
   * @param returnType The returnType.
   * @param name The name.
   * @return The generic node.
   */
  public Node cInJavaCodeWithoutCEnv(Node returnType, String name) {
    Node v$1 = GNode.create("Modifier", "private");
    Node v$2 = GNode.create("Modifier", "native");
    Node v$3 = GNode.create("Modifiers", v$1, v$2);
    Node v$4 = GNode.create("Type", returnType, null);
    Node v$5 = GNode.create("FormalParameters", false);
    Node v$6 = GNode.create("MethodDeclaration", v$3, null, v$4, name, v$5, 
      null, null, null);
    return v$6;
  }

  /**
   * Create a block.
   *
   * @param clazz The clazz.
   * @param actuals The actuals.
   * @param result The result.
   * @return The generic node.
   */
  public Node closureExpression(Node clazz, List<Node> actuals, String result) {
    Node v$1 = GNode.create("Modifiers", false);
    Node v$2 = GNode.create("Type", clazz, null);
    Node v$3 = GNode.create("Arguments", actuals.size()).
      addAll(actuals);
    Node v$4 = GNode.create("NewClassExpression", null, null, clazz, v$3, 
      null);
    Node v$5 = GNode.create("Declarator", "jEnv", null, v$4);
    Node v$6 = GNode.create("Declarators", v$5);
    Node v$7 = GNode.create("FieldDeclaration", v$1, v$2, v$6);
    Node v$8 = GNode.create("PrimaryIdentifier", "jEnv");
    Node v$9 = GNode.create("SelectionExpression", v$8, result);
    Node v$10 = GNode.create("ReturnStatement", v$9);
    Node v$11 = GNode.create("Block", v$7, v$10);
    return v$11;
  }

  /**
   * Create a block.
   *
   * @param clazz The clazz.
   * @param actuals The actuals.
   * @return The generic node.
   */
  public Node closureStatement(Node clazz, List<Node> actuals) {
    Node v$1 = GNode.create("Modifiers", false);
    Node v$2 = GNode.create("Type", clazz, null);
    Node v$3 = GNode.create("Arguments", actuals.size()).
      addAll(actuals);
    Node v$4 = GNode.create("NewClassExpression", null, null, clazz, v$3, 
      null);
    Node v$5 = GNode.create("Declarator", "jEnv", null, v$4);
    Node v$6 = GNode.create("Declarators", v$5);
    Node v$7 = GNode.create("FieldDeclaration", v$1, v$2, v$6);
    Node v$8 = GNode.create("Block", v$7);
    return v$8;
  }

  /**
   * Create a field declaration.
   *
   * @param type The type.
   * @param name The name.
   * @return The generic node.
   */
  public Node declareField(Node type, String name) {
    Node v$1 = GNode.create("Modifiers", false);
    Node v$2 = GNode.create("Type", type, null);
    Node v$3 = GNode.create("Declarator", name, null, null);
    Node v$4 = GNode.create("Declarators", v$3);
    Node v$5 = GNode.create("FieldDeclaration", v$1, v$2, v$4);
    return v$5;
  }

  /**
   * Create a selection expression.
   *
   * @param name The name.
   * @return The generic node.
   */
  public Node getThisDotField(String name) {
    Node v$1 = GNode.create("ThisExpression", null);
    Node v$2 = GNode.create("SelectionExpression", v$1, name);
    return v$2;
  }

  /**
   * Create a method declaration.
   *
   * @param returnType The returnType.
   * @param name The name.
   * @param envType The envType.
   * @param expr The expr.
   * @return The generic node.
   */
  public Node javaInCExpression(Node returnType, String name, Node envType, 
                                Node expr) {
    Node v$1 = GNode.create("Modifier", "private");
    Node v$2 = GNode.create("Modifiers", v$1);
    Node v$3 = GNode.create("Type", returnType, null);
    Node v$4 = GNode.create("Modifier", "final");
    Node v$5 = GNode.create("Modifiers", v$4);
    Node v$6 = GNode.create("Type", envType, null);
    Node v$7 = GNode.create("FormalParameter", v$5, v$6, null, "cEnv", null);
    Node v$8 = GNode.create("FormalParameters", v$7);
    Node v$9 = GNode.create("QualifiedIdentifier", "Exception");
    Node v$10 = GNode.create("ThrowsClause", v$9);
    Node v$11 = GNode.create("ReturnStatement", expr);
    Node v$12 = GNode.create("Block", v$11);
    Node v$13 = GNode.create("MethodDeclaration", v$2, null, v$3, name, v$8, 
      null, v$10, v$12);
    return v$13;
  }

  /**
   * Create a method declaration.
   *
   * @param name The name.
   * @param envType The envType.
   * @param expr The expr.
   * @return The generic node.
   */
  public Node javaInCExpressionVoid(String name, Node envType, Node expr) {
    Node v$1 = GNode.create("Modifier", "private");
    Node v$2 = GNode.create("Modifiers", v$1);
    Node v$3 = GNode.create("VoidType", false);
    Node v$4 = GNode.create("Modifier", "final");
    Node v$5 = GNode.create("Modifiers", v$4);
    Node v$6 = GNode.create("Type", envType, null);
    Node v$7 = GNode.create("FormalParameter", v$5, v$6, null, "cEnv", null);
    Node v$8 = GNode.create("FormalParameters", v$7);
    Node v$9 = GNode.create("QualifiedIdentifier", "Exception");
    Node v$10 = GNode.create("ThrowsClause", v$9);
    Node v$11 = GNode.create("ExpressionStatement", expr);
    Node v$12 = GNode.create("Block", v$11);
    Node v$13 = GNode.create("MethodDeclaration", v$2, null, v$3, name, v$8, 
      null, v$10, v$12);
    return v$13;
  }

  /**
   * Create a method declaration.
   *
   * @param name The name.
   * @param envType The envType.
   * @param stmt The stmt.
   * @return The generic node.
   */
  public Node javaInCStatement(String name, Node envType, Node stmt) {
    Node v$1 = GNode.create("Modifier", "private");
    Node v$2 = GNode.create("Modifiers", v$1);
    Node v$3 = GNode.create("VoidType", false);
    Node v$4 = GNode.create("Modifier", "final");
    Node v$5 = GNode.create("Modifiers", v$4);
    Node v$6 = GNode.create("Type", envType, null);
    Node v$7 = GNode.create("FormalParameter", v$5, v$6, null, "cEnv", null);
    Node v$8 = GNode.create("FormalParameters", v$7);
    Node v$9 = GNode.create("QualifiedIdentifier", "Exception");
    Node v$10 = GNode.create("ThrowsClause", v$9);
    Node v$11 = GNode.create("Block", stmt);
    Node v$12 = GNode.create("MethodDeclaration", v$2, null, v$3, name, v$8, 
      null, v$10, v$11);
    return v$12;
  }

  /**
   * Create a block.
   *
   * @param name The name.
   * @return The generic node.
   */
  public Node loadLibrary(Node name) {
    Node v$1 = GNode.create("PrimaryIdentifier", "System");
    Node v$2 = GNode.create("Arguments", name);
    Node v$3 = GNode.create("CallExpression", v$1, null, "loadLibrary", v$2);
    Node v$4 = GNode.create("ExpressionStatement", v$3);
    Node v$5 = GNode.create("Block", v$4);
    return v$5;
  }

  /**
   * Create a block.
   *
   * @param result The result.
   * @param value The value.
   * @param abrupt The abrupt.
   * @return The generic node.
   */
  public Node returnResult(String result, Node value, String abrupt) {
    Node v$1 = GNode.create("ThisExpression", null);
    Node v$2 = GNode.create("SelectionExpression", v$1, result);
    Node v$3 = GNode.create("Expression", v$2, "=", value);
    Node v$4 = GNode.create("ExpressionStatement", v$3);
    Node v$5 = GNode.create("ThisExpression", null);
    Node v$6 = GNode.create("SelectionExpression", v$5, abrupt);
    Node v$7 = GNode.create("BooleanLiteral", "true");
    Node v$8 = GNode.create("Expression", v$6, "=", v$7);
    Node v$9 = GNode.create("ExpressionStatement", v$8);
    Node v$10 = GNode.create("ReturnStatement", null);
    Node v$11 = GNode.create("Block", v$4, v$9, v$10);
    return v$11;
  }

  /**
   * Create a block.
   *
   * @param abrupt The abrupt.
   * @return The generic node.
   */
  public Node returnVoid(String abrupt) {
    Node v$1 = GNode.create("ThisExpression", null);
    Node v$2 = GNode.create("SelectionExpression", v$1, abrupt);
    Node v$3 = GNode.create("BooleanLiteral", "true");
    Node v$4 = GNode.create("Expression", v$2, "=", v$3);
    Node v$5 = GNode.create("ExpressionStatement", v$4);
    Node v$6 = GNode.create("ReturnStatement", null);
    Node v$7 = GNode.create("Block", v$5, v$6);
    return v$7;
  }

  /**
   * Create an expression.
   *
   * @param name The name.
   * @param value The value.
   * @return The generic node.
   */
  public Node setThisDotField(String name, Node value) {
    Node v$1 = GNode.create("ThisExpression", null);
    Node v$2 = GNode.create("SelectionExpression", v$1, name);
    Node v$3 = GNode.create("Expression", v$2, "=", value);
    return v$3;
  }

}
