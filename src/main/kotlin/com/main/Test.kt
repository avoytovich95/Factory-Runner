//package com.main
//
//import com.parallel.Factory
//import com.parallel.SampleSet
//import com.parallel.Solution
//import com.parallel.Station
//import java.util.concurrent.*
//
//fun main(args: Array<String>) {
//  val maxX = 10
//  val maxY = 10
//  val populations = 64
//  val runs = 10000
//  val threads = 8
//
//  val name = 0
//  val stations = Array(15) { Station(name) }
//
//  val myExchanger = Exchanger<Factory>()
//  val myQueue: BlockingQueue<Solution> = LinkedBlockingQueue<Solution>()
//  val phaser = Phaser()
//
//  var label = 0
//  val sampels = Array(threads) {
//    SampleSet(label++, stations, myExchanger, myQueue, populations, maxX, maxY, runs, phaser)
//  }
//
//  val thread = ThreadRunner(myQueue, phaser)
//
//  thread.start()
//  sampels.forEach { s -> Thread(s).start() }
//}
//
//class ThreadRunner(val queue: BlockingQueue<Solution>, val phaser: Phaser): Thread(){
//
//  var bestSolution = Solution(1, 1, 0.0)
//
//  init {
//    phaser.register()
//  }
//
//  override fun run() {
//    val map = HashMap<Int, Solution>()
//
//    while (phaser.registeredParties > 1) {
//
//
//      val solution = queue.poll(2, TimeUnit.SECONDS)
//
//      if (solution.threadLabel !in map)
//        map[solution.threadLabel] = solution
//      else if (map[solution.threadLabel]!!.fitness < solution.fitness)
//        map[solution.threadLabel] = solution
//
//      if (solution.fitness > bestSolution.fitness) {
//        bestSolution = solution
//        println("New solution found!!!!")
//        println("${bestSolution.threadLabel} ::== ${bestSolution.fitness}")
//      }
//
////      println("Solution from ${solution.threadLabel} ::== ${solution.fitness}")
//    }
//    println("Finished")
//    map.forEach { (k, v) -> println("Thread $k: ${v.fitness}") }
//
//    println("Best Solution: ${bestSolution.fitness}")
//
//    phaser.arriveAndDeregister()
//  }
//}
