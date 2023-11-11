SCREEN_WIDTH .equ 320
SCREEN_HEIGHT .equ 240
VRAM_B1 .equ vRam
VRAM_B2 .equ vRam + 38400
BUFFER_B1 .equ vRam + 76800
BUFFER_B2 .equ vRam + 76800 + 38400



set_pixel:
    pop          HL
    ld           (Var_Safe1), HL

    pop          DE; B = color
    pop          BC; HL = Y
    push         DE

    ld           A, 120
    cp           A, C
    jp           NC, set_pixel__block_2

set_pixel__block_1:
    ld           DE, SCREEN_WIDTH
    call         multiply_int

    ld           DE, VRAM_B1
    add          HL, DE

    jp           pixel_set
set_pixel__block_2:
    ld           DE, 120
    sbc          HL, DE

    ld           DE, SCREEN_WIDTH
    call         multiply_int

    ld           DE, VRAM_B1
    add          HL, DE

    jp           pixel_set



buffer_pixel:
    pop          HL
    ld           (Var_Safe1), HL

    pop          DE; B = color
    pop          BC; HL = Y
    push         DE

    ld           A, 120
    cp           A, C
    jp           NC, buffer_pixel__block_2

buffer_pixel__block_1:
    ld           DE, SCREEN_WIDTH
    call         multiply_int

    ld           DE, BUFFER_B1
    add          HL, DE

    jp           pixel_set
buffer_pixel__block_2:
    ld           DE, 120
    sbc          HL, DE

    ld           DE, SCREEN_WIDTH
    call         multiply_int

    ld           DE, BUFFER_B1
    add          HL, DE

    jp           pixel_set



pixel_set:
    pop          AF
    pop          DE
    add          HL, DE

    ld           (HL), A


    ld           HL, (Var_Safe1)
    jp           (HL)



clear_screen:
    pop          DE
    pop          AF

    ld           BC, VRAM_B1 + 76800
    ld           HL, VRAM_B1
clear_screen__loop:
    ld           (HL), A
    inc          HL
    and          A
    sbc          HL, BC
    add          HL, BC
    jp           NZ, clear_screen__loop

    ex           DE, HL
    jp           (HL)



clear_buffer:
    pop          DE
    pop          AF

    ld           BC, BUFFER_B1 + 76800
    ld           HL, BUFFER_B1
clear_buffer__loop:
    ld           (HL), A
    inc          HL
    and          A
    sbc          HL, BC
    add          HL, BC
    jp           NZ, clear_buffer__loop

    ex           DE, HL
    jp           (HL)



set_mode:
    pop         HL
    pop         AF
    cp          A, regTrue
    jp          Z, text_mode
draw_mode:
    ld          A, $27
    ld          ($E30018), A
    jp          (HL)
text_mode:
    ld          A, $2D
    ld          ($E30018), A
    jp          (HL)


write_buffer:
    ld           BC, 76800
    ld           HL, BUFFER_B1
    ld           DE, VRAM_B1
    LDIR
    ret
