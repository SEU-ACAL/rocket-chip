// See LICENSE.Berkeley and LICENSE.SiFive for license details.

package freechips.rocketchip.rocket

import chisel3._
import chisel3.util._
import org.chipsalliance.cde.config._
import freechips.rocketchip.tile.CoreBundle

case object ASIdBits extends Field[Int](0)
case object VMIdBits extends Field[Int](0)

/** =SFENCE=
  * rs1 rs2
  * {{{
  *  0   0 -> flush All
  *  0   1 -> flush by ASID
  *  1   1 -> flush by ADDR
  *  1   0 -> flush by ADDR and ASID
  * }}}
  * {{{
  * If rs1=x0 and rs2=x0, the fence orders all reads and writes made to any level of the page tables, for all address spaces.
  * If rs1=x0 and rs2!=x0, the fence orders all reads and writes made to any level of the page tables, but only for the address space identified by integer register rs2. Accesses to global mappings (see Section 4.3.1) are not ordered.
  * If rs1!=x0 and rs2=x0, the fence orders only reads and writes made to the leaf page table entry corresponding to the virtual address in rs1, for all address spaces.
  * If rs1!=x0 and rs2!=x0, the fence orders only reads and writes made to the leaf page table entry corresponding to the virtual address in rs1, for the address space identified by integer register rs2. Accesses to global mappings are not ordered.
  * }}}
  */
class SFenceReq(implicit p: Parameters) extends CoreBundle()(p) {
  val rs1 = Bool()
  val rs2 = Bool()
  val addr = UInt(vaddrBits.W)
  val asid = UInt((asIdBits max 1).W) // TODO zero-width
  val hv = Bool()
  val hg = Bool()
}

class TLBReq(lgMaxSize: Int)(implicit p: Parameters) extends CoreBundle()(p) {
  /** request address from CPU. */
  val vaddr = UInt(vaddrBitsExtended.W)
  /** don't lookup TLB, bypass vaddr as paddr */
  val passthrough = Bool()
  /** granularity */
  val size = UInt(log2Ceil(lgMaxSize + 1).W)
  /** memory command. */
  val cmd  = Bits(M_SZ.W)
  val prv = UInt(PRV.SZ.W)
  /** virtualization mode */
  val v = Bool()

}

class TLBExceptions extends Bundle {
  val ld = Bool()
  val st = Bool()
  val inst = Bool()
}

class TLBResp(lgMaxSize: Int = 3)(implicit p: Parameters) extends CoreBundle()(p) {
  // lookup responses
  val miss = Bool()
  /** physical address */
  val paddr = UInt(paddrBits.W)
  val gpa = UInt(vaddrBitsExtended.W)
  val gpa_is_pte = Bool()
  /** page fault exception */
  val pf = new TLBExceptions
  /** guest page fault exception */
  val gf = new TLBExceptions
  /** access exception */
  val ae = new TLBExceptions
  /** misaligned access exception */
  val ma = new TLBExceptions
  /** if this address is cacheable */
  val cacheable = Bool()
  /** if caches must allocate this address */
  val must_alloc = Bool()
  /** if this address is prefetchable for caches*/
  val prefetchable = Bool()
  /** size/cmd of request that generated this response*/
  val size = UInt(log2Ceil(lgMaxSize + 1).W)
  val cmd = UInt(M_SZ.W)

}
