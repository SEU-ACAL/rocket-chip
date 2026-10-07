// See LICENSE.Berkeley and LICENSE.SiFive for license details.

package freechips.rocketchip.rocket

import chisel3._
import chisel3.util._
import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.tile.CoreBundle

class FrontendReq(implicit p: Parameters) extends CoreBundle()(p) {
  val pc = UInt(vaddrBitsExtended.W)
  val speculative = Bool()
}

class FrontendExceptions extends Bundle {
  val pf = new Bundle {
    val inst = Bool()
  }
  val gf = new Bundle {
    val inst = Bool()
  }
  val ae = new Bundle {
    val inst = Bool()
  }
}

class FrontendResp(implicit p: Parameters) extends CoreBundle()(p) {
  val btb = new BTBResp
  val pc = UInt(vaddrBitsExtended.W)  // ID stage PC
  val data = UInt((fetchWidth * coreInstBits).W)
  val mask = Bits(fetchWidth.W)
  val xcpt = new FrontendExceptions
  val replay = Bool()
}

class FrontendPerfEvents extends Bundle {
  val acquire = Bool()
  val tlbMiss = Bool()
}

class FrontendIO(implicit p: Parameters) extends CoreBundle()(p) {
  val might_request = Output(Bool())
  val clock_enabled = Input(Bool())
  val req = Valid(new FrontendReq)
  val sfence = Valid(new SFenceReq)
  val resp = Flipped(Decoupled(new FrontendResp))
  val gpa = Flipped(Valid(UInt(vaddrBitsExtended.W)))
  val gpa_is_pte = Input(Bool())
  val btb_update = Valid(new BTBUpdate)
  val bht_update = Valid(new BHTUpdate)
  val ras_update = Valid(new RASUpdate)
  val flush_icache = Output(Bool())
  val npc = Input(UInt(vaddrBitsExtended.W))
  val perf = Input(new FrontendPerfEvents())
  val progress = Output(Bool())
}
