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

import fr.geocites.gugus.calibration._

import scala.util.Random

object EvaluateMarius extends App {

  val model = MariusModel(
    economicMultiplier = 0.2743965501,
    sizeEffectOnSupply = 1.0056140036,
    sizeEffectOnDemand = 1.0734676356,
    distanceDecay = 0.1715067921,
    wealthToPopulationExponent = 0.3520455485,
    populationToWealthExponent = 1.179702831,
    bonusMultiplier = 97.4206661151,
    fixedCost = 0.2257967847,
    ruralMultiplier = 0.018269691

  )

  // not used, marius model is deterministic
  implicit val rng = new Random(42)

  val evaluation = Evaluation(model).multiMicro
  println(evaluation.map(_.formatted("%g")).mkString("\t"))

}
