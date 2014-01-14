import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Hashtable;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Printer;
import xtc.tree.Visitor;

/**
 * A C++ Class printer that prints the corresponding C++ Class structs from a Java AST
 * <p>This also builds a ClassTree class hierarchy from a Java AST
 *
 */
public class CPPClassPrinter extends Visitor
{
  private Printer out = null;

  private Hashtable<String, ArrayList<String>> extendedBy;
  // Takes the name of any class that has not yet been added to the ClassTree
  // Returns a list of classes not yet in the ClassTree that want to extend that class

  private Hashtable<String, GNode> pendingClasses;
  // Takes the name of a class that has not been added to the tree, because what it extends is not in the tree
  // Returns the GNode of the ClassDeclaration for that class

  private ClassTree classes;

  private ClassNode currClass;
  private String extension; // For extends retrieval
  private ArrayList<ClassParameter> params; // For parameter retrieval

  private String dataType; // For storing the data type of class members

  private boolean isStatic = false;

  /**
   * Create a new class printer
   *
   * @param p A printer
   */
  public CPPClassPrinter(Printer p)
  {
    out = p;

    pendingClasses = new Hashtable<String, GNode>();
    extendedBy = new Hashtable<String, ArrayList<String>>();

    classes = new ClassTree();

    currClass = null;
    extension = null;
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
   * Visit a ClassBody node
   *
   * @param n The node
   */
  public void visitClassBody(GNode n)
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
    if(n.get(3) != null && n.get(3) instanceof Node) // if class extends another class
    {
      dispatch((Node) n.get(3)); // Retrieve extends information

      String className = n.getString(1);
      ClassNode c = classes.add(className, extension);

      if(c != null) // The superclass was found
      {
        c.setPrint(true);

        currClass = c;

        dispatch((Node) n.get(5)); // Dispatch into the class body

        if(extendedBy.get(className) != null)
        {
          for(String pendingClass : extendedBy.get(className))
          {
            dispatch(pendingClasses.get(pendingClass)); // Now that the superclass has been created, create all of the classes that extend this class

            pendingClasses.remove(pendingClass);
          }

          extendedBy.remove(className);
        }
      }
      else // The superclass was not found
      {
        if(extendedBy.get(extension) == null)
          extendedBy.put(extension, new ArrayList<String>());

        extendedBy.get(extension).add(className); // Add the class to the list of classes that extends extension

        pendingClasses.put(className, n); // Store the GNode of the ClassDeclaration

        currClass = null;
        extension = null;
      }
    }
    else // otherwise, extend java.lang.Object
    {
      String className = n.getString(1);

      ClassNode c = classes.add(className);
      c.setPrint(true);

      currClass = c;

      dispatch((Node) n.get(5)); // Dispatch into the class body
    }
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

    classes.print(out); // Print the ClassTree's vtables

    out.flush();
  }

  /**
   * Visit a ConstructorDeclaration node
   *
   * @param n The node
   */
  public void visitConstructorDeclaration(GNode n)
  {
    params = new ArrayList<ClassParameter>();

    dispatch((Node) n.get(3)); // Retrive parameters information

    currClass.addConstructor(params);
  }

  /**
   * Visit a Declarator node
   *
   * @param n The node
   */
  public void visitDeclarator(GNode n)
  {
    boolean isPrimitive = (classes.findClass(dataType) == null);

    if(n.get(2) != null && n.get(2) instanceof Node)
    {
      StringWriter w = new StringWriter();
      Printer p = new Printer(w);
      new CPPPrinter(p).dispatch((Node) n.get(2));

      String initialization = w.toString();

      currClass.addMember(new ClassMember(dataType, n.getString(0), initialization, isStatic, isPrimitive));
    }
    else currClass.addMember(new ClassMember(dataType, n.getString(0), isStatic, isPrimitive));
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
   * Visit an Extension node
   *
   * @param n The node
   */
  public void visitExtension(GNode n) // FIXME: Allow extension of Arrays of a class
  {
    Node type = (Node) n.get(0);
    Node identifier = (Node) type.get(0);

    extension = identifier.getString(0);
  }

  /**
   * Visit a FieldDeclaration node
   *
   * @param n The node
   */
  public void visitFieldDeclaration(GNode n)
  {
    dispatch((Node) n.get(0));

    Node type = (Node) n.get(1);
    Node identifier = (Node) type.get(0);

    dataType = identifier.getString(0);

    if(dataType.equals("byte"))
      dataType = "signed char";
    else if(dataType.equals("boolean"))
      dataType = "bool";

    dispatch((Node) n.get(2));
  }

  /**
   * Visit a FormalParameter node
   *
   * @param n The node
   */
  public void visitFormalParameter(GNode n) // FIXME: Allow Arrays as parameters
  {
    Node type = (Node) n.get(1);
    Node identifier = (Node) type.get(0);
    Node dimensions = (Node) type.get(1);
    int dims = 0;

    String param = identifier.getString(0);

    if(param.equals("byte"))
      param = "signed char";
    else if(param.equals("boolean"))
      param = "bool";

    if(dimensions != null)
      dims = dimensions.size();

    params.add(new ClassParameter(param, dims));
  }

  /**
   * Visit a FormalParameters node
   *
   * @param n The node
   */
  public void visitFormalParameters(GNode n)
  {
    for(Object c : n)
    {
      if(c != null && c instanceof Node)
        dispatch((Node) c);
    }
  }

  /**
   * Visit a MethodDeclaration node
   *
   * @param n The node
   */
  public void visitMethodDeclaration(GNode n) // FIXME: Allow return types to be Arrays
  {
    ClassParameter retType;
    Node ret = (Node) n.get(2);

    if(ret.hasName("VoidType"))
      retType = new ClassParameter("void");
    else
    {
      Node type = ret;
      Node identifier = (Node) type.get(0);
      Node dimensions = (Node) type.get(1);
      int dims = 0;

      if(dimensions != null)
        dims = dimensions.size();

      String dType = identifier.getString(0);

      if(dType.equals("byte"))
        dType = "signed char";
      else if(dType.equals("boolean"))
        dType = "bool";

      retType = new ClassParameter(dType, dims);
    }

    dispatch((Node) n.get(0)); // Check modifiers

    params = new ArrayList<ClassParameter>();

    dispatch((Node) n.get(4)); // Retrive parameters information

    ClassMethod m = new ClassMethod(n.getString(3), retType, params, currClass.getName(), isStatic);

    currClass.addMethod(m);
  }

  /**
   * Visit a Modifier node
   *
   * @param n The node
   */
  public void visitModifier(GNode n)
  {
    if(n.getString(0).equals("static"))
      isStatic = true;
  }

  /**
   * Visit a Modifiers node
   *
   * @param n The node
   */
  public void visitModifiers(GNode n)
  {
    isStatic = false;

    for(Object c : n)
    {
      if(c != null && c instanceof Node)
        dispatch((Node) c);
    }
  }

  /**
   * Get the stored ClassTree class hierarchy
   *
   * @return The class tree for the previously traversed Java AST
   */
  public ClassTree getClassTree()
  {
    return classes;
  }
}
