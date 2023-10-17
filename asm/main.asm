#include "asm/include.inc"
.assume	ADL=1
stackStart .equ saveSScreen+768
.org	userMem-2
	.db          tExtTok,tAsm84CeCmp
	             
	ld           (StackSave),SP
	ld           SP,stackStart
	call         init
	call         _homeup
	call         _ClrScrnFull
	             
	;[12:var, 7:hello, 6::, 7:int, 5:=, 7:326]
	ld           HL,326
	push         HL
	             
ProgramExit:
	call         _GetKey
	call         _ClrScrnFull
	res          donePrgm,(iy+doneFlags)
	ld           SP,(StackSave)
	ret          
#include "asm/api.asm"
StackSave:
	.db          0,0,0

