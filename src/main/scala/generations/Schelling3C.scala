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

import net.sourceforge.openforecast.models.DoubleExponentialSmoothingModel

import scala.util.Random
import scala.util.Random._

object Schelling3C {

  def apply(setup: (Model => Any)*)(rng: Random) = {

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

    model.schedule = null
    setup.foreach(_(model))
    model.setRandom(rng.self)
    model.buildSchedule()
    model.begin()
    model
  }

  def apply(
    thresholdRed: Double,
    thresholdGreen: Double,
    thresholdBlue: Double,
    chanceMix: Double): Random => Model =
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
      _.setThresholdRed(thresholdRed),
      _.setThresholdGreen(thresholdGreen),
      _.setThresholdBlue(thresholdBlue),
      _.setChanceMix(chanceMix)
    )(_)

}
