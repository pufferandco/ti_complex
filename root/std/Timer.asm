CONTROL_REGISTER .equ F20000
TOGGLE_BIT .equ 0
CLOCK_SOURCE_BIT .equ 1
ALLOW_INTERRUPT_BIT .equ 2
CLOCK_DIRECTION_BIT .equ 2
COUNTER_REGISTER .equ $F20000



start_timer:
    ld           A, 1
    ld           (COUNTER_REGISTER), A
    ld           HL, 0
    ld           (COUNTER_REGISTER), HL

    ld           HL, CONTROL_REGISTER
    set          TOGGLE_BIT, (HL)

    ret



stop_timer:
    ld           HL, CONTROL_REGISTER
    set          TOGGLE_BIT, (HL)
    ret



read_timer:
    pop          HL
    ld           (Var_Safe1), HL

    ld           A, (COUNTER_REGISTER)
    ld           L, A
    ld           A, (COUNTER_REGISTER+1)
    ld           H, A
    ld           A, (COUNTER_REGISTER+2)
    ld           E, A
    ld           A, (COUNTER_REGISTER+3)
    ld           D, A

    ld           A, 5

    call         divide_long_byte

    push         HL
    ld           HL, (Var_Safe1)
    jp           (HL)


init_timer:
    ld           HL, CONTROL_REGISTER
    set          CLOCK_SOURCE_BIT, (HL)
    res          ALLOW_INTERRUPT_BIT, (HL)
    inc          HL
    res          CLOCK_DIRECTION_BIT, (HL)

    ret
