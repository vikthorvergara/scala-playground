package kdtree

class KDTreeSpec extends munit.FunSuite:

  test("build empty tree") {
    val tree = KDTree.build(Seq.empty)
    assertEquals(tree, None)
  }

  test("build single point") {
    val tree = KDTree.build(Seq(Array(1.0, 2.0)))
    assert(tree.isDefined)
    assertEquals(KDTree.size(tree), 1)
    assert(tree.get.point.sameElements(Array(1.0, 2.0)))
  }

  test("build preserves all points") {
    val points = Seq(
      Array(2.0, 3.0),
      Array(5.0, 4.0),
      Array(9.0, 6.0),
      Array(4.0, 7.0),
      Array(8.0, 1.0),
      Array(7.0, 2.0)
    )
    val tree = KDTree.build(points)
    assertEquals(KDTree.size(tree), 6)
    val allPoints = KDTree.toList(tree)
    points.foreach { p =>
      assert(allPoints.exists(_.sameElements(p)), s"Missing point (${p(0)}, ${p(1)})")
    }
  }

  test("insert into empty tree") {
    val tree = KDTree.insert(None, Array(3.0, 4.0), k = 2)
    assert(tree.isDefined)
    assert(tree.get.point.sameElements(Array(3.0, 4.0)))
  }

  test("insert multiple points") {
    var tree: Option[KDNode] = None
    tree = KDTree.insert(tree, Array(5.0, 5.0), k = 2)
    tree = KDTree.insert(tree, Array(2.0, 3.0), k = 2)
    tree = KDTree.insert(tree, Array(8.0, 1.0), k = 2)
    tree = KDTree.insert(tree, Array(1.0, 9.0), k = 2)
    assertEquals(KDTree.size(tree), 4)
  }

  test("contains finds inserted points") {
    val points = Seq(
      Array(3.0, 6.0),
      Array(17.0, 15.0),
      Array(13.0, 15.0),
      Array(6.0, 12.0),
      Array(9.0, 1.0),
      Array(2.0, 7.0),
      Array(10.0, 19.0)
    )
    val tree = KDTree.build(points)
    points.foreach { p =>
      assert(KDTree.contains(tree, p, k = 2), s"Should contain (${p(0)}, ${p(1)})")
    }
    assert(!KDTree.contains(tree, Array(0.0, 0.0), k = 2))
    assert(!KDTree.contains(tree, Array(99.0, 99.0), k = 2))
  }

  test("nearest neighbor on single point") {
    val tree = KDTree.build(Seq(Array(1.0, 1.0)))
    val result = KDTree.nearestNeighbor(tree, Array(5.0, 5.0), k = 2)
    assert(result.isDefined)
    assert(result.get.sameElements(Array(1.0, 1.0)))
  }

  test("nearest neighbor finds exact match") {
    val points = Seq(
      Array(2.0, 3.0),
      Array(5.0, 4.0),
      Array(9.0, 6.0),
      Array(4.0, 7.0),
      Array(8.0, 1.0),
      Array(7.0, 2.0)
    )
    val tree = KDTree.build(points)
    val result = KDTree.nearestNeighbor(tree, Array(5.0, 4.0), k = 2)
    assert(result.get.sameElements(Array(5.0, 4.0)))
  }

  test("nearest neighbor finds closest point") {
    val points = Seq(
      Array(2.0, 3.0),
      Array(5.0, 4.0),
      Array(9.0, 6.0),
      Array(4.0, 7.0),
      Array(8.0, 1.0),
      Array(7.0, 2.0)
    )
    val tree = KDTree.build(points)
    val result = KDTree.nearestNeighbor(tree, Array(6.0, 3.0), k = 2)
    assert(result.isDefined)
    val nearest = result.get
    val bruteForceNearest = points.minBy(p => KDTree.distanceSq(p, Array(6.0, 3.0)))
    assertEqualsDouble(KDTree.distanceSq(Array(6.0, 3.0), nearest), KDTree.distanceSq(Array(6.0, 3.0), bruteForceNearest), 1e-9)
  }

  test("nearest neighbor brute force comparison") {
    val rng = new scala.util.Random(42)
    val points = (1 to 200).map(_ => Array(rng.nextDouble() * 100, rng.nextDouble() * 100))
    val tree = KDTree.build(points)

    for _ <- 1 to 50 do
      val target = Array(rng.nextDouble() * 100, rng.nextDouble() * 100)
      val kdResult = KDTree.nearestNeighbor(tree, target, k = 2).get
      val bruteForce = points.minBy(p => KDTree.distanceSq(p, target))
      val kdDist = KDTree.distanceSq(target, kdResult)
      val bfDist = KDTree.distanceSq(target, bruteForce)
      assertEqualsDouble(kdDist, bfDist, 1e-9)
  }

  test("range search returns points in bounds") {
    val points = Seq(
      Array(2.0, 3.0),
      Array(5.0, 4.0),
      Array(9.0, 6.0),
      Array(4.0, 7.0),
      Array(8.0, 1.0),
      Array(7.0, 2.0)
    )
    val tree = KDTree.build(points)
    val results = KDTree.rangeSearch(tree, Array(3.0, 2.0), Array(8.0, 5.0), k = 2)
    val expected = points.filter(p => p(0) >= 3 && p(0) <= 8 && p(1) >= 2 && p(1) <= 5)
    assertEquals(results.length, expected.length)
    expected.foreach { p =>
      assert(results.exists(_.sameElements(p)), s"Missing (${p(0)}, ${p(1)}) in range results")
    }
  }

  test("range search returns empty for no matches") {
    val points = Seq(Array(1.0, 1.0), Array(2.0, 2.0), Array(3.0, 3.0))
    val tree = KDTree.build(points)
    val results = KDTree.rangeSearch(tree, Array(10.0, 10.0), Array(20.0, 20.0), k = 2)
    assertEquals(results.length, 0)
  }

  test("range search brute force comparison") {
    val rng = new scala.util.Random(99)
    val points = (1 to 300).map(_ => Array(rng.nextDouble() * 50, rng.nextDouble() * 50))
    val tree = KDTree.build(points)

    for _ <- 1 to 30 do
      val x1 = rng.nextDouble() * 40
      val y1 = rng.nextDouble() * 40
      val lower = Array(x1, y1)
      val upper = Array(x1 + rng.nextDouble() * 10, y1 + rng.nextDouble() * 10)
      val kdResults = KDTree.rangeSearch(tree, lower, upper, k = 2)
      val bfResults = points.filter(p => p(0) >= lower(0) && p(0) <= upper(0) && p(1) >= lower(1) && p(1) <= upper(1))
      assertEquals(kdResults.length, bfResults.length)
  }

  test("3-dimensional tree") {
    val points = Seq(
      Array(1.0, 2.0, 3.0),
      Array(4.0, 5.0, 6.0),
      Array(7.0, 8.0, 9.0),
      Array(2.0, 1.0, 5.0),
      Array(6.0, 3.0, 4.0)
    )
    val tree = KDTree.build(points)
    assertEquals(KDTree.size(tree), 5)

    val nearest = KDTree.nearestNeighbor(tree, Array(3.0, 2.0, 4.0), k = 3)
    val bruteForce = points.minBy(p => KDTree.distanceSq(p, Array(3.0, 2.0, 4.0)))
    assert(nearest.get.sameElements(bruteForce))

    val range = KDTree.rangeSearch(tree, Array(0.0, 0.0, 0.0), Array(5.0, 5.0, 5.0), k = 3)
    val expected = points.filter(p => p(0) <= 5 && p(1) <= 5 && p(2) <= 5)
    assertEquals(range.length, expected.length)
  }

  test("distanceSq correctness") {
    assertEqualsDouble(KDTree.distanceSq(Array(0.0, 0.0), Array(3.0, 4.0)), 25.0, 1e-9)
    assertEqualsDouble(KDTree.distanceSq(Array(1.0, 1.0), Array(1.0, 1.0)), 0.0, 1e-9)
    assertEqualsDouble(KDTree.distanceSq(Array(1.0, 2.0, 3.0), Array(4.0, 6.0, 3.0)), 25.0, 1e-9)
  }

  test("nearest neighbor on empty tree") {
    val result = KDTree.nearestNeighbor(None, Array(1.0, 2.0), k = 2)
    assertEquals(result, None)
  }

  test("range search on empty tree") {
    val results = KDTree.rangeSearch(None, Array(0.0, 0.0), Array(10.0, 10.0), k = 2)
    assertEquals(results.length, 0)
  }
