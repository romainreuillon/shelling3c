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

  def init(model: Model) = {
    val control = new PlainController
    model.setController(control)
    model.setCommandLineArgs(Array.empty[String])
    control.setExitOnExit(false)
    control.setModel(model)
    model.setController(control)
  }

  val model = new BatchModel with NoReport
  init(model)

  model.setSeed(42)
  model.setNumAgents(360)
  model.setNumBlueAgents(0)
  model.setWorldXSize(20)
  model.setWorldYSize(20)
  model.setFractionRed(0.5)
  model.setMoveMethod(Model.randomMoveMethod)
  model.setThresholdRed(0.7)
  model.setThresholdGreen(0.7)
  model.setRandomMoveProbability(0.0)
  model.setChanceDeath(0.01)
  model.setChanceBirth(0.01)
  model.setChanceMix(1.0)

  model.begin()

  println(model.getAverageNumNborsGreen)

  model.step()

  println(model.getAverageNumNborsGreen)

}
