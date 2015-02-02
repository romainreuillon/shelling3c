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

import scala.util.Random

object Statistic {
  import io._

  def variance(s: Seq[Double]) = {
    val avg = s.sum / s.size
    s.map(v => math.pow(v - avg, 2)).sum / s.size
  }

  def median(sequence: Seq[Double]): Double = {
    val sortedSequence = sequence.toArray.filterNot(_.isNaN).sorted
    val size = sortedSequence.size
    if (size == sequence.size)
      if (size % 2 == 0) (sortedSequence(size / 2) + sortedSequence((size / 2) - 1)) / 2
      else sortedSequence((size / 2))
    else Double.NaN
  }

  def sample(s: Seq[Double], samples: Int)(implicit rng: Random) =
    (0 until samples).map(i => s(rng.nextInt(s.size)))

  def bootstrapVariance(data: Seq[Double], samples: Int, bootstrap: Int)(implicit rng: Random) = {
    val medians = for { s <- 0 until bootstrap } yield median(sample(data, samples))
    variance(medians)
  }

}
