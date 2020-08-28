package org.plumelib.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.checkerframework.checker.index.qual.LengthOf;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;
import org.checkerframework.checker.determinism.qual.*;

/**
 * Given a set of collections, yield each combination that takes one element from each collection.
 * Each tuple has a value from each candidate list. Each combination'slength is the same as the
 * number of input lists.
 *
 * <p>For instance, given {@code [["a1", "a2"], ["b1"], ["c1", "c2", "c3"]]}, this class yields in
 * turn:
 *
 * <pre>
 *   ["a1", "b1", "c1"]
 *   ["a1", "b1", "c2"]
 *   ["a1", "b1", "c3"]
 *   ["a2", "b1", "c1"]
 *   ["a2", "b1", "c2"]
 *   ["a2", "b1", "c3"]
 * </pre>
 */
public class CombinationIterator<T> implements Iterator<List<T>> {

  /** Lists of candidate values for each position in generated lists. */
  private final List<T>[] listsOfCandidates;
  /** Iterators for each list of candidate values. */
  private final Iterator<T>[] iterators;
  /** The size of each returned result; the length of listsOfCandidates. */
  private final @LengthOf({"listsOfCandidates", "iterators"}) int combinationSize;
  /** The next value to return, or null if to more values. */
  private @Nullable List<T> nextValue;

  /**
   * Creates a {@link CombinationIterator} for lists constructed from the given candidates. Each
   * generated list will be the same length as the given list.
   *
   * @param collectionsOfCandidates lists of candidate values for each position in generated lists
   */
  @SuppressWarnings({"rawtypes", "unchecked",  // for generic array creation
          "determinism:assignment.type.incompatible"  // Iteration over OrderNonDet collection for assigning into another
  })
  public CombinationIterator(Collection<? extends Collection<T>> collectionsOfCandidates) {
    int size = collectionsOfCandidates.size();
    // Just like collectionsOfCandidates, but indexable.
    @PolyDet ArrayList<? extends Collection<T>> listOfCollectionsOfCanditates =
        new ArrayList<>(collectionsOfCandidates);
    listsOfCandidates = new ArrayList[size];
    iterators = new Iterator[size];
    combinationSize = size;
    nextValue = (combinationSize == 0 ? null : new @PolyDet ArrayList<>(collectionsOfCandidates.size()));

    for (int i = 0; i < combinationSize; i++) {
      Collection<T> userSuppliedCandidates = listOfCollectionsOfCanditates.get(i);

      List<T> candidates = new ArrayList<>(userSuppliedCandidates);
      listsOfCandidates[i] = candidates;
      Iterator<T> it = candidates.iterator();
      iterators[i] = it;
      if (nextValue != null) {
        if (it.hasNext()) {
          nextValue.add(it.next());
        } else {
          nextValue = null;
        }
      }
    }
  }

  @Override
  @EnsuresNonNullIf(expression = "nextValue", result = true)
  @SuppressWarnings("determinism:return.type.incompatible")  // Safe to return PolyDet(down) boolean for a PolyDet nextValue
  public @PolyDet("down") boolean hasNext(@GuardSatisfied CombinationIterator<T> this) {
    return nextValue != null;
  }

  /** Advance {@code nextValue} to the next value, or to null if there are no more values. */
  @RequiresNonNull("nextValue")
  @SuppressWarnings("determinism:method.invocation.invalid")  // Advancing PolyDet pointers
  private void advanceNext(@GuardSatisfied CombinationIterator<T> this) {
    for (int i = combinationSize - 1; i >= 0; i--) {
      if (iterators[i].hasNext()) {
        nextValue.set(i, iterators[i].next());
        return;
      } else {
        iterators[i] = listsOfCandidates[i].iterator();
        nextValue.set(i, iterators[i].next());
      }
    }
    nextValue = null;
  }

  @Override
  @SuppressWarnings("determinism:argument.type.incompatible")  // OK to pass PolyDet nextValue to ArrayList constructor
  public @PolyDet("up") List<T> next(@GuardSatisfied CombinationIterator<T> this) {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }

    @PolyDet("up") List<T> result = new @PolyDet("up") ArrayList<T>(nextValue);
    advanceNext();
    return result;
  }

  @Override
  public void remove(@GuardSatisfied CombinationIterator<T> this) {
    throw new UnsupportedOperationException(
        "Remove not implemented for randoop.reflection.SubstitutionEnumerator");
  }
}
