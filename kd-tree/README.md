# K-Dimensional Tree (KD-Tree)

A space-partitioning data structure for organizing points in k-dimensional space.

## Use Case

- Nearest neighbor search (GPS, recommendation engines, image similarity)
- Range search (spatial databases, collision detection)
- Spatial indexing (robotics, computer vision, GIS)

## Pros and Cons

| Pros | Cons |
|---|---|
| Nearest neighbor in O(log n) average | Degrades to O(n) worst case if unbalanced |
| Range search prunes entire subtrees | Performance drops when k > 20 (curse of dimensionality) |
| Simple to implement | Static structure — insertions can unbalance it |
| Low memory overhead | Not cache-friendly for very large datasets |

## Operations

| Operation | Average | Worst |
|---|---|---|
| `build` | O(n log n) | O(n log n) |
| `insert` | O(log n) | O(n) |
| `nearestNeighbor` | O(log n) | O(n) |
| `rangeSearch` | O(sqrt(n) + m) | O(n) |
| `contains` | O(log n) | O(n) |

## Running

```bash
cd kd-tree
sbt test
```

## Tests

17 tests covering build, insert, contains, nearest neighbor, range search, 3D support, and brute-force correctness comparisons with randomized data.
