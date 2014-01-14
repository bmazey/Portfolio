import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Hashtable;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Printer;
import xtc.tree.Visitor;

/**
 *  The main C++ source printer that translates a Java AST into C++ source code
 *
 *  * Only allows one-dimensional arrays
 *  * Need to incorporate modifiers (private, static, final, etc.)
 *  * Note that when printing:
 *      -variables and methods escape '_' with '__' (ignoring __isa and __delete())
 *      -variables end with '_var'
 *      -methods end with '_' and then a list of parameter types separated by '_'
 *
 */
public class CPPPrinter extends Visitor
{
  protected static Printer out = null;

  private int printCode = 0;
  // 0 = nothing found
  // 1 = reference to some out member found
  // 2 = reference to System.out found => rewrite the print or println statement and reset printCode to 0

  private int dimensions = 0; // Dimension of array (for array variable declaration)

  private boolean printMain = false; // Whether or not to print main methods
  private boolean hasDefaultConstructor = false; // Whether or not the current class creates its own default constructor

  private String inClass = null; // What class you are in
  private String className = null; // Standard Java class name
  private ClassNode currClass = null; // ClassNode of the current class

  private boolean inMethod = false; // If you are currently in a method
  private ArrayList<String> localVars = null; // List of local variables within a method (including parameters) -- nulls indicate that you are entering a deeper scope
  private Hashtable<String, ArrayList<String>> localTypes = null; // Hashtable from variable names to their types within a method

  private String lastType = null; // If the a data type was previously retrieved its name will be stored here.  Otherwise, this contains null.

  private ClassTree classes = null; // Class tree, to be retrieved from CPPClassPrinter, if necessary

  private String mainClass; // The name of the main class in the Java program

  private boolean isStatic = false; // If a 'static' modifier was found
  private boolean staticMethod = false; // If you are in a static method

  private boolean inConstructor = false; // If you are printing a constructor
  private boolean inInitializer = false; // If you are printing an initializer

  private int tempCount = 0; // Counter for number of temporary variables

  private boolean genericClassSelector = false; // Used by SelectionExpression to determine whether the selection is from an instance of a class or from the generic class itself

  /**
   * Create a new translator for a Java AST with no main class
   *
   * @param p  A printer
   */
  public CPPPrinter(Printer p)
  {
    out = p;
  }

  /**
   * Create a new translator with the given main class
   *
   * @param p  A printer
   * @param mc The name of the main class
   */
  public CPPPrinter(Printer p, String mc)
  {
    out = p;
    mainClass = mc;
  }

  /**
   * Create a new translator with the given main class and class hierarchy
   *
   * @param p  A printer
   * @param mc The name of the main class
   * @param c  A ClassTree instance containing a class hierarchy
   */
  public CPPPrinter(Printer p, String mc, ClassTree c)
  {
    out = p;
    mainClass = mc;
    classes = c;
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
   * Visit an AdditiveExpression node
   *
   * @param n The node
   */
  public void visitAdditiveExpression(GNode n)
  {
    dispatch((Node) n.get(0)); // Print first value

    out.p(" " + n.getString(1) + " ");

    dispatch((Node) n.get(2)); // Print second value
  }

  /**
   * Visit an Arguments node
   *
   * @param n The node
   */
  public void visitArguments(GNode n)
  {
    Node nextArg = null;

    for(Object c : n)
    {
      if(c != null && c instanceof Node)
      {
        if(nextArg != null)
        {
          dispatch(nextArg);

          // Insert commas between arguments
          out.p(", ");
        }

        nextArg = (Node) c;
      }
    }

    if(nextArg != null)
    {
      dispatch(nextArg);
    }
  }

  /**
   * Visit a BasicCastExpression node
   *
   * @param n The node
   */
  public void visitBasicCastExpression(GNode n)
  {
    out.p("(");
    dispatch((Node) n.get(0)); // Print type to be casted into
    out.p(")");

    out.p(" (");
    dispatch((Node) n.get(2)); // Print what is to be casted
    out.p(")");
  }

  /**
   * Visit a BasicForControl node
   *
   * @param n The node
   */
  public void visitBasicForControl(GNode n)
  {
    // Dispatch declarations of for loop
    dispatch((Node) n.get(0));
    dispatch((Node) n.get(1));

    out.p(" ");

    dispatch((Node) n.get(2));

    out.p("; ");

    dispatch((Node) n.get(3)); // Dispatch conditionals of for loop

    out.p("; ");

    dispatch((Node) n.get(4)); // Dispatch updates of for loop
  }

  /**
   * Visit a Block node
   *
   * @param n The node
   */
  public void visitBlock(GNode n)
  {
    out.indent();
    out.pln("{");
    out.incr();

    if(localVars != null)
      localVars.add(null);

    if(inInitializer)
    {
      // Look for if the super constructor is called
      boolean callsSuper = false;

      if(n.size() > 0)
      {
        Node checking = (Node) n.get(0);

        // Check for ExpressionStatement
        if(checking != null && checking instanceof Node && checking.hasName("ExpressionStatement") && checking.size() > 0)
        {
          checking = (Node) checking.get(0);

          // Check for CallExpression and if it is a call to the super() constructor
          if(checking != null && checking instanceof Node && checking.hasName("CallExpression") && checking.get(0) == null && checking.get(2) != null && checking.get(2) instanceof String && checking.getString(2).equals("super"))
            callsSuper = true;
          else callsSuper = false;
        }
        else callsSuper = false;
      }
      else callsSuper = false;

      // Print generic initializer only if the super() constructor is not the first line of code in the block
      if(!callsSuper)
      {
        if(inInitializer)
        {
          out.indent();
          out.pln("__" + currClass.getSuperclass() + "::init(__this);");
        }
      }
    }

    for(Object c : n)
    {
      if(c != null && c instanceof Node)
        dispatch((Node) c);
    }

    if(inInitializer)
    {
      out.indent();
      out.pln("return __this;");
    }

    if(localVars != null)
    {
       String currVar = localVars.remove(localVars.size() - 1);
       while(currVar != null)
       {
         localTypes.get(currVar).remove(localTypes.get(currVar).size() - 1);

         if(localTypes.get(currVar).size() == 0)
           localTypes.remove(currVar);

         currVar = localVars.remove(localVars.size() - 1);
       }
    }

    out.decr();
    out.indent();
    out.pln("}");
    out.pln();
  }

  /**
   * Visit a BooleanLiteral node
   *
   * @param n The node
   */
  public void visitBooleanLiteral(GNode n)
  {
    out.p(n.getString(0));
  }

  /**
   * Visit a BreakStatement node
   *
   * @param n The node
   */
  public void visitBreakStatement(GNode n)
  {
    out.indent();
    out.p("break");

    if(n.get(0) != null)
      out.p(" " + n.getString(0));

    out.pln(";");
  }

  /**
   * Visit a CallExpression node
   *
   * @param n The node
   */
  public void visitCallExpression(GNode n)
  {
    printCode = 0;

    if(n.get(3) == null) // If call has already been printed and stored to a temporary variable, Arguments will be set to null in ExpressionStatement
    {
      out.p(n.getString(0)); // Print the temporary variable that contains the result of a previous call
    }
    else if(n.get(0) == null) // Calling method from within class
    {
      if(staticMethod)
      {
        out.p(inClass + "::" + n.getString(2) + "(");

        Node args = (Node) n.get(3);

        dispatch((Node) n.get(3)); // Print the arguments

        out.p(")");
      }
      else if(n.getString(2).equals("super"))
      {
        out.p("__" + currClass.getSuperclass() + "::init(");

        if(inConstructor)
          out.p("this");
        else out.p("__this");

        Node args = (Node) n.get(3);

        if(args.size() > 0) // Print comma only if there are other parameters
          out.p(", ");

        dispatch((Node) n.get(3)); // Print the arguments

        out.p(")");
      }
      else if(inConstructor)
      {
        out.p("this->__vptr->" + n.getString(2) + "(this");

        Node args = (Node) n.get(3);

        if(args.size() > 0) // Print comma only if there are other parameters
          out.p(", ");

        dispatch((Node) n.get(3)); // Print the arguments

        out.p(")");
      }
      else 
      {
        out.p("__this->__vptr->" + n.getString(2) + "(__this");

        Node args = (Node) n.get(3);

        if(args.size() > 0) // Print comma only if there are other parameters
          out.p(", ");

        dispatch((Node) n.get(3)); // Print the arguments

        out.p(")");
      }
    }
    else if(n.get(0) instanceof Node && ((Node) n.get(0)).hasName("SuperExpression"))
    {
      dispatch((Node) n.get(0)); // Print the super node

      out.p("::" + n.getString(2) + "(__this");

      Node args = (Node) n.get(3);

      if(args.size() > 0) // Print comma only if there are other parameters
        out.p(", ");

      dispatch((Node) n.get(3)); // Print the arguments

      out.p(")");
    }
    else
    {
      dispatch((Node) n.get(0)); // Print the object that is calling a method

      if(printCode == 2)
      {
        printCode = 0;

        if(n.getString(2).equals("print"))
        {
          out.p("cout << ");

          dispatch((Node) n.get(3)); // Print the value to be printed
        }
        else if(n.getString(2).equals("println"))
        {
          out.p("cout << ");

          dispatch((Node) n.get(3)); // Print the value to be printed

          out.p(" << endl");
        }
        else // Other System.out calls not supported
        {
          out.p("System->out->");
          out.p(n.getString(2));
          out.p("(");

          dispatch((Node) n.get(3)); // Print the arguments

          out.p(")");
        }
      }
      else
      {
        out.p("->__vptr->" + n.getString(2) + "(");

        dispatch((Node) n.get(0)); // Print the object whose method is being called

        Node args = (Node) n.get(3);

        if(args.size() > 0) // Print comma only if there are other parameters
          out.p(", ");

        dispatch((Node) n.get(3)); // Print the arguments

        out.p(")");
      }
    }
  }

  /**
   * Visit a CaseClause node
   *
   * @param n The node
   */
  public void visitCaseClause(GNode n)
  {
    out.indent();
    out.p("case ");

    dispatch((Node) n.get(0)); // Print case value

    out.pln(":");
    out.incr();

    for(int i = 1; i < n.size(); i++)
    {
      dispatch((Node) n.get(i)); // Dispatch into case code
    }

    out.decr();
  }

  /**
   * Visit a CastExpression node
   *
   * @param n The node
   */
  public void visitCastExpression(GNode n)
  {
    out.p("(");
    dispatch((Node) n.get(0)); // Print type to be casted into
    out.p(")");

    out.p(" (");
    dispatch((Node) n.get(1)); // Print what is to be casted
    out.p(")");
  }

  /**
   * Visit a CatchClause node
   *
   * @param n The node
   */
  public void visitCatchClause(GNode n)
  {
    out.indent();
    out.p("catch(");

    dispatch((Node) n.get(0)); // Print exception to be caught

    out.pln(")");

    dispatch((Node) n.get(1)); // Dispatch into catch block
  }

  /**
   * Visit a CharacterLiteral node
   *
   * @param n The node
   */
  public void visitCharacterLiteral(GNode n)
  {
    out.p(n.getString(0));
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
    out.indent();

    // dispatch((Node) n.get(0)); // Check modifiers

    className = n.getString(1);
    inClass = "__" + className;
    currClass = classes.findClass(className); // Find the ClassNode for this class in the ClassTree

    hasDefaultConstructor = false;

    dispatch((Node) n.get(5)); // Dispatch into the class body

    if(!hasDefaultConstructor)
    {
      out.pln();
      out.indent();
      out.p(inClass + "::" + inClass + "() : __vptr(&__vtable)");

      for(ClassMember cm : currClass.getMembers())
      {
        if(!cm.isStatic())
        {
          if(cm.hasInitialization())
            out.p(", " + cm.getName().replace("_", "__") + "_var(" + cm.getInitialization() + ")");
          else if(classes.findClass(cm.getType()) != null)
            out.p(", " + cm.getName().replace("_", "__") + "_var(__rt::null())");
          else out.p(", " + cm.getName().replace("_", "__") + "_var(0)");
        }
      }

      out.pln();

      out.indent();
      out.pln("{");
      out.indent();
      out.pln("}");
      out.pln();

      inInitializer = true;

      out.indent();
      out.pln(className + " " + inClass + "::init(" + className + " __this)");
      out.indent();
      out.pln("{");
      out.incr();

      out.indent();
      out.pln("__" + currClass.getSuperclass() + "::init(__this);");
      out.pln();

      out.indent();
      out.pln("return __this;");

      out.decr();
      out.indent();
      out.pln("}");
      out.pln();

      inInitializer = false;
    }

    className = null;
    inClass = null;
    currClass = null;
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

    out.indent();
    out.pln("int main(int argc, char* argv[])");
    out.indent();
    out.pln("{");
    out.incr();

    out.indent();
    out.pln("cout << boolalpha;");

    out.indent();
    out.pln("__rt::Ptr<__rt::Array<String> > args = new __rt::Array<String>(argc - 1);");
    out.indent();
    out.pln("for (int32_t i = 1; i < argc; i++)");
    out.indent();
    out.pln("{");

    out.incr();
    out.indent();
    out.pln("(*args)[i - 1] = __rt::literal(argv[i]);");

    out.decr();
    out.indent();
    out.pln("}");
    out.indent();
    out.pln("__" + mainClass + "::main(args);");
    out.indent();
    out.pln("return 0;");

    out.decr();
    out.indent();
    out.pln("}");

    out.flush();
  }

  /**
   * Visit a ConcreteDimensions node
   *
   * @param n The node
   */
  public void visitConcreteDimensions(GNode n)
  {
    dispatch((Node) n.get(0)); // FIXME: Only allowing one dimension for now
  }

  /**
   * Visit a ConditionalStatement node
   *
   * @param n The node
   */
  public void visitConditionalStatement(GNode n)
  {
    out.indent();
    out.p("if(");

    dispatch((Node) n.get(0)); // Print condition

    out.pln(")");

    dispatch((Node) n.get(1)); // dispatch into if statement

    if(n.get(2) != null)
    {
      out.indent();
      out.pln("else");

      dispatch((Node) n.get(2)); // dispatch into else statement
    }
  }

  /**
   * Visit a ConstructorDeclaration node
   *
   * @param n The node
   */
  public void visitConstructorDeclaration(GNode n)
  {
    // Print only the initializations of class data members -- call init() when creating new variables to do the actual constructor code
    inConstructor = true;

    localVars = new ArrayList<String>();
    localTypes = new Hashtable<String, ArrayList<String>>();

    out.indent();

    out.p(inClass + "::" + inClass + "(");

    if(((Node) n.get(3)).size() == 0)
      hasDefaultConstructor = true;

    dispatch((Node) n.get(3)); // Print the parameters

    out.p(") : __vptr(&__vtable)");

    for(ClassMember cm : currClass.getMembers())
    {
      if(!cm.isStatic())
      {
        if(cm.hasInitialization())
          out.p(", " + cm.getName().replace("_", "__") + "_var(" + cm.getInitialization() + ")");
        else if(classes.findClass(cm.getType()) != null)
          out.p(", " + cm.getName().replace("_", "__") + "_var(__rt::null())");
        else out.p(", " + cm.getName().replace("_", "__") + "_var(0)");
      }
    }

    out.indent();
    out.pln("{");

    // Nothing here

    out.indent();
    out.pln("}");

    out.pln();

    localVars = null;
    localTypes = null;

    inConstructor = false;

    // Print corresponding init() for super() constructor calls
    inInitializer = true;

    localVars = new ArrayList<String>();
    localTypes = new Hashtable<String, ArrayList<String>>();

    out.pln();
    out.indent();
    out.p(className + " " + inClass + "::init(" + className + " __this");

    Node args = (Node) n.get(3);

    if(args.size() > 0) // Print comma only if there are other parameters
      out.p(", ");

    dispatch((Node) n.get(3)); // Print the arguments

    out.pln(")");

    dispatch((Node) n.get(5)); // Print the contents of the constructor again in the initializer

    localVars = null;
    localTypes = null;

    inInitializer = false;
  }

  /**
   * Visit a Declarator node
   *
   * @param n The node
   */
  public void visitDeclarator(GNode n)
  {
    out.p(n.getString(0).replace("_", "__") + "_var"); // Finish printing declaration

    if(localVars != null)
    {
      localVars.add(n.getString(0));

      if(localTypes.get(n.getString(0)) == null)
        localTypes.put(n.getString(0), new ArrayList<String>());

      localTypes.get(n.getString(0)).add(lastType);
      //TODO: Add Array support
    }

    // If it exists, print the initialization
    if(n.get(2) != null && n.get(2) instanceof Node)
    {
      if(lastType != null && !((Node) n.get(2)).hasName("NewClassExpression") && !((Node) n.get(2)).hasName("NewArrayExpression") && classes.findClass(lastType) != null)
      {
        out.pln(";");

        if(classes.findClass(lastType) != null)
        {
          out.indent();
          out.p("if(");
          dispatch((Node) n.get(2));
          out.p(" == __rt::null() || __" + lastType + "::__class()->__vptr->isInstance(__" + lastType + "::__class(), ");
          dispatch((Node) n.get(2));
          out.pln("))");

          out.indent();
          out.pln("{");
          out.incr();
        }

        out.indent();
        out.p(n.getString(0).replace("_", "__") + "_var");
        out.p(" = ");

        dispatch((Node) n.get(2));

        out.pln(";");

        if(classes.findClass(lastType) != null)
        {
          out.decr();

          out.indent();
          out.pln("}");

          out.indent();
          out.p("else throw new ClassCastException()");
        }
      }
      else
      {
        out.p(" = ");

        dispatch((Node) n.get(2));
      }
    }
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
   * Visit a DefaultClause node
   *
   * @param n The node
   */
  public void visitDefaultClause(GNode n)
  {
    out.indent();
    out.pln("default:");
    out.incr();

    for(int i = 1; i < n.size(); i++)
    {
      dispatch((Node) n.get(i)); // Dispatch into case code
    }

    out.decr();
  }

  /**
   * Visit a Dimensions node
   *
   * @param n The node
   */
  public void visitDimensions(GNode n)
  {
    for(Object c : n)
    {
      if(c != null && c instanceof String)
        dimensions++;
    }
  }

  /**
   * Visit a DoWhileStatement node
   *
   * @param n The node
   */
  public void visitDoWhileStatement(GNode n)
  {
    out.indent();
    out.pln("do");

    dispatch((Node) n.get(0)); // Print the body of the do-while statement

    out.indent();
    out.p("while(");

    dispatch((Node) n.get(1)); // Print the condition of the do-while statement

    out.pln(");");
  }

  /**
   * Visit an EqualityExpression node
   *
   * @param n The node
   */
  public void visitEqualityExpression(GNode n)
  {
    dispatch((Node) n.get(0)); // Print first value

    out.p(" " + n.getString(1) + " ");

    dispatch((Node) n.get(2)); // Print second value
  }

  /**
   * Visit an Expression node
   *
   * @param n The node
   */
  public void visitExpression(GNode n)
  {
    dispatch((Node) n.get(0)); // Print first value

    out.p(" " + n.getString(1) + " ");

    dispatch((Node) n.get(2)); // Print second value
  }

  /**
   * Visit an ExpressionList node
   *
   * @param n The node
   */
  public void visitExpressionList(GNode n)
  {
    Node nextExp = null;

    for(Object c : n)
    {
      if(c != null && c instanceof Node)
      {
        if(nextExp != null)
        {
          dispatch(nextExp);

          // Insert commas between expressions
          out.p(", ");
        }

        nextExp = (Node) c;
      }
    }

    if(nextExp != null)
    {
      dispatch(nextExp);
    }
  }

  /**
   * Visit an ExpressionStatement node
   *
   * @param n The node
   */
  public void visitExpressionStatement(GNode n)
  {
    printArrayChecks(n);

    // Check for invalid casts
    ArrayList<Node> casts = retrieve(n, "CastExpression");

    if(casts.size() > 0)
    {
      int i;

      out.indent();
      out.p("if(");

      for(i = 0; i < casts.size(); i++)
      {
        Node m = casts.get(i);
        Node type = (Node) m.get(0);
        Node identifier = (Node) type.get(0);

        out.p("(");
        dispatch((Node) m.get(1));
        out.p(" == __rt::null() || __" + identifier.getString(0) + "::__class()->__vptr->isInstance(__" + identifier.getString(0) + "::__class(), ");
        dispatch((Node) m.get(1));
        out.p("))");

        if(i < casts.size() - 1)
          out.p(" && ");
        else out.pln(")");
      }

      out.indent();
      out.pln("{");

      out.incr();
    }

    ArrayList<Node> calls = retrieve(n, "CallExpression");

    if(calls.size() > 0)
    {
      int i;

      for(i = 0; i < calls.size() - 1; i++)
      {
        Node m = calls.get(i);

        if(m.size() != 2 || !m.getString(1).equals("out") || m.get(0) == null || !(m.get(0) instanceof Node) || !((Node) m.get(0)).hasName("PrimaryIdentifier") || !((Node) m.get(0)).getString(0).equals("System"))
        {
          if(m.get(0) != null) // If not calling a method within this class
          {
            out.indent();
            out.p("__rt::checkNotNull(");
            dispatch((Node) m.get(0));
            out.pln(");");

            StringWriter w = new StringWriter();
            out.flush();
            Printer temp = out;
            out = new Printer(w);
            dispatch((Node) m.get(0));
            out.flush();
            out = temp;

            String calledVariable = w.toString();

            if(localVars.contains(calledVariable))
            {
              ClassNode calledClass = classes.findClass(localTypes.get(calledVariable).get(localTypes.get(calledVariable).size() - 1));
              ClassMethod calledMethod = null;

              int j;

              for(j = 0; j < calledClass.getMethods().size() && calledMethod == null; i++)
              {
                if(m.getString(2).equals(calledClass.getMethods().get(i).getName()))
                  calledMethod = calledClass.getMethods().get(i);
              }

              out.indent();
              out.p(calledMethod.getReturnType().getType() + " tmp_" + tempCount + " = ");
              dispatch(m);
              out.pln(";");

              m.set(0, "tmp_" + tempCount);
              m.set(3, null);

              localVars.add("tmp_" + tempCount);

              if(localTypes.get("tmp_" + tempCount) == null)
                localTypes.put("tmp_" + tempCount, new ArrayList<String>());

              localTypes.get("tmp_" + tempCount).add(calledMethod.getReturnType().getType());

              tempCount++;
            }
          }
          else // Calling method within class scope
          {
            ClassMethod calledMethod = null;

            int j;

            for(j = 0; j < currClass.getMethods().size() && calledMethod == null; i++)
            {
              if(m.getString(2).equals(currClass.getMethods().get(i).getName()))
                calledMethod = currClass.getMethods().get(i);
            }

            out.indent();
            out.p(calledMethod.getReturnType().getType() + " tmp_" + tempCount + " = ");
            dispatch(m);
            out.pln(";");

            m.set(0, "tmp_" + tempCount);
            m.set(3, null);

            localVars.add("tmp_" + tempCount);

            if(localTypes.get("tmp_" + tempCount) == null)
              localTypes.put("tmp_" + tempCount, new ArrayList<String>());

            localTypes.get("tmp_" + tempCount).add(calledMethod.getReturnType().getType());

            tempCount++;
          }
        }
      }
    }

    out.indent();

    dispatch((Node) n.get(0)); // Print the expression

    out.pln(";");

    if(casts.size() > 0)
    {
      out.decr();

      out.indent();
      out.pln("}");

      out.indent();
      out.pln("else throw new ClassCastException();");
    }
  }

  /**
   * Visit a FieldDeclaration node
   *
   * @param n The node
   */
  public void visitFieldDeclaration(GNode n)
  {
    if(inClass == null || inMethod)
    {
      out.indent();
      // dispatch((Node) n.get(0)); // Check modifiers

      dispatch((Node) n.get(1)); // Print data type

      out.p(" ");

      dispatch((Node) n.get(2)); // Print variable names

      out.pln(";");
    }
  }

  /**
   * Visit a FloatingPointLiteral node
   *
   * @param n The node
   */
  public void visitFloatingPointLiteral(GNode n)
  {
    out.p(n.getString(0));
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
    out.p(n.getString(3).replace("_", "__") + "_var"); // Print parameter name

    if(localVars != null)
    {
      localVars.add(n.getString(3));

      if(localTypes.get(n.getString(3)) == null)
        localTypes.put(n.getString(3), new ArrayList<String>());

      localTypes.get(n.getString(3)).add(lastType);
      //TODO: add Array support
    }
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
   * Visit a ForStatement node
   *
   * @param n The node
   */
  public void visitForStatement(GNode n)
  {
    out.indent();
    out.p("for(");

    dispatch((Node) n.get(0)); // Print internal of for loop

    out.pln(")");

    dispatch((Node) n.get(1)); // Dispatch the loop
  }

  /**
   * Visit an InstanceOfExpression node
   *
   * @param n The node
   */
  public void visitInstanceOfExpression(GNode n)
  {
    out.p("__");
    dispatch((Node) n.get(1)); // Print the class name
    out.p("::__class()->__vptr->isInstance(__");
    dispatch((Node) n.get(1)); // Print the class Name
    out.p("::__class(), ");
    dispatch((Node) n.get(0)); // Print the variable to be checked
    out.p(")");
  }

  /**
   * Visit an IntegerLiteral node
   *
   * @param n The node
   */
  public void visitIntegerLiteral(GNode n)
  {
    out.p(n.getString(0));
  }

  /**
   * Visit a MethodDeclaration node
   *
   * @param n The node
   */
  public void visitMethodDeclaration(GNode n)
  {
    inMethod = true;
    localVars = new ArrayList<String>();
    localTypes = new Hashtable<String, ArrayList<String>>();

    localVars.add(null);

    out.indent();

    dispatch((Node) n.get(0)); // Check modifiers

    if(isStatic)
      staticMethod = true;

    dispatch((Node) n.get(2)); // Print return type

    out.p(" " + inClass + "::" + n.getString(3) + "(");

    if(!staticMethod)
    {
      out.p(className + " __this");

      if(((Node) n.get(4)).size() > 0)
        out.p(", ");
    }

    dispatch((Node) n.get(4)); // Print the parameters

    out.pln(")");

    dispatch((Node) n.get(7)); // Dispatch into method

    localVars = null;
    localTypes = null;

    staticMethod = false;

    inMethod = false;
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
   * Visit a MultiplicativeExpression node
   *
   * @param n The node
   */
  public void visitMultiplicativeExpression(GNode n)
  {
    dispatch((Node) n.get(0)); // Print first value

    out.p(" " + n.getString(1) + " ");

    dispatch((Node) n.get(2)); // Print second value
  }

  /**
   * Visit a NewArrayExpression node
   *
   * @param n The node
   */
  public void visitNewArrayExpression(GNode n)
  { // FIXME:  Make multi-dimensional arrays work
    // Print initializer call
    out.p("__rt::Array<");
    dispatch((Node) n.get(0)); // Print array type
    out.p(">::init(");

    // Print initialization statement
    out.p("new __rt::Array<");
    dispatch((Node) n.get(0)); // Print array type
    out.p(">(");
    dispatch((Node) n.get(1)); // Print array size
    out.p("), ");

    dispatch((Node) n.get(1)); // Print array size again as a parameter for init()
    out.p(")");
  }

  /**
   * Visit a NewClassExpression node
   *
   * @param n The node
   */
  public void visitNewClassExpression(GNode n)
  {
    // Print initialization method
    out.p("__");
    dispatch((Node) n.get(2)); // Print class name
    out.p("::init(");

    // Print object initialization
    out.p("new __"); // TODO: What about arrays?

    dispatch((Node) n.get(2)); // Print class name

    out.p("()"); // Use only the default constructor to create objects (The initializations will all be done in the init() call

    if(((Node) n.get(3)).size() > 0)
      out.p(", "); // Print a comma only if there are more arguments

    dispatch((Node) n.get(3)); // Print arguments again

    out.p(")");
  }

  /**
   * Visit a PostfixExpression node
   *
   * @param n The node
   */
  public void visitPostfixExpression(GNode n)
  {
    dispatch((Node) n.get(0)); // Print variable

    out.p(n.getString(1)); // Print postfix operator
  }

  /**
   * Visit a PrimaryIdentifier node
   *
   * @param n The node
   */
  public void visitPrimaryIdentifier(GNode n)
  {
    if(printCode == 1 && n.getString(0).equals("System"))
      printCode = 2;
    else if(classes.findClass(n.getString(0)) != null)
    {
      out.p("__" + n.getString(0));
      genericClassSelector = true;
    }
    else if(inClass != null && (inMethod || inInitializer) && !localVars.contains(n.getString(0)))
    {
      if(staticMethod)
        out.p(inClass + "::" + n.getString(0).replace("_", "__") + "_var");
      else out.p("__this->" + n.getString(0).replace("_", "__") + "_var");
    }
    else out.p(n.getString(0).replace("_", "__") + "_var");
  }

  /**
   * Visit a PrimitiveType node
   *
   * @param n The node
   */
  public void visitPrimitiveType(GNode n)
  {
    if(n.getString(0).equals("byte"))
    {
      lastType = "signed char";
      out.p("signed char");
    }
    else if(n.getString(0).equals("boolean"))
    {
      lastType = "bool";
      out.p("bool");
    }
    else
    {
      lastType = n.getString(0);
      out.p(n.getString(0));
    }
  }

  /**
   * Visit a QualifiedIdentifier node
   *
   * @param n The node
   */
  public void visitQualifiedIdentifier(GNode n)
  {
    lastType = n.getString(0);

    out.p(n.getString(0));
  }

  /**
   * Visit a RelationalExpression node
   *
   * @param n The node
   */
  public void visitRelationalExpression(GNode n)
  {
    dispatch((Node) n.get(0)); // Print first term

    out.p(" " + n.getString(1) + " ");

    dispatch((Node) n.get(2)); // Print second term
  }

  /**
   * Visit a ReturnStatement node
   *
   * @param n The node
   */
  public void visitReturnStatement(GNode n)
  {
    out.indent();
    out.p("return ");

    dispatch((Node) n.get(0)); // Print value

    out.pln(";");
  }

  /**
   * Visit a SelectionExpression node
   *
   * @param n The node
   */
  public void visitSelectionExpression(GNode n)
  {
    if(n.getString(1).equals("out"))
      printCode = 1;

    genericClassSelector = false;

    dispatch((Node) n.get(0));

    if(printCode < 2)
    {
      if(genericClassSelector)
        out.p("::");
      else out.p("->");

      out.p(n.getString(1).replace("_", "__") + "_var");
    }

    genericClassSelector = false;
  }

  /**
   * Visit a StringLiteral node
   *
   * @param n The node
   */
  public void visitStringLiteral(GNode n)
  {
    out.p("__rt::literal(" + n.getString(0) + ")");
  }

  /**
   * Visit a SubscriptExpression node
   *
   * @param n The node
   */
  public void visitSubscriptExpression(GNode n)
  { // Assumes subscript is for an array access
    // Need to do array notNull and index within bounds checks before entering here

    dispatch((Node) n.get(0)); // Print variable name

    out.p("->__data[");

    dispatch((Node) n.get(1)); // Print index

    out.p("]");
  }

  /**
   * Visit a SuperExpression node
   *
   * @param n The node
   */
  public void visitSuperExpression(GNode n)
  {
    out.p("__" + currClass.getSuperclass());
  }

  /**
   * Visit a SwitchStatement node
   *
   * @param n The node
   */
  public void visitSwitchStatement(GNode n)
  {
    out.indent();
    out.p("switch(");

    dispatch((Node) n.get(0)); // Print the value of the switch

    out.pln(")");

    out.indent();
    out.pln("{");
    out.incr();

    for(int i = 1; i < n.size(); i++)
    {
      if(n.get(i) != null && n.get(i) instanceof Node)
        dispatch((Node) n.get(i)); // Print case statements

    }

    out.decr();
    out.indent();
    out.pln("}");
  }

  /**
   * Visit a ThisExpression node
   *
   * @param n The node
   */
  public void visitThisExpression(GNode n)
  {
    if(inConstructor)
      out.p("this");
    else out.p("__this");
  }

  /**
   * Visit a TryCatchFinallyStatement node
   *
   * @param n The node
   */
  public void visitTryCatchFinallyStatement(GNode n)
  {
    out.indent();
    out.pln("try");

    dispatch((Node) n.get(1)); // Dispatch into try statement

    if(n.get(2) != null) // Dispatch into catch statement if it exists
      dispatch((Node) n.get(2));

    if(n.get(3) != null) // Dispatch into finally statement if it exists
    {
      out.indent();
      out.pln("finally");

      dispatch((Node) n.get(3));
    }
  }

  /**
   * Visit a Type node
   *
   * @param n The node
   */
  public void visitType(GNode n)
  {
    lastType = null; // Reset variable

    int i;

    if(n.get(1) != null && n.get(1) instanceof Node) // Check if this is an array
    {
      dimensions = 0;

      dispatch((Node) n.get(1)); // If it is, find the number of dimensions
    }

    if(dimensions > 0)
    {
      for(i = 0; i < dimensions; i++)
      {
        out.p("__rt::Ptr<__rt::Array<");
      }
    }

    dispatch((Node) n.get(0));

    if(dimensions > 0)
    {
      for(i = 0; i < dimensions; i++)
      {
        out.p(">, __rt::array_policy>");

        if(i < dimensions - 1)
          out.p(" ");
      }

      dimensions = 0;
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

  /**
   * Visit a WhileStatement node
   *
   * @param n The node
   */
  public void visitWhileStatement(GNode n)
  {
     out.indent();
     out.p("while(");

     dispatch((Node) n.get(0)); // Print condition

     out.pln(")");

     dispatch((Node) n.get(1)); // Dispatch into loop
  }

  /**
   * Print the necessary array exception checks within the given node
   *
   * @param n The node at which to start the search
   */
  protected void printArrayChecks(GNode n)
  {
    new Visitor()
    {
      public void visit(Node n)
      {
        for(Object c : n)
        {
          if(c != null && c instanceof Node)
            dispatch((Node) c);
        }
      }

      public void visitSubscriptExpression(GNode n)
      {
        out.indent();
        out.p("__rt::checkNotNull(");
        out.p(((Node) n.get(0)).getString(0).replace("_", "__") + "_var");
        out.pln(");");

        out.indent();
        out.p("__rt::checkIndex(");
        out.p(((Node) n.get(0)).getString(0).replace("_", "__") + "_var, " + ((Node) n.get(1)).getString(0).replace("_", "__") + "_var");
        out.pln(");");
      }
    }.dispatch(n);
  }

  /**
   * Returns all nodes with a given name
   *
   * @param n        The node at which to start the search
   * @param nodeName The name of the nodes to search for
   * @return         A list of nodes within the given node that have the given name
   */
  protected ArrayList<Node> retrieve(Node m, final String nodeName)
  {
    final ArrayList<Node> list = new ArrayList<Node>();

    // Retrieves subnodes with name nodeName, using depth-first search
    new Visitor()
    {
      public void visit(Node n)
      {
        for(Object c : n)
        {
          if(c != null && c instanceof Node)
            dispatch((Node) c);
        }

        if(n.hasName(nodeName))
          list.add(n);
      }
    }.dispatch(m);

    return list;
  }
}
