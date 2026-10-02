package com.kzhu.realestate.source;

/** Filter passed to a data source. Every field is optional; sources ignore what they cannot apply. */
public class DataQuery {
  public String state;
  public String county;
  public String keyword;
  public String sinceDate; // yyyy-MM-dd
  public int limit = 1000;

  public static DataQuery of(String state, String keyword, String sinceDate, int limit) {
    DataQuery q = new DataQuery();
    q.state = state;
    q.keyword = keyword;
    q.sinceDate = sinceDate;
    if (limit > 0) {
      q.limit = limit;
    }
    return q;
  }
}
