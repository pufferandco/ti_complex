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
	             
	             
	ld           H,5
	push         HL
	             
	ld           HL,(stackStart-3)
	push         HL
	ld           H,0
	push         HL
	pop          AF
	pop          HL
	call         byte_higher
	push         HL
while_start5:
	ld           HL,0
	add          HL,SP
	ld           SP,(callStack)
	push         HL
	ld           (callStack),SP
	ld           SP,HL
	pop          AF
	cp           A,%11111111
	jp           NZ,while_end6
	             
	ld           HL,(stackStart-3)
	push         HL
	pop          DE
	ld           HL,0
	ld           L,D
	call         _DispHL
	call         _NewLine
	             
	ld           HL,(stackStart-3)
	push         HL
	ld           H,1
	pop          AF
	sub          A,H
	ld           H,A
	ld           (stackStart-3),HL
	ld           SP,(callStack)
	pop          HL
	ld           (callStack),SP
	ld           SP,HL
	jp           while_start5
while_end6:
	             
	ld           HL,(stackStart-3)
	push         HL
	pop          DE
	ld           HL,0
	ld           L,D
	call         _DispHL
	call         _NewLine
	             
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
stackStart .equ saveSScreen+762
callStackStart .equ saveSScreen+768

