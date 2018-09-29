package com.parallel

import java.util.concurrent.ThreadLocalRandom

class Station(var name: String, val score: Int) {

  private val affinities = ArrayList<Double>()

  var affinity = 0.0
  var tempAffinity = 0.0

  constructor(name: String): this(name, ThreadLocalRandom.current().nextInt(1, 11))
  constructor(name: Int): this(name.toString())

  fun setAffinity(affinities: ArrayList<Double>) {
    this.affinities.clear()
    affinities.forEach { e -> this.affinities += e }
    affinities.forEach { a -> affinity += a * score}

    affinity /= affinities.size
  }

  fun setTempAffinity(affinities: ArrayList<Double>) {
    affinities.forEach { a -> tempAffinity += a * score }
    tempAffinity /= affinities.size
  }

  private fun setAffinity() {
    affinities.forEach { a -> affinity += a * score }
    affinity /= affinities.size
  }

  override fun toString(): String {
    return "<Name: $name | ID: $score | Affinity: $affinity>"
  }
}