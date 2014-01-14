import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Printer;
import xtc.tree.Visitor;

/** 
 * A C++ header printer that prints the necessary forward declarations for the C++ source code using a Java AST
 *
 */
public class CPPHeaderPrinter extends Visitor
{
  private Printer out; //The printer
 
  /**
   * Create a new header printer
   *
   * @param p A printer
   */
  public CPPHeaderPrinter(Printer p)
  {
    out = p;
  }

  /**
   * Visit a generic node
   *
   * @param n The node
   */
  public void visit(Node n)
  {
    for(Object c : n)
    {
      if(c != null && c instanceof Node)
        dispatch((Node) c);
    }
  }

  /**
   * Visit a ClassDeclaration node
   *
   * @param n The node
   */
  public void visitClassDeclaration(GNode n)
  {
    out.indent();

    //dispatch((Node) n.get(0)); // Print modifiers

    out.pln("struct __" + n.getString(1) + ";"); // Print class declaration

    out.indent();
    out.p("struct __");
    out.p(n.getString(1));
    out.pln("_VT;");
    out.pln();
  }

  /**
   * Visit a CompilationUnit node
   *
   * @param n The node
   */
  public void visitCompilationUnit(GNode n)
  {
    out.indent();
    out.pln("#include <iostream>");
    out.indent();
    out.pln("#include \"java_lang.cc\"");
    out.indent();
    out.pln("#include \"ptr.h\"");
    out.pln();
    out.indent();
    out.pln("using namespace std;");
    out.indent();
    out.pln("using namespace java::lang;");
    out.pln();

    for(Object c : n)
    {
      if(c != null && c instanceof Node)
        dispatch((Node) c);
    }

    out.flush();
  }

  /**
   * Visit a ConstructorDeclaration node
   *
   * @param n The node
   */
  public void visitConstructorDeclaration(GNode n)
  {
    out.indent();

    // dispatch((Node) n.get(0)); // Print modifiers

    out.p(n.getString(2));
    out.p("(");

    dispatch((Node) n.get(3)); // Print the parameters

    out.pln(");");
  }

  /**
   * Visit a Declarator node
   *
   * @param n The node
   */
  public void visitDeclarator(GNode n)
  {
    out.p(n.getString(0));
  }

  /**
   * Visit a Declarators node
   *
   * @param n The node
   */
  public void visitDeclarators(GNode n)
  {
    for(Object c : n)
    {
      if(c != null && c instanceof Node)
        dispatch((Node) c);
    }
  }

  /**
   * Visit a FieldDeclaration node
   *
   * @param n The node
   */
  public void visitFieldDeclaration(GNode n)
  {
    out.indent();
    // dispatch((Node) n.get(0)); // Print modifiers

    dispatch((Node) n.get(1)); // Print data type

    out.p(" ");

    dispatch((Node) n.get(2)); // Print variable names

    out.pln(";");
  }

  /**
   * Visit a FormalParameter node
   *
   * @param n The node
   */
  public void visitFormalParameter(GNode n)
  {
    dispatch((Node) n.get(1)); // Print the type of the parameter

    out.p(" ");
    out.p(n.getString(3)); // Print parameter name
  }
  
  /**
   * Visit a FormalParameters node
   *
   * @param n The node
   */
  public void visitFormalParameters(GNode n)
  {
    Node nextParam = null;

    for(Object c : n)
    {
      if(c != null && c instanceof Node)
      {
        if(nextParam != null)
        {
          dispatch(nextParam);

          // Insert commas between parameters
          out.p(", ");
        }

        nextParam = (Node) c;
      }
    }

    if(nextParam != null)
    {
      dispatch(nextParam);
    }
  }
  
  /**
   * Visit a MethodDeclaration node
   *
   * @param n The node
   */
  public void visitMethodDeclaration(GNode n)
  {
    if(!n.getString(3).equals("main"))
    {
      out.indent();

      // dispatch((Node) n.get(0)); // Print modifiers

      dispatch((Node) n.get(2)); // Print return type

      out.p(" ");
      out.p(n.getString(3));
      out.p("(");

      dispatch((Node) n.get(4)); // Print the parameters

      out.pln(");");
    }
  }

  /**
   * Visit a Modifier node
   *
   * @param n The node
   */
  public void visitModifier(GNode n)
  {
    out.p(n.getString(0) + " ");
  }

  /**
   * Visit a Modifiers node
   *
   * @param n The node
   */
  public void visitModifiers(GNode n)
  {
    for(Object c : n)
    {
      if(c != null && c instanceof Node)
        dispatch((Node) c);
    }
  }

  /**
   * Visit a PrimitiveType node
   *
   * @param n The node
   */
  public void visitPrimitiveType(GNode n)
  {
    if(n.getString(0).equals("boolean"))
      out.p("bool");
    else out.p(n.getString(0));
  }

  /**
   * Visit a QualifiedIdentifier node
   *
   * @param n The node
   */
  public void visitQualifiedIdentifier(GNode n)
  {
    out.p(n.getString(0));
  }
  
  /**
   * Visit a Type node
   *
   * @param n The node
   */
  public void visitType(GNode n)
  {
    for(Object c : n)
    {
      if(c != null && c instanceof Node)
      {
        dispatch((Node) c);
      }
    }
  }

  /**
   * Visit a VoidType node
   *
   * @param n The node
   */
  public void visitVoidType(GNode n)
  {
    out.p("void");
  }
}
