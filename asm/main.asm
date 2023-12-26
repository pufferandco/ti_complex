#include "asm/include.inc"
.assume	ADL=1
.org	userMem-2
	.db          tExtTok,tAsm84CeCmp
	             
	set          AppAutoScroll,(IY + AppFlags)
	ld           (StackSave),SP
	ld           HL,callStackStart
	ld           (callStack),HL
	ld           SP,stackStart
	call         init
	call         _homeup
	call         _ClrScrnFull
	             
	ld           A,0
	push         AF
while_start21:
	ld           HL,(stackStart-3)
	push         HL
	ld           A,30
	push         AF
	pop          HL
	pop          AF
	call         byte_smaller
	cp           A,%11111111
	jp           NZ,while_end22
	call         sub_block_enter
	call         sub_block_enter
	ld           HL,(stackStart-3)
	push         HL
	ld           HL,0
	add          HL,SP
	ld           SP,(callStack)
	call         fib_2
	pop          HL
	ld           (callStack),SP
	ld           SP,HL
	push         DE
	call         printLn_int
	ld           HL,(stackStart-3)
	push         HL
	pop          AF
	inc          A
	push         AF
	pop          HL
	ld           (stackStart-3),HL
	call         sub_block_leave
	jp           while_start21
while_end22:
	             
ProgramExit:
	call         _ClrScrnFull
	res          donePrgm,(iy+doneFlags)
	ld           SP,(StackSave)
	ret          
#include "asm/defaults.asm"
StackSave:
	.db          0,0,0
CallStack:
	.db          0,0,0
SaveLocation:
	.db          0,0,0
stackStart .equ saveSScreen+7315
callStackStart .equ saveSScreen+14630
globalVars .equ pixelShadow

fib_2:
	push         IX
	ld           (callStack),SP
	ld           SP,HL
	ld           IX,3
	add          IX,SP
	call         sub_block_enter
	ld           HL,(IX-3)
	push         HL
	ld           A,1
	push         AF
	pop          HL
	pop          AF
	call         byte_equals
	push         AF
	ld           HL,(IX-3)
	push         HL
	ld           A,0
	push         AF
	pop          HL
	pop          AF
	call         byte_equals
	push         AF
	pop          HL
	pop          AF
	or           A,H
	cp           A,%11111111
	jp           NZ,if_next_10
	ld           HL,1
	push         HL
	pop          DE
	ld           B,1
	call         multi_block_leave
	jp           fib_end_3
	jp           if_end_9
if_next_10:
	call         sub_block_enter
	ld           HL,(IX-3)
	push         HL
	pop          AF
	dec          A
	push         AF
	ld           HL,0
	add          HL,SP
	ld           SP,(callStack)
	call         fib_2
	pop          HL
	ld           (callStack),SP
	ld           SP,HL
	push         DE
	call         sub_block_enter
	ld           HL,(IX-3)
	ld           A,H
	sub          A,2
	ld           H,A
	push         HL
	ld           HL,0
	add          HL,SP
	ld           SP,(callStack)
	call         fib_2
	pop          HL
	ld           (callStack),SP
	ld           SP,HL
	push         DE
	pop          HL
	pop          DE
	add          HL,DE
	push         HL
	pop          DE
	ld           B,1
	call         multi_block_leave
	jp           fib_end_3
if_end_9:
	call         sub_block_leave
	ld           DE,0
fib_end_3:
	ld           SP,(callStack)
	pop          IX
	ret          

