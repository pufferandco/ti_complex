CONTROL_REGISTER .equ $F20030
TOGGLE_BIT .equ 3
CLOCK_SOURCE_BIT .equ 4
ALLOW_INTERRUPT_BIT .equ 5
CLOCK_DIRECTION_BIT .equ 3
COUNTER_REGISTER .equ $F20010



start_timer:
    ld           HL, 1
    ld           (COUNTER_REGISTER), HL

    ld           HL, CONTROL_REGISTER
    set          TOGGLE_BIT, (HL)

    ret



stop_timer:
    ld           HL, CONTROL_REGISTER
    res          TOGGLE_BIT, (HL)
    ret



read_timer:
    pop          HL
    ld           (Var_Safe1), HL

    ld           HL, (COUNTER_REGISTER)

    ;ld           A, 200

    ;call         divide_long_byte

    push         HL
    call         stop_timer
    ld           HL, (Var_Safe1)
    jp           (HL)


init_timer:
    ld           HL, CONTROL_REGISTER
    res          CLOCK_SOURCE_BIT, (HL)
    ;res          ALLOW_INTERRUPT_BIT, (HL)
    ;inc          HL
    ;set          CLOCK_DIRECTION_BIT, (HL)

    ret
