/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *   Mupen64plus - gregimm.c                                               *
 *   Mupen64Plus homepage: http://code.google.com/p/mupen64plus/           *
 *   Copyright (C) 2007 Richard Goedeken (Richard42)                       *
 *   Copyright (C) 2002 Hacktarux                                          *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.          *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

#include <stdio.h>

#include "assemble.h"
#include "interpret.h"

#include "../recomph.h"
#include "../recomp.h"
#include "../r4300.h"
#include "../ops.h"
#include "../macros.h"

#include "../../memory/memory.h"

void genbltz_test(void)
{
  int rs_64bit = is64((unsigned int *)dst->f.i.rs);
   
  if (rs_64bit == 0)
  {
    int rs = allocate_register_32((unsigned int *)dst->f.i.rs);
    
    cmp_reg32_imm32(rs, 0);
    setl_m8abs((unsigned char *) &branch_taken);
  }
  else if (rs_64bit == -1)
  {
    cmp_m32abs_imm32(((unsigned int *)dst->f.i.rs)+1, 0);
    setl_m8abs((unsigned char *) &branch_taken);
  }
  else
  {
    int rs = allocate_register_64((unsigned long long *)dst->f.i.rs);

    cmp_reg64_imm8(rs, 0);
    setl_m8abs((unsigned char *) &branch_taken);
  }
}

void genbltz(void)
{
#if defined(COUNT_INSTR)
   inc_m32abs(&instr_count[47]);
#endif
#ifdef INTERPRET_BLTZ
   gencallinterp((unsigned long long)BLTZ, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BLTZ, 1);
    return;
     }
   
   genbltz_test();
   gendelayslot();
   gentest();
#endif
}

void genbltz_out(void)
{
#if defined(COUNT_INSTR)
   inc_m32abs(&instr_count[47]);
#endif
#ifdef INTERPRET_BLTZ_OUT
   gencallinterp((unsigned long long)BLTZ_OUT, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BLTZ_OUT, 1);
    return;
     }
   
   genbltz_test();
   gendelayslot();
   gentest_out();
#endif
}

void genbltz_idle(void)
{
#ifdef INTERPRET_BLTZ_IDLE
   gencallinterp((unsigned long long)BLTZ_IDLE, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BLTZ_IDLE, 1);
    return;
     }
   
   genbltz_test();
   gentest_idle();
   genbltz();
#endif
}

void genbgez_test(void)
{
  int rs_64bit = is64((unsigned int *)dst->f.i.rs);
   
  if (rs_64bit == 0)
  {
    int rs = allocate_register_32((unsigned int *)dst->f.i.rs);
    cmp_reg32_imm32(rs, 0);
    setge_m8abs((unsigned char *) &branch_taken);
  }
  else if (rs_64bit == -1)
  {
    cmp_m32abs_imm32(((unsigned int *)dst->f.i.rs)+1, 0);
    setge_m8abs((unsigned char *) &branch_taken);
  }
  else
  {
    int rs = allocate_register_64((unsigned long long *)dst->f.i.rs);
    cmp_reg64_imm8(rs, 0);
    setge_m8abs((unsigned char *) &branch_taken);
  }
}

void genbgez(void)
{
#if defined(COUNT_INSTR)
   inc_m32abs(&instr_count[48]);
#endif
#ifdef INTERPRET_BGEZ
   gencallinterp((unsigned long long)BGEZ, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BGEZ, 1);
    return;
     }
   
   genbgez_test();
   gendelayslot();
   gentest();
#endif
}

void genbgez_out(void)
{
#if defined(COUNT_INSTR)
   inc_m32abs(&instr_count[48]);
#endif
#ifdef INTERPRET_BGEZ_OUT
   gencallinterp((unsigned long long)BGEZ_OUT, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BGEZ_OUT, 1);
    return;
     }
   
   genbgez_test();
   gendelayslot();
   gentest_out();
#endif
}

void genbgez_idle(void)
{
#ifdef INTERPRET_BGEZ_IDLE
   gencallinterp((unsigned long long)BGEZ_IDLE, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BGEZ_IDLE, 1);
    return;
     }
   
   genbgez_test();
   gentest_idle();
   genbgez();
#endif
}

void genbltzl(void)
{
#if defined(COUNT_INSTR)
   inc_m32abs(&instr_count[49]);
#endif
#ifdef INTERPRET_BLTZL
   gencallinterp((unsigned long long)BLTZL, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BLTZL, 1);
    return;
     }
   
   genbltz_test();
   free_all_registers();
   gentestl();
#endif
}

void genbltzl_out(void)
{
#if defined(COUNT_INSTR)
   inc_m32abs(&instr_count[49]);
#endif
#ifdef INTERPRET_BLTZL_OUT
   gencallinterp((unsigned long long)BLTZL_OUT, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BLTZL_OUT, 1);
    return;
     }
   
   genbltz_test();
   free_all_registers();
   gentestl_out();
#endif
}

void genbltzl_idle(void)
{
#ifdef INTERPRET_BLTZL_IDLE
   gencallinterp((unsigned long long)BLTZL_IDLE, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BLTZL_IDLE, 1);
    return;
     }
   
   genbltz_test();
   gentest_idle();
   genbltzl();
#endif
}

void genbgezl(void)
{
#if defined(COUNT_INSTR)
   inc_m32abs(&instr_count[50]);
#endif
#ifdef INTERPRET_BGEZL
   gencallinterp((unsigned long long)BGEZL, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BGEZL, 1);
    return;
     }
   
   genbgez_test();
   free_all_registers();
   gentestl();
#endif
}

void genbgezl_out(void)
{
#if defined(COUNT_INSTR)
   inc_m32abs(&instr_count[50]);
#endif
#ifdef INTERPRET_BGEZL_OUT
   gencallinterp((unsigned long long)BGEZL_OUT, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BGEZL_OUT, 1);
    return;
     }
   
   genbgez_test();
   free_all_registers();
   gentestl_out();
#endif
}

void genbgezl_idle(void)
{
#ifdef INTERPRET_BGEZL_IDLE
   gencallinterp((unsigned long long)BGEZL_IDLE, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BGEZL_IDLE, 1);
    return;
     }
   
   genbgez_test();
   gentest_idle();
   genbgezl();
#endif
}

void genbranchlink(void)
{
   int r31_64bit = is64((unsigned int*)&reg[31]);
   
   if (r31_64bit == 0)
     {
    int r31 = allocate_register_32_w((unsigned int *)&reg[31]);
    
    mov_reg32_imm32(r31, dst->addr+8);
     }
   else if (r31_64bit == -1)
     {
    mov_m32abs_imm32((unsigned int *)&reg[31], dst->addr + 8);
    if (dst->addr & 0x80000000)
      mov_m32abs_imm32(((unsigned int *)&reg[31])+1, 0xFFFFFFFF);
    else
      mov_m32abs_imm32(((unsigned int *)&reg[31])+1, 0);
     }
   else
     {
    int r31 = allocate_register_64_w((unsigned long long *)&reg[31]);
    
    mov_reg32_imm32(r31, dst->addr+8);
    movsxd_reg64_reg32(r31, r31);
     }
}

void genbltzal(void)
{
#if defined(COUNT_INSTR)
   inc_m32abs(&instr_count[51]);
#endif
#ifdef INTERPRET_BLTZAL
   gencallinterp((unsigned long long)BLTZAL, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BLTZAL, 1);
    return;
     }
   
   genbltz_test();
   genbranchlink();
   gendelayslot();
   gentest();
#endif
}

void genbltzal_out(void)
{
#if defined(COUNT_INSTR)
   inc_m32abs(&instr_count[51]);
#endif
#ifdef INTERPRET_BLTZAL_OUT
   gencallinterp((unsigned long long)BLTZAL_OUT, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BLTZAL_OUT, 1);
    return;
     }
   
   genbltz_test();
   genbranchlink();
   gendelayslot();
   gentest_out();
#endif
}

void genbltzal_idle(void)
{
#ifdef INTERPRET_BLTZAL_IDLE
   gencallinterp((unsigned long long)BLTZAL_IDLE, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BLTZAL_IDLE, 1);
    return;
     }
   
   genbltz_test();
   genbranchlink();
   gentest_idle();
   genbltzal();
#endif
}

void genbgezal(void)
{
#if defined(COUNT_INSTR)
   inc_m32abs(&instr_count[52]);
#endif
#ifdef INTERPRET_BGEZAL
   gencallinterp((unsigned long long)BGEZAL, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BGEZAL, 1);
    return;
     }
   
   genbgez_test();
   genbranchlink();
   gendelayslot();
   gentest();
#endif
}

void genbgezal_out(void)
{
#if defined(COUNT_INSTR)
   inc_m32abs(&instr_count[52]);
#endif
#ifdef INTERPRET_BGEZAL_OUT
   gencallinterp((unsigned long long)BGEZAL_OUT, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BGEZAL_OUT, 1);
    return;
     }
   
   genbgez_test();
   genbranchlink();
   gendelayslot();
   gentest_out();
#endif
}

void genbgezal_idle(void)
{
#ifdef INTERPRET_BGEZAL_IDLE
   gencallinterp((unsigned long long)BGEZAL_IDLE, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BGEZAL_IDLE, 1);
    return;
     }
   
   genbgez_test();
   genbranchlink();
   gentest_idle();
   genbgezal();
#endif
}

void genbltzall(void)
{
#if defined(COUNT_INSTR)
   inc_m32abs(&instr_count[53]);
#endif
#ifdef INTERPRET_BLTZALL
   gencallinterp((unsigned long long)BLTZALL, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BLTZALL, 1);
    return;
     }
   
   genbltz_test();
   genbranchlink();
   free_all_registers();
   gentestl();
#endif
}

void genbltzall_out(void)
{
#if defined(COUNT_INSTR)
   inc_m32abs(&instr_count[53]);
#endif
#ifdef INTERPRET_BLTZALL_OUT
   gencallinterp((unsigned long long)BLTZALL_OUT, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BLTZALL_OUT, 1);
    return;
     }
   
   genbltz_test();
   genbranchlink();
   free_all_registers();
   gentestl_out();
#endif
}

void genbltzall_idle(void)
{
#ifdef INTERPRET_BLTZALL_IDLE
   gencallinterp((unsigned long long)BLTZALL_IDLE, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BLTZALL_IDLE, 1);
    return;
     }
   
   genbltz_test();
   genbranchlink();
   gentest_idle();
   genbltzall();
#endif
}

void genbgezall(void)
{
#if defined(COUNT_INSTR)
   inc_m32abs(&instr_count[54]);
#endif
#ifdef INTERPRET_BGEZALL
   gencallinterp((unsigned long long)BGEZALL, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BGEZALL, 1);
    return;
     }
   
   genbgez_test();
   genbranchlink();
   free_all_registers();
   gentestl();
#endif
}

void genbgezall_out(void)
{
#if defined(COUNT_INSTR)
   inc_m32abs(&instr_count[54]);
#endif
#ifdef INTERPRET_BGEZALL_OUT
   gencallinterp((unsigned long long)BGEZALL_OUT, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BGEZALL_OUT, 1);
    return;
     }
   
   genbgez_test();
   genbranchlink();
   free_all_registers();
   gentestl_out();
#endif
}

void genbgezall_idle(void)
{
#ifdef INTERPRET_BGEZALL_IDLE
   gencallinterp((unsigned long long)BGEZALL_IDLE, 1);
#else
   if (((dst->addr & 0xFFF) == 0xFFC && 
       (dst->addr < 0x80000000 || dst->addr >= 0xC0000000))||no_compiled_jump)
     {
    gencallinterp((unsigned long long)BGEZALL_IDLE, 1);
    return;
     }
   
   genbgez_test();
   genbranchlink();
   gentest_idle();
   genbgezall();
#endif
}

