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
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package schelling

import fr.iscpif.scalabc._
import fr.iscpif.scalabc.distance._
import fr.iscpif.scalabc.prior._
import fr.iscpif.scalabc.sampling._
import schelling.Indicator.Observable
import fr.iscpif.mgo.newRNG
import scala.util.Random

object ABC extends App {

  val abc = new LenormandExecuter with JabotMover with DefaultDistance {
    def summaryStatsTarget = Seq(Indicator.targetSwitch, Indicator.targetFractionBlue)

    def simulations = 100

    def priors = Seq(
      Uniform(0.0, 1.0),
      Uniform(0.0, 1.0),
      Uniform(0.0, 1.0),
      Uniform(0.0, 1.0)
    )

    def clamp(d: Double) = if (d < 0.0) 0.0 else if (d > 1.0) 1.0 else d

    override def model(input: Seq[Double], seed: Long): Seq[Double] = {
      val i = input.map(clamp)
      val m = Schelling3C(i(0), i(1), i(2), i(3))
      val o =
        new Observable {
          def model: (Random) => Model = m
        }
      val (switch, fraction) = o.observables(newRNG(seed))
      Seq(switch, fraction)
    }
  }

  for {
    s <- abc.run(newRNG(42))
  } println(s.accepted)

}
