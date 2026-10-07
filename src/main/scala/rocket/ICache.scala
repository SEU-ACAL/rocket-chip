// See LICENSE.Berkeley and LICENSE.SiFive for license details.

package freechips.rocketchip.rocket

import freechips.rocketchip.tile.L1CacheParams
import freechips.rocketchip.util.{Code, RandomReplacement}

case class ICacheParams(
    nSets: Int = 64,
    nWays: Int = 4,
    rowBits: Int = 128,
    nTLBSets: Int = 1,
    nTLBWays: Int = 32,
    nTLBBasePageSectors: Int = 4,
    nTLBSuperpages: Int = 4,
    cacheIdBits: Int = 0,
    tagECC: Option[String] = None,
    dataECC: Option[String] = None,
    itimAddr: Option[BigInt] = None,
    prefetch: Boolean = false,
    blockBytes: Int = 64,
    latency: Int = 2,
    fetchBytes: Int = 4) extends L1CacheParams {
  def tagCode: Code = Code.fromString(tagECC)
  def dataCode: Code = Code.fromString(dataECC)
  def replacement = new RandomReplacement(nWays)
}
