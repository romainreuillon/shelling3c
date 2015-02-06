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

import fr.geocites.gugus.balance._
import fr.geocites.gugus.transaction._
import fr.geocites.gugus.urbanisation._
import fr.geocites.marius._

case class MariusModel(
  economicMultiplier: Double,
  sizeEffectOnSupply: Double,
  sizeEffectOnDemand: Double,
  distanceDecay: Double,
  wealthToPopulationExponent: Double,
  populationToWealthExponent: Double,
  bonusMultiplier: Double,
  fixedCost: Double,
  ruralMultiplier: Double) extends Marius with Bonus with FixedCostTransaction with UrbanTransition with From1959To1989
