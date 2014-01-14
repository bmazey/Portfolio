/**
 * A representation of a data member
 */
public class ClassMember
{
  private String type; // The data type of the data member
  private String name; // The name of the data member
  private String initialization; // The C++ code for the initialization of this data member, if there is any
  private boolean isStatic; // If this data member is static
  private boolean isPrimitive = false; // If this data member is primite -- Assume that the data member is not primitive, unless told otherwise.

  /**
   * Create a new data member representation
   *
   * @param t The data type
   * @param n The name
   * @param s Whether or not this member is static
   * @param p Whether or not this member is primitive
   */
  public ClassMember(String t, String n, boolean s, boolean p)
  {
    type = t;
    name = n;
    initialization = null;
    isStatic = s;
    isPrimitive = p;
  }

  /**
   * Create a new data member representation
   *
   * @param t The data type
   * @param n The name
   * @param i The C++ code for the initialization of this member
   * @param s Whether or not this member is static
   * @param p Whether or not this member is primitive
   */
  public ClassMember(String t, String n, String i, boolean s, boolean p)
  {
    type = t;
    name = n;
    initialization = i;
    isStatic = s;
    isPrimitive = p;
  }

  /**
   * Get the data type of the data member
   *
   * @return The data type
   */
  public String getType()
  {
    return type;
  }

  /**
   * Get the data type of the data member
   *
   * @return The data type
   */
  public String getName()
  {
    return name;
  }

  /**
   * Get the C++ initialization code of the data member
   *
   * @return The C++ initialization code
   */
  public String getInitialization()
  {
    return initialization;
  }

  /**
   * Whether or not the data member has initialization code
   *
   * @return If the data member has initialization code, returns true; otherwise, returns false
   */
  public boolean hasInitialization()
  {
    return initialization != null;
  }

  /**
   * Whether or not the data member is static
   *
   * @return If the data member is static, returns true; otherwise, returns false
   */
  public boolean isStatic()
  {
    return isStatic;
  }

  /**
   * Whether or not the data member is primitive
   *
   * @return If the data member is primitive, returns true; otherwise, returns false
   */
  public boolean isPrimitive()
  {
    return isPrimitive;
  }
}
