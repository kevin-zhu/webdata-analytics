package com.kzhu.realestate.source;

import java.util.Map;
import java.util.Properties;

/**
 * Maps source column names to the fields of our model. Configured with properties of the form
 * {@code <prefix>.field.<modelField>=<sourceColumn>}; a missing mapping falls back to a column with
 * the same name as the model field.
 */
public class FieldMapping {
  private final Properties props;
  private final String prefix;

  public FieldMapping(Properties props, String prefix) {
    this.props = props;
    this.prefix = prefix;
  }

  public String get(Map<String, String> row, String modelField) {
    String column = props.getProperty(prefix + ".field." + modelField, modelField);
    return row.get(column);
  }

  /** Column used for date filtering (SoQL $where), or null if not configured. */
  public String dateColumn() {
    return props.getProperty(prefix + ".dateColumn");
  }

  /** Optional fixed full-text filter (SoQL $q) for datasets that mix professions/case types. */
  public String fixedQuery() {
    return props.getProperty(prefix + ".q");
  }

  public String stateColumn() {
    return props.getProperty(prefix + ".stateColumn");
  }
}
