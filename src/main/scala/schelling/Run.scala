package schelling

import java.io.{ Writer, PrintWriter }

import scala.util.Random

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

import Indicator._

object Run extends App {

  val m =
    Schelling3C(
      _.setNumAgents(60),
      _.setNumBlueAgents(10),
      _.setWorldXSize(10),
      _.setWorldYSize(10),
      _.setFractionRed(0.5),
      _.setMoveMethod(Model.randomMoveMethod),
      _.setThresholdRed(1),
      _.setThresholdGreen(0),
      _.setThresholdBlue(0),
      _.setRandomMoveProbability(0.0),
      _.setChanceDeath(0.01),
      _.setChanceBirth(0.01),
      _.setChanceMix(0.15)
    )(_)

  val fitness = new Fitness2 {
    override def model = m
  }

  println(fitness.value(new Random(42)))

}
