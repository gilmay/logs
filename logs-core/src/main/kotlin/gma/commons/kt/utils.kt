package gma.commons.kt

import java.util.*

fun <E> Collection<E>.asUnmodifiable(): Collection<E> = Collections.unmodifiableCollection(this)

fun <E> List<E>.asUnmodifiable(): List<E> = Collections.unmodifiableList(this)

fun <E> Set<E>.asUnmodifiable(): Set<E> = Collections.unmodifiableSet(this)

fun <E> SortedSet<E>.asUnmodifiable(): SortedSet<E> = Collections.unmodifiableSortedSet(this)

fun <E> NavigableSet<E>.asUnmodifiable(): NavigableSet<E> = Collections.unmodifiableNavigableSet(this)

fun <K, V> Map<K, V>.asUnmodifiable(): Map<K, V> = Collections.unmodifiableMap(this)

fun <K, V> SortedMap<K, V>.asUnmodifiable(): SortedMap<K, V> = Collections.unmodifiableSortedMap(this)

fun <K, V> NavigableMap<K, V>.asUnmodifiable(): NavigableMap<K, V> = Collections.unmodifiableNavigableMap(this)

fun <E> List<E>.toUnmodifiable(): List<E> = Collections.unmodifiableList(this.toList())

fun <E> Set<E>.toUnmodifiable(): Set<E> = Collections.unmodifiableSet(this.toSet())

fun <E> SortedSet<E>.toUnmodifiable(): SortedSet<E> = Collections.unmodifiableSortedSet(this.toSortedSet(comparator()))

fun <E> NavigableSet<E>.toUnmodifiable(): NavigableSet<E> =
    Collections.unmodifiableNavigableSet(toCollection(TreeSet(comparator())))

fun <K, V> Map<K, V>.toUnmodifiable(): Map<K, V> = Collections.unmodifiableMap(this.toMap())

fun <K, V> SortedMap<K, V>.toUnmodifiable(): SortedMap<K, V> =
    Collections.unmodifiableSortedMap(this.toSortedMap(comparator()))

fun <K, V> NavigableMap<K, V>.toUnmodifiable(): NavigableMap<K, V> =
    Collections.unmodifiableNavigableMap(TreeMap<K, V>(comparator()))
