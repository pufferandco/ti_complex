#include "bin/asm/include.inc"
.assume	ADL=1
stackStart .equ saveSScreen+768
.org	userMem-2
	.db          tExtTok,tAsm84CeCmp
	             
	ld           (StackSave),SP
	ld           SP,stackStart
	call         init
	call         _homeup
	call         _ClrScrnFull
	             
	             
	ld           H,%00000000
	push         HL
	pop          AF
	call         print_bool
	call         _NewLine
	             
ProgramExit:
	call         _GetKey
	call         _ClrScrnFull
	res          donePrgm,(iy+doneFlags)
	ld           SP,(StackSave)
	ret          
#include "bin/asm/api.asm"
StackSave:
	.db          0,0,0

