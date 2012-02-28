/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *   Mupen64plus - rjump.c                                                 *
 *   Mupen64Plus homepage: http://code.google.com/p/mupen64plus/           *
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

#ifdef __GNUC__
# define ASM_NAME(name) asm(name)
#else
# define ASM_NAME(name)
#endif

static long save_ebp ASM_NAME("save_ebp") = 0;
static long save_ebx ASM_NAME("save_ebx") = 0;
static long save_esi ASM_NAME("save_esi") = 0;
static long save_edi ASM_NAME("save_edi") = 0;
static long save_esp ASM_NAME("save_esp") = 0;
static long save_eip ASM_NAME("save_eip") = 0;

void dyna_start(void (*code)())
{
  /* save the base and stack pointers */
  /* make a call and a pop to retrieve the instruction pointer and save it too */
  /* then call the code(), which should theoretically never return.  */
  /* When dyna_stop() sets the *return_address to the saved EIP, the emulator thread will come back here. */
  /* It will jump to label 2, restore the base and stack pointers, and exit this function */
#if defined(_WIN32) && !defined(__GNUC__)
   __asm
   {
     mov _save_ebp, ebp
     mov _save_esp, esp
     call point1
     jmp point2
   point1:
     pop eax
     mov _save_eip, eax
     mov eax, code
     call eax
   point2:
     mov ebp, _save_ebp
     mov esp, _save_esp
   }
#elif defined(__GNUC__) && defined(__i386__)
   asm volatile 
      (" movl %%ebp, save_ebp \n"
       " movl %%esp, save_esp \n"
       " movl %%ebx, save_ebx \n"
       " movl %%esi, save_esi \n"
       " movl %%edi, save_edi \n"
       " call 1f              \n"
       " jmp 2f               \n"
       "1:                    \n"
       " popl %%eax           \n"
       " movl %%eax, save_eip \n"
       " call *%[codeptr]     \n"
       "2:                    \n"
       " movl save_ebp, %%ebp \n"
       " movl save_esp, %%esp \n"
       " movl save_ebx, %%ebx \n"
       " movl save_esi, %%esi \n"
       " movl save_edi, %%edi \n"
       :
       : [codeptr]"r"(code)
       : "eax", "ecx", "edx", "memory"
       );
#endif

    /* clear flag; stack is back to normal */
    dynarec_stack_initialized = 0;

    /* clear the registers so we don't return here a second time; that would be a bug */
    /* this is also necessary to prevent compiler from optimizing out the static variables */
    save_edi=0;
    save_esi=0;
    save_ebx=0;
    save_ebp=0;
    save_esp=0;
    save_eip=0;
}

void dyna_stop()
{
  if (save_eip == 0)
    printf("Warning: instruction pointer is 0 at dyna_stop()\n");
  else
  {
    *return_address = (unsigned long) save_eip;
  }
}

