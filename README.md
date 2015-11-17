# Paleo  [![Build Status](https://travis-ci.org/netzwerg/paleo.svg?branch=master)](https://travis-ci.org/netzwerg/paleo) [ ![Download](https://api.bintray.com/packages/netzwerg/maven/paleo/images/download.svg) ](https://bintray.com/netzwerg/maven/paleo/_latestVersion)

Immutable Java 8 data frames with typed columns.

A data frame is composed of `0..n` named columns, which all contain the same number of row values. Each column has a fixed
data type, which allows for type-safe value access. The following column types are supported out-of-the-box:

* **Int**: Primitive `int` values
* **Double**: Primitive `double` values
* **Boolean**: Primitive `boolean` values
* **String**: `java.lang.String` values
* **Timestamp**: `java.time.Instant` values
* **Category**: Categorical `String` values (aka factors)

Columns can be created via simple factory methods, or populated from text files.

# Hello Paleo

The `paleo-core` module provides all classes to identify, create, and structure typed columns: 

```java
// Type-safe column identifiers
final StringColumnId NAME = ColumnIds.stringCol("Name");
final CategoryColumnId COLOR = ColumnIds.categoryCol("Color");
final DoubleColumnId SERVING_SIZE = ColumnIds.doubleCol("Serving Size (g)");

// Convenient column creation
StringColumn nameColumn = StringColumn.ofAll(NAME, "Banana", "Blueberry", "Lemon", "Apple");
CategoryColumn colorColumn = CategoryColumn.ofAll(COLOR, "Yellow", "Blue", "Yellow", "Green");
DoubleColumn servingSizeColumn = DoubleColumn.ofAll(SERVING_SIZE, 118, 148, 83, 182);

// Grouping columns into a data frame
DataFrame dataFrame = DataFrame.ofAll(nameColumn, colorColumn, servingSizeColumn);

// Typed random access to individual values (based on rowIndex / columnId)
String lemon = dataFrame.getValueAt(2, NAME);
double appleServingSize = dataFrame.getValueAt(3, SERVING_SIZE);

// Typed stream-based access to all values
DoubleStream servingSizes = servingSizeColumn.getValues();
double maxServingSize = servingSizes.summaryStatistics().getMax();

// Smart column implementations
Set<String> colors = colorColumn.getCategories();
```

# Parsing From Text / File

The `paleo-io` module parses data frames from tab-delimited text representations. The structure of the
data frame (i.e. the names and types of its columns) can be defined in one of two ways:

## Header Rows

In its simplest format, the tab-delimited text representation directly contains the column meta-data in the form of two
header rows. The first row specifies the column names, the second row specifies the column types (actual data starting
on third row):

```
1 Name    Color
2 String  Category
3 Banana  Yellow
...
n Apple   Green
```

The contents can then be parsed via `Parser.parseTabDelimited(Reader in)`, e.g. like:

```java
final String EXAMPLE =
            "Name\tColor\tServing Size (g)\n" +
            "String\tCategory\tDouble\n" +
            "Banana\tYellow\t118\n" +
            "Blueberry\tBlue\t148\n" +
            "Lemon\tYellow\t83\n" +
            "Apple\tGreen\t182";

DataFrame dataFrame = Parser.parseTabDelimited(new StringReader(EXAMPLE));
```

## External JSON Schema

Generally it is advisable to separate the structural information from the actual data. Paleo therefore supports the
definition of an external JSON schema. The format is inspired by the [JSON Table Schema](http://dataprotocols.org/json-table-schema):

```json
{
  "title": "Example Schema",
  "dataFileName": "data.txt",
  "fields": [
    {
      "name": "Name",
      "type": "String"
    },
    {
      "name": "Color",
      "type": "Category"
    },
    {
      "name": "Serving Size (g)",
      "type": "Double"
    },
    {
      "name": "Exemplary Date",
      "type": "Timestamp",
      "format": "yyyyMMddHHmmss"
    }
  ]
}
```

Dedicated parsing methods allow to first parse the schema from JSON, and subsequently use it to create a `DataFrame`.
A given base directory is used to load the actual data (i.e. to resolve the location of the configured `dataFileName`):

```java
Schema schema = Schema.parseJson(new StringReader(EXAMPLE_SCHEMA));
DataFrame dataFrame = Parser.parseTabDelimited(schema, baseDir);
```

## Working With Parsed Data Frames

Once a `DataFrame` instance has been parsed, its data can be accessed through a type-safe API:

```java
final String EXAMPLE =
            "Name\tColor\tServing Size (g)\n" +
            "String\tCategory\tDouble\n" +
            "Banana\tYellow\t118\n" +
            "Blueberry\tBlue\t148\n" +
            "Lemon\tYellow\t83\n" +
            "Apple\tGreen\t182";

DataFrame dataFrame = Parser.parseTabDelimited(new StringReader(EXAMPLE));

// Lookup typed identifiers by column index
final StringColumnId NAME = dataFrame.getColumnId(0, ColumnType.STRING);
final CategoryColumnId COLOR = dataFrame.getColumnId(1, ColumnType.CATEGORY);
final DoubleColumnId SERVING_SIZE = dataFrame.getColumnId(2, ColumnType.DOUBLE);

// Use identifier to access columns & values
StringColumn nameColumn = dataFrame.getColumn(NAME);
IndexedSeq<String> nameValues = nameColumn.getValues();

// ... or access individual values via row index / column id 
String yellow = dataFrame.getValueAt(2, COLOR);
```

# Usage

All modules are available via [Bintray/JCenter](https://bintray.com/netzwerg/maven/paleo/view).

## Repository Configuration

Gradle:

```groovy
repositories {
    jcenter()
}
```

Maven `settings.xml`:

```xml
<repository>
    <snapshots>
      <enabled>false</enabled>
    </snapshots>
    <id>central</id>
    <name>bintray</name>
    <url>http://jcenter.bintray.com</url>
</repository>
```

## Using the `paleo-core` module

Gradle:

```groovy
compile 'ch.netzwerg:paleo-core:0.3.0'
```

Maven:

```xml
<dependency>
    <groupId>ch.netzwerg</groupId>
    <artifactId>paleo-core</artifactId>
    <version>0.3.0</version>
    <type>jar</type>
</dependency>
```

## Using the `paleo-io` module

Optional (requires `paleo-core`)

Gradle:

```groovy
compile 'ch.netzwerg:paleo-io:0.3.0'
```

Maven:

```xml
<dependency>
    <groupId>ch.netzwerg</groupId>
    <artifactId>paleo-io</artifactId>
    <version>0.3.0</version>
    <type>jar</type>
</dependency>
```

# Javaslang

Paleo makes extensive use of the [Javaslang](https://github.com/javaslang/javaslang) library. Javaslang provides
awesome collection classes which offer functionality way beyond the standard JDK. Working with the Javaslang classes
is highly recommended, but it is always possible to back out and convert to JDK standards (e.g. with `toJavaList()`).

# Factory-Methods vs. Builders

Paleo tries to make the best compromise between parsing speed, index-based value lookup, and memory usage. That's why
it offers two ways to create columns: Static factory methods allow for convenient construction if all values are already
available. Individual column builders should be used if columns are constructed via successive value addition.

# Why The Name?

The backing data structures are all about **raw** values and **primitive** types &mdash; this somehow reminded me of
the paleo diet.

&copy; 2015 Rahel Lüthy
