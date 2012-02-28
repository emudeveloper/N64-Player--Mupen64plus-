/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *   Mupen64plus - rjump.c                                                 *
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

#include <stdlib.h>

#include "../recomp.h"
#include "../r4300.h"
#include "../macros.h"
#include "../ops.h"
#include "../recomph.h"

extern int dynarec_stack_initialized;  /* in gr4300.c */

void dyna_jump()
{
    if (stop == 1)
    {
        dyna_stop();
        return;
    }

    if (PC->reg_cache_infos.need_map)
        *return_address = (unsigned long) (PC->reg_cache_infos.jump_wrapper);
    else
        *return_address = (unsigned long) (actual->code + PC->local_addr);
}

static long save_rsp = 0;
static long save_rip = 0;

void dyna_start(void (*code)())
{
  /* save the base and stack pointers */
  /* make a call and a pop to retrieve the instruction pointer and save it too */
  /* then call the code(), which should theoretically never return.  */
  /* When dyna_stop() sets the *return_address to the saved RIP, the emulator thread will come back here. */
  /* It will jump to label 2, restore the base and stack pointers, and exit this function */
  printf("R4300 core: starting 64-bit dynamic recompiler at: 0x%lx.\n", (unsigned long) code);
#if defined(__GNUC__) && defined(__x86_64__)
   asm volatile
      (" push %%rbx           \n"  /* we must push an even # of registers to keep stack 16-byte aligned */
       " push %%r12           \n"
       " push %%r13           \n"
       " push %%r14           \n"
       " push %%r15           \n"
       " push %%rbp           \n"
       " mov  %%rsp, save_rsp \n"
       " call 1f              \n"
       " jmp 2f               \n"
       "1:                    \n"
       " pop  %%rax           \n"
       " mov  %%rax, save_rip \n"
       " call *%%rbx          \n"
       "2:                    \n"
       " mov  save_rsp, %%rsp \n"
       " pop  %%rbp           \n"
       " pop  %%r15           \n"
       " pop  %%r14           \n"
       " pop  %%r13           \n"
       " pop  %%r12           \n"
       " pop  %%rbx           \n"
       :
       : "b" (code)
       : "%rax", "memory"
       );
#endif

    /* clear flag; stack is back to normal */
    dynarec_stack_initialized = 0;

    /* clear the registers so we don't return here a second time; that would be a bug */
    save_rsp=0;
    save_rip=0;
}

void dyna_stop()
{
  if (save_rip == 0)
    printf("Warning: instruction pointer is 0 at dyna_stop()\n");
  else
  {
    *return_address = (unsigned long) save_rip;
  }
}

