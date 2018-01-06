package edu.columbia.rdf.matcalc.toolbox.conversion;

public class Conversion implements Comparable<Conversion> {

  private String mId;
  private String mType;
  private Conversion mParent;

  public Conversion(String id, String type) {
    this(id, null, type);
  }

  public Conversion(String id, Conversion parent, String type) {
    mId = id;
    mParent = parent;
    mType = type;
  }

  public Conversion(Conversion c, String type) {
    this(c.getId(), c, type);
  }

  @Override
  public int compareTo(Conversion c) {
    return mId.compareTo(c.mId);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Conversion) {
      return compareTo((Conversion) o) == 0;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return mId.hashCode();
  }

  public Conversion getParent() {
    return mParent;
  }

  public String getId() {
    return mId;
  }

  public String getType() {
    return mType;
  }
}
