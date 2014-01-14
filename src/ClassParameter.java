/**
 * A representation of a type
 */
public class ClassParameter
{
  private String type; // The data type
  private int dimensions; // The number of dimensions of the data type (0 just means the basic type)

  /**
   * Create a new type
   *
   * @param t The data type
   */
  public ClassParameter(String t)
  {
    type = t;
    dimensions = 0;
  }

  /**
   * Create a new array type
   *
   * @param t The data type
   * @param d The number of dimensions
   */
  public ClassParameter(String t, int d)
  {
    type = t;
    dimensions = d;
  }

  /**
   * Gets the data type
   *
   * @return The data type of the type
   */
  public String getType()
  {
    return type;
  }

  /**
   * Gets the dimensions
   *
   * @return The number of dimensions of the type
   */
  public int getDimensions()
  {
    return dimensions;
  }
}
