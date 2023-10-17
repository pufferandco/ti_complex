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
	             
	             
	ld           H,150
	push         HL
	             
	ld           HL,(stackStart-3)
	push         HL
	ld           H,100
	push         HL
	pop          AF
	pop          HL
	call         byte_higher
	push         HL
	pop          AF
	cp           A,%11111111
	jp           NZ,elif_next_4
	             
	ld           HL,string_5
	call         _PutS
	call         _NewLine
	jp           end_if_2
elif_next_4:
	ld           HL,(stackStart-3)
	push         HL
	ld           H,30
	push         HL
	pop          AF
	pop          HL
	call         byte_higher
	push         HL
	pop          AF
	cp           A,%11111111
	jp           NZ,elif_next_7
	             
	ld           HL,string_8
	call         _PutS
	call         _NewLine
	jp           end_if_2
elif_next_7:
	ld           HL,(stackStart-3)
	push         HL
	ld           H,15
	push         HL
	pop          AF
	pop          HL
	call         byte_higher
	push         HL
	pop          AF
	cp           A,%11111111
	jp           NZ,elif_next_10
	             
	ld           HL,string_11
	call         _PutS
	call         _NewLine
	jp           end_if_2
elif_next_10:
	             
	ld           HL,string_12
	call         _PutS
	call         _NewLine
end_if_2:
	             
ProgramExit:
	call         _GetKey
	call         _ClrScrnFull
	res          donePrgm,(iy+doneFlags)
	ld           SP,(StackSave)
	ret          
#include "asm/api.asm"
StackSave:
	.db          0,0,0

string_5:
	.db          "ancient age", 0
string_8:
	.db          "aging", 0
string_11:
	.db          "prime age", 0
string_12:
	.db          "baby", 0
