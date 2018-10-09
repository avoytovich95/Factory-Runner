package com.parallel

import java.util.concurrent.*
import kotlin.collections.ArrayList

class SampleSet(
    private val threadLabel: Int,
    private val stations: Array<Station>,
    private val swapper: Exchanger<Factory>,
    private val queue: BlockingQueue<Solution>,
    private val setTotal: Int,
    private val maxX: Int,
    private val maxY: Int,
    private val runs: Int,
    private val phaser: Phaser?
): Runnable {

  private val factories = ArrayList<Factory>()

  init {
    for (i in 0 until setTotal)
      factories += Factory(maxX, maxY, stations.clone())
    phaser!!.register()
  }

  override fun run() {
    for (i in 1..runs) {
      breed()
      while (factories.size != setTotal * 2) {
        factories += Factory(maxX, maxY, stations)
      }
      try {
        factories += swapper.exchange(
            factories.removeAt(ThreadLocalRandom.current().nextInt(0, factories.size)),
            1,
            TimeUnit.SECONDS
        )
      } catch (e: TimeoutException) { }
      trim()


      factories.sort()
      val solution = factories.last().getSolution()
      solution.threadLabel = this.threadLabel
      solution.run = i
      queue.put(solution)

    }

    phaser!!.arriveAndDeregister()
  }

  private fun breed() {
    val nextGen = ArrayList<Factory>()
    factories.shuffle()

    for (s in 0 until setTotal step 2) {
      if (s == setTotal - 1)
        break
      factories += factories[s] + factories[s + 1]
    }
    factories += nextGen
  }

  private fun trim() {
    factories.sort()
    var index = 0
    while (factories.size != setTotal) {
      factories.removeAt(index++)
    }
  }
}