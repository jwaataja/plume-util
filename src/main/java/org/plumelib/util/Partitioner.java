package org.plumelib.util;

import org.checkerframework.checker.determinism.qual.NonDet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A Partitioner accepts Objects and assigns them to an equivalence class.
 *
 * @param <ELEMENT> the type of elements to be classified
 * @param <CLASS> the type of equivalence classes (classification buckets)
 * @see MultiRandSelector
 */
public interface Partitioner<ELEMENT extends @Nullable @NonDet Object,
    CLASS extends @Nullable @NonDet Object> {

  /**
   * @param obj the Object to be assigned to a bucket
   * @return a key representing the bucket containing obj
   */
  CLASS assignToBucket(ELEMENT obj);
}
