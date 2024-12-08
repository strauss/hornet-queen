# Hornet Queen

Hornet Queen is a collection library for primitive types.
It aims at preserving runtime memory while still sustaining performance in comparison to the standard collection framework of Java/Kotlin.
The library is written in Kotlin and perfectly usable in any Kotlin or Java project.
The Kotlin code is written in a "Java friendly" way for allowing a seamless integration in Java projects.
The target VM version is 17.
The used Kotlin version is 2.0.10-RC (the latest and greatest).

Hornet Queen was inspired by GNU Trove4J, which is unfortunately discontinued.
I was impressed by its implementation of hash tables and I actually adapted this implementation for Hornet Queen.

## Content (so far)

The library is not complete yet. It currently includes the following:

- Array
    - [`PrimitiveArray`](src/main/kotlin/de/dreamcube/hornet_queen/array/PrimitiveArray.kt) (oh yes, I implemented my own arrays, the reason is
      explained further down the document)
- Lists
    - [`PrimitiveArrayBasedList`](src/main/kotlin/de/dreamcube/hornet_queen/list/PrimitiveArrayBasedList.kt)
        - [`PrimitiveArrayList`](src/main/kotlin/de/dreamcube/hornet_queen/list/PrimitiveArrayList.kt)
        - [`PrimitiveLinkedList`](src/main/kotlin/de/dreamcube/hornet_queen/list/PrimitiveLinkedList.kt) (yes, it is array based, and yes, it makes
          sense)
- Sets
    - [`HashTableBasedSet`](src/main/kotlin/de/dreamcube/hornet_queen/set/HashTableBasedSet.kt)
    - [`BitSetBasedSet`](src/main/kotlin/de/dreamcube/hornet_queen/set/BitSetBasedSet.kt) (for `Byte`, `Short`, `Char`, and `Int`)
- Map
    - [`HashTableBasedMap`](src/main/kotlin/de/dreamcube/hornet_queen/map/HashTableBasedMap.kt)

The hash set and the hash map implementation are based on the same hash table implementation.
The supported primitive types are (in Kotlin's notation) `Byte`, `Short`, `Char`, `Int`, `Float`, `Long`, `Double` and finally `UUID`, although it is
technically not a primitive type at all.

Including `Boolean` does not make sense in this context.
A set of `Boolean` is ... let's say useless.
A map of any type to `Boolean` can be achieved by simply using a set.
At most, a list of `Boolean` could be usable, but I am not planning to include it in the near future.

## How to integrate Hornet Queen as dependency

You need to add Jitpack as repository and include the dependency on this project.

### Maven

```xml

<project>
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
    
    <dependencies>
        <dependency>
            <groupId>de.dreamcube</groupId>
            <artifactId>hornet-queen</artifactId>
            <version>0.2.0</version>
        </dependency>
    </dependencies>
</project>
```

### Gradle

#### Kotlin

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("de.dreamcube:hornet-queen:0.2.0")
}
```

#### Groovy

```groovy
repositories {
    maven {
        url "https://jitpack.io"
    }
}

dependencies {
    implementation 'de.dreamcube:hornet-queen:0.2.0'
}
```

## Key features

- No major dependencies, except for the Kotlin runtime.
- The library is implemented without any form of code generation.
    - Comparable libraries, such as GNU Trove4J, use code templates and code generation based on these templates for creating copies of the
      implementing classes.
    - Hornet Queen tries to implement everything as generically as possible for avoiding any form of code generation or code duplication.
    - In most of the cases, only the constructors for each concrete class is implemented (one implementation class per primitive type).
- All collection classes implement the matching interfaces of the standard collection framework. Examples:
    - `PrimitiveIntArrayList` implements the Kotlin interface `MutableList<Int>` and therefore also the Java interface `List<Integer>`.
    - `PrimitiveByteSetB` (BitSet based set for `Byte`) implements the Kotlin interface `MutableSet<Byte>` and therefore also the Java
      interface `Set<Byte>`.
    - `HashTableBasedMap<Int, Any>` implements the Kotlin interface `MutableMap<Int, Any>` and therefore also the Java
      interface `Map<Integer, Object>`.
- The Java class `UUID` is treated as primitive type, resulting in a space efficient way for creating a set of `UUID`s or using a `UUID` as key for a
  map that is used for a cache.

## Implementation details

### Primitive types, Kotlin and generic type variables

Unlike Java, Kotlin does not distinguish between primitive types (e.g., `int`) and wrapper types (e.g., `Integer`).
Instead, a common type name is used (e.g., `Int`).
In some cases they are identical to the wrapper class' name (e.g., `Long`).
This allows for implicitly using primitive types in a generic way, such as in interfaces.
Kotlin automatically chooses the best underlying type for the situation.
Hornet Queen explicitly enforces the usage of primitive types in all its implementing classes due to the nature of the underlying array
implementation (see next section).

The worst thing, that can happen, is autoboxing the values when querying for them.
When using the library from within Kotlin, this usually doesn't happen.
When using it from within Java it might happen.
However, this process is unnoticeably fast.
The exception is the `UUID` type.
This one is always explicitly (un-)boxed.
When comparing Hornet Queen's `UUIDSet` with a Java `HashSet<UUID>` the former can compete in most scenarios while saving memory.
For performance comparison have a look at the
[`SetPerformanceComparison.kt`](src/test/kotlin/de/dreamcube/hornet_queen/set/set_performance_comparison.kt) file among the test cases.

### The Array implementation

Almost all collections in this library are based on arrays.
Since one of the goals is achieving a very high level of generality, the arrays also have to be generic.
Primitive arrays are well known for not being generic at all.

The solution was implementing my own array class.
The first attempt was the class [`PrimitiveArrayWithConverters`](src/main/kotlin/de/dreamcube/hornet_queen/array/PrimitiveArrayWithConverters.kt),
which uses a `ByteArray` as underlying data structure and (dis-)assembles the data when writing to or reading from it.
It works, but it is slower than a standard primitive array.
I left it in the library for fun, but it shouldn't be used.
If you want to see how slow it is, have a look at [`arrayComparison.kt`](src/test/kotlin/de/dreamcube/hornet_queen/array/array_comparison.kt) among
the test cases.

The more successful attempt was adapting the Java class `java.nio.ByteBuffer`.
As the name suggests, the `ByteBuffer` is usually used as buffer structure for file operations in the Java native input output classes.
It also allows for directly accessing data within the structure by index.
The best part are its convenience methods for reading primitive types from the given index.
This aspect, in combination with Kotlins ability to define the operator functions `get` and `set`, enable my implementation to be used exactly as a
normal array, at least from within Kotlin.

The bonus feature of `ByteBuffer` is the ability to use a native byte array as underlying structure.
Using a normal (non-native) byte array, results in a generic array which is as efficient as a normal primitive array of any primitive type.
Switching to native mode results in a generic array with superior efficiency compared to primitive arrays of the same type.

All collection implementations in Hornet Queen, that are based on this generic array implementation, can be used in native mode (which is the
default).
The native `ByteBuffer` is the main reason why Hornet Queen performs very well in comparison to the non-generic implementations, such as Trove4J.

#### Disadvantages

- The underlying structure of `ByteBuffer` is always a byte array with a maximum size of about 2GB (2^31 bytes). Therefore, the index space is
  limited, based on the primitive data type. It always starts at 0 and ends at:
    - `Byte`:  2,147,483,647 (`Int.MAX_VALUE` - 8)
    - `Short` and `Char`: 1,073,741,823
    - `Int` and `Float`: 536,870,911
    - `Long` and `Double`: 268,435,455
    - `UUID`: 134,217,727

I got rid of the first disadvantage.
Both implementations now use the mechanics the `ByteBuffer` is actually meant to be used for copying the content.

I have plans to overcome the size limitation, this is currently not at the top of the priority list (see last section).

### List implementations

Both list implementations are based on the generic array implementation, even the linked list.
The array list is a straightforward adaptation of the standard `ArrayList`.
Its main advantage is the avoidance of wrapper classes and, if using the native mode, its superior performance.

The linked list implementation is a bit odd.
One would argue that a linked list's main advantage is the avoidance of unused array space.
This advantage is, naturally, lost in this approach.
However, instead of node objects, my linked list implementation uses two
[`PrimitiveIntArray`](src/main/kotlin/de/dreamcube/hornet_queen/array/PrimitiveIntArray.kt)s for storing the forward and backward links.
Node objects are fully blown objects with an overhead of 8 to 16 bytes each (depending on the JVM implementation).
Each node objects uses two references to the successor and predecessor respectively.
A reference takes 4 to 8 bytes of space, also depending on the JVM implementation.
And I didn't consider the usage of wrapper classes in this discussion.

That means, even if there is a bit of "dead memory" in form of unused array space, it is very likely that a java `LinkedList` of a primitive type
would still take up more space than the array based linked list.

In most cases the [`PrimitiveArrayList`](src/main/kotlin/de/dreamcube/hornet_queen/list/PrimitiveArrayList.kt) should be used.
There are some niche applications where a [`PrimitiveLinkedList`](src/main/kotlin/de/dreamcube/hornet_queen/list/PrimitiveLinkedList.kt) is
preferable.
It depends on how you use the list.
E.g., if you are planning on filtering out elements in-place, using the iterator, a linked list is the better option because it does not require
expensive shift operations that would occur if doing it with an array list instead.

### Set implementations

#### BitSet based sets

The Java class `BitSet` is a highly underestimated data structure that certainly needs more love.
I tried to give it exactly that by adapting it to being used as foundation for some of Hornet Queen's set implementations.

A `BitSet` is capable of assigning each positive integer a boolean value.
Under ideal circumstances, an entry in this set occupies exactly one bit.
The actual space consumption is determined by the biggest number in the set.
If `Int.MAX_VALUE` is part of the set (worst case), its memory consumption is 256MB, even if it's the only element in the set.
For a more realistic assumption consider this: If you know that your biggest number is 1023 you will need 128 bytes of memory as soon as you store the
number 1023 in the set.

The bit set adaptations, used in Hornet Queen, also allow for negative values.
For `Byte` and `Short` the values are interpreted as unsigned values.
`Char` is by itself an unsigned value and does not allow for negative values in the first place.
For `Int`, two bit sets are used and the negative values are mapped to the positive range (shifted by 1 for avoiding overflows ...
see [`BitSetBasedSet.kt`](src/main/kotlin/de/dreamcube/hornet_queen/set/BitSetBasedSet.kt) for details).

Here are the maximum sizes of [`BitSetBasedSet`](src/main/kotlin/de/dreamcube/hornet_queen/set/BitSetBasedSet.kt)

- `PrimitiveByteSetB`: 32 bytes
- `PrimitiveShortSetB` and `PrimitiveCharSetB`: 8 KB
- `PrimitiveIntSetB`: 512 MB

For byte sets choosing the bit set variant is almost always the better option.
For short sets the same reasoning is true.
For char sets it is even more true because most common chars are in the lower range of possible values.
For integer sets it highly depends on the expected number range.

Depending on the operation, the `BitSet` based sets perform faster.
The iterator performs slower because it is required to also iterate over the numbers that are not part of the set.
This is actually the only disadvantage of `BitSet`s.
Execute [`SetPerformanceComparison.kt`](src/test/kotlin/de/dreamcube/hornet_queen/set/set_performance_comparison.kt) to see for yourself.

#### Hash table based

Hornet Queen's hash table implementation is in principle identical to the hash table implementation of GNU Trove4J.
It trades space for speed and vice versa.
The actual trade can be chosen by the load factor.
The load factor has to be above 0 and at most 1.
Reasonable values are between 0.5 and 0.95.
The default load factor is 0.75 (in analogy to Java).
Trove4J uses 0.5 as default, which is certainly faster, but in some applications memory is more important than speed.
Experiments have shown, that even a value of 0.9 can result in fairly efficient hash tables.

The higher the load factor, the higher the chances for collisions.
The more collisions, the slower the structure.
Ideally you want no collisions at all.
If you are unsure, stick to the 0.75 or experiment with different values.

If you have to remove elements frequently, it is possible that the structure grows physically but without growing logically.
The reason is the nature of collision handling.
If you remove a value, it is only marked as being deleted and the space cannot be freed right away (that would be a costly operation).

The [`HashTableBasedSet`](src/main/kotlin/de/dreamcube/hornet_queen/set/HashTableBasedSet.kt) provides the functions `manualRehash()`
and `shrinkToLoadFactor()`.
The function `manualRehash()` effectively frees all the deleted cells without changing the size of the underlying structure.
It is useful if you plan on adding more elements to the set.
The function `shrinkToLoadFactor()` does the same, but also shrinks the structure to match the load factor.
This is useful if you know that you are done with creating the set and don't expect more elements to be added.

Trove4J has an automatic shrinking mechanism if the hash table is below a certain load factor.
I decided that I don't want to adapt this aspect.
I wanted to give the users of Hornet Queen the freedom (but also the responsibility) to decide when to shrink/rehash the hash table.

### Map implementation(s)

Currently, there is only the hash table based map implemented.
The underlying data structure is the very same hash table as for the sets.
Everything that applies to hash table based sets also applies to hash table based maps.
This is especially noticeable for the functions `manualRehash()` and `shrinkToLoadFactor()`.

The hash table based maps in Hornet Queen are special in that they support both primitive and non-primitive value types.
The keys are always primitive types (including `UUID`).
All structures, covered so far, have their dedicated classes that can be used: e.g., `PrimitiveIntSet` or `UUIDArrayList`.
For maps this would require a lot of different classes (81 ... 90 if you want to allow for object value types).
This approach did not seem reasonable for me so came up with a different solution.

#### Instantiating primitive maps

Usually I don't like the builder pattern.
In most cases it lacks benefits and adds unnecessary complexity.
Here, however, I saw potential in utilizing the builder pattern for creating arbitrary primitive maps.

The class [`HashTableBasedMapBuilder`](src/main/kotlin/de/dreamcube/hornet_queen/map/HashTableBasedMapBuilder.kt) contains everything that is required
for creating a map with primitive key types and arbitrary value types.
I will explain its usage with examples.

##### Kotlin

```kotlin
// simple example
val intIntMap: MutableMap<Int, Int> = HashTableBasedMapBuilder
    .useIntKey()
    .useIntValue()
    .create()

// complex example, parameter names given for clarity
val uuidStringMap: MutableMap<UUID, String> = HashTableBasedMapBuilder
    .useUUIDKey(native = false)
    .useArbitraryTypeValue<String>()
    .create(initialCapacity = 42, loadFactor = 0.8)
```

##### Java

```java
// simple example
final Map<Integer, Integer> integerIntegerMap = HashTableBasedMapBuilder
   .useIntKey()
   .useIntValue()
   .create();

// complex example, parameter names cannot be given because Java :-) Look at the Kotlin example!
final Map<UUID, String> uuidStringMap = HashTableBasedMapBuilder
   .useUUIDKey(false)
   .<String>useArbitraryTypeValue()
   .create(42, 0.8);
```

Please note that `useIntValue()` and all other corresponding functions also have the native flag as optional parameter.

## Known issues

- In some cases the implicit maximum array size can be a problem when resizing a hash table.
    - This will be addressed in future releases, maybe by somehow™ getting rid of the size limit.
- The generic arrays are no classical arrays and all array convenience functions in Kotlin and convenience methods from the class `Arrays` are not
  applicable.
    - For now, this seems to be an unsolvable problem. Maybe I will provide some of these functions/methods in the future.
- There is a bug in the Linked List if the first element is removed repeatedly and only one element is left. For now just use the Array List :-)

## (E)FAQs

Since this is the first release, there have not been any questions yet ... therefore the "E" stands for "expected".

- Why did you pick this name?
    - First of all, I am very bad at naming things.
    - While developing this library, a hornet queen "invaded" my office.
    - People have named better things in a worse way.
- Why did you write the library in Kotlin?
    - Kotlin currently is the best language for the JVM.
    - Kotlin offers features that wouldn't be (easily) possible in Java benefiting Hornet Queen.
    - Kotlin is the language of my personal choice when it comes to developing something besides my actual job.
- Why do you treat UUID as primitive type?
    - UUIDs are a great choice as primary key in database tables for several reasons that are beyond the scope of this documentation.
    - When creating a data cache, the most obvious data structure is a Map with the primary key as map key.
    - Libraries like Trove4J provided the `TLongHashMap` for `long` keys saving both space and time.
    - I wanted to provide something comparable for `UUID` keys.
    - Actually, a Trove4J based implementation of their HashTable for UUID keys was the core inspiration for Hornet Queen.
      I had to copy a lot of their code to make it work and I realized that this approach was not feasible at all.
      This experience also lead to my desire to avoid code generation at all costs.
- What are you hiding in the `hash` package among the test cases?
    - Just go and see for yourself :-)

## Planned features for the future

- Tree based sets and maps
    - Trading more time for less space ... if done correctly
- Heap based priority queues
    - Because ... why not?
- Overcome the size limitation of generic arrays and raise it to `Int.MAX_VALUE` for all types or even beyond (index space `Long` would be fun). The
  sky is the limit :-)
- Convenience functions for generic arrays.
- Multidimensional generic arrays
    - I might also include my implementation of multidimensional regular arrays that is slumbering in one of my private toy projects.
- (Truly) immutable collections
    - Those are especially useful for functional programming
- Bit set based boolean lists ... if anyone needs them.
- Whatever else comes into my mind and fits into Hornet Queen :-)
