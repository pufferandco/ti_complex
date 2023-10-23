#include "asm/include.inc"
.assume	ADL=1
.org	userMem-2
	.db          tExtTok,tAsm84CeCmp
	             
	ld           (StackSave),SP
	ld           HL,callStackStart
	ld           (callStack),HL
	ld           SP,stackStart
	call         init
	call         _homeup
	call         _ClrScrnFull
	             
	             
	             
ProgramExit:
	call         _GetKey
	call         _ClrScrnFull
	res          donePrgm,(iy+doneFlags)
	ld           SP,(StackSave)
	ret          
#include "asm/api.asm"
StackSave:
	.db          0,0,0
CallStack:
	.db          0,0,0
stackStart .equ saveSScreen+512
callStackStart .equ saveSScreen+768

hello_2:
	             
	ld           HL,string_3
	call         _PutS
	             
	ld           HL,(stackStart-3)
	push         HL
	pop          DE
	ld           HL,0
	ld           H,D
	ld           L,E
	call         _DispHL
	call         _NewLine

string_3:
	.db          "hello number", 0
