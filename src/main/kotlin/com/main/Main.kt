package com.main

import com.parallel.Factory
import com.parallel.SampleSet
import com.parallel.Solution
import com.parallel.Station
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.lang.IllegalStateException
import java.util.concurrent.*
import javax.swing.JFrame
import javax.swing.SwingUtilities

object Main: ActionListener {

  private val display = Display()

  private var queue: BlockingQueue<Solution> = LinkedBlockingQueue<Solution>()
  private var exchanger = Exchanger<Factory>()
  private var phaser = Phaser()

  private var stations: Array<Station>? = null
  private var controller: Controller? = null
  private var samples: Array<SampleSet>? = null

  private var maxX = 0
  private var maxY = 0
  private var runs = 0
  private var stationCount = 0
  private var factories = 0
  private var threads = 0

  @JvmStatic
  fun main(args: Array<String>) {
    SwingUtilities.invokeLater {
      display.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
      display.runButton.addActionListener(this)

      display.isVisible = true
    }
  }


  override fun actionPerformed(e: ActionEvent?) {
    stations = null
    controller = null
    samples = null

    if (display.allFilled()) {
      maxX = display.xText.text.toInt()
      maxY = display.yText.text.toInt()
      runs = display.runText.text.toInt()
      stationCount = display.stationsText.text.toInt()
      factories = display.factoriesText.text.toInt()
      threads = display.threadsText.text.toInt()

      display.setGrid(maxX, maxY)

      display.disableAll()

      var name = 0
      var label = 0

      stations = Array(stationCount) { Station(name++) }
      controller = Controller(display, queue, phaser, runs)

      samples = Array(threads) {
        SampleSet(label++, stations!!.copyOf(), exchanger, queue, factories, maxX, maxY, runs, phaser)
      }
      Thread(controller).start()
      samples!!.forEach { s -> Thread(s).start() }
    }
  }

}

class Controller(
    private val ui: Display,
    private val queue: BlockingQueue<Solution>?,
    private val phaser: Phaser,
    private val runs: Int
): Runnable{

  private var run = 0
  private var progress = 0

  override fun run() {
    var bestSolution = Solution(1, 1, 0.0)
    var solution: Solution

    while (phaser.registeredParties != 0) {

      try {
        solution = queue!!.poll(500, TimeUnit.MILLISECONDS)
      } catch (t: IllegalStateException) {
        break
      }

      if (run < solution.run) {
        run = solution.run
        progress = Math.floor((run.toDouble() / runs.toDouble()) * 100).toInt()
        ui.updateProgress(progress)
      }

      if (bestSolution.fitness < solution.fitness) {
        bestSolution = solution
        ui.updateGrid(bestSolution)
      }

    }

    ui.updateProgress(0)
    ui.enableAll()
  }

}