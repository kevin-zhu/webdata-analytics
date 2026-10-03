package com.kzhu.realestate.source;

import java.util.ArrayList;
import java.util.List;

/** Builds SoQL $where clauses from a {@link DataQuery}. */
final class SocrataQueries {
  private SocrataQueries() {}

  static String where(FieldMapping map, DataQuery q) {
    List<String> parts = new ArrayList<>();
    if (q.state != null && map.stateColumn() != null) {
      parts.add(map.stateColumn() + " = '" + escape(q.state) + "'");
    }
    if (q.sinceDate != null && map.dateColumn() != null) {
      parts.add(map.dateColumn() + " >= '" + escape(q.sinceDate) + "'");
    }
    return parts.isEmpty() ? null : String.join(" AND ", parts);
  }

  private static String escape(String s) {
    return s.replace("'", "''");
  }
}
