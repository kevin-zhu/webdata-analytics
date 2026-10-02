package com.kzhu.realestate.source;

import java.io.IOException;
import java.util.List;

/** Base interface for anything that pulls a list of real estate related records. */
public interface RealEstateDataSource<T> {
  /** Stable name, used as the output file name and the {@code source} field of records. */
  String name();

  List<T> fetch(DataQuery query) throws IOException;
}
