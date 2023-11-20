create_app_var: ;size: int, name_size: byte, name: string
    pop         HL
    ld          (Var_Safe1), HL

    pop         DE
    ld          HL, OP1
    ld          (HL), ProgObj
    inc         HL
    ex          DE, HL
    pop         BC

    LDIR

    pop         HL
    push        HL
    call        _CreateProg
    pop         BC

    ex          DE, HL

    ld          (HL), C
    inc         HL
    ld          (HL), B

    ld          HL, (Var_Safe1)
    jp          (HL)
