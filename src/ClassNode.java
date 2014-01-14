import java.util.ArrayList;

import xtc.tree.Printer;
import xtc.tree.Node;

/**
 * A representation of a Java class
 *
 * * Note that when printing:
 *    -variables and methods escape '_' with '__' (ignoring __isa and __delete())
 *    -variables end with '_var'
 *    -methods end with '_' and then a list of parameter types separated by '_'
 */
public class ClassNode
{
  private String name; // Name of the class
  private ArrayList<ClassMethod> methods; // A list of methods within the class
  private ArrayList<ClassMember> members; // A list of data members within the class
  private ArrayList<ArrayList<ClassParameter>> constructors; // A list of lists of constructor argument types

  private ArrayList<ClassNode> children; // A list of subclasses of this class

  private String superclass; // The name of the superclass of this class
  private ClassNode superclassNode = null; // The classNode of the superclass of this class

  private boolean toPrint = true; // Whether or not this classNode needs to be printed

  /**
   * Create a new class representation of a class that does not extend any other class
   *
   * @param n The name of the class
   */
  public ClassNode(String n)
  {
    name = n;

    methods = new ArrayList<ClassMethod>();

    members = new ArrayList<ClassMember>();

    constructors = new ArrayList<ArrayList<ClassParameter>>();
    constructors.add(new ArrayList<ClassParameter>()); // Class must have default constructor

    children = new ArrayList<ClassNode>();
  }

  /**
   * Create a new class representation of a class that extends some other class
   *
   * @param n         The name of the class
   * @param inherited The ClassNode of the class that this class extends
   */
  public ClassNode(String n, ClassNode inherited)
  {
    name = n;

    // Inherit methods and data members
    methods = new ArrayList<ClassMethod>();
    methods.addAll(inherited.getMethods());

    members = new ArrayList<ClassMember>();
    members.addAll(inherited.getMembers());

    constructors = new ArrayList<ArrayList<ClassParameter>>();
    constructors.add(new ArrayList<ClassParameter>()); // Class must have default constructor

    children = new ArrayList<ClassNode>();

    superclass = inherited.getName();
    superclassNode = inherited;
  }

  /**
   * Get the name of the class
   *
   * @return The name of the class
   */
  public String getName()
  {
    return name;
  }

  /**
   * Add a method to the class
   *
   * @param m A ClassMethod representation of a method
   */
  public void addMethod(ClassMethod m) // FIXME: Allow overloading (will need to change compareTo for ClassMethod)
  {
    int index = -1;

    int i;

    for(i = 0; i < methods.size() && index < 0; i++)
    {
      if(m.equals(methods.get(i)))
        index = i;
    }

    if(index < 0)
      methods.add(m);
    else methods.set(index, m);
  }

  /**
   * Get all of the methods of the class
   *
   * @return An ArrayList of ClassMethods that represent the methods of the class
   */
  public ArrayList<ClassMethod> getMethods()
  {
    return methods;
  }

  /**
   * Add a data member to the class
   *
   * @param m A ClassMember representation of a data member
   */
  public void addMember(ClassMember m)
  {
    members.add(m);
  }

  /**
   * Get all of the data members of the class
   *
   * @return An ArrayList of ClassMembers that represent the data members of the class
   */
  public ArrayList<ClassMember> getMembers()
  {
    return members;
  }

  /**
   * Add a constructor to the class
   *
   * @param m An ArrayList of ClassParameters, which represent the parameters of the constructor
   */
  public void addConstructor(ArrayList<ClassParameter> args)
  {
    if(args.size() != 0) // Already added if args.size() == 0
      constructors.add(args);
  }

  /**
   * Add a subclass to the class
   *
   * @param child The ClassNode of a subclass
   */
  public void addChild(ClassNode child)
  {
    children.add(child);
  }

  /**
   * Gets the name of the superclass of the class
   *
   * @return The name of the superclass of the class
   */
  public String getSuperclass()
  {
    return superclass;
  }

  /**
   * Gets the ClassNode of the superclass of the class
   *
   * @return The ClassNode of the superclass of the class
   */
  public ClassNode getSuperclassNode()
  {
    return superclassNode;
  }

  /**
   * Gets all superclass of the class in the order of closest superclass to farthest superclass
   *
   * @param list An ArrayList of ClassNodes of superclasses between the beginning class and the current class
     @return     An ArrayList of ClassNodes of superclasses of the beginning class up to the current class
   */
  public ArrayList<ClassNode> getAncestors(ArrayList<ClassNode> list)
  {
    if(superclassNode != null)
    {
      list.add(superclassNode);
      return superclassNode.getAncestors(list);
    }
    else return list;
  }

  /**
   * Sets whether or not to print the class
   *
   * @param p Whether or not the class should be printed
   */
  public void setPrint(boolean p)
  {
    toPrint = p;
  }

  /**
   * Searches to see if the class or a subclass of the class has a given name
   * 
   * @param n The name of the class to search for
   * @return  If a ClassNode with the given name was found, returns the matching ClassNode; otherwise, returns null
   */
  public ClassNode getClass(String n)
  {
    if(name.equals(n))
      return this;

    int i;
    ClassNode cn = null;

    for(i = 0; i < children.size() && cn == null; i++)
    {
      cn = children.get(i).getClass(n);
    }

    return cn;
  }

  /**
   * Prints the data layout and vtable for the class, as well as for the subclasses of the class
   *
   * @param out A printer
   */
  public void print(Printer out)
  {
    if(toPrint)
    {
      int t, i, j;
      int dims;

      /*
       * Print the class
       */
      out.indent();
      out.pln("struct __" + name);
      out.indent();
      out.pln("{");

      out.incr();

      out.indent();
      out.pln("__" + name + "_VT* __vptr;");
      out.pln();

      // Print data members
      for(t = 0; t < members.size(); t++)
      {
         out.indent();
         if(members.get(t).isStatic())
           out.p("static ");
         out.pln(members.get(t).getType() + " " + members.get(t).getName().replace("_", "__") + "_var;");
      }

      out.pln();

      // Print constructors
      for(t = 0; t < constructors.size(); t++)
      {
        out.indent();

        out.p("__" + name + "(");

        for(i = 0; i < constructors.get(t).size(); i++)
        {
          dims = constructors.get(t).get(i).getDimensions();

          for(j = 0; j < dims; j++)
          {
            out.p("__rt::Ptr<__rt::Array<");
          }

          out.p(constructors.get(t).get(i).getType());

          for(j = 0; j < dims; j++)
          {
            out.p(">, __rt::array_policy>");

            if(j < dims - 1)
              out.p(" ");
          }

          if(i < constructors.get(t).size() - 1)
            out.p(", ");
        }

        out.pln(");");
      }

      out.pln();

      // Print initializers
      for(t = 0; t < constructors.size(); t++)
      {
        out.indent();

        out.p("static " + name + " init(" + name);

        if(constructors.get(t).size() > 0)
          out.p(", ");

        for(i = 0; i < constructors.get(t).size(); i++)
        {
          dims = constructors.get(t).get(i).getDimensions();

          for(j = 0; j < dims; j++)
          {
            out.p("__rt::Ptr<__rt::Array<");
          }

          out.p(constructors.get(t).get(i).getType());

          for(j = 0; j < dims; j++)
          {
            out.p(">, __rt::array_policy>");

            if(j < dims - 1)
              out.p(" ");
          }

          if(i < constructors.get(t).size() - 1)
            out.p(", ");
        }

        out.pln(");");
      }

      out.pln();

      // Print methods
      for(t = 0; t < methods.size(); t++)
      {
        ClassMethod m = methods.get(t);

        dims = m.getReturnType().getDimensions();

        out.indent();
        out.p("static ");

        for(j = 0; j < dims; j++)
        {
          out.p("__rt::Ptr<__rt::Array<");
        }

        out.p(m.getReturnType().getType() + " " + m.getName() + "(");

        for(j = 0; j < dims; j++)
        {
          out.p(">, __rt::array_policy>");

          if(j < dims - 1)
            out.p(" ");
        }

        if(!m.isStatic())
        {
          out.p(m.getDefiningClass());

          if(m.getParameters().size() > 0)
            out.p(", ");
        }

        for(i = 0; i < m.getParameters().size(); i++)
        {
          dims = m.getParameters().get(i).getDimensions();

          for(j = 0; j < dims; j++)
          {
            out.p("__rt::Ptr<__rt::Array<");
          }

          out.p(m.getParameters().get(i).getType());

          for(j = 0; j < dims; j++)
          {
            out.p(">, __rt::array_policy>");

            if(j < dims - 1)
              out.p(" ");
          }

          if(i < m.getParameters().size() - 1)
            out.p(", ");
        }

        out.pln(");");
      }

      out.pln();

      out.indent();
      out.pln("static Class __class();");
      out.indent();
      out.pln("static __" + name + "_VT __vtable;");

      out.decr();

      out.indent();
      out.pln("};");
      out.pln();


      /*
       * Print the vtable
       */
      out.indent();
      out.pln("struct __" + name + "_VT");
      out.indent();
      out.pln("{");

      out.incr();

      // Print vtable declarations
      out.indent();
      out.pln("Class __isa;");
      out.indent();
      out.pln("void (*__delete)(__" + name + "*);");

      for(t = 0; t < methods.size(); t++)
      {
        ClassMethod m = methods.get(t);

        if(!m.isStatic())
        {
          dims = m.getReturnType().getDimensions();

          out.indent();

          for(j = 0; j < dims; j++)
          {
            out.p("__rt::Ptr<__rt::Array<");
          }

          out.p(m.getReturnType().getType());

          for(j = 0; j < dims; j++)
          {
            out.p(">, __rt::array_policy>");

            if(j < dims - 1)
              out.p(" ");
          }

          out.p(" (*" + m.getName() + ")");

          out.p("(");

          if(m.getName().equals("getClass"))
            out.p(name);
          else out.p(m.getDefiningClass());

          if(m.getParameters().size() > 0)
            out.p(", ");

          for(i = 0; i < m.getParameters().size(); i++)
          {
            out.p(m.getParameters().get(i).getType());

            if(i < m.getParameters().size() - 1)
              out.p(", ");
          }

          out.pln(");");
        }
      }

      out.pln();

      // Print default constructor
      out.indent();
      out.pln("__" + name + "_VT() :");

      out.incr();

      out.indent();
      out.pln("__isa(__" + name + "::__class()),");
      out.indent();
      out.p("__delete(__rt::__delete<__" + name + ">)");

      for(t = 0; t < methods.size(); t++)
      {
        ClassMethod m = methods.get(t);

        if(!m.isStatic())
        {
          out.pln(",");
          out.indent();

          out.p(m.getName() + "(");

          if(m.getName().equals("getClass"))
            out.p("(Class(*)(" + name + "))&__" + m.getDefiningClass() + "::" + m.getName());
          else out.p("&__" + m.getDefiningClass() + "::" + m.getName());

          out.p(")");
        }
      }

      out.pln();

      out.decr();

      out.indent();
      out.pln("{");
      out.indent();
      out.pln("}");

      out.decr();
      out.indent();
      out.pln("};");
      out.pln();

      // Call the vtable's default contructor
      out.indent();
      out.pln("__" + name + "_VT __" + name + "::__vtable;");
      out.pln();

      // Print internal __class() method
      out.indent();
      out.pln("Class __" + name + "::__class()");
      out.indent();
      out.pln("{");

      out.incr();

      out.indent();
      out.pln("static Class k = new __Class(__rt::literal(\"" + name + "\"), __" + superclass + "::__class());");

      out.indent();
      out.pln("return k;");

      out.decr();

      out.indent();
      out.pln("}");

      out.pln();

      // Print static member initializations
      for(t = 0; t < members.size(); t++)
      {
         if(members.get(t).isStatic())
         {
           out.indent();
           out.p(members.get(t).getType() + " __" + name + "::" + members.get(t).getName().replace("_", "__") + "_var = ");

           if(members.get(t).hasInitialization())
             out.p(members.get(t).getInitialization());
           else
           {
             if(members.get(t).isPrimitive())
               out.p("0");
             else out.p("__rt::null()");
           }

           out.pln(";");
         }
      }

      out.pln();

      // Override the pipe operator to print this class correctly
      out.indent();
      out.pln("std::ostream& operator<<(std::ostream& out, " + name + " o)");
      out.indent();
      out.pln("{");
      out.incr();

      out.indent();
      out.pln("out << o->__vptr->toString(o);");
      out.indent();
      out.pln("return out;");

      out.decr();
      out.indent();
      out.pln("}");

      out.pln();
    }

    int k;

    // Print the subclasses
    for(k = 0; k < children.size(); k++)
    {
      children.get(k).print(out);
    }
  }
}
