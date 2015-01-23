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

import uchicago.src.sim.space.Object2DGrid
import collection.JavaConversions._
import math._
import scala.util.Random

object Indicator {

  implicit class Object2DGridToArrayOfType(grid: Object2DGrid) {
    def toTorus = {
      def sizeX = grid.getSizeX
      def sizeY = grid.getSizeY
      def matrix =
        for {x <- 0 until sizeX}
        yield for {y <- 0 until sizeY}
        yield {
          grid.getObjectAt(x, y) match {
            case null => -1
            case x => x.asInstanceOf[Agent].getType
          }
        }

      new Torus[Int](matrix.map(_.toArray).toArray, sizeX, sizeY)
    }
  }

  implicit class PositiveModulo(i: Int) {
    def positiveModulo(size: Int) = {
      val mod = i % size
      if(mod < 0) mod + size else mod
    }
  }

  class Torus[T](val array: Array[Array[T]], val sizeX: Int, val sizeY: Int) {
    def apply(x: Int, y: Int) = array(x.positiveModulo(sizeX))(y.positiveModulo(sizeY))
  }


  def averageSwitchRate(world: Torus[Int]) = {
    val switchRates =
      for {
        x <- 0 until world.sizeX
        y <- 0 until world.sizeY
      } yield switchRate(world, x, y).toDouble
    switchRates.sum / switchRates.size
  }

  def switchRate(world: Torus[Int], x: Int, y: Int) = {
    val offsets = List((-1, -1), (0, -1), (1, -1), (1, 0), (1, 1), (0, 1), (-1, 1), (-1, 0))

    def nbSwitch(previous: Int, o: List[(Int, Int)]): Int =
      o match {
        case Nil => 0
        case (ox, oy) :: t =>
          (previous, world(x + ox, y + oy)) match {
            case (-1, current) => nbSwitch(current, t)
            case (_, -1) => nbSwitch(previous, t)
            case (_, current) =>
              val switch = if (previous == current) 0 else 1
              switch + nbSwitch(current, t)
          }
      }

    val (lastX, lastY) = offsets.last
    nbSwitch(world(x + lastX,  y + lastY), offsets)
  }

  def unsatisfied(model: Model) =
    model.getAgentList.map(_.asInstanceOf[Agent]).count(a => a.getFractionNborsSame <= a.getThreshold) / model.getAgentList.size


  trait Fitness {
    def model: Random => Model
    def maxUnsatisfied: Double = 0.8
    def minSwitch: Double = 5.0
    def warming = 100
    def range = 50

    def value(rng: Random) = {
      val m = model(rng)
      (0 until warming).foreach(_ => m.step)
      val (totalSwitch, totalUnsatisfied) =
        (0 until range).foldLeft((0.0, 0.0)) {
          case ((totalSwitch, totalUnsatisfied), _) =>
            val v = (averageSwitchRate(m.getWorld.toTorus), unsatisfied(m))
            m.step
            (totalSwitch + v._1, totalUnsatisfied + v._2)
        }
      val (normalisedSwitch, normalisedUnsatisfied) = (totalSwitch.toDouble / range, totalUnsatisfied.toDouble / range)
      val diffSwitch = (minSwitch - normalisedSwitch) / minSwitch
      val diffUnsatisfied = (normalisedUnsatisfied - maxUnsatisfied) / maxUnsatisfied
      //(max(0.0, diffSwitch), max(0.0, diffUnsatisfied))
      (abs(diffSwitch), abs(diffUnsatisfied))
    }
  }


}
