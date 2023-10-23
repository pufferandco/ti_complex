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
	             
	             
	ld           HL,string_1
	call         _PutS
	call         _NewLine
	             
	ld           H,52
	push         HL
	             
	             
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
stackStart .equ saveSScreen+512
callStackStart .equ saveSScreen+768

hello_5:
	push         IX                        ; push stack_start
	push         HL                        ; push current_stack_location
	ld           (callStack),SP
	ld           SP,HL
	ld           IX,0
	add          IX,SP
	             
	ld           HL,string_6
	call         _PutS
	             
	ld           HL,(IX-3)
	push         HL
	pop          DE
	ld           HL,0
	ld           H,D
	ld           L,E
	call         _DispHL
	call         _NewLine
null:
	ld           SP,(callStack)
	pop          HL
	pop          IX
	ret          

string_1:
	.db          "hello", 0
string_6:
	.db          "hello number", 0
