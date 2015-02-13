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

package marius

import cmaes.CMAEvolutionStrategy
import schelling.Indicator.Fitness2
import org.apache.commons.math3.random.{RandomAdaptor, Well44497a}
import fr.geocites.gugus.calibration._

import scala.util.Random

object MariusCalibrationCMAES extends App {


  val sigma = 0.2

  val nbIterMax = 99999

  val fitnessTarget = 0.1

  val cma = new CMAEvolutionStrategy();

  val nbParams = 9

  val min = getLowerBounds
  val max = getUpperBounds

 // val start = getStart

  var initParam = new Array[Double](nbParams)

  cma.options.verbosity = -1;
  cma.setDimension(nbParams);

  for (i <- 0 to nbParams - 1) {
    initParam(i) = Math.random();
   // initParam(i) = 1/Math.PI*Math.acos(1 - 2*(start(i) - min(i)) /(max(i) - min(i))) ;
  }

  cma.setInitialX(initParam);
  cma.setInitialStandardDeviation(sigma);


  var lowerSigma = new Array[Double](nbParams)

  for (i <- 0 to nbParams - 1) {
    lowerSigma(i) = 0.02;
  }

  cma.options.lowerStandardDeviations = lowerSigma;

  var fitness = cma.init();

  var solutionFound = false
  var bestFit = 99999999999.0;
  var itr = 0


  while (solutionFound == false & itr < nbIterMax) {

    var pop = cma.samplePopulation();
    var currentParam = new Array[Double](nbParams)

    for (i <- 0 to pop.length - 1) {

      for (j <- 0 to nbParams - 1) {
        currentParam(j) = min(j) + (max(j) - min(j)) * (1 - Math.cos(pop(i)(j) * Math.PI)) / 2.0;
      }

      var results = computeFitness(currentParam)

      fitness(i) = results(0)

      if(itr ==0){
        bestFit = fitness(i);
      }

      itr += 1

      if(itr % 10 == 0){
        println("itr " + itr)
      }


      if(fitness(i) <= bestFit ){

        bestFit = Math.min(bestFit, fitness(i))
        println("Iter " + itr)
        println("economicMultiplier " + currentParam(0))
        println("sizeEffectOnSupply " + currentParam(1))
        println("sizeEffectOnDemand " + currentParam(2))
        println("distanceDecay " + currentParam(3))
        println("wealthToPopulationExponent " + currentParam(4))
        println("populationToWealthExponent " + currentParam(5))
        println("bonusMultiplier " + currentParam(6))
        println("fixedCost " + currentParam(7))
        println("ruralMultiplier " + currentParam(8))
        println(" ")
        println("deadCities " + results(1))
        println("distance " + results(2))
        println("overflow " + results(3))
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

  def computeFitness(g: Array[Double]): Array[Double] = {
    val m = MariusModel(g(0), g(1), g(2), g(3),g(4),g(5),g(6),g(7),g(8))

    implicit val rng = new Random(42)
    val f = Evaluation(m).multiMicro

    var results = new Array[Double](4)

    for (i <- 0 to 2) {
      if(f(i) == Double.PositiveInfinity){
        f(i) = Double.MaxValue/1000.0
      }
    }


    results(0)  =  f(0) + f(1) + f(2);

    if(f(0) > 0){
      results(0) += 10 + 100*f(0)
    }

    if(f(2) > 0){
      results(0) += 10 + 100*f(2)
    }

    results(1)  = f(0)
    results(2)  = f(1)
    results(3)  = f(2)

    return results
  }

  def getLowerBounds(): Array[Double] = {


    val lowerbounds = new Array[Double](9)

    lowerbounds(0)  = 0  //economicMultiplier
    lowerbounds(1)  = 1  //sizeEffectOnSupply
    lowerbounds(2)  = 1  //sizeEffectOnDemand
    lowerbounds(3)  = 0  //distanceDecay
    lowerbounds(4)  = 0  //wealthToPopulationExponent
    lowerbounds(5)  = 1  //populationToWealthExponent
    lowerbounds(6)  = 0  //bonusMultiplier
    lowerbounds(7)  = 0  //fixedCost
    lowerbounds(8)  = 0  //ruralMultiplier

    return lowerbounds
  }


  def getUpperBounds(): Array[Double] = {

    val upperbounds = new Array[Double](9)

    upperbounds(0)  = 1.0  //economicMultiplier
    upperbounds(1)  = 10.0  //sizeEffectOnSupply
    upperbounds(2)  = 10.0  //sizeEffectOnDemand
    upperbounds(3)  = 10.0  //distanceDecay
    upperbounds(4)  = 10.0  //wealthToPopulationExponent
    upperbounds(5)  = 10.0  //populationToWealthExponent
    upperbounds(6)  = 100.0  //bonusMultiplier
    upperbounds(7)  = 100.0  //fixedCost
    upperbounds(8)  = 1.0  //ruralMultiplier

    return upperbounds
  }


//  def getStart(): Array[Double] = {
//
//    val start = new Array[Double](9)
//
//    start(0)  = 0.07599265854680215  //economicMultiplier
//    start(1)  = 1.0000050748418732  //sizeEffectOnSupply
//    start(2)  = 1.000580821648009  //sizeEffectOnDemand
//    start(3)  = 0.05086890777395714 //distanceDecay
//    start(4)  = 0.28261666534369345  //wealthToPopulationExponent
//    start(5)  = 1.0671710538416619  //populationToWealthExponent
//    start(6)  = 91.45476572250138  //bonusMultiplier
//    start(7)  = 53.37075629707178 //fixedCost
//    start(8)  = 0.10534550468739817 //ruralMultiplier
//
//    return start
//  }





}
