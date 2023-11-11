HEAP_START .equ saveSScreen+14630
HEAP_SIZE .equ 21945
MAX_HEAP_ELEMENT_SIZE .equ 65536
MAX_STRING_SIZE .equ 255



regTRUE .equ %11111111
regFALSE .equ %00000000

Var_Safe1:
    .db 0,0,0


get_string_size:							; (pointer(HL) string_ptr) -> double(HL) size
	ld 		A, 0							; byte(A) compare_byte = 0
	ld 		BC, MAX_STRING_SIZE     		; double(BC) fail_save = MAX_STRING_SIZE
	
	push 	HL								; pointer(DE) string_start = copy string_ptr
	pop		DE								;;
	
	CPIR									; while(&string_ptr != compare_byte && fail_save != 0){string_ptr++; fail_save--;}
	
	or		A   							;;  size = string_ptr - string_start
	sbc		HL, DE							;; 
	
	ret										; return size



get_array_size:
    pop     DE
    pop     HL
    ld      BC, 0
    ld      B, (HL)
    inc     HL
    ld      C, (HL)
    push    BC
    ex      DE, HL
    jp      (HL)



merge_byte_to_int:
    pop     HL
    ld      BC, 0
    pop     AF
    ld      B, A
    pop     AF
    ld      C, A
    push    BC
    jp      (HL)



get_upper_int:
    pop     HL
    pop     BC
    ld      A, B
    push    AF
    jp      (HL)



get_lower_int:
    pop     HL
    pop     BC
    ld      A, C
    push    AF
    jp      (HL)



set_character_cursor:
    pop     HL
    pop     AF
    ld      (curRow),A
    pop     AF
    ld      (curCol), A
    jp      (HL)



init:										; initializes stuff
CopyHL1555Palette:
	ld hl,$E30200
	ld b,0
_cp1555loop:
	ld d,b
	ld a,b
	and %11000000
	srl d
	rra
	ld e,a
	ld a,%00011111
	and b
	or e
	ld (hl),a
	inc hl
	ld (hl),d
	inc hl
	inc b
	jr nz,_cp1555loop

	ret



print_bool:
	cp		A, regTRUE
	jp		Z, print_bool__true
	ld		HL, false_string
	call 	_PutS
	ret
print_bool__true:
	ld		HL, true_string
	call 	_PutS
	ret

true_string:
	.db "true",0
false_string:
	.db "false",0



printLn_string:
    pop         DE
    pop         HL
    call        _PutS
    call        _NewLine
    ex          DE, HL
    jp          (HL)



print_byte:
    pop         DE
    pop         BC
    push        DE
    ld          HL, 0
    ld          L, B
    call        _DispHL
    ret



printLn_byte:
    pop         DE
    pop         BC
    push        DE
    ld          HL, 0
    ld          L, B
    call        _DispHL
    call        _NewLine
    ret



print_int:
    pop         DE
    pop         BC
    push        DE
    ld          HL, 0
    ld          L, C
    ld          H, B
    call        _DispHL
    ret



printLn_int:
    pop         DE
    pop         BC
    push        DE
    ld          HL, 0
    ld          L, C
    ld          H, B
    call        _DispHL
    call        _NewLine
    ret



byte_smaller:
    cp		    A, H
    jr		    C, A_true_ret
    ld	        A, regFALSE
    ret



byte_higher:
    cp		    A, H
    jr		    Z, A_false_ret
    jr		    C, A_false_ret
    ld	        A, regTRUE
    ret



byte_higher_or_equals:
    cp		    A, H
    jr		    NC, A_true_ret
    ld	        A, regFALSE
    ret



byte_lower_or_equals:
    cp		    A, H
    jr		    Z, A_true_ret
    jr          C, A_true_ret
    ld	        A, regFALSE
    ret



byte_equals:
    cp	        A, H
    jp          C, A_false_ret
    jp          Z, A_true_ret
    ld          A, regFALSE
    ret



byte_not_equals:
    cp	        A, H
    jp          C, A_true_ret
    jp          Z, A_false_ret
    ld          A, regTRUE
    ret



A_true_ret:
    ld          A, regTRUE
    ret



A_false_ret:
    ld          A, regFALSE
    ret



int_smaller:
    or          A
    sbc         HL, DE
    add         HL, DE
    jp          C, A_true_ret
    ld          A, regFALSE
    ret



int_higher:
    or          A
    sbc         HL, DE
    add         HL, DE
    jp          Z, A_false_ret
    jp          NC, A_true_ret
    ld          A, regFALSE
    ret



int_higher_or_equals:
    or          A
    sbc         HL, DE
    add         HL, DE
    jp          NC, A_true_ret
    ld          A, regFALSE
    ret



int_smaller_or_equals:
    or          A
    sbc         HL, DE
    add         HL, DE
    jp          Z, A_true_ret
    jp          C, A_true_ret
    ld          A, regFALSE
    ret



int_equals:
    or          A
    sbc         HL, DE
    add         HL, DE
    jp          C, A_false_ret
    jp          Z, A_true_ret
    ld          A, regFALSE
    ret



int_not_equals:
    or          A
    sbc         HL, DE
    add         HL, DE
    jp          C, A_true_ret
    jp          Z, A_false_ret
    ld          A, regTRUE
    ret



sub_block_enter:
    pop         DE
    ld          HL, 0
	add         HL, SP
	ld          SP, (callStack)
	push        HL
	ld          (callStack), SP
	ld          SP, HL
	ex          DE, HL
	jp          (HL)



sub_block_leave:
    pop         DE
    ld          SP,(callStack)
    pop         HL
    ld          (callStack),SP
    ld          SP,HL
	ex          DE, HL
    jp          (HL)


multi_block_leave:
    pop         HL
    ld          (Var_Safe1), HL
    ld          SP,(callStack)
multi_block_loop:
    pop         HL
    djnz        multi_block_loop
    ld          (callStack),SP
    ld          SP,HL
	ld          HL, (Var_Safe1)
	jp          (HL)



function_did_not_return:
	ld		HL, function_did_not_return__message ; string(HL) error_message = "out of memory!"
	call	_PutS
	jp		ProgramExit

function_did_not_return__message:
	.db 	"out of memory!", 0



index_out_of_bounds:
    ld      HL, index_out_of_bounds__message
    call    _PutS
    jp      ProgramExit

index_out_of_bounds__message:
    .db     "index out of bounds!", 0



multiply_int:
   ld	hl, 0

   sla	e
   rl	d
   jr	nc, $+4
   ld	h, b
   ld	l, c

   ld	a, 15
multiply_int__loop:
   add	hl, hl
   rl	e
   rl	d
   jr	nc, $+6
   add	hl, bc
   jr	nc, $+3
   inc	de

   dec	a
   jr	nz, multiply_int__loop

   ret



sleep:
    or          A
    ld          DE, 0
sleep__loop:
    dec         HL
    sbc         HL, DE
    add         HL, DE
    jr	        nz, sleep__loop

    ret



sleep_millis:
    pop         HL
    ld          (Var_Safe1), HL

    pop         HL
    ld          BC, 0

sleep_millis__loop:
    push        HL
    ld          HL, 500
    call        sleep
    pop         HL

    dec         HL
    sbc         HL, BC
    add         HL, BC
    jp          nz, sleep_millis__loop


    ld          HL, (Var_Safe1)
    jp          (HL)


randData:
    .dw 0
random:
        push    hl
        push    de
        ld      hl,(randData)
        ld      a,r
        ld      d,a
        ld      e,(hl)
        add     hl,de
        add     a,l
        xor     h
        ld      (randData),hl
        pop     de
        pop     hl
        ret






KEY_MAPPING:
    .db $21, $30
    .db $22, $31
    .db $1A, $32
    .db $12, $33
    .db $23, $34
    .db $1B, $35
    .db $13, $36
    .db $24, $37
    .db $1C, $38
    .db $14, $39
    .db $0C, $2A
    .db $0B, $2D
    .db $0A, $2B
    .db $25, $2C
    .db $19, $2E

ALPHA_KEY_MAPPING:
    .db $2F, $61
    .db $27, $62
    .db $1F, $63
    .db $2E, $64
    .db $26, $65
    .db $1E, $66
    .db $16, $67
    .db $0E, $68
    .db $2D, $69
    .db $25, $6A
    .db $1D, $6B
    .db $15, $6C
    .db $0D, $6D
    .db $2C, $6E
    .db $24, $6F
    .db $1C, $70
    .db $14, $71
    .db $0C, $72
    .db $2B, $73
    .db $23, $74
    .db $1B, $75
    .db $13, $76
    .db $0B, $77
    .db $2A, $78
    .db $22, $79
    .db $1A, $7A
    .db $21, $20