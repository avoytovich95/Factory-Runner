package com.main

import com.parallel.Factory
import com.parallel.Solution
import com.parallel.Station
import java.awt.*
import javax.swing.*
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.locks.ReentrantLock


class Display: JFrame() {

  var maxX = 0
  var maxY = 0

  val xText = JTextField()
  val yText = JTextField()
  val stationsText = JTextField()
  val factoriesText = JTextField()
  val threadsText = JTextField()
  val runText = JTextField()

  val runButton = JButton("Run Algorithm")

  val gridPanel = JPanel()
  val progress = JProgressBar()
  val lock = ReentrantLock()

  init {
    setSize(WIDTH, HEIGHT)
    layout = GridBagLayout()
    componentOrientation = ComponentOrientation.RIGHT_TO_LEFT
    createForm()
  }

  private fun createForm() {
    val c = GridBagConstraints()
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 0.1
    c.weighty = 0.1
    c.gridx = 0
    c.gridy = 1
    add(JLabel(" X"), c)

    c.gridy = 2
    add(JLabel(" Y"), c)

    c.gridy = 3
    add(JLabel(" Stations"), c)

    c.gridy = 4
    add(JLabel(" Factories"), c)

    c.gridy = 5
    add(JLabel(" Threads"), c)

    c.gridy = 6
    add(JLabel(" Runs"), c)

    c.weightx = 0.3
    c.gridx = 1
    c.gridy = 1
    add(xText, c)

    c.gridy = 2
    add(yText, c)

    c.gridy = 3
    add(stationsText, c)

    c.gridy = 4
    add(factoriesText, c)

    c.gridy = 5
    add(threadsText, c)

    c.gridy = 6
    add(runText, c)

    c.gridwidth = 2
    c.gridx = 0
    c.gridy = 7
    add(runButton, c)

    createGrid(10, 10)

    c.gridwidth = 3
    c.gridx = 0
    c.gridy = 8
    c.weightx = 4.0
    add(progress, c)

    c.gridwidth = 3
    c.gridx = 0
    c.gridy = 0
    c.fill = GridBagConstraints.CENTER
    val title = JLabel("Factory Runner")
    title.font = Font("Consolas", Font.BOLD, 18)
    add(title, c)
  }

  private fun createGrid(x: Int, y: Int) {
    gridPanel.layout = GridLayout(x, y)
    gridPanel.setSize(600, 600)
    for (xv in 0 until x) {
      for (yv in 0 until y) {
        val box = Box.createHorizontalBox()
        box.isOpaque = true
        box.background = Color(
            ThreadLocalRandom.current().nextInt(1, 256),
            ThreadLocalRandom.current().nextInt(1, 256),
            ThreadLocalRandom.current().nextInt(1, 256)
        )
        gridPanel.add(box)
      }
    }

    val c = GridBagConstraints()
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 3.0
    c.gridwidth = 1
    c.gridheight = 7
    c.ipady = 500
    c.gridy = 1
    c.gridx = 2
    add(gridPanel, c)
  }

  fun setGrid(xMax: Int, yMax: Int) {
    SwingUtilities.invokeLater {
      progress.value = 0
      gridPanel.removeAll()
      maxX = xMax
      maxY = yMax
      gridPanel.layout = GridLayout(xMax, yMax)

      for (xv in 0 until xMax) {
        for (yv in 0 until yMax) {
          Box.createHorizontalBox().run {
            isOpaque = true
            background = Color.WHITE
            gridPanel.add(this)
          }
        }
      }
      gridPanel.updateUI()
      repaint()
    }
  }

  fun updateGrid(solution: Solution) {
    SwingUtilities.invokeLater {
      lock.lock()
      try {
        gridPanel.removeAll()
        for (y in maxY - 1 downTo 0) {
          for (x in 0 until maxX) {
            gridPanel.add(solution.grid[x][y])
            solution.grid[x][y].background
          }
        }
        gridPanel.updateUI()
        repaint()
      } finally {
        lock.unlock()
      }
    }
  }

  fun updateProgress(progress: Int) {
    SwingUtilities.invokeLater {
      lock.lock()
      try {
        this.progress.value = progress
        repaint()
      } finally {
        lock.unlock()
      }
    }
  }

  fun disableAll() {
    xText.isEnabled = false
    yText.isEnabled = false
    stationsText.isEnabled = false
    factoriesText.isEnabled = false
    threadsText.isEnabled = false
    runText.isEnabled = false
    runButton.isEnabled = false
  }

  fun enableAll() {
    lock.lock()
    SwingUtilities.invokeLater {
      xText.isEnabled = true
      yText.isEnabled = true
      stationsText.isEnabled = true
      factoriesText.isEnabled = true
      threadsText.isEnabled = true
      runText.isEnabled = true
      runButton.isEnabled = true
      repaint()
    }
    lock.unlock()
  }

  fun allFilled(): Boolean =
      xText.text != "" && yText.text != "" && stationsText.text != "" &&
          factoriesText.text != "" && threadsText.text != "" &&
          runText.text != ""

  companion object {
    private const val WIDTH = 800
    private const val HEIGHT = 600
  }
}