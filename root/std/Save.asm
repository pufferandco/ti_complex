create_app_var: ;size: int, name: string
    pop         HL
    ld          (Var_Safe1), HL

    pop         DE
    ld          HL, OP1
    ld          (HL), AppVarObj
    inc         HL
    ex          DE, HL
    ld          BC, 8

    LDIR


    pop         HL
    push        HL
    call        _CreateAppVar

    pop         BC

    ex          DE, HL

    ld          (HL), C
    inc         HL
    ld          (HL), B
    inc         HL

    push        HL

    ld          HL, (Var_Safe1)
    jp          (HL)



get_app_var:
    pop         HL
    ld          (Var_Safe1), HL

    pop         DE
    ld          HL, OP1
    ld          (HL), AppVarObj
    inc         HL
    ex          DE, HL
    ld          BC, 8
    LDIR

    call        _ChkFindSym
    jp          NC, get_app_var__found

get_app_var__not_found:
        ld          HL, 0
        push        HL

        ld          HL, (Var_Safe1)
        jp          (HL)
get_app_var__found:
        inc         DE
        inc         DE
        push        DE

        ld          HL, (Var_Safe1)
        jp          (HL)



del_app_var:
    pop         HL
    ld          (Var_Safe1), HL

    pop         DE
    ld          HL, OP1
    ld          (HL), AppVarObj
    inc         HL
    ex          DE, HL
    ld          BC, 8
    LDIR

    call        _ChkFindSym
    jp          C, error_message
    ld          B, 0
    call        _DelVar

    ld          HL, (Var_Safe1)
    jp          (HL)



file_exception:
    ld          HL, error_message
    call        _PutS
    call        _getKey

    jp           ProgramExit


error_message:
    .db         "file error", 0
