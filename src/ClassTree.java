import java.util.ArrayList;

import xtc.tree.Printer;

/**
 * A class hierarchy composed of ClassNodes
 */
public class ClassTree
{
  private ClassNode head;

  /**
   * Create a new ClassTree containing only java.lang.Object, java.lang.String, and java.lang.Class
   */
  public ClassTree()
  {
    ArrayList<ClassParameter> params;

    // Create java.lang.Object node
    ClassNode o = new ClassNode("Object");
    o.setPrint(false);

    params = new ArrayList<ClassParameter>();
    o.addMethod(new ClassMethod("hashCode", new ClassParameter("int32_t"), params, "Object", false));

    params = new ArrayList<ClassParameter>();
    params.add(new ClassParameter("Object", 0));
    o.addMethod(new ClassMethod("equals", new ClassParameter("bool"), params, "Object", false));

    params = new ArrayList<ClassParameter>();
    o.addMethod(new ClassMethod("getClass", new ClassParameter("Class"), params, "Object", false));

    params = new ArrayList<ClassParameter>();
    o.addMethod(new ClassMethod("toString", new ClassParameter("String"), params, "Object", false));

    // Create java.lang.String node
    ClassNode s = new ClassNode("String", o);
    o.addChild(s);
    s.setPrint(false);

    params = new ArrayList<ClassParameter>();
    s.addMethod(new ClassMethod("hashCode", new ClassParameter("int32_t"), params, "String", false));

    params = new ArrayList<ClassParameter>();
    params.add(new ClassParameter("Object"));
    s.addMethod(new ClassMethod("equals", new ClassParameter("bool"), params, "String", false));

    params = new ArrayList<ClassParameter>();
    s.addMethod(new ClassMethod("toString", new ClassParameter("String"), params, "String", false));

    params = new ArrayList<ClassParameter>();
    s.addMethod(new ClassMethod("length", new ClassParameter("int32_t"), params, "String", false));

    params = new ArrayList<ClassParameter>();
    params.add(new ClassParameter("int32_t"));
    s.addMethod(new ClassMethod("charAt", new ClassParameter("char"), params, "String", false));

    // Create java.lang.Class node
    ClassNode c = new ClassNode("Class", o);
    o.addChild(c);
    c.setPrint(false);

    params = new ArrayList<ClassParameter>();
    c.addMethod(new ClassMethod("toString", new ClassParameter("String"), params, "Class", false));

    params = new ArrayList<ClassParameter>();
    c.addMethod(new ClassMethod("getName", new ClassParameter("String"), params, "Class", false));

    params = new ArrayList<ClassParameter>();
    c.addMethod(new ClassMethod("getSuperclass", new ClassParameter("Class"), params, "Class", false));

    params = new ArrayList<ClassParameter>();
    c.addMethod(new ClassMethod("isPrimitive", new ClassParameter("bool"), params, "Class", false));

    params = new ArrayList<ClassParameter>();
    c.addMethod(new ClassMethod("isArray", new ClassParameter("bool"), params, "Class", false));

    params = new ArrayList<ClassParameter>();
    c.addMethod(new ClassMethod("getComponentType", new ClassParameter("Class"), params, "Class", false));

    params = new ArrayList<ClassParameter>();
    params.add(new ClassParameter("Object"));
    c.addMethod(new ClassMethod("isInstance", new ClassParameter("bool"), params, "Class", false));

    head = o;
  }

  /**
   * Create a ClassNode that extends java.lang.Object
   *
   * @param className The name of the new ClassNode
   * @return          The newly created ClassNode
   */
  public ClassNode add(String className)
  {
    ClassNode c = new ClassNode(className, head);
    head.addChild(c);

    return c;
  }

  /**
   * Create a ClassNode that extends a specific class
   *
   * @param className The name of the new ClassNode
   * @param extension The name of the class that the ClassNode extends
   * @return          If the extension class was found, returns the newly created ClassNode; otherwise, returns null
   */
  public ClassNode add(String className, String extension)
  {
    ClassNode inherits = findClass(extension);

    if(inherits == null)
      return null;

    ClassNode c = new ClassNode(className, inherits);
    inherits.addChild(c);

    return c;
  }
  /**
   * Find a ClassNode within the class hierarchy
   *
   * @param n The name of the class
   * @return  If the corresponding ClassNode was found, returns the ClassNode; otherwise, returns null
   */
  public ClassNode findClass(String n)
  {
    return head.getClass(n);
  }

  /**
   * Prints the data layouts and vtables of all of the ClassNodes in the class hierachy
   *
   * @param out A printer
   */
  public void print(Printer out)
  {
    head.print(out);
  }
}
