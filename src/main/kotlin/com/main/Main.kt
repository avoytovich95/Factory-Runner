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

  val display = Display()

  var queue: BlockingQueue<Solution>? = null
  var exchanger: Exchanger<Factory>? = null
  var phaser: Phaser? = null

  var samples: Array<SampleSet?>? = null
  var controller: Controller? = null
  var stations: Array<Station?>? = null

  var maxX = 0
  var maxY = 0
  var runs = 0
  var stationCount = 0
  var factories = 0
  var threads = 0

  @JvmStatic
  fun main(args: Array<String>) {
    SwingUtilities.invokeLater {
      display.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
      display.runButton.addActionListener(this)

      display.isVisible = true
    }
  }


  override fun actionPerformed(e: ActionEvent?) {
    queue = null
    exchanger = null
    phaser = null

    if (samples != null) {
      samples!!.forEach { i -> i!!.clear() }
      samples!!.fill(null)
    }
    controller = null
    if (stations != null)
      stations!!.fill(null)
    stations = null



    if (display.allFilled()) {
      queue = LinkedBlockingQueue<Solution>()
      exchanger = Exchanger()
      phaser = Phaser()

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
        SampleSet(label++, stations!!.clone(), exchanger, queue, factories, maxX, maxY, runs, phaser)
      }
      Thread(controller).start()
      samples!!.forEach { s -> Thread(s).start() }
    }
  }

}

class Controller(val ui: Display, val queue: BlockingQueue<Solution>?, val phaser: Phaser?, val runs: Int): Runnable{

  var run = 0
  var progress = 0

  override fun run() {
    var bestSolution = Solution(1, 1, 0.0)
    var solution: Solution

    while (phaser!!.registeredParties != 0) {

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
//    ui.runButton.isEnabled = true
    ui.enableAll()
  }

}