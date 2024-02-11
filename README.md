# EvPathFinder

This application provides an algorithm for finding the shortest path in a graph for electric cars. In addition to the duration required by an edge, the resulting consumption is also taken into account. Charging stations can also occur at nodes and offer the possibility of increasing the range.

## History

EvPathFinder was developed as part of a student research project at the Cooperative State University Stuttgart Campus Horb. The application can be extended by any number of other shortest-path algorithms, whereby the framework conditions are determined by the properties of the graph.

## Getting Started

### Dependencies and Installation
- Neo4j
- Adapt the URI and AuthTokens in the code accordingly.

```java
    public Neo4jReader(String uri, String user, String password) {
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }
```

### Usage
- The application is currently controlled via the console.


## Contributing

Pull requests are welcome. For major changes, please open an issue first
to discuss what you would like to change.

Please make sure to update tests as appropriate.

## Authors

[@FabUnger](https://github.com/FabUnger)
