#include "asm/include.inc"
.assume	ADL=1
.org	userMem-2
	.db          tExtTok,tAsm84CeCmp

	set          AppAutoScroll,(IY + AppFlags)
	ld           (StackSave),SP
	ld           SP,stackStart
	call         _homeup
	call         _ClrScrnFull

    ld          A, 0

_loop:
        call        fib_routine

        push        AF
        call        _DispHL
        call        _newLine
        pop         AF

	    inc         A
	    cp          A, 30
	    jp          NZ, _loop

ProgramExit:
	call         _ClrScrnFull
	res          donePrgm,(iy+doneFlags)
	ld           SP,(StackSave)
	ret

StackSave:
	.db          0,0,0
stackStart .equ saveSScreen+7315

fib_routine: ; (index: byte(A)) -> int(HL) destroys all
    push    AF

    cp      A, 0
    jp      Z, fib_routine__ret_1
    cp      A, 1
    jp      Z, fib_routine__ret_1

    dec     A
    call    fib_routine

    push    HL
    dec     A
    call    fib_routine

    pop    DE
    add    HL, DE
    pop    AF
    ret



fib_routine__ret_1:
    ld      HL, 1
    pop    AF
    ret