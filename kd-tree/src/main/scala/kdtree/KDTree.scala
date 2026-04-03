package kdtree

import scala.math.Ordering.Double.TotalOrdering

type Point = Array[Double]

case class KDNode(
    point: Point,
    left: Option[KDNode] = None,
    right: Option[KDNode] = None,
    splitDim: Int = 0
)

object KDTree:

  def build(points: Seq[Point]): Option[KDNode] =
    if points.isEmpty then None
    else buildRecursive(points, depth = 0, points.head.length)

  private def buildRecursive(points: Seq[Point], depth: Int, k: Int): Option[KDNode] =
    if points.isEmpty then return None
    val axis = depth % k
    val sorted = points.sortBy(_(axis))
    val medianIdx = sorted.length / 2
    val medianPoint = sorted(medianIdx)
    val others = sorted.take(medianIdx) ++ sorted.drop(medianIdx + 1)
    val (leftPts, rightPts) = others.partition(_(axis) < medianPoint(axis))
    Some(KDNode(
      point = medianPoint,
      left = buildRecursive(leftPts, depth + 1, k),
      right = buildRecursive(rightPts, depth + 1, k),
      splitDim = axis
    ))

  def insert(root: Option[KDNode], point: Point, k: Int): Option[KDNode] =
    root match
      case None => Some(KDNode(point, splitDim = 0))
      case Some(node) => Some(insertRecursive(node, point, depth = 0, k))

  private def insertRecursive(node: KDNode, point: Point, depth: Int, k: Int): KDNode =
    val axis = depth % k
    if point(axis) < node.point(axis) then
      node.copy(left = Some(node.left match
        case None => KDNode(point, splitDim = (depth + 1) % k)
        case Some(left) => insertRecursive(left, point, depth + 1, k)
      ))
    else
      node.copy(right = Some(node.right match
        case None => KDNode(point, splitDim = (depth + 1) % k)
        case Some(right) => insertRecursive(right, point, depth + 1, k)
      ))

  def nearestNeighbor(root: Option[KDNode], target: Point, k: Int): Option[Point] =
    root.map(node => nnSearch(node, target, node.point, 0, k))

  private def nnSearch(node: KDNode, target: Point, best: Point, depth: Int, k: Int): Point =
    val axis = depth % k
    val currentBest =
      if distanceSq(target, node.point) < distanceSq(target, best) then node.point
      else best

    val diff = target(axis) - node.point(axis)
    val (first, second) = if diff < 0 then (node.left, node.right) else (node.right, node.left)

    val bestAfterFirst = first match
      case Some(child) => nnSearch(child, target, currentBest, depth + 1, k)
      case None => currentBest

    val bestAfterSecond =
      if diff * diff < distanceSq(target, bestAfterFirst) then
        second match
          case Some(child) => nnSearch(child, target, bestAfterFirst, depth + 1, k)
          case None => bestAfterFirst
      else bestAfterFirst

    bestAfterSecond

  def rangeSearch(root: Option[KDNode], lower: Point, upper: Point, k: Int): Seq[Point] =
    root match
      case None => Seq.empty
      case Some(node) => rangeRecursive(node, lower, upper, 0, k)

  private def rangeRecursive(node: KDNode, lower: Point, upper: Point, depth: Int, k: Int): Seq[Point] =
    val axis = depth % k
    val inRange = (0 until k).forall(i => node.point(i) >= lower(i) && node.point(i) <= upper(i))
    val current = if inRange then Seq(node.point) else Seq.empty

    val searchLeft = node.left.isDefined && lower(axis) <= node.point(axis)
    val searchRight = node.right.isDefined && upper(axis) >= node.point(axis)

    val leftResults =
      if searchLeft then rangeRecursive(node.left.get, lower, upper, depth + 1, k)
      else Seq.empty

    val rightResults =
      if searchRight then rangeRecursive(node.right.get, lower, upper, depth + 1, k)
      else Seq.empty

    current ++ leftResults ++ rightResults

  def distanceSq(a: Point, b: Point): Double =
    a.zip(b).map((ai, bi) => (ai - bi) * (ai - bi)).sum

  def contains(root: Option[KDNode], target: Point, k: Int): Boolean =
    root match
      case None => false
      case Some(node) =>
        if node.point.sameElements(target) then true
        else
          val axis = node.splitDim
          if target(axis) < node.point(axis) then contains(node.left, target, k)
          else contains(node.right, target, k)

  def size(root: Option[KDNode]): Int =
    root match
      case None => 0
      case Some(node) => 1 + size(node.left) + size(node.right)

  def toList(root: Option[KDNode]): List[Point] =
    root match
      case None => Nil
      case Some(node) => toList(node.left) ++ List(node.point) ++ toList(node.right)
