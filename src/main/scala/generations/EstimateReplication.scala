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

import generations.Indicator.Fitness
import org.apache.commons.math3.random.{ RandomAdaptor, Well44497a }
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }
import scala.util.Random
import scalax.io.Resource
import util.Random._
import Statistic._
import scala.concurrent.ExecutionContext.Implicits.global

object EstimateReplication extends App {

  val replications = 10000

  case class Point(
    thresholdRed: Double,
    thresholdGreen: Double,
    thresholdBlue: Double,
    chanceMix: Double)

  val points = Seq(Point(0.5, 0.5, 0.5, 0.5), Point(0.1, 0.1, 0.1, 0.1), Point(0.9, 0.9, 0.9, 0.9))

  def computeReplications(point: Point)(implicit rng: Random) =
    for {
      r <- (0 until replications)
      seed = rng.nextLong()
    } yield Future {
      val Point(tr, tg, tb, cm) = point
      val rng = new RandomAdaptor(new Well44497a(seed))
      val fitness = new Fitness {
        def model = Schelling3C(tr, tg, tb, cm)
      }
      fitness.value(rng)
    }

  for {
    (point, seed) <- points.zipWithIndex
  } {
    implicit val rng: Random = new RandomAdaptor(new Well44497a(seed))
    val outputFile = Resource.fromFile(s"/tmp/replications/${point.productIterator.mkString("_")}.csv")
    val (o1, o2) = computeReplications(point).map(Await.result(_, Duration.Inf)).unzip
    for {
      size <- 1 to 500
      v1 = bootstrapVariance(o1, size, 100)
      v2 = bootstrapVariance(o2, size, 100)
    } outputFile.append(s"$size,$v1, $v2\n")
  }

}
