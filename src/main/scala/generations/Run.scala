package generations

import java.io.{Writer, PrintWriter}

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


object Run extends App {

  val model =
    Schelling3C(
      _.setSeed(42),
      _.setNumAgents(360),
      _.setNumBlueAgents(0),
      _.setWorldXSize(20),
      _.setWorldYSize(20),
      _.setFractionRed(0.5),
      _.setMoveMethod(Model.randomMoveMethod),
      _.setThresholdRed(0.7),
      _.setThresholdGreen(0.7),
      _.setRandomMoveProbability(0.0),
      _.setChanceDeath(0.01),
      _.setChanceBirth(0.01),
      _.setChanceMix(1.0)
    )
  
  println(model.getAverageNumNborsGreen)

  model.step()

  println(model.getAverageNumNborsGreen)

}
