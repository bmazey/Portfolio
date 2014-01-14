import java.util.ArrayList;

/**
 * A representation of a method
 */
public class ClassMethod implements Comparable<ClassMethod>
{
  private String name; // The name of the method
  private ClassParameter returnType; // The return type of the method
  private ArrayList<ClassParameter> parameters; // The parameters of the method

  private String classDefined; // Which class defined this version of the method

  private boolean isStatic; // Whether or not the method is static

  /**
   * Create a new representation of a method
   *
   * @param n   The name
   * @param r   The return type representation as a ClassParameter
   * @param p   An ArrayList of ClassParameters, representing the parameters of this method
   * @param def The name of the class that defined this method
   * @param s   Whether or not this method is static
   */
  public ClassMethod(String n, ClassParameter r, ArrayList<ClassParameter> p, String def, boolean s)
  {
    name = n;
    returnType = r;
    parameters = p;
    classDefined = def;
    isStatic = s;
  }

  /**
   * Gets the name of the method
   *
   * @return The name of the method
   */
  public String getName()
  {
    return name;
  }

  /**
   * Gets the return type of the method
   *
   * @return A ClassParameter, representing the return type of the method
   */
  public ClassParameter getReturnType()
  {
    return returnType;
  }

  /**
   * Gets the parameters of the method
   *
   * @return An ArrayList of ClassParameters, representing the parameters of the method
   */
  public ArrayList<ClassParameter> getParameters()
  {
    return parameters;
  }

  /**
   * Gets the defining class of the method
   *
   * @return The name defining class of the method
   */
  public String getDefiningClass()
  {
    return classDefined;
  }

  /**
   * Gets whether or not the method is static
   *
   * @param s Whether or not the method is static
   */
  public void setStatic(boolean s)
  {
    isStatic = s;
  }

  /**
   * Gets whether or not the method is static
   *
   * @return If the method is static, returns true; otherwise, returns false
   */
  public boolean isStatic()
  {
    return isStatic;
  }

  /**
   * Calculates how this method compares to another method, mainly to see if the two methods are equal
   *
   * @param other Another method
   * @return      If the methods have different names or different parameters, returns a non-zero integer; otherwise returns zero
   */
  public int compareTo(ClassMethod other)
  {
    if(name.compareTo(other.getName()) != 0)
      return name.compareTo(other.getName());

    if(parameters.size() != other.getParameters().size())
      return parameters.size() - other.getParameters().size();
/* Add this back when method overloading is allowed
    int i;

    for(i = 0; i < parameters.size(); i++)
    {
      if(parameters.get(i).compareTo(other.getParameters().get(i)) != 0)
        return parameters.get(i).compareTo(other.getParameters().get(i));
    }
*/
    return 0;
  }

  /**
   * Checks to see if two methods are equal
   *
   * @param other Another method
   * @return      If the value of compareTo(other) is zero, returns true; otherwise, returns false
   */
  public boolean equals(ClassMethod other)
  {
    return this.compareTo(other) == 0;
  }
}
