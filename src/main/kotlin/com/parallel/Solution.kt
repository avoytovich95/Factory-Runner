package com.parallel

import java.awt.Color
import javax.swing.Box

class Solution(val maxX: Int, val maxY: Int, val fitness: Double) {

  val grid = Array(maxX) { _ -> Array(maxY) { Box.createHorizontalBox() }}
  var threadLabel = 0
  var run = 0

  constructor(x: Int, y: Int, f: Factory): this(x, y, f.fitness) {
    for (xv in 0 until maxX) {
      for (yv in 0 until maxY) {
        val s = f[xv, yv]
        grid[xv][yv].run {
          isOpaque = true
          val color = if (s != null)
            getColor(s.score)
          else
            getColor(-1)
          background = color
        }
      }
    }
  }

  private fun getColor(score: Int): Color {
    return when (score) {
      1 -> Color(201, 2, 2)
      2 -> Color(201, 71, 1)
      3 -> Color(201, 134, 0)
      4 -> Color(201, 164, 0)
      5 -> Color(201, 201, 0)
      6 -> Color(167, 201, 0)
      7 -> Color(137, 201, 0)
      8 -> Color(83, 201, 0)
      9 -> Color(0, 201, 10)
      10 -> Color(0, 165, 33)
      else -> Color.WHITE
    }
  }
}