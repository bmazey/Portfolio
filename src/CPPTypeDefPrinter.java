import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Printer;
import xtc.tree.Visitor;

/*
 *  A C++ typedef printer that prints the necessary typedefs for a given Java AST
 *
 */
public class CPPTypeDefPrinter extends Visitor
{
  private Printer out;

  /**
   * Create a new typedef printer
   *
   * @param p A printer
   */
  public CPPTypeDefPrinter(Printer p)
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
    String className = n.getString(1);

    out.indent();
    out.pln("typedef __rt::Ptr<__" + className + ", __rt::object_policy> " + className + ";");
  }

  /**
   * Visit a CompilationUnit node
   *
   * @param n The node
   */
  public void visitCompilationUnit(GNode n)
  {
    for(Object c : n)
    {
      if(c != null && c instanceof Node)
        dispatch((Node) c);
    }

    out.pln();
    out.pln();
    out.flush();
  }
}
