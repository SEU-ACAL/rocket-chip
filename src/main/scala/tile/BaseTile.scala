// See LICENSE.SiFive for license details.

package freechips.rocketchip.tile

import chisel3.util.{log2Ceil, log2Up}
import org.chipsalliance.cde.config._
import freechips.rocketchip.rocket._
import freechips.rocketchip.util.BooleanToAugmentedBoolean

case object TileKey extends Field[TileParams]
case object CacheBlockBytes extends Field[Int]
case object MasterPortBeatBytes extends Field[Int]

case class TileParams(
  core: CoreParams,
  icache: Option[ICacheParams],
  dcache: Option[DCacheParams],
  btb: Option[BTBParams])

trait HasTileParameters {
  implicit val p: Parameters
  def tileParams: TileParams = p(TileKey)
  def usingVM = tileParams.core.useVM
  def usingUser = tileParams.core.useUser || usingSupervisor
  def usingSupervisor = tileParams.core.hasSupervisorMode
  def usingHypervisor = usingVM && tileParams.core.useHypervisor
  def usingDebug = tileParams.core.useDebug
  def usingRoCC = false
  def usingBTB = tileParams.btb.exists(_.nEntries > 0)
  def usingPTW = usingVM
  def usingDataScratchpad = tileParams.dcache.flatMap(_.scratch).isDefined
  def xLen = tileParams.core.xLen
  def xBytes = xLen / 8
  def iLen = 32
  def pgIdxBits = 12
  def pgLevelBits = 10 - log2Ceil(xLen / 32)
  def pgLevels = tileParams.core.pgLevels
  def maxSVAddrBits = pgIdxBits + pgLevels * pgLevelBits
  def hypervisorExtraAddrBits = if (usingHypervisor) 2 else 0
  def maxHVAddrBits = maxSVAddrBits + hypervisorExtraAddrBits
  def minPgLevels = xLen match { case 32 => 2; case 64 => 3 }
  def asIdBits = p(ASIdBits)
  def vmIdBits = p(VMIdBits)
  def maxPAddrBits = (xLen, usingVM) match {
    case (_, false) => xLen
    case (32, true) => 34
    case (64, true) => 56
  }
  def paddrBits: Int
  def cacheBlockBytes = p(CacheBlockBytes)
  def lgCacheBlockBytes = log2Up(cacheBlockBytes)
  def masterPortBeatBytes = p(MasterPortBeatBytes)
  def dcacheArbPorts = 1 + usingVM.toInt + usingDataScratchpad.toInt +
    (tileParams.core.useVector && tileParams.core.vectorUseDCache).toInt
  def vaddrBits = if (usingVM) maxHVAddrBits else (paddrBits + 1) min xLen
  def vpnBits = vaddrBits - pgIdxBits
  def ppnBits = paddrBits - pgIdxBits
  def vpnBitsExtended = vpnBits + (if (vaddrBits < xLen) 1 + usingHypervisor.toInt else 0)
  def vaddrBitsExtended = vpnBitsExtended + pgIdxBits
}
