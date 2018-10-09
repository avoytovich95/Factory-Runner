package com.parallel

import java.util.concurrent.ThreadLocalRandom
import kotlin.collections.ArrayList

class Factory(private val maxX: Int, private val maxY: Int): Comparable<Factory> {

  class Coord(val x: Int, val y: Int) {
    override fun toString(): String {
      return "($x, $y)"
    }
  }

  private lateinit var stations: Array<Station>
  private var layout = Array(maxX) { _ -> Array(maxY) { -1 } }

  // Variables for calculating the fitness of the factory
  private val affinityList = ArrayList<Double>()
  private val visited = ArrayList<Coord>()
  private val maxRecursion = 3
  private var currentRecursion = 0

  //Tracking variables
  var fitness = 0.0
  var mutations = 0
  var generation = 0
  val mutationProb = 25

  constructor(maxX: Int, maxY: Int, stations: Array<Station>): this(maxX, maxY) {
    placeStations(stations)
  }

  fun placeStations(stations: Array<Station>) {
    this.stations = stations.clone()
    for (i in 0 until this.stations.size) {
      var x: Int
      var y: Int
      while (true) {
        x = ThreadLocalRandom.current().nextInt(0, maxX)
        y = ThreadLocalRandom.current().nextInt(0, maxY)

        if (layout[x][y] == -1) {
          layout[x][y] = i
          break
        }
      }
    }
    findFitness()
  }

  /**
   * Mating function
   */
  operator fun plus(o: Factory): Factory =
    if (ThreadLocalRandom.current().nextInt(0, 101) > 5) {
      breed(o)
    } else {
      if (ThreadLocalRandom.current().nextInt(0, 101) <= mutationProb)
        mutate()
      if (ThreadLocalRandom.current().nextInt(0, 101) <= mutationProb)
        o.mutate()
      val offSpring = Factory(maxX, maxY)
      offSpring.placeStations(this.stations)
      offSpring
    }

  operator fun get(x: Int, y: Int): Station? {
    val i = layout[x][y]
    return if (i != -1)
      stations[i]
    else
      null
  }

  private fun breed(o: Factory): Factory {
    val splitX = ThreadLocalRandom.current().nextInt(2, maxX - 1)
    val splitY = ThreadLocalRandom.current().nextInt(2, maxY - 1)
    val xRange: IntRange
    val yRange: IntRange

    when (ThreadLocalRandom.current().nextInt(1, 5)) {
      1 -> {
        xRange = splitX until maxX
        yRange = splitY until maxY
      }
      2 -> {
        xRange = 0 until splitX
        yRange = splitY until maxY

      }
      3 -> {
        xRange = 0 until splitX
        yRange = 0 until splitY
      }
      4 -> {
        xRange = splitX until maxX
        yRange = 0 until splitY
      }
      else -> throw IllegalThreadStateException("Unexpected random value!")
    }

    if (ThreadLocalRandom.current().nextInt(0, 101) < 5)
      mutate()
    if (ThreadLocalRandom.current().nextInt(0, 101) < 5)
      o.mutate()

    val thisFitness = findFitness(xRange, yRange, true)
    val otherFitness = o.findFitness(xRange, yRange, true)

    val gen = if (this.generation > o.generation)
      generation
    else
      o.generation

    return when {
      thisFitness > otherFitness -> createOffSpring(layout, xRange, yRange, gen)
      thisFitness < otherFitness -> createOffSpring(o.layout, xRange, yRange, gen)
      ThreadLocalRandom.current().nextBoolean() -> createOffSpring(layout, xRange, yRange, gen)
      else -> createOffSpring(o.layout, xRange, yRange, gen)
    }
  }

  private fun createOffSpring(grid: Array<Array<Int>>, xRange: IntRange, yRange: IntRange, gen: Int): Factory {
    val offSpring = Factory(maxX, maxY)
    offSpring.generation = gen + 1
    offSpring.stations = this.stations

    val transferred = ArrayList<Int>()
    xRange.forEach { x ->
      yRange.forEach { y ->
        offSpring.layout[x][y] = grid[x][y]
        if (grid[x][y] != -1)
          transferred += grid[x][y]
      }
    }

    for (i in 0 until stations.size) {
      if (i !in transferred) {
        while (true) {
          val x = ThreadLocalRandom.current().nextInt(0, maxX)
          val y = ThreadLocalRandom.current().nextInt(0, maxY)

          if (offSpring.layout[x][y] == -1) {
            offSpring.layout[x][y] = i
            transferred += i
            break
          }
        }
      }
    }
    offSpring.findFitness()
    return offSpring
  }

  private fun findFitness(): Double {
    fitness = findFitness(0 until maxX, 0 until maxY, false)
    return fitness
  }

  private fun findFitness(xRange: IntRange, yRange: IntRange, quad: Boolean): Double {
    affinityList.clear()
    visited.clear()

    xRange.forEach { x ->
      yRange.forEach { y ->
        if (layout[x][y] != -1) {
          findAffinity(x, y)
          if (quad)
            stations[layout[x][y]].setTempAffinity(affinityList)
          else
            stations[layout[x][y]].setAffinity(affinityList)

          affinityList.clear()
          visited.clear()
        }
      }
    }
    var total = 0.0
    var count = 0
    xRange.forEach { x ->
      yRange.forEach { y ->
        if (layout[x][y] != -1) {
          count ++
          total += stations[layout[x][y]].affinity
        }
      }
    }
    return total / count
  }

  private fun findAffinity(x: Int, y: Int) {
    if (x < 0 || x >= maxX || y < 0 || y >= maxY
        || currentRecursion == maxRecursion)
      return
    else if (Coord(x, y) !in visited) {
      currentRecursion++

      if (layout[x][y] == -1)
        affinityList += 0.0
      else if (currentRecursion != 1)
        affinityList.add((stations[layout[x][y]].score.toDouble() / (currentRecursion - 1)))

      visited += Coord(x, y)

      findAffinity(x, y + 1)
      findAffinity(x + 1, y)
      findAffinity(x, y - 1)
      findAffinity(x - 1, y)
      currentRecursion--
    }
  }

  private fun mutate() {
    mutations++
    val mutations = ThreadLocalRandom.current().nextInt(1, Math.floor((maxX * maxY).toDouble() / 2).toInt())

    for (i in 1..mutations) {
      val x1 = ThreadLocalRandom.current().nextInt(0, maxX)
      val y1 = ThreadLocalRandom.current().nextInt(0, maxY)
      val x2 = ThreadLocalRandom.current().nextInt(0, maxX)
      val y2 = ThreadLocalRandom.current().nextInt(0, maxY)
      val item = layout[x1][y1]
      layout[x1][y1] = layout[x2][y2]
      layout[x2][y2] = item
    }
    findFitness()
  }

  override fun toString(): String {
    return "<Generation: $generation | Fitness: $fitness | Mutations: $mutations>"
  }

  fun getSolution() = Solution(maxX, maxY, this)

  override fun compareTo(other: Factory): Int = when {
      fitness > other.fitness -> 1
      other.fitness > fitness -> -1
      else -> 0
    }
}