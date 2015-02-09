/*
 * Copyright (C) 2015 Romain Reuillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package generations

import cmaes.CMAEvolutionStrategy
import org.apache.commons.math3.random.{ Well44497a, RandomAdaptor }
import scala.util.Random
import generations.Indicator.{Fitness2, Fitness}
import Statistic._

object CalibrationCMAES extends App {

  val replications = 50

  val sigma = 0.5

  val nbIterMax = 99999

  val fitnessTarget = 0.1

  val cma = new CMAEvolutionStrategy();

  val nbParams = 4

  def min = List.fill(nbParams)(0.0)
  def max = List.fill(nbParams)(1.0)

  var initParam = new Array[Double](nbParams)

  cma.options.verbosity = -1;
  cma.setDimension(nbParams);

  for (i <- 0 to nbParams - 1) {
    initParam(i) = Math.random();
  }

  cma.setInitialX(initParam);
  cma.setInitialStandardDeviation(sigma);

  var fitness = cma.init();

  var solutionFound = false
  var bestFit = 99999.0;
  var itr = 0

  implicit val rng: Random = new RandomAdaptor(new Well44497a(1))

  while (solutionFound == false & itr < nbIterMax) {

    var pop = cma.samplePopulation();
    var currentParam = new Array[Double](nbParams)

    for (i <- 0 to pop.length - 1) {

      for (j <- 0 to nbParams - 1) {
        currentParam(j) = min(j) + (max(j) - min(j)) * (1 - Math.cos(pop(i)(j) * Math.PI)) / 2.0;
      }

      var results = computeReplication(currentParam, rng)

      fitness(i) = results(0)

      itr += 1

      if(itr % 10 == 0){
        println("itr " + itr)
      }
      if(fitness(i) < bestFit ){
        bestFit = Math.min(bestFit, fitness(i))
        println("Iter " + itr)
        println("thresholdRed " + currentParam(0))
        println("thresholdGreen " + currentParam(1))
        println("thresholdBlue " + currentParam(2))
        println("chanceMix " + currentParam(3))
        println(" ")
        println("medianFitSwitch " + results(1))
        println("medianFitFractionBlue " + results(2))
        println(" ")
        println("bestFit " + bestFit)
        println(" ")
      }

      if (fitness(i) < fitnessTarget) {
        solutionFound = true
      }
    }

    cma.updateDistribution(fitness);
  }

  def computeReplication(g: Array[Double], rng: Random): Array[Double] = {
    val m = Schelling3C(g(0), g(1), g(2), g(3))

    val f =
      new Fitness2 {
        def model = m
      }

    var ArrayFitness =new Array[Double](replications)
    var ArrayFitSwitch =new Array[Double](replications)
    var ArrayfitFractionBlue =new Array[Double](replications)

    for (i <- 0 to replications-1) {
      var (o1,o2) = f.value(rng)
      ArrayFitness(i) = Math.pow(o1,2) + Math.pow(o2,2)
      ArrayFitSwitch(i) = o1
      ArrayfitFractionBlue(i) = o2
    }

    var medianFit = median(ArrayFitness.toSeq)
    var medianFitSwitch = median(ArrayFitSwitch.toSeq)
    var medianFitFractionBlue= median(ArrayfitFractionBlue.toSeq)

    var results = new Array[Double](3)
    results(0)  = medianFit
    results(1)  = medianFitSwitch
    results(2)  = medianFitFractionBlue

    return results
  }

}
