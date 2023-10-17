HEAP_START .equ plotSScreen
HEAP_SIZE .equ 21945
MAX_HEAP_ELEMENT_SIZE .equ 65536
; struct heap_header(
; boolean(b0) is_used,
; boolean(b1) is_last 
;)
;

	.db 0
malloc__required_size:
	.db 0, 0, 0
malloc:			; (double(BC) required_size) -> pointer(BC) # gets space in memory
	ld		(malloc__required_size-1), BC 	; move requred_size(BC -> RAM)
	ld		HL, HEAP_START					; pointer(HL) current_location = HEAP_START
	
malloc__loop:
	ld  	B, (HL)							; heap_header(B) header = &current_location
	
	inc 	HL								; current_location++
	ld		DE, 0							; double(DE) size = 0;
	ld		D, (HL)							; size.upper = &current_location
	inc		HL								; current_location++
	ld		E, (HL)							; size.lower = &current_location
	inc		HL								; current_location++
	push	HL								; move current_location(HL -> STACK)
	
	bit 	0, B							; if(not header.is_used):
	jp 		Z, malloc__found_block			;;	goto $found_block
								
malloc__goto_next:					
	pop		HL								; move current_location(STACK -> HL)
	
	bit 	1, B							; if(header.is_last):
	jp 		Z, malloc__out_of_space			;;	goto $out_of_space
	
	add		HL, DE							; current_location += size
	jp 		malloc__loop					; goto $loop	

malloc__found_block:			
	ld 		HL, malloc__required_size		; *double(HL) *minimum_size = required_size
	
	ld		A, D							; copy size.upper(D -> A)
	cp 		A, (HL)							; if(size.upper < required_size.upper)
	jp		C, malloc__goto_next			;;	goto $goto_next 
	
	inc		HL								; *minimum_size++
	ld		A, E							; copy size.upper(E -> A)
	cp 		A, (HL)							; if(size.lower < required_size.lower)
	jp		C, malloc__goto_next			;;	goto $goto_next 
	
	dec		HL								; *minimum_size--
	ld		A, D							; copy size.upper(D -> A)
	cp 		A, (HL)							; if(size.upper != required_size.upper)
	jp		NZ, malloc__split				;;	goto $split
	
	inc		HL								; *minimum_size++
	ld		A, E							; copy size.upper(E -> A)
	cp 		A, (HL)							; if(size.lower == required_size.lower)
	jp		NZ, malloc__split				;;	goto $split
	
	pop		HL								; copy current_location(STACK -> HL)
	push	HL
	dec		HL
	dec		HL
	dec		HL
	set 	0, (HL) 						; header.is_used = true
	
	pop		BC								; move current_location(STACK -> BC)
	ret										; return current_location
	
malloc__split:
	pop		HL								; copy current_location(STACK -> HL)
	push	HL								;;
	dec		HL								;;
	dec		HL								;;
	dec		HL								;;
	res 	1, (HL) 						;; header.is_last = false
	set 	1, (HL) 						;; header.is_used = true
	
	ex		DE, HL							; double(HL) old_size = size
	
	ld		DE, 0							;; move required_size(RAM -> DE)
	ld		A, (malloc__required_size)		;; 
	ld		D, A							;;
	ld		A, (malloc__required_size+1)	;;
	ld		E, A							;;
	
	sub		A, 0							; carry = false
	push	HL								; copy old_size(HL -> STACK)
	sbc		HL, DE 							; spliced_size(HL) = old_size - required_size

	ex		DE, HL							; move spliced_size(HL -> DE)
	pop 	HL								; move old_size(STACK -> HL)
	
	add		HL, BC							; new_location(HL) = current_location + old_size

	ld		(HL), 0							; new heap_header(*HL) = {is_used = false}
	inc		HL								; new_location++
	ld		(HL), D							; heap[new_location] = spliced_size.upper
	inc		HL								; new_location++
	ld		(HL), E							; heap[new_location] = spliced_size.lower

	
	pop		BC								; move old_location(STACK -> BC)
	ret 									; return old_location(BC)
	
malloc__out_of_space:
	ld		HL, malloc__space_error_message ; string(HL) error_message = "out of memory!"
	call	_PutS
	jp		ProgramExit
	
malloc__space_error_message:
	.db 	"out of memory!", 0



free:										; (pointer(HL) location) -> null # gets space in memory
	dec		HL								; location -= 3
	dec		HL								;;
	dec		HL								;;
	
	set 	0, (HL)							; heap_header(location).is_used = false
	ret										; return null



merge:										; () -> null # gets space in memory
	ld		HL, HEAP_START					; pointer(HL) current_location = HEAP_START
	ld		B, (HL)							; heap_header(B) header = &current_location
	ld		DE, 0							; double(DE) size = 0
	ld		BC, 0							; double(BC) new_size = 0

merge__loop:
	bit 	1, B							; if(header.is_last):
	ret 	Z								;;	return null
	
	push	HL								; pointer(STACK) old_location = copy current_location
	
	inc		HL								; current_location++
	ld		D, (HL)							; size.upper = &current_location
	inc		HL								; current_location++
	ld		E, (HL)							; size.lower = &current_location
	
	add		HL, DE							; current_location += size
	
	bit 	0, B							; if(header.is_used):
	jp 		Z, merge__continue				;;	goto $continue
	
	ld		B, (HL)							; header = &current_location
	
	bit 	0, B							; if(header.is_used):
	jp 		Z, merge__continue				;;	goto $continue
	
	inc		HL								; current_location++
	ld		B, (HL)							; new_size.upper = &current_location
	inc		HL								; current_location++
	ld		C, (HL)							; new_size.lower = &current_location
	
	ex		DE, HL							; move current_location(HL -> DE), move size(DE -> HL)
	add		HL, BC							; new_size += size
	ex		DE, HL							; move size(HL -> DE)
	
	pop		HL								; move old_location(STACK -> HL)
	
	inc		HL								; old_location += size
	ld		(HL), D 						; new_size.upper = &current_location
	inc		HL								; old_location += size
	ld		(HL), E 						; new_size.lower = &current_location
	
	dec		HL								; current_location = old_location-2
	dec		HL								;;
	
	jp		merge__loop						; goto $loop
	
merge__continue:
	pop		DE								; delete(STACK) old_location
	ret										; return null	
	


get_string_size:							; (pointer(HL) string_ptr) -> double(HL) size 
	ld 		A, 0							; byte(A) compare_byte = 0
	ld 		BC, MAX_HEAP_ELEMENT_SIZE		; double(BC) fail_save = MAX_STRING_SIZE
	
	push 	HL								; pointer(DE) string_start = copy string_ptr
	pop		DE								;;
	
	CPIR									; while(&string_ptr != compare_byte && fail_save != 0){string_ptr++; fail_save--;}
	
	add		A, 0							;;  size = string_ptr - string_start
	sbc		HL, DE							;; 
	
	ret										; return size	
	


string_to_heap:								; (string(HL) String) -> pointer(DE) heap_pointer
	push	HL								; copy String(HL -> STACK)
	
	call	get_string_size					;double(HL) string_size = get_string_size(String)
	
	ld		BC, 0							; move string_size(HL -> BC)
	ld		B,	H							;;
	ld		C,	L							;;
	push	BC								; copy string_size(BC -> STACK)

	inc		BC
	
	call	malloc							; pointer(BC) heap_location = malloc(string_size)
	
	push	BC								; move heap_location(BC -> DE)
	pop		DE								;;
	pop		BC								; move string_size(STACK -> BC)
	pop		HL								; move String(STACK -> HL)
	push	DE								; pointer(STACK) new_pointer = heap_location(DE)
	
	LDIR									; while(string_size != 0){
	;LDIR									;;	&heap_location = &String
	;LDIR									;;	heap_location
	;LDIR									;;	String++
	;LDIR									;; 	string_size--
	;LDIR									;;}
	
	pop		DE 								; return new_pointer
	ret										;;



init:										; initializes stuff
	ld		A, %01000000
	ld		(HEAP_START), a
	ld		HL, HEAP_SIZE
	ld		(HEAP_START+1), HL
	ret



print_bool:
	cp		A, %11111111
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