HEAP_START .equ saveSScreen+14630
HEAP_SIZE .equ 21945
MAX_HEAP_ELEMENT_SIZE .equ 65536
MAX_STRING_SIZE .equ 255



regTRUE .equ %11111111
regFALSE .equ %00000000

Var_Safe1:
    .db 0,0,0



init:										; initializes stuff
    ;ld              HL, OP1
    ;call            _PutS
    ;push            IX
    ;ld              HL, symTable
    ;ld              IX, OP1
    ;call            fdetect
    ;;jp              NZ, thrown_error
    ;pop             IX
    ;push            BC
    ;pop             HL
;
    ;;ld              HL, saveMemory-MemoryStart+4
    ;;ex              DE, HL
    ;;add             HL, DE
    ;ld              (SaveLocation), HL

	ld              hl,$E30200               ; initializes colors
	ld              b,0

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
	ld A, ($F30000)                          ; initializes random seed
	ld (seed_lower+1), A
    ld A, ($F30004)
    ld (seed_lower+2), A
    ld A, ($F30008)
    ld (seed_upper+1), A
    ld A, ($F3000C)
    ld (seed_upper+2), A

	ret




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



multiply_int:; num1: int(DE), num2(BC): int -> int(HL)
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



divide_byte:
    pop         HL
    pop         BC
    pop         AF
	ld          C,$00

divide_byte__loop:
	inc         C
	sub         A, B
	jr          Z, divide_byte__end
	jr          NC,divide_byte__loop

divide_byte__overflow:
	ld          A, C
    dec         A
    push        AF
    jp          (HL)

divide_byte__end:
    ld          A, C
    push        AF
    jp          (HL)



modulo_byte:
    pop         HL
    pop         BC
    pop         AF

    cp          A, B
    jp          C, modulo_byte__end
modulo_byte__loop:
    sub         A, B
    cp          A, B
    jp          NC, modulo_byte__loop

modulo_byte__end:
    push        AF
    jp          (HL)




divide_long_byte: ;DE HL / A -> DEHL
   xor	a
   ld	b, 32

divide_long_byte__loop:
   add	hl, hl
   rl	e
   rl	d
   rla
   jr	c, $+5
   cp	c
   jr	c, $+4

   sub	c
   inc	l

   djnz	divide_long_byte__loop

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


seed_upper:
    .db 00,208,80
seed_lower:
    .db 00,31,221

random_number:
    ld hl,(seed_upper)
    ld b,h
    ld c,l
    add hl,hl
    add hl,hl
    inc l
    add hl,bc
    ld (seed_upper),hl
    ld hl,(seed_lower)
    add hl,hl
    sbc a,a
    and %00101101
    xor l
    ld l,a
    ld (seed_lower),hl
    add hl,bc
    ret



;load_from_memory:
;    pop         HL
;    ld          (Var_Safe1), HL
;
;    ld          HL, SaveMemory
;    ld          BC, 0
;    ld          C, (HL)
;    inc         HL
;    ld          B, (HL)
;    inc         HL
;
;    ex          DE, HL
;
;    ld          HL, (SaveLocation)
;    inc         HL
;    inc         HL
;
;    LDIR
;
;    ld          HL, (Var_Safe1)
;    jp          (HL)



write_to_memory:
    ld          DE, (SaveLocation)
    ld          HL, SaveMemory
    ld          BC, saveSize
    ld          A, (SaveMemory)

    LDIR

    ret



;-------------------------------------
; fdetect
; detects appvars, prot progs, and
; progs given a 0-terminated string
; pointed to by ix.
;-------------------------------------
; INPUTS:
; hl->place to start searching
; ix->string to find
;
; OUTPUTS:
; hl->place stopped searching
; de->program data
; bc=program size
; OP1 contains the name and type byte
; z flag set if found
;-------------------------------------
fdetect:
 ld de,(ptemp)
 call _cphlde
 ld a,(hl)
 ld (typeByte_SMC),a
 jr nz,fcontinue
 inc a
 ret
fcontinue:
 push hl
 cp appvarobj
 jr z,fgoodtype
 cp protprogobj
 jr z,fgoodtype
 cp progobj
 jr nz,fskip
fgoodtype:
 dec hl
 dec hl
 dec hl
 ld e, (hl)
 dec hl
 ld d,(hl)
 dec hl
 ld a,(hl)
 call _SetDEUToA
 push de
 pop hl
 cp $D0
 jr nc,finRAM
 push ix
 push de
  push hl
  pop ix
  ld a,10
  add a,(ix+9)
  ld de,0
  ld e,a
  add hl,de
  ex (sp),hl
  add hl,de
 pop de
 ex de,hl
 pop ix
finRAM:
 inc de
 inc de
 ld bc,0
 ld c,(hl)
 inc hl
 ld b,(hl)
 inc hl ; hl -> data
 push bc ; bc = size
 push ix
 pop bc
fchkstr:
 ld a,(bc)
 or a,a
 jr z,ffound
 cp (hl)
 inc bc
 inc de
 inc hl
 jr z,fchkstr
 pop bc
fskip:
 pop hl
 call fbypassname
 jr fdetect
ffound:
 push bc
 pop hl
 push ix
 pop bc
 or a,a
 sbc hl,bc
 push hl
 pop bc
 pop hl ; size
 or a,a
 sbc hl,bc
 push hl
 pop bc
 pop hl ; current search location
 push bc
 call fbypassname
 pop bc
 xor a
 ret
fbypassname:
 push de
  ld bc,-6
  add hl,bc
  ld de,OP1
  push de
   ld b,(hl)		; Name to OP1 -> For things like archiving/deleting
   inc b
fbypassnameloop:
   ld a,(hl)
   ld (de),a
   inc de
   dec hl
   djnz fbypassnameloop
   xor a
   ld (de),a
typeByte_SMC: =$+1
   ld a,15h
  pop de
  ld (de),a
 pop de
 ret



thrown_error:
    pop         HL
    call        _PutS
    call        _getKey

    jp           ProgramExit



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