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

import fr.iscpif.mgo._
import util.Random._
import generations.Indicator.Fitness
import org.apache.commons.math3.random.Well44497a

import scala.util.Random
import scalax.io.Resource

object CalibrationNGSA2 extends  App {

  val replications = 50

  trait SchellingPB extends GAProblem with MGFitness {

    def min = List.fill(genomeSize)(0.0)
    def max = List.fill(genomeSize)(1.0)

    def genomeSize = 4

    type P = Seq[Double]

    override def express(g: Seq[Double], rng: Random) = {
      val m =
        Schelling3C(
          _.setWorldXSize(20),
          _.setWorldYSize(20),
          _.setNumAgents(360),
          _.setNumBlueAgents(0),
          _.setFractionRed(0.5),
          _.setFractionGreen(0.5),
          _.setMoveMethod(Model.randomMoveMethod),
          _.setRandomMoveProbability(0.0),
          _.setChanceDeath(0.01),
          _.setChanceBirth(0.01),
          _.setThresholdRed(g(0)),
          _.setThresholdGreen(g(1)),
          _.setThresholdBlue(g(2)),
          _.setChanceMix(g(3))
        )(_)

      val f =
        new Fitness {
          def model = m
        }

      val (o1, o2) = (0 until replications).map(_ => f.value(rng)).unzip
      Seq(o1.sum/o1.size, o2.sum/o2.size)
    }

    override def evaluate(p: P, rng: Random) = p

  }



  val m =
    new SchellingPB with NSGAII with CounterTermination {
      def steps = 100
      def mu = 100
      def lambda = 100
    }

  implicit val rng = newRNG(42)

  val res =
    m.evolve.untilConverged {
      s =>
        println(s.generation)
        val output = Resource.fromFile(s"/tmp/scheling/res${s.generation}.csv")
        for {
          r <- s.population.toIndividuals
        } {
          def line = m.scale(m.values.get(r.genome)) ++ m.fitness(r)
          output.append(line.mkString(",") + "\n")
        }
    }





}
